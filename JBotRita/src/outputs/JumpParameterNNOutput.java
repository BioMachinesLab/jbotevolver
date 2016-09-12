package outputs;


import actuator.JumpActuator;
import actuator.JumpParameterActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class JumpParameterNNOutput extends NNOutput {
	private JumpParameterActuator actuactor;
	private double angle;
	private boolean isToJump;
	private double power;
	
	public JumpParameterNNOutput(Actuator actuactor, Arguments args){
		super(actuactor, args);
		this.actuactor=( JumpParameterActuator) actuactor;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		// TODO Auto-generated method stub
		return 3;
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
				if(index==1)
					angle=value;
				else
					power=value;
			}
		}
		
	}

	@Override
	public void apply() {
		if(isToJump){
			actuactor.setAngle(angle);
			actuactor.setPower(power);
			actuactor.jumpStatus();
		}
		else
			actuactor.offStatus();
	}

}