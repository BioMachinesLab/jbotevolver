package gui.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

@SuppressWarnings("serial")
public class BlenderRenderer extends Renderer {	
	protected HashMap<Robot, String> robotNames;
	protected HashMap<Prey,  String> preyNames;

	protected String  outputDirectory           = "blenderrenderer";
	protected String  masterFilename            = "animation.py";
	protected String  aniFunctionsFilename      = "anifunctions.py";
	protected String  createObjectsFilename     = "createobjects.py";
	protected String  frameFilename             = "frames";
	protected int     framesPerFile             = 10;
	protected int     currentFrame              = 0;
	protected int     framesBeforeFileChange    = 0;
	protected boolean firstFrame                = true;
	protected int     nextPreyNumber            = 0;
	protected int     nextRobotNumber           = 0;

	protected String masterFileHeader = "" +
	"#!BPY\n" +
	"import bpy\n" +
	"import math\n" +
	"from math import *\n" + 
	"import os\n" +
	"try:\n" +
	"  os.chdir(\"%s\")\n" +
	"except:\n" +
	"  try:\n" + 
	"    os.chdir(\"../%s\")\n" +
	"  except:\n" +
	"    nothing = \"nothing\"\n" +
	"f = Blender.Get('curframe')\n";

	protected String framesFileHeader = "" + 
	"#!BPY\n" +
	"import bpy\n" +
	"import math\n" +
	"from math import *\n" +
	"exec file(\"anifunctions.py\")\n" + 
	"f = Blender.Get('curframe')\n";

	protected String createObjectsHeader = "" +
	"import bpy\n" +
	"scn = Scene.GetCurrent()\n" +
	"context = scn.getRenderingContext()\n" +
	"context.endFrame(%d)\n" +
	"def CreateRobot(robotname):\n" + 
	"  o = Object.Get(\"robotmodel\")\n" + 
	"  o.select(1)\n" +
	"  Object.Duplicate(0, 0, 0)\n" +
	"  os = Object.GetSelected()\n" +
	"  os[0].setName(robotname)\n" +
	"  for o in os:\n" +
	"    o.select(0)\n" +
	"\n" +
	"def CreatePrey(preyname):\n" + 
	"  o = Object.Get(\"preymodel\")\n" + 
	"  o.select(1)\n" +
	"  Object.Duplicate(0, 0, 0)\n" +
	"  os = Object.GetSelected()\n" +
	"  os[0].setName(preyname)\n" +
	"  for o in os:\n" +
	"    o.select(0)\n" + 
	"\n";

	protected String aniFunctionsContent = "" +
	"#!BPY\n" +
	"import bpy\n" +	
	"import math\n" +
	"from math import *\n" +
	"\n" +
	"def EnableObject(o):\n" +
	"  o = o\n" +
	"def DisableObject(o):\n" +
	"  o.LocX = 10000\n" +
	"  o.LocY = 10000\n" +
	"  o.LocZ = 10000\n" +
	"\n" +  
	"def RotateX(x, y, angle):\n" +
	"  return cos(angle) * x - sin(angle) * y\n" +
	"\n" +
	"def RotateY(x, y, angle):\n" +
	"  return cos(angle) * y + sin(angle) * x\n" +
	"\n" +
	"def SetRobotState(robotname, locx, locy, orientation, radius, red, green, blue):\n" + 
	"  o = Object.Get(robotname)\n" +
	"  o.LocX = locx\n" +
	"  o.LocY = locy\n" +
	"  o.RotZ = orientation\n" +
	"  o.SizeX = radius\n" +
	"  o.SizeY = radius\n" +
	"  mat = o.getData().materials[0]\n" +
	"  mat.R = red\n" +
	"  mat.G = green\n" +
	"  mat.B = blue\n" +
	"\n" +
	"def SetPreyState(preyname, locx, locy, orientation, radius):\n" +
	"  o = Object.Get(preyname)\n" +
	"  o.SizeX = radius\n" +
	"  o.SizeY = radius\n" +
	"  o.LocX = locx\n" +
	"  o.LocY = locy\n" +
	"  o.RotZ = orientation\n";

	PrintStream masterFile       = null;
	PrintStream currentFrameFile = null;

	public BlenderRenderer(Arguments args) {
		super(args);
		robotNames = new HashMap<Robot, String>();
		preyNames  = new HashMap<Prey,  String>();        
	}

