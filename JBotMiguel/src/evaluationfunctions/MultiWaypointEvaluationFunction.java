package evaluationfunctions;

import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.DifferentialDriveRobot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class MultiWaypointEvaluationFunction extends EvaluationFunction{
	
	private double time = 0;
	private double avgSpeed = 0;
	private int id = 0;
	private int lightPoles = 0;
	
	public MultiWaypointEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		time = simulator.getTime();
		DifferentialDriveRobot r = (DifferentialDriveRobot)simulator.getEnvironment().getRobots().get(0);
		LightPole p = null;
		
		for(PhysicalObject o : simulator.getEnvironment().getAllObjects()) {
			if(o.getType() == PhysicalObjectType.LIGHTPOLE) {
				p = (LightPole)o;
				break;
			}
		}
		
		if(p == null) {
			simulator.stopSimulation();
			lightPoles++;
			this.fitness = lightPoles + (simulator.getEnvironment().getSteps()-simulator.getTime())/simulator.getEnvironment().getSteps();
		} else {
			
			if(id == 0) {
				id = p.getId();
			}
			
			if(id != p.getId()) {
				lightPoles++;
				id = p.getId();
			}
			
			double dist = r.getPosition().distanceTo(p.getPosition());
			
			this.fitness = 1.5-dist/1.5 + lightPoles;
			
			avgSpeed+=r.getLeftWheelSpeed()+r.getRightWheelSpeed();
		}
			
		if(avgSpeed/time/2.0 < 0)
			fitness = 0;
	}
}