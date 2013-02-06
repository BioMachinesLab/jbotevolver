package simulation.robot.actuators;

import java.lang.reflect.Constructor;

import simulation.Simulator;
import simulation.SimulatorObject;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class Actuator extends SimulatorObject {
	protected int id;
	
	public Actuator(Simulator simulator, int id, Arguments args) {
		super();
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public abstract void apply(Robot robot);
	
	public static Actuator getActuator(Simulator simulator, String name, Arguments arguments) {
		
		int id = arguments.getArgumentAsIntOrSetDefault("id",0);
		
		try {
			Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 3 && params[0] == Simulator.class &&
						params[1] == int.class &&
						params[2] == Arguments.class)
					return (Actuator) constructor.newInstance(simulator,id,arguments);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		throw new RuntimeException("Unknown actuator: " + name);
	}
}