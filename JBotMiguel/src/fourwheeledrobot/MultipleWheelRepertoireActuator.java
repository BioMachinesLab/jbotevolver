package fourwheeledrobot;

import evorbc.mappingfunctions.MappingFunction;
import mathutils.Vector2d;
import multiobjective.MOChromosome;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.Factory;

/**
 * This actuator uses a behavior repertoire to choose the
 * parameters for different actuator. It should be
 * placed before the other actuator in the configuration file
 * 
 * @author miguelduarte
 */
public class MultipleWheelRepertoireActuator extends Actuator{
	
	protected final double maxAllele = 10;
	protected MultipleWheelAxesActuator wheels;
	protected double heading;
	protected double speed;
	//the repertoire is a 2D grid, where each cell has the
	//locomotion parameters (a double[]) for a total of 3 dimensions
	protected double[][][] repertoire;
	protected int nParams;
	protected double stop = 0;
	protected int lock = 0;
	protected int controlCycle = 0;
	protected Vector2d lastPoint;
	protected String repertoireLocation;
	protected double[] prevBehavior;
	protected MappingFunction mappingFunction;
	
	public MultipleWheelRepertoireActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		Arguments a = new Arguments(args.getArgumentAsString("wheels"));
		
		int numArgs = a.getNumberOfArguments();
		
		if(numArgs == 1) {
			Arguments wheelArgs = new Arguments(a.getArgumentAsString(a.getArgumentAt(0)));
			wheels = (MultipleWheelAxesActuator)Actuator.getActuator(simulator, wheelArgs.getArgumentAsString("classname"), wheelArgs);
			nParams = wheels.getNumberOfSpeeds()+wheels.getNumberOfRotations();
			this.repertoireLocation = wheelArgs.getArgumentAsString("repertoire");
			repertoire = (double[][][])simulator.getSerializableObjects().get("repertoire");
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
			nParams = wheels.getNumberOfSpeeds()+wheels.getNumberOfRotations();
			repertoire = (double[][][])simulator.getSerializableObjects().get("repertoire");
		}
		
		if(args.getArgumentIsDefined("mapping")) {
			Arguments fillArguments = new Arguments(args.getArgumentAsString("mapping"));
			mappingFunction = (MappingFunction)Factory.getInstance(fillArguments.getArgumentAsString("classname"),(Object)repertoire);
		}
		
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		
		controlCycle++;
		
		//select correct behavior from repertoire, or lock for X number of timesteps
		double[] behavior;
		if(prevBehavior == null || (lock == 0 || controlCycle % lock == 0)) {
			behavior = selectBehaviorFromRepertoire();
		}else{
			behavior = prevBehavior;
		}
		
		if(behavior == null) {
			System.err.println("Cannot find the correct behavior in repertoire! "+heading+" "+speed);
		} else {
			//actuate the real actuator using the parameters
			for(int i = 0 ;  i < wheels.getNumberOfSpeeds() ; i++) {
				double val = ((behavior[i] / maxAllele) + 1.0) / 2.0;
				wheels.setWheelSpeed(i, val);
			}
			
			for(int i = 0 ;  i < wheels.getNumberOfRotations() ; i++) {
				double val = ((behavior[i+wheels.getNumberOfSpeeds()] / maxAllele) + 1.0) / 2.0;
				wheels.setRotation(i, val);
			}
		}
		if(stop-- <= 0) {
			wheels.apply(robot, timeDelta);
		}
		
		prevBehavior = behavior;
	}
	
	protected double[] selectBehaviorFromRepertoire() {

		double[] behavior = null;
		
		double s = this.speed;
		
		Vector2d point = null;
		
		do{
			point = mappingFunction.map(this.heading,s);
			behavior = repertoire[(int)point.x][(int)point.y];
			
			if(behavior == null) {
				//reduce the size of the circle to find an appropriate point
				//in case there is no filling. this is to deal with edge cases
				s=s*2.0-1.0;
				s*=0.95;
//				MAPElitesEvolution.printRepertoire(repertoire, (int)point.x, (int)point.y);
				if(Math.abs(s) < 0.1)
					break;
				
				s=s/2.0 + 0.5;
			}
			
		} while(behavior == null);
		
		lastPoint = new Vector2d(point);
		
		return behavior;
	}
	
	//receives heading [0,1]
	public void setHeading(double heading) {
		this.heading = heading;
//		System.out.print("h: "+heading+" -> "+Math.toDegrees(this.heading));
	}

	//receives speed [0,1]
	public void setWheelSpeed(double speed) {
		this.speed = speed;
//		System.out.println("\t\ts: "+speed+" -> "+this.speed);
	}
	
	public double getHeading() {
		return heading;
	}
	
	public double getWheelSpeed() {
		return speed;
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
	
	public void stop(double val) {
		this.stop = val;
	}
	
	public Vector2d getLastPoint() {
		return lastPoint;
	}

	public String getRepertoireLocation() {
		return repertoireLocation;
	}
	
	public double[][][] getRepertoire() {
		return repertoire;
	}
	
	public double[] getLastBehavior() {
		return repertoire[(int)lastPoint.y][(int)lastPoint.x];
	}
	
}