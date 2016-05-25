package fourwheeledrobot;

import java.util.Scanner;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

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
	protected double resolution;
	protected double circleRadius;
	protected int type = 0;
	protected double stop = 0;
	protected int lock = 0;
	protected int controlCycle = 0;
	
	protected double[] prevBehavior;
	
	public MultipleWheelRepertoireActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		Arguments a = new Arguments(args.getArgumentAsString("wheels"));
		
		int numArgs = a.getNumberOfArguments();
		
		if(numArgs == 1) {
			Arguments wheelArgs = new Arguments(a.getArgumentAsString(a.getArgumentAt(0)));
			wheels = (MultipleWheelAxesActuator)Actuator.getActuator(simulator, wheelArgs.getArgumentAsString("classname"), wheelArgs);
			nParams = wheels.getNumberOfSpeeds()+wheels.getNumberOfRotations();
			repertoire = loadRepertoire(simulator, wheelArgs.getArgumentAsString("repertoire"));
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
			repertoire = loadRepertoire(simulator, wheelArgs.getArgumentAsString("repertoire"));
//			System.out.println(actuatorChosen+" "+numArgs+" "+wheelArgs.getArgumentAsString("repertoire"));
		}
		
//		for(int i = 0 ; i < repertoire.length ; i++) {
//			for(int j = 0 ; j < repertoire[i].length ; j++) {
//				System.out.print(repertoire[i][j] == null ? " " : "X");
//			}
//			System.out.println();
//		}
		
		type = args.getArgumentAsIntOrSetDefault("type", type);
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
		
		do{
			Vector2d point = circlePoint(this.heading,s);
//			int[] pos = getLocationFromBehaviorVector(new double[]{point.x,point.y});
//			behavior = repertoire[pos[1]][pos[0]];
			behavior = repertoire[(int)point.y][(int)point.x];
			
			//reduce the size of the circle to find an appropriate point
			s*=0.95;
			if(Math.abs(s) < 0.1)
				break;
		} while(behavior == null);
		
		return behavior;
	}
	
	private Vector2d circlePoint(double percentageAngle, double speedPercentage) {
		Vector2d res = null;
		
//		percentageAngle = 1;
//		speedPercentage = 0.5;
		
		if(type == 0) {
			
			double h =  percentageAngle * (Math.PI/2);
			
			if(speedPercentage < 0) {
				h+=Math.PI;
				speedPercentage*=-1;
			}
			res = new Vector2d(speedPercentage*circleRadius*Math.cos(h),speedPercentage*circleRadius*Math.sin(h));
		
		} else if(type == 1){
			
			speedPercentage = speedPercentage/2.0 + 0.5;
			
			double h =  percentageAngle * (Math.PI);
			res = new Vector2d(speedPercentage*circleRadius*Math.cos(h),speedPercentage*circleRadius*Math.sin(h));
			
		}
		
		res.x+=repertoire.length/2;
		res.y+=repertoire[0].length/2;
		
		return res;
	}
	
	private int[] getLocationFromBehaviorVector(double[] vec) {
		int[] mapLocation = new int[vec.length];
		
		for(int i = 0 ; i < vec.length ; i++) {
			int pos = (int)(vec[i]/resolution + repertoire.length/2.0);
			mapLocation[i] = pos;
		}
		return mapLocation;
	}
	
	protected double[][][] loadRepertoire(Simulator simulator, String f) {
		
		double[][][] r = null;
		
		try {
		
			Scanner s = new Scanner(simulator.getFileProvider().getFile(f));
			
			int size = s.nextInt();
			r = new double[size][size][];
			
			resolution = readDouble(s);
			
			while(s.hasNext()) {
				int x = s.nextInt();
				int y = s.nextInt();
				
				circleRadius = Math.max(circleRadius,new Vector2d(x - size/2.0, y - size/2.0).length());
				
				r[x][y] = new double[nParams];
				for(int d = 0 ; d < nParams ; d++) {
					r[x][y][d] = readDouble(s);
				}
			}
			
			s.close();
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
//		System.out.print("BEHAVIOR! ");
//		for(int i = 0 ; i < r.length ; i++) {
//			for(int j = 0 ; j < r[i].length ; j++) {
//				if(r[i][j] != null) {
//					for(int z = 0 ; z < r[i][j].length ; z++) {
//						System.out.print(r[i][j][z]+" ");
//					}
//				}
//			}
//		}
//		System.out.println();
		
		return r;
	}
	
	//receives heading [0,1] and saves as angle [-1,1]
	public void setHeading(double heading) {
		this.heading = (heading - 0.5) * 2;
//		System.out.print("h: "+heading+" -> "+Math.toDegrees(this.heading));
	}

	//receives speed [0,1] and saves as percentage [-1,1]
	public void setWheelSpeed(double speed) {
		this.speed = (speed - 0.5) * 2;
//		System.out.println("\t\ts: "+speed+" -> "+this.speed);
	}
	
	public double getHeading() {
		return heading;
	}
	
	public double getWheelSpeed() {
		return speed;
	}
	
	private double readDouble(Scanner s) {
		return Double.parseDouble(s.next().trim().replace(',','.'));
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
}