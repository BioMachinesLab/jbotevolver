package gui.util;

// GraphViz.java - a simple API to call dot from Java programs

/*$Id$*/
/*
 ******************************************************************************
 *                                                                            *
 *              (c) Copyright 2003 Laszlo Szathmary                           *
 *                                                                            *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms of the GNU Lesser General Public License as published by   *
 * the Free Software Foundation; either version 2.1 of the License, or        *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY *
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public    *
 * License for more details.                                                  *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public License   *
 * along with this program; if not, write to the Free Software Foundation,    *
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                              *
 *                                                                            *
 ******************************************************************************
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import evolutionaryrobotics.neuralnetworks.CTRNNMultilayer;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

/**
 * <dl>
 * <dt>Purpose: GraphViz Java API
 * <dd>
 *
 * <dt>Description:
 * <dd> With this Java class you can simply call dot
 *      from your Java programs
 * <dt>Example usage:
 * <dd>
 * <pre>
 *    GraphViz gv = new GraphViz();
 *    gv.addln(gv.start_graph());
 *    gv.addln("A -> B;");
 *    gv.addln("A -> C;");
 *    gv.addln(gv.end_graph());
 *    System.out.println(gv.getDotSource());
 *
 *    String type = "gif";
 *    File out = new File("out." + type);   // out.gif in this example
 *    gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
 * </pre>
 * </dd>
 *
 * </dl>
 *
 * @version v0.4, 2011/02/05 (February) -- Patch of Keheliya Gallaba is added. Now you
 * can specify the type of the output file: gif, dot, fig, pdf, ps, svg, png, etc.
 * @version v0.3, 2010/11/29 (November) -- Windows support + ability 
 * to read the graph from a text file
 * @version v0.2, 2010/07/22 (July) -- bug fix
 * @version v0.1, 2003/12/04 (December) -- first release
 * @author  Laszlo Szathmary (<a href="jabba.laci@gmail.com">jabba.laci@gmail.com</a>)
 */
public class GraphViz
{
   /**
    * The dir. where temporary files will be created.
    */
	protected static String TEMP_DIR = System.getProperty("user.dir")+"/graphviz";

   /**
    * Where is your dot program located? It will be called externally.
    */
   protected static String DOT; 

   /**
    * The source of the graph written in dot language.
    */
	protected StringBuilder graph = new StringBuilder();
	
	protected int input = 0;
	protected int hidden = 0;
	protected int output = 0;
	
	protected NeuralNetwork network = null;
	
	protected ImageShower imageShower = null;
	
	protected String type = "png";
	
	protected int resolutionHeight = 0;

   /**
    * Constructor: creates a new GraphViz object that will contain
    * a graph.
    */
   public GraphViz() {
	   String os = System.getProperty("os.name");
	   
	   if(os.contains("Windows")) {
		   DOT="C:/Program Files (x86)/Graphviz2.38/bin/dot.exe";
		   //DOT = "c:/Program Files/Graphviz2.26.3/bin/dot.exe";
	   }else if(os.contains("OS X")) {
		  DOT = "/usr/local/bin/dot";
	   }else {
		   DOT = "/usr/bin/dot"; 
	   }
	   
	   File dir = new File(TEMP_DIR);
	   
	   if(!dir.exists())
		   dir.mkdir();
	   
	   GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	   resolutionHeight = gd.getDisplayMode().getHeight() - 100;
   }
   
   public GraphViz(NeuralNetwork network) {
	   this();
	   this.network = network;
	   setupNetwork();
   }
   
   public GraphViz(int input, int hidden, int output) {
	   this();
	   
	   this.input = input;
	   this.hidden = hidden;
	   this.output = output;
	   
	   setupNetwork();
   }
   
   protected void setupNetwork() {
	   this.graph = new StringBuilder();
	   
	   addln(start_graph());
	   
	   if(network != null) {
		   this.input = network.getNumberOfInputNeurons();
		   this.output = network.getNumberOfOutputNeurons();
		   
		   if(network.getClass().equals(CTRNNMultilayer.class)) {
			   CTRNNMultilayer multilayer = (CTRNNMultilayer)network;
			   this.hidden = multilayer.getHiddenStates().length;
			   
			   createNodes();
			   connectNetwork();
		   }
	   }
	   
	   addln(end_graph());
   }
   
