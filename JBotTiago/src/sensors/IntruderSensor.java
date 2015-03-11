package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class IntruderSensor extends ConeTypeSensor {
	
	private Vector2d estimatedValue;
	private double metersAhead;
	private LinkedList<PhysicalObject> estimatedIntruders;
	private PhysicalObject[] robotsCloseToPrey;
	private ArrayList<Robot> robots;
	@ArgumentsAnnotation(name="sharewith", help="Number of robots that will receive information", defaultValue="3")
	private int numberOfRecivingRobots;
	private Simulator simulator;
	private GeometricCalculator geometricCalculator;
	private int preyId;
	@ArgumentsAnnotation(name="changecolor", help="Set to 1 to let the robot change is body color when seeing a prey.", values={"0","1"})
	private boolean changeColor;
	
	public IntruderSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		setAllowedObjectsChecker(new AllowMouseChecker(id));
		changeColor = args.getArgumentAsIntOrSetDefault("changecolor", 0)==1;
		numberOfRecivingRobots = args.getArgumentAsIntOrSetDefault("sharewith", 3);
		metersAhead = robot.getRadius()*10;
		robots = simulator.getRobots();
		estimatedIntruders = new LinkedList<PhysicalObject>();
		robotsCloseToPrey = new PhysicalObject[numberOfRecivingRobots];
		geometricCalculator = new GeometricCalculator();
		preyId = -1;
	}
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < readings.length; j++){
			if(openingAngle > 0.018){ //1degree
				readings[j] = calculateContributionToSensor(j, source);
			}
		}
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		estimatedIntruders.clear();
		if(preyId != -1)
			clearEstimationFromCloseRobots();		
		
		estimatedValue = null;
		preyId = -1;
		
		super.update(time, teleported);
		
		if(estimatedValue != null){
			sendEstimationToCloseRobots();
			
			if(changeColor){
				RobotRGBColorActuator rgbActuator = (RobotRGBColorActuator) robot.getActuatorByType(RobotRGBColorActuator.class);
				rgbActuator.setAll(1, 0, 0);
			}
			
		}else{
			if(changeColor){
				RobotRGBColorActuator rgbActuator = (RobotRGBColorActuator) robot.getActuatorByType(RobotRGBColorActuator.class);
				rgbActuator.setAll(0, 0, 0);
			}
		}
		
	}

	private void clearEstimationFromCloseRobots() {
		for (int i = 0; i < robotsCloseToPrey.length; i++) {
			if(robotsCloseToPrey[i] != null){
				SharedStateSensor sharedSensor = (SharedStateSensor) ((Robot)robotsCloseToPrey[i]).getSensorByType(SharedStateSensor.class);
				if(sharedSensor == null)
					return;
				sharedSensor.clearEstimations(this.robot.getId(), preyId);
			}
			robotsCloseToPrey[i] = null;
		}
		//apagar o próprio
		SharedStateSensor sharedSensor = (SharedStateSensor) this.robot.getSensorByType(SharedStateSensor.class);
		sharedSensor.clearEstimations(this.robot.getId(), preyId);
	}
	
	private void sendEstimationToCloseRobots() {
		for (int i = 0; i < numberOfRecivingRobots; i++) {
			if(robotsCloseToPrey[i] != null) {
				SharedStateSensor sharedSensor = (SharedStateSensor) ((Robot)robotsCloseToPrey[i]).getSensorByType(SharedStateSensor.class);
				if(sharedSensor == null)
					return;
				sharedSensor.addEstimation(this.robot.getId(),preyId, estimatedValue);
			}
		}
		//enviar para o próprio
		SharedStateSensor sharedSensor = (SharedStateSensor) this.robot.getSensorByType(SharedStateSensor.class);
		sharedSensor.addEstimation(this.robot.getId(), preyId, estimatedValue);
	}

	@Override
	protected double calculateContributionToSensor(int i, PhysicalObjectDistance source) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(i, source);
		
		if((sensorInfo.getDistance() < getCutOff()) && (sensorInfo.getAngle() < (openingAngle / 2.0)) && (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			double orientation = geometricCalculator.getGeometricInfoBetweenPoints(robot.getPosition(), robot.getOrientation(),source.getObject().getPosition(), simulator.getTime()).getAngle();
			
			calculateEstimatedIntruderPosition(i,orientation,source.getObject());
			
			calculateRobotsCloseToPrey((Robot)source.getObject());
			
			preyId = source.getObject().getId();
			
			return ((openingAngle/2) - Math.abs(sensorInfo.getAngle())) / (openingAngle/2);
		}else
			return 0;		
	}

	private void calculateRobotsCloseToPrey(Robot prey) {
		int count = 0;
		int position = 0;
		double biggerDistanceToPrey = 0;
		
		for (Robot robot : robots) {
			if (!robot.getDescription().equals("prey") && robot.getId() != this.robot.getId()) {

				if(count < numberOfRecivingRobots){
					robotsCloseToPrey[count] = robot;
					count ++;
				}else if(robot.getDistanceBetween(prey.getPosition()) < biggerDistanceToPrey)
					robotsCloseToPrey[position] = robot;
				 
				double aux = 0;
				
				for (int j = 0; j < numberOfRecivingRobots; j++) {
					if(robotsCloseToPrey[j] != null){
						double distanceToPrey = robotsCloseToPrey[j].getDistanceBetween(prey.getPosition());
						if(distanceToPrey > aux){
							aux = distanceToPrey;
							position = j;
						}
					}
				}
				
				biggerDistanceToPrey = aux;
			}
		}
	}

	private void calculateEstimatedIntruderPosition(int sensor, double orientation, PhysicalObject obj){
		if(!estimatedIntruders.contains(obj)){
			double alpha = robot.getOrientation() - orientation;
			
			double x = robot.getPosition().x + (FastMath.cosQuick(alpha) * (metersAhead + robot.getRadius()));
			double y = robot.getPosition().y + (FastMath.sinQuick(alpha) * (metersAhead + robot.getRadius()));
			
			if(estimatedValue == null){
				estimatedValue = new Vector2d(x, y);
			}else{
				estimatedValue.setX(x);
				estimatedValue.setY(y);
			}
				
			estimatedIntruders.add(obj);
		}
	}
	
	public boolean foundIntruder() {
		return !estimatedIntruders.isEmpty();
	}

	public LinkedList<PhysicalObject> getEstimatedIntruder() {
		return estimatedIntruders;
	}
	
	public PhysicalObject[] getRobotsCloseToPrey() {
		return robotsCloseToPrey;
	}
	
	public int getNumberOfRecivingRobots() {
		return numberOfRecivingRobots;
	}
	
	public void setNumberOfRecivingRobots(int recivingRobots) {
		this.numberOfRecivingRobots = recivingRobots;
		
		if(recivingRobots > robotsCloseToPrey.length){
			robotsCloseToPrey = new PhysicalObject[recivingRobots];
		}
		
	}
	
}
