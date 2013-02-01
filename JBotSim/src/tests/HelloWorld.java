package tests;

import gui.WithControlsGui;
import gui.renderer.TwoDRenderer;

import java.io.IOException;
import java.util.LinkedList;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;

public class HelloWorld {

	public static void main(String[] args) throws IOException {
		LinkedList<String> arguments = new LinkedList<String>();
		arguments.add("--robots");
		arguments.add("robotconfigid=1,name=differentialdrive,placement=rectangle,radius=.05,color=red,sensors=(),actuators=()");
		arguments.add("--controllers");
		arguments.add("name=keyboard");
		arguments.add("--environment");
		arguments.add("name=RoundForage,densityoffood=1,nestlimit=0.25,foragelimit=2,forbiddenarea=5");
		
		JBotSim jbot             = new JBotSim(arguments.toArray(new String[0]));
		Simulator sim            = jbot.createSimulator();
		LinkedList<Robot> robots = jbot.createRobots(1);
		System.out.println(robots.size());
		
		sim.getEnvironment().addRobots(robots);
		
		System.out.println(sim.getEnvironment().getRobots().get(0));
		
		TwoDRenderer renderer  = new TwoDRenderer(sim);
		WithControlsGui gui    = new WithControlsGui(sim, renderer);
		
		gui.run(sim, renderer, 10000);
		
	}	
}
