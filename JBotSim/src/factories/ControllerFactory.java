package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;
import simulation.Controller;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.MultiPreyPickerActuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.actuators.RobotColorActuator;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.behaviors.OpenDoorBehavior;
import simulation.robot.sensors.BehaviorSensor;
import simulation.robot.sensors.EpuckLightSensor;
import simulation.robot.sensors.WallButtonSensor;
import simulation.robot.sensors.XRayPreySensor;
import simulation.robot.sensors.CompassSensor;
import simulation.robot.sensors.DoubleParameterSensor;
import simulation.robot.sensors.EnergySensor;
import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.GroundRGBColorSensor;
import simulation.robot.sensors.InNestSensor;
import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.NearRobotSensor;
import simulation.robot.sensors.PerimeterSimpleLightTypeSensor;
import simulation.robot.sensors.PerimeterSimpleRobotColorSensor;
import simulation.robot.sensors.PositionSensor;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.PreySensor;
import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.RobotRGBColorSensor;
import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimpleLightTypeSensor;
import simulation.robot.sensors.SimpleRobotColorSensor;
import simulation.util.Arguments;
import controllers.KeyboardController;
import controllers.ProgrammedForager;
import controllers.ProgrammedMazeSolver;
import controllers.RobotFollower;
import evolutionaryrobotics.neuralnetworks.BehaviorController;
import evolutionaryrobotics.neuralnetworks.CTRNNMultilayer;
import evolutionaryrobotics.neuralnetworks.MulitlayerPerceptron;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.neuralnetworks.inputs.BehaviorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.CompassNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.DoubleParameterNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.EnergyNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.EpuckIRNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.GroundRGBColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.InNestNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.LightTypeNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.MultiPreyCarriedNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.inputs.NearRobotNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.PositionNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.PreyCarriedNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.RobotColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.RobotRGBColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SimpleLightTypeNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SimpleRobotColorNNInput;
import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;
import evolutionaryrobotics.neuralnetworks.outputs.BehaviorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.FixedNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.MultiNeuronRobotColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.MultiPreyPickerNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.OpenDoorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.PreyPickerNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.RobotColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.RobotRGBColorNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SimpleNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SimplePreyPickerNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SysoutNNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.TwoWheelNNOutput;

public class ControllerFactory extends Factory implements Serializable {
	int chromosomeLength = 0;

	public ControllerFactory(Simulator simulator) {
		super(simulator);
	}

	public Controller getController(Robot robot, Arguments arguments) {
		
		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Controller 'name' not defined: "+ arguments.toString());
		}

