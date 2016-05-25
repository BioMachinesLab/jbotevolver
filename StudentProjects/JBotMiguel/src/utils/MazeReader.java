package utils;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;
import comm.FileProvider;

import evolutionaryrobotics.JBotEvolver;

/**
 *
 * @author jorge
 */
public class MazeReader {
    
    public static final String MAZE_ID = "maze";
    public static final String START_ID = "start";
    public static final String END_ID = "end";
    private final SVGDiagram diagram;
    private double scale = 0.01;
    private double wallSize = 0.01;
    private double offsetX = -280;
    private double offsetY = -40;
    
    public MazeReader(ByteArrayInputStream byteArrayInputStreamS) throws FileNotFoundException,IOException {
        SVGUniverse univ = new SVGUniverse();
        URI uri = univ.loadSVG(byteArrayInputStreamS, "maze");
        diagram = univ.getDiagram(uri);
    }
    
    public MazeReader(ByteArrayInputStream byteArrayInputStreamS, double scale, double wallSize) throws FileNotFoundException,IOException {
        this(byteArrayInputStreamS);
        this.scale = scale;
    }
    
    public Wall[] getSegments(Simulator simulator) throws SVGException {
        SVGElement maze = diagram.getElement(MAZE_ID);
        List<Wall> segs = new ArrayList<Wall>();
        for(Object o : maze.getChildren(null)) {
            if(o instanceof ShapeElement) {
                Shape shape = ((ShapeElement) o).getShape();
                PathIterator iter = shape.getPathIterator(null,0.001d);
                Vector2d last = null;
                while(!iter.isDone()) {
                    double[] coords = new double[2];
                    iter.currentSegment(coords);
                    Vector2d curr = new Vector2d((coords[0]+offsetX)*scale,(coords[1]+offsetY)*scale*-1);
                    if(last != null) {
                        segs.add(new Wall(simulator, last, curr,wallSize));
                        System.out.println(last.x+" "+last.y+" "+curr.x+" "+curr.y+" "+wallSize);
                    }
                    last = curr;
                    iter.next();
                }
            }
        }
        
        return segs.toArray(new Wall[segs.size()]);
    }
    
    public String getSegmentsAsText(Simulator simulator) throws SVGException {
    	String text = "";
        SVGElement maze = diagram.getElement(MAZE_ID);
        List<Wall> segs = new ArrayList<Wall>();
        for(Object o : maze.getChildren(null)) {
            if(o instanceof ShapeElement) {
                Shape shape = ((ShapeElement) o).getShape();
                PathIterator iter = shape.getPathIterator(null,0.001d);
                Vector2d last = null;
                while(!iter.isDone()) {
                    double[] coords = new double[2];
                    iter.currentSegment(coords);
                    Vector2d curr = new Vector2d((coords[0]+offsetX)*scale,(coords[1]+offsetY)*scale*-1);
                    if(last != null) {
                        segs.add(new Wall(simulator, last, curr,wallSize));
                        text+=last.x+" "+last.y+" "+curr.x+" "+curr.y+" "+wallSize+"\n";
                    }
                    last = curr;
                    iter.next();
                }
            }
        }
        
        return segs.size()+"\n"+text;
    }
    
    public Vector2d getStart() throws SVGException {
        return getPoint(START_ID);
    }
    
    public Vector2d getEnd() throws SVGException {
        return getPoint(END_ID);
    }
    
    private Vector2d getPoint(String id) throws SVGException {
        SVGElement point = diagram.getElement(id);
        ShapeElement shape = (ShapeElement) point;
        Rectangle2D bb = shape.getBoundingBox();
        return new Vector2d((bb.getX()+offsetX)*scale,(bb.getY()+offsetY)*scale*-1);
    }
    
    public static void main(String[] args) throws Exception{
		String[] mazes = new String[]{/*"star","zigzag","hard","medium","multi","open","subset","trumpet",*/ "obstacle"};
		for(String s : mazes) {
			MazeReader m = new MazeReader(FileProvider.getDefaultFileProvider().getFile("mazes/svg/"+s+".svg"));
			JBotEvolver j = new JBotEvolver(new String[]{"../../EvolutionAutomator/wheels_maze/AWS_3Actuator_zigzag/1/_showbest_current.conf"});
			String txt = m.getSegmentsAsText(j.createSimulator());
			FileWriter file = new FileWriter("mazes/svg/"+s+".txt");
		    BufferedWriter output = new BufferedWriter(file);
		    output.write(txt);
		    output.write(m.getStart().x+" "+m.getStart().y+" ");
		    output.write(m.getEnd().x+" "+m.getEnd().y);
		    output.close();
		}
	}
    
}