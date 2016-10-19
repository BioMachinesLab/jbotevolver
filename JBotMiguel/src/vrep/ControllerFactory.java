package vrep;

import java.util.HashMap;

public class ControllerFactory {

    private static final HashMap<Integer, VRepController> CONTROLLERS = new HashMap<>();
    public static final float WEIGHTS_START_CODE = 123456;    

    public static void loadController(int[] handles, float[] parameters) {

        if (parameters.length == 0) {
            throw new RuntimeException("Parameters array is empty!");
        }

        int type = (int) parameters[0];

        for (int i = 0; i < handles.length; i++) {
            switch (type) {
                case 0:
                    CONTROLLERS.put(handles[i], new VRepDummyController(parameters));
                    break;
                case 1:
                    CONTROLLERS.put(handles[i], new VRepRepertoireController(parameters));
                    break;
                case 2:
                    CONTROLLERS.put(handles[i], new VRepRepertoireControllerStable(parameters));
                    break;                    
                case 3:
                    CONTROLLERS.put(handles[i], new VRepRepertoireStopController(parameters));
                    break;
                case 4:
                    CONTROLLERS.put(handles[i], new VRepRepertoireDiscreteController(parameters));
                    break;
                case 5:
                    CONTROLLERS.put(handles[i], new VRepNEATController(parameters));
                    break;
            }
        }
    }

    public static float[] controlStep(int handle, float[] inputs) {
        VRepController c = CONTROLLERS.get(handle);
        if (c != null) {
            c.tick();
            return c.controlStep(inputs);
        } else {
            System.out.println("Controller is not yet loaded for this handle: " + handle);
        }
        return null;
    }
    
    public static double[] extractWeights(float[] controllerParams) {
        int offset = 0;
        while(controllerParams[offset] != WEIGHTS_START_CODE) {
            offset++;
        }
        offset++;
        double weights[] = new double[controllerParams.length - offset];
        for(int i = 0 ; i < weights.length ; i++) {
            weights[i] = controllerParams[i + offset];
        }
        System.out.println("offset: " + offset);
        return weights;
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
