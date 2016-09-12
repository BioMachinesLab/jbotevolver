package vrep;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

import evorbc.mappingfunctions.MappingFunction;
import evorbc.mappingfunctions.Polar180MappingFunction;
import mathutils.Vector2d;

public class VRepRepertoireController extends VRepController {

	public static String REPERTOIRE_FILENAME = "repertoire.txt";
	protected static double[][][] repertoire;
	protected static double resolution;
	protected static int nParams = 24;

	protected VRepNEATController ann;
	protected double heading;
	protected double speed;
	protected MappingFunction bm;
	
	protected String debugName;
	protected boolean debug;
	
	public VRepRepertoireController(float[] parameters) {
		super(parameters);
		
		if(parameters != null) {
			ann = new VRepNEATController(parameters);
		}
		
		nParams = (int)parameters[1];//locomotion parameters
		
		loadRepertoire(REPERTOIRE_FILENAME);
		
		bm = new Polar180MappingFunction(repertoire);
		bm.fill();
		
		File f = new File("debug.txt");
		debug = f.exists();
		if(debug) {
			System.out.println("################DEBUG MODE################");
			try {
				debugName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString()+".txt";
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
                
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
		
		double[] behavior = null;
		
		double s = this.speed;
		
		Vector2d point = null;
		
		do{
			point = bm.map(this.heading,s);
			//TODO maybe change the values here
			behavior = repertoire[(int)point.y][(int)point.x];
			
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
		
		if(debug)
			debug(inputs, outputs, point);
		
		time++;
		
		return doubleToFloat(behavior);
	}
	
	protected void debug(float[] inputs, float[] outputs, Vector2d point) {
		try {
			FileWriter fw = new FileWriter(new File(debugName), true);
			String line = time+"";
			
			for(float f : inputs)
				line = line+"\t"+f;
			
			for(float f : outputs)
				line = line+"\t"+f;
			
			line = line+"\t"+point.x+"\t"+point.y;
			
			fw.append(line+"\n");
			fw.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
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

	private static double readDouble(Scanner s) {
		return Double.parseDouble(s.next().trim().replace(',', '.'));
	}
	
	public int getNumberOfParameters() {
		return nParams;
	}
	
}