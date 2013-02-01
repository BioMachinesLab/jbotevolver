package simulation.environment;

import gui.renderer.Renderer;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

public class DoubleLightPoleEnvironment extends Environment {

	private double lightPoleRadius;
	private double lightPoleDistance;
	private double forageLimit, forbiddenArea;
	private LightPole lightPoleLeft, lightPoleRight;

	public DoubleLightPoleEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		double widthPole = arguments.getArgumentIsDefined("width") ? arguments
				.getArgumentAsDouble("width") : -1.0;
		double heightPole = arguments.getArgumentIsDefined("height") ? arguments
				.getArgumentAsDouble("height") : 0.0;
		lightPoleDistance = arguments.getArgumentIsDefined("lightpoledistance") ? arguments
				.getArgumentAsDouble("lightpoledistance") : 2.0;
		double auxWidth = widthPole + lightPoleDistance;
		lightPoleRadius = arguments.getArgumentIsDefined("lightpoleradius") ? arguments
				.getArgumentAsDouble("lightpoleradius") : 0.10;
		lightPoleLeft = new LightPole(simulator, "Left", widthPole, heightPole,
				lightPoleRadius);
		lightPoleRight = new LightPole(simulator, "Right", auxWidth, heightPole,
				lightPoleRadius);
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
	public void draw(Renderer renderer) {
		renderer.drawCircle(lightPoleLeft.getPosition(),
				lightPoleLeft.getRadius());
		renderer.drawCircle(lightPoleRight.getPosition(),
				lightPoleRight.getRadius());
	}
	@Override
	public void update(double time) {}
}