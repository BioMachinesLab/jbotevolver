/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ArtificialPheromoneObject;
import simulation.robot.Robot;
import simulation.robot.sensors.NearRobotSensor;

/**
 *
 * @author Borga
 */

public class CenterOfMassEvaluationFunction extends EvaluationFunction {

	/**
	 * 
	 */
	private int steps = 0;
	private double fitness;

	public CenterOfMassEvaluationFunction(Simulator simulator){
		super(simulator);
	}

	public void update(double time){
		steps++;
		Vector2d coord = new Vector2d();
		double x=0.0, y=0.0;
		double distance = 0.0;
		double auxDist = 0.0;
		double finalDist = 0.0;
		int near = 0;
		int nRobots = simulator.getEnvironment().getRobots().size();


		for(Robot r: simulator.getEnvironment().getRobots()){
			x+=r.getPosition().x;
			y+=r.getPosition().y;
		}
		x=x/simulator.getEnvironment().getRobots().size();
		y=y/simulator.getEnvironment().getRobots().size();
		coord.set(x, y);


		for(Robot t: simulator.getEnvironment().getRobots()){
			distance = coord.distanceTo(t.getPosition());
			if(distance <0.3)
				distance = 0;
//			if(distance >((NearRobotSensor) t.getSensorWithId(1)).getRange()){
//				distance =((NearRobotSensor) t.getSensorWithId(1)).getRange();
//			}
//			auxDist += (1-(distance/((NearRobotSensor) t.getSensorWithId(1)).getRange()));
			near+=nearRobots(t);
		}

		/*for(ArtificialPheromoneObject a: simulator.getEnvironment().getPlacedPheromones()){
        	a.evapore();
        }
        simulator.getEnvironment().removePheromones();*/

		finalDist = (((auxDist/nRobots)*10)/11)+((near/(nRobots*(nRobots-1)))/11);

		fitness +=(finalDist);
	}
	@Override
	public double getFitness() {
		return fitness/1200;
	}

	private int nearRobots(Robot robot){
		double distance = Integer.MAX_VALUE;
		int near = 0;
		Vector2d coord = new Vector2d();
		coord = robot.getPosition();
		for (Robot t: simulator.getEnvironment().getRobots()){
			distance = coord.distanceTo(t.getPosition());
			if (distance <0.3 && distance !=0){
				near ++;
			}
		}
		return near;
	}


}
