package vrep;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class VRepController {

    public static final String DEBUG_FLAG = "debug.txt";
    protected static boolean debug = false;
    private static boolean debugInit = false;
    protected float[] parameters;
    protected String debugFile;
    protected int ticks;
    
    public VRepController(float[] parameters) {
        this.parameters = parameters;
        this.ticks = 0;
        checkDebugMode();

        if (debug) {
            try {
                debugFile = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()).toString() + ".txt";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static synchronized void checkDebugMode() {
        if(!debugInit) {
            File f = new File(DEBUG_FLAG);
            debug = f.exists();
            if(debug) {
                System.out.println("################ DEBUG MODE ACTIVATED ################");
            }
            debugInit = true;
        }
    }

    public abstract float[] controlStep(float[] inputs);
    
    public void tick() {
        ticks++;
    }
    
    public void appendToDebug(String line) {
        if(debug) {
            try {
                FileWriter fw = new FileWriter(new File(debugFile), true);
                fw.append(line + "\n");
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
    }

    public static double[] floatToDouble(float[] f) {
        double[] res = new double[f.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (double) f[i];
        }
        return res;
    }

    public static float[] doubleToFloat(double[] d) {
        float[] res = new float[d.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (float) d[i];
        }
        return res;
    }
}
