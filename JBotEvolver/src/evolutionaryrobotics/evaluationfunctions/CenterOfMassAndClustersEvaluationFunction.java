package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.*;
import simulation.util.ClusterUtil;

public class CenterOfMassAndClustersEvaluationFunction extends EvaluationFunction{

	private double fitness;
	private int numClusters=0;
	private static final long serialVersionUID = 1L;

	public CenterOfMassAndClustersEvaluationFunction(Simulator simulator) {
		super(simulator);

	}

	public void update(double time){
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
		x=x/nRobots;
		y=y/nRobots;
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


		numClusters=getNumClusters();

		finalDist = (((auxDist/nRobots)*2)/3)+(float)(1.0/numClusters)/3;
		//finalDist = (float) (1.0/numClusters);
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

	private ArrayList<ClusterUtil> setClusters(){
		ArrayList<ClusterUtil> clusters = new ArrayList<ClusterUtil>();
		for(Robot r: simulator.getEnvironment().getRobots()){
			clusters.add(new ClusterUtil(r));
		}
		return clusters;
	}

	private int getNumClusters(){
		ArrayList<ClusterUtil> clusters = setClusters();
		int clusterNumber=0;
		for(ClusterUtil r: clusters){
			searchNeighbors(clusters, r);
		}
		for(ClusterUtil c: clusters){
			if(c.getClusterElements().size()!=0)
				clusterNumber++;
		}
		return clusterNumber;
	}

	private void searchNeighbors(ArrayList<ClusterUtil> clusters, ClusterUtil cluster){
		for(ClusterUtil c: clusters){
			if(!cluster.equals(c) && c.getClusterElements().size()!=0){
				if(cluster.getDistance(c)<0.3+(0.01*simulator.getEnvironment().getRobots().size())){
					cluster.addRobots(c.getClusterElements());
					c.removeAllRobots();				
				}
			}
		}
	}
}
