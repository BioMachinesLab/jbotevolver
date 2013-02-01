package simulation.robot;

import simulation.Simulator;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.collisionhandling.knotsandbolts.Shape;
import simulation.robot.actuators.Actuator;
import simulation.util.MathUtils;

public class ARDrone extends Robot {

	public ARDrone(Simulator simulator, String name, double x, double y,
			double orientation, double mass, double radius,
			double distanceWheels, String color) {
		super(simulator, name, x, y, orientation, mass, radius, distanceWheels, color);
	}

	@Override
	public void updateActuators(int simulationStep, double timeDelta) {	
		
		//leftWheelSpeed rightWheelSpeed
		
		double newX = position.getX()+timeDelta*leftWheelSpeed;
		double newY = position.getY()+timeDelta*rightWheelSpeed;
		
		position.set(newX,newY);
		
//		orientation = MathUtils.modPI2(orientation);
		
		for(Actuator actuator: actuators){
			actuator.apply(this);
		}
	}

}
