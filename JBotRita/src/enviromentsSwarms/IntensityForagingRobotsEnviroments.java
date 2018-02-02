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

	@ArgumentsAnnotation(name = "preyIntensity", defaultValue = "30.0")
	protected double intensity;
	
	public IntensityForagingRobotsEnviroments(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		intensity = arguments.getArgumentIsDefined("preyIntensity") ? arguments
				.getArgumentAsDouble("preyIntensity") : 30.0;
	}


	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			numberOfFoodSuccessfullyForaged = (int) (intensity - prey
					.getIntensity());

			if (numberOfFoodSuccessfullyForaged == robots.size()) {
				prey.setColor(Color.WHITE);
				simulator.stopSimulation();
			}
			
			if (prey.getIntensity() > 0)
				prey.setColor(Color.BLACK);
		}

	}	
	
}