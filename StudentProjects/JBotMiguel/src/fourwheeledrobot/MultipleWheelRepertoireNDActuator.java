package fourwheeledrobot;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
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
	protected int type = 0;//0=cartesian,1=spherical
	
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
			//cartesian
			for(int dim = 0 ; dim < map.getNDimensions() ; dim++) {
				
				double limit = map.getLimits()[dim];
				
				if(!map.getCircularDimension(dim))//if not circular, we have to check the filling radius
					limit = map.getCircleRadius()*map.getResolutions()[dim];
				
				double val =  limit*2*actuations[dim] - limit;//limit*2 * [0,1] - limit
				buckets[dim] = map.valueToBucket(dim, val);
			}
		} if(type == 1) {
			//n-sphere: https://en.wikipedia.org/wiki/N-sphere#Spherical_coordinates
			
			int nSphere = 0;
			
			for(int i = 0 ; i < map.getNDimensions() ; i++) {
				if(map.getCircularDimension(i))
					break;
				nSphere++;
			}
			
			int actuationIndex = 0;
			
			double r = actuations[actuationIndex++];
			double[] phi = new double[nSphere-1];
			
			for(int i = 0 ; i < phi.length ; i++) {
				if(i == phi.length-1)
					phi[i] = actuations[actuationIndex++]*2*Math.PI;
				else
					phi[i] = actuations[actuationIndex++]*Math.PI;
			}
			
			double[] vals = new double[map.getNDimensions()];
			
			for(int i = 0 ; i < nSphere ; i++) {
				double val = r;
				
				if(i != vals.length-1) {
					for(int j = 0 ; j <= i ; j++) {
						if(j == i) {
							val*=Math.cos(phi[j]);
						} else {
							val*=Math.sin(phi[j]);
						}
					}	
				} else {
					for(int j = 0 ; j < i ; j++) {
						val*=Math.sin(phi[j]);
					}
				}
				vals[i] = val;
				double circleRadius =map.getCircleRadius();//circle radius in buckets
				circleRadius*= map.getResolutions()[i];//circle radius in cartesian coordinates
				vals[i]*= circleRadius*vals[i];
				buckets[i] = map.valueToBucket(i, val);
			}
			
			//regular cartesian conversion for non-circular dimensions
			for(int dim = nSphere ; dim < vals.length ; dim++) {
				double limit = map.getLimits()[dim];
				
				if(!map.getCircularDimension(dim))//if not circular, we have to check the filling radius
					limit = map.getCircleRadius()*map.getResolutions()[dim];
				
				double val =  limit*2*actuations[dim] - limit;//limit*2 * [0,1] - limit
				buckets[dim] = map.valueToBucket(dim, val);
			}
			
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
//			System.out.println();
//			for(int i = 0 ; i < vec.length ; i++) {
//				System.out.print(bucket[i]+" ");
//			}
//			System.out.println();
//			for(int i = 0 ; i < vec.length ; i++) {
//				System.out.print(vec[i]+" ");
//			}
//			System.out.println();
			
			System.err.println("Cannot find the correct behavior in repertoire!");
//			System.exit(0);
		} else {
			
			double[] behavior = c.getAlleles();
			
			//			System.out.println();
//			System.out.println("Circle radius: "+map.getCircleRadius()+" "+map.getResolutions()[0]);
//			for(int i = 0 ; i < vec.length ; i++)
//				System.out.print(vec[i]+" ");
//			System.out.println();
//			for(int i = 0 ; i < bucket.length ; i++)
//				System.out.print(bucket[i]+" ");
//			System.out.println();
			
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
		try {
			ObjectInputStream ois = new ObjectInputStream(simulator.getFileProvider().getFile(f));
			NDBehaviorMap map = (NDBehaviorMap)ois.readObject();
			ois.close();
			return map;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setActuation(double actuation, int dimension) {
		this.actuations[dimension] = actuation;
	}
	
	public void stop(double val) {
		this.stop = val;
	}

	public int getNDimensions() {
		return map.getNDimensions();
	}
}