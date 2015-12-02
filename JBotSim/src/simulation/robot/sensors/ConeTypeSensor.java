	package simulation.robot.sensors;

import java.util.ArrayList;
import java.util.Random;

import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowObstacleChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public abstract class ConeTypeSensor extends Sensor {

	public static final float NOISESTDEV = 0.05f; 
	public static final int DEFAULT_RANGE = 1;

	protected ClosePhysicalObjects closeObjects;
	@ArgumentsAnnotation(name = "range", defaultValue = "1")
	protected double 			   range;
	protected double			   cutOff;
	protected double[]             readings;
	protected double[] 			   angles;
	protected double[] 			   originalAngles;
	@ArgumentsAnnotation(name = "forcesensorposition", help="Value of the angle for the robot sensor.", defaultValue = "1")
	protected double               angleposition;
	@ArgumentsAnnotation(name = "numbersensors", defaultValue = "1")
	protected int 				   numberOfSensors;
	protected Vector2d 			   sensorPosition 	= new Vector2d();
	@ArgumentsAnnotation(name = "angle", defaultValue = "90")
	protected double 			   openingAngle = 90;

	protected Environment env;
	protected Double time;
	protected GeometricCalculator geoCalc;
	protected Random random;
	
	protected ClosePhysicalObjects closeObstacles;
	@ArgumentsAnnotation(name = "checkobstacles", values={"0","1"})
	protected boolean checkObstacles = false;
	protected double[] obstacleReadings;
	
	@ArgumentsAnnotation(name = "evolvablerange", values={"0","1"})
	protected boolean evolvableRange = false;
	@ArgumentsAnnotation(name = "evolvableangle", values={"0","1"})
	protected boolean evolvableOpeningAngle = false;
	@ArgumentsAnnotation(name = "eyes", values={"0","1"})
	protected boolean eyes = false;
	@ArgumentsAnnotation(name = "eyesfront&back", help="Set to 1 to place one sensor on the front of the robot and another on the back.", values={"0","1"})
	protected boolean eyesFrontBack = false;
	@ArgumentsAnnotation(name = "epucksensorsposition", values={"0","1"})
	protected boolean epuckSensorsPosition = false;
	
	protected double initialRange = 0;
	protected double initialOpeningAngle = 0;
	@ArgumentsAnnotation(name = "eyesangle", help="Value of the distance between the two front sensors of the robot.", defaultValue = "15")
	protected int eyesAngle = 15;
	
	@ArgumentsAnnotation(name = "binary", help="", defaultValue = "0")
	protected boolean binary = false;
	
	public ConeTypeSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id, robot, args);
		this.geoCalc = new GeometricCalculator();//simulator.getGeoCalculator();
		this.env = simulator.getEnvironment();
		this.time = simulator.getTime();
		this.random = simulator.getRandom();
		
		numberOfSensors = (args.getArgumentIsDefined("numbersensors")) ? args.getArgumentAsInt("numbersensors") : 1;
		range = (args.getArgumentIsDefined("range")) ? args.getArgumentAsDouble("range") : 1;
		openingAngle = Math.toRadians((args.getArgumentIsDefined("angle")) ? args.getArgumentAsDouble("angle") : 90);
		
		checkObstacles = args.getArgumentAsIntOrSetDefault("checkobstacles",0) == 1;
		evolvableRange = args.getArgumentAsIntOrSetDefault("evolvablerange",0) == 1; 
		evolvableOpeningAngle = args.getArgumentAsIntOrSetDefault("evolvableangle",0) == 1;
		
		if (args.getArgumentAsIntOrSetDefault("evolvable",0) == 1){
			evolvableRange = true; 
			evolvableOpeningAngle = true;
		}
		
		angleposition = args.getArgumentAsDoubleOrSetDefault("forcesensorposition", -1);
		eyes = args.getArgumentAsIntOrSetDefault("eyes", 0) == 1;
		eyesFrontBack = args.getArgumentAsIntOrSetDefault("eyesfront&back", 0) == 1;
		eyesAngle = args.getArgumentAsIntOrSetDefault("eyesangle", eyesAngle);
		epuckSensorsPosition = args.getArgumentAsIntOrSetDefault("epucksensorsposition", 0) == 1;
		binary = args.getFlagIsTrue("binary");

		cutOff = range;
		
		this.readings 		= new double[numberOfSensors];
		this.angles 		= new double[numberOfSensors];
		
		if(eyes){
			angles[0]= FastMath.toRadians(eyesAngle);
			angles[1]= FastMath.toRadians(360-eyesAngle);
		}else if(eyesFrontBack){
			angles[0]= FastMath.toRadians(0);
			angles[1]= FastMath.toRadians(180);
		}else if(epuckSensorsPosition){
			angles[0]= FastMath.toRadians(17);
			angles[1]= FastMath.toRadians(90);
			angles[2]= FastMath.toRadians(270);
			angles[3]= FastMath.toRadians(343);
		}else if(angleposition < 0)
			setupPositions(numberOfSensors, args);
		else
			angles[0] = FastMath.toRadians(angleposition);
		
		if(checkObstacles) {
			setAllowedObstaclesChecker(new AllowObstacleChecker(robot.getId()*100));
			this.obstacleReadings = new double[numberOfSensors];
		}
		
		this.originalAngles = angles.clone();
		
		initialRange = range;
		initialOpeningAngle = openingAngle;
	}
	
	public void setAllowedObstaclesChecker(AllowedObjectsChecker aoc) {
		if(aoc != null)
			this.closeObstacles = new ClosePhysicalObjects(env,range,aoc);
	}
	
	public void setAllowedObjectsChecker(AllowedObjectsChecker aoc) {
		if(aoc != null)
			this.closeObjects 	= new ClosePhysicalObjects(env,range,aoc);
	}
	
	public void setupPositions(Vector2d[] positions) {
		Vector2d frontVector = new Vector2d(1,0); 
		for (int i=0;i< numberOfSensors;i++){
			Vector2d v = positions[i];
			angles[i] = (v.getY()<0?-1:1)*v.angle(frontVector);
		}
	}
	
	public void setupPositions(int numberSensors, Arguments args) {
		
		if(args.getArgumentIsDefined("angles")) {
			
			String[] split =args.getArgumentAsString("angles").split(","); 
			for (int i = 0; i < numberSensors; i++){
				angles[i] = Math.toRadians(Double.parseDouble(split[i]));
			}
		} else {
			double delta = 2 * Math.PI / numberSensors;
			double angle = 0;
			for (int i = 0; i < numberSensors; i++){
				angles[i] = angle;
				angle+=delta;
			}
		}
	}

	public double[] getAngles() {
		return angles;
	}
	
	public double[] getSensorsOrientations() {
		return angles;
	}
	
	public double[] getOriginalAngles() {
		return originalAngles;
	}
	
	public ClosePhysicalObjects getCloseObjects() {
		return closeObjects;
	}

	public double getRange() {
		return range;
	}
	
	public double getOpeningAngle() {
		return openingAngle;
	}
	
	public double getCutOff() {
		return cutOff;
	}
	
	public void setCutOff(double cutOff) {
		this.cutOff = cutOff;
	}

	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		if(closeObjects != null)
			closeObjects.update(time, teleported);
//		if(robot.getId()==0)
//			System.out.println(closeObjects.getNumberOfCloseObjects()+" "+time);
		try { 
			for(int j = 0; j < readings.length; j++){
				readings[j] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if(source.getObject().isInvisible())
					continue;
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(sensorPosition, source.getObject(), time));
				}
			}
			
			if(checkObstacles)
				checkObstacles(time, teleported);
			
			if(binary) {
				for(int j = 0; j < readings.length; j++){
					readings[j] = readings[j] != 0 ? 1.0 : 0.0;
				}
			}

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	protected void checkObstacles(double time, ArrayList<PhysicalObject> teleported) {
		for(int j = 0; j < obstacleReadings.length; j++){
			obstacleReadings[j] = 0.0;
		}
		
		if(closeObstacles != null)
			closeObstacles.update(time, teleported);
		CloseObjectIterator iterator = closeObstacles.iterator();
		while(iterator.hasNext()){
			PhysicalObjectDistance source=iterator.next();
			if (source.getObject().isEnabled()){
				calculatedObstacleContributions(source);
				iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(sensorPosition, source.getObject(), time));
			}
		}
	}

	public double getSensorReading(int sensorNumber){
		return readings[sensorNumber];
	}
	
	protected void calculatedObstacleContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < obstacleReadings.length; j++){
			obstacleReadings[j] = FastMath.max(calculateContributionToSensor(j, source)*1/*(1 + random.nextGaussian()* NOISESTDEV)*/, readings[j]);
		}
	}

	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < readings.length; j++){
			if(openingAngle > 0.018){ //1degree
				readings[j] = FastMath.max(calculateContributionToSensor(j, source)*1/*(1 + 
						random.nextGaussian()* NOISESTDEV)*/, readings[j]);
			}
		}
	}

	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			PhysicalObjectDistance source) {
		double orientation=angles[sensorNumber]+robot.getOrientation();
//		sensorPosition.set(Math.cos(orientation)*robot.getRadius()+robot.getPosition().getX(),
//				Math.sin(orientation)*robot.getRadius()+robot.getPosition().getY());
		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetween(sensorPosition, 
				orientation, source.getObject(), time);
		return sensorInfo;
	}

	protected GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d toPoint){
		double orientation=angles[sensorNumber]+robot.getOrientation();
		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(
				sensorPosition, orientation, toPoint, time);
		return sensorInfo;
	}
	
	protected abstract double calculateContributionToSensor(int i, PhysicalObjectDistance source);

	public int getNumberOfSensors() {
		return numberOfSensors;
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public void setOpeningAngle(double openingAngle) {
		this.openingAngle = openingAngle;
	}
	
	@Override
	public int getNumberExtraParameters() {
		
		int extraParams = 0;
		
		if(evolvableRange)
			extraParams++;
		if(evolvableOpeningAngle)
			extraParams++;
			
		return extraParams;
	}
	
	@Override
	public void setExtraParameters(double[] parameters) {
		
		int param = 0;
		
		if(evolvableRange) {
			setRange(normalize(parameters[param])*initialRange);
			setCutOff(normalize(parameters[param])*initialRange);
			param++;
		}
		if(evolvableOpeningAngle){
//			System.out.println("RANGE: "+ range);
			setOpeningAngle(normalize(parameters[param])*initialOpeningAngle);
//			System.out.println("ANGLE: "+ Math.toDegrees(openingAngle));
			param++;
		}
	}
	
	public double normalize(double parameter) {
		//from [-10;10] to [0;1]
		return (parameter+10.0)/20.0;
	}
	
	public double getInitialOpeningAngle() {
		return initialOpeningAngle;
	}
	
	public double getInitialRange() {
		return initialRange;
	}

}