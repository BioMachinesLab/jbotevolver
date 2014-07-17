package evaluationfunctions;

import java.util.HashMap;
import java.util.LinkedList;

import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class SharedStateEvaluationFunction extends EvaluationFunction {

	private static final double MAXFITNESS = 1;
	private IntruderSensor sensors[];
	private double averageWheelSpeed;
	private double timesteps;
	private int programmesRobots = 0;
	private HashMap<Integer,Integer> preysSeen;
	
	public SharedStateEvaluationFunction(Arguments args) {
		super(args);
		preysSeen = new HashMap<Integer, Integer>();
		averageWheelSpeed = 0.0;
		timesteps = 0.0;
	}

	@Override
	public void update(Simulator simulator) {

		preysSeen.clear();
		
		if(sensors == null) {
			
			Arguments programmedRobotArguments = simulator.getArguments().get("--programmedrobots");
			programmesRobots = programmedRobotArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
			
			int count = 0;
			for(Robot r : simulator.getRobots()) {
				if(r.getSensorByType(IntruderSensor.class) != null)
					count++;
			}
			
			sensors = new IntruderSensor[count];
			
			for (int j = 0; j < simulator.getRobots().size(); j++) {
				if(!simulator.getRobots().get(j).getDescription().equals("prey")){
					IntruderSensor s = (IntruderSensor)simulator.getRobots().get(j).getSensorByType(IntruderSensor.class);
					sensors[j] = s;
				}
			}	
		}
		
		for(Integer i : preysSeen.keySet()){
			preysSeen.put(i, 0);
		}
		
		for(int j = 0 ; j < sensors.length ; j++){
			IntruderSensor s = sensors[j];
			if (s.foundIntruder()) {
				LinkedList<PhysicalObject> intruders = s.getEstimatedIntruder();
				for(PhysicalObject p : intruders){
					if(preysSeen.get(p.getId()) == null)
						preysSeen.put(p.getId(), 1);
					else
						preysSeen.put(p.getId(), preysSeen.get(p.getId())+1);
				}
			}
		}
		
		for(Integer i : preysSeen.keySet()){
			double seeing = preysSeen.get(i);
			if(seeing == 1){
				fitness += (MAXFITNESS/2)/simulator.getEnvironment().getSteps()/programmesRobots;
			}else if (seeing > 1) {
				fitness += MAXFITNESS/simulator.getEnvironment().getSteps()/programmesRobots;
			}
		}
		
		
		for (Robot r : simulator.getRobots()) {
			
			if(!r.getDescription().equals("prey")){
				DifferentialDriveRobot dr = (DifferentialDriveRobot) r;			
				averageWheelSpeed += ((dr.getLeftWheelSpeed() + dr.getRightWheelSpeed())/2)/(sensors.length);
			}
		}
		
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