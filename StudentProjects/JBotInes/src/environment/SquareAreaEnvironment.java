package environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import sensors.DistanceToBSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Line;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;

public class SquareAreaEnvironment extends Environment {

	private double forageLimit;
	private double forbiddenArea;
	private double distance;
	private double wallsDistance = 100;
	private int numberOfClusters;
	private List<List<Robot>> clusters;
	private boolean connected;
	private double communicationRange;
	
	public SquareAreaEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		distance = arguments.getArgumentAsDoubleOrSetDefault("distance", distance);
		wallsDistance  = arguments.getArgumentAsDoubleOrSetDefault("wallsdistance", wallsDistance);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		if(distance > 0) {
			for(Robot r : robots) {
				double x = simulator.getRandom().nextDouble()*distance*2-distance;
				double y = simulator.getRandom().nextDouble()*distance*2-distance;
				r.setPosition(x, y);
				r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			}
		}
		
		RobotSensor sensor = (RobotSensor) getRobots().get(0).getSensorByType(RobotSensor.class);
		communicationRange = sensor.getRange();
		
		//geofence points
				LinkedList<Vector2d> points = new LinkedList<Vector2d>();
				
				Vector2d upperLeft = new Vector2d(-1*wallsDistance, 1*wallsDistance);
				points.add(upperLeft);
				Vector2d upperRight = new Vector2d(1*wallsDistance, 1*wallsDistance);
				points.add(upperRight);
				Vector2d lowerRight = new Vector2d(1*wallsDistance, -1*wallsDistance);
				points.add(lowerRight);
				Vector2d lowerLeft = new Vector2d(-1*wallsDistance, -1*wallsDistance);
				points.add(lowerLeft);
				
				addLines(points, simulator);
		
	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	protected void addLines(LinkedList<Vector2d> waypoints, Simulator simulator) {

        for (int i = 1; i < waypoints.size(); i++) {

            Vector2d va = waypoints.get(i - 1);
            Vector2d vb = waypoints.get(i);

            simulation.physicalobjects.Line l = new simulation.physicalobjects.Line(simulator, "line" + i, va.getX(), va.getY(), vb.getX(), vb.getY());
            addObject(l);
        }

        Vector2d va = waypoints.get(waypoints.size() - 1);
        Vector2d vb = waypoints.get(0);

        Line l = new Line(simulator, "line0", va.getX(), va.getY(), vb.getX(), vb.getY());
        addObject(l);
    }
	
	@Override
	public void update(double time) {
		updateConnected();
	}

	private void updateConnected() {
		clusters = getClusters(getRobots());
		numberOfClusters = clusters.size();
		connected = numberOfClusters==1;
		
	}
	
	// bottom-up single-linked clustering
    public List<List<Robot>> getClusters(Collection<Robot> robots) {
        List<List<Robot>> clusters = new ArrayList<>();
        // One cluster for each robot
        for (Robot r : robots) {
            List<Robot> cluster = new ArrayList<>();
            cluster.add(r);
            clusters.add(cluster);
        }

        boolean merged = true;
        // stop when the clusters cannot be merged anymore
        
        while(merged) {
            merged = false;
            // find two existing clusters to merge
            for (int i = 0; i < clusters.size() && !merged; i++) {
                for (int j = i + 1; j < clusters.size() && !merged; j++) {
                    List<Robot> c1 = clusters.get(i);
                    List<Robot> c2 = clusters.get(j);
                    // check if the two clusters have (at least) one individual close to the other
                    for (int ri = 0; ri < c1.size() && !merged; ri++) {
                        for (int rj = 0; rj < c2.size() && !merged; rj++) {
                            Robot r1 = c1.get(ri);
                            Robot r2 = c2.get(rj);
                            
                            if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= communicationRange) {
	                            // do the merge
	                            merged = true;
	                            clusters.get(i).addAll(clusters.get(j));
	                            clusters.remove(j);
                            }
                        }
                    }
                }
            }
        }
        return clusters;
    }

	public int getNumberOfClusters() {
		return numberOfClusters;
	}
}