   private void createNodes() {
	   String result = "node [shape=circle,fixedsize=true,width=0.9];";
	   
	   result+="size=\"13,13\"; ranksep=\"2.2 equally\"";
	   
	   result+="{rank=same;";
	   for(int i = 0 ; i < input ; i++)
		   result+="in"+i+" ";
	   result+=";}";
	   
	   if(hidden > 0) {
		   result+="{rank=same;";
		   for(int i = 0 ; i < hidden ; i++)
			   result+="hi"+i+" ";
		   result+=";}";
	   }
	   
	   result+="{rank=same;";
	   for(int i = 0 ; i < output ; i++)
		   result+="out"+i+" ";
	   result+=";}";
	   
	   String defaultColor = "FFFFFF";
	   String currentColor;

	   for(int i = 0 ; i < input ; i++) {
		   currentColor = defaultColor;
		   if(network != null) 
			   currentColor = getColor(network.getInputNeuronStates()[i]);
		   result+="in"+i+" [style=filled, fillcolor=\"#"+currentColor+"\"];";
	   }
	
	   for(int i = 0 ; i < hidden ; i++) {
		   currentColor = defaultColor;
		   if(network != null) {
			   CTRNNMultilayer ctrnn = (CTRNNMultilayer)network;
			   currentColor = getColor(ctrnn.getHiddenStates()[i]);
		   }
		   result+="hi"+i+" [style=filled, fillcolor=\"#"+currentColor+"\"];";
	   }
	   
	   for(int i = 0 ; i < output ; i++) {
		   currentColor = defaultColor;
		   if(network != null) 
			   currentColor = getColor(network.getOutputNeuronStates()[i]);
		   result+="out"+i+" [style=filled, fillcolor=\"#"+currentColor+"\"];";
	   }

	   addln(result);
   }
   
   private String getColor(double activation) {
	   //activation can go well beyond [-1.0:1.0], this is wrong
	   double color = 255-255.0*activation;
	   String currentColor = Integer.toHexString((int)color);
	   currentColor+=currentColor+currentColor;
	   return currentColor;
   }
   
   private void connectNetwork() {
	   
	   if(hidden > 0) {
		   
		   for(int i = 0 ; i < input ; i++)
			   for(int h = 0 ; h < hidden ; h++)
				   addln("in"+i+" -> hi"+h+";");
		   
		   for(int h1 = 0 ; h1 < hidden ; h1++)
			   for(int h2 = h1 ; h2 < hidden ; h2++)
				   addln("hi"+h1+" -> hi"+h2+";");
		   
		   for(int h = 0 ; h < hidden ; h++)
			   for(int o = 0 ; o < output ; o++)
				   addln("hi"+h+" -> out"+o+";");
		   
	   } else {
		   
		   for(int i = 0 ; i < input ; i++)
			   for(int o = 0 ; o < output ; o++)
				   addln("in"+i+" -> out"+o+";");  
	   }
   }
   
   public void changeNeuralNetwork(NeuralNetwork n) {
	   this.network = n;
	   setupNetwork();
	   if(imageShower != null) {
		   BufferedImage img = this.getGraph(this.getDotSource());
		   imageShower.changeImage(img);
	   }
   }
   
   public void show() {
	   if(imageShower == null) {
		   imageShower = new ImageShower(this.getGraph(this.getDotSource()));
	   }
	   imageShower.setVisible(true);
   }

   /**
    * Returns the graph's source description in dot language.
    * @return Source of the graph in dot language.
    */
   public String getDotSource() {
      return graph.toString();
   }

   /**
    * Adds a string to the graph's source (without newline).
    */
   public void add(String line) {
      graph.append(line);
   }

   /**
    * Adds a string to the graph's source (with newline).
    */
   public void addln(String line) {
      graph.append(line + "\n");
   }

   /**
    * Adds a newline to the graph's source.
    */
   public void addln() {
      graph.append('\n');
   }

