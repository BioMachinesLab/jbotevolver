package evolutionaryrobotics.parallel;

import java.io.Serializable;

public class SlaveData implements Serializable{
	public String startTime 		= "N/A";
	public String endTime   		= "N/A";
	public String slaveStatus 		= "Starting...";
	public int numberOfChromosomesProcessed = 0;
	public double averageTimePerChromosome  = 0.0;
	public double lastChromosomeTime        = 0.0;
	public String slaveAddress 				= "N/A";
	public int slavePort 					= 0;
	public boolean running 					= false;
	public int connectionCount			    = 0;
	public int id;
}
