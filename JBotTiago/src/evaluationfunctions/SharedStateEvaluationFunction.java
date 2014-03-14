package evaluationfunctions;

import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedStateEvaluationFunction extends EvaluationFunction {

	private static final double MAXFITNESS = 1;
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
//		double numberIntruders = 0;
//		Robot prey = null;
//		
//		for (Robot r : simulator.getEnvironment().getRobots()) {
//			if(r.getDescription().equals("prey")){
//				prey = r;
//			}
//		}
		
		if(sensors == null) {
			sensors = new IntruderSensor[simulator.getRobots().size()-1];
			
			for (int j = 0; j < simulator.getRobots().size(); j++) {
				if(!simulator.getRobots().get(j).getDescription().equals("prey")){
					IntruderSensor s = (IntruderSensor)simulator.getRobots().get(j).getSensorByType(IntruderSensor.class);
					sensors[j] = s;
				}
			}	
		}
		
//		double bonus = 0;  
		int robotsSeeingIntruder = 0;
		
		for(int j = 0 ; j < sensors.length ; j++){
			IntruderSensor s = sensors[j];
			if (s.foundIntruder()) {
				robotsSeeingIntruder++;
//				double angle = Math.abs(s.getIntruderOrientation());
//				bonus += s.getSensorReading(0)/(simulator.getRobots().size()-1);
//				if (angle <= 7){
//					numberIntruders++;
//					double reward = (s.getInitialOpeningAngle()/2-angle)/(s.getInitialOpeningAngle()/2);
//					fitness += reward * 1.0/simulator.getRobots().size()/simulator.getEnvironment().getSteps();
//				}
			}
		}
		
		
		if(robotsSeeingIntruder == 1){
			fitness += (MAXFITNESS/2)/simulator.getEnvironment().getSteps();
		}else if (robotsSeeingIntruder > 1) {
			fitness += MAXFITNESS/simulator.getEnvironment().getSteps();
		}
		
//		if(bonus > 0) {
//			bonus /= (double)simulator.getEnvironment().getSteps();
//			fitness+=bonus;
//		}
		
		
		for (Robot r : simulator.getRobots()) {
			
			if(!r.getDescription().equals("prey")){
//				double distanceToPrey = r.getPosition().distanceTo(prey.getPosition()) - (r.getRadius() - prey.getRadius());
//				
//				if (distanceToPrey < 1)
//					fitness += (1-distanceToPrey) * 0.2/simulator.getRobots().size()/simulator.getEnvironment().getSteps();
//				
				DifferentialDriveRobot dr = (DifferentialDriveRobot) r;			
				averageWheelSpeed += ((dr.getLeftWheelSpeed() + dr.getRightWheelSpeed())/2)/(simulator.getRobots().size()-1);
				
//				if(r.isInvolvedInCollison()){                                                                           
//					simulator.stopSimulation();                                                                                    
//				} 
			
//				if(distanceToPrey < 0.5){                                                                               
//					fitness += distanceToPrey * 0.2/simulator.getRobots().size()/simulator.getEnvironment().getSteps();    
//				} 
			}
		}
		
//		if (numberIntruders == simulator.getRobots().size()){
//			simulator.stopSimulation();
//			fitness += 1 + (simulator.getEnvironment().getSteps() - simulator.getTime())/simulator.getEnvironment().getSteps();
//		}

		timesteps = simulator.getTime();
		
	}
	
	@Override
	public double getFitness() {
		if (averageWheelSpeed/timesteps < 0) {
			return -1;
		}else
			return super.getFitness();
	}
	
}