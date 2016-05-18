package vrep;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import mathutils.Vector2d;

public class VRepRepertoireController extends VRepController {

	public static String REPERTOIRE_FILENAME = "../repertoire.txt";
	protected static double[][][] repertoire;
	protected static double resolution;
	protected static double circleRadius;
	protected static int nParams = 24;

	protected VRepNEATController ann;
	protected double heading;
	protected double speed;
	
	public VRepRepertoireController(float[] parameters) {
		super(parameters);
		
		if(parameters != null) {
			ann = new VRepNEATController(parameters);
		}
		
		loadRepertoire(REPERTOIRE_FILENAME);
	}

	protected static synchronized void loadRepertoire(String filename) {

		if (repertoire != null)
			return;

		double[][][] r = null;

		try {

			Scanner s = new Scanner(new File(filename));
			
			int size = s.nextInt();
			r = new double[size][size][];

			resolution = readDouble(s);
			
			while (s.hasNext()) {
				int x = s.nextInt();
				int y = s.nextInt();
				
				circleRadius = Math.max(circleRadius, new Vector2d(x - size / 2.0, y - size / 2.0).length());

				r[x][y] = new double[nParams];
				for (int d = 0; d < nParams; d++) {
					r[x][y][d] = readDouble(s);
				}
			}

			s.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		repertoire = r;
	}

	@Override
	public float[] controlStep(float[] inputs) {
		
		float[] outputs = ann.controlStep(inputs);
		
		this.heading = outputs[0];
		this.speed = outputs[1];
		
		double[] behavior = selectBehaviorFromRepertoire();
		
		return doubleToFloat(behavior);
	}

	// receives heading [0,1] and saves as angle [-1,1]
	public void setHeading(double heading) {
		this.heading = (heading - 0.5) * 2;
		// System.out.print("h: "+heading+" -> "+Math.toDegrees(this.heading));
	}

	// receives speed [0,1] and saves as percentage [-1,1]
	public void setWheelSpeed(double speed) {
		this.speed = (speed - 0.5) * 2;
		// System.out.println("\t\ts: "+speed+" -> "+this.speed);
	}

	protected double[] selectBehaviorFromRepertoire() {
		
		double[] behavior = null;
		
		double s = this.speed;
		
		do {
			Vector2d point = circlePoint(this.heading, s);
			// int[] pos = getLocationFromBehaviorVector(new
			// double[]{point.x,point.y});
			// behavior = repertoire[pos[1]][pos[0]];
			behavior = repertoire[(int) point.y][(int) point.x];

			// reduce the size of the circle to find an appropriate point
			s *= 0.95;
			if (Math.abs(s) < 0.1)
				break;
		} while (behavior == null);
		
		return behavior;
	}

	private Vector2d circlePoint(double percentageAngle, double speedPercentage) {
		Vector2d res = null;
		
		// percentageAngle = 1;
		// speedPercentage = 0.5;

		double h = percentageAngle * (Math.PI / 2);

		if (speedPercentage < 0) {
			h += Math.PI;
			speedPercentage *= -1;
		}
		res = new Vector2d(speedPercentage * circleRadius * Math.cos(h),
				speedPercentage * circleRadius * Math.sin(h));

		res.x += repertoire.length / 2;
		res.y += repertoire[0].length / 2;
		
		return res;
	}
	
	private static double readDouble(Scanner s) {
		return Double.parseDouble(s.next().trim().replace(',', '.'));
	}
	
	public int getNumberOfParameters() {
		return nParams;
	}
	
}