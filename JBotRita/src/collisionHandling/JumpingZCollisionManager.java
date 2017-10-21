package collisionHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.naming.InitialContext;

import physicalobjects.WallWithZ;
import mathutils.Vector2d;
import robots.JumpingRobot;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;

public class JumpingZCollisionManager extends SimpleCollisionManager {
	private Vector2d oldPos = null;
	private Vector2d robotPos = null;
	private boolean robotHasOldPos = false;

	public JumpingZCollisionManager(Simulator simulator) {
		super(simulator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void handleCollisions(Environment environment, double time) {

		for (Robot r : environment.getRobots()) {
			r.getCollidingObjects().clear();
		}

		for (MovableObject mo : environment.getMovableObjects()) {
			mo.setInvolvedInCollison(false);
			mo.setInvolvedInCollisonWall(false);
			mo.shape.computeNewPositionAndOrientationFromParent();
			mo.shape.getCloseRobot().update(time, environment.getTeleported());
			mo.shape.getClosePrey().update(time, environment.getTeleported());
			mo.shape.getCloseWalls().update(time, environment.getTeleported());
		}

		Vector2d temp = new Vector2d();

		// robot - robot collisions
		for (Robot robot : environment.getRobots()) {
			ClosePhysicalObjects closeRobots = robot.shape.getCloseRobot();
			CloseObjectIterator iterator = closeRobots.iterator();
			while (iterator.hasNext()) {
				PhysicalObjectDistance closeRobot = iterator.next();
				temp.set(closeRobot.getObject().getPosition());
				temp.sub(robot.getPosition());
				double length = temp.length() - robot.getRadius()
						- closeRobot.getObject().getRadius();

				if (length < 0) {
					setLength(temp, length / 2);
					robot.move(temp);
					temp.negate();
					((MovableObject) closeRobot.getObject()).move(temp);
					if (!robot.ignoreRobotToRobotCollisions()) {
						robot.setInvolvedInCollison(true);
						robot.getCollidingObjects().add(closeRobot.getObject());
						((Robot) closeRobot.getObject()).getCollidingObjects()
								.add(robot);
						closeRobot.getObject().setInvolvedInCollison(true);
					}

				} else {

					iterator.updateCurrentDistance(length);
				}
			}
		}
		// robot - wall
		for (Robot robot : environment.getRobots()) {
			ClosePhysicalObjects closeWalls = robot.shape.getCloseWalls();
			CloseObjectIterator iterator = closeWalls.iterator();
			while (iterator.hasNext()) {

				Wall closeWall = (Wall) (iterator.next().getObject());

				int status = checkIfCollided(closeWall, robot);
				if (status != -1) {
					if (robot instanceof JumpingRobot) {
						JumpingRobot myRobot = ((JumpingRobot) robot);
						if (myRobot.ignoreWallCollisions()) {
							//System.out.println("entrei na colisão com parede em ignore");
							if (closeWall instanceof WallWithZ) {
								//System.out.println("instanciaWallWithZ");
								//System.out.println("height"+myRobot.getHeight());

								if (((WallWithZ) closeWall).getHeightZ() > myRobot
										.getHeight()) {
									//System.out.println("a parede é maior");
									myRobot.stopJumping();
									handle_RobotWithWall_Collisiion(robot,
											closeWall, status);
								}else{
									//System.out.println("saltei");
								}

							}
						} else {
							//System.out.println("anão cheguei a ser ignore, para sequer ver a colisão");

							myRobot.stopJumping();
							handle_RobotWithWall_Collisiion(robot, closeWall,
									status);
						}
					} else {
						//System.out.println("não sou robot saltitante");

						handle_RobotWithWall_Collisiion(robot, closeWall,
								status);
					}

				}

			}

		}
		// prey - wall
		for (Prey prey : environment.getPrey()) {
			ClosePhysicalObjects closeWalls = prey.shape.getCloseWalls();
			CloseObjectIterator iterator = closeWalls.iterator();
			while (iterator.hasNext()) {
				Wall closeWall = (Wall) (iterator.next().getObject());
				int status = checkIfCollided(closeWall, prey);
				if (status != -1) {
					Vector2d newPosition = handleCollision(prey, closeWall,
							status);
					prey.moveTo(newPosition);
					prey.setInvolvedInCollison(true);
				}
			}
		}
		// robot - prey collisions
		for (Prey p : environment.getPrey()) {
			if (p.isEnabled()) {
				ClosePhysicalObjects closeRobots = p.shape.getCloseRobot();
				CloseObjectIterator iterator = closeRobots.iterator();

				// if the number of robots touching the prey is less than the
				// mass, make it stay in the same place
				// first, count the robots that are touching
				// then, move the prey
				boolean heavy = false;
				if (p.getMass() > 1) {
					int number = 0;
					while (iterator.hasNext()) {
						Robot r = (Robot) (iterator.next().getObject());
						temp.set(p.getPosition());
						temp.sub(r.getPosition());

						double length = temp.length() - r.getRadius()
								- p.getRadius();
						if (length < 0) {
							number++;
						}
					}
					if (number < p.getMass()) {
						heavy = true;
					}
				}

				iterator = closeRobots.iterator();
				while (iterator.hasNext()) {
					Robot r = (Robot) (iterator.next().getObject());

					temp.set(p.getPosition());
					temp.sub(r.getPosition());

					double length = temp.length() - r.getRadius()
							- p.getRadius();
					if (length < 0) {

						setLength(temp, length / 2);

						r.move(temp);
						temp.negate();
						if (!heavy)
							p.move(temp);
						// robot.setInvolvedInCollison(true);
						// closePrey.setInvolvedInCollison(true);
					} else {
						iterator.updateCurrentDistance(length);
					}
				}
			}
		}
		/*
		 * // robot - prey collisions for (Robot robot :
		 * environment.getRobots()) { ClosePhysicalObjects closePreys =
		 * robot.shape.getClosePrey(); CloseObjectIterator iterator =
		 * closePreys.iterator(); // closePreys.debugInfo(); while
		 * (iterator.hasNext()) { Prey closePrey = (Prey)
		 * (iterator.next().getObject()); if (closePrey.isEnabled()) {
		 * temp.set(closePrey.getPosition()); temp.sub(robot.getPosition());
		 * Vector2d temp2 = new Vector2d(temp);
		 * 
		 * double length = temp.length() - robot.getRadius() -
		 * closePrey.getRadius(); if (length < 0) {
		 * 
		 * double div = 2; double mult = 1;
		 * 
		 * setLength(temp, length/2); setLength(temp2, length/2);
		 * robot.move(temp); temp2.negate(); // if(closePrey.getMass() !=
		 * Double.MAX_VALUE) closePrey.move(temp2); //
		 * robot.setInvolvedInCollison(true); //
		 * closePrey.setInvolvedInCollison(true); } else {
		 * iterator.updateCurrentDistance(length); } } } }
		 */

		// prey - prey collisions
		for (Prey prey : environment.getPrey()) {
			if (prey.isEnabled()) {
				ClosePhysicalObjects closePreys = prey.shape.getClosePrey();
				CloseObjectIterator iterator = closePreys.iterator();
				while (iterator.hasNext()) {
					PhysicalObjectDistance closePrey = iterator.next();
					if (closePrey.getObject().isEnabled()) {
						temp.set(closePrey.getObject().getPosition());
						temp.sub(prey.getPosition());
						double tempLength = temp.length();
						double length = tempLength - prey.getDiameter();

						if (length < 0) {
							if (tempLength > 0) {
								setLength(temp, length / 2);
							} else {
								temp.set(0, length / 2);
								temp.rotate(simulator.getRandom()
										.nextGaussian() * Math.PI);
							}
							prey.move(temp);
							temp.negate();
							((MovableObject) closePrey.getObject()).move(temp);
							// prey.setInvolvedInCollison(true);
							// closePrey.getObject().setInvolvedInCollison(true);
						} else {
							iterator.updateCurrentDistance(length);
						}
					}
				}
			}
		}
	}

	private void handle_RobotWithWall_Collisiion(Robot robot, Wall closeWall,
			int status) {
		Vector2d newPosition = handleCollision(robot, closeWall, status);
		robot.moveTo(newPosition);
		robot.setInvolvedInCollison(true);
		robot.setInvolvedInCollisonWall(true);

		robot.getCollidingObjects().add(closeWall);

		if (robot.specialWallCollisions()) {
			DifferentialDriveRobot rr = (DifferentialDriveRobot) robot;
			rr.setWheelSpeed(rr.getLeftWheelSpeed() * 0.5,
					rr.getRightWheelSpeed() * 0.5);

		}
	}

}
