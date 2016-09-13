package evaluationfunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import robots.JumpingSumo;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import enviromentsSwarms.EmptyEnviromentsWithFixPositions;
import enviromentsSwarms.FireEnv;
import environments_JumpingSumoIntensityPreys.JS_Environment;

public class FlockingEvaluationFunction extends JumpingSumoEvaluationFunction {

	private double current = 0.0;
	private double clusters = 0.0;
	private HashMap<Robot, Vector2d> robotsPosition = new HashMap<Robot, Vector2d>();
	private double sumOfDistances = 0.0;
	private ArrayList<Robot> robots = new ArrayList<Robot>();
	private double rewardForOrientation=0.0;
	private double rewardForCohesion=0.0;
	private Simulator simulator;
	private double distance=0.0;



	public FlockingEvaluationFunction(Arguments args) {
		super(args);
	}

	// considerar a media - media diferença relativa da orientacaçao
	// cluster
	// distancia

	// @Override
	public double getFitness() {
//		for (Robot r : robots) {
//			sumOfDistances += r.getPosition().distanceTo(robotsPosition.get(r));
//		}
		double numberOfRobots = robots.size();

		if (numberOfRobots == 0)
			numberOfRobots = 1;
		
		//double rewardDistancia=(sumOfDistances / numberOfRobots) / ((simulator.getEnvironment().getSteps()/40/2));

		//double rewardDistancia=(distance / numberOfRobots) / ((simulator.getEnvironment().getSteps()/2));
		double rewardDistancia=(distance / numberOfRobots) / ((simulator.getEnvironment().getSteps()*0.1));

		//System.out.println(rewardDistancia +"distance");

		return fitness+rewardDistancia;
		
	}

	// @Override
	public void update(Simulator simulator) {
		this.simulator=simulator;
		EmptyEnviromentsWithFixPositions environment = (EmptyEnviromentsWithFixPositions) simulator
				.getEnvironment();
		robots = environment.getRobots();
		double numberOfRobots = robots.size();
		double avarageRelativeOrientation=0.0;
		double desvioPadrao=0.0;
		double numberOfRobotJumping=0.0;
		if (simulator.getTime() > 0) {
			ArrayList<Double> robotsOrientation = new ArrayList<Double>();
			double sumRelativeOrientation = 0.0;
			for(Robot r: robots){
				//System.out.println(Math.toDegrees(r.getOrientation()));
				double differenceOfOrientation = calculateDifferenceBetweenAngles(
						Math.toDegrees(r.getOrientation()),
						0);
				robotsOrientation.add(differenceOfOrientation);
				
				sumRelativeOrientation+=differenceOfOrientation;
				
				distance+=r.getPosition().distanceTo(robotsPosition.get(r));
				robotsPosition.put(r, new Vector2d(r.getPosition()));
				//System.out.println("soma"+sumRelativeOrientation);
			}
			
			
			avarageRelativeOrientation = sumRelativeOrientation
						/ (numberOfRobots);

			//System.out.println("media"+avarageRelativeOrientation);
			
			
			for(Double orientation: robotsOrientation){
				desvioPadrao+=Math.pow(orientation-avarageRelativeOrientation, 2);
				//System.out.println(desvioPadrao);
			}
			
			desvioPadrao=Math.sqrt(desvioPadrao/(numberOfRobots-1));
		
			
			
			//System.out.println("desvioPadrao final"+desvioPadrao);
			
			
			
			rewardForOrientation+=1-desvioPadrao/360;
			
			//System.out.println("reward"+rewardForOrientation);
			
			
			//rewardForCohesion+=numberOfRobotJumping/numberOfRobots;
			
//			if(avarageRelativeOrientation>0.5){
//				rewardForOrientation=(1-avarageRelativeOrientation)/0.5;
//			}else{
//				rewardForOrientation=avarageRelativeOrientation/0.5;
//			}
			
		
		
			
//			if(avarageRelativeOrientation>0.5){
//				rewardForOrientation=(1-avarageRelativeOrientation)/0.5;
//			}else{
//				rewardForOrientation=avarageRelativeOrientation/0.5;
//			}
			
			clusters += 1 - (environment.getClusters(robots).size() - 1)
					/ (numberOfRobots - 1);
			
			

			//fitness = current * -0.001 +  rewardForOrientation ;
			//System.out.println(rewardForOrientation/environment.getSteps()  +"reward");
			//System.out.println(clusters/environment.getSteps()  +"cluster");

			fitness = current * -0.001  +  clusters/environment.getSteps()+ rewardForOrientation/environment.getSteps() ;
		} else {

			for (Robot robot : robots) {
				robotsPosition.put(robot, new Vector2d(robot.getPosition()));
			}
		}
	}

