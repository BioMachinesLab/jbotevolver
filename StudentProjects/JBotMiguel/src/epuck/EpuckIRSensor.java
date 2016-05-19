package epuck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.checkers.AllowWallRobotChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

/**
 * Sensors that mimic those of the e-puck. More information here:
 * http://www.e-puck
 * .org/index.php?option=com_content&view=article&id=22&Itemid=13
 * 
 * @author miguelduarte
 */

@SuppressWarnings("serial")
public class EpuckIRSensor extends ConeTypeSensor {

	private static double RANGE = 5;
	private static double REAL_RANGE = 0.14;
	private int[] chosenReferences;
	public double last2 = 0;
	public double last5 = 0;
	private int numberOfRays = 7;
	private boolean noiseEnabled = true;
	private double[][] rayReadings;
	private double offsetNoise = 0;
	private double[] offsetNoises;
	
	private boolean senseRobot = false;
	
	//Epuck 2019, moving wheels, wood, sensors 0,2,5,7
	
	static double sensorAverages[] = {3817.45625,3293.9575,1741.14,897.92,556.1275,215.16375,107.09875,60.41125,39.54625,25.67875,19.43125,14.98,11.05375,8.74625,9.405};
	
	static double sensorReferenceValues[][] = {
		//e-puck 2419
		{3842.55,3774.18,1769.57,979.94,546.55,190.11,99.29,57.8,37.71,23.99,16.61,12.74,10.36,7.99,6.39},//sensor 0
		{3774.7,2169.01,1159.18,700.52,357.89,158.47,73.1,40.68,29.75,22.29,17.39,14.6,11.51,9.82,8.19},//sensor 2
		{3802.84,2903.36,1613.81,855.75,540.96,198.83,115.64,57.55,42.41,30.72,22.55,15.59,13.53,11.62,10.19},//sensor 5
		{3863.45,3712.37,1818.04,838.54,460.32,211.27,95.33,52.03,32.52,16.09,13.62,10.1,6,5.11,5.11},//sensor 7
		//e-puck 2426
		{3818.34,3718.1,1775.67,919.59,667.54,211.8,109.18,61.61,38.91,24.63,18.84,13.72,10.63,7.71,4.34},//sensor 0
		{3776.71,2638.39,1357.47,557.26,490.67,168.25,74.32,43.09,26.08,17.82,11.75,10.93,7.95,6.14,7.1},//sensor 2
		{3809.74,3642.96,1866.72,947.59,673.53,315.55,154.85,83.36,55.58,36.6,29.8,24.23,16.13,13.82,11.91},//sensor 5
		{3851.32,3793.29,2568.66,1384.17,711.56,267.03,135.08,87.17,53.41,33.29,24.89,17.93,12.32,7.76,22.01}//sensor 7
		
	};
	static double sensorReferenceStdDeviation[][] = {
		//e-puck 2419
		{0.638357267,1.505855239,21.55191639,7.138375165,4.9626102,2.148929966,2.475055555,2.306512519,2.169308646,1.802747903,1.399249799,1.23790145,1.187602627,1.526400996,1.028542658},//sensor 0
		{2.156385865,57.9390188,34.71696415,12.09585053,7.678404782,4.687120651,2.586503431,1.974234029,1.818653348,2.060558177,2.31902997,1.496662955,1.85199892,1.107971119,1.579208663},//sensor 2
		{1.230609605,143.2685953,51.37775686,20.82612542,13.3775334,9.120367317,6.671611499,4.479676328,4.467874215,3.583517825,2.882273408,2.642328519,3.598485793,2.599153708,2.575635844},//sensor 5
		{1.444818328,34.18673866,2.763765547,1.627390549,1.215565712,2.576256975,3.484408128,2.34288284,1.479729705,1.312211873,1.362204096,1.170469991,1,0.798686422,0.798686422},//sensor 7
		//e-puck 2426
		{1.115526781,2.479919354,8.171970387,4.876668945,3.40123507,2.391652149,2.878819202,2.176671771,1.903129002,1.978155707,2.452427369,1.783704011,1.604088526,1.893647274,1.320757358},//sensor 0
		{1.022692525,36.07683329,13.3726998,5.12761153,5.734204391,2.947456531,1.896734035,1.887299658,2.062425756,1.458629494,1.620956508,2.035951866,1.961504525,1.356613431,1.846618531},//sensor 2
		{0.576541412,17.24466294,17.28761406,5.649946902,2.376783541,2.527350391,1.840516232,1.10923397,1.106164545,1.264911064,2.529822128,1.605334856,1.85286265,1.423938201,0.980765008},//sensor 5
		{0.597996656,1.210743573,17.71170235,6.827964558,1.070700705,1.431467778,1.61666323,1.99025124,1.342348688,1.313735133,1.287594657,1.282614517,1.325745074,0.53141321,0.727942305}//sensor 7
	};
	
