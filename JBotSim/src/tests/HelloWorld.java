package tests;

import gui.WithControlsGui;
import gui.renderer.TwoDRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;

public class HelloWorld {

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		LinkedList<String> arguments = new LinkedList<String>();
		arguments.add("--robots");
		arguments.add("robotconfigid=1,name=Epuck,radius=.05,color=red,sensors=(EpuckIRSensor=(id=1)),actuators=(TwoWheelActuator=(id=1))");
		arguments.add("--controllers");
//		arguments.add("name=KeyboardController");
		arguments.add("name=NeuralNetworkController,network=CTRNNMultilayer,robotconfigid=1,hiddennodes=3,inputs=(EpuckIRNNInput=(id=1)),outputs=(FixedNNOutput=(TwoWheelNNOutput=(id=1),value=0.7)),weights=(-6.616121370699313,-9.043912833071044E-4,1.5032863630309485,0.6244008271845928,4.891734832662374,0.38099440074751756,0.03126109111289052,-4.780775810320788,-7.94931421274473,2.835676118247296,-2.8562128983955146,-3.7450416890205585,-0.8139674430206081,-3.979845801668362,-0.5409698813119663,2.4997336728160686,-0.3499494297393089,-1.8735329368823381,-4.600451941030473,-4.03394978357712,-4.818976440279687,0.9842590677429728,-1.3728101580251448,1.989384913226786,-1.7099487975490493,-5.494783982310734,-3.842411894148708,1.9135089776239904,-2.354745362388502,0.16600930796098834,-1.3136536900533253,7.130745893155513,0.39295040022649746,2.6607165658867125,1.0034047058755338)");
		arguments.add("--environment");
		arguments.add("name=RoundForageEnvironment,densityofpreys=0,nestlimit=0.25,foragelimit=2,forbiddenarea=5");
		
		JBotSim jbot             = new JBotSim(arguments.toArray(new String[0]));
		Simulator sim            = jbot.createSimulator();
		Environment env 		 = jbot.getEnvironment(sim);
		ArrayList<Robot> robots  = jbot.createRobots(sim);
		env.addRobots(robots);
		System.out.println(robots.size());
		System.out.println(sim.getEnvironment().getRobots().get(0));
		
		TwoDRenderer renderer  = new TwoDRenderer(sim);
		WithControlsGui gui    = new WithControlsGui(sim, renderer);
		
		gui.run(sim, renderer, 1000);
		
	}	
}
