package main.logprocessing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import gui.renderer.Renderer;

public class DoubleFitnessViewer extends JFrame {
	private static final long serialVersionUID = 7255389709069384290L;
	private Renderer rendererLeft;
	private Renderer rendererRight;
	private CompareFitnessField plot;

	private JButton replayButton;
	private JButton pausePlayButton;

	public DoubleFitnessViewer() {
		super("Position Plot");
		setLayout(new BorderLayout());
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

		JPanel centerPanel = new JPanel(new GridLayout(1, 2));
		centerPanel.add(rendererLeft);
		centerPanel.add(rendererRight);
		add(centerPanel,BorderLayout.CENTER);
		
		replayButton = new JButton("Replay");
		replayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plot != null)
					plot.interrupt();
				plot = new CompareFitnessField(t);
				plot.start();
			}
		});

		pausePlayButton = new JButton("Pause/Play");
		pausePlayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (plot != null)
					plot.pause();
			}
		});

		JPanel southPanel = new JPanel();
		southPanel.add(pausePlayButton);
		southPanel.add(pausePlayButton);
		add(southPanel, BorderLayout.SOUTH);
		
		setVisible(true);
	}

	public void setRendererLeft(Renderer renderer) {
		if (this.rendererLeft != null) {
			remove(this.rendererLeft);
		}
		this.rendererLeft = renderer;

		add(renderer);
	}

	public void setRendererRight(Renderer renderer) {
		if (this.rendererRight != null) {
			remove(this.rendererRight);
		}
		this.rendererRight = renderer;

		add(renderer);
	}

}
