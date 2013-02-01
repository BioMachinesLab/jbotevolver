package factories;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;

import javax.print.attribute.standard.Finishings;

import simulation.Simulator;
import simulation.util.Arguments;

import evolutionaryrobotics.populations.MuLambdaPopulation;
import evolutionaryrobotics.populations.Population;

public class PopulationFactory extends Factory implements Serializable {
	
	public PopulationFactory(Simulator simulator) {
		super(simulator);
		// TODO Auto-generated constructor stub
	}

	public Population getPopulation(Arguments arguments, int genomeLength)
			throws Exception {
		if (!arguments.getArgumentIsDefined("name")
				&& !arguments.getArgumentIsDefined("load")) {
			throw new Exception("Population 'name' or 'load' not defined: "
					+ arguments.toString());
		}

		Population population;

		if (arguments.getArgumentIsDefined("name")) {
			String populationName = arguments.getArgumentAsString("name");

			if (populationName.equalsIgnoreCase("mulambda")) {
				population = new MuLambdaPopulation(simulator, genomeLength,
						arguments);

			} else {
				throw new Exception("Population 'name' unknown: "
						+ populationName);
			}
		} else if (arguments.getArgumentIsDefined("load")) {
			
			File f = new File(arguments.getArgumentAsString("load"));
			
			File populationFile = new File(arguments.getArgumentAsString("parentfolder")+"/populations/"+f.getName());
			
			if(!populationFile.exists())
				populationFile = new File(arguments.getArgumentAsString("parentfolder")+"/../populations/"+f.getName());
			
			FileInputStream fis = new FileInputStream(populationFile);
			GZIPInputStream gzipIn = new GZIPInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(gzipIn);
			population = (Population) in.readObject();
			in.close();
		} else {
			throw new RuntimeException(
					"For the --population argument, supply a name=... or a load=... argument");
		}

		if (population != null) {
			population.parseArguments(arguments);
		}

		population.setGenerationRandomSeed(simulator.getRandom().nextInt());

		return population;
	}

	public Population getCoevolvedPopulation(Arguments arguments,
			int genomeLength, String team) throws Exception {
		Population population;

		if (arguments.getArgumentIsDefined("name")) {
			String populationName = arguments.getArgumentAsString("name");

			if (populationName.equalsIgnoreCase("mulambda")) {
				population = new MuLambdaPopulation(simulator, genomeLength,
						arguments);

			} else {
				throw new Exception("Population 'name' unknown: "
						+ populationName);
			}
		} else {
			String load = "load"+team;
			if (arguments.getArgumentIsDefined(load)) {
				File f = new File(arguments.getArgumentAsString(load));
				
				File populationFile = new File(arguments.getArgumentAsString("parentfolder")+"/populations/"+f.getName());
				
				if(!populationFile.exists())
					populationFile = new File(arguments.getArgumentAsString("parentfolder")+"/../populations/"+f.getName());
				
				FileInputStream fis = new FileInputStream(populationFile);
				
				GZIPInputStream gzipIn = new GZIPInputStream(fis);
				ObjectInputStream in = new ObjectInputStream(gzipIn);
				population = (Population) in.readObject();
				in.close();

			} else {
				throw new RuntimeException(
						"For the --population argument, supply a name=... or a loadA/B=... argument");
			}
		}
		if (population != null) {
			population.parseArguments(arguments);
		}

		population.setGenerationRandomSeed(simulator.getRandom().nextInt());

		return population;
	}
}
