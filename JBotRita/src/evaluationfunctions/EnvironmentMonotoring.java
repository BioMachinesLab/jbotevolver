package evaluationfunctions;

import java.util.LinkedList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class EnvironmentMonotoring extends EvaluationFunction {

		private Simulator simulator;
		LinkedList<Integer> positionsOccupied = new LinkedList<Integer>();
		private int numberCollisions;

		
		public EnvironmentMonotoring(Arguments args) {
			super(args);
		}
		
		@Override
		public double getFitness() {
				System.out.println("positionsOccupied.size()" + positionsOccupied.size());
				Environment env=simulator.getEnvironment();
				double max_PossibleNumberOfCollisions=simulator.getTime()*env.getRobots().size();
				double penalty_for_collision= -numberCollisions/max_PossibleNumberOfCollisions;
				return positionsOccupied.size()/(double)( env.getHeight()*env.getWidth())+ penalty_for_collision; 
		}
		
		@Override
		public void update(Simulator simulator) { 
			this.simulator=simulator;
			for(Robot r : simulator.getEnvironment().getRobots()){
				Vector2d robot_pos=r.getPosition();
				int posOfGrid=((int) robot_pos.x)*10 + (int) robot_pos.y;
				if(!positionsOccupied.contains(posOfGrid)){
					positionsOccupied.add(posOfGrid);
				}
				
				if(r.isInvolvedInCollison()){
					numberCollisions++;
				}
			}		
		}
		

	}