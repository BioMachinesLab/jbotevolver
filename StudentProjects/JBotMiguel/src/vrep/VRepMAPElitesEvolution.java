package vrep;

import static controllers.VRepGenericController.parseDoubleArray;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import mathutils.Vector2d;
import multiobjective.GenericEvaluationFunction;
import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import novelty.BehaviourResult;
import novelty.EvaluationResult;
import novelty.ExpandedFitness;
import novelty.FitnessResult;
import novelty.evaluators.FinalPositionWithOrientationBehavior;
import novelty.results.VectorBehaviourExtraResult;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.VRepTaskExecutor;
import evolution.MAPElitesEvolution;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evorbc.qualitymetrics.CircularQualityMetric;

/**
 * MAP-Elites "illumination" algorithm J.-B. Mouret and J. Clune, Illuminating
 * search spaces by mapping elites, arXiv 2015
 *
 * @author miguelduarte
 */
public class VRepMAPElitesEvolution extends MAPElitesEvolution {

    // Expected: time | tiltkill | stoptime (optional)
    protected float[] globalParams;
    // Expected: 0 (controller type)
    protected float[] controllerParams;

    public int excluded = 0;
    protected double feasibilityThreshold = 0.0;
    protected int genomeLength = 0;

    public VRepMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
        super(jBotEvolver, taskExecutor, arg);

        if (!arg.getArgumentIsDefined("genomelength")) {
            throw new RuntimeException("Missing 'genomelength' arg in VREPMapElitesEvolution");
        }
        this.genomeLength = arg.getArgumentAsInt("genomelength");

        globalParams = parseDoubleArray(arg.getArgumentAsString("globalparams"));
        controllerParams = parseDoubleArray(arg.getArgumentAsString("controllerparams"));

        feasibilityThreshold = arg.getArgumentAsDoubleOrSetDefault("feasibility", feasibilityThreshold);
    }

    public static double getFitness(MOChromosome moc) {
        ExpandedFitness fit = (ExpandedFitness) moc.getEvaluationResult();
        BehaviourResult br = (BehaviourResult) fit.getCorrespondingEvaluation(1);

        double[] behavior = (double[]) br.value();
        Vector2d pos = new Vector2d(behavior[0], behavior[1]);
        double orientation = ((VectorBehaviourExtraResult) br).getExtraValue();

        double fitness = CircularQualityMetric.calculateOrientationFitness(pos, orientation);

        return fitness;
    }

    @Override
    protected void evaluateAndAdd(ArrayList<MOChromosome> randomChromosomes) {

        try {
            Chromosome[] chromosomes = new Chromosome[randomChromosomes.size()];

            for (int i = 0; i < chromosomes.length; i++) {
                chromosomes[i] = randomChromosomes.get(i);
            }

            float fixedParams[] = new float[globalParams.length + 1];
            fixedParams[0] = globalParams.length;
            System.arraycopy(globalParams, 0, fixedParams, 1, globalParams.length);

            float[][] packet = VRepUtils.createDataPacket(chromosomes, controllerParams);

            int nTasks = VRepUtils.sendTasks((VRepTaskExecutor) taskExecutor, fixedParams, packet);
            float[][] results = VRepUtils.receiveTasks((VRepTaskExecutor) taskExecutor, nTasks);

            for (int t = 0; t < results.length; t++) {
                float[] vals = results[t];
                int index = 0;
                int nResults = (int) vals[index++];
                for (int r = 0; r < nResults; r++) {
                    int id = (int) vals[index++];

                    Chromosome c = null;
                    for (int ch = 0; ch < randomChromosomes.size(); ch++) {
                        if (randomChromosomes.get(ch).getID() == id) {
                            c = randomChromosomes.get(ch);
                            break;
                        }
                    }

                    int nVals = (int) vals[index++];

                    //positions of robot
                    float posX = vals[index++];
                    float posY = vals[index++];
                    float posZ = vals[index++];
                    double orientation = vals[index++]; // [-PI,PI]
                    float distanceTraveled = vals[index++];
                    float feasibility = vals[index++];

                    Vector2d pos = new Vector2d(posX, posY);

                    double fitness = CircularQualityMetric.calculateOrientationFitness(pos, orientation);

                    FitnessResult fr = new FitnessResult(fitness);
                    GenericEvaluationFunction br = new FinalPositionWithOrientationBehavior(new Arguments(""), pos, orientation);

                    ArrayList<EvaluationResult> sampleResults = new ArrayList<>();
                    int sampleIndex = 0;

                    sampleResults.add(sampleIndex++, fr);
                    sampleResults.add(sampleIndex++, ((GenericEvaluationFunction) br).getEvaluationResult());

                    ExpandedFitness ef = new ExpandedFitness();
                    ef.setEvaluationResults(sampleResults);

                    MOFitnessResult result = new MOFitnessResult(r, (MOChromosome) c, ef);
                    MOChromosome moc = result.getChromosome();

                    moc.setEvaluationResult(result.getEvaluationResult());

                    if (feasibility > this.feasibilityThreshold) {
                        ((MAPElitesPopulation) population).addToMap(moc);
                    } else {
                        excluded++;
                    }
                }
            }
            System.out.println("Total excluded so far: " + excluded);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

}
