package environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import mathutils.Vector2d;
import controllers.WalkerController;
import sensors.DistanceToBSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.robot.Robot;
import simulation.robot.sensors.NestSensor;
import simulation.util.Arguments;

public class NestEnvironment extends Environment{

	private Nest nest;
	private double nestLimit;
	private ArrayList<Robot> preprogRobots = new ArrayList<Robot>();
	private ArrayList<Robot> evolvedRobots = new ArrayList<Robot>();
	private Random random;
	private List<List<Robot>> clusters;
	private int numberOfClusters;
	private boolean connected;
	private double shortRange = 0.0;
	private double longRange = 0.0;
	
	public NestEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		nestLimit = args.getArgumentIsDefined("nestlimit") ? args.getArgumentAsDouble("nestlimit") : .5;
		random = simulator.getRandom();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
		
		Arguments args = simulator.getArguments().get("--preprogrammed");
		int numberOfWalkers = args.getArgumentAsIntOrSetDefault("numberofrobots",1);
		for(int i = 0; i < numberOfWalkers; i++){
			Robot walker = Robot.getRobot(simulator, args);
			walker.setController(new WalkerController(simulator, walker, args));
			walker.setPosition(newRandomPosition()); //random dispersion from center
			addRobot(walker);
		}	
		
		for(Robot r: simulator.getRobots()){
			if(r.getDescription().equals("type0"))
				preprogRobots.add(r);
			else if(r.getDescription().equals("type1")) {
				evolvedRobots.add(r);
			}
		}
		
		shortRange = ((NestSensor) preprogRobots.get(0).getSensorByType(NestSensor.class)).getRange();
		longRange = ((DistanceToBSensor) evolvedRobots.get(0).getSensorByType(DistanceToBSensor.class)).getRange();
	}
	
	@Override
	public void update(double time) {
		clusters = getClusters(getRobots()); 
		numberOfClusters = clusters.size();
		connected = numberOfClusters==1;		
	}
	
	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*nestLimit*10+nestLimit;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}

	public boolean isConnected() {
		return connected;
	}

	public int getNumberOfClusters() {
		return numberOfClusters;
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

							if (r1.getPosition().distanceTo(r2.getPosition()) - r1.getRadius() - r2.getRadius() <= shortRange) {
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
}
