package environment;


import simulation.Simulator;
import simulation.physicalobjects.Nest;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class IntensityForagingNestEnvironments extends IntensityForagingRobotsEnviroments {

	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	protected double nestLimit;
	protected Nest nest;

	public IntensityForagingNestEnvironments(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		nestLimit= arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
	}

	public double getNestRadius() {
		return nestLimit;
	}

	
}