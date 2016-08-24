package evorbc.qualitymetrics;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class RadialQualityMetric extends EvaluationFunction{

	protected double orientation = 0;
	protected boolean useDistance = false;
	protected DistanceQualityMetric dqm;
	protected Vector2d position;
	
	public RadialQualityMetric(Arguments args) {
		super(args);
		
		useDistance = args.getFlagIsTrue("usedistance");
		
		if(useDistance)
			dqm = new DistanceQualityMetric(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		position = new Vector2d(r.getPosition());
		orientation = r.getOrientation();
		
		if(useDistance)
			dqm.update(simulator);
	}
	
	@Override
	public double getFitness() {
		double fitness = 0;
		
		if(useDistance)
			fitness+=dqm.getFitness();
		
		return 0;//TODO finish this
		
	}

}
