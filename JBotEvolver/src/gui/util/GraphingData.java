package gui.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.util.Vector;

import javax.swing.JPanel;

public class GraphingData extends JPanel {
	private static final int MAX = 4000;
	private int showLast = 2000;
	final int PAD = 20;
	private String xLabel = "Generations";
	private double max = 0;
	private double xInc = 0;
	private double scale = 0;
	
	private Vector<Vector<Double>> listOfData = new Vector<Vector<Double>>();
	private Vector<Double> simpleData = new Vector<Double>();

	public void addData(Double value) {
		simpleData.add(value);
		if (simpleData.size() > MAX) {
			simpleData.remove(0);
		}
		listOfData.clear();
		listOfData.add(0, simpleData);
		repaint();
	}
	
	public void addDataList(Double[] dataList){
		Vector<Double> aux = new Vector<Double>();
		
		for (int i = 0; i < dataList.length; i++) 
			aux.add(dataList[i]);
		
		listOfData.add(aux);
	}

	public int getShowLast() {
		return showLast;
	}

	public void setShowLast(int showLast) {
		this.showLast = showLast;
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth();
		int h = getHeight();
		// Draw ordinate.
		g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
		// Draw abcissa.
		g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));
		// Draw labels.
		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics("0", frc);
		float sh = lm.getAscent() + lm.getDescent();
		// Ordinate label.
		String s = "data";
		float sy = PAD + ((h - 2 * PAD) - s.length() * sh) / 2 + lm.getAscent();
		// for(int i = 0; i < s.length(); i++) {
		// String letter = String.valueOf(s.charAt(i));
		// float sw = (float)font.getStringBounds(letter, frc).getWidth();
		// float sx = (PAD - sw)/2;
		// g2.drawString(letter, sx, sy);
		// sy += sh;
		// }
		// Abcissa label.
		s = xLabel;
		sy = h - PAD + (PAD - sh) / 2 + lm.getAscent();
		float sw = (float) font.getStringBounds(s, frc).getWidth();
		float sx = (w - sw) / 2;
		g2.drawString(s, sx, sy);
		
		Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.BLACK, Color.CYAN, Color.DARK_GRAY, Color.MAGENTA,Color.YELLOW,};
		int colorIndex = 0;

		for (Vector<Double> dataList : listOfData) {
			double currentMax = getMax(dataList);
			
			if(currentMax > max)
				max = currentMax;
			
			xInc = (double) (w - 2 * PAD) / (showLast - 1);
			scale = (double) (h - 2 * PAD) / max;
		}
		
		for (Vector<Double> dataList : listOfData) {
			
			Vector<Double> data = new Vector<Double>();
			
			data.addAll(dataList);
			
			int currentColor = (colorIndex++)%colors.length;

			// Draw lines.
			int init = Math.max(0, data.size() - showLast);
			g2.setPaint(colors[currentColor]);
			for (int i = init; i < data.size() - 1; i++) {
				double x1 = PAD + (i - init) * xInc;
				double y1 = h - PAD - scale * data.get(i);
				double x2 = PAD + (i - init + 1) * xInc;
				double y2 = h - PAD - scale * data.get(i + 1);
				g2.draw(new Line2D.Double(x1, y1, x2, y2));
			}
			
			// Mark data points.
//			g2.setPaint(Color.red);
//			for (int i = Math.max(0, data.size() - showLast); i < data.size(); i++) {
//				double x = PAD + (i - init) * xInc;
//				double y = h - PAD - scale * data.get(i);
//				g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
//			}

		}
		
		g2.setPaint(Color.black);
		double scale = (double) (h - 2 * PAD) / max;
		g2.drawString(String.valueOf(max), 0, (int) (h - PAD - scale * max));
		
	}

	private double getMax(Vector<Double> dataList) {
		double max = -Integer.MAX_VALUE;
		for (int i = Math.max(0, dataList.size() - showLast); i < dataList.size(); i++) {
			if (dataList.get(i) > max)
				max = dataList.get(i);
		}
		return max;
	}
	
	public void setxLabel(String name,int generationsNumber) {
		xLabel = name + " ( " + generationsNumber + " )";
	}
}