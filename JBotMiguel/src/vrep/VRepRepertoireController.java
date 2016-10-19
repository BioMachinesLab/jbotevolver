package vrep;

import java.io.File;
import java.util.Scanner;

import evorbc.mappingfunctions.MappingFunction;
import evorbc.mappingfunctions.Polar180MappingFunction;
import mathutils.Vector2d;

public class VRepRepertoireController extends VRepController {
    
    public static final String REPERTOIRE_FILENAME = "repertoire.txt";
    protected static double[][][] repertoire = null;
    protected static double resolution = -1;
    protected static int nParams = -1;
    
    protected VRepNEATController ann;
    protected MappingFunction bm;
    
    public VRepRepertoireController(float[] parameters) {
        super(parameters);
        
        if (parameters != null) {
            ann = new VRepNEATController(parameters);
        }
        
        nParams = (int) parameters[1]; //locomotion parameters
        loadRepertoire(REPERTOIRE_FILENAME);
        
        bm = new Polar180MappingFunction(repertoire);
        bm.fill();
    }
    
    protected static synchronized void loadRepertoire(String filename) {
        if (repertoire != null) {
            return;
        }
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
            System.out.println("Rerpertoire loaded: " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        repertoire = r;
    }
    
    @Override
    public float[] controlStep(float[] inputs) {
        if(inputs == null || inputs.length == 0) {
            return null;
        }
        float[] outputs = ann.controlStep(inputs);
        
        double heading = outputs[0];
        double speed = outputs[1];
        
        double[] behavior = null;
        double s = speed;
        Vector2d point = null;
        do {
            point = bm.map(heading, s);
            behavior = repertoire[(int) point.y][(int) point.x];
            
            if (behavior == null) {
                //reduce the size of the circle to find an appropriate point
                //in case there is no filling. this is to deal with edge cases
                s = (s * 2.0 - 1.0) * 0.95;
                if (Math.abs(s) < 0.1) {
                    break;
                }
                s = s / 2.0 + 0.5;
            }
        } while (behavior == null);
        
        if (debug) {
            StringBuilder line = new StringBuilder();
            for (float f : inputs) {
                line.append(f).append(" ");
            }
            for (float f : outputs) {
                line.append(f).append(" ");
            }
            line.append(point.x).append(" ").append(point.y);
            super.appendToDebug(line.toString());
        }
        return ControllerFactory.doubleToFloat(behavior);
    }
    
    private static double readDouble(Scanner s) {
        return Double.parseDouble(s.next().trim().replace(',', '.'));
    }
    
}
