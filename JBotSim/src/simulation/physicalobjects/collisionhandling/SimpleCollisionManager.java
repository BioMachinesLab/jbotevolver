package simulation.physicalobjects.collisionhandling;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CollisionManager;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;

public class SimpleCollisionManager extends CollisionManager {

	public SimpleCollisionManager(Simulator simulator) {
		super(simulator);
	}

	@Override
	public void handleCollisions(Environment environment, int time) {

		for (MovableObject mo : environment.getMovableObjects()) {
			mo.setInvolvedInCollison(false);
			mo.shape.computeNewPositionAndOrientationFromParent();
			mo.shape.getCloseRobot().update(time, environment.getTeleported());
			mo.shape.getClosePrey().update(time, environment.getTeleported());
			mo.shape.getCloseWalls().update(time, environment.getTeleported());
		}

		Vector2d temp = new Vector2d();

		//robot - robot collisions 
		for (Robot robot : environment.getRobots()) {
			ClosePhysicalObjects closeRobots = robot.shape.getCloseRobot();
			CloseObjectIterator iterator = closeRobots.iterator();  
			while(iterator.hasNext()){
				PhysicalObjectDistance closeRobot = iterator.next();
				temp.set(closeRobot.getObject().getPosition());
				temp.sub(robot.getPosition());
				double length = temp.length() - robot.getRadius() - closeRobot.getObject().getRadius();

				if (length < 0) {
					temp.setLength(length / 2);
					robot.move(temp); 
					temp.negate();
					((MovableObject) closeRobot.getObject()).move(temp);
					robot.setInvolvedInCollison(true);
					closeRobot.getObject().setInvolvedInCollison(true);

				} else {

					iterator.updateCurrentDistance(length);
				}
			}
		}

		for (Robot robot : environment.getRobots()) {

			ClosePhysicalObjects closeWalls = robot.shape.getCloseWalls();
			CloseObjectIterator iterator = closeWalls.iterator();  
			while(iterator.hasNext()){
				
				Wall closeWall = (Wall)(iterator.next().getObject());
				int status = checkIfCollided(closeWall, robot);
				if(status != -1){
					Vector2d newPosition = handleCollision(robot, closeWall, status);
					robot.moveTo(newPosition);
					robot.setInvolvedInCollison(true);
				}
			}
		}

		//robot - prey collisions 
		for (Robot robot : environment.getRobots()) {
			ClosePhysicalObjects closePreys = robot.shape.getClosePrey();
			CloseObjectIterator iterator = closePreys.iterator();  
			//			closePreys.debugInfo();
			while(iterator.hasNext()){
				Prey closePrey = (Prey)(iterator.next().getObject());
				if(closePrey.isEnabled()){
					temp.set(closePrey.getPosition());
					temp.sub(robot.getPosition());
					double length = temp.length() - robot.getRadius() - closePrey.getRadius();

					if (length < 0) {
						temp.setLength(length / 2);
						robot.move(temp); 
						temp.negate();
						closePrey.move(temp);	
						//						robot.setInvolvedInCollison(true);
						//						closePrey.setInvolvedInCollison(true);
					} else {
						iterator.updateCurrentDistance(length);
					}
				}
			}
		}

		//prey - prey collisions
		for (Prey prey : environment.getPrey()) {
			if(prey.isEnabled()){
				ClosePhysicalObjects closePreys = prey.shape.getClosePrey();
				CloseObjectIterator iterator = closePreys.iterator();  
				while(iterator.hasNext()){
					PhysicalObjectDistance closePrey = iterator.next();
					if(closePrey.getObject().isEnabled()){
						temp.set(closePrey.getObject().getPosition());
						temp.sub(prey.getPosition());
						double tempLength = temp.length();
						double length = tempLength - prey.getDiameter();

						if (length < 0) {
							if ( tempLength > 0) {
								temp.setLength(length / 2);
							} else {
								temp.set(0, length / 2);
								temp.rotate(simulator.getRandom().nextGaussian()*Math.PI);
							}
							prey.move(temp); 
							temp.negate();
							((MovableObject) closePrey.getObject()).move(temp);	
							//					prey.setInvolvedInCollison(true);
							//					closePrey.getObject().setInvolvedInCollison(true);
						} else {
							iterator.updateCurrentDistance(length);
						}
					}
				}
			}
		}
	}

	private Vector2d handleCollision(Robot robot, Wall wall, int collisionStatus) {
		
		double valueX = robot.getPosition().getX(), valueY = robot.getPosition().getY();
		switch(collisionStatus){
		//robot comes from the right
		case 0:
			valueX = wall.getTopLeftX() + wall.getWidth() + robot.getRadius();
			break;
			//from the left
		case 1:
			valueX = wall.getTopLeftX() - robot.getRadius();
			break;
			//from above
		case 2:
			valueY = wall.getTopLeftY() + robot.getRadius();
			break;
			//from below.
		case 3:
			valueY = wall.getTopLeftY() - wall.getHeight();
			break;
		}

		return new Vector2d(valueX, valueY);
	}

	private int checkIfCollided(Wall closeWall, Robot robot) {

		Vector2d topLeft = new Vector2d(closeWall.getTopLeftX(), closeWall.getTopLeftY()),
		topRight = new Vector2d(closeWall.getTopLeftX() + closeWall.getWidth(), closeWall.getTopLeftY()),
		bottomLeft = new Vector2d(closeWall.getTopLeftX(), closeWall.getTopLeftY() - closeWall.getHeight()),
		bottomRight = new Vector2d(closeWall.getTopLeftX() + closeWall.getWidth(), 
				closeWall.getTopLeftY() - closeWall.getHeight());
		
		if(simulation.util.MathUtils.distanceBetween(topRight, bottomRight, robot.getPosition()) <= robot.getRadius()){
			//System.out.println("robot from right");
			return 0;
		}
		if(simulation.util.MathUtils.distanceBetween(topLeft, bottomLeft, robot.getPosition()) <= robot.getRadius()){
			//System.out.println("robot from left");
			return 1;
		}
		if(simulation.util.MathUtils.distanceBetween(topRight, topLeft, robot.getPosition()) <= robot.getRadius()){
			//System.out.println("robot from above");
			return 2;
		}
		if(simulation.util.MathUtils.distanceBetween(bottomRight, bottomLeft, robot.getPosition()) <= robot.getRadius()){
			//System.out.println("robot from below");
			return 3;
		}
		
		if(robot.getPosition().x > topLeft.x && robot.getPosition().x < topRight.x) {
			if(robot.getPosition().y < topLeft.y && robot.getPosition().y > bottomLeft.y) {
				return 4;
			}
		}
		

		return -1;
	}
}
