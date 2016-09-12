package physicalobjects;


import java.awt.Color;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;

public class IntensityPrey extends Prey {
	private double intensity;

	public IntensityPrey(Simulator simulator, String name, double x, double y,
			double angle, double mass, double radius, double intensity) {
		super(simulator, name, x, y, angle, mass, radius);
		this.intensity = intensity;
		
	}

	public IntensityPrey(Simulator simulator, String name, Vector2d position,
			int angle, double mass, double radius, double intensity) {
		this(simulator, name, position.x, position.y, angle, mass, radius,
				intensity);

	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	@Override
	public void teleportTo(Vector2d pos) {
		position = new Vector2d(pos);
		env.addTeleported(this);
	}

}
