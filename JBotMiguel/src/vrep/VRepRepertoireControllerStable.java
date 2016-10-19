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
public class VRepRepertoireControllerStable extends VRepRepertoireController {
    
    private float[] lastBehav = null;
    
    public VRepRepertoireControllerStable(float[] parameters) {
        super(parameters);
    }

    @Override
    public float[] controlStep(float[] inputs) {
        float[] newBehav = super.controlStep(inputs);
        if(newBehav == null) {
            return null;
        }
        
        if(lastBehav == null || !same(newBehav, lastBehav)) {
            lastBehav = newBehav;
            return lastBehav;
        } else {
            return null;
        }
    }  
    
 static boolean same(float[] a, float[] b) {
        if(a == b) {
            return true;
        }
        if(a.length != b.length) {
            return false;
        }
        for(int i = 0 ; i < a.length ; i++) {
            if(Math.abs(a[i]-b[i]) > 0.001) {
                return false;
            }
        }
        return true;
    }    
}
