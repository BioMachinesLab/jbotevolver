package gui.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A simple GUI to edit a text file
 * 
 * @author miguelduarte
 */
public class Editor extends JFrame {
	private static final long serialVersionUID = 1L;
	private String filePath;
	private JEditorPane textArea;

	public Editor(String filePath) {

		this.filePath = filePath;
		String s = "";

		try {
			File f = new File(filePath);
			Scanner scanner = new Scanner(f);

			while(scanner.hasNext())
				s+=scanner.nextLine()+"\n";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		textArea = new JEditorPane();
		textArea.setText(s);

		JButton saveButton = new JButton("Save");

		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveFile();
			}
		});

		JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JScrollPane(textArea),BorderLayout.CENTER);
		panel.add(saveButton,BorderLayout.SOUTH);

		add(panel);

		setSize(600,500);
		setLocationRelativeTo(null);

		setVisible(true);
	}

	private void saveFile() {

		try {
			FileWriter fw = new FileWriter(new File(filePath));
			fw.write(textArea.getText());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
