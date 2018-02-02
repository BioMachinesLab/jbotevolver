package enviromentsSwarms;

import java.awt.Color;

import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import environment.ForagingIntensityPreysEnvironment;

public class IntensityForagingRobotsEnviroments extends
		ForagingIntensityPreysEnvironment {

	@ArgumentsAnnotation(name = "preyIntensity", defaultValue = "nºof robots")
	protected double intensity;

	
	public IntensityForagingRobotsEnviroments(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		intensity = arguments.getArgumentIsDefined("preyIntensity") ? arguments
				.getArgumentAsDouble("preyIntensity") : robots.size();
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
	}
	
	@Override
	protected void addPrey(int i){
		this.addPrey(new IntensityPrey(simulator, "Prey " + i,
				randomPosition(), 0, PREY_MASS, 0.1, intensity));
	}


	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			numberOfFoodSuccessfullyForaged = (int) (intensity - prey
					.getIntensity());

			if (numberOfFoodSuccessfullyForaged >= intensity) {
				prey.setColor(Color.WHITE);
				simulator.stopSimulation();
			}
			else
				prey.setColor(Color.BLACK);
		}

	}	
	
}