		String controllerName = arguments.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(controllerName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				return (Controller) constructor.newInstance(simulator,arguments);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 

		throw new RuntimeException("Unknown controller: " + controllerName);

//		if (!arguments.getArgumentIsDefined("name")) {
//			throw new RuntimeException("Controller 'name' not defined: "
//					+ arguments.toString());
//		}
//
//		String controllerName = arguments.getArgumentAsString("name");
//		if (controllerName.equalsIgnoreCase("MultilayerPerceptron")) {
//			Vector<NNInput> inputs = getNNInputs(robot, arguments);
//			Vector<NNOutput> outputs = getNNOutputs(robot, arguments);
//
//			controller = new NeuralNetworkController(simulator, robot,
//					new MulitlayerPerceptron(inputs, outputs, arguments));
//			chromosomeLength = ((NeuralNetworkController) controller)
//					.getRequiredNumberOfWeights();
//		} else if (controllerName.equalsIgnoreCase("CTRNNMultilayer")) {
//			int numberOfHiddenNodes = arguments
//					.getArgumentIsDefined("hiddennodes") ? arguments
//					.getArgumentAsInt("hiddennodes") : 5;
//
//			Vector<NNInput> inputs = getNNInputs(robot, arguments);
//			Vector<NNOutput> outputs = getNNOutputs(robot, arguments);
//
//			controller = new NeuralNetworkController(simulator, robot,
//					new CTRNNMultilayer(inputs, outputs, arguments));
//			
//			if(arguments.getArgumentIsDefined("weights")) {
//				String[] rawArray = arguments.getArgumentAsString("weights").split(",");
//				double[] weights = new double[rawArray.length];
//				for(int i = 0 ; i < weights.length ; i++)
//					weights[i] = Double.parseDouble(rawArray[i]);
//				((NeuralNetworkController) controller).setNNWeights(weights);
//			}
//			
//			chromosomeLength = ((NeuralNetworkController) controller)
//					.getRequiredNumberOfWeights();
//		}else if (controllerName.equalsIgnoreCase("ProgrammedForger")) {
//
//			controller = new ProgrammedForager(simulator, robot, arguments);
//
//		} else if (controllerName.equalsIgnoreCase("ProgrammedMazeSolver")) {
//
//			controller = new ProgrammedMazeSolver(simulator, robot, arguments);
//
//		} else if (controllerName.equalsIgnoreCase("controllers.KeyboardController")) {
//			controller = new KeyboardController(simulator, robot);
//		} else if (controllerName.equalsIgnoreCase("Behavior")) {
//			controller = new BehaviorController(simulator, robot,arguments);
//			chromosomeLength = ((NeuralNetworkController) controller.getEvolvingController())
//			.getRequiredNumberOfWeights();
//		} else if (controllerName.equalsIgnoreCase("robotfollower")) {
//			controller = new RobotFollower(simulator, robot, arguments);
//		} else if (controllerName.equalsIgnoreCase("preprogrammed")) {
//			
//			String behaviorType = arguments.getArgumentAsString("type");
//			
//			controller = null;
//			
//			if(behaviorType.equals("opendoor")) {
//				controller = new OpenDoorBehavior(simulator, robot, arguments);
//			}
//		} else {
//			throw new RuntimeException("Unknown controller name: "
//					+ controllerName);
//		}

	}

	public Vector<NNInput> getNNInputs(Robot robot, Arguments arguments) {

		Arguments inputs = new Arguments(
				arguments.getArgumentAsString("inputs"));

		if (inputs.getNumberOfArguments() == 1) {
			if (inputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
				return getInputsAutomatically(robot);
			}
		}

		Vector<NNInput> nnInputs = new Vector<NNInput>();

		for (int i = 0; i < inputs.getNumberOfArguments(); i++) {
			NNInput nnInput = createInput(robot, inputs.getArgumentAt(i),
					new Arguments(inputs.getValueAt(i)));
			nnInputs.add(nnInput);
		}
		return nnInputs;
	}

