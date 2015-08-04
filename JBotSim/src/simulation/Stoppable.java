/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

/**
 *
 * @author jorge
 */
public interface Stoppable extends Updatable {
    
    public void terminate(Simulator simulator);
    
}
