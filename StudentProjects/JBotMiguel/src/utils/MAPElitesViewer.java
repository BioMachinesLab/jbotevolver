package utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Marker;
import simulation.util.Arguments;
import evaluationfunctions.OrientationEvaluationFunction;
import evolution.MAPElitesPopulation;
import evolution.PostEvaluator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import gui.extended.TwoDRendererWheels;
import gui.renderer.TwoDRendererDebug;

public class MAPElitesViewer {
	
	protected JBotEvolver jbot;
	protected String folder;
	protected String baseFolder;
	protected TwoDRendererDebug renderer;
	protected JFrame frame;
	protected JLabel genLabel;
	protected Simulator sim;
	protected int currentGens;
	protected JSlider slider;
	
	protected ViewerThread viewer;
	protected ArrayList<String> files = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
//		new MAPElitesViewer("../../EvolutionAutomator/intersected_repertoire_all/", true);
//		new MAPElitesViewer("../../EvolutionAutomator/repertoire/", true);
//		new MAPElitesViewer("bigdisk/december2015/10samples/repertoire/", true);
		new MAPElitesViewer("bigdisk/repertoire/", true);
//		new MAPElitesViewer("hexamap_big/", true);
//		new MAPElitesViewer("hexamap_free/", true);
//		new MAPElitesViewer("hexamap_debug/", true);
	}
	
	public MAPElitesViewer(String folder, boolean gui) {
		this(folder);
		
		if(gui) {
			renderer = new TwoDRendererWheels(new Arguments(""));
			frame = createJFrame();
			setGeneration(currentGens);
			viewer = new ViewerThread();
			viewer.start();
		}
	}
	
	public MAPElitesViewer(String folder) {
		this.baseFolder = folder;
		
		searchFiles(new File(folder));
		
		this.folder = baseFolder+files.get(0);
		
		if(baseFolder.equals(files.get(0)+"/")) {
			this.folder = baseFolder;
		}
		
		
		load();
	}
	
	protected void searchFiles(File f) {
		
		if(f.isDirectory()) {
			
			for(String s : f.list()) {
				if(s.equals("repertoire_name.txt") || s.equals("_showbest_current.conf")) {
					files.add(f.getPath().replaceAll(this.baseFolder, ""));
					return;
				}
			}
			
			for(File i : f.listFiles()) {
				if(i.isDirectory()) {
					searchFiles(i);
				}
			}
		}
	}
	
	public void load() {
		
		int gens = 0;
		
		String file = folder+"/_showbest_current.conf";
		
		try {
			jbot = new JBotEvolver(new String[]{file});
			Population pop = jbot.getPopulation();
			gens = pop.getNumberOfCurrentGeneration();
			if(slider != null) {
				slider.setMaximum(gens);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		currentGens = gens;
	}
	
	public void play(Vector2d pos) {
		MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
		MOChromosome c = pop.getChromosomeFromBehaviorVector(new double[]{pos.x,pos.y});
		
		System.out.println("Expected: "+pos.x+" "+pos.y);
		
		if(c == null)
			return;
		
		sim = jbot.createSimulator();
		renderer.setSimulator(sim);
		sim.addRobots(jbot.createRobots(sim, c));
		sim.setupEnvironment();
		placeMarkers((MAPElitesPopulation)pop);
		if(sim.getArguments().get("--controllers").getCompleteArgumentString().contains("Hexa"))
			sim.simulate();
		else {
			for(double i = 0 ; i < sim.getEnvironment().getSteps() ; i++) {
				sim.performOneSimulationStep(i);
				refresh();
				try {Thread.sleep(50);} catch (InterruptedException e) {}
			}
		}
		sim.terminate();
		renderer.drawFrame();
	}
	
	public void setGeneration(int i) {
		try {
			jbot.loadFile(folder+"/show_best/showbest"+i+".conf", "");
			Population pop = jbot.getPopulation();
		
			sim = jbot.createSimulator();
			jbot.setupBestIndividual(sim);
			sim.setupEnvironment();
			
			placeMarkers(pop);
			
			if(genLabel != null)
				genLabel.setText("Generation: "+i);
			
			refresh();
			
		} catch(Exception e) {
			System.err.println("Can't find population file "+i);
			e.printStackTrace();
		}
	}
	
	public void refresh() {
		if(renderer != null) {
			renderer.setSimulator(sim);
			renderer.drawFrame();
		}
	}
	
	public void placeMarkers(Population p) {
		
		MAPElitesPopulation pop = (MAPElitesPopulation)p;
		
		int count = 0;
		
		for(int x = 0 ; x < pop.getMap().length ; x++) {
			for(int y = 0 ; y < pop.getMap()[x].length ; y++) {
				
				MOChromosome res = pop.getMap()[x][y];
				
				if(res != null) {
					
					ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
					BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
					
					if(br instanceof VectorBehaviourExtraResult) {
						double[] behavior = (double[])br.value();
						Vector2d pos = new Vector2d(behavior[0],behavior[1]);
						
						double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
						
						double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
						
						int[] supposedLocation = pop.getLocationFromBehaviorVector(behavior);
						
						Marker m;
						
						pos.x = (x-pop.getMap().length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						pos.y = (y-pop.getMap()[x].length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						
						if(supposedLocation[0] != x || supposedLocation[1] != y) {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.BLACK);
						} else {
							if(fitness < 0.8)
								m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.RED);
							else{
								m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, getColor(fitness));
								count++;
							}
						}
						sim.getEnvironment().addStaticObject(m);
					} else {
						if(br instanceof VectorBehaviourResult) {
							double[] behavior = (double[])br.value();
							Vector2d pos = new Vector2d(behavior[0],behavior[1]);
							
							Marker m = new Marker(sim, "m", pos.x, pos.y, Math.PI, 0.01, 0, Color.RED);
							sim.getEnvironment().addStaticObject(m);
						}
					}
				}
			}
		}
	}
	
	protected Color getColor(double fitness) {
		Color firstCol = Color.GREEN;
		Color secondCol = Color.RED;
		int R = (int)Math.abs(firstCol.getRed() * fitness + secondCol.getRed()* (1 - fitness));
		int G = (int)Math.abs(firstCol.getGreen() * fitness + secondCol.getGreen()* (1 - fitness));
		int B = (int)Math.abs(firstCol.getBlue() * fitness + secondCol.getBlue()* (1 - fitness));
		
		return new Color(R,G,B);
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
		slider = new JSlider(JSlider.HORIZONTAL,0,currentGens,0);
		
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
		
		for(int i = 0 ; i < 6 ; i++)
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
		
		JPanel sidePanel = new JPanel();
		
		String[] data = new String[files.size()];
		for(int i = 0 ; i < data.length ; i++) {
			data[i] = files.get(i);
		}
		
		final JList<String> list = new JList<String>(data);
		JScrollPane pane = new JScrollPane(list);
		pane.setPreferredSize(new Dimension(200, 600));
		sidePanel.add(pane);
		frame.add(sidePanel,BorderLayout.WEST);
		list.setSelectedIndex(0);
		
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {//This line prevents double events
					String str = list.getSelectedValue();
					loadFile(str);
			    }
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
	
	protected void loadFile(String file) {
		this.folder = this.baseFolder+file+"/";
		load();
		setGeneration(this.currentGens);
		refresh();
	}
	
	public class ViewerThread extends Thread{
		public void run() {
			while(true) {
				try {
					Vector2d loc = renderer.getSelectedLocation();
					if(loc != null) {
						play(loc);
						renderer.clearSelectedLocation();
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {}
				} catch(Exception e){
					e.printStackTrace();
				}
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