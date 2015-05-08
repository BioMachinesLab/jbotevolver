package evaluationfunctions;

import java.util.ArrayList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction6 extends EvaluationFunction{

	private ArrayList<Robot> preys = new ArrayList<Robot>();
	private OpenEnvironment env;

	public CoveredAreaEvaluationFunction6(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {

		env = (OpenEnvironment) simulator.getEnvironment();
		preys = env.getPreyRobots();

		double preyFitness = 0;

		if(env.isConnected()) {

			for(Robot r: simulator.getEnvironment().getRobots()){

				if((r.getDescription().equals("type0") && r.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0)==1)
						|| r.getDescription().equals("type1")) {
					for(Robot prey: preys){
						double distance = prey.getPosition().distanceTo(r.getPosition());
						if(distance < 1)
							preyFitness += 1/distance;
					}
				} 

			}

		}

		
		fitness += preyFitness * 0.00001;

	}
	
	@Override
	public double getFitness() {
		return fitness + env.getNumberOfFoodSuccessfullyForaged()*50;
	}
}
