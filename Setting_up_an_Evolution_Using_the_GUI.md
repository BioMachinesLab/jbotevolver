# Introduction #

An evolution can be set up using the GUI of JBot Evolver.  To launch the setup GUI navigate to the ViewerMain class in the "evolutionaryrobotics" package.
Add your content here.


# Details #

# Arguments #

  * **Output Name:**  The name of the folder that will contain the results of this simulation.

  * **Robot:**  Currently only DifferentialDriveRobot is available.  See [[here](here.md)] for differential drive robot.  The front of this robot is indicated by the head of the arrow.

  * **Sensor:**  The type of sensor that will be used by the selected Robot.  See [[here](here.md)] for more on the available sensors and their options.

  * **Actuator:**  The type of actuator that will be used by the selected Robot.  See [[here](here.md)] for more on the available actuators and their options.

> <a href='Hidden comment:  TODO The restraints on the selection of sensors and actuators    need to be documented.  The significance of adding another sensor or actuator of the same type needs to be documented. sjn '></a>

  * **Controller:** The type of controller that will be used by the robot.

  * **NeuralNetwork:**  The type of neural network that will be used.  Typically the CTRNNMultilayer should be chosen.

  * **Population:**  Currently only MuLambdaPopulation is available.

  * **Environment:**  The type of environment in which the robot will operate.  See [[here](here.md)] for more on the available environments and their options.

  * **TaskExecutor:**  The manner in which the running evolutions will be executed.  Typically, ParallelTaskExecutor should be chosen for many-core computers, whereas SequentialTaskExecutor should be chosen for those with only one core.

  * **Evolution:**  Typically GenerationalEvolution should be chosen.
> > <a href='Hidden comment:  TODO A simple description of what Evolution is for should be documented. sjn '></a>

  * **EvaluationFunction:**  The evaluation function according to which offspring will be chosen to reproduce.
<a href='Hidden comment:  TODO A better description of what EvaluationFunction is for should be documented. sjn '></a>


# Attributes List #

The selected sensors and actuators will appear in this section.  Double click to see a preview.



# General Guidelines #

All arguments must be specified.  Once an argument is selected, the corresponding options should be verified.  Then click the "Add to File" button in order to confirm and add the selected items to the configuration file.  Only certain combinations of the Sensor, Actuator, Environment, and EvaluationFunction arguments are valid.  To test if your configurations are valid, click on the "Test File" button.  To run the simulation click "Run Evolution".  See [[here](here.md)] for information on the Evolution tab, and [[here](here.md)] for information on viewing the results in the Results tab.




<a href='Hidden comment:  TODO Continue here. sjn '></a>