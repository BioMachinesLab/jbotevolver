package evolutionaryrobotics.evolution.neat;

import java.util.ArrayList;
import java.util.Iterator;

import evolutionaryrobotics.evolution.neat.core.NEATChromosome;
import evolutionaryrobotics.evolution.neat.core.NEATFeatureGene;
import evolutionaryrobotics.evolution.neat.core.NEATLinkGene;
import evolutionaryrobotics.evolution.neat.core.NEATNetDescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATNeuralNet;
import evolutionaryrobotics.evolution.neat.core.NEATNodeGene;
import evolutionaryrobotics.evolution.neat.ga.core.Gene;

/**
 *
 * @author jorge
 */
public class NEATSerializer {
    
    public static final double NODE = 0d, LINK = 1d, FEATURE = 2d;

    public static double[] serialize(NEATNeuralNet net) {
    	NEATNetDescriptor descr = (NEATNetDescriptor) net.netDescriptor();
        NEATChromosome chromo = (NEATChromosome) descr.neatStructure();
        Gene[] genes = chromo.genes();
        
        int length = (genes.length - net.getFeatureGenes().length)*5 + net.getFeatureGenes().length*3;
        double[] res = new double[length];
        
        int i = 0;
        for(Gene gene : genes) {
            if(gene instanceof NEATNodeGene) {
                NEATNodeGene neatGene = (NEATNodeGene) gene;
                res[i++] = NODE;
                res[i++] = (double) neatGene.id();
                res[i++] = neatGene.sigmoidFactor();
                res[i++] = (double) neatGene.getType();
                res[i++] = neatGene.bias();
                
            } else if (gene instanceof NEATLinkGene) {
                NEATLinkGene neatGene = (NEATLinkGene) gene;
                res[i++] = LINK;
                res[i++] = neatGene.isEnabled() ? 1d : 0d;
                res[i++] = (double) neatGene.getFromId();
                res[i++] = (double) neatGene.getToId();
                res[i++] = neatGene.getWeight();
            } else if (gene instanceof NEATFeatureGene) {
            	NEATFeatureGene neatGene = (NEATFeatureGene) gene;
                res[i++] = FEATURE;
                res[i++] = neatGene.geneAsNumber().doubleValue();
                res[i++] = neatGene.getInnovationNumber();
            }
        }
        return res;
    }
    
    public static NEATNeuralNet deserialize(String ser) {
        String[] split = ser.split(",");
        ArrayList<Double> stuff = new ArrayList<Double>();
        for(String s : split) {
            stuff.add(Double.parseDouble(s));
        }
        
        ArrayList<Gene> genes = new ArrayList<Gene>();
        Iterator<Double> iter = stuff.iterator();
        while(iter.hasNext()) {
            double type = iter.next();
            if(type == NODE) {
                int id = (int) (double) iter.next();
                double sigF = iter.next();
                int t = (int) (double) iter.next();
                double bias = iter.next();
                genes.add(new NEATNodeGene(0, id, sigF, t, bias));
            } else if(type == LINK) {
                boolean enabled = iter.next() == 1d;
                int from = (int) (double) iter.next();
                int to = (int) (double) iter.next();
                double weight = iter.next();
                genes.add(new NEATLinkGene(0, enabled, from, to, weight));
            } else if(type == FEATURE) {
                double weight = iter.next();
                int innov = iter.next().intValue();
                genes.add(new NEATFeatureGene(innov, weight));
            }
            
        }
        Gene[] geneArray = new Gene[genes.size()];
        genes.toArray(geneArray);
        NEATChromosome chromo = new NEATChromosome(geneArray);
        NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
        descr.updateStructure(chromo);
        NEATNeuralNet network = new NEATNeuralNet();
        network.createNetStructure(descr);
        network.updateNetStructure();
        return network;
    }
}
