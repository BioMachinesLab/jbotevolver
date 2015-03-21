# Introduction #

JBotEvolver is modular, and its core is composed of two different projects. If you intend on using the simulator to evolve controllers, both projects should be checked-out from the repository. You can then use them as external libraries and create your own project that uses and extends the simulator's capabilities.

  * JBotSim - responsible for the simulated environment, robot models, controllers, physics, rendering, etc.
  * JBotEvolver - responsible for evolution-related code, such as evolutionary algorithms, evaluation function, populations, etc.

# Setting up the Code #

Checkout both the JBotSim and JBotEvolver projects into your IDE. Make sure JBotSim is set as a dependency of JBotEvolver. When working with the simulator, it's a good practice to create a different project that depends on JBotEvolver. In this new project, you can extend the simulator without cluttering the core codebase. You can then load your new classes transparently in the configuration files.

# Running Evolutions #

After you setup a configuration file (see "Setting up an Evolution" wiki page), you can run an evolution by creating using the existing EvolverMain class, where you only need to indicate the location of the file:

```
new EvolverMain(new String[]{"configuration_file.conf"});
```

Alternatively, JBotEvolver can be used as a library. The following is an example of how an evolution can be manually ran by using various methods of JBotEvolver:

```
JBotEvolver jBotEvolver = new JBotEvolver(args);
TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
taskExecutor.start();
Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
evo.executeEvolution();
taskExecutor.stopTasks();
```