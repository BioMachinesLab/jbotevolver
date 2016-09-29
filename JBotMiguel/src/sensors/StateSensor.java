package sensors;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import actuators.StateActuator;

public class StateSensor extends Sensor {
	
	private int numberOfStates;
	private int idActuator;
	private double[] readings;
	private Simulator simulator;
	
	public StateSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		numberOfStates = args.getArgumentAsIntOrSetDefault("states", 1);
		
		if(!args.getArgumentIsDefined("idactuator"))
			throw new RuntimeException("StateSensor needs 'idactuator' argument");
		
		idActuator = args.getArgumentAsInt("idactuator");
		readings = new double[numberOfStates];
		this.simulator = simulator;
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		super.update(time, teleported);
		
		for(int i = 0 ; i < readings.length ; i++)
			readings[i] = 0;
		
		double nRobots = simulator.getRobots().size();
		
		for(Robot r : simulator.getRobots()) {
			if(r.getId() != robot.getId()) {
				Actuator a = r.getActuatorWithId(idActuator);
				StateActuator sa = (StateActuator)a;
				for(int i = 0 ; i < readings.length ; i++) {
					readings[i]+= sa.getState(i)/(nRobots-1);
				}
			}
		}
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}
	
	public int getNumberOfStates() {
		return numberOfStates;
	}
}