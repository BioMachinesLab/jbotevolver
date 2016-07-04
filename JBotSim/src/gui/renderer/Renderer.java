package gui.renderer;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

import simulation.Simulator;
import simulation.util.Arguments;
import simulation.util.Factory;

/**
 * Responsible for rendering the simulation. "Rendering" is broadly defined: a
 * renderer may display robots moving around in a virtual world, it can display
 * statistics or it can save the simulation in a file.
 * 
 * @author alc
 *
 */
public abstract class Renderer extends Component {

	protected Simulator simulator;
	protected String titleText = "";

	public Renderer(Arguments args) {
	}

	/**
	 * Draw one frame. Notice that this method may be called several times for
	 * the same simulation step and it may not be called for all simulation
	 * steps.
	 */
	public abstract void drawFrame();

	/**
	 * Dispose of any resources allocated by the renderer.
	 */
	public abstract void dispose();

	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	public abstract void zoomIn();

	public abstract void zoomOut();

	public abstract void resetZoom();

	public void moveLeft() {
	}

	public void moveRight() {
	}

	public void moveUp() {
	}

	public void moveDown() {
	}

	public static Renderer getRenderer(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Renderer 'classname' not defined: " + arguments.toString());

		return (Renderer) Factory.getInstance(arguments.getArgumentAsString("classname"), arguments);
	}

	public void setText(String titleText) {
		this.titleText = titleText;
	}

	protected void drawTitle(Graphics g) {
		if (titleText != null && !titleText.isEmpty()) {
			Font f = g.getFont();
			
//			if (System.getProperty("os.name").contains("Windows")) {
//				Font font = new Font(f.getName(), f.getStyle(), f.getSize() * 2);
//				g.setFont(font);
//			}			

			int x = getWidth() / 2 - g.getFontMetrics().stringWidth(titleText) / 2;
			int y = (int) (g.getFontMetrics().getHeight() * 1.5);

			g.drawString(titleText, x, y);
			g.setFont(f);
		}
	}
}