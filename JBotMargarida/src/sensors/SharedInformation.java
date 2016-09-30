package sensors;
import java.io.Serializable;

import mathutils.Vector2d;


public class SharedInformation implements Serializable {
	private Vector2d position;
	private double intensity;
	private double timeStep;
	
	public SharedInformation(Vector2d position, double intensity, double timeStep) {
		super();
		this.position = position;
		this.intensity = intensity;
		this.timeStep = timeStep;
	}

	public Vector2d getPosition() {
		return position;
	}

	public void setPosition(Vector2d position) {
		this.position = position;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public double getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	
	
	
}
