package logprocessing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import gui.renderer.Renderer;

public class RendererViewer extends JFrame {
	private static final long serialVersionUID = -4468147780385822977L;

	protected ArrayList<Renderer> renderers;
	private JPanel mainPanel;
	private JPanel controlsPanel;
	private Dimension rendererSize = new Dimension(400, 400);

	private JButton replayButton;
	private JButton playPauseButton;

	public RendererViewer(String windowName) {
		super(windowName);

		setLayout(new BorderLayout());

		mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER);

		buildControlsPanel();
		add(controlsPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public ArrayList<Renderer> getRenderers() {
		return renderers;
	}

	public void addRenderer(Renderer renderer) {
		renderers.add(renderer);
		updateWindow();
	}

	public void removeRenderer(Renderer renderer) {
		renderers.remove(renderer);
	}

	public void removeRenderer(int index) {
		renderers.remove(index);
	}

	public void replaceRenderer(int index, Renderer renderer) {
		renderers.set(index, renderer);
	}

	public void updateWindow() {
		mainPanel.setLayout(new GridLayout(1, renderers.size()));
		mainPanel = new JPanel();

		for (Renderer renderer : renderers) {
			mainPanel.add(renderer);
		}

		mainPanel.setSize(rendererSize.width * renderers.size(), rendererSize.height);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	}

	@Override
	public void setVisible(boolean b) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		super.setVisible(b);
	}

	private void buildControlsPanel() {
		replayButton = new JButton("Replay");
		playPauseButton = new JButton("Pause/Play");
		controlsPanel.add(replayButton);
		controlsPanel.add(playPauseButton);
	}

	public void setRendererDimension(Dimension rendererSize) {
		this.rendererSize = rendererSize;
	}

	public void addPlayButtonListener(ActionListener actionListener) {
		playPauseButton.addActionListener(actionListener);
	}

	public void addReplayButtonListener(ActionListener actionListener) {
		replayButton.addActionListener(actionListener);
	}
}
