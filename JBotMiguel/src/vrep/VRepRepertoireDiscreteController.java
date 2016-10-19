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
public class VRepRepertoireDiscreteController extends VRepRepertoireController {
    
    protected int stopSteps;
    protected int lockSteps;

    protected float[] currentBehavior = null;    
    protected int time = -1;
    
    public VRepRepertoireDiscreteController(float[] parameters) {
        super(parameters);
        stopSteps = (int) parameters[4];
        lockSteps = (int) parameters[5];
        System.out.println("Discrete controller with " + stopSteps + " stop steps and " + lockSteps + " lock steps");
    }
    
    @Override
    public float[] controlStep(float[] inputs) {        
        // The control step is always executed because of recurrencies etc
        float[] newBehav = super.controlStep(inputs);
        if(newBehav == null) {
            return null;
        }
        
        if(currentBehavior == null && (ticks < lockSteps || ticks - time > stopSteps)) { // done stopping
            currentBehavior = newBehav;
            time = ticks;
            appendToDebug("# Starting execution @ " + ticks);
            return currentBehavior;
        } else if(currentBehavior != null && ticks - time > lockSteps) { // done executing
            currentBehavior = null;
            time = ticks;
            appendToDebug("# Start stopping @ " + ticks);
            return VRepRepertoireStopController.STOP_SIGNAL;
        }
        return null;
    }
}
