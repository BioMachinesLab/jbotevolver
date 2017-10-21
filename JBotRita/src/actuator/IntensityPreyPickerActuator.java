package actuator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import robots.JumpingRobot;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class IntensityPreyPickerActuator extends Jump_IntensityPreyPickerActuator {
	
	private boolean cannotPick = false;

	public IntensityPreyPickerActuator(Simulator simulator, int id,
			Arguments args) {
		super(simulator, id, args);
	}

	//in case there are obstacles between the robot and the prey, the robot can't pick it.
	@Override
	public void apply(Robot robot,double timeDelta) {
		if(isToPick){
			super.findBestPrey(robot);
			if (bestPrey != null) {

				if (robot instanceof JumpingSumo) {
					if (!((JumpingSumo) robot).ignoreWallCollisions()) {
						ClosePhysicalObjects closeWalls = robot.shape
								.getCloseWalls();
						CloseObjectIterator iterator2 = closeWalls.iterator();

						Vector2d robotPosition = robot.getPosition();
						Vector2d bestPreyPosition = bestPrey.getPosition();

						ArrayList<Wall> walls = new ArrayList<Wall>();

						HashMap<Vector2d, Wall> closestDistanceToWall = new HashMap<Vector2d, Wall>();
						LinkedList<Vector2d> distances = new LinkedList<Vector2d>();

						Vector2d pos = robot.getPosition();
						while (iterator2.hasNext()) {
							Wall closeWall = (Wall) (iterator2.next()
									.getObject());

							Vector2d topLeft = new Vector2d(
									closeWall.getTopLeftX(),
									closeWall.getTopLeftY()), topRight = new Vector2d(
									closeWall.getTopLeftX()
											+ closeWall.getWidth(),
									closeWall.getTopLeftY()), bottomLeft = new Vector2d(
									closeWall.getTopLeftX(),
									closeWall.getTopLeftY()
											- closeWall.getHeight()), bottomRight = new Vector2d(
									closeWall.getTopLeftX()
											+ closeWall.getWidth(),
									closeWall.getTopLeftY()
											- closeWall.getHeight());

							Vector2d closestDistance = topLeft;

							if (pos.distanceTo(topRight) < pos
									.distanceTo(closestDistance))
								closestDistance = topRight;
							if (pos.distanceTo(bottomLeft) < pos
									.distanceTo(closestDistance))
								closestDistance = bottomLeft;
							if (pos.distanceTo(bottomRight) < pos
									.distanceTo(closestDistance))
								closestDistance = bottomRight;

							closestDistanceToWall.put(closestDistance,
									closeWall);

							distances.add(closestDistance);

						}

						int lenght = distances.size();

						for (int a = 0; a < lenght; a++) {
							Vector2d closestDistance = distances.get(0);
							int z = 0;
							for (int i = 1; i < distances.size(); i++) {
								if (pos.distanceTo(distances.get(i)) < pos
										.distanceTo(closestDistance)) {
									closestDistance = distances.get(i);
									z = i;
								}
							}
							walls.add(closestDistanceToWall
									.get(closestDistance));
							if (distances.size() >= 2)
								distances.remove(z);
						}

						for (Wall closeWall : walls) {
							if (robotPosition.x != bestPreyPosition.x) {
								double m = (robotPosition.y - bestPreyPosition.y)
										/ (robotPosition.x - bestPreyPosition.x);
								double b = robotPosition.y - m
										* robotPosition.x;

								Vector2d topLeft = new Vector2d(
										closeWall.getTopLeftX(),
										closeWall.getTopLeftY()), topRight = new Vector2d(
										closeWall.getTopLeftX()
												+ closeWall.getWidth(),
										closeWall.getTopLeftY()), bottomLeft = new Vector2d(
										closeWall.getTopLeftX(),
										closeWall.getTopLeftY()
												- closeWall.getHeight()), bottomRight = new Vector2d(
										closeWall.getTopLeftX()
												+ closeWall.getWidth(),
										closeWall.getTopLeftY()
												- closeWall.getHeight());

								if (closeWall.getHeight() > closeWall
										.getWidth()) { // vertical
									double x_intersection;

									if (robot.getPosition().x < bestPreyPosition.x) {
										x_intersection = topRight.x;
									} else {
										x_intersection = topLeft.x;
									}
									double y_intersection = m * x_intersection
											+ b;
									
									if (((x_intersection >= robotPosition.x
											&& x_intersection <= bestPreyPosition.x
											&& y_intersection >= bottomLeft.y
											&& y_intersection <= topLeft.y && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y))) || (x_intersection >= bestPreyPosition.x
											&& x_intersection <= robotPosition.x
											&& y_intersection >= bottomLeft.y
											&& y_intersection <= topLeft.y && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y))))) {
										cannotPick = true;
										break;
									}
								} else { // horizontal
									
									double y_intersection;
									if (robot.getPosition().y < bestPreyPosition.y) {
										y_intersection = closeWall
												.getTopLeftY();
									} else {
										y_intersection = bottomLeft.y;

									}

									double x_intersection = (y_intersection - b)
											/ m;
									
									if (((x_intersection >= robotPosition.x
											&& x_intersection <= bestPreyPosition.x
											&& x_intersection >= bottomLeft.x
											&& x_intersection <= topRight.x && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y))) || (x_intersection >= bestPreyPosition.x
											&& x_intersection <= robotPosition.x
											&& x_intersection >= bottomLeft.x
											&& x_intersection <= topRight.x && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y))))) {
										cannotPick = true;
										break;
									}
								}

							} else {

							
								Vector2d topLeft = new Vector2d(
										closeWall.getTopLeftX(),
										closeWall.getTopLeftY()), topRight = new Vector2d(
										closeWall.getTopLeftX()
												+ closeWall.getWidth(),
										closeWall.getTopLeftY()), bottomLeft = new Vector2d(
										closeWall.getTopLeftX(),
										closeWall.getTopLeftY()
												- closeWall.getHeight()), bottomRight = new Vector2d(
										closeWall.getTopLeftX()
												+ closeWall.getWidth(),
										closeWall.getTopLeftY()
												- closeWall.getHeight());
								

								double y_intersection;
								if (robot.getPosition().y < bestPreyPosition.y) {
									y_intersection = closeWall.getTopLeftY();
								} else {
									y_intersection = bottomLeft.y;

								}
								if ((robotPosition.x >= topLeft.x
										&& robotPosition.x <= topRight.x && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y)))) {
									cannotPick = true;
									break;

								}

							}
							
							if(cannotPick == true)
								break;
						}
						if (walls.size() >= 1) {
							if (cannotPick == false){
								pickUpPrey(robot, bestPrey);
								
							}
							else{
								cannotPick = false;
							}

						} else {
							pickUpPrey(robot, bestPrey);
						}

					}
				} else {
					pickUpPrey(robot, bestPrey);
				}
			}
		

	}
	}
	

}

