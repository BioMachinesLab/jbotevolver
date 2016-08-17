package fourwheeledrobot;

import java.util.Scanner;
import mathutils.MathUtils;
import mathutils.Vector2d;
import multiobjective.MOChromosome;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evaluationfunctions.RadialOrientationEvaluationFunction;
import evolution.NDBehaviorMap;

/**
 * @author miguelduarte
 */
public class MultipleWheelRepertoireNDActuator extends Actuator{

	protected int[] prevBehavior;
	protected double[] actuations;

	protected NDBehaviorMap map;
	protected MultipleWheelAxesActuator wheels;
	
	protected int lock = 0;
	protected int controlCycle = 0;
	protected double stop = 0;
	protected int type = 0;//0=spherical
	
	public MultipleWheelRepertoireNDActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		Arguments a = new Arguments(args.getArgumentAsString("wheels"));
		
		int numArgs = a.getNumberOfArguments();
		
		if(numArgs == 1) {
			Arguments wheelArgs = new Arguments(a.getArgumentAsString(a.getArgumentAt(0)));
			wheels = (MultipleWheelAxesActuator)Actuator.getActuator(simulator, wheelArgs.getArgumentAsString("classname"), wheelArgs);
			map = loadNDBehaviorMap(simulator, wheelArgs.getArgumentAsString("repertoire"));
			lock = (int)(args.getArgumentAsDoubleOrSetDefault("lock", lock) / simulator.getTimeDelta());
		} else {
			
			int fitnessSample = simulator.getArguments().get("--environment").getArgumentAsInt("fitnesssample");
			int actuatorChosen = fitnessSample % numArgs;
			
			boolean randomize = simulator.getArguments().get("--robots").getFlagIsTrue("randomizeactuator");
			
			if(randomize)
				actuatorChosen = simulator.getRandom().nextInt(numArgs);
			
			if(simulator.getArguments().get("--robots").getArgumentIsDefined("chosenactuator"))
				actuatorChosen = simulator.getArguments().get("--robots").getArgumentAsInt("chosenactuator");
			
			Arguments wheelArgs = new Arguments(a.getArgumentAsString(a.getArgumentAt(actuatorChosen)));
			wheels = (MultipleWheelAxesActuator)Actuator.getActuator(simulator, wheelArgs.getArgumentAsString("classname"), wheelArgs);
			map = loadNDBehaviorMap(simulator, wheelArgs.getArgumentAsString("repertoire"));
		}
		type = args.getArgumentAsIntOrSetDefault("type", type);
		actuations = new double[map.getNDimensions()];
	}
	
	private int[] selectBehaviorFromRepertoire() {
		
		int[] buckets = new int[map.getNDimensions()];
		
		if(type == 0) {
			//same as EvoRBC GECCO paper: Alpha [-90,90], speed [-1,1]
			
			int nSphere = 0;
			
			for(int i = 0 ; i < map.getNDimensions() ; i++) {
				if(map.getCircularDimension(i))
					break;
				nSphere++;
			}
			
			double speedPercentage = actuations[0];
			double headingPercentage = actuations[1];
			
			double h =  headingPercentage * (Math.PI/2);
			
			double circleRadius = map.getCircleRadius()-1;//circle radius in buckets
			
			if(nSphere != 2 && actuations.length != 3)
				throw new RuntimeException("MultipleWheelRepertoireNDActuator unimplemented for this type of map!");
			
			if(speedPercentage < 0) {
				h+=Math.PI;
				speedPercentage*=-1;
			}
			
			//multiply by the circle radius in cartesian coordinates
			double x = speedPercentage*Math.cos(h)* (circleRadius*map.getResolutions()[0]);
			double y = speedPercentage*Math.sin(h)* (circleRadius*map.getResolutions()[1]);
			
			buckets[0] = map.valueToBucket(0, x);
			buckets[1] = map.valueToBucket(1, y);
			
			//regular cartesian conversion for non-circular dimensions (ie, orientation)
			double limit = map.getLimits()[2];
			
			if(!map.getCircularDimension(2))//if not circular, we have to check the filling radius
				limit = map.getCircleRadius()*map.getResolutions()[2];

			double orientation =  limit*actuations[2];//limit*2 * [0,1] - limit
			
			orientation+= RadialOrientationEvaluationFunction.getTargetOrientation(new Vector2d(x,y));
			orientation = MathUtils.modPI2(orientation);
			
			buckets[2] = map.valueToBucket(2, orientation);
			
		}
		return buckets;
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {

		controlCycle++;
		
		//select correct behavior from repertoire, or lock for X number of timesteps
		int[] bucket;
		if(prevBehavior == null || (lock == 0 || controlCycle % lock == 0)) {
			bucket = selectBehaviorFromRepertoire();
		}else{
			bucket = prevBehavior;
		}
		
		double[] vec = map.getValuesFromBucketVector(bucket);
		
		MOChromosome c = map.getChromosome(vec);
		
		if(c == null) {
			System.err.println("Cannot find the correct behavior in repertoire!");
		} else {
			
			double[] behavior = c.getAlleles();
			
			int behaviorIndex = 0;
			
			//actuate the real actuator using the parameters
			for(int i = 0 ;  i < wheels.getNumberOfSpeeds() ; i++) {
				double val = ((behavior[behaviorIndex++] / map.getMaxAllele()) + 1.0) / 2.0;
				wheels.setWheelSpeed(i, val);
			}
			
			for(int i = 0 ;  i < wheels.getNumberOfRotations() ; i++) {
				double val = ((behavior[behaviorIndex++] / map.getMaxAllele()) + 1.0) / 2.0;
				wheels.setRotation(i, val);
			}
		}
		if(stop-- <= 0) {
			wheels.apply(robot, timeDelta);
		}
		
		prevBehavior = bucket;
	}
	
	protected NDBehaviorMap loadNDBehaviorMap(Simulator simulator, String f) {
		
		if(simulator.getArguments().get("--simulator") != null)  {
			Arguments args = simulator.getArguments().get("--simulator");
			String prefix = args.getArgumentAsStringOrSetDefault("folder","");
			f= prefix+f;
		}
		
		NDBehaviorMap map = null;
		
		try {
			Scanner s = new Scanner(simulator.getFileProvider().getFile(f));
			
			StringBuffer b = new StringBuffer();
			
			while(s.hasNextLine()) {
				String line = s.nextLine();
				b.append(line+"\n");
			}
			
			map = NDBehaviorMap.deserialize(b.toString());
			
			s.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	public void setActuation(double actuation, int dimension) {
		this.actuations[dimension] = (actuation - 0.5) *2.0;
	}
	
	public void stop(double val) {
		this.stop = val;
	}

	public int getNDimensions() {
		return map.getNDimensions();
	}
	
	public double[] getCompleteRotations() {
		return wheels.getCompleteRotations();
	}
	
	public double[] getCompleteSpeeds() {
		return wheels.getCompleteSpeeds();
	}
	
	public double getMaxSpeed() {
		return wheels.getMaxSpeed();
	}
	
	public int[] getPrevBehavior() {
		return prevBehavior;
	}
	
	public NDBehaviorMap getMap() {
		return map;
	}
	
	public int[] getRealPrevBehavior() {
		double[] vec = map.getValuesFromBucketVector(prevBehavior);
		MOChromosome moc = map.getChromosomeFromBehaviorVector(vec);
		vec = map.getBehaviorVector(moc);
		int[] realVec = map.getBucketsFromBehaviorVector(vec);
		
		return realVec;
	}
}