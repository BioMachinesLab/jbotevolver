package environment;

import java.awt.Color;

import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class IntensityForagingRobotsEnviroments extends
		RoundForageIntensityEnvironment {

	@ArgumentsAnnotation(name = "preyIntensity", defaultValue = "nºof robots")
	protected double intensity;
	
	private boolean setIntensityWithRobotsSize;

	public IntensityForagingRobotsEnviroments(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		
		if(arguments.getArgumentIsDefined("preyIntensity")){
			intensity=arguments.getArgumentAsDouble("preyIntensity");
		}else{
			setIntensityWithRobotsSize=true;
		}
		
		//Robot a=new Robot();
	}
	
	@Override
	public void setup(Simulator simulator) {
		if(setIntensityWithRobotsSize)
			intensity=robots.size();
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
				System.out.println("entrei aqui" +intensity );

				prey.setColor(Color.WHITE);
				simulator.stopSimulation();
			}
			else
				prey.setColor(Color.BLACK);
		}

	}	
	
}