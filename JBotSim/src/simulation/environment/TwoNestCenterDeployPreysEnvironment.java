package simulation.environment;

import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;

public class TwoNestCenterDeployPreysEnvironment extends
		TwoNestSimpleForageEnvironment implements NestEnvironment {

	private int numberOfPreysAvailable;
	private double rateOfNewPreyPerCycle;

	private LinkedList<Prey> foragedPreys = new LinkedList<Prey>();

	public TwoNestCenterDeployPreysEnvironment(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);

		numberOfPreysAvailable = arguments
				.getArgumentIsDefined("numberOfPreysAvailable") ? arguments
				.getArgumentAsInt("numberOfPreysAvailable") : 3;

		rateOfNewPreyPerCycle = arguments
				.getArgumentIsDefined("rateOfNewPreyPerCycle") ? arguments
				.getArgumentAsDouble("rateOfNewPreyPerCycle") : .1;

		deployPreys();
	}

	// private Vector2d newRandomPosition() {
	// Vector2d position;
	// do {
	// double radius = simulator.getRandom().nextDouble() * (forageLimit);
	// double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
	// position = new Vector2d(radius * Math.cos(angle), radius
	// * Math.sin(angle));
	// } while (position.distanceTo(nestA.getPosition()) < nestA.getRadius()
	// || position.distanceTo(nestB.getPosition()) < nestB.getRadius());
	// return position;
	// }

	protected Vector2d newRandomPosition() {
		return new Vector2d(0, simulator.getRandom().nextDouble()
				* 2 * forageLimit - forageLimit);
	}

	protected void reDeployPreys() {
		if (simulator.getRandom().nextFloat() < rateOfNewPreyPerCycle
				&& foragedPreys.size() > 0) {
			Prey newPrey = foragedPreys.poll();
			newPrey.setEnabled(true);
			newPrey.teleportTo(newRandomPosition());
		}

	}

	@Override
	protected void releasePrey(Prey prey) {
		super.releasePrey(prey);
		foragedPreys.add(prey);
	}

	@Override
	public void update(int time) {
		super.update(time);
		reDeployPreys();
	}

	@Override
	protected int getAmoutOfFood() {
		return numberOfPreysAvailable;
	}

}