	protected Vector<NNInput> getInputsAutomatically(Robot robot) {
		Vector<NNInput> nnInputs = new Vector<NNInput>();
		Iterator<Sensor> i = robot.getSensors().iterator();
		
		while (i.hasNext()) {
			Sensor sensor = i.next();
			
			if (sensor.getClass().equals(LightTypeSensor.class)) {
				nnInputs.add(createInput(robot, "LightType", new Arguments(
						"id=" + sensor.getId() + ",name=lighttype")));
			} else if (sensor.getClass().equals(EpuckLightSensor.class)) {
				nnInputs.add(createInput(robot, "EpuckLight", new Arguments(
						"id=" + sensor.getId() + ",name=epucklight")));
			} else if (sensor.getClass().equals(PreySensor.class)) {
				nnInputs.add(createInput(robot, "Prey", new Arguments("id="
						+ sensor.getId() + ",name=prey")));
			} else if (sensor.getClass().equals(SimpleLightTypeSensor.class)) {
				nnInputs.add(createInput(robot, "SimpleLightType",
						new Arguments("id=" + sensor.getId()
								+ ",name=simplelighttype")));
			} else if (sensor.getClass().equals(XRayPreySensor.class)) {
				nnInputs.add(createInput(robot, "SimpleLightType",
						new Arguments("id=" + sensor.getId()
								+ ",name=simplelighttype")));
			} else if (sensor.getClass().equals(
					PerimeterSimpleLightTypeSensor.class)) {
				nnInputs.add(createInput(robot, "SimpleLightType",
						new Arguments("id=" + sensor.getId()
								+ ",name=simplelighttype")));
			} else if (sensor.getClass().equals(PreyCarriedSensor.class)) {
				nnInputs.add(createInput(robot, "PreyCarried", new Arguments(
						"id=" + sensor.getId() + ",name=preycarried")));
			} else if (sensor.getClass().equals(InNestSensor.class)) {
				nnInputs.add(createInput(robot, "InNest", new Arguments("id="
						+ sensor.getId() + ",name=innest")));
			} else if (sensor.getClass().equals(RobotColorSensor.class)) {
				nnInputs.add(createInput(robot, "RobotColor", new Arguments(
						"id=" + sensor.getId() + ",name=robotcolorsensor"))); 
			} else if (sensor.getClass().equals(RobotRGBColorSensor.class)) {
				nnInputs.add(createInput(robot, "RobotRGBColor", new Arguments(
						"id=" + sensor.getId() + ",name=robotcolorsensor")));
			} else if (sensor.getClass().equals(SimpleRobotColorSensor.class)) {
				nnInputs.add(createInput(robot, "SimpleRobotColor",
						new Arguments("id=" + sensor.getId()
								+ ",name=simplerobotcolorsensor")));
			} else if (sensor.getClass().equals(
					PerimeterSimpleRobotColorSensor.class)) {
				nnInputs.add(createInput(robot, "SimpleRobotColor",
						new Arguments("id=" + sensor.getId()
								+ ",name=simplerobotcolorsensor")));
			} else if (sensor.getClass().equals(NearRobotSensor.class)) {
				nnInputs.add(createInput(robot, "NearRobot", new Arguments(
						"id=" + sensor.getId() + ",name=nearrobotsensor")));
			} else if (sensor.getClass().equals(CompassSensor.class)) {
				nnInputs.add(createInput(robot, "Compass", new Arguments("id="
						+ sensor.getId() + ",name=compasssensor")));
			}else if(sensor.getClass().equals(EnergySensor.class)) {
				nnInputs.add(createInput(robot, "Energy", new Arguments("id=" + sensor.getId() + ",name=energysensor")));
			}else if(sensor.getClass().equals(PositionSensor.class)) {
				nnInputs.add(createInput(robot, "Position", new Arguments("id=" + sensor.getId() + ",name=positionsensor")));
			}else if(sensor.getClass().equals(DoubleParameterSensor.class)) {
				nnInputs.add(createInput(robot, "DoubleParameter", new Arguments("id=" + sensor.getId() + ",name=doubleparametersensor")));
			}else if(sensor.getClass().equals(GroundRGBColorSensor.class)){
				nnInputs.add(createInput(robot,"GroundRGBColorSensor", new Arguments("id=" +sensor.getId() + ",name=groundrgbcolorsensor")));
			} else if(sensor.getClass().equals(BehaviorSensor.class)){
				nnInputs.add(createInput(robot,"BehaviorSensor", new Arguments("id=" +sensor.getId() + ",name=behaviorsensor")));
			} else if(sensor.getClass().equals(EpuckIRSensor.class)){
				nnInputs.add(createInput(robot,"EPuckIRSensor", new Arguments("id=" +sensor.getId() + ",name=epuckirsensor")));
			} else if(sensor.getClass().equals(WallButtonSensor.class)){
				nnInputs.add(createInput(robot,"LightType", new Arguments("id=" +sensor.getId() + ",name=lighttypesensor")));
			} else {
				throw new RuntimeException(
						"Trying to automatically create input for sensor: "
								+ sensor
								+ ", but sensor unknown here -- sensor.class: "
								+ sensor.getClass() + ", LightType.class: "
								+ LightTypeSensor.class);
			}
		}

		return nnInputs;
	}

	public NNInput createInput(Robot robot, String name, Arguments arguments) {
		int id = 0;
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");

		if (name.equalsIgnoreCase("lighttype")
				|| name.equalsIgnoreCase("lighttypeinput") ||
				name.equalsIgnoreCase("epucklight")
				|| name.equalsIgnoreCase("epucklightinput")||
				name.equalsIgnoreCase("prey")
				|| name.equalsIgnoreCase("preyinput") ||
				name.equalsIgnoreCase("wallbutton") || 
				name.equalsIgnoreCase("wallbuttoninput") ) {
			return new LightTypeNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("simplelighttype")
				|| name.equalsIgnoreCase("simplelighttypeinput")) {
			return new SimpleLightTypeNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("robotcolor")
				|| name.equalsIgnoreCase("robotcolorinput")) {
			return new RobotColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("robotrgbcolor")
				|| name.equalsIgnoreCase("robotrgbcolorinput")) {
			return new RobotRGBColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("simplerobotcolor")
				|| name.equalsIgnoreCase("simplerobotcolorinput")) {
			return new SimpleRobotColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("preycarried")
				|| name.equalsIgnoreCase("preycarriedinput")) {
			return new PreyCarriedNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("MultyPreyCarriedSensor")) {
			return new MultiPreyCarriedNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("innest")
				|| name.equalsIgnoreCase("innestinput")) {
			return new InNestNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("nearrobot")
				|| name.equalsIgnoreCase("nearrobotinput")) {
			return new NearRobotNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("compass")) {
			return new CompassNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("position")) {
			return new PositionNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("doubleparameter")) {
			return new DoubleParameterNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("energy")) {
			return new EnergyNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("monitor") || name.equalsIgnoreCase("monitorinput")) {
			String new_name=arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(arguments.getArgumentAsString(new_name));				
			return new SysoutNNInput(createInput(robot, new_name, actuators));
		} else if (name.equalsIgnoreCase("groundrgbcolor") || name.equalsIgnoreCase("groundrgbcolorsensor")) {
			return new GroundRGBColorNNInput(robot.getSensorWithId(id));
		} else if (name.equalsIgnoreCase("behavior") || name.equalsIgnoreCase("behaviorsensor")) {
			return new BehaviorNNInput(robot.getSensorWithId(id), robot);
		} else if (name.equalsIgnoreCase("epuckir") || name.equalsIgnoreCase("epuckirsensor")) {
			return new EpuckIRNNInput(robot.getSensorWithId(id));
		} else
			throw new RuntimeException("Unknown nn input: " + name);
	}

	public Vector<NNOutput> getNNOutputs(Robot robot, Arguments arguments) {
		Arguments outputs = new Arguments(
				arguments.getArgumentAsString("outputs"));

		if (outputs.getNumberOfArguments() == 1) {
			if (outputs.getArgumentAt(0).equalsIgnoreCase("auto")) {
				return getOutputsAutomatically(robot);
			}
		}

		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();

		for (int i = 0; i < outputs.getNumberOfArguments(); i++) {
			NNOutput nnOutput = createOutput(robot, outputs.getArgumentAt(i),
					new Arguments(outputs.getValueAt(i)));
			nnOutputs.add(nnOutput);
		}

		return nnOutputs;
	}

