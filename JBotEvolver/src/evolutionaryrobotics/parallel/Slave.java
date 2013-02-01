package evolutionaryrobotics.parallel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import simulation.Simulator;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import experiments.Experiment;
import factories.EvaluationFunctionFactory;
import factories.ExperimentFactory;
import gui.BatchGui;
import gui.Gui;
import gui.ScreenSaverGui;

public class Slave {
	String      masterAddress;
	PrintStream out;
	boolean     keepRunning      = true;
	Socket      socket           = null;
	boolean     screenSaverMode  = false;
	private int masterPort       = 0;
	Gui         gui               = null;
	Simulator   simulator 		 = new Simulator(new SimRandom());

	public Slave(int port, PrintStream out, boolean screenSaverMode) throws Exception {
		this.out = out;
		this.screenSaverMode = screenSaverMode; 
		ServerSocket serverSocket = null;
		Socket       socket = null;
		try {
			serverSocket = new ServerSocket(port);
			out.println("Slave  opened server socket on port: " + port);
			while (true) {
				socket = serverSocket.accept();
				masterAddress = socket.getInetAddress().toString();
				masterAddress = masterAddress.substring(1, masterAddress.length());
				out.println("Connection from master: " + socket.toString());
				keepRunning = true;
				serve(socket);
			}							
		} finally {
			if (serverSocket != null) 
				serverSocket.close();
		}
	}

	public Slave(String master, int masterPort, PrintStream out, boolean screenSaverMode) throws Exception {
		if (masterPort == 0) {
			this.masterPort = Master.DEFAULTMASTERSLAVEPORT;
		} else {
			this.masterPort = masterPort;
		};
		
		this.out             = out;
		this.screenSaverMode = screenSaverMode;

		if (screenSaverMode) {
			gui = new ScreenSaverGui();
		}
		
		masterAddress = master;
		out.println("Trying to connect to: " + master + " on port " + this.masterPort);
		try {
			InetAddress address = InetAddress.getByName(master);
			keepRunning = true;
			Socket socket = new Socket(address, this.masterPort);
			serve(socket);
		} catch (IOException e) {
			e.printStackTrace(out);
		}
	}

	public void serve(Socket socket) throws Exception {
		Experiment experiment = null;
		this.socket = socket;
		//socket.setSoTimeout(120000);

		try {
			socket.setKeepAlive(true);
			ObjectOutputStream socketOut = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream  socketIn  = new ObjectInputStream(socket.getInputStream());

			socketOut.writeObject(Master.MASTERVERSION);
			Boolean masterAcceptedVersion = (Boolean) socketIn.readObject();
			if (!masterAcceptedVersion.booleanValue()) { 
				System.out.println("Wrong slave version -- probably too old!");
				System.exit(0);
			}

			if (!screenSaverMode)
				gui = new BatchGui();

			int numberOfChromosomeEvaluated = 0;
			long time1= System.currentTimeMillis();
			while (keepRunning) {
//				out.println("Time: " + (System.currentTimeMillis() - time1));
//				time1 = System.currentTimeMillis();
				Boolean moreWork = (Boolean) socketIn.readObject();
				if (moreWork.booleanValue()) {
					// First, we get the experiment: 
					Arguments experimentArguments  = (Arguments) socketIn.readObject(); 
					Arguments environmentArguments = (Arguments) socketIn.readObject();
					Arguments robotArguments       = (Arguments) socketIn.readObject();
					Arguments controllerArguments  = (Arguments) socketIn.readObject(); 


					Arguments evaluationArguments = (Arguments) socketIn.readObject();

					Long randomSeed = (Long) socketIn.readObject();

					Chromosome chromosome = (Chromosome) socketIn.readObject();
					Integer samplesPerIndividual = (Integer) socketIn.readObject();
					Integer stepsPerRun          = (Integer) socketIn.readObject();

					long startTime = System.currentTimeMillis();

					out.print((numberOfChromosomeEvaluated++) + ": Experiment: " + experimentArguments.getArgumentAsString("name") + " (chrom: " + chromosome.getID() +", samples: " + samplesPerIndividual.intValue() + " of " + stepsPerRun.intValue() + " steps) ...");
					out.flush();

					double fitness = 0;
					long   tempSeed = randomSeed; 


					for (int i = 0; i < samplesPerIndividual.intValue(); i++) {					
						// Make sure that random seed is controlled and the same for all individuals at the beginning of each trial:
						experimentArguments.setArgument("fitnesssample", i);
						environmentArguments.setArgument("fitnesssample", i);
						environmentArguments.setArgument("totalsamples", samplesPerIndividual.intValue());
//						System.out.print("  S:"+tempSeed);
						simulator.getRandom().setSeed(tempSeed);
						tempSeed = simulator.getRandom().nextLong();
						experiment = (new ExperimentFactory(simulator)).getExperiment(experimentArguments, environmentArguments, robotArguments, controllerArguments);
						experiment.setChromosome(chromosome);
						simulator.setEnvironment(experiment.getEnvironment());
						EvaluationFunction evaluationFunction = (new EvaluationFunctionFactory(simulator)).getEvaluationFunction(evaluationArguments, experiment);
						gui.run(simulator, null, experiment, evaluationFunction, stepsPerRun.intValue());
						fitness += evaluationFunction.getFitness();
//						System.out.println("FITNESS SAMPLE "+i+": "+evaluationFunction.getFitness());
					}
					
					double time = System.currentTimeMillis() - startTime;
					time /= 1000;
					fitness /= samplesPerIndividual.doubleValue();
					
					out.printf("fitness obtained: %f -- time: %5.2f\n", fitness, time);

					socketOut.writeDouble(fitness);
					socketOut.reset();	
				} else {
					keepRunning = false;
				}					 
			}
		} catch (Exception e) {
			e.printStackTrace(out);

			try {
				socket.close();
			} catch (Exception e1) {}
		}
		
		out.println("Closing connection");
		socket.close();		

		if (gui != null) {
			gui.dispose();
		}
	} 

	public void end() {
		keepRunning = false;
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
