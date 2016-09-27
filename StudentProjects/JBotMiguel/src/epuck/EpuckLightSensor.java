package epuck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;

/**
 * Sensors that mimic those of the e-puck. More information here:
 * http://www.e-puck
 * .org/index.php?option=com_content&view=article&id=22&Itemid=13
 * 
 * @author miguelduarte
 */

public class EpuckLightSensor extends LightTypeSensor {

	private static final long serialVersionUID = 3083908290165398312L;
	
	private static double RANGE = 50;
	private static double REAL_RANGE = 0.14;
	private int[] chosenReferences;
	private int[] currentFixedTimeSteps;
	private int fixedTimeSteps =  0;
	private int[] lastTimeStep;
	private int[] ignoredLights = new int[2];
	private int ignoredLightsIndex = 0;
	
	static double sensorReferenceValues[][] = {
        {2264.67,3055.55,3463.21,3667.23,3785.69,3865.43,3897.8,3916.48,3930.3,3937.47,3940.65,3945.53,3947.79,3949.4,3950.57,3960.65},
        {2136.66,3123.69,3573.17,3738.73,3798.88,3885.31,3921.55,3944.63,3959.92,3969.24,3976.75,3980.37,3984.64,3986.82,3989.42,4001.31}
	};
	
	static double sensorReferenceStdDeviation[][] = {
		{962.8849054,548.247834,302.5840146,182.8446256,109.8660726,59.38354233,37.36816827,24.42641193,17.49199817,13.31349316,10.33090025,7.993065745,6.376982045,5.375872022,4.591851478,0.476969601},
		{1071.826182,501.4138749,265.3489798,166.4808611,124.0827369,70.94993939,46.21003679,33.0955148,23.44426582,17.95278251,13.7349008,11.11274494,8.74244817,7.444971457,5.9969659,0.462493243}
	};
	
	static double distances[] = {0,0.5,1,1.5,2,3,4,5,6,7,8,9,10,11,12,13};

	double last0 = 0;
	double last1 = 0;
	
	double lastNetworkInputValue[];
	double maximumNNInputValueDrop  = 0;

	protected Random random;
	
//	public EpuckLightSensor(Simulator simulator,int id, Robot robot, Vector2d [] positions, 
//			double openingAngle, double range, double maximumNNInputValueDrop, AllowedObjectsChecker allowedSources, int fixedTimeSteps) {
//		super(simulator, id, robot, positions, openingAngle, RANGE, allowedSources);
	public EpuckLightSensor(Simulator simulator,int id, Robot robot, Arguments arguments) {
		super(simulator,id,robot,arguments);
		setupPositions(2,Math.toRadians(90));
		
		((Epuck) robot).setLightSensor(this);
		
		this.random = simulator.getRandom();
				
		chosenReferences = new int[numberOfSensors];
		
		for(int i = 0 ; i < numberOfSensors ; i++) {
			int random = simulator.getRandom().nextInt(sensorReferenceValues.length);
			chosenReferences[i] = random;
		}
		
		lastNetworkInputValue = new double[numberOfSensors];
		this.maximumNNInputValueDrop = arguments.getArgumentIsDefined("maxvaluedrop") ? arguments.getArgumentAsDouble("maxvaluedrop") : 1.0;
		this.fixedTimeSteps = arguments.getArgumentIsDefined("fixedvalue") ? arguments.getArgumentAsInt("fixedvalue") : 0;	
		currentFixedTimeSteps = new int[numberOfSensors];
		lastTimeStep = new int[numberOfSensors];
		
		setAllowedObjectsChecker(new AllowLightChecker());
	}
	
