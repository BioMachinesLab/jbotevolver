package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class DataViewer extends JFrame {
	private static final long serialVersionUID = -5808128136352356396L;
	protected List<Container> containers=new ArrayList<Container>();
	protected Container centerPanel;
	protected Container controlsPanel;

	protected JButton replayButton;
	protected JButton playPauseButton;

	public DataViewer(String windowName) {
		super(windowName);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildGUI();
	}

	protected void buildGUI() {
		setLayout(new BorderLayout());

		centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);

		controlsPanel = buildControlsPanel();
		add(controlsPanel, BorderLayout.SOUTH);
	}

	public List<Container> getContainers() {
		return containers;
	}

	public void addContainer(Container renderer) {
		containers.add(renderer);
		updateWindow();
	}

	public void removeContainer(Container renderer) {
		containers.remove(renderer);
	}

	public void removeContainer(int index) {
		containers.remove(index);
	}

	public void replaceContainer(int index, Container renderer) {
		containers.set(index, renderer);
	}

	public void updateWindow() {
		centerPanel.setLayout(new GridLayout(1, containers.size()));
		centerPanel = new JPanel();

		int height = 0, width = 0;
		for (Container renderer : containers) {
			centerPanel.add(renderer);
			width += renderer.getPreferredSize().getWidth();
			height += renderer.getPreferredSize().getHeight();
		}

		centerPanel.setSize(width, height);
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

	protected Container buildControlsPanel() {
		replayButton = new JButton("Replay");
		playPauseButton = new JButton("Pause/Play");

		JPanel controlsPanel = new JPanel(new FlowLayout());
		controlsPanel.add(replayButton);
		controlsPanel.add(playPauseButton);

		return controlsPanel;
	}
}
