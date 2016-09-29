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
public class VRepRepertoireTransitionController extends VRepRepertoireController {
    
    protected int stopSteps;
    protected int lockSteps;
    
    public static final float[] STOP_SIGNAL = new float[]{0};
    public static final int STATE_STOPPED = 0, STATE_STOPPING = 1, STATE_FREE = 2, STATE_LOCKED = 3;
    protected int state;
    protected float[] currentBehavior = null;    
    protected int time = -1;
    
    public VRepRepertoireTransitionController(float[] parameters) {
        super(parameters);
        stopSteps = (int) parameters[4];
        lockSteps = (int) parameters[5];
        state = STATE_STOPPED;
        System.out.println("Locking controller with " + stopSteps + " stop steps and " + lockSteps + " lock steps");
    }
    
    @Override
    public float[] controlStep(float[] inputs) {
        appendToDebug("# State: " + state + " @ " + ticks);
        
        // The control step is always executed because of recurrencies etc
        float[] newBehav = super.controlStep(inputs);
       
        if(state == STATE_STOPPING && ticks - time > stopSteps) {
            state = STATE_STOPPED; // done stopping
            appendToDebug("# Done stopping");
        }
        
        if(state == STATE_LOCKED && ticks - time > lockSteps) {
            state = STATE_FREE; // done locking     
            appendToDebug("# Done locking");
        }
        
        // start new behavior
        if(state == STATE_STOPPED) {
            currentBehavior = newBehav;
            time = ticks;
            state = lockSteps > 0 ? STATE_LOCKED : STATE_FREE;
            appendToDebug("# Starting new behavior");
            return currentBehavior;
        }
        
        // change current behavior
        if(state == STATE_FREE && !same(newBehav,currentBehavior)) {
            appendToDebug("# Changing behavior");
            if(stopSteps > 0) {
                currentBehavior = STOP_SIGNAL;
                time = ticks;
                state = STATE_STOPPING;
                appendToDebug("# Start stopping");
            } else if(lockSteps > 0) {
                currentBehavior = newBehav;
                time = ticks;
                state = STATE_LOCKED;
                appendToDebug("# Start locking");
            } else {
                currentBehavior = newBehav;
            }
        }
        return currentBehavior;
    }
    
    private boolean same(float[] a, float[] b) {
        if(a == b) {
            return true;
        }
        if(a.length != b.length) {
            return false;
        }
        for(int i = 0 ; i < a.length ; i++) {
            if(Math.abs(a[i]-b[i]) > 0.01) {
                return false;
            }
        }
        return true;
    }
}
