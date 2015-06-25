/*
 * Created on 23-Jun-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package evolutionaryrobotics.evolution.neat.core;

import java.util.Collection;

import evolutionaryrobotics.evolution.neat.core.control.NEAT;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.evolution.neat.nn.core.Learnable;
import evolutionaryrobotics.evolution.neat.nn.core.NeuralNetDescriptor;
import evolutionaryrobotics.evolution.neat.nn.core.NeuralNetLayerDescriptor;
import evolutionaryrobotics.evolution.neat.nn.core.NeuralNetType;

/**
 * @author MSimmerson
 *
 * Descriptor for a NEAT neural net.
 */
public class NEATNetDescriptor implements NeuralNetDescriptor {
	private int inputSize;
	private Learnable learnable;
	private Chromosome structure;
	
	public NEATNetDescriptor(int inputSize, Learnable learnable) {
		this.inputSize = inputSize;
		this.learnable = learnable;
	}
	
	public void updateStructure(Chromosome structure) {
		this.structure = structure;
	}
	
	
	public Chromosome neatStructure() {
		return (this.structure);
	}
	
	public void addLayerDescriptor(NeuralNetLayerDescriptor descriptor) {
		throw new UnsupportedOperationException("addLayerDescriptor not used in NEAT");
	}

	public NeuralNetType neuralNetType() {
		return (new NEAT());
	}

	public int numInputs() {
		return (this.inputSize);
	}

	public Collection layerDescriptors() {
		throw new UnsupportedOperationException("layerDescriptors not used in NEAT");
	}

	public Learnable learnable() {
		return (this.learnable);
	}

	public boolean isRecurrent() {
		return false;
	}
}
