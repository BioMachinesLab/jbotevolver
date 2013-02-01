package factories;

import simulation.Simulator;

public class Factory {
	protected Simulator simulator;

	public Factory(Simulator simulator) {
		super();
		this.simulator = simulator;
	}

}
