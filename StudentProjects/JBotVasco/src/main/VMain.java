package main;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import evolutionaryrobotics.JBotEvolver;
import gui.Gui;

public class VMain {

	public static void main(String[] args) {
		//StackDumper.dumpWhenSysOutContains("{--environment=classname=environment");

		args = new String[] { "--gui", "classname=CIResultViewerGui," + "enabledebugoptions=1,"
				+ "showCurrentFileLabel=1," + "renderer=(" + "classname=CITwoDRenderer" + ")" };

		JBotEvolver jbot = null;
		try {
			jbot = new JBotEvolver(args);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		JFrame frame = new JFrame();
		frame.add(Gui.getGui(jbot, jbot.getArguments().get("--gui")));
		frame.setSize(1000, 600);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
