package simulation.robot;

import java.util.ArrayList;

import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.utils.CIArguments;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class CISensorWrapper extends Sensor{

	@ArgumentsAnnotation(name="ci", defaultValue="")
	private CISensor cisensor;
	
	public CISensorWrapper(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		String name = args.getArgumentValue("ci");
		
		args.removeArgument("classname");
		args.removeArgument("ci");
		args.setArgument("classname", name);
		
		CIArguments ciargs = new CIArguments(args.getCompleteArgumentString(),true);
		
		cisensor = CISensor.getSensor(((AquaticDroneCI)this.robot), ciargs.getArgumentAsString("classname"), ciargs);
		((AquaticDroneCI)this.robot).getCISensors().add(cisensor);
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return cisensor.getSensorReading(sensorNumber);
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		cisensor.update(time, ((AquaticDroneCI)this.robot).getEntities());
	}

}
