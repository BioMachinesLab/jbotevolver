package evaluationfunctions;

import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedStateEvaluationFunction extends EvaluationFunction {

	private IntruderSensor sensors[];
	private double averageWheelSpeed;
	private double timesteps;
	
	public SharedStateEvaluationFunction(Arguments args) {
		super(args);
		averageWheelSpeed = 0.0;
		timesteps = 0.0;
	}

	@Override
	public void update(Simulator simulator) {
		double numberIntruders = 0;
		Prey p = simulator.getEnvironment().getPrey().get(0);
		
		if(sensors == null) {
			sensors = new IntruderSensor[simulator.getRobots().size()];
			
			for (int j = 0; j < simulator.getRobots().size(); j++) {
				IntruderSensor s = (IntruderSensor)simulator.getRobots().get(j).getSensorByType(IntruderSensor.class);
				sensors[j] = s;
			}	
		}

		for(int j = 0 ; j < sensors.length ; j++){
			IntruderSensor s = sensors[j];
			if (s.foundIntruder()) {
				numberIntruders++;
				double angle = Math.abs(s.getIntruderOrientation());
				if (angle <= 7){
					double reward = (s.getInitialOpeningAngle()/2-angle)/(s.getInitialOpeningAngle()/2);
					fitness += reward * 1.0/simulator.getRobots().size()/simulator.getEnvironment().getSteps();
				}
			}
		}
		
		for (Robot r : simulator.getRobots()) {
			double distanceToPrey = r.getPosition().distanceTo(p.getPosition()) - (r.getRadius() - p.getRadius());
			
			if (distanceToPrey < 1)
				fitness += (1-distanceToPrey) * 0.2/simulator.getRobots().size()/simulator.getEnvironment().getSteps();
			
			DifferentialDriveRobot dr = (DifferentialDriveRobot) r;			
			averageWheelSpeed += ((dr.getLeftWheelSpeed() + dr.getRightWheelSpeed())/2)/simulator.getRobots().size();
			
		}
		
		if (numberIntruders == simulator.getRobots().size()){
			simulator.stopSimulation();
			fitness += 1 + (simulator.getEnvironment().getSteps() - simulator.getTime())/simulator.getEnvironment().getSteps();
		}

		timesteps = simulator.getTime();
		
		
	}
	
	@Override
	public double getFitness() {
		if (averageWheelSpeed/timesteps < 0) {
			return 0;
		}else
			return super.getFitness();
	}

}
