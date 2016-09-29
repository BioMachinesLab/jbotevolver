package evorbc.utils;

import java.awt.Color;
import java.util.ArrayList;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.results.VectorBehaviourResult;
import simulation.physicalobjects.Marker;
import taskexecutor.results.SimpleFitnessResult;
import evolution.MAPElitesPopulation;
import evolution.NDBehaviorMap;
import evolution.NDMAPElitesEvolution;
import evolution.NDMAPElitesPopulation;
import evolutionaryrobotics.populations.Population;

public class NDMAPElitesViewer extends MAPElitesViewer{
	
	public static void main(String[] args) throws Exception {
		new NDMAPElitesViewer("rep_test/", true);
//		new NDMAPElitesViewer("repertoire/", true);
	}
	
	public NDMAPElitesViewer(String folder, boolean gui) {
		super(folder,gui);
	}
	
	public NDMAPElitesViewer(String folder) {
		super(folder);
	}
	
	@Override
	public void play(Vector2d pos) {
		NDMAPElitesPopulation pop = (NDMAPElitesPopulation)jbot.getPopulation();
		
		handlePopulation(pop);
		
		MOChromosome c = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{pos.x,pos.y,0});
		
		double angle = -Math.PI;
		while(c == null && angle <= Math.PI) {
			c = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{pos.x,pos.y,angle});
			angle+=Math.toRadians(45);
		}

		if(c == null) {
			System.out.println("Cant find behavior at pos "+pos.x+","+pos.y);
			return;
		}
		
		
		int[] buckets = pop.getNDBehaviorMap().getBucketsFromBehaviorVector(pop.getNDBehaviorMap().getBehaviorVector(c));
		for(int i = 0 ; i < buckets.length ; i++) {
			System.out.print(buckets[i]+" ");
		}
		System.out.println();
		
//		System.out.println(pos.x+" "+pos.y+" "+pop.getNDBehaviorMap().getLocationHash(c));
		
		sim = jbot.createSimulator();
		renderer.setSimulator(sim);
		sim.addRobots(jbot.createRobots(sim, c));
		sim.setupEnvironment();
		
		placeMarkers(pop);
		
		if(sim.getArguments().get("--controllers").getCompleteArgumentString().contains("Hexa"))
			sim.simulate();
		else {
			for(double i = 0 ; i < sim.getEnvironment().getSteps() ; i++) {
				sim.performOneSimulationStep(i);
				refresh();
				try {Thread.sleep(50);} catch (InterruptedException e) {}
			}
		}
		
//		System.out.println("Final orientation: "+sim.getRobots().get(0).getOrientation()+", desired orientation: "+vec[2]+", "+sim.getRobots().get(0).getPosition());
		
		sim.terminate();
		renderer.drawFrame();
	}
	
	public void handlePopulation(NDMAPElitesPopulation pop) {
//		System.out.println(pop.getChromosomes().length);
//		if(pop.getNumberOfCurrentGeneration()==1)
////			NDMAPElitesEvolution.expandToCircle(pop);
//			NDMAPElitesEvolution.expandToCircleEuclidean(pop);
//		System.out.println(pop.getChromosomes().length);
	}
	
	public void placeMarkers(Population p) {
		
		NDMAPElitesPopulation pop = (NDMAPElitesPopulation)p;
		
		handlePopulation(pop);
		
		NDBehaviorMap map = pop.getNDBehaviorMap();
		
		map = NDBehaviorMap.deserialize(map.serialize());
		
		double[][] vals = new double[map.getSizes()[0]][map.getSizes()[1]];
		
		double[] xBuckets = map.getBucketListing(0);
		double[] yBuckets = map.getBucketListing(1);
		double[] orBuckets = map.getBucketListing(2);
		for(int x = 0 ; x < xBuckets.length ; x++) {
			for(int y = 0 ; y < yBuckets.length ; y++) {
				for(int z = 0 ; z < orBuckets.length ; z++) {
					
					double[] vec = new double[]{xBuckets[x],yBuckets[y],orBuckets[z]};
		
					MOChromosome res = map.getChromosomeFromBehaviorVector(vec);
					
					if(res == null)
						continue;
					
					double[] behavior = map.getBehaviorVector(res);
					
//					System.out.println(behavior[0]+","+behavior[1]+","+behavior[2]);
//					System.out.println(xBuckets[x]+","+yBuckets[y]+","+orBuckets[z]);
					
					Vector2d pos = new Vector2d(xBuckets[x],yBuckets[y]);
					
					vals[x][y]++;
					
					if(vals[x][y] == 1) {
						
						for(int i = 0 ; i < orBuckets.length ; i++) {
							
							sim.getEnvironment().addStaticObject(getMarker(new double[]{pos.x, pos.y, orBuckets[i]},pop,Color.DARK_GRAY,0.01, 0.01));
						}
					}
					sim.getEnvironment().addStaticObject(getMarker(new double[]{pos.x, pos.y, behavior[2]},pop,Color.GREEN,0.00, 0.01));
				}
			}
		}
	}
	
	private Marker getMarker(double[] vec, NDMAPElitesPopulation pop, Color c, double radius, double length) {
		//we specify the center of the buckets using x,y, but they actually refer to their border. 
		//we must shift the x,y coordinates to accurately represent them.
		double x = vec[0] + pop.getNDBehaviorMap().getResolutions()[0]/2;
		double y = vec[1] + pop.getNDBehaviorMap().getResolutions()[1]/2;
		double z = vec[2];
		return new Marker(sim, "", x, y, z, radius*2, length, c, true);
	}
}