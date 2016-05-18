package controllers;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.remoteApi;
import evaluationfunctions.DummyEvaluationFunction;

public class HexapodShowbestController extends Controller implements FixedLenghtGenomeEvolvableController{

	private remoteApi vrep;
	private int clientId;
	protected float[] parameters;
	protected boolean init = false;
	protected double[] weights;
	protected int genomeLength;
	protected int inputs;
	protected int outputs;
	protected int controllerType = 1;//1=RepertoireNEAT, 2=NEAT, 3=Dummy
	protected Simulator sim;
	protected String ip = "127.0.0.1";
	
	public HexapodShowbestController(Simulator simulator,Robot robot,Arguments args) {
		super(simulator, robot, args);
		
		if(!args.getArgumentIsDefined("inputs") || !args.getArgumentIsDefined("outputs"))
			throw new RuntimeException("Argument 'inputs' not defined for class HexapodShowbestController!");
		
		ip = args.getArgumentAsStringOrSetDefault("ip", ip);
		
		controllerType= args.getArgumentAsIntOrSetDefault("controllertype", controllerType);
		
		if(args.getArgumentIsDefined("weights")) {
			String[] rawArray = args.getArgumentAsString("weights").split(",");
			double[] weights = new double[rawArray.length];
			for(int i = 0 ; i < weights.length ; i++)
				weights[i] = Double.parseDouble(rawArray[i]);
			setNNWeights(weights);
		}
		this.sim = simulator;
	}
	
	public void init() {
		vrep = new remoteApi();
		vrep.simxFinish(-1); // just in case, close all opened connections
		clientId = vrep.simxStart(ip,19996,true,false,5000,5);
	}
	
	@Override
	public void end() {
//		vrep.simxStopSimulation(clientId,vrep.simx_opmode_blocking);
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!init) {
			init();
			init = true;
			System.out.println("Sent!");
			sendDataToVREP(parameters);
			float[] data = getDataFromVREP();
			double fit = getFitness(data);
			DummyEvaluationFunction eval = (DummyEvaluationFunction)sim.getCallbacks().get(0);
			eval.setFitness(fit);
			System.out.println("Received!");
		}
	}
	
	protected double getFitness(float[] vals) {
		int index = 0;
		
		int nResults = (int)vals[index++];
		
		//id
		int id = (int)vals[index++];
		//number of values
		int nVals = (int)vals[index++];
		//fitness
		float fitness = vals[index++];

		return fitness;
	}
	
	@Override
	public void setNNWeights(double[] weights) {
		this.weights = weights;
		this.genomeLength = weights.length;
		
		int parametersIndex = 0;
		
		this.parameters = new float[6+weights.length];
		
		this.parameters[parametersIndex++] = 1;//1 fixed parameters
		this.parameters[parametersIndex++] = 40;//15 seconds
		this.parameters[parametersIndex++] = 1;//1 individual
		this.parameters[parametersIndex++] = 1;//id
		this.parameters[parametersIndex++] = genomeLength+1;//size of type+genome
		this.parameters[parametersIndex++] = controllerType;
		
		for(int i = 0 ; i < weights.length ; i++)
			this.parameters[parametersIndex++] = (float)weights[i];
		
//		switch (controllerType) {
//		case 1:
//			VRepRepertoireController c1 = new VRepRepertoireController(parameters);
//			inputs = 0;
//			outputs = c1.getNumberOfParameters();
//			break;
//		case 2:
//			VRepNEATController c2 = new VRepNEATController(parameters);
//			this.inputs = c2.getNumberOfInputs();
//			this.inputs = c2.getNumberOfInputs();
//			break;
//		}
	}

	@Override
	public int getGenomeLength() {
		return genomeLength;
	}

	@Override
	public double[] getNNWeights() {
		return weights;
	}

	@Override
	public int getNumberOfInputs() {
		return inputs;
	}

	@Override
	public int getNumberOfOutputs() {
		return getGenomeLength();
	}
	
	protected float[] getDataFromVREP() {
    	CharWA str=new CharWA(0);
    	while(vrep.simxGetStringSignal(clientId,"toClient",str,remoteApi.simx_opmode_oneshot_wait) != remoteApi.simx_return_ok) {
    		try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    	
		vrep.simxClearStringSignal(clientId, "toClient", remoteApi.simx_opmode_oneshot);
		 
		FloatWA f = new FloatWA(0);
		f.initArrayFromCharArray(str.getArray());
		return f.getArray();
    }
    
    protected void sendDataToVREP(float[] arr) {
    	FloatWA f = new FloatWA(arr.length);
    	f.setValue(arr);
    	char[] chars = f.getCharArrayFromArray();
    	String tempStr = new String(chars);
		CharWA str = new CharWA(tempStr);
		vrep.simxWriteStringStream(clientId,"fromClient",str,remoteApi.simx_opmode_oneshot);
    }
}
