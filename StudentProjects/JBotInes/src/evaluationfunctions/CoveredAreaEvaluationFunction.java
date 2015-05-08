package evaluationfunctions;

import java.util.ArrayList;

import sensors.NearTypeBRobotSensor;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import environment.OpenEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CoveredAreaEvaluationFunction extends EvaluationFunction {

	private static final String DESC_A = "type0";
	private static final String DESC_B = "type1";

	private ArrayList<Robot> typeA = new ArrayList<Robot>();
	private ArrayList<Robot> typeB = new ArrayList<Robot>();

	private static final double rangeBToB = 2.0;
	private static final double rangeA = 0.5;

	public CoveredAreaEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {

		OpenEnvironment env = (OpenEnvironment) simulator.getEnvironment();
		typeA = env.getTypeARobots();
		typeB = env.getTypeBRobots();

		double fitB = 0.0;
		double fitA = 0.0;


		double xmax = - Double.MIN_VALUE;
		double xmin = Double.MAX_VALUE;
		double ymax = - Double.MIN_VALUE;
		double ymin = Double.MAX_VALUE;

		double areaCovered = 0.0;
		//boolean verticesDefined = false;
		// double fit = 0.0;

		//System.out.println("Number of robots: " + simulator.getEnvironment().getRobots().size());

		for (Robot r : simulator.getEnvironment().getRobots()) {

			//System.out.println("Robot " + r.getId() + ", position " + r.getPosition());
			boolean robotBDetectedByA = false;

			for (Robot b : typeB) {
				if (!b.equals(r)) {

					// if type B -> fit++ if another b is in range
					if (r.getDescription().equals(DESC_B) && r.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0) == 1) {

						NearTypeBRobotSensor s = (NearTypeBRobotSensor) r
								.getSensorByType(NearTypeBRobotSensor.class);
						double distance = r.getPosition().distanceTo(
								b.getPosition());
						if (distance <= (s.getRange()) - b.getDiameter())
							fitB += distance / (s.getRange() - b.getDiameter());
					}
				}

				// if type A -> fit++ if one b is in range; fit++ if the area covered is the widest (having b in range)
				if (r.getDescription().equals(DESC_A) && r.getSensorByType(NearTypeBRobotSensor.class).getSensorReading(0) == 1) {

					if (!robotBDetectedByA) {
						NearTypeBRobotSensor s = (NearTypeBRobotSensor) r.getSensorByType(NearTypeBRobotSensor.class);
						double distance = r.getPosition().distanceTo(
								b.getPosition());
						if (distance <= (s.getRange()) - b.getDiameter())
							fitA += distance / (s.getRange() - b.getDiameter());
						robotBDetectedByA = true;
					}

					if (r.getPosition().x < xmin)
						xmin = r.getPosition().x;

					if (r.getPosition().y < ymin)
						ymin = r.getPosition().y;

					if (r.getPosition().x > xmax)
						xmax = r.getPosition().x;

					if (r.getPosition().y > ymax)
						ymax = r.getPosition().y;

				}
			}			
		} 




		// calc area covered
		//		if(!samePositions(down, left) && !samePositions(down, right) && !samePositions(right, left))
		//			areaCovered = down.distanceTo(left) * down.distanceTo(right);
		//		else {
		//			if(!samePositions(down, left) && !samePositions(down, up) && !samePositions(up, left))
		//				areaCovered = left.distanceTo(down) * left.distanceTo(up);
		//			else {
		//				if(!samePositions(up, left) && !samePositions(up, right) && !samePositions(right, left))
		//					areaCovered = up.distanceTo(left) * up.distanceTo(right);
		//				else {
		//					if(!samePositions(down, up) && !samePositions(down, right) && !samePositions(right, up))
		//						areaCovered = right.distanceTo(down) * right.distanceTo(up);
		//				}
		//			}
		//		}

//		if(down != null && left != null && right != null)
//			areaCovered = down.distanceTo(left) * down.distanceTo(right);

		
		areaCovered = Math.abs(xmax - xmin) * Math.abs(ymax - ymin);
		fitness += areaCovered * 0.01 + fitA * 0.001 + fitB * 0.003;

		//System.out.println("Fitness = " + areaCovered + " + " + fitA + " + " + fitB );
		//System.out.println("Up: " + up + " Right: "+ right + " Down: " + down + "Left: " + left);

		// fitness += fitA * 0.001 + fitB * 0.001;
		// fitness = fitB * 0.5 + fitA * 0.5;

	}

	//	private boolean samePositions(Vector2d v1, Vector2d v2){
	//		return (v1.x == v2.x && v1.y == v2.y);	
	//	}

}

//tlsrs@iscte.pt
