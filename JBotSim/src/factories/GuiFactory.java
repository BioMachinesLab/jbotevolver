package factories;

import java.io.Serializable;

import gui.Gui;
import gui.RendererOnlyGui;
import gui.WithControlsGui;
import gui.renderer.BlenderRenderer;
import gui.renderer.NullRenderer;
import gui.renderer.Renderer;
import gui.renderer.TraceRenderer;
import gui.renderer.TwoDRenderer;
import gui.renderer.TwoDRendererDebug;
import simulation.Simulator;
import simulation.util.Arguments;

public class GuiFactory extends Factory implements Serializable {

	public static Gui getGui(Simulator simulator, Arguments arguments) throws Exception {
		if (arguments == null) {
			return new WithControlsGui(simulator, new TwoDRenderer(simulator)); 
		}

		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("--gui name=... not defined");
		}

		String guiName = arguments.getArgumentAsString("name");


		String rendererName = (arguments.getArgumentIsDefined("renderer")) ? arguments.getArgumentAsString("renderer") : "";
		Renderer renderer;

		if(rendererName.equalsIgnoreCase("twodrenderer")){
			renderer = new TwoDRenderer(simulator); 
		} else	if(rendererName.equalsIgnoreCase("twodrendererdebug") || guiName.equalsIgnoreCase("debug")){
			renderer = new TwoDRendererDebug(simulator); 
		} else	if(rendererName.equalsIgnoreCase("tracerenderer")){
			renderer = new TraceRenderer();
			((TraceRenderer) renderer).setSimulator(simulator);
		} else	if(rendererName.equalsIgnoreCase("blenderrenderer")){
			renderer = new BlenderRenderer(simulator);			
		} else if(rendererName.equalsIgnoreCase("null") || rendererName.equalsIgnoreCase("none")) {
			renderer  = new NullRenderer();
		} else {
			renderer = new TwoDRenderer(simulator); 
		}

		if (guiName.equalsIgnoreCase("renderonly") || guiName.equalsIgnoreCase("rendereronly")) {
			return new RendererOnlyGui(renderer, arguments);
		} else if (guiName.equalsIgnoreCase("withcontrols") || guiName.equalsIgnoreCase("full")) {
			return new WithControlsGui(simulator, renderer);

		} else if (guiName.equalsIgnoreCase("debug")) {
			return new WithControlsGui(simulator, new TwoDRendererDebug(simulator)); 
		}
		throw new RuntimeException("Gui with name '" + guiName + "' not found");
	}
}
