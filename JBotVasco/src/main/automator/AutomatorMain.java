package main.automator;

import src.Controller;
import src.Main;

public class AutomatorMain extends Main {
	public AutomatorMain(String[] args) {
		super(args);
	}

	@Override
	public synchronized void execute() {
		boolean allEvolved = false;

		while (!allEvolved) {

			allEvolved = true;

			for (Controller c : controllers) {
				allEvolved = allEvolved && c.hasBeenEvolved();
				if (!c.hasBeenEvolved() && c.readyToEvolve() && !c.isEvolving()) {
					System.out.printf("[%s] Evolving %s%n", getClass().getSimpleName(), c.getName());
					AutomatorEvolution evo = new AutomatorEvolution(this, c, defaultArgs);
					evo.start();
				}
			}
			try {
				if (!allEvolved)
					wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}