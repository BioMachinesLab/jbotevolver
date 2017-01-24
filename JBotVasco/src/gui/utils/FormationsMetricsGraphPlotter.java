package gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import evolutionaryrobotics.FormationTaskMetricsCodex;
import evolutionaryrobotics.FormationTaskMetricsData;
import gui.util.Graph;
import gui.util.GraphPlotter;

public class FormationsMetricsGraphPlotter extends GraphPlotter {
	private static final long serialVersionUID = -7417290618569517026L;
	private RequestMetricsParamsPane panel = null;
	private Graph graph;
	private String[] files;
	private MetricsType metricsType;

	public enum MetricsType {
		TIME_INSIDE_FORMATION, TIME_TO_FIRST_OCCUPATION, PERMUTATION_METRICS, REOCUPATION_TIME;
	}

	public FormationsMetricsGraphPlotter(String[] files, MetricsType metricsType) {
		super();
		this.files = files;
		this.metricsType = metricsType;

		JPanel mainPanel = new JPanel(new BorderLayout());
		getContentPane().add(mainPanel);

		graph = new Graph();
		mainPanel.add(graph, BorderLayout.CENTER);

		panel = new RequestMetricsParamsPane();

		if (panel.triggerDialog() == JOptionPane.OK_OPTION) {
			int totalGenerations = 0;
			for (String filePath : files) {
				File file = new File(filePath);
				ArrayList<FormationTaskMetricsData> metricsData = FormationTaskMetricsCodex.decodeMetricsDataFile(file);
				totalGenerations = Math.max(metricsData.size(), totalGenerations);

				loadMetricsDataOnGraph(graph, metricsData, file, metricsType);
			}

			graph.setxLabel("Generations (" + (totalGenerations) + ")");
			switch (metricsType) {
			case TIME_INSIDE_FORMATION:
				graph.setyLabel("Robots time in spot");
				break;
			case TIME_TO_FIRST_OCCUPATION:
				graph.setyLabel("Time to first full occupation");
				break;
			case PERMUTATION_METRICS:
				graph.setyLabel("Different spots occupied per robot");
				break;
			case REOCUPATION_TIME:
				graph.setyLabel("Time until faulty robot spot is occupied");
				break;
			}

			graph.setShowLast(totalGenerations);

			setSize(800, 600 + graph.getHeaderSize());
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);
		}

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		JButton saveToImageButton = new JButton("Save graph as image");
		saveToImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (saveImage()) {
					JOptionPane.showMessageDialog(null, "Saved image!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Error saving image!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonsPanel.add(saveToImageButton);

		JButton saveToFileButton = new JButton("Save graph data");
		saveToFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (saveData()) {
					JOptionPane.showMessageDialog(null, "Saved data!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Error saving data!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonsPanel.add(saveToFileButton);

		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
	}

	private void loadMetricsDataOnGraph(Graph graph, ArrayList<FormationTaskMetricsData> metricsData, File file,
			MetricsType type) {
		// Instead of differentiate, just create three arrays and use only the
		// Needed ones
		metricsData.sort(new Comparator<FormationTaskMetricsData>() {
			@Override
			public int compare(FormationTaskMetricsData o1, FormationTaskMetricsData o2) {
				if (o1.getGeneration() < o2.getGeneration()) {
					return -1;
				} else {
					if (o1.getGeneration() > o2.getGeneration()) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});

		Double[] dataList_0 = new Double[metricsData.size()];
		Double[] dataList_1 = new Double[metricsData.size()];
		Double[] dataList_2 = new Double[metricsData.size()];

		for (int i = 0; i < metricsData.size(); i++) {
			FormationTaskMetricsData data = metricsData.get(i);
			switch (type) {
			case TIME_INSIDE_FORMATION:
				dataList_0[i] = data.getTimeInside_min();
				dataList_1[i] = data.getTimeInside_avg();
				dataList_2[i] = data.getTimeInside_max();
				break;
			case TIME_TO_FIRST_OCCUPATION:
				dataList_0[i] = data.getTimeFirstTotalOccup_min();
				dataList_1[i] = data.getTimeFirstTotalOccup_avg();
				dataList_2[i] = data.getTimeFirstTotalOccup_max();
				break;
			case PERMUTATION_METRICS:
				dataList_0[i] = data.getNumberDiffSpotsOccupied_min();
				dataList_1[i] = data.getNumberDiffSpotsOccupied_avg();
				dataList_2[i] = data.getNumberDiffSpotsOccupied_max();
				break;
			case REOCUPATION_TIME:
				dataList_0[i] = data.getReocupationTime_min();
				dataList_1[i] = data.getReocupationTime_avg();
				dataList_2[i] = data.getReocupationTime_max();
				break;
			}
		}

		String name = file.getParentFile().getParentFile().getName();
		name += File.separatorChar + file.getParentFile().getName();
		name += File.separatorChar + file.getName();
		name += "_" + type.name();

		if (panel.getDialogResult() == JOptionPane.OK_OPTION) {
			if (panel.isPlotMinimumSelected()) {
				graph.addDataList(dataList_0);
				graph.addLegend(name + "_min");
			}

			if (panel.isPlotAverageSelected()) {
				graph.addDataList(dataList_1);
				graph.addLegend(name + "_avg");
			}

			if (panel.isPlotMaximumSelected()) {
				graph.addDataList(dataList_2);
				graph.addLegend(name + "_max");
			}
		}
	}

	private class RequestMetricsParamsPane {
		private JCheckBox minimumCheckBox = null;
		private JCheckBox averageCheckBox = null;
		private JCheckBox maximumCheckBox = null;
		private int dialogResult = 0;

		public int triggerDialog() {
			minimumCheckBox = new JCheckBox("Minimum");
			averageCheckBox = new JCheckBox("Average");
			averageCheckBox.setSelected(true);
			maximumCheckBox = new JCheckBox("Maximum");

			String message = "Select fields to plot:";
			Object[] params = { message, maximumCheckBox, averageCheckBox, minimumCheckBox };
			dialogResult = JOptionPane.showConfirmDialog(null, params, "Plot parameters", JOptionPane.YES_NO_OPTION);
			return dialogResult;
		}

		public boolean isPlotMinimumSelected() {
			return minimumCheckBox != null && minimumCheckBox.isSelected();
		}

		public boolean isPlotAverageSelected() {
			return averageCheckBox != null && averageCheckBox.isSelected();
		}

		public boolean isPlotMaximumSelected() {
			return maximumCheckBox != null && maximumCheckBox.isSelected();
		}

		public int getDialogResult() {
			return dialogResult;
		}
	}

	private boolean saveImage() {
		return saveGraphAsImage(graph, files);
	}

	private boolean saveData() {
		return saveGraphDataToFile(graph, files, metricsType.toString(), true);
	}

	@Override
	protected boolean saveGraphAsImage(Graph graph, String[] files) {
		try {
			if (files != null && files.length > 0) {
				Rectangle originalBounds = getBounds();
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				setBounds(0, 0, screen.width, screen.height);
				validate();
				repaint();
				graph.enableMouseLines(false);

				BufferedImage img = new BufferedImage(graph.getWidth(), graph.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = img.createGraphics();
				graph.printAll(g2d);
				g2d.dispose();

				graph.enableMouseLines(true);
				setBounds(originalBounds);

				File f = new File(files[0]);
				String name = "";

				if (files.length == 1) {
					name = f.getParentFile().getParentFile().getName() + "_" + f.getParentFile().getName() + "_"
							+ metricsType.toString() + "." + SAVE_TO_DATA_EXTENSION;
				} else {
					name = f.getParentFile().getParentFile().getName() + "_" + metricsType.toString() + "."
							+ SAVE_TO_DATA_EXTENSION;
				}

				if (!new File(PLOTS_IMAGES_FOLDER_PATH).exists()) {
					new File(PLOTS_IMAGES_FOLDER_PATH).mkdir();
				}

				ImageIO.write(img, SAVE_TO_DATA_EXTENSION, new File(PLOTS_IMAGES_FOLDER_PATH, name));
			}
		} catch (IOException e) {
			System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
			return false;
		}

		return true;
	}
}
