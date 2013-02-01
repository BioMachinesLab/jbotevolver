package evolutionaryrobotics.mains;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.applet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import simulation.Simulator;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import experiments.Experiment;
import factories.EvaluationFunctionFactory;
import factories.ExperimentFactory;
import factories.PopulationFactory;
import gui.Gui;
import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

public class AppletMain extends JApplet implements Gui {

	Arguments experimentArguments = null;
	Arguments environmentArguments = null;
	Arguments robotsArguments = null;
	Arguments controllersArguments = null;

	Arguments populationArguments = null;
	Arguments evaluationArguments = null;
	Arguments guiArguments = null;
	Arguments masterArguments = null;

	DiskStorage diskStorage = new DiskStorage(null);
	String[] commandlineArguments = null;

	Gui gui = null;
	EvaluationFunction evaluationFunction = null;
	Population population;
	Experiment experiment;
	SimRandom simRandom = new SimRandom();
	Simulator simulator = new Simulator(simRandom);

	boolean actAsSlave = false;
	boolean slaveConnectsToMaster = false;
	int slavePort = 8000;
	String masterAddress = null;

	TwoDRenderer renderer;
	long randomSeed = 0;
	Runner r;
	int chosenIndex = 0;
	Graphics bufferGraphics; 
	Image offscreen;
	JPanel painelLateral;

	public AppletMain() throws Exception {

	}

