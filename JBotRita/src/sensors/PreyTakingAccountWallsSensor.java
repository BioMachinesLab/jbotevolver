package sensors;


import java.util.HashMap;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.sensors.PreySensor;
import simulation.util.Arguments;

public class PreyTakingAccountWallsSensor extends PreySensor{
	private Robot robot;
	private boolean sensorNotAvailable;
	private Simulator simulator;
	private HashMap <Integer, Boolean> sensorsDisabled= new HashMap <Integer, Boolean>() ;
	private int i=0;
	
	public PreyTakingAccountWallsSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.simulator=simulator;
		this.robot=robot;
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
	
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
	
		if((sensorInfo.getDistance() < getCutOff()) && 
		   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
		   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
				ClosePhysicalObjects closeWalls = robot.shape.getCloseWalls();
				CloseObjectIterator iterator2 = closeWalls.iterator();
				Vector2d robotPosition = robot.getPosition();
				
				
				for(Prey bestPrey: simulator.getEnvironment().getPrey()){
					Vector2d bestPreyPosition = bestPrey.getPosition();
			

				while(iterator2.hasNext()) {
					i++;
					Wall closeWall = (Wall) (iterator2.next().getObject());
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
					
					
					if (robotPosition.x != bestPreyPosition.x) {
						double m = (robotPosition.y - bestPreyPosition.y)
								/ (robotPosition.x - bestPreyPosition.x);
						double b = robotPosition.y - m
								* robotPosition.x;

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
								sensorNotAvailable = true;
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
								sensorNotAvailable = true;
								break;
							}
						}

					} else {
	
						double y_intersection;
						if (robot.getPosition().y < bestPreyPosition.y) {
							y_intersection = closeWall.getTopLeftY();
						} else {
							y_intersection = bottomLeft.y;

						}
						if ((robotPosition.x >= topLeft.x
								&& robotPosition.x <= topRight.x && ((y_intersection >= robotPosition.y && y_intersection <= bestPreyPosition.y) || (y_intersection <= robotPosition.y && y_intersection >= bestPreyPosition.y)))) {
							sensorNotAvailable = true;
							break;
						}

					}
					
					if(sensorNotAvailable == true)
						break;
				}
				
				
				if (i>= 1) {
					if (sensorNotAvailable == false){
						sensorsDisabled.put(sensorNumber, false);
						return (getRange() - sensorInfo.getDistance()) / getRange();
					}
					else{
						sensorsDisabled.put(sensorNumber, true);
						sensorNotAvailable = false;
						return 0;
					}

				} else {
					sensorsDisabled.put(sensorNumber, false);
					return (getRange() - sensorInfo.getDistance()) / getRange();
				}

			}
		}
		sensorsDisabled.put(sensorNumber, false);
 		return 0;
	}
	
	
	public boolean getIsDisabled(int numberSensor){
		if(sensorsDisabled.get(numberSensor)==null)
			return false;
		return sensorsDisabled.get(numberSensor);
	}
	
}
