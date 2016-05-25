package outputs;


import actuator.JumpSumoActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpSumoNNOutput extends NNOutput {
	private JumpSumoActuator actuactor;
	private double angle;
	private boolean isToJump=false;
	private double power;
	private static final double HIGHJUMP_ANGLE=60;
	private static final double HIGHJUMP_POWER=4.5;  //distance 0.7
	private static final double LONGJUMP_ANGLE=30;
	private static final double LONGJUMP_POWER=5.5; //distance 1
	
	
	public JumpSumoNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=( JumpSumoActuator) actuactor;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public void setValue(int index, double value) {
		// TODO Auto-generated method stub
		
		if(index==0){
			if (value > 0.5) {
				isToJump=true;
				//actuactor.jumpStatus();
			}
			else{
				isToJump=false;
	//			System.out.println("é para o status tar off");
				//actuactor.offStatus();
			}
		}else {
			if(isToJump){
				if(value>0.5){
					angle=LONGJUMP_ANGLE;
					power=LONGJUMP_POWER;
				}
				else{
					angle=HIGHJUMP_ANGLE;
					power=HIGHJUMP_POWER;
				}
			}
		}
		
	}

	@Override
	public void apply() {
		if(isToJump){
			actuactor.jump(angle,power);
		}
		
	}

}