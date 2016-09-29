package novelty.evaluators;

import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import novelty.EvaluationResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 * @author jorge
 */
public class FinalPositionBehavior extends GenericEvaluationFunction {

	private VectorBehaviourResult vbr;
    private Vector2d pos;
	
    public FinalPositionBehavior(Arguments args) {
		super(args);
	}

    @Override
    public EvaluationResult getEvaluationResult() {
    	vbr = new VectorBehaviourResult(pos.getX(),  pos.getY());
        return vbr;
    }
    
    @Override
    public void update(Simulator simulator) {
    	Robot r = simulator.getRobots().get(0);
    	pos = r.getPosition();
    }
}