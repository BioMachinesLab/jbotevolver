package novelty.evaluators;

import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import novelty.EvaluationResult;
import novelty.results.VectorBehaviourExtraResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FinalPositionWithOrientationBehavior extends GenericEvaluationFunction {

	private VectorBehaviourResult vbr;
    private Vector2d pos;
    private double orientation;
	
    public FinalPositionWithOrientationBehavior(Arguments args) {
		super(args);
	}
    
    public FinalPositionWithOrientationBehavior(Arguments args, Vector2d pos, double orientation) {
		this(args);
		this.pos = pos;
		this.orientation = orientation;
	}

    @Override
    public EvaluationResult getEvaluationResult() {
    	vbr = new VectorBehaviourExtraResult(orientation, pos.getX(),  pos.getY());
        return vbr;
    }
    
    @Override
    public void update(Simulator simulator) {
    	Robot r = simulator.getRobots().get(0);
    	pos = r.getPosition();
    	orientation = r.getOrientation();
    }
    
    public double getOrientation() {
		return orientation;
	}
}