	@Override
	public void init() {
		setSize(400, 400);
		try {
			parseArgs();
			execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Arguments createOrPrependArguments(Arguments previous,
			String newArgumentString) {
		if (newArgumentString.charAt(0) == '+') {
			if (previous != null) {
				return new Arguments(newArgumentString.substring(1,
						newArgumentString.length())
						+ ","
						+ previous.getCompleteArgumentString());
			} else {
				return new Arguments(newArgumentString.substring(1,
						newArgumentString.length()));
			}
		} else {
			return new Arguments(newArgumentString);
		}
	}

	public void parseArgs() throws IOException {
		String[] args = new String[0];

		int currentIndex = 0;

		if (true) {// oops
			String[] argsFromFile = readOptionsFromString(AppletData
					.getConf(chosenIndex));
			String[] newArgs = new String[argsFromFile.length];

			// System.out.println("file: " + argsFromFile.length + " cmd: " +
			// args.length + ", new: " + newArgs.length);

			for (int i = 0; i < argsFromFile.length; i++) {
				newArgs[i] = argsFromFile[i];
			}

			args = newArgs;
		}

		while (currentIndex < args.length) {
			if (currentIndex + 1 == args.length) {
				System.err.println("Error: " + args[currentIndex]
						+ " misses an argument");
				System.exit(-1);
			}

			if (!args[currentIndex].equalsIgnoreCase("--random-seed")
					&& args[currentIndex + 1].charAt(0) == '-') {
				System.err.println("Error: Argument for " + args[currentIndex]
						+ " cannot start with a '-' (and therefore cannot be "
						+ args[currentIndex + 1] + ")");
				System.exit(-1);
			}

			if (args[currentIndex].equalsIgnoreCase("--gui")) {
				guiArguments = new Arguments(args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--experiment")) {
				experimentArguments = createOrPrependArguments(
						experimentArguments, args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--environment")) {
				environmentArguments = createOrPrependArguments(
						environmentArguments, args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--robots")) {
				robotsArguments = createOrPrependArguments(robotsArguments,
						args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--controllers")) {
				controllersArguments = createOrPrependArguments(
						controllersArguments, args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--population")) {
				populationArguments = createOrPrependArguments(
						populationArguments, args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--evaluation")) {
				evaluationArguments = createOrPrependArguments(
						evaluationArguments, args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--random-seed")) {
				// System.out.println("random s: " + args[currentIndex + 1]);
				randomSeed = Long.parseLong(args[currentIndex + 1]);
				simRandom.setSeed(randomSeed);
			} else if (args[currentIndex].equalsIgnoreCase("--output")) {
				diskStorage = new DiskStorage(args[currentIndex + 1]);
			} else if (args[currentIndex].equalsIgnoreCase("--slave")) {
				actAsSlave = true;
				try {
					slavePort = Integer.parseInt(args[currentIndex + 1]);
				} catch (NumberFormatException ne) {
					slaveConnectsToMaster = true;
					masterAddress = args[currentIndex + 1];
				}
			}

			currentIndex += 2;
		}

		commandlineArguments = args;
	}

	public String[] readOptionsFromString(String str) throws IOException {
		BufferedReader bufferedReader = null;
		StringBuffer sb = new StringBuffer();
		String nextLine;

		bufferedReader = new BufferedReader(new StringReader(str));
		while ((nextLine = bufferedReader.readLine()) != null) {
			int index = nextLine.indexOf('#');
			if (index > -1) {
				if (index == 0)
					nextLine = "";
				else
					nextLine = nextLine.substring(0, index - 1);
			}

			sb.append(nextLine + " ");
		}

		String oldString = sb.toString();
		String newString = oldString;
		do {
			oldString = newString;
			newString = oldString.replace("  ", " ");
			newString = newString.replace("\t", " ");
			newString = newString.replace("\n", " ");
			newString = newString.replace(", ", ",");
			newString = newString.replace("( ", "(");
			newString = newString.replace(" )", ")");
		} while (!newString.equals(oldString));

		return newString.split(" ");
	}

	public void createExperimentAndEvaluationFunction() throws Exception {
		experiment = (new ExperimentFactory(simulator)).getExperiment(
				experimentArguments, environmentArguments, robotsArguments,
				controllersArguments/* , BehaviorArguments */);
		simulator.setEnvironment(experiment.getEnvironment());

		if (experiment == null) {
			throw new RuntimeException(
					"No experiment created -- experiment arguments: "
							+ experimentArguments);
		}

		evaluationFunction = null;
		if (evaluationArguments != null) {
			evaluationFunction = (new EvaluationFunctionFactory(simulator))
					.getEvaluationFunction(evaluationArguments, experiment);
		}
	}

	public void execute() throws Exception {

		newExperiment();

		/****************
		 * WINDOW SETUP *
		 ****************/

		this.setSize(750, 500);

		JPanel painel = new JPanel(new BorderLayout());
		painel.setSize(750, 500);

		painelLateral = new JPanel(new GridLayout(3, 1));
		painelLateral.setSize(250, 500);
		String[] data = {
						"A1  (Implicit Comm. & Sync.)",
						"A6  (Task Allocation)",
						"A10 (Task Allocation)",
						"A21 (Implicit Comm.)",
						"B5 (Implicit Comm. & Sync)",
						"B14 (Explicit Comm. & Sync.)",
						"B21 (Synchronization)"
						};
		final JList list = new JList(data);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(200, 80));
		list.setSelectedIndex(0);
		painelLateral.add(listScroller);
		
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				chosenIndex = list.getSelectedIndex();
				if (r != null)
					r.stopSimulation();
				if (chosenIndex >= 0) {
					newExperiment();
					run(simulator, renderer, experiment, evaluationFunction,
							population.getNumberOfStepsPerSample());
				}
			}
		});

		/*JPanel choosePanel = new JPanel();
		JButton chooseButton = new JButton("Choose Run");

		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chosenIndex = list.getSelectedIndex();
				if (r != null)
					r.stopSimulation();
				if (chosenIndex >= 0) {
					newExperiment();
					run(simulator, renderer, experiment, evaluationFunction,
							population.getNumberOfStepsPerSample());
				}
			}
		});*/

		//choosePanel.add(chooseButton);
		//painelLateral.add(choosePanel);
		
		this.getContentPane().setBackground(new Color(232,232,232));
		painelLateral.setBackground(new Color(232,232,232));

		JButton zoomIn = new JButton("Zoom in");
		JButton zoomOut = new JButton("Zoom out");

		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				renderer.zoomIn();
			}
		});
		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				renderer.zoomOut();
			}
		});

