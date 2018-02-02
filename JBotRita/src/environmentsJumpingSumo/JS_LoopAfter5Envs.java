package environmentsJumpingSumo;

import java.awt.Color;


import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class JS_LoopAfter5Envs extends JS_Environment {
	
	private int fitnesssample = 0;
	private Arguments args;
	private RandomEnvRatios0_5 ratio1_2;
	private RandomEnvRatios1 ratio1;
	private RandomEnvRatio2 ratio2;
	private RandomEnvRatio4 ratio2_5;
	private int envTime;
	 
	public JS_LoopAfter5Envs(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		envTime=fitnesssample;
		fitnesssample = fitnesssample % 12;
		args = arguments;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		envTime=(int)(fitnesssample/4) + 1;
		if (fitnesssample == 0 || fitnesssample == 4 || fitnesssample ==8) {
			ratio1_2 = new RandomEnvRatios0_5(simulator, args);	
			ratio1_2.setup(this, simulator, calculateRatio(1,2));
			//System.out.println("Fitnessample "+fitnesssample + " ratio " + calculateRatio(1,2) );
		}
		else if (fitnesssample == 1 || fitnesssample == 5 || fitnesssample == 9) {
			ratio1 = new RandomEnvRatios1(simulator, args);
			ratio1.setup(this, simulator,calculateRatio(2,4));
			//System.out.println("Fitnessample "+ fitnesssample  + " ratio " + calculateRatio(2,4) );
		}
		else if (fitnesssample == 2 || fitnesssample == 6 || fitnesssample == 10) {
			ratio2 = new RandomEnvRatio2(simulator, args);
			ratio2.setup(this, simulator, calculateRatio(4,8));
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(4,8) );
		} else if(fitnesssample == 3 || fitnesssample == 7 || fitnesssample == 11){
			ratio2_5 = new RandomEnvRatio4(simulator, args);
			ratio2_5.setup(this, simulator, calculateRatio(8,16));
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(8,16)+ "as");
		}
	}

	private double calculateRatio(double startRange, double endRange){
		return startRange + ((endRange-startRange)/4)*(envTime);
	}
	
	
//	public JS_LoopAfter5Envs(Simulator simulator, Arguments arguments) {
//		super(simulator, arguments);
//		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
//		envTime=fitnesssample;
//		fitnesssample = fitnesssample % 8;
//		args = arguments;
//	}
//
//	@Override
//	public void setup(Simulator simulator) {
//		super.setup(simulator);
//		envTime=(int)(fitnesssample/3) + 1;
//		if (fitnesssample == 0 || fitnesssample == 4 ) {
//			ratio1_2 = new RandomEnvRatios0_5(simulator, args);	
//			ratio1_2.setup(this, simulator, calculateRatio(1,2));
//			//System.out.println("Fitnessample "+fitnesssample + " ratio " + calculateRatio(1,2) );
//		}
//		else if (fitnesssample == 1 || fitnesssample == 5 ) {
//			ratio1 = new RandomEnvRatios1(simulator, args);
//			ratio1.setup(this, simulator,calculateRatio(2,4));
//			//System.out.println("Fitnessample "+ fitnesssample  + " ratio " + calculateRatio(2,4) );
//		}
//		else if (fitnesssample == 2 || fitnesssample == 6 ) {
//			ratio2 = new RandomEnvRatio2(simulator, args);
//			if(fitnesssample==6)
//				envTime=(int)(fitnesssample/3) ;
//			ratio2.setup(this, simulator, calculateRatio(4,8));
//			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(4,8) );
//		} else if(fitnesssample == 3 || fitnesssample == 7 ){
//			ratio2_5 = new RandomEnvRatio4(simulator, args);
//			ratio2_5.setup(this, simulator, calculateRatio(8,16));
//			envTime=envTime-1 ;
//		//	System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(8,16)+ "as");
//		}
//	}
//
//	private double calculateRatio(double startRange, double endRange){
//		return startRange + ((endRange-startRange)/3)*(envTime);
//	}
//	
//	
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
