/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrep;

/**
 *
 * @author jorge
 */
public class VRepRepertoireStopController extends VRepRepertoireController {
    
    protected int stopSteps;
    public static final float[] STOP_SIGNAL = new float[]{0};
    protected float[] currentBehavior = null;    
    protected int time = -1;
    
    public VRepRepertoireStopController(float[] parameters) {
        super(parameters);
        stopSteps = (int) parameters[4];
        appendToDebug("# Stopping controller with " + stopSteps + " stop steps");
    }
    
    @Override
    public float[] controlStep(float[] inputs) {        
        // The control step is always executed because of recurrencies etc
        float[] newBehav = super.controlStep(inputs);
        if(newBehav == null) {
            return null;
        }
        
        if(currentBehavior == null && (ticks < stopSteps || ticks - time > stopSteps)) { // done stopping
            currentBehavior = newBehav;
            appendToDebug("# Starting execution @ " + ticks);
            return currentBehavior;
        } else if(currentBehavior != null && !VRepRepertoireControllerStable.same(currentBehavior, newBehav)) { // done executing
            currentBehavior = null;
            time = ticks;
            appendToDebug("# Start stopping @ " + ticks);
            return STOP_SIGNAL;
        }
        return null;
    }
}
