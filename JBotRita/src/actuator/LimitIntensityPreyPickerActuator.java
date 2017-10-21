package actuator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import robots.JumpingRobot;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class LimitIntensityPreyPickerActuator extends
		Jump_IntensityPreyPickerActuator {
	
	@ArgumentsAnnotation(name = "limitOfTaking", defaultValue = "1")
	protected double limitOfTaking;
	
	public LimitIntensityPreyPickerActuator(Simulator simulator,
			int id, Arguments args) {
		super(simulator, id, args);
		
		this.limitOfTaking = args.getArgumentAsDoubleOrSetDefault(
				"limitOfTaking", 1);
	}
	
	@Override
	public void apply(Robot robot,double timeDelta) {
			if(isToPick){
				if(taking<=limitOfTaking){
					findBestPrey(robot);
					if (bestPrey != null) {
	
						if (robot instanceof JumpingRobot) {
							if (!((JumpingRobot) robot).ignoreWallCollisions()) {
								pickUpPrey(robot, bestPrey);
							}
						} else {
							pickUpPrey(robot, bestPrey);
						}
					}
				}
			}
	}

	@Override
	public void pickUpPrey(Robot robot, IntensityPrey prey) {
			isPicking=true;
			prey.setIntensity(prey.getIntensity() - taking);
			limitOfTaking=limitOfTaking-taking;
	}
	
	

}