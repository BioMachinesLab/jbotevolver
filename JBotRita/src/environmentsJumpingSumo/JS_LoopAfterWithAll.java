package environmentsJumpingSumo;

import java.awt.Color;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class JS_LoopAfterWithAll extends JS_Environment {
	
	private int fitnesssample = 0;
	private Arguments args;
	private RandomEnvRatios0_5 ratio1_2;
	private RandomEnvRatios1 ratio1;
	private RandomEnvRatio2 ratio2;
	private RandomEnvRatio4 ratio2_5;
	private int envTime;
	 
	
	private JS_EasiestEnv_JustPreys2 ratioExactly0_25; 
	private JS_EnvRatio0_5 ratioExactly0_5;
	private JS_EnvRatio1 ratioExactly1;
	private JS_EnvRatio2 ratioExactly2;
	private JS_EnvRatio4 ratio16;
	private RandomEnvRatio2New ratio2New;
	
	private RandomEnvRatio2New ratios2New;

	public JS_LoopAfterWithAll(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		envTime=fitnesssample;
		fitnesssample = fitnesssample % 17;
		args = arguments;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		envTime=(int)(fitnesssample%4) ;
		if (fitnesssample == 0) {
			ratioExactly0_25 = new JS_EasiestEnv_JustPreys2(simulator, args);
			ratioExactly0_25.setup(this, simulator);
			//System.out.println("Fitnessample "+fitnesssample + " ratio " + 0.25 );
		}
		else if (fitnesssample == 1 || fitnesssample == 2 || fitnesssample ==3) {  //ambiente de 0.25 a 0.5
			ratio1_2 = new RandomEnvRatios0_5(simulator, args);	
			ratio1_2.setup(this, simulator, calculateRatio(1,2));
			//System.out.println("Fitnessample "+fitnesssample + " ratio " + calculateRatio(1,2) );
		}
		else if (fitnesssample == 4 ) { //ambiente de 0.5
			ratioExactly0_5 = new JS_EnvRatio0_5(simulator, args);
			ratioExactly0_5.setup(this, simulator,1.25);
			//System.out.println("Fitnessample "+fitnesssample + " ratio " + 0.5 );
		}else if (fitnesssample == 5 || fitnesssample == 6 || fitnesssample == 7) {  //0.5 a 1
			ratio1 = new RandomEnvRatios1(simulator, args);
			ratio1.setup(this, simulator,calculateRatio(2,4));
			//System.out.println("Fitnessample "+ fitnesssample  + " ratio " + calculateRatio(2,4) );
		}else if (fitnesssample == 8) {  //ratio 1
			ratioExactly1 = new JS_EnvRatio1(simulator, args);
			ratioExactly1.setup(this, simulator);
			//System.out.println("Fitnessample "+ fitnesssample  + " ratio " + 1 );
		}else if (fitnesssample == 9 || fitnesssample == 10 || fitnesssample == 11) {   //1 a 2
//			ratio2 = new RandomEnvRatio2(simulator, args);
//			ratio2.setup(this, simulator, calculateRatio(4,8));
			
			ratios2New = new RandomEnvRatio2New(simulator, args);
			ratios2New.setup(this, simulator, calculateRatio(4,8));
			
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(4,8) );
		} else if (fitnesssample == 12 ) {   //2
//			ratio2 = new RandomEnvRatio2(simulator, args);
//			ratio2.setup(this, simulator, 8);
			
			ratio2New = new RandomEnvRatio2New(simulator, args);
			ratio2New.setup(this, simulator, 8);
			
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + 2 );
		}else if(fitnesssample == 13 || fitnesssample == 14 || fitnesssample == 15){
			ratio2_5 = new RandomEnvRatio4(simulator, args);
			ratio2_5.setup(this, simulator, calculateRatio(8,16));
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + calculateRatio(8,16)+ "as");
		}else if(fitnesssample == 16 ){ //4
			ratio16 = new JS_EnvRatio4(simulator, args);
			ratio16.setup(this, simulator);
			//System.out.println("Fitnessample "+fitnesssample  + " ratio " + 4);
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
