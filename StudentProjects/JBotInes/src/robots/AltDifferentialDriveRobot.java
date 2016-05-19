package robots;

import java.util.ArrayList;
import java.util.LinkedList;

import sensors.DistanceToASensor;
import sensors.TypeARobotSensor;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class AltDifferentialDriveRobot extends DifferentialDriveRobot {
	private ArrayList<Robot> robots;
	private LinkedList<Robot> neighbors = new LinkedList<Robot>();

	public AltDifferentialDriveRobot(Simulator simulator, Arguments args) {
		super(simulator, args);

		robots = simulator.getEnvironment().getRobots();
	}

	@Override
	public void updateSensors(double simulationStep, ArrayList<PhysicalObject> teleported) {
		updateNeighbors();
		super.updateSensors(simulationStep, teleported);
	}
	
	public LinkedList<Robot> getNeighbors() {
		return neighbors;
	}

	public void updateNeighbors() {
		
		neighbors.clear();

		for(Robot r: robots){
			if(r instanceof AltDifferentialDriveRobot){
				DistanceToASensor sensor = (DistanceToASensor) r.getSensorByType(DistanceToASensor.class);
				if(r.getId() != this.getId() && this.getPosition().distanceTo(r.getPosition()) < sensor.getRange() - r.getDiameter()) {
					AltDifferentialDriveRobot robot = (AltDifferentialDriveRobot) r;
					if(!neighbors.contains(robot)){
						neighbors.add(robot);
						//getNeighborsNeighbors(robot);
					}
				}

			}
		}
	}
	
	private void getNeighborsNeighbors(AltDifferentialDriveRobot robot){
		if(!robot.getNeighbors().isEmpty()){
			for(Robot neighbor: robot.getNeighbors()){
				if(!this.neighbors.contains(neighbor) && neighbor.getId() != this.getId())
					neighbors.add(neighbor);
			}
		}
	}

}