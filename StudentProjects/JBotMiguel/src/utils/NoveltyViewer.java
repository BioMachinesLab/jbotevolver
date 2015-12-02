package utils;

import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedList;
import javax.swing.JFrame;
import mathutils.Vector2d;
import novelty.BehaviourResult;
import novelty.NoveltyEvaluation;
import novelty.NoveltyEvaluation.ArchiveEntry;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;
import evolution.PostEvaluator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.populations.Population;
import gui.renderer.TwoDRenderer;

public class NoveltyViewer {
	
	public static void main(String[] args) throws Exception {
		
//		String folder = "nsga2_nf";
//		String folder = "nsga2_novelty";
		String folder = "br_novelty";
		
		String file = folder+"/_showbest_current.conf";
		
		JBotEvolver jbot = new JBotEvolver(new String[]{file});
		Population pop = jbot.getPopulation();
		int gens = pop.getNumberOfCurrentGeneration();
		
//		ResultViewerGui gui = new ResultViewerGui(jbot, new Arguments(""));
		TwoDRenderer renderer = new TwoDRenderer(new Arguments(""));
		
		JFrame frame = new JFrame();
		frame.add(renderer);
		frame.setSize(700, 700);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		for(int i = 0 ; i < gens ; i++) {
			jbot.loadFile(folder+"/show_best/showbest"+i+".conf", "");
			pop = jbot.getPopulation();
		
			Simulator sim = jbot.createSimulator();
			jbot.setupBestIndividual(sim);
			sim.setupEnvironment();
			
			NoveltyEvaluation post = (NoveltyEvaluation)getPostEvaluator(pop, NoveltyEvaluation.class);
			
			placeLightPoles(post,sim);
			
			renderer.setSimulator(sim);
			renderer.drawFrame();
			
			System.out.println(pop.getNumberOfCurrentGeneration());
			Thread.sleep(10);
		}
	}
	
	public static void placeLightPoles(NoveltyEvaluation post, Simulator sim) {
		for(ArchiveEntry entry : post.getArchive()) {
			BehaviourResult res = entry.getBehaviour();
			double[] behavior = (double[])res.value();
			Vector2d pos = new Vector2d(behavior[0],behavior[1]);
			LightPole lp = new LightPole(sim, "lp", pos.x, pos.y, 0.01);
			lp.setColor(Color.RED);
			sim.getEnvironment().addStaticObject(lp);
		}
	}
	
	public static PostEvaluator getPostEvaluator(Population pop, Class c) {
		LinkedList<Serializable> objs = pop.getSerializableObjects();
		for(Serializable s : objs) {
			if(s.getClass() == c)
				return (PostEvaluator)s;
		}
		return null;
	}

}