	public void setupPositions(int numberSensors, double offset) {
		double delta = 2 * Math.PI / numberSensors;
		double angle = offset;
		for (int i=0;i< numberSensors;i++){
			angles[i] = angle;
			angle+=delta;
		}
	}
	
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++){
			readings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		
		LightPole l = (LightPole)source.getObject();
		
		boolean ignoredLight = false;
		
		for(int i = 0 ; i < ignoredLights.length ; i++) {
			if(l.getId() == ignoredLights[i]) {
				ignoredLight = true;
				break;
			}
		}
		
		double result = 0;
		double sensorValue = 0;
		double inputValue = 0;
		
		if(l.isTurnedOn()) {
			GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source.getObject().getPosition());
			
			double distance = sensorInfo.getDistance();
			
			if((sensorInfo.getAngle()< (openingAngle)) && (sensorInfo.getAngle()>(-openingAngle))){
				if (distance < REAL_RANGE) {
					sensorValue = distanceToSensor(distance, sensorNumber);
					inputValue = sensorToInput(sensorValue, sensorNumber);
				}
			}
		}
		
		if(sensorValue == 0 && inputValue == 0) {
			//necessary to drop the existing value
			inputValue = sensorToInput(sensorReferenceValues[chosenReferences[sensorNumber]][sensorReferenceValues.length-1]*2,sensorNumber);
		}
		
		result = inputValue;
		
		if(ignoredLight)
			result = 0;
		else if(result == 1)
			ignoredLights[ignoredLightsIndex++] = l.getId();
		
		if(sensorNumber==0) last0 = result;
		if(sensorNumber==1) last1 = result;
		
		return result;
	}
	
	private double distanceToSensor(double distance, int sensorNumber) {

		distance*=100;//0.01 in the simulator is 1cm. We use the value 1 as 1cm
		
		double[] sensorReferences = sensorReferenceValues[chosenReferences[sensorNumber]];
		double[] stdDeviations = sensorReferenceStdDeviation[chosenReferences[sensorNumber]];
		
		double result = 0.0;
		
		if(distance < distances[distances.length-1]) {

	        int i;
	        for(i = 0; i < distances.length ; i++) {
	            if(distance <= distances[i]) {
	                if(i == 0) {
	                    result = sensorReferences[i];
	                    break;
	                }
	                
	                double distanceBefore = distances[i-1];
	                double distanceAfter = distances[i];
	                
	                double linearization = (distance - distanceBefore)/(distanceAfter - distanceBefore);
	                
	                double valueAfter = sensorReferences[i];
	                double valueBefore = sensorReferences[i-1];
	                double currentDistance = (valueAfter-valueBefore)*linearization+valueBefore;
	                
	                result = currentDistance + random.nextGaussian() * (int)stdDeviations[i];
	                break;
	            }
	        }
	    }
		
		return result;
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		if(closeObjects != null)
			closeObjects.update(time, teleported);
		try { 
			for(int j = 0; j < numberOfSensors; j++){
				readings[j] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
//					iterator.updateCurrentDistance(geoCalc.getDistanceBetween(sensorPosition, source.getObject(),time));
				}
			}
			
			for(int j = 0; j < numberOfSensors; j++){

				if(currentFixedTimeSteps[j] < fixedTimeSteps 
						&& (lastNetworkInputValue[j] == 1 || readings[j] == 1)) {
					currentFixedTimeSteps[j]++;
					readings[j] = 1;
				} else {
					currentFixedTimeSteps[j] = 0;
				}
				
				lastNetworkInputValue[j] = readings[j];
			}

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	private double sensorToInput(double value, int sensorNumber) {
		
		double[] sensorReferences = sensorReferenceValues[chosenReferences[sensorNumber]];

		double result = 0.0;

	    if(value < sensorReferences[sensorReferences.length-1]) {

	        int i;
	        for(i = 0; i < sensorReferences.length ; i++) {
	            if(value < sensorReferences[i]) {
	            	if(i == 0) {
	            		result = 1.0;
	            		break;
	            	}

	            	double linearization = (value - sensorReferences[i-1])
	            			/(sensorReferences[i]-sensorReferences[i-1]);

	                double distanceBefore = distances[i-1];
	                double distanceAfter = distances[i];
	                double currentDistance = (distanceAfter-distanceBefore)*linearization+distanceBefore;

	                double division = currentDistance/distances[distances.length-1];

	                result = 1.0-division;
	                break;
	            }
	        }
	    }
	    
	    if(result  > 0.1) result = 1;
		
		if (result < lastNetworkInputValue[sensorNumber] - maximumNNInputValueDrop) {
			result = lastNetworkInputValue[sensorNumber] - maximumNNInputValueDrop;
		}
		
	    return result;
	}
	
	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			Vector2d source) {
		double orientation = angles[sensorNumber] + robot.getOrientation();
		sensorPosition.set(Math.cos(orientation) * robot.getRadius()
				+ robot.getPosition().getX(),
				Math.sin(orientation) * robot.getRadius()
						+ robot.getPosition().getY());

		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, 
				orientation,source, time);

		return sensorInfo;
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "EPuckLightSensors [readings=" + Arrays.toString(readings) + "]";
	}
}
