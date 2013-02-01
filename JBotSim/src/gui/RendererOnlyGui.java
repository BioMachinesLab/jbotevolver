package gui;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import javax.swing.JFrame;
import experiments.Experiment;
import gui.renderer.Renderer;
import simulation.Simulator;
import simulation.util.Arguments;

public class RendererOnlyGui implements Gui {
	protected Renderer renderer   = null;
	protected int renderFrequency = 1;
	protected JFrame frame;
	protected Simulator simulator;

	public RendererOnlyGui(Simulator simulator) {
		super();
		this.simulator = simulator;
		frame = new JFrame("RendererOnlyGui");
		frame.setSize(700, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public RendererOnlyGui(Renderer renderer, Arguments arguments) {
		super();
		
		renderFrequency   = (arguments.getArgumentIsDefined("frequency")) ? arguments.getArgumentAsInt("frequency") : 1;
		
		this.renderer 			= renderer;
		frame = new JFrame("RendererOnlyGui");
		frame.getContentPane().add(renderer.getComponent());
		frame.setSize(700, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));
		frame.setVisible(true);
	}

//	@Override
	public void dispose() {
		if (renderer != null) {
			renderer.dispose();
		}
		frame.setVisible(false);
	}


//	@Override
	public void run(Simulator simulator, Renderer rendererTo,
			 int maxNumberOfSteps) {

//		frame.getContentPane().addKeyListener(experiment.getEnvironment());
		int currentStep = 0;
		Renderer usedRenderer;
		if(renderer == null){
			usedRenderer = rendererTo;
			Component rendererComponent = renderer.getComponent();
			if (rendererComponent != null) 
				frame.getContentPane().add(rendererComponent);
		} else {			
			usedRenderer = renderer;
		}

		while (currentStep < maxNumberOfSteps) {						
			if (currentStep % renderFrequency  == 0) {
				usedRenderer.drawFrame();
				if(renderFrequency<10){
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			simulator.performOneSimulationStep(currentStep);
			currentStep++;
		}
//		frame.getContentPane().removeKeyListener(experiment.getEnvironment());

	}
}
