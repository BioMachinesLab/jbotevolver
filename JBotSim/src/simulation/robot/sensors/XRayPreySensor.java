package simulation.robot.sensors;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class XRayPreySensor extends SimpleLightTypeSensor {

	protected GeometricCalculator calc;
	
	public XRayPreySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowOrderedPreyChecker(robot.getId()));
		this.calc = simulator.getGeoCalculator();
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(getCloseObjects() != null)
			getCloseObjects().update(time, teleported);
		try { 
			for(int j = 0; j < numberOfSensors; j++){
				readings[j] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				Prey prey = (Prey)source.getObject();
				if (prey.isEnabled() || prey.getHolder()!=null && prey.getHolder()!=robot){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(calc.getDistanceBetween(
							sensorPosition, source.getObject()));
				}
			}

		} catch (Exception e) {
			e.printStackTrace(); 
		}

	}
}
