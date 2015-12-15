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
	
	public MultipleWheelRepertoireActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		Arguments a = new Arguments(args.getArgumentAsString("wheels"));
		wheels = (MultipleWheelAxesActuator)Actuator.getActuator(simulator, a.getArgumentAsString("classname"), a);
		nParams = wheels.getNumberOfSpeeds()+wheels.getNumberOfRotations();
		repertoire = loadRepertoire(simulator, args.getArgumentAsString("repertoire"));
		type = args.getArgumentAsIntOrSetDefault("type", type);
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
		
		//select correct behavior from repertoire
		double[] behavior = selectBehaviorFromRepertoire();
		
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
		
		wheels.apply(robot, timeDelta);
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
}