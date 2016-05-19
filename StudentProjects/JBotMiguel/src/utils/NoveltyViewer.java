package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.postevaluators.NoveltyEvaluation;
import novelty.postevaluators.NoveltyEvaluation.ArchiveEntry;
import novelty.results.VectorBehaviourExtraResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.physicalobjects.Marker;
import simulation.util.Arguments;
import evaluationfunctions.OrientationEvaluationFunction;
import evolution.PostEvaluator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.populations.Population;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

public class NoveltyViewer {
	
	protected String file;
	protected JBotEvolver jbot;
	protected String folder;
	protected Renderer renderer;
	protected JFrame frame;
	protected JLabel genLabel;
	protected Simulator sim;
	protected int currentGens;
	
	public static void main(String[] args) throws Exception {
		
		String[] folder = new String[]{
//				"../../EvolutionAutomator/br/two/1/",
				"../../EvolutionAutomator/br/four/1/",
				"../../EvolutionAutomator/br/six/1/",
//				"../../EvolutionAutomator/br/eight/1/",
				};
		
		for(String s : folder)
			new NoveltyViewer(s);
	}
	
	public NoveltyViewer(String folder) {
		this.folder = folder;
		
		file = folder+"/_showbest_current.conf";
		
		load();
		
		renderer = new TwoDRenderer(new Arguments(""));
		frame = createJFrame();
		setGeneration(currentGens);
	}
	
	public void load() {
		
		int gens = 0;
		
		try {
			jbot = new JBotEvolver(new String[]{file});
			Population pop = jbot.getPopulation();
			gens = pop.getNumberOfCurrentGeneration();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		currentGens = gens;
	}
	
	public void setGeneration(int i) {
		try {
			jbot.loadFile(folder+"/show_best/showbest"+i+".conf", "");
			Population pop = jbot.getPopulation();
		
			sim = jbot.createSimulator();
			jbot.setupBestIndividual(sim);
			sim.setupEnvironment();
			
			NoveltyEvaluation post = (NoveltyEvaluation)getPostEvaluator(pop, NoveltyEvaluation.class);
			
			placeMarkers(post);
			
//			testDirections();
			
			genLabel.setText("Generation: "+i);
			
			refresh();
			
			Thread.sleep(10);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void refresh() {
		renderer.setSimulator(sim);
		renderer.drawFrame();
	}
	
	public void placeMarkers(NoveltyEvaluation post) {
		for(ArchiveEntry entry : post.getArchive()) {
			MOChromosome res = entry.getChromosome();
			
			ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
			BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
			
			if(br instanceof VectorBehaviourResult) {
				double[] behavior = (double[])br.value();
				Vector2d pos = new Vector2d(behavior[0],behavior[1]);
				Marker m = new Marker(sim, "m", pos.x, pos.y, Math.PI, 0.01, 0, Color.RED);
				sim.getEnvironment().addStaticObject(m);
			}
			
			if(br instanceof VectorBehaviourExtraResult) {
				double[] behavior = (double[])br.value();
				Vector2d pos = new Vector2d(behavior[0],behavior[1]);
				
				double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
				
				Marker m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.RED);
				sim.getEnvironment().addStaticObject(m);
			}
		}
	}
	
	public PostEvaluator getPostEvaluator(Population pop, Class c) {
		LinkedList<Serializable> objs = pop.getSerializableObjects();
		for(Serializable s : objs) {
			if(c.isAssignableFrom(s.getClass()))
				return (PostEvaluator)s;
		}
		return null;
	}
	
	public JFrame createJFrame() {
		JFrame frame = new JFrame(folder);
		frame.setLayout(new BorderLayout());
		frame.add(renderer,BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		final JSlider slider = new JSlider(JSlider.HORIZONTAL,0,currentGens,0);
		
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider)e.getSource();
				int gen = slider.getValue();
				setGeneration(gen);
			}
		});
		
		genLabel = new JLabel();
		
		JButton plus = new JButton("+");
		JButton minus = new JButton("-");
		
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0 ; i < 3 ; i++)
					renderer.zoomIn();
				refresh();
			}
		});
		
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int i = 0 ; i < 3 ; i++)
				renderer.zoomOut();
				refresh();
			}
		});
		
		for(int i = 0 ; i < 5 ; i++)
			plus.doClick();
		
		final RefreshThread refreshThread = new RefreshThread(slider);
		refreshThread.start();
		
		JCheckBox autoRefresh = new JCheckBox("Refresh");
		autoRefresh.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED) {
		        	refreshThread.unpause();
		        } else {
		        	refreshThread.pause();
		        };
		    }
		});
		
		bottomPanel.add(slider);
		bottomPanel.add(genLabel);
		bottomPanel.add(plus);
		bottomPanel.add(minus);
		bottomPanel.add(autoRefresh);
		
		frame.add(bottomPanel,BorderLayout.SOUTH);
		
		frame.setSize(700, 700);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		return frame;
	}
	
	private void testDirections() {
		
		double max = 2;
		double inc = 0.05;
		
		for(double i = -max ; i < max ; i+=inc) {
			for(double j = -max ; j < max ; j+=inc) {
				Vector2d pos = new Vector2d(i,j);
				
				double orientation = OrientationEvaluationFunction.getOrientationFromCircle(pos);
				
				Marker m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.01, 0.05, Color.RED);
				sim.getEnvironment().addStaticObject(m);
			}
		}
	}
	
	public class RefreshThread extends Thread{
		
		private JSlider slider;
		private long waitTime = 100;
		private long minWaitTime = 100;
		private long maxWaitTime = 10000;
		private long timeIncrement = 100;
		private boolean active = false;
		
		public RefreshThread(JSlider slider) {
			this.slider = slider;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					if(active) {
						
						int gens = currentGens;
						
						load();
						
						if(currentGens > gens) {
							slider.setMaximum(currentGens);
							slider.setValue(currentGens);
							setGeneration(currentGens);
							waitTime = minWaitTime;
						} else {
							waitTime+=timeIncrement;
						}
						
						if(waitTime > maxWaitTime) waitTime = maxWaitTime;
					
						Thread.sleep(waitTime);
						
					} else {
						synchronized (this) {
							this.wait();
						}
					}
				} catch(InterruptedException e) {}
			}
		}
		
		public void pause() {
			active = false;
		}
		
		public void unpause() {
			if(!active) {
				active = true;
				this.interrupt();
			}
		}
	}
}