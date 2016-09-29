package gui.extended;

import gui.ResultViewerGui;
import simulation.JBotSim;
import simulation.util.Arguments;

public class ResultViewerExtended extends ResultViewerGui {
	
	public ResultViewerExtended(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}
	/*
	@Override
	public synchronized void update(Simulator simulator) {
		
//		boolean showNN = this.showNeuralNetwork;
//		this.showNeuralNetwork = false;
		super.update(simulator);
//		
//		this.showNeuralNetwork = showNN;
		
		if(showNeuralNetwork && (graphViz == null || simulator.getTime() == 0)) {
			NeuralNetworkController nn = (NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController();
			if(graphViz == null || !(graphViz instanceof GraphVizExtended))
				graphViz = new GraphVizExtended(nn.getNeuralNetwork());
			else
				graphViz.changeNeuralNetwork(nn.getNeuralNetwork());
		}
		
		if(showNeuralNetwork) {
			graphViz.changeNeuralNetwork(((NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController()).getNeuralNetwork());
			graphViz.show();
		}
	}*/
}
