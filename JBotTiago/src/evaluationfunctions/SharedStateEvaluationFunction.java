package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedStateEvaluationFunction extends EvaluationFunction {

	public SharedStateEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		int robotsCloseToPrey = 0;
		
		for(Robot r : simulator	.getRobots()) {
			
			Prey p = simulator.getEnvironment().getPrey().get(0);
			
			double distanceToPrey = (r.getPosition().distanceTo(p.getPosition())) - (r.getRadius() + p.getRadius());
			
			if(distanceToPrey < 1) {
			
				fitness += (1-distanceToPrey) * 1.0/simulator.getRobots().size()/simulator.getEnvironment().getSteps();
				
				if(distanceToPrey <= 0.1){
					robotsCloseToPrey++;
				}
			}
		}
		
		if (robotsCloseToPrey == simulator.getRobots().size()){
			simulator.stopSimulation();
			fitness += 1 + (simulator.getEnvironment().getSteps() - simulator.getTime())/simulator.getEnvironment().getSteps();
		}

	}
	
	@Override
	public double getFitness() {
		return super.getFitness();
	}

}