	static double distances[] = {0,0.5,1,1.5,2,3,4,5,6,7,8,9,10,11,12};

	protected Random random;
	private Vector2d[][] cones;
	private Vector2d[] sensorPositions;
	private double fullOpeningAngle;
	private double minimumDistances[][];
	private double cutoffAngle;
	
	//Debug
	public Vector2d[][][] rayPositions;
	
	public EpuckIRSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.random = simulator.getRandom();
		numberOfSensors = 4;
		this.readings = new double[numberOfSensors];
		range = RANGE;
		
		senseRobot = args.getArgumentAsIntOrSetDefault("senserobot", 0) == 1;

		cutoffAngle = args.getArgumentAsDoubleOrSetDefault("cutoffangle", 45);
		boolean fixedSensor = args.getArgumentAsIntOrSetDefault("fixedsensor", 0) == 1;
		noiseEnabled = args.getArgumentAsIntOrSetDefault("noiseenabled", 1) ==  1;
		numberOfRays = args.getArgumentAsIntOrSetDefault("numberofrays", 7);
		offsetNoise = args.getArgumentAsDoubleOrSetDefault("offsetnoise", 0);
		offsetNoises = new double[numberOfSensors];
		
		for(int i = 0 ; i < offsetNoises.length ; i++) {
			double rand = simulator.getRandom().nextDouble()*offsetNoise;
			boolean negative = simulator.getRandom().nextBoolean();
			
			if(negative)
				rand*=-1;
			offsetNoises[i] = rand;
		}
		
		if(numberOfRays%2 == 0)
			numberOfRays++;
		
		rayReadings = new double[numberOfSensors][numberOfRays];
		
		chosenReferences = new int[numberOfSensors];
		
		if(fixedSensor) {
			int index = 0;
			for(int i = 0 ; i < numberOfSensors ; i++) {
				if(args.getArgumentIsDefined("fixedsensornumber"))
					index = args.getArgumentAsInt("fixedsensornumber");
				else if(args.getArgumentIsDefined("references")) {
					index = i+args.getArgumentAsInt("references")*4;
				}else if(args.getArgumentIsDefined("randomizedreferences")) {
					index = 4*args.getArgumentAsInt("randomizedreferences")+simulator.getRandom().nextInt(4);
				}
				chosenReferences[i] = index;
			}
		}else {
			for(int i = 0 ; i < numberOfSensors ; i++) {
				int random = simulator.getRandom().nextInt(sensorReferenceValues.length);
				chosenReferences[i] = random;
			}
		}

		this.fullOpeningAngle = openingAngle;
		this.openingAngle = openingAngle / 2;
		
		initAngles();
		((Epuck) robot).setIRSensor(this);
		setAllowedObjectsChecker(new AllowWallRobotChecker(robot.getId()));
		
		sensorPositions = new Vector2d[numberOfSensors];
		cones = new Vector2d[numberOfSensors][numberOfRays];
		for(int i = 0 ; i < numberOfSensors ; i++) {
			sensorPositions[i] = new Vector2d();
			for(int j = 0 ; j < numberOfRays ; j++)
				cones[i][j] = new Vector2d();
		}
		
		minimumDistances = new double[numberOfSensors][numberOfRays];
	}

	private void initAngles() {
		this.angles = new double[numberOfSensors];
		if(numberOfSensors == 4){
			angles[3] = 0.296705973;
			angles[2] = Math.PI / 2;
			angles[1] = 3.0 * (Math.PI / 2.0);
			angles[0] = 5.98647933;
		}else{
			angles[7] = 0.296705973;
			angles[6] = 0.872664626;
			angles[5] = Math.PI / 2.0;
			angles[4] = 2.61799388;
			angles[3] = 3.66519143;
			angles[2] = 3.0 * (Math.PI / 2.0);
			angles[1] = 5.41052068;
			angles[0] = 5.98647933;
		}
	}
	
	private void updateCones() {
		
		rayPositions = new Vector2d[numberOfSensors][numberOfRays][2];
		minimumDistances = new double[numberOfSensors][numberOfRays];
		
		for(int sensorNumber = 0 ; sensorNumber < numberOfSensors ; sensorNumber++) {
			double orientation = angles[sensorNumber] + robot.getOrientation();
			
			sensorPositions[sensorNumber].set(
					FastMath.cosQuick(orientation) * robot.getRadius() + robot.getPosition().getX(),
					FastMath.sinQuick(orientation) * robot.getRadius() + robot.getPosition().getY()
				);
			
			double alpha = (this.fullOpeningAngle)/(numberOfRays-1);
			
			for(int i = 0 ; i < numberOfRays ; i++) {
				
				cones[sensorNumber][i].set(
						FastMath.cosQuick(orientation - openingAngle + alpha*i)* REAL_RANGE + sensorPositions[sensorNumber].getX(),
						FastMath.sinQuick(orientation - openingAngle + alpha*i)* REAL_RANGE + sensorPositions[sensorNumber].getY()
					 );
			}
		}
	}

	/**
	 * Calculates the distance between the robot and a given PhisicalObject if
	 * in range
	 * 
	 * @return distance or 0 if not in range
	 */
	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
			
