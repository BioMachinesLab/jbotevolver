package environments_ForagingIntensityPreys;

import java.awt.Color;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;

public class LoopEnv_2 extends ForagingIntensityPreysEnvironment {
	private boolean firstPositionOfPreyWasAdded = false;
	private int fitnesssample = 0;
	private Arguments args;
	// Environments used in this loop
	private EasiestEnv_JustPreys easiestEnv;
	private EasyEnv_WallsSpreadAround easyEnv;
	private MeddiumEnv_WallsCross meddiumEnv;
	private DifficultEnv_WallsMazze difficultEnv;
	private MostDifficultEnv_SquareAtSouth mostDifficultEnv;
	public LoopEnv_2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		fitnesssample = fitnesssample % 5;
		args = arguments;

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		if (fitnesssample == 0) {
			easiestEnv = new EasiestEnv_JustPreys(simulator, args);
			easiestEnv.setup(this, simulator);
		} else if (fitnesssample == 1) {
			easyEnv = new EasyEnv_WallsSpreadAround(simulator, args);
			easyEnv.setup(this, simulator);
		} else if (fitnesssample == 2) {
			meddiumEnv = new MeddiumEnv_WallsCross(simulator, args);
			meddiumEnv.setup(this, simulator);
		} else if(fitnesssample == 3) {
			difficultEnv = new DifficultEnv_WallsMazze(simulator, args);
			difficultEnv.setup(this, simulator);
		}
		else{
			mostDifficultEnv= new MostDifficultEnv_SquareAtSouth(simulator,args);
			mostDifficultEnv.setup(this, simulator);
		}
	}

	public void addPreys(Vector2d newRandomPositionDifficultEnvironment) {
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPositionDifficultEnvironment, 0, PREY_MASS,
					PREY_RADIUS, randomIntensity()));
		}
	}

	protected Vector2d newRandomPosition() {
		if (fitnesssample == 0) {// simplestEnvironment
			return easiestEnv.newRandomPosition();
		} else if (fitnesssample == 1) { // easyEnvironment
			return easyEnv.newRandomPosition();
		} else if (fitnesssample == 2) {// MeddiumEnvironment
			return meddiumEnv.newRandomPosition();
		} else if(fitnesssample ==3){ // difficultEnvironment
			return difficultEnv.newRandomPosition();
		}
		else
			return mostDifficultEnv.newRandomPosition();
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				prey.setIntensity(randomIntensity());
				prey.teleportTo(newRandomPosition());
				numberOfFoodSuccessfullyForaged++;
				preyEated = prey;
				change_PreyInitialDistance = true;
			}

			if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}
	}

}
