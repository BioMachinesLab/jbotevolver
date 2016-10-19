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
    protected double maxTilt = 9999;
    protected int genomeLength = 0;
    protected String fitness;

    public VRepMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
        super(jBotEvolver, taskExecutor, arg);

        if (!arg.getArgumentIsDefined("genomelength")) {
            throw new RuntimeException("Missing 'genomelength' arg in VREPMapElitesEvolution");
        }
        this.genomeLength = arg.getArgumentAsInt("genomelength");

        globalParams = parseDoubleArray(arg.getArgumentAsString("globalparams"));
        controllerParams = parseDoubleArray(arg.getArgumentAsString("controllerparams"));
        
        fitness = arg.getArgumentAsString("fitness");

        maxTilt = arg.getArgumentAsDoubleOrSetDefault("maxtilt", maxTilt);
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

                    MOChromosome c = null;
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
                    float tilt = vals[index++];
                    Vector2d pos = new Vector2d(posX, posY);

                    double fit = getFitness(fitness, pos, orientation, tilt, maxTilt, false);
                    FitnessResult fr = new FitnessResult(fit);
                    VectorBehaviourExtraResult br = new VectorBehaviourExtraResult(orientation, pos.getX(),  pos.getY());

                    ArrayList<EvaluationResult> sampleResults = new ArrayList<>();
                    sampleResults.add(fr);
                    sampleResults.add(br);

                    ExpandedFitness ef = new ExpandedFitness();
                    ef.setEvaluationResults(sampleResults);
                    c.setEvaluationResult(ef);

                    if (tilt < maxTilt) {
                        ((MAPElitesPopulation) population).addToMap(c);
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
    
    public static double getFitness(String mode, Vector2d pos, double ori, double tilt, double maxTilt, boolean debug) {
        double f = 0;
        if(mode.contains("circular")) {
            double t = CircularQualityMetric.calculateOrientationFitness(pos, ori);
            if(debug) {
                System.out.println("Circular: " + t);
            }
            f += t;
        }
        if(mode.contains("tilt")) {
            double t = 1 - Math.min(1, (double) tilt / maxTilt);
            if(debug) {
                System.out.println("Tilt: " + t + " (" + tilt + "/" + maxTilt + ")");
            }
            f+= t;
        }
        return f;
    }

}
