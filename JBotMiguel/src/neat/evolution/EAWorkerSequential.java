package neat.evolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import neat.evaluation.EvaluationResult;
import org.encog.EncogError;
import org.encog.ml.ea.exception.EARuntimeError;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.EvolutionaryOperator;
import org.encog.ml.ea.species.Species;

/**
 * A worker thread for an Evolutionary Algorithm.
 */
public class EAWorkerSequential {

	private final Random rnd;

	private final BasicEAJBot train;
	
	private HashMap<Species,Integer> speciesMap = new HashMap<Species, Integer>();
	private ArrayList<Species> order = new ArrayList<Species>();
	private ArrayList<GenomeEvaluation> results = new ArrayList<GenomeEvaluation>();
	private boolean stopExecuting = false;
	
	public EAWorkerSequential(final BasicEAJBot theTrain, Random r) {
		this.train = theTrain;
		this.rnd = r;
	}

	private Genome chooseParent(Species species) {
		final int idx = this.train.getSelection().performSelection(this.rnd, species);
		return species.getMembers().get(idx);
	}
	
	public void addSpecies(Species s, int children) {
		speciesMap.put(s,children);
		order.add(s);
	}
	
	public void run() {
		for(Species s : order) {
			
			if(stopExecuting)
				break;
			
			int children = speciesMap.get(s);
			submitEvaluationSamples(s, children);
		}
		
		getResults();
	}
	
	public void stopExecuting() {
		stopExecuting = true;
	}
	
	private void getResults() {
		
		int resultsLeft = results.size();
		
		while(resultsLeft-- > 0 && !stopExecuting) {
			EvaluationResult r = train.getEvaluationResult();
			
			boolean success = false;
			
			for(GenomeEvaluation g : results) {
				if(r.getEvalId() == g.getGenome().hashCode()) {
					g.setResult(r);
					g.getGenome().setScore(r.getFitness());
					g.getGenome().setAdjustedScore(r.getFitness());
					success = true;
					break;
				}
			}
			
			if(!success) {
				throw new RuntimeException("Didnt find the correct genome! hashcode issue, probably! "+r.getEvalId());
			}
		}
		for(GenomeEvaluation ge : results) {
			this.train.addChild(ge.getGenome());
		}
	}
	
	private void submitEvaluationSamples(Species species, int necessaryChildren) {
		for(int i = 0 ; i < necessaryChildren ; i++) {
			boolean success = false;
			int tries = this.train.getMaxOperationErrors();
			Genome children[] = new Genome[this.train.getOperators().maxOffspring()];
			Genome parents[] = new Genome[train.getOperators().maxParents()];
			do {
				try {
					// choose an evolutionary operation (i.e. crossover or a type of
					// mutation) to use
					final EvolutionaryOperator opp = this.train.getOperators()
							.pickMaxParents(this.rnd,
									species.getMembers().size());
	
					children[0] = null;
					
					// prepare for either sexual or asexual reproduction either way,
					// we
					// need at least
					// one parent, which is the first parent.
					//
					// Chose the first parent, there must be at least one genome in
					// this
					// species
					parents[0] = chooseParent(species);
					
					// if the number of individuals in this species is only
					// one then we can only clone and perhaps mutate, otherwise use
					// the crossover probability to determine if we are to use
					// sexual reproduction.
					if (opp.parentsNeeded() > 1) {
						int numAttempts = 5;
	
						parents[1] = chooseParent(species);
						while (parents[0] == parents[1]
								&& numAttempts-- > 0) {
							parents[1] = chooseParent(species);
						}
	
						// success, perform crossover
						if (parents[0] != parents[1]) {
							opp.performOperation(this.rnd, parents, 0,
									children, 0);
						}
					} else {
						
						// clone a child (asexual reproduction)
						opp.performOperation(this.rnd, parents, 0,
								children, 0);
						
						
						children[0].setPopulation(parents[0]
								.getPopulation());
						
					}
					// process the new child
					for (Genome child : children) {
						if (child != null) {
							child.setPopulation(parents[0].getPopulation());
							if (this.train.getRules().isValid(child)) {
								child.setBirthGeneration(this.train.getIteration());
								this.train.submitEvaluation(child,child.hashCode());
								
								GenomeEvaluation e = new GenomeEvaluation(child);
								results.add(e);
								
								success = true;
								break;
							}
						}
					}
				} catch (EARuntimeError e) {
					e.printStackTrace();
					tries--;
					if (tries < 0) {
						throw new EncogError(
								"Could not perform a successful genetic operaton after "
										+ this.train.getMaxOperationErrors()
										+ " tries.");
					}
				} catch (final Throwable t) {
					t.printStackTrace();
					if (!this.train.getShouldIgnoreExceptions()) {
						this.train.reportError(t);
					}
				}
				
			} while (!success);
		}
	}
	
	class GenomeEvaluation {
		
		private Genome genome;
		private EvaluationResult result;
		
		public GenomeEvaluation(Genome g) {
			this.genome = g;
		}
		
		public void setResult(EvaluationResult result) {
			this.result = result;
		}
		
		public Genome getGenome() {
			return genome;
		}
		
		public EvaluationResult getResult() {
			return result;
		}
	}
}