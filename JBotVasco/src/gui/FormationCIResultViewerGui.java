package gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.tree.TreePath;

import evolutionaryrobotics.JBotEvolver;
import gui.util.SpringUtilities;
import gui.utils.MetricsGraphPlotter;
import gui.utils.MetricsGraphPlotter.MetricsType;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;

public class FormationCIResultViewerGui extends CIResultViewerGui {
	private static final long serialVersionUID = -272519649519979276L;
	private JButton timeInsideRobotMetricsButton;
	private JButton timeUntilFirstOccupationButton;
	private JButton permutationMetricButton;
	private JButton reocupationTimeButton;

	public FormationCIResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}

	@Override
	protected void launchGraphPlotter(JBotEvolver jbot, Simulator sim) {
		new CIGraphPlotter(jbot, sim);
	}

	@Override
	protected Container initRightWrapperPanel() {

		Container panel = super.initRightWrapperPanel();

		if (enableDebugOptions) {
			// Metrics panel
			JPanel metricsPanel = new JPanel(new GridLayout(4, 1));
			metricsPanel.setBorder(BorderFactory.createTitledBorder("Metrics"));

			timeInsideRobotMetricsButton = new JButton("Time inside spots");
			timeUntilFirstOccupationButton = new JButton("Time until first occupation");
			permutationMetricButton = new JButton("Occupied spots count");
			reocupationTimeButton = new JButton("Reocupation time");

			metricsPanel.add(timeInsideRobotMetricsButton);
			metricsPanel.add(timeUntilFirstOccupationButton);
			metricsPanel.add(permutationMetricButton);
			metricsPanel.add(reocupationTimeButton);

			debugOptions.add(metricsPanel);

			SpringUtilities.makeGrid(debugOptions, 4, 1, // rows, cols
					0, 0, // initialX, initialY
					10, 10);
		}

		// @vasco: 1200 Pixels is hardcoded value and 300 came from super
		// Class
		final int HEIGHT = 1200;
		final int WIDTH = 300;
		if (panel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) panel).getViewport();
			((JPanel) viewport.getView()).setPreferredSize(new Dimension(WIDTH, HEIGHT));

			return panel;
		} else {
			panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			return new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		}
	}

	@Override
	protected void initListeners() {
		super.initListeners();

		if (enableDebugOptions) {
			timeInsideRobotMetricsButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.TIME_INSIDE_FORMATION);
				}
			});

			timeUntilFirstOccupationButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.TIME_TO_FIRST_OCCUPATION);
				}
			});

			permutationMetricButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.PERMUTATION_METRICS);
				}
			});

			reocupationTimeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.REOCUPATION_TIME);
				}
			});
		}
	}

	private void plotGraph(MetricsType type) {
		TreePath[] selectedFiles = fileTree.getSelectedFilesPaths();
		ArrayList<String> paths = new ArrayList<String>();

		// List all the selected folders and files
		if (selectedFiles == null) {
			JOptionPane.showMessageDialog(this, "No folders or files selected!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			String parentpath = "";
			for (TreePath treePath : selectedFiles) {
				if (treePath.getParentPath() == null) {
					paths.add(treePath.getLastPathComponent().toString());
				} else {
					if (parentpath.equals("")) {
						parentpath = treePath.getParentPath().toString().replace("[", "");
						parentpath = parentpath.replace("]", "");

						if (parentpath.endsWith(".")) {
							parentpath = parentpath.substring(0, parentpath.lastIndexOf("."));
						}
					}

					if (System.getProperty("os.name").contains("Windows")) {
						paths.add(parentpath + File.separatorChar + treePath.getLastPathComponent());
					} else {
						paths.add(parentpath + "/" + treePath.getLastPathComponent());
					}
				}
			}
		}

		// Join all the selected folders and files paths
		final String[] mainFolders = new String[paths.size()];
		for (int i = 0; i < paths.size(); i++) {
			File file = new File(paths.get(i));
			mainFolders[i] = file.isDirectory() ? file.getAbsolutePath() : file.getParent();
		}

		Thread t = new Thread(new GraphTread(mainFolders, type));
		t.start();
	}

	protected class GraphTread implements Runnable {
		private String[] folders;
		private MetricsType type;

		public GraphTread(String[] folders, MetricsType type) {
			this.folders = folders;
			this.type = type;
		}

		@Override
		public void run() {
			// Get a list of all the metrics files
			ArrayList<String> files = new ArrayList<String>();
			for (String folder : folders) {
				String[] fs = getMetricsFiles(folder).split("###");

				for (String str : fs) {
					if (!str.isEmpty()) {
						files.add(str);
					}
				}
			}

			if (files.isEmpty()) {
				System.out.println("Empty!");
			}

			if (files != null && !files.isEmpty()) {
				new MetricsGraphPlotter(files.toArray(new String[files.size()]), type);
			} else {
				JOptionPane.showMessageDialog(null, "No files to compare!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		/*
		 * Recursively look for a file in the supplied folder
		 */
		private String getMetricsFiles(String folder) {
			File f = new File(folder + "/_metrics.log");

			try {
				if (f.exists()) {
					return f.getAbsolutePath();
				} else {
					if (folder == null) {
						JOptionPane.showMessageDialog(null, "No folders selected!", "Error", JOptionPane.ERROR_MESSAGE);
						return null;
					} else {
						String[] directories = (new File(folder)).list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return (new File(dir, name)).isDirectory();
							}
						});
						String result = "";
						if (directories != null) {
							for (String dir : directories) {
								String dirResult = getMetricsFiles(folder + "/" + dir);
								if (!dirResult.isEmpty())
									result += dirResult + "###";
							}
						}

						return result;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
	}
}
