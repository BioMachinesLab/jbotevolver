# Introduction #

To setup an evolution, it is necessary to create a configuration file where the different parameters can be defined. This configuration file consists of a plain text file where the order of the different sets of arguments is not important. It is possible to organize the configurations through indentation, since  whitespace characters are ignored. Each argument set must be preceded by a '--' before its name. The full list of available sets of arguments is the following:

  * --robots
  * --environment
  * --evaluation
  * --evolution
  * --executor
  * --random-seed
  * --output
  * --controllers
  * --population

The different classes are loaded in the simulator using Java's Reflection by specifying the "classname" argument. New classes can be implemented anywhere in the codebase, as JBotEvolver automatically looks for the classes in all packages.

Each argument set can have different arguments, depending on the implementation. For instance, some sensors might have arguments for the range and opening angles, while others might use different arguments. You can always check the implementations of each class to understand what arguments they can receive. This allows us to quickly change the experimental parameters without recompiling the code.

Below, we discuss each individual set of arguments.

# Output #
This parameter is where you can define the name of the folder where your the experimental data will be saved

```
--output folder
```


# Robots #
This parameter allow you to define the characteristics of the robots, such as the type (by adding the name of the Java class in the field "classname"), initial position, radius, color, sensors and actuators.
When defining the type of sensors and actuators that you want to give to your robot, you need to specify the type (using the classname, exactly as before), id and some times extra parameters that the sensor/actuator may need. An example of a robot declaration could be:

```
--robots
	classname=DifferentialDriveRobot,
	x=0.5,
	y=.5,
	radius=.05,
        numberofrobots=1,
	color=red,
	sensors=(
		SimpleNestSensor=(classname=SimpleNestSensor,id=1,numbersensors=8, orientation=0, angle=45, range=5),
		SimplePreySensor=(classname=SimplePreySensor,id=2,numbersensors=8, orientation=0, angle=45, range=3)
	),
	actuators=(
		TwoWheelActuator=(classname=TwoWheelActuator,id=1,maxspeed=0.1)
	)
```

# Environment #

This parameter lets you specify environment where the robot will be tested, as well as the number of steps/control cycles for each simulation sample:

```
--environment 
	classname=RoundForageEnvironment,
	densityofpreys=0.1,
	nestlimit=0.25,
	foragelimit=2,
	forbiddenarea=5,
	steps=500
```

# Controllers #

This parameter allows you to define the controller that will control the robot. At the moment, we support two classes of controllers: (i) preprogrammed controllers, and (ii) neural network-based controllers, which can be evolved. Any type of controller can potentially be implemented. In the case of neural network-based controllers, you can specify the parameters of the neural network like so:

```
--controllers
	classname=NeuralNetworkController,
	network=(classname=CTRNNMultilayer,
		hiddennodes=3,
		inputs=(
			SimpleNestNNInput=(classname=SimpleNestNNInput,id=1),
			SimplePreyNNInput=(classname=SimplePreyNNInput,id=2)
		),outputs=(
			TwoWheelNNOutput=(classname=TwoWheelNNOutput,id=1)
		),
		robotconfigid=1
	)
```

The IDs in the "controllers" argument set should match the IDs in the "robots" argument set.

# Population #

This argument set allows you to specify the evolutionary population settings for your evolution. In this case, we use a mu+lambda population with mutation only:

```
--population 
	classname=MuLambdaPopulation,
	size=100,
	samples=10,
	generations=100,
	mutationrate=0.1
```

# Evaluation #

This argument set is used to indicate the fitness function that will be used to evaluate the robots' performance and some time extra parameters that might be necessary:

```
--evaluation 
	classname=StayAtDistanceToNestEvaluationFunction,
	distance=1
```

# Evolution #

With this argument set you can select the type of evolution that you want, such as generational evolution, co-evolution, NEAT, etc:

```
--evolution classname=GenerationalEvolution
```

# Executor #

With this argument set you can select the way that your computer will execute the evolutionary process. Three different task executors are implemented:

  * Sequential Task Executor
    * Executes the tasks locally in sequence
  * Parallel Task Executor
    * Executes tasks locally in parallel, taking advantage of the number of processors in the computer
  * Conilon Task Executor
    * Uses our lightweight distributed computing platform for a faster evolution time. You can find Conilon at the following URL: https://code.google.com/p/conilon/

```
--executor classname=SequentialTaskExecutor
```

# Random-seed #

This argument set gives the possibility to change the initial random seed, allowing you to reach different solutions. If the same random seed is used, the evolutionary process should reach exactly the same solutions.

```
--random-seed 1337
```