   /**
    * Returns the graph as an image in binary format.
    * @param dot_source Source of the graph to be drawn.
    * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
    * @return A byte array containing the image of the graph.
    */
   public BufferedImage getGraph(String dot_source)
   {
      File dot;
      byte[] img_stream = null;
      try {
    	 long time = System.currentTimeMillis();
         dot = writeDotSourceToFile(dot_source);
//         System.out.println("time "+(System.currentTimeMillis()-time));
         if (dot != null)
         {
            img_stream = get_img_stream(dot, type);
            InputStream in = new ByteArrayInputStream(img_stream);
            BufferedImage image = ImageIO.read(in);
            return image;
         }
         return null;
      } catch (Exception ioe) { return null; }
   }

   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param file  Name of the file to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, String file)
   {
      File to = new File(file);
      return writeGraphToFile(img, to);
   }

   /**
    * Writes the graph's image in a file.
    * @param img   A byte array containing the image of the graph.
    * @param to    A File object to where we want to write.
    * @return Success: 1, Failure: -1
    */
   public int writeGraphToFile(byte[] img, File to)
   {
      try {
         FileOutputStream fos = new FileOutputStream(to);
         fos.write(img);
         fos.close();
      } catch (IOException ioe) { ioe.printStackTrace(); return -1; }
      return 1;
   }

   /**
    * It will call the external dot program, and return the image in
    * binary format.
    * @param dot Source of the graph (in dot language).
    * @param type Type of the output image to be produced, e.g.: gif, dot, fig, pdf, ps, svg, png.
    * @return The image of the graph in .gif format.
    */
   private byte[] get_img_stream(File dot, String type)
   {
      File img;
      byte[] img_stream = null;
	  long time = System.currentTimeMillis();
	  
      try {
         img = File.createTempFile("graph_", "."+type, new File(GraphViz.TEMP_DIR));
         img.deleteOnExit();
         Runtime rt = Runtime.getRuntime();
         
         // patch by Mike Chenault
         String[] args = {DOT, "-T"+type, dot.getAbsolutePath(), "-o", img.getAbsolutePath()};
         Process p = rt.exec(args);
         
         p.waitFor();
         
//         System.out.println("wait "+(System.currentTimeMillis()-time));

         FileInputStream in = new FileInputStream(img.getAbsolutePath());
         img_stream = new byte[in.available()];
         in.read(img_stream);
         // Close it if we need to
         if( in != null ) in.close();
         
//         System.out.println("read "+(System.currentTimeMillis()-time));

//         if (img.delete() == false) 
//            System.err.println("Warning: " + img.getAbsolutePath() + " could not be deleted!");
         
//         System.out.println("delete "+(System.currentTimeMillis()-time));
      }
      catch (IOException ioe) {
         System.err.println("Error:    in I/O processing of tempfile in dir " + GraphViz.TEMP_DIR+"\n");
         System.err.println("       or in calling external command");
         ioe.printStackTrace();
      }
      catch (InterruptedException ie) {}
      
      return img_stream;
   }

   /**
    * Writes the source of the graph in a file, and returns the written file
    * as a File object.
    * @param str Source of the graph (in dot language).
    * @return The file (as a File object) that contains the source of the graph.
    */
   private File writeDotSourceToFile(String str) throws java.io.IOException
   {
      File temp;
      try {
         temp = File.createTempFile("graph_", ".dot.tmp", new File(GraphViz.TEMP_DIR));
         temp.deleteOnExit();
         FileWriter fout = new FileWriter(temp);
         fout.write(str);
         fout.close();
      }
      catch (Exception e) {
    	 e.printStackTrace();
         System.err.println("Error: I/O error while writing the dot source to temp file!");
         return null;
      }
      return temp;
   }

   /**
    * Returns a string that is used to start a graph.
    * @return A string to open a graph.
    */
   public String start_graph() {
      return "digraph G {";
   }

   /**
    * Returns a string that is used to end a graph.
    * @return A string to close a graph.
    */
   public String end_graph() {
      return "}";
   }
   
   public class ImageShower extends JFrame{
	   
	   private ImagePanel panel;
	   
	   public ImageShower(BufferedImage img) {
		   panel = new ImagePanel(img);
		   getContentPane().add(panel);
		   setSize(panel.getSize());
		   setVisible(true);
	   }
	   
	   public void changeImage(BufferedImage img) {
		   panel.setImage(img);
		   setSize(panel.getSize());
		   this.repaint();
	   }
   }
   
   public class ImagePanel extends JPanel {
	   
	   private BufferedImage img;
	   private Dimension d = new Dimension(1,1);
	   
	   public ImagePanel(BufferedImage img) {
		   setImage(img);
	   }
	   
	   public void setImage(BufferedImage img) {
		   
		   if(img != null)
			   d = new Dimension(img.getWidth(),img.getHeight()+20);
		   
		   if(img != null && img.getHeight() > resolutionHeight) {
			   img = resizeImg(img);
		   }
		   
		   this.img = img;
	   }
	   
	   public BufferedImage resizeImg(BufferedImage before) {
		   int w = before.getWidth();
		   int h = before.getHeight();
		   BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		   AffineTransform at = new AffineTransform();
		   at.scale(resolutionHeight/(double)h,resolutionHeight/(double)h);
		   AffineTransformOp scaleOp = 
		      new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		   after = scaleOp.filter(before, after);
		   d = new Dimension((int)(resolutionHeight/(double)h*before.getWidth()), (int)(resolutionHeight/(double)h*before.getHeight())+20);
		   return after;
	   }
	   
	   public void paintComponent(Graphics g) {
		   if(img != null)
			   g.drawImage(img, 0, 0, null);
	   }
	   
	   @Override
	   public Dimension getSize() {
		   return d;
	   }
   }
}