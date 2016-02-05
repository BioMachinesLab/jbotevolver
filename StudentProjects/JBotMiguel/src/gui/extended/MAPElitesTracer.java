package gui.extended;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
import evaluationfunctions.OrientationEvaluationFunction;
import evolution.MAPElitesPopulation;

public class MAPElitesTracer extends Tracer {
	
	public MAPElitesTracer(Arguments args) {
		super(args);
	}
	
	public void drawMapElites(String[] names, Simulator[] sim, MAPElitesPopulation[] pop) {
		
		double startxo = -0.35;
		
		double xo = startxo;//-sim.length*0.8/2;
		double yo = -0.7;
		
		double xinc = 0.75;
		double yinc = 0.78;

		setup(sim[0]);
		
		width = 1.7;
		height = (sim.length+1)/2;
		
		Graphics2D g = createCanvas(sim[0]);
		
		for(int i = 0 ; i < sim.length ; i++) {
			
			Simulator s = sim[i];
			MAPElitesPopulation p = pop[i];
			
//			if(i == sim.length/2)
//				drawMapElites(s, p, g, xo, yo, false, true);
//			else if(i == 0)
//				drawMapElites(s, p, g, xo, yo, true, false);
//			else
				drawMapElites(s, p, g, xo, yo, false, false, names[i]);
			xo+=xinc;
			
			if(i%2==0) {
				yo+=yinc;
				xo=startxo;
			}
			
		}
		
		writeGraphics(g,sim[0],"");
	}
	
	public void drawMapElites(Simulator sim, MAPElitesPopulation pop) {
		
		setup(sim);
		width = 1;
		height = 1;
		
		Graphics2D g = createCanvas(sim);
		drawMapElites(sim, pop, g, 0, 0, true, true,"");
		writeGraphics(g,sim,"");
	}
	
	public Graphics2D drawMapElites(Simulator sim, MAPElitesPopulation pop, Graphics2D g, double xo, double yo, boolean leftText, boolean bottomText, String name) {
		
		drawGrid(g,xo,yo,leftText,bottomText,name);
		
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
						
						//switch x and y, and angle
						orientation+=Math.PI/2;
						orientation*=-1;
						orientation+=Math.PI;
						pos.x = (y-pop.getMap()[x].length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						pos.y = (x-pop.getMap().length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						
						pos.x+=xo;
						pos.y+=yo;
						
						if(supposedLocation[0] != x || supposedLocation[1] != y) {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.GRAY);
						} else {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, /*getColor(fitness)*/Color.GREEN.darker());
//							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, getColor(fitness));
							
							if(fitness < 0.8) {
								m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.RED);
							}
							
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
	
	protected void drawGrid(Graphics2D g, double xo, double yo, boolean left, boolean bottom, String name) {
		double inc = 0.1;
		float lineWidth = 0.2f;

		Stroke original = g.getStroke();
//		BasicStroke dashed = new BasicStroke(lineWidth,
//                BasicStroke.CAP_BUTT,
//                BasicStroke.JOIN_MITER,
//                2.0f, new float[]{3f}, 0.0f);
//        g.setStroke(dashed);
        
        double limit = 0.35;
        
        DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
        
        int axisFontSize = 18;
        
        IntPos aa = transform(-0.075+xo, limit+yo+0.01);
        
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.setColor(Color.BLACK);
		g.drawString(name, aa.x, aa.y);
        
		for(double y = -limit, count = 0 ; y <= limit+inc/2.0 ; y+=inc) {
			
			double d = new Double(df2.format(y)).doubleValue();
			
			if(Math.abs(d) != limit) {
				
				IntPos a = transform(-limit+xo, y+yo);
				IntPos b = transform(-limit+xo+0.015, y+yo);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
				
				a = transform(limit+xo, y+yo);
				b = transform(limit+xo-0.015, y+yo);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
				
			} else {
				
				IntPos a = transform(-limit+xo, y+yo);
				IntPos b = transform(limit+xo, y+yo);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
			}
		}
		
		for(double x = -limit, count = 0 ; x <= limit+inc/2.0 ; x+=inc) {
			
			double d = new Double(df2.format(x)).doubleValue();
			
			if(Math.abs(d) != limit) {
				
				IntPos a = transform(x+xo, -limit+yo);
				IntPos b = transform(x+xo, -limit+yo+0.015);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
				
				a = transform(x+xo, limit+yo);
				b = transform(x+xo, limit+yo-0.015);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
				
			} else {
				
				IntPos a = transform(x+xo, -limit+yo);
				IntPos b = transform(x+xo, limit+yo);
				g.setColor(Color.GRAY);
				g.drawLine(a.x, a.y, b.x, b.y);
	
			}
		}
		
		
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, axisFontSize));
		
		//bl
		IntPos a = transform(-limit+xo+0.01, -limit+yo+0.02);
		g.drawString(""+(-limit), a.x, a.y);
		
		//br
		a = transform(limit+xo-0.085, -limit+yo+0.02);
		g.drawString(""+(limit), a.x, a.y);
		//tl
		a = transform(-limit+xo+0.01, limit+yo-0.04);
		g.drawString(""+(limit), a.x, a.y);
		
		
		a = transform(0+xo, -limit+yo);
		
		g.setFont(new Font("Arial", Font.PLAIN, 22));
		if(left)
			g.drawString("Lateral displacement (m)", a.x-130, a.y+40);
		
		a = transform(0+xo, limit+yo);
		
		AffineTransform orig = g.getTransform();
		g.rotate(-Math.PI/2);
		if(bottom)
			g.drawString("Forward displacement (m)", -a.x-130, a.y-40);
		g.setTransform(orig);
		
		g.setStroke(original);
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