	@Override
	public void dispose() {
		masterFile.close();
		if (currentFrameFile != null) {
			currentFrameFile.close();
		}
		PrintStream createObjectsFile;
		try {
			createObjectsFile = openForWriting(outputDirectory + "/" + createObjectsFilename, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot open '" + outputDirectory + "/" + createObjectsFilename + "' for writing: " + e);
		}

		createObjectsFile.printf(createObjectsHeader, currentFrame + 1);	

		for (String s : preyNames.values()) {
			createObjectsFile.println("CreatePrey(\"" + s + "\")");
		}

		for (String s : robotNames.values()) {
			createObjectsFile.println("CreateRobot(\"" + s + "\")");
		}

		createObjectsFile.close();

		PrintStream aniFunctionsFile;
		try {
			aniFunctionsFile = openForWriting(outputDirectory + "/" + aniFunctionsFilename, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot open '" + outputDirectory + "/" + aniFunctionsFilename + "' for writing: " + e);
		}
		aniFunctionsFile.println(aniFunctionsContent);
		aniFunctionsFile.close();
	}

	public void setOutputDirectory(String directory) {
		outputDirectory = directory;
	}

	protected PrintStream openForWriting(String filename, boolean append) throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(filename, append));
	}

	@Override
	public void paint(Graphics g) {
		g.drawString("Outputting to directory \"" + outputDirectory + "\", current frame: " + currentFrame, 10, 10);
	}

	@Override
	public void drawFrame() {
		if (firstFrame) {
			File f = new File(outputDirectory);
			if (!f.exists())
				if (!f.mkdirs()) {
					throw new RuntimeException("Cannot create output directory: " + outputDirectory);
				}	
			try {
				masterFile = openForWriting(outputDirectory + "/" + masterFilename, false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot open '" + outputDirectory + "/" + masterFilename + "' for writing: " + e);
			}
			masterFile.printf(masterFileHeader, outputDirectory, outputDirectory);
			firstFrame = false;
		}

		if (framesBeforeFileChange == 0) {
			if (currentFrameFile != null) {
				currentFrameFile.close();
			}

			String currentFrameFilename = outputDirectory + "/" + frameFilename + currentFrame + ".py";
			try {
				currentFrameFile = openForWriting(currentFrameFilename, false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException("Cannot open '" + currentFrameFilename + "' for writing: " + e);
			}
			masterFile.printf("if f > %d and f <= %d:\n", currentFrame, currentFrame + framesPerFile);
			masterFile.printf("  exec file(\"" + frameFilename + currentFrame + ".py\")\n\n");

			framesBeforeFileChange = framesPerFile;

			currentFrameFile.println(framesFileHeader);
			repaint();
		} 
		framesBeforeFileChange--;

		currentFrameFile.printf("if f == %d:\n", currentFrame + 1);

		if(simulator.getEnvironment().getMovableObjects().size() > 0) {
			Vector2d position;
			double   orientation;
			double   radius;
			Color    color;
			for (PhysicalObject m : simulator.getEnvironment().getAllObjects()) {
				position    = m.getPosition();
				orientation = m.getOrientation();
				radius      = m.getRadius();
				switch(m.getType()) {
				case PREY:
					String preyName = preyNames.get(m);
					if (preyName == null) {
						preyName = "prey" + (nextPreyNumber++);
						preyNames.put((Prey) m, preyName);
					}

					currentFrameFile.printf("  SetPreyState(\"%s\", %f, %f, %f, %f)\n", 
							preyName,
							position.getX(),
							position.getY(),
							orientation,
							radius);
					break;
				case ROBOT:
					color       = ((Robot) m).getBodyColor();
					String robotName = robotNames.get(m);
					if (robotName == null) {
						robotName = "robot" + (nextRobotNumber++);
						robotNames.put((Robot) m, robotName);
					}
					currentFrameFile.printf("  SetRobotState(\"%s\", %f, %f, %f, %f, %f, %f, %f)\n",
							robotName,
							position.getX(),
							position.getY(),
							orientation,
							radius,
							color.getRed()   / 255.0,  
							color.getGreen() / 255.0, 
							color.getBlue()  / 255.0);
					break;
				}
			}
		}
		currentFrame++;
	}

	@Override
	public void setSimulator(Simulator simulator) {
		this.simulator = simulator;
	}

	@Override
	public void resetZoom() {
	}

	@Override
	public void zoomIn() {
	}

	@Override
	public void zoomOut() {
	}
}