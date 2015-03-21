# Introduction #

JBotEvolver has a Graphical User Interface (GUI) that allows the user to easily check the results of simulations/experiments. The GUI can be loaded by running the ViewerMain class. The results can be analyzed even if the experiment has not yet terminated. For instance, if the total number of generations for the evolutionary algorithm is 100, you can start analyzing results after one generation has completed. Below is a screenshot of the GUI and its main controls:

![http://i.imgur.com/kfOUxRH.jpg](http://i.imgur.com/kfOUxRH.jpg)


# A - File tree #

The file tree allows you to navigate your filesystem in order to reach the experimental results quickly. Once inside the folder where the results are located, various files and folders that are available:

  * `_showbest_current.conf` --- loads the controllers for the current generation
  * `_arguments.conf` --- has a copy of the original configuration file used to execute the experiment
  * `_generationnumber` --- indicates the current generation number
  * `_fitness.log` saves the best, average and minimum fitness for the controllers of each generation
  * `_restartevolution.conf` --- this file can be used to resume an experiment that was interrupted (not with the GUI)
  * `populations` --- includes the serialized version of the controllers at each generation
  * `show_best` --- includes configuration files that allow you to quickly check the behavior of the best controllers at each generation

# B - Edit/Load/Plot #

This set of buttons allows the user to:
  * Edit: edit the text in the configuration files
  * Load: load the configurations into memory an show them in the GUI's renderer
  * Plot fitness: plot a fitness graph of the best controllers in each generation in this folder. If the current folder has more than one experiment, it will plot the fitness graph of every experiment recursively and display it simultaneously, allowing a comparison of results. If JBotEvolver is run on UNIX/Linux, it is advisable to install gnuplot in order to have better looking plots. Otherwise, a custom-made plotting solution is displayed.

# C - Extra arguments #

Sometimes it is useful to be able to change the configuration parameters of experiments. In this text area, it is possible to do so without modifying the existing configuration file. A special syntax allows you to override only some arguments.

For instance, if we want to add more robots, we can add the following:

```
--robots +numberofrobots=10
```

By using the `+` sign, we are overriding **only** the number of robots. The remaining arguments in the "--robots" argument set (such as the radius or color) remains the same. If we want to change only two arguments, we just need one `+` sign:

```
--robots +numberofrobots=10,orientation=0
```

If we want to override the complete argument set, we just omit the `+` sign. In the following case, we override everything in the "--evaluation" argument set, but only some arguments in "--robots":

```
--evaluation classname=StayAtDistanceToNestEvaluationFunction,distance=1
--robots +numberofrobots=10,orientation=0
```

After changing one or more arguments, the experiment needs to be loaded again by clicking the "Load" button or double-clicking the `_showbest_current.conf` file in the file tree.

# D - Simulation control #

In the right panel, it is possible to control various aspects of the current simulation:

  * Start: resumes the current simulation
  * Pause: pauses the current simulation
  * Quit: exists the GUI
  * Plot Graph: plots the inputs/outputs of neural network-based controllers graphically over the course of one simulation
  * Sleep: adjusts the speed of the simulation (lower values result in a faster simulation)
  * Play position: allows the user to move to a particular point in time of the current simulation
  * Number of steps to shift: changes the granularity of the fast-forward and rewind shortcuts

# Shortcuts #

The GUI has a number of useful keyboard shortcuts. In Mac OS X, CTRL is replaced by CMD:

  * CTRL + ENTER: load file
  * CTRL + P: pause/resume
  * CTRL + S: start
  * CTRL + Left/Right: rewind/fast-forward
  * +/-: zoom in/zoom out
  * ALT + arrows: pan camera

# Renderer #

The default renderer is the TwoDRenderer class, but different renderers can be easily implemented by extending the Renderer class. After implementing a new renderer, it can be loaded by changing the renderer classname in the ViewerMain class.