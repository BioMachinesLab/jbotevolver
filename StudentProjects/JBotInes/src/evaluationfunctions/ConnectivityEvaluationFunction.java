package evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;
import environment.NestEnvironment;
import environment.OpenEnvironment2;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ConnectivityEvaluationFunction extends EvaluationFunction{

	private Double timestep = 0.0;
	private NestEnvironment env;
	private int numberOfSteps = 0;
	private int clustersSum = 0;

	public ConnectivityEvaluationFunction(Arguments args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(Simulator simulator) {
		timestep = simulator.getTime();
		env = (NestEnvironment) simulator.getEnvironment();
		numberOfSteps = env.getSteps();
		
		if(timestep % 10 == 0){
			clustersSum += env.getNumberOfClusters();
		}
		
		fitness = 1 / (clustersSum / (env.getSteps()/10.0));
		
	}

}
