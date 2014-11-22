package simulation.environment;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class DoubleLightPoleEnvironment extends Environment {

	
	@ArgumentsAnnotation(name="lightpoleradius", defaultValue="0.1")
	private double lightPoleRadius;
	
	@ArgumentsAnnotation(name="lightpoledistance", defaultValue="2")
	private double lightPoleDistance;
	
	@ArgumentsAnnotation(name="widthpole", defaultValue="-1")
	private double widthPole;
	
	@ArgumentsAnnotation(name="heightpole", defaultValue="0")
	private double heightPole;
	
	private double forageLimit, forbiddenArea;
	private LightPole lightPoleLeft, lightPoleRight;

	public DoubleLightPoleEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		widthPole = arguments.getArgumentIsDefined("widthpole") ? arguments.getArgumentAsDouble("widthpole") : -1.0;
		heightPole = arguments.getArgumentIsDefined("heightpole") ? arguments.getArgumentAsDouble("heightpole") : 0.0;
		lightPoleDistance = arguments.getArgumentIsDefined("lightpoledistance") ? arguments.getArgumentAsDouble("lightpoledistance") : 2.0;
		lightPoleRadius = arguments.getArgumentIsDefined("lightpoleradius") ? arguments.getArgumentAsDouble("lightpoleradius") : 0.10;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		double auxWidth = widthPole + lightPoleDistance;
		lightPoleLeft = new LightPole(simulator, "Left", widthPole, heightPole,lightPoleRadius);
		lightPoleRight = new LightPole(simulator, "Right", auxWidth, heightPole,lightPoleRadius);
		addObject(lightPoleLeft);
		addObject(lightPoleRight);
	}

	public double getLightPolesRadius() {
		return lightPoleRadius;
	}

	public double getLightPoleDistance() {
		return lightPoleDistance;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void update(double time) {}
}