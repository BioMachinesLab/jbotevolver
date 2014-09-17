import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import neat.continuous.ERNEATContinuousNetwork;
import neat.continuous.NEATContinuousNetwork;
import neat.continuous.NEATContinuousNeuronGene;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;


public class NEATTEst {
	
	public static void main(String[] args) {
		
		//0 B (not used)
		//1 I  (1-4)
		//3 O
		//4 H  (4-3)
		
		List<NEATLink> list = new ArrayList<NEATLink>();
		
		list.add(new NEATLink(1, 3, -3.3549357832289295));
		list.add(new NEATLink(3, 2, -2.3484578414079236));
		list.add(new NEATLink(3, 3, -0.1598904751499151));
		
		
		ActivationFunction[] af = new ActivationFunction[4];
		
		for(int i = 0 ; i < af.length ; i++) {
			af[i] = new ActivationSigmoid();
		}
		
		List<NEATNeuronGene> neurons = new LinkedList<NEATNeuronGene>();
		
		neurons.add(new NEATNeuronGene(NEATNeuronType.Bias, new ActivationSigmoid(), 0, 0));
		neurons.add(new NEATNeuronGene(NEATNeuronType.Input, new ActivationSigmoid(), 1, 1));
		neurons.add(new NEATNeuronGene(NEATNeuronType.Output, new ActivationSigmoid(), 2, 2));
		neurons.add(new NEATContinuousNeuronGene(NEATNeuronType.Hidden, new ActivationSigmoid(), 3, 3, 0.15682641189774638, -8.016385371818051E-4));
		
		//TODO check how big the decay can be! change in initial pop and mutations
		
		NEATContinuousNetwork nOriginal = new NEATContinuousNetwork(1, 1, list, af, neurons);
		
		ERNEATContinuousNetwork jbotNetwork = new ERNEATContinuousNetwork(nOriginal);
		
		for(int i = 0 ; i < 50 ; i++) {
			
			MLData input = new BasicMLData(1);
			
			if(i >= 30) {
				input.add(0, 0);
			} else
				input.add(0,0.8535898384267142);
			
			double[] o = jbotNetwork.propagateInputs(input.getData());
			System.out.print(i+" "+o[0]);
		}
	}

}
