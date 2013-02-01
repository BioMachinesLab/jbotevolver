package simulation.environment;

import experiments.FoodWaterExperiment;
import gui.renderer.Renderer;

import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FoodWaterEnvironment extends Environment {

	private static final Double MAX = new Double(1);
	private Vector2d foodPosition;
	private double foodRadius;
	private Vector2d waterPosition;
	private double waterRadius;
	private double minDistance, maxDistance;
	private Double resourceConsumption;
	private int liveSpan;
	private int reproductionTime;

	private LinkedList<Robot> parents = new LinkedList<Robot>();

	public FoodWaterEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments.getArgumentIsDefined("width") ? arguments
				.getArgumentAsDouble("width") : 5.0, arguments
				.getArgumentIsDefined("height") ? arguments
				.getArgumentAsDouble("height") : 5.0);
		minDistance = arguments.getArgumentIsDefined("mindistance") ? arguments
				.getArgumentAsDouble("mindistance") : 2.0;

		maxDistance = arguments.getArgumentIsDefined("maxdistance") ? arguments
				.getArgumentAsDouble("maxdistance") : 5.0;

		foodRadius = arguments.getArgumentIsDefined("foodradius") ? arguments
				.getArgumentAsDouble("foodradius") : 0.2;

		waterRadius = arguments.getArgumentIsDefined("waterradius") ? arguments
				.getArgumentAsDouble("waterradius") : 0.2;

		resourceConsumption = arguments
				.getArgumentIsDefined("resourceconsumption") ? arguments
				.getArgumentAsDouble("resourceconsumption") : 0.01;

		liveSpan = arguments.getArgumentIsDefined("livespan") ? arguments
				.getArgumentAsInt("livespan") : 1500;

		reproductionTime = arguments.getArgumentIsDefined("reproductiontime") ? arguments
				.getArgumentAsInt("reproductiontime") : 1000;

		placeResources();

	}

	private void placeResources() {
		// Food
		double angle = newRandomAngle();
		double center = calculateCenter();
		foodPosition = new Vector2d(center * Math.cos(angle), center
				* Math.sin(angle));

		// Water
		angle = newRandomAngle() / 2 + Math.PI / 2 + angle;
		center = calculateCenter();
		waterPosition = new Vector2d(center * Math.cos(angle), center
				* Math.sin(angle));
	}

	private double newRandomAngle() {
		double r = simulator.getRandom().nextDouble() * 2 * Math.PI;
		return r;
	}

	private double calculateCenter() {
		double distanceGap = maxDistance - minDistance;
		double randomInGap = simulator.getRandom().nextDouble() * distanceGap;
		double preyCenter = minDistance + randomInGap;

		return preyCenter;
	}

	@Override
	public void draw(Renderer renderer) {
		renderer.drawCircle(foodPosition, foodRadius);
		renderer.drawCircle(waterPosition, waterRadius);
	}

	@Override
	public void update(double time) {
		boolean anyAlive = false;
		for (Robot robot : robots) {
			if (robot.getParameterAsInteger(FoodWaterExperiment.BORN_TIME)
					+ reproductionTime == simulator.getTime()) {
				parents.add(robot);
			}
			if (robot.isEnabled()
					&& (robot.getParameterAsDouble(FoodWaterExperiment.FOOD) <= 0 || robot
							.getParameterAsDouble(FoodWaterExperiment.WATER) <= 0)
					|| robot.getParameterAsInteger(FoodWaterExperiment.BORN_TIME)
							+ liveSpan == simulator.getTime()) {
				robot.setEnabled(false);
			} else {
				anyAlive = true;
				if (robot.getParameterAsDouble(FoodWaterExperiment.FOOD) < robot

				.getParameterAsDouble(FoodWaterExperiment.WATER)) {
					if (robot.getPosition().distanceTo(foodPosition) < foodRadius) {
						recharge(robot, FoodWaterExperiment.FOOD);
					}
				} else if (robot.getParameterAsDouble(FoodWaterExperiment.FOOD) > robot
						.getParameterAsDouble(FoodWaterExperiment.WATER)) {
					if (robot.getPosition().distanceTo(waterPosition) < waterRadius) {
						recharge(robot, FoodWaterExperiment.WATER);
					}
				} else {
					if (robot.getPosition().distanceTo(foodPosition) < foodRadius) {
						recharge(robot, FoodWaterExperiment.FOOD);
					} else if (robot.getPosition().distanceTo(waterPosition) < waterRadius) {
						recharge(robot, FoodWaterExperiment.WATER);
					}
				}
				spend(robot);
			}
		}
		if (!anyAlive) {
			simulator.getExperiment().endExperiment();
		}
		for(Robot robot:parents){
			createNewRobot(robot);
		}
		parents.clear();
	}

	private void createNewRobot(Robot parent) {
		simulator.getExperiment().createOneRobot();

		Robot robot = robots.get(robots.size() - 1);
		robot.setPosition(parent.getPosition());
		robot.setParameter(FoodWaterExperiment.FOOD, new Double(1));
		robot.setParameter(FoodWaterExperiment.WATER, new Double(1));
		robot.setParameter(FoodWaterExperiment.RECHARGED, new Integer(0));
		robot.setParameter(FoodWaterExperiment.BORN_TIME, new Double(simulator.getTime()));
	}

	private void spend(Robot robot) {
		robot.setParameter(FoodWaterExperiment.FOOD,
				robot.getParameterAsDouble(FoodWaterExperiment.FOOD)
						- resourceConsumption);
		robot.setParameter(FoodWaterExperiment.WATER,
				robot.getParameterAsDouble(FoodWaterExperiment.WATER)
						- resourceConsumption);

	}

	private void recharge(Robot robot, String resource) {
		robot.setParameter(resource, MAX);
		robot.setParameter(FoodWaterExperiment.RECHARGED,
				robot.getParameterAsInteger(FoodWaterExperiment.RECHARGED) + 1);
	}
}