	private void robotIsInvolvedInCollison(Robot r) {
		if (r.isInvolvedInCollison()) {
			current++;
		}
	}
	
	private double calculateDifferenceBetweenAngles( double secondAngle, double firstAngle)
	  {
	        double difference = secondAngle - firstAngle;
	        while (difference < -180) difference += 360;
	        while (difference > 180) difference -= 360;
	        return difference;
	 }

	public int getClusters(Simulator simulator) {
		Environment environment = simulator.getEnvironment();
		ArrayList<Robot> robots = environment.getRobots();
		LinkedList<Cluster> clusters = new LinkedList<Cluster>();
		for (int i = 0; i < robots.size(); i++) {
			for (int j = i + 1; j < robots.size(); j++) {
				Robot r1 = robots.get(i);
				Robot r2 = robots.get(j);
				if (r1.getPosition().distanceTo(r2.getPosition())
						- r1.getRadius() - r2.getRadius() <= 2) {
					boolean hasAlreadyCluster = false;
					Vector2d r1Pos = r1.getPosition();
					Vector2d r2Pos = r2.getPosition();
					for (Cluster c : clusters) {
						Vector2d centroid = c.getCentroid();
						if (c.getRobots().contains(r1)
								&& !c.getRobots().contains(r2)) {
							c.addRobot(r2);
							c.setCentroid(new Vector2d((centroid.getX() + r2Pos
									.getX()) / 2, (centroid.getY() + r2Pos
									.getY()) / 2));
							hasAlreadyCluster = true;
							break;
						} else if (c.getRobots().contains(r2)
								&& !c.getRobots().contains(r1)) {
							c.addRobot(r1);
							c.setCentroid(new Vector2d(
									(c.getCentroid().getX() + r1Pos.getX()) / 2,
									(c.getCentroid().getY() + r1Pos.getY()) / 2));
							hasAlreadyCluster = true;
							break;
						} else {
							if (r1.getPosition().distanceTo(c.getCentroid())
									- r1.getRadius() <= 2) {
								c.addRobot(r1);
								c.addRobot(r2);
								c.setCentroid(new Vector2d(
										(centroid.getX() + r2Pos.getX()) / 2,
										(centroid.getY() + r2Pos.getY()) / 2));
								c.setCentroid(new Vector2d(
										(c.getCentroid().getX() + r1Pos.getX()) / 2,
										(c.getCentroid().getY() + r1Pos.getY()) / 2));
								hasAlreadyCluster = true;
								hasAlreadyCluster = true;
								break;
							}
						}

					}

					if (!hasAlreadyCluster) {
						List<Robot> listOfRobots = new LinkedList<Robot>();
						listOfRobots.add(r1);
						listOfRobots.add(r2);
						Vector2d centroid = new Vector2d(
								(r1Pos.getX() + r2Pos.getX()) / 2,
								(r1Pos.getY() + r2Pos.getY()) / 2);
						clusters.add(new Cluster(listOfRobots, centroid));
					}

				}
			}
		}

		for (Robot r : robots) {
			boolean foundRobot = false;
			for (Cluster c : clusters) {
				if (c.getRobots().contains(r)) {
					foundRobot = true;
					break;
				}
			}
			if (foundRobot == false)
				clusters.add(new Cluster(r));
		}

		System.out.println(clusters.size());
		return clusters.size();
	}

	class Cluster {
		private List<Robot> robots = new LinkedList<Robot>();
		private Vector2d centroid = new Vector2d(0, 0);

		public Cluster(List<Robot> robots, Vector2d centroid) {
			this.robots = robots;
			this.centroid = centroid;
		}

		public Cluster(Robot r) {
			robots.add(r);
			this.centroid = new Vector2d(r.getPosition().getX(), r
					.getPosition().getY());
		}

		public Vector2d getCentroid() {
			return centroid;
		}

		public void setCentroid(Vector2d newCentroid) {
			centroid = newCentroid;
		}

		public void addRobot(Robot r) {
			robots.add(r);
		}

		public List<Robot> getRobots() {
			return robots;
		}

		public int sizeOfCluster() {
			return robots.size();
		}
	}

	// Vector2d robotBeforePosition = robotsPosition.get(r);
	// Vector2d robotPosition = r.getPosition();

	// sumOfDistances += r.getPosition().distanceTo(
	// robotBeforePosition);

	// robotsPosition.put(r, new Vector2d(robotPosition));
	//
	// listOfDistances.add(robotBeforePosition
	// .distanceTo(new Vector2d(0, 0)));
	// sair do for
	// Collections.sort(listOfDistances);
	// double distanceDifference = listOfDistances.getLast()
	// - listOfDistances.getFirst();

	// getClusters(simulator);

}