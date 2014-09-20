import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import neat.continuous.NEATContinuousCODEC;
import neat.continuous.NEATContinuousGenome;
import neat.continuous.NEATContinuousNetwork;
import neat.continuous.NEATContinuousNeuronGene;
import neat.layerered.LayeredANN;
import neat.layerered.continuous.LayeredContinuousNEATCODEC;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;


public class NEATTEst {
	
	public static void main(String[] args) {
		
		//0 B (not used)
		//1 I  (1-4)
		//3 O
		//4 H  (4-3)
		
		List<NEATLinkGene> list = new ArrayList<NEATLinkGene>();
		
		list.add(new NEATLinkGene(1, 3, true, 1, -3.3549357832289295));
		list.add(new NEATLinkGene(3, 2, true, 2,-2.3484578414079236));
		list.add(new NEATLinkGene(3, 3, true, 2,-0.1598904751499151));
		
		
		ActivationFunction[] af = new ActivationFunction[4];
		
		for(int i = 0 ; i < af.length ; i++) {
			af[i] = new ActivationSigmoid();
		}
		
		List<NEATNeuronGene> neurons = new LinkedList<NEATNeuronGene>();
		
		neurons.add(new NEATNeuronGene(NEATNeuronType.Bias, new ActivationSigmoid(), 0, 0));
		neurons.add(new NEATNeuronGene(NEATNeuronType.Input, new ActivationSigmoid(), 1, 1));
		neurons.add(new NEATNeuronGene(NEATNeuronType.Output, new ActivationSigmoid(), 2, 2));
		neurons.add(new NEATContinuousNeuronGene(NEATNeuronType.Hidden, new ActivationSigmoid(), 3, 3, 0.15682641189774638, -8.016385371818051E-4));
		
		NEATContinuousGenome g = new NEATContinuousGenome(neurons, list, 1, 1);
		
		NEATContinuousCODEC neatcodec = new NEATContinuousCODEC();
		LayeredContinuousNEATCODEC cod = new LayeredContinuousNEATCODEC();
		
		LayeredANN layered = (LayeredANN)cod.decode(g);
		NEATContinuousNetwork continuous = (NEATContinuousNetwork)neatcodec.decode(g);
		
//		ERNEATContinuousNetwork jbotNetwork = new ERNEATContinuousNetwork(nOriginal);
		
		for(int i = 0 ; i < 50 ; i++) {
			
			MLData input = new BasicMLData(1);
			
			if(i >= 30) {
				input.add(0, 0);
			} else
				input.add(0,0.8535898384267142);
			
			double[] o = continuous.compute(input.getData());
			System.out.println(i+" "+o[0]);
		}
	}

}
