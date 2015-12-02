package novelty.metrics;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import novelty.EvaluationResult;
import novelty.VectorBehaviourResult;

/**
 * @author jorge
 */
public class MazeBehaviourFinal extends GenericEvaluationFunction {

    public MazeBehaviourFinal(Arguments args) {
		super(args);
	}

	private VectorBehaviourResult vbr;
    private Vector2d pos;

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