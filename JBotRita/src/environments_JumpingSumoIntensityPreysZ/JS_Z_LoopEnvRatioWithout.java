package environments_JumpingSumoIntensityPreysZ;

import java.awt.Color;

import environments_JumpingSumoIntensityPreys.JS_EasiestEnv_JustPreys;
import environments_JumpingSumoIntensityPreys.JS_Environment;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;


public class JS_Z_LoopEnvRatioWithout extends JS_Environment {
	
	private int fitnesssample = 0;
	private Arguments args;
	private JS_EasiestEnv_JustPreys ratio1; 
	private JS_Z_EnvRatio_1_5 ratio1_5;
	private JS_Z_EnvRatio2 ratio2;
	private JS_Z_EnvRatio2_5 ratio2_5;
	private JS_Z_EnvRatio3 ratio3;
	private JS_Z_EnvWithoutRatio withoutRatio;

	public JS_Z_LoopEnvRatioWithout(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		fitnesssample = fitnesssample % 5;
		args = arguments;
		
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		if (fitnesssample == 0) {
			ratio1 = new JS_EasiestEnv_JustPreys(simulator, args);
			ratio1.setup(this, simulator);
		} else if (fitnesssample == 1) {
			ratio1_5 = new JS_Z_EnvRatio_1_5(simulator, args);
			ratio1_5.setup(this, simulator,1.25);
		} else if (fitnesssample == 2) {
			ratio2 = new JS_Z_EnvRatio2(simulator, args);
			ratio2.setup(this, simulator,3);
		} else if (fitnesssample == 3) {
			ratio2_5 = new JS_Z_EnvRatio2_5(simulator, args);
			ratio2_5.setup(this, simulator);
		} else if(fitnesssample==4){
			ratio3 = new JS_Z_EnvRatio3(simulator, args);
			ratio3.setup(this, simulator, 6,8 );
		} else {
			withoutRatio = new JS_Z_EnvWithoutRatio(simulator, args);
			withoutRatio.setup(this, simulator);
		}
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				numberOfFoodSuccessfullyForaged=1;
				simulator.stopSimulation();
				preyEated = prey;
				change_PreyInitialDistance = true;
			}
			 if (prey.getIntensity() <=0)
					prey.setColor(Color.WHITE);	
				else if (prey.getIntensity() < 9)
					prey.setColor(Color.BLACK);
				else if (prey.getIntensity() < 13)
					prey.setColor(Color.GREEN.darker());
				else
					prey.setColor(Color.RED);
		}
	}
}
