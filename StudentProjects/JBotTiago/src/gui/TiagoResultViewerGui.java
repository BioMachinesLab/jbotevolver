package gui;

import simulation.JBotSim;
import simulation.util.Arguments;

public class TiagoResultViewerGui extends ResultViewerGui {

	public TiagoResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
		
		extraArguments.setText("--gui +classname=TiagoResultViewerGui,renderer=(classname=TwoDRendererDebug,conesensorid=1,drawIds=1,boardSensors=0,paperSensors=0)");
	}

}
