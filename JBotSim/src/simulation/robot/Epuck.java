package simulation.robot;

import simulation.Simulator;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.EpuckLightSensor;

public class Epuck extends Robot {

	public Epuck(Simulator simulator, String name, double x, double y,
			double orientation, double mass, double radius,
			double distanceBetweenwheels, String color) {
		super(simulator, name, x, y, orientation, mass, radius, distanceBetweenwheels,
				color);
		// TODO Auto-generated constructor stub
	}

	private EpuckLightSensor lightSensor;
	
	public void setLightSensor(EpuckLightSensor epuckLightSensor) {
		this.lightSensor = epuckLightSensor;
	}
	
	public EpuckLightSensor getLightSensor() {
		return lightSensor;
	}
	
	private EpuckIRSensor irSensor;

	public void setIRSensor(EpuckIRSensor epuckIRSensor) {
		this.irSensor = epuckIRSensor;
	}
	public EpuckIRSensor getIrSensor() {
		return irSensor;
	}

	
}
