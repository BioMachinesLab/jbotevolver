package evolutionaryrobotics.evolution.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import evolutionaryrobotics.neuralnetworks.Chromosome;

public class PopulationTable implements Serializable {

	private int size;
	private List<Chromosome> table;
	private int pointer = 0;

	public PopulationTable(int size) {
		this.size = size;
		table = new ArrayList<Chromosome>(size);
		pointer = 0;
	}

	public void add(Chromosome c) {
		if (table.size() < size) {
			table.add(c);
			pointer++;
		} else {
			if (pointer == size) {
				pointer = 0;
			}
			table.set(pointer, c);
			pointer++;
		}
	}

	public List<Chromosome> getTable() {
		return table;
	}
}