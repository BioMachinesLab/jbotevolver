package vrep;

import taskexecutor.VRepTaskExecutor;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATPopulation;

public class VRepNEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithmWrapper {

    protected float[] globalParams;
    protected float[] controllerParams;

    public VRepNEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo) {
        super(descriptor, evo);
    }

    public void setParameters(float[] globalParams, float[] controlPar) {
        this.globalParams = globalParams;
        this.controllerParams = controlPar;
    }

    @Override
    protected void evaluatePopulation(Chromosome[] genotypes) {
        evolutionaryrobotics.neuralnetworks.Chromosome[] chromosomes = new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];
        for (int i = 0; i < chromosomes.length; i++) {
            chromosomes[i] = ((NEATPopulation) evo.getPopulation()).convertChromosome(genotypes[i], i);
        }
        float[][] packet = VRepUtils.createDataPacket(chromosomes, controllerParams);

        float fixedParams[] = new float[globalParams.length + 1];
        fixedParams[0] = globalParams.length;
        System.arraycopy(globalParams, 0, fixedParams, 1, globalParams.length);

        int nTasks = VRepUtils.sendTasks((VRepTaskExecutor) evo.getTaskExecutor(), fixedParams, packet);
        float[][] results = VRepUtils.receiveTasks((VRepTaskExecutor) evo.getTaskExecutor(), nTasks);
        
        for (float[] vals : results) {
            int index = 0;
            int nResults = (int) vals[index++];
            for (int res = 0; res < nResults; res++) {
                int id = (int) vals[index++];
                int nVals = (int) vals[index++];
                float fitness = vals[index++];
                fitness += 10;
                evo.getPopulation().setEvaluationResult(chromosomes[id], fitness);
                genotypes[id].updateFitness(fitness);
            }
        }
    }
}
