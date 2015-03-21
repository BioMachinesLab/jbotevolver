# Introduction #

The results of a completed or in progress simulation can be seen by clicking on the Results tab.


# Overview of Files and Folders #

In the directory tree navigate to the JBotEvolver directory.  Inside this directory find a directory containing your results.  The name is what was entered in the "Output Name" text field in the Configuration tab for the simulation.  The file with this name followed by a ".conf" extension contains the configurations used in that simulation.  It can be viewed by highlighting it and clicking on the "Edit" button.

> ## Inside the Results Folder ##

  * arguments.conf  This file also contains the chosen configuration for this simulation.  Highlight and click the "Edit" button to view.

  * _fitness.log This file contains the numerical data of the simulation, including the average and worst fitness of each generation, as well as the date and time on which the evolution was run.  Highlight and click the "Edit" button to view._

<a href='Hidden comment: 
TODO Include description of what the _generationnumber file shows.  sjn
'></a>

<a href='Hidden comment: 
TODO Include description of what the _restartevolution.conf file shows.  sjn
'></a>

  * _showbest\_current.conf  Double click on this file to enable viewing the best current evolution.  See below for detailed controls._

<a href='Hidden comment: 
TODO Make above, an internal link instead of saying below.  sjn
'></a>


  * Click Plot Fitness at any time to see a graphical plot of the fitness for each generation.

<a href='Hidden comment: 
TODO Add what Compare Fitness button does .  sjn
'></a>


<a href='Hidden comment: 
TODO Add how to work with the Extra arguments window and the New Random Seed button .  sjn
'></a>

> ## Viewing Previous Evolutions ##

  * The Start/Pause button This button plays and pauses the progress of the evolution.

<a href='Hidden comment: 
TODO Add what Plot Neural Activations does.  sjn
'></a>

  * Sleep between control steps (ms) This slider adjusts the speed.  The more ms between each control step, the slower the progress displays.

  * Play position  This slider allows one to jump to different points in time of the evolution.

  * Control step  This text field shows the current control step


<a href='Hidden comment: 
TODO Add what numbers in Fitness text field mean as opposed to numbers found in the fitness log.  sjn
'></a>

