package epuck;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.util.Arguments;

public class Epuck extends DifferentialDriveRobot {

	private EpuckLightSensor lightSensor;
	private EpuckIRSensor irSensor;
	
	public Epuck(Simulator simulator, Arguments args) {
		super(simulator, args);
	}
	
	public void setLightSensor(EpuckLightSensor epuckLightSensor) {
		this.lightSensor = epuckLightSensor;
	}
	
	public EpuckLightSensor getLightSensor() {
		return lightSensor;
	}

	public void setIRSensor(EpuckIRSensor epuckIRSensor) {
		this.irSensor = epuckIRSensor;
	}
	public EpuckIRSensor getIrSensor() {
		return irSensor;
	}	
}