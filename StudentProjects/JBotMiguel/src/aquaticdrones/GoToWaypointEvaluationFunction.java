package aquaticdrones;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GoToWaypointEvaluationFunction extends EvaluationFunction{
	
	private double avgSpeed = 0;
	private double time = 0;
	double totalDist = 0;
	double bonus = 0;
	double collisionPenalty = 0;
	
	public GoToWaypointEvaluationFunction(Arguments args) {
		super(args);
		collisionPenalty = args.getArgumentAsDoubleOrSetDefault("collisions",collisionPenalty);
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
		
		double dist = r.getPosition().distanceTo(p.getPosition());
		if(totalDist == 0)
			totalDist = p.getDistanceBetween(new Vector2d(0,0));
		
		this.fitness = (totalDist-dist)/totalDist;
		if(dist < 3) {
			this.fitness = 1;
			if(r.getStopTimestep() > 0) {
				bonus+=1.0/simulator.getEnvironment().getSteps();
			}
		}
		
		avgSpeed+=r.getLeftWheelSpeed()+r.getRightWheelSpeed();
		
		if(avgSpeed/time/2.0 < 0)
			fitness = 0;
		
		if(collisionPenalty > 0 && r.isInvolvedInCollison())
			bonus-=collisionPenalty;
	}
	
	@Override
	public double getFitness() {
		return super.getFitness() + bonus;
	}
	
}