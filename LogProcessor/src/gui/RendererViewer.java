package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import gui.renderer.Renderer;

public abstract class RendererViewer extends JFrame {
	private static final long serialVersionUID = -5808128136352356396L;
	private ArrayList<Renderer> renderers;
	private JPanel renderersPanel;
	protected JPanel controlsPanel;

	protected JButton replayButton;
	protected JButton playPauseButton;
	protected Container extrasContainer;

	public RendererViewer(String windowName) {
		super(windowName);

		setLayout(new BorderLayout());

		renderersPanel = new JPanel();
		add(renderersPanel, BorderLayout.CENTER);

		buildControlsPanel();
		add(controlsPanel, BorderLayout.SOUTH);

		extrasContainer = new Container();
		add(extrasContainer, BorderLayout.EAST);

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
		renderersPanel.setLayout(new GridLayout(1, renderers.size()));
		renderersPanel = new JPanel();

		int height = 0, width = 0;
		for (Renderer renderer : renderers) {
			renderersPanel.add(renderer);
			width += renderer.getPreferredSize().getWidth();
			height += renderer.getPreferredSize().getHeight();
		}

		renderersPanel.setSize(width, height);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

		getContentPane().validate();
	}

	@Override
	public void setVisible(boolean b) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

		setVisible(b);
	}

	private void buildControlsPanel() {
		replayButton = new JButton("Replay");
		playPauseButton = new JButton("Pause/Play");
		controlsPanel.add(replayButton);
		controlsPanel.add(playPauseButton);
	}

	public void addPlayButtonListener(ActionListener actionListener) {
		playPauseButton.addActionListener(actionListener);
	}

	public void addReplayButtonListener(ActionListener actionListener) {
		replayButton.addActionListener(actionListener);
	}

	public Container getExtrasContainer() {
		return extrasContainer;
	}

	public void setExtrasContainer(Container extrasContainer) {
		this.extrasContainer = extrasContainer;

		if (extrasContainer != null) {
			updateWindow();
		}
	}
}