//		if(!newMode)
//			return oldCalculateContributionToSensor(sensorNumber,source);
		
		double inputValue = 0;
		
		if(source.getObject().getType() != PhysicalObjectType.ROBOT) {
		
			Wall w = (Wall) source.getObject();
			
			for(int i = 0 ; i < numberOfRays ; i++) {
				Vector2d cone = cones[sensorNumber][i];
				
				rayPositions[sensorNumber][i][0] = sensorPositions[sensorNumber];
				if(rayPositions[sensorNumber][i][1] == null)
					rayPositions[sensorNumber][i][1] = cone;
				
				Vector2d intersection = null;
				intersection = w.intersectsWithLineSegment(sensorPositions[sensorNumber], cone, FastMath.toRadians(cutoffAngle));
				
//				if(sensorNumber == 3 && i == 1) {
//					System.out.println(source.getObject().getId());
//					w.intersectsWithLineSegmentPrint(sensorPositions[sensorNumber], cone, Math.toRadians(cutoffAngle));
//				}
				
				if(intersection != null) {
					
					double distance = intersection.distanceTo(sensorPositions[sensorNumber]);
					cone.angle(intersection);
					
					if(distance < REAL_RANGE) {
						double sensorValue = distanceToSensor(distance, sensorNumber);
						
						sensorValue+=sensorValue*offsetNoises[sensorNumber];
						
						inputValue = sensorToInput(sensorValue, sensorNumber);
						
						if((minimumDistances[sensorNumber][i] == 0 || distance < minimumDistances[sensorNumber][i]) && inputValue > rayReadings[sensorNumber][i]) {
							rayPositions[sensorNumber][i][1] = intersection;
							rayReadings[sensorNumber][i] = FastMath.max(inputValue, rayReadings[sensorNumber][i]);
						}
					}
				}
			}

		} else if(source.getObject().getType() == PhysicalObjectType.ROBOT && senseRobot) {
			
			//TODO This isn't really accurate
			
			GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source.getObject().getPosition());
			
			double distance = sensorInfo.getDistance();
			
			Robot robot = ((Robot)source.getObject());
			
			distance-=robot.getRadius();
			
			if(distance < REAL_RANGE && (sensorInfo.getAngle()< (openingAngle)) &&
					(sensorInfo.getAngle()>(-openingAngle))){
				
				double sensorValue = distanceToSensor(distance, sensorNumber);
				
				inputValue = sensorToInput(sensorValue, sensorNumber);
			}
		
			for(int i = 0 ; i < numberOfRays ; i++)
				rayReadings[sensorNumber][i] = FastMath.max(inputValue, rayReadings[sensorNumber][i]);
		}
		return inputValue;
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		updateCones();

		if(closeObjects != null) {
			closeObjects.update(time, teleported);
		}
		try { 
			for(int i = 0; i < numberOfSensors; i++){
				for(int j = 0; j < numberOfRays; j++){
					rayReadings[i][j] = 0.0;
				}
				readings[i] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
//					iterator.updateCurrentDistance(geoCalc.getDistanceBetween(robot.getPosition(), source.getObject(),time));
				}
			}
			
			for(int i = 0; i < numberOfSensors; i++){
				int aboveZero = 0;
				double copy[] = new double[numberOfRays];
//				for(int j = 0; j < numberOfRays; j++){
//					System.out.println(i+" "+j+" "+rayReadings[i][j]);
					
//					double distanceToCenter = Math.abs(j-numberOfRays/2);
//					if(distanceToCenter == 0)
//						distanceToCenter++;
					
				for(int ray = 0 ; ray < copy.length ; ray++){
					copy[ray] = rayReadings[i][ray];
				}
				
				double avg = 0;
				double maxRays = 4;
				
				for(int m = 0 ; m < maxRays ; m++) {
					int currentMaxIndex = 0;
					for(int z = 1 ; z < copy.length ; z++) {
						if(copy[z] > copy[currentMaxIndex]) {
							currentMaxIndex = z;
						}
					}
					avg+=copy[currentMaxIndex]/maxRays;
					copy[currentMaxIndex] = 0;
				}
					
				readings[i]=avg;//(rayReadings[i][j]/(double)numberOfRays/*/distanceToCenter*/);
				
				for(int j = 0; j < rayReadings[i].length ; j++) {
					if(rayReadings[i][j] > 0)
						aboveZero++;
				}
				
//				if(aboveZero <= 2 /*|| rayReadings[i][numberOfRays/2] == 0*/)
//					readings[i] = 0;
					
//					if(i == 0) {
//						readings[i] = Math.min(1, readings[i]*1.5);
//					}
				
//				System.out.print(readings[i]+" ");
			}
