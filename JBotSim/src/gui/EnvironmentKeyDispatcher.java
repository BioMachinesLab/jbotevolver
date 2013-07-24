package gui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

import simulation.Simulator;

public class EnvironmentKeyDispatcher implements KeyEventDispatcher {
	protected Simulator simulator;
	
	
	public EnvironmentKeyDispatcher(Simulator simulator) {
		super();
		this.simulator = simulator;
	}

	public boolean dispatchKeyEvent(KeyEvent e) {
		boolean discardEvent = false;
		if(simulator != null && simulator.getEnvironment() != null) {
			if (e.getID() == KeyEvent.KEY_TYPED) {
				simulator.getEnvironment().keyTyped(e);
			}
	
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				simulator.getEnvironment().keyPressed(e);
			}
	
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				simulator.getEnvironment().keyReleased(e);
			}
		}

		return discardEvent;
	}
}

