package gui.extended;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.physicalobjects.Marker;
import simulation.util.Arguments;
import updatables.Tracer;
import evolution.MAPElitesPopulation;
import evorbc.qualitymetrics.CircularQualityMetric;

public class MAPElitesTracer extends Tracer {
	
	public MAPElitesTracer(Arguments args) {
		super(args);
	}
	
	public void drawMapElites(Simulator sim, MAPElitesPopulation pop) {
		
		setup(sim);
		
		double limit = getLimit(pop);
		
		width = limit*2;
		height = limit*2;
		
		Graphics2D g = createCanvas(sim);
		drawMapElites(sim, pop, g, limit);
		writeGraphics(g,sim,"");
	}
	
	protected double getLimit(MAPElitesPopulation pop) {
		
		double limit = 0;
		
		for(int x = 0 ; x < pop.getMap().length ; x++) {
			for(int y = 0 ; y < pop.getMap()[x].length ; y++) {
				
				if(pop.getMap()[x][y] != null) {
					double px = (y - pop.getMap().length/2.0) * pop.getMapResolution();
					double py = (x - pop.getMap().length/2.0) * pop.getMapResolution();
					
					limit = Math.max(Math.abs(new Vector2d(px,py).length() + 0.05), limit);
				}
			}
		}
		
		double realLimit = 0;
		
		for(realLimit = 0 ; realLimit <= limit ; realLimit+=0.05);
		
		return Math.max(realLimit,0.15);
	}
	
	public Graphics2D drawMapElites(Simulator sim, MAPElitesPopulation pop, Graphics2D g, double limit) {
		
		drawGrid(g, limit);
		
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
						
						double fitness = CircularQualityMetric.calculateOrientationFitness(pos, orientation);
						
						int[] supposedLocation = pop.getLocationFromBehaviorVector(behavior);
						
						Marker m;
						
						//switch x and y, and angle
						orientation+=Math.PI/2;
						orientation*=-1;
						orientation+=Math.PI;
						pos.x = (y - pop.getMap().length/2.0) * pop.getMapResolution();
						pos.y = (x - pop.getMap().length/2.0) * pop.getMapResolution();
						
						if(supposedLocation[0] != x || supposedLocation[1] != y) {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.GRAY);
						} else {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, /*getColor(fitness)*/Color.GREEN.darker());
						}
						
						drawMarker(g,m);
					} else {
						if(br instanceof VectorBehaviourResult) {
							double[] behavior = (double[])br.value();
							Vector2d pos = new Vector2d(behavior[0],behavior[1]);
							
							Marker m = new Marker(sim, "m", pos.x, pos.y, Math.PI, 0.01, 0, Color.RED);
							drawMarker(g,m);
						}
					}
				}
			}
		}
		return g;
	}
	
	protected void drawGrid(Graphics2D g, double limit) {

		Stroke original = g.getStroke();
        
        int axisFontSize = 18;
        
        IntPos aa = transform(-0.075, limit+0.01);
        
		//draw ticks
		drawYTicks(g,limit);
		drawXTicks(g,limit);
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, axisFontSize));
		
		String limitStr = String.format("%1$,.2f", limit);
		
		//bl
		IntPos a = transform(-limit+0.01, -limit+0.02);
		g.drawString("-"+(limitStr), a.x, a.y);
		
		//br
		a = transform(limit-0.085, -limit+0.02);
		g.drawString(""+(limitStr), a.x, a.y);
		//tl
		a = transform(-limit+0.01, limit-0.04);
		g.drawString(""+(limitStr), a.x, a.y);
		
		
		a = transform(0, -limit);
		
//		g.setFont(new Font("Arial", Font.PLAIN, 22));
//		if(left)
//			g.drawString("Lateral displacement (m)", a.x-130, a.y+40);
//		
//		a = transform(0+xo, limit+yo);
//		
//		AffineTransform orig = g.getTransform();
//		g.rotate(-Math.PI/2);
//		if(bottom)
//			g.drawString("Forward displacement (m)", -a.x-130, a.y-40);
//		g.setTransform(orig);
		
		g.setStroke(original);
	}
	
	protected void drawYTicks(Graphics2D g, double limit) {
		
		g.setColor(Color.GRAY);
		
		IntPos a = transform(limit, -limit);
		IntPos b = transform(limit, limit);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		a = transform(-limit, -limit);
		b = transform(-limit, limit);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		//y = 0
		a = transform(-limit, 0);
		b = transform(-limit+0.015, 0);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		a = transform(limit, 0);
		b = transform(limit-0.015, 0);
		g.drawLine(a.x, a.y, b.x, b.y);
	}
	
	protected void drawXTicks(Graphics2D g, double limit) {

		g.setColor(Color.GRAY);
		
		IntPos a = transform(-limit, limit);
		IntPos b = transform(limit, limit);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		a = transform(-limit, -limit);
		b = transform(limit, -limit);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		//x=0
		a = transform(0, -limit);
		b = transform(0, -limit+0.015);
		g.drawLine(a.x, a.y, b.x, b.y);
		
		a = transform(0, limit);
		b = transform(0, limit-0.015);
		g.drawLine(a.x, a.y, b.x, b.y);
	}
	
	protected void drawMarker(Graphics2D g, Marker m) {
		
		IntPos a = transform(m.getPosition().getX(), m.getPosition().getY());
		
		int markerSize = 2;
		double markerLength = m.getLength();
		
		g.setColor(m.getColor());
		g.drawOval(a.x-markerSize/2, a.y-markerSize/2, markerSize, markerSize);
		
		double orientation = m.getOrientation();
		Vector2d endPoint = new Vector2d(m.getPosition());
		endPoint.add(new Vector2d(markerLength*Math.cos(orientation),markerLength*Math.sin(orientation)));
		
		IntPos end = transform(endPoint.x, endPoint.y);
		
		g.drawLine(a.x, a.y, end.x, end.y);
	}
	
	private Color getColor(double fitness) {
		Color firstCol = Color.GREEN;
		Color secondCol = Color.RED;
		int R = (int)Math.abs(firstCol.getRed() * fitness + secondCol.getRed()* (1 - fitness));
		int G = (int)Math.abs(firstCol.getGreen() * fitness + secondCol.getGreen()* (1 - fitness));
		int B = (int)Math.abs(firstCol.getBlue() * fitness + secondCol.getBlue()* (1 - fitness));
		
		return new Color(R,G,B);
	}

	@Override
	public void terminate(Simulator simulator) {
		
	}

	@Override
	public void snapshot(Simulator simulator) {
			
	}

}