//			System.out.println();
			if(((Epuck) robot).getLightSensor() != null) {
                if(((Epuck) robot).getLightSensor().last1 > 0)
                	readings[2] = (readings[2]  + (1.0-readings[2] )*0.5*0.7)*(1+0.1*random.nextGaussian());
                if(((Epuck) robot).getLightSensor().last0 > 0)
                	readings[1] = (readings[1]  + (1.0-readings[1] )*0.5*0.7)*(1+0.1*random.nextGaussian());
    		}
			
			last2 = readings[1];
			last5 = readings[2];
			
//				readings[0]*= 0;
//				readings[3]*= 1;

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++){
			calculateContributionToSensor(j, source);
		}
	}
	
	protected void oldCalculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++){
			readings[j] = FastMath.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}
	
	private double distanceToSensor(double distance, int sensorNumber) {
		
		double[] sensorReferences = sensorReferenceValues[chosenReferences[sensorNumber]];
		double[] stdDeviations = sensorReferenceStdDeviation[chosenReferences[sensorNumber]];
		
		distance*=100;//0.01 in the simulator is 1cm. We use the value 1 as 1cm
		
		if(distance < distances[distances.length-1]) {

	        int i;
	        for(i = 0; i < distances.length ; i++) {
	            if(distance <= distances[i]) {
	                if(i == 0)
	                    return sensorReferences[i];

	                double distanceBefore = distances[i-1];
	                double distanceAfter = distances[i];
	                
	                double linearization = (distance-distanceBefore)/(distanceAfter-distanceBefore);
	                
	                double sensorReferenceAfter = sensorReferences[i];
	                double sensorReferenceBefore = sensorReferences[i-1];
	                double currentSensorValue = (sensorReferenceBefore-sensorReferenceAfter)*(1-linearization)+sensorReferenceAfter;
	                
	                double noise = random.nextGaussian() * (int)stdDeviations[i];
	                
	                if(!noiseEnabled)
	                	noise = 0;
	                
	                return currentSensorValue + noise;
	            }
	        }
	    }
		
		return 0.0;
	}
	
	double sensorToInput(double value, int sensorNumber) {
		
	    if(value > sensorAverages[sensorAverages.length-1]) {

	        int i;
	        for(i = 0; i < sensorAverages.length ; i++) {
	            if(value > sensorAverages[i]) {
	                if(i == 0)
	                    return 1.0;

	                double referenceBefore =sensorAverages[i-1];
	                double referenceAfter =sensorAverages[i];
	                
	                double linearization = (value-referenceAfter)/(referenceBefore-referenceAfter);
	                
	                double distanceAfter = distances[i];
	                double distanceBefore = distances[i-1];
	                double currentDistance = (distanceBefore-distanceAfter)*(linearization)+distanceAfter;
	                
	                double currentValue = 1.0-currentDistance/distances[sensorAverages.length-1];
	                currentValue = FastMath.min(currentValue,1);

	                return currentValue;
	            }
	        }
	    }
	    return 0.0;
	}

	@Override
	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			Vector2d source) {
		double orientation = angles[sensorNumber] + robot.getOrientation();
		sensorPosition.set(FastMath.cosQuick(orientation) * robot.getRadius()
				+ robot.getPosition().getX(),
				FastMath.sinQuick(orientation) * robot.getRadius()
						+ robot.getPosition().getY());

		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(sensorPosition, 
				orientation,source, time);

		return sensorInfo;
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "EPuckIRSensors [readings=" + Arrays.toString(readings) + "]";
	}
}