	protected Vector<NNOutput> getOutputsAutomatically(Robot robot) {
		Vector<NNOutput> nnOutputs = new Vector<NNOutput>();
		Iterator<Actuator> i = robot.getActuators().iterator();

		while (i.hasNext()) {
			Actuator actuator = i.next();
			if (actuator.getClass().equals(TwoWheelActuator.class)) {
				nnOutputs.add(createOutput(robot, "TwoWheel", new Arguments("id=" + actuator.getId() + ",name=twowheel")));
			} else if (actuator.getClass().equals(RobotColorActuator.class)) {
				nnOutputs.add(createOutput(robot, "RobotColor", new Arguments(
						"id=" + actuator.getId() + ",name=robotcolor")));
			} else if (actuator.getClass().equals(RobotRGBColorActuator.class)) {
				nnOutputs.add(createOutput(robot, "RobotRGBColor",
						new Arguments("id=" + actuator.getId()
								+ ",name=robotcolor")));
			} else if (actuator.getClass().equals(PreyPickerActuator.class)) {
				nnOutputs.add(createOutput(robot, "PreyPicker", new Arguments(
						"id=" + actuator.getId() + ",name=preypicker")));
			}else if (actuator.getClass().equals(MultiPreyPickerActuator.class)) {
				nnOutputs.add(createOutput(robot, "MultiPreyPicker", new Arguments(
						"id=" + actuator.getId() + ",name=multipreypicker")));
			} else {
				throw new RuntimeException(
						"Trying to automatically create output for actuator: "
								+ actuator + ", but actuator unknown here");
			}
		}

		return nnOutputs;
	}

	public NNOutput createOutput(Robot robot, String name, Arguments arguments) {
		int id = 0;
		if (arguments.getArgumentIsDefined("id"))
			id = arguments.getArgumentAsInt("id");

		if (name.equalsIgnoreCase("twowheel") || name.equalsIgnoreCase("twowheeloutput")) {		
			return new TwoWheelNNOutput(robot.getActuatorWithId(id), arguments);
		} else 	if (name.equalsIgnoreCase("robotcolor") || name.equalsIgnoreCase("robotcoloroutput")) {		
			return new RobotColorNNOutput(robot.getActuatorWithId(id),arguments);
		}else if(name.equalsIgnoreCase("behavior") || name.equalsIgnoreCase("behavioroutput")){
			return new BehaviorNNOutput(robot.getActuatorWithId(id), arguments);
		}else 	if (name.equalsIgnoreCase("robotrgbcolor") || name.equalsIgnoreCase("robotrgbcoloroutput")) { 
			return new RobotRGBColorNNOutput(robot.getActuatorWithId(id),arguments);
		} else if (name.equalsIgnoreCase("multineuroncolor") || name.equalsIgnoreCase("multineuroncoloroutput")) {
			return new MultiNeuronRobotColorNNOutput(robot.getActuatorWithId(id), arguments);
		} else if (name.equalsIgnoreCase("preypicker") || name.equalsIgnoreCase("preypickeroutput")) {
			return new PreyPickerNNOutput(robot.getActuatorWithId(id), arguments);
		} else if (name.equalsIgnoreCase("simplepreypicker")) {
			return new SimplePreyPickerNNOutput(robot.getActuatorWithId(id), arguments);
		} else if (name.equalsIgnoreCase("multipreypicker") || name.equalsIgnoreCase("multipreypickeroutput")) {
			return new MultiPreyPickerNNOutput(robot.getActuatorWithId(id), arguments);
		} else if (name.equalsIgnoreCase("monitor") || name.equalsIgnoreCase("monitoroutput")) {
			//TODO
			String new_name = arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(arguments.getArgumentAsString(new_name));
			return new SysoutNNOutput(createOutput(robot, new_name, actuators));
		} else if (name.equalsIgnoreCase("fixed")
				|| name.equalsIgnoreCase("fixedoutput")) {
			//TODO
			String new_name = arguments.getArgumentAt(0);
			Arguments actuators = new Arguments(
					arguments.getArgumentAsString(new_name));
			return new FixedNNOutput(createOutput(robot, new_name, actuators),
					arguments.getArgumentAsDouble("value"));
		} else if (name.equalsIgnoreCase("simple") || name.equalsIgnoreCase("simpleoutput")) {
			//TODO
			return new SimpleNNOutput(arguments.getArgumentAsInt("numberofoutputs"));
		} else if (name.equalsIgnoreCase("opendooroutput") || name.equalsIgnoreCase("opendoor")) {
			return new OpenDoorNNOutput(robot.getActuatorWithId(id), arguments);
		} else
			throw new RuntimeException("Unknown nn output: " + name);
	}

	public int getChromosomeLength() {
		return chromosomeLength;
	}
}