		JPanel zoomPanel = new JPanel();
		zoomPanel.add(zoomOut);
		zoomPanel.add(zoomIn);
		JPanel padding = new JPanel();
		padding.setBackground(new Color(232,232,232));
		painelLateral.add(padding);//Just to pad
		painelLateral.add(zoomPanel);
		
		painelLateral.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		zoomPanel.setBackground(new Color(232,232,232));
		//choosePanel.setBackground(new Color(232,232,232));

		painel.add(painelLateral, BorderLayout.EAST);
		//renderer.setSize(530, 500);
		//painel.add(renderer, BorderLayout.CENTER);
		
		offscreen = createImage(this.getSize().width,this.getSize().height); 
		bufferGraphics = offscreen.getGraphics(); 

		this.setContentPane(painel);
		this.validate();
		
		//start experiment when everything is loaded
		if (r != null)
			r.stopSimulation();
		if (chosenIndex >= 0) {
			newExperiment();
			run(simulator, renderer, experiment, evaluationFunction,
					population.getNumberOfStepsPerSample());
		}
		
	}

	public void newExperiment() {

		renderer = null;
		population = null;
		simulator = null;
		simRandom = null;

		try {
			simRandom = new SimRandom();
			simRandom.setSeed(randomSeed);
			simulator = new Simulator(simRandom);
			parseArgs();

			createExperimentAndEvaluationFunction();

			population = new PopulationFactory(simulator).getPopulation(
					populationArguments, experiment.getGenomeLength());

			Chromosome bestChromosome = new Chromosome(
					AppletData.getAlleles(chosenIndex), 1);
			experiment.setChromosome(bestChromosome);

			simulator.setEnvironment(experiment.getEnvironment());

			renderer = new TwoDRenderer(simulator);
			renderer.zoomIn(); renderer.zoomIn();
			renderer.zoomIn(); renderer.zoomIn();
			renderer.zoomIn(); renderer.zoomIn();
			renderer.setSize(530, 500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		if (renderer != null) {
			getContentPane().paint(bufferGraphics);
			renderer.paint(bufferGraphics);
			g.drawImage(offscreen,0,0,this);
			//getContentPane().paint(g);
			//renderer.paint(g);
		}
	}

	@Override
	public void dispose() {
		
	}

	public void stopSimulation() {
		r.stopSimulation();
	}

	@Override
	public void run(Simulator simulator, Renderer renderer,
			Experiment experiment, EvaluationFunction evaluationFunction,
			int maxNumberOfSteps) {

		r = new Runner(simulator, renderer, experiment, evaluationFunction,
				population.getNumberOfStepsPerSample(), this);

		r.start();
	}
}

class Runner extends Thread {

	private Simulator simulator;
	private Renderer renderer;
	private Experiment experiment;
	private EvaluationFunction evaluationFunction;
	private int maxNumberOfSteps;
	private Applet applet;
	private boolean go = true;

	public Runner(Simulator simulator, Renderer renderer,
			Experiment experiment, EvaluationFunction evaluationFunction,
			int maxNumberOfSteps, Applet applet) {
		this.simulator = simulator;
		this.renderer = renderer;
		this.experiment = experiment;
		this.evaluationFunction = evaluationFunction;
		this.maxNumberOfSteps = maxNumberOfSteps;
		this.applet = applet;
	}

	public void stopSimulation() {
		this.go = false;
	}

	@Override
	public void run() {

		int currentStep = 0;
		int renderFrequency = 1;

		try {

			while (!experiment.hasEnded() && currentStep < maxNumberOfSteps
					&& go && simulator != null) {
				// System.out.println(currentStep);
				if (currentStep % renderFrequency == 0) {
					renderer.drawFrame();
					applet.repaint();
					if (renderFrequency < 10) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				simulator.performOneSimulationStep(currentStep);
				if (evaluationFunction != null) {
					evaluationFunction.step();
				}
				currentStep++;
			}

			if (evaluationFunction != null) {
				System.out.println(evaluationFunction.getFitness());
			}
		} catch (Exception e) {
		}
	}
}