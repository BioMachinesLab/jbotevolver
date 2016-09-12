package renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mathutils.Vector2d;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 *
 * @author jorge
 */
public class PathTracer extends Tracer {

	private boolean hideStart = false;
	private boolean hideFinal = false;
	private boolean fade = false;
	private int steps = 10;
	private HashMap<Robot, List<Vector2d>> points = null;
	private HashMap<Vector2d, Integer> robotTrace = new HashMap<Vector2d, Integer>() ;
	private int frameCount = 0;

	public PathTracer(Arguments args) {
		super(args);
		hideStart = args.getFlagIsTrue("hidestart");
		hideFinal = args.getFlagIsTrue("hidefinal");
		fade = args.getFlagIsTrue("fade");
		steps = args.getArgumentAsIntOrSetDefault("steps", steps);
		
	}

	@Override
	public void update(Simulator simulator) {
		if (points == null) { // first step -- setup
			width = simulator.getEnvironment().getWidth();
			height = simulator.getEnvironment().getHeight();
			points = new HashMap<>();
		}

		if (insideTimeframe(simulator)) {
			// RECORD PATHS
			for (Robot r : simulator.getRobots()) {
				if (!points.containsKey(r)) {
					points.put(r, new ArrayList<Vector2d>());
				}
				Vector2d pos=new Vector2d(r.getPosition());
				
				points.get(r).add(pos);
				if (r instanceof JumpingSumo) {
					if (((JumpingSumo) r).isJumping())
						robotTrace.put(pos, 1);
					else if (((JumpingSumo) r).isDrivingAfterJumping())
						robotTrace.put(pos, 2);
					else if (!((JumpingSumo) r).statusOfJumping())
						robotTrace.put(pos, 3);
					else
						robotTrace.put(pos, 4);
				}else
					robotTrace.put(pos, 0);
			}
		}

		super.update(simulator);
	}

	public void snapshot(Simulator simulator) {

		double maxAbsX = 0, maxAbsY = 0;
		for (List<Vector2d> l : points.values()) {
			for (Vector2d v : l) {
				maxAbsX = Math.max(maxAbsX, Math.abs(v.x));
				maxAbsY = Math.max(maxAbsY, Math.abs(v.y));
			}
		}
		width = Math.max(Math.max(maxAbsX, maxAbsY) * 2, width);
		height = width;

		Graphics2D gr = createCanvas(simulator);

		// DRAW PATHS
		System.out.println(points);
		for (Robot r : points.keySet()) {
			List<Vector2d> pts = points.get(r);
			if (!fade) {
				if(drawWalls)
		        	drawWalls(gr,simulator);
				int[] xs = new int[pts.size()];
				int[] ys = new int[pts.size()];
				
				for (int i = 0; i < pts.size()-1; i++) {
					IntPos t = transform(pts.get(i).x, pts.get(i).y);
					IntPos t1 = transform(pts.get(i+1).x, pts.get(i+1).y);
					xs[i] = t.x;
					ys[i] = t.y;
					
//					if(robotTrace.get(pts.get(i))==1){
//						mainColor=Color.BLUE;
//						gr.setPaint(mainColor);
//					}
//					else
//						mainColor=Color.BLACK;
					
					if(robotTrace.get(pts.get(i))==1){  //laranja
						mainColor=new Color(255,0,127);
						System.out.println("1"+i);
						gr.setPaint(mainColor);
					}else if(robotTrace.get(pts.get(i))==2){
						mainColor=new Color(153
								,51,255); //amarelo
						System.out.println("2"+i);
						gr.setPaint(mainColor);
					}else if(robotTrace.get(pts.get(i))==3){
						mainColor=new Color(0,0,0);
						System.out.println("3"+i);
						gr.setPaint(mainColor);
					}
					else if(robotTrace.get(pts.get(i))==0){
						mainColor=new Color(0,0,0);
						System.out.println("0"+i);
						gr.setPaint(mainColor);
					}	
					
					else if(robotTrace.get(pts.get(i))==4){
						mainColor=new Color(0,0,0);
						System.out.println("0"+i);
						gr.setPaint(mainColor);
					}	
					//gr.setStroke(new BasicStroke(1.0f));
					
					//gr.setStroke(new BasicStroke(3.0f));
					gr.setStroke(new BasicStroke(5.0f));
					//gr.drawPolyline(xs, ys, pts.size());
					gr.drawLine(t.x, t.y, t1.x, t1.y);
				}
			} else {
				 if(drawWalls)
			        	drawWalls(gr,simulator);
				if (steps == 0) {
					steps = pts.size();
				}
				int stepSize = (int) Math.ceil(pts.size() / (double) steps);
				for (int s = 0; s < steps; s++) {
					// GET POINTS FOR SEGMENT
					int start = s * stepSize;
					int end = Math.min(start + stepSize, pts.size() - 1);

					if (end < start)
						continue;

					LinkedList<IntPos> polyLine = new LinkedList<>();
					for (int i = 0; i <= end - start; i++) {
						IntPos t = transform(pts.get(start + i).x,
								pts.get(start + i).y);
						
						if (polyLine.isEmpty() || polyLine.getLast().x != t.x
								|| polyLine.getLast().y != t.y) {
							polyLine.add(t);
						}
						if(robotTrace.get(pts.get(start + i))==1){  //laranja
							mainColor=new Color(255,128,0);
							System.out.println("1");
							gr.setPaint(mainColor);
						}else if(robotTrace.get(pts.get(start + i))==2){
							mainColor=new Color(255,255,0); //amarelo
							System.out.println("2");
							gr.setPaint(mainColor);
						}else if(robotTrace.get(pts.get(start + i))==3){
							mainColor=new Color(222,0,222);
							System.out.println("3");
							gr.setPaint(mainColor);
						}
						else if(robotTrace.get(pts.get(start + i))==0){
							mainColor=new Color(0,0,0);
							System.out.println("0");
							gr.setPaint(mainColor);
						}		
					}
					// CONVERT TO ARRAYS FORMAT
					int[] xs = new int[polyLine.size()];
					int[] ys = new int[xs.length];
					int i = 0;
					for (IntPos t : polyLine) {
						xs[i] = t.x;
						ys[i++] = t.y;
					}

					// DRAW POLYLINE
//					int alpha = Math.max(50,
//							(int) Math.round((double) (s + 1) / steps * 255));
					int alpha = 255;
					Color c = new Color(mainColor.getRed(),
							mainColor.getGreen(), mainColor.getBlue(), alpha);

					gr.setPaint(c);
					gr.setStroke(new BasicStroke(lineWidth));
					gr.drawPolyline(xs, ys, xs.length);
				}
			}
		}

		// size
		// gr.drawLine(0, 0, (int)(5*scale), 0);

		Color color;

		// DRAW INITIAL POSITIONS
		if (!hideStart) {

			for (Robot r : points.keySet()) {

				color = fade ? new Color(mainColor.getRed(),
						mainColor.getGreen(), mainColor.getBlue(), 50)
						: mainColor;

				drawRobot(gr, r, points.get(r).get(0), true, color);
			}
		}

		// DRAW FINAL POSITIONS
		if (!hideFinal) {
			for (Robot r : points.keySet()) {

				color = mainColor;

				drawRobot(gr, r, points.get(r).get(points.get(r).size() - 1),
						false, color);
			}
		}
		 
		writeGraphics(gr, simulator, name.isEmpty() ? "" + frameCount++ : name);
	}

	@Override
	public void terminate(Simulator simulator) {
		snapshot(simulator);
	}
	
	
}
