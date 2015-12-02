package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mathutils.MathUtils;
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
	
	protected JBotEvolver jbot;
	protected String folder;
	protected Renderer renderer;
	protected JFrame frame;
	protected JLabel genLabel;
	protected Simulator sim;
	
	public NoveltyViewer(String folder) throws Exception {
		this.folder = folder;
		
		String file = folder+"/_showbest_current.conf";
		
		jbot = new JBotEvolver(new String[]{file});
		Population pop = jbot.getPopulation();
		int gens = pop.getNumberOfCurrentGeneration();
		
		renderer = new TwoDRenderer(new Arguments(""));
		frame = createJFrame(gens);
		setGeneration(gens);
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
				
				Marker m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.01, 0.1, Color.RED);
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
	
	public JFrame createJFrame(int gens) {
		JFrame frame = new JFrame(folder);
		frame.setLayout(new BorderLayout());
		frame.add(renderer,BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		JSlider slider = new JSlider(JSlider.HORIZONTAL,0,gens,0);
		
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
		
		bottomPanel.add(slider);
		bottomPanel.add(genLabel);
		bottomPanel.add(plus);
		bottomPanel.add(minus);
		
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
	
	public static void main(String[] args) throws Exception {
		
//		String folder = "nsga2_nf";
//		String folder = "nsga2_novelty";
//		String folder = "../../EvolutionAutomator/br/ninety/1/";
		
		
		String[] folder = new String[]{
				"../../EvolutionAutomator/br/two/1/",
//				"../../EvolutionAutomator/br/four/1/",
//				"../../EvolutionAutomator/br/six/1/",
//				"../../EvolutionAutomator/br/eight/1/",
				};
		
		for(String s : folder)
			new NoveltyViewer(s);
		
	}

}
