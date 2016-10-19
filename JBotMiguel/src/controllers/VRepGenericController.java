/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import evaluationfunctions.DummyEvaluationFunction;
import evorbc.qualitymetrics.CircularQualityMetric;
import java.util.ArrayList;
import java.util.Arrays;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import vrep.ControllerFactory;
import vrep.VRepMAPElitesEvolution;
import vrep.VRepNEATController;
import vrep.VRepUtils;

/**
 *
 * @author jorge
 */
public class VRepGenericController extends Controller implements FixedLenghtGenomeEvolvableController {

    protected float[] globalParams;
    protected float[] controllerParams; // the first one should be the controller type
    protected boolean waitForResult;
    protected double[] weights;
    protected float[] params;
    protected Simulator sim;

    public VRepGenericController(Simulator simulator, Robot robot, Arguments args) {
        super(simulator, robot, args);
        Arguments evolutionArgs = simulator.getArguments().get("--evolution");
        globalParams = parseDoubleArray((args.getArgumentIsDefined("globalparams") ? args : evolutionArgs).getArgumentAsString("globalparams"));
        controllerParams = parseDoubleArray((args.getArgumentIsDefined("controllerparams") ? args : evolutionArgs).getArgumentAsString("controllerparams"));
        waitForResult = args.getArgumentAsIntOrSetDefault("waitforresult", 1) == 1;
        this.sim = simulator;    
    }

    public static float[] parseDoubleArray(String str) {
        String[] split = str.split(",");
        float[] res = new float[split.length];
        for (int i = 0; i < split.length; i++) {
            res[i] = Float.parseFloat(split[i].trim());
        }
        return res;
    }

    @Override
    public void setNNWeights(double[] weights) {
        this.weights = weights;

        ArrayList<Float> temp = new ArrayList<>();

        temp.add((float) globalParams.length); // length of fixed parameters
        for (float p : globalParams) {
            temp.add(p);
        }
        temp.add(1f); // number of individuals (1)
        temp.add(1f); // individual id
        temp.add((float) controllerParams.length + 1 + weights.length); // size of the individual (including type)
        for (float p : controllerParams) {
            temp.add(p);
        }
        temp.add(ControllerFactory.WEIGHTS_START_CODE);
        for (double w : weights) {
            temp.add((float) w);
        }

        this.params = new float[temp.size()];
        for (int i = 0; i < params.length; i++) {
            this.params[i] = temp.get(i);
        }
    }

    @Override
    public void controlStep(double time) {
        if(sim.getTime() > 0) { // do just one step (that will do everything)
            return;
        }
        VRepUtils.sendDataToVREPDefault(params);
        System.out.println("Sent to VRep: " + Arrays.toString(params));

        if (waitForResult) {
            float[] data = VRepUtils.getDataFromVREPDefault();
            System.out.println("Received from VRep: " + Arrays.toString(data));

            double fit = parseResult(data);

            if (!sim.getCallbacks().isEmpty() && sim.getCallbacks().get(0) instanceof DummyEvaluationFunction) {
                DummyEvaluationFunction eval = (DummyEvaluationFunction) sim.getCallbacks().get(0);
                eval.setFitness(fit);
            } else {
                System.out.println("Fitness: " + fit);
            }
        }
    }

    protected double parseResult(float[] vals) {
        int index = 0;
        int nResults = (int) vals[index++];
        int id = (int) vals[index++];
        int nVals = (int) vals[index++];

        float fitness = 0;

        if (sim.getArguments().get("--evolution").getArgumentAsString("classname").contains("VRepMAPElitesEvolution")) {
            float x = vals[index++];
            float y = vals[index++];
            float z = vals[index++];
            float orientation = vals[index++];
            float tilt = vals[index++];
            Vector2d pos = new Vector2d(x, y);
            
            String fitnessFun = sim.getArguments().get("--evolution").getArgumentAsString("fitness");
            double maxTilt = sim.getArguments().get("--evolution").getArgumentAsDouble("maxtilt");
            fitness = (float) VRepMAPElitesEvolution.getFitness(fitnessFun, pos, orientation, tilt, maxTilt, true);
            
            robot.setPosition(pos);
            robot.setOrientation(orientation);
        } else {
            fitness = vals[index++];
        }
        return fitness;
    }

    @Override
    public int getGenomeLength() {
        return weights.length;
    }

    @Override
    public double[] getNNWeights() {
        return weights;
    }

    @Override
    public int getNumberOfInputs() {
        return 0;
    }

    @Override
    public int getNumberOfOutputs() {
        return 0;
    }

}
