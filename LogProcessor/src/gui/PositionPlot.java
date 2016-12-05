package gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class PositionPlot extends RendererViewer {
	private final static String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";

	private File folder;
	private ArrayList<String> lines = new ArrayList<String>();
	private boolean pause = false;

	public PositionPlot(String pathToFile) {
		super("Position plot");
		this.file = file;

		try {
			Scanner s = new Scanner(new File(file));

			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (!line.startsWith("#") && !line.isEmpty())
					lines.add(line);
			}

			System.out.println(lines.size());

			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new PositionPlot(INPUT_FOLDER);
	}
}
