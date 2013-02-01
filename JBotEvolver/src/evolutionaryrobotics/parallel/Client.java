package evolutionaryrobotics.parallel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class Client {

	private int numberOfChromosomesLeft;
	private long randomSeed;
	private long startTime;
	private int totalNumberOfEvaluationsNecessary;
	private Population population;
	private DiskStorage diskStorage;
	private Arguments evaluationArguments;
	private Arguments controllerArguments;
	private Arguments experimentArguments;
	private Arguments environmentArguments;
	private Arguments robotArguments;
	private int masterPort;
	private String masterAddress;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private int priority;
	
	private Simulator simulator;

	public Client(Simulator simulator, int priority, String masterAddress, int masterPort, 
			Arguments experimentArguments, Arguments environmentArguments, Arguments robotArguments, 
			Arguments controllerArguments, Population population, Arguments evaluationArguments, 
			DiskStorage diskStorage) {

		this.simulator = simulator;
		this.priority = priority;
		this.masterAddress = masterAddress;
		numberOfChromosomesLeft = population.getPopulationSize();
		this.population         = population;
		this.diskStorage        = diskStorage;
		this.experimentArguments  = experimentArguments;
		this.environmentArguments = environmentArguments;
		this.robotArguments       = robotArguments;
		this.controllerArguments  = controllerArguments;
		this.evaluationArguments  = evaluationArguments;

		if (population.getPopulationSize() - population.getNumberOfChromosomesEvaluated() == 0) {
			population.createNextGeneration();
		}

		startTime = System.currentTimeMillis();


		if (masterPort == 0) {
			this.masterPort = MultiMaster.DEFAULTMASTERSLAVEPORT;
		} else {
			this.masterPort = masterPort;
		};


	}


	public void execute() {

		while(!population.evolutionDone()){

			try {
				totalNumberOfEvaluationsNecessary = (population.getNumberOfGenerations() - population.getNumberOfCurrentGeneration()) * population.getPopulationSize();
				System.out.println("Trying to connect to: " + masterAddress + " on port " + this.masterPort);
				System.out.println(totalNumberOfEvaluationsNecessary + "= ("+population.getNumberOfGenerations() +"-"+ population.getNumberOfCurrentGeneration()+") *" + population.getPopulationSize());

				InetAddress address = InetAddress.getByName(masterAddress);
				socket = new Socket(address, this.masterPort);
				in  = new ObjectInputStream(socket.getInputStream());
				out = new ObjectOutputStream(socket.getOutputStream());


				socket.setKeepAlive(true);
				socket.setSoTimeout(0);

				out.writeObject(Master.MASTERVERSION);
				Boolean masterAcceptedVersion = (Boolean) in.readObject();
				if (!masterAcceptedVersion.booleanValue()) { 
					System.out.println("Wrong client version -- probably too old!");
					System.exit(0);
				}

				out.writeObject(new Integer(totalNumberOfEvaluationsNecessary));

				//send priority
				out.writeObject(new Integer(priority));

				//send arguments
				out.writeObject(experimentArguments);
				out.writeObject(environmentArguments);
				out.writeObject(robotArguments);
				out.writeObject(controllerArguments);
				out.writeObject(evaluationArguments);
				
				randomSeed = simulator.getRandom().nextLong();

				while(!population.evolutionDone()){
					System.out.println(this.toString() + ": Sending generation " + population.getNumberOfCurrentGeneration());
					out.writeObject(population);
					//out.writeObject(new Long(randomSeed));
					out.reset();
					population = (Population) in.readObject();
					System.out.println(this.toString() + ": Received fitness for generation " + population.getNumberOfCurrentGeneration());		
					diskStorage.savePopulation(population,simulator.getRandom());
					System.out.println("Seed: " + population.getGenerationRandomSeed() + ", aver fit:"+ population.getAverageFitness() );
					simulator.getRandom().setSeed(population.getGenerationRandomSeed());
					population.createNextGeneration();
				}

				socket.close();

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Client reconnecting!");
			}
		}
		System.out.println(this.toString() + ": Done");
	}

}
