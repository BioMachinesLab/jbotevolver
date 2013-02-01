package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class StartupGui implements ActionListener, KeyListener {
	JButton browseButton;
	JButton runButton;
	JButton quitButton;

	JButton saveButton;
	JButton reopenButton;
	JButton saveAsButton;

	JFrame frame;

	JComboBox fileComboBox;
	JComboBox argsComboBox;
	JEditorPane editPane;
	JLabel      currentFilenameLabel;
	String      currentFilename = new String("");

	String      originalFileContent = new String("");

	boolean run = false;

	public static final String ARGHISTORYFILENAME = ".ersimbadarghistory";
	public static final String FILEHISTORYFILENAME = ".ersimbadfilehistory";

	public StartupGui() {
		frame = new JFrame("ERSimbad");
		frame.setSize(800, 700);
		frame.setResizable(true);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(2,1));
		frame.getContentPane().add(northPanel, BorderLayout.NORTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		northPanel.add(topPanel);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout());
		northPanel.add(bottomPanel);

		topPanel.add(new JLabel("Conf. file: "));
		fileComboBox = new JComboBox(getHistory(FILEHISTORYFILENAME));
		fileComboBox.setPreferredSize(new Dimension(600, 28));
		fileComboBox.setEditable(true);
		topPanel.add(fileComboBox);
		fileComboBox.addActionListener(this);
		browseButton = new JButton("Browse...");
		browseButton.addActionListener(this);
		topPanel.add(browseButton);

		bottomPanel.add(new JLabel("Additional arguments: "));
		argsComboBox = new JComboBox(getHistory(ARGHISTORYFILENAME));
		argsComboBox.setPreferredSize(new Dimension(500, 28));
		argsComboBox.setEditable(true);
		argsComboBox.addActionListener(this);
		bottomPanel.add(argsComboBox);

		runButton = new JButton("Run!");
		bottomPanel.add(runButton);
		runButton.addActionListener(this);

		quitButton = new JButton("Quit");
		bottomPanel.add(quitButton);
		quitButton.addActionListener(this);

		JPanel editorPanel = new JPanel();
		frame.getContentPane().add(editorPanel, BorderLayout.CENTER);
		editorPanel.setLayout(new BorderLayout());
		JPanel editorButtonPanel = new JPanel();

		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		editorButtonPanel.add(saveButton);
		saveButton.setEnabled(false);
		saveAsButton = new JButton("As save...");
		saveAsButton.addActionListener(this);
		editorButtonPanel.add(saveAsButton);
		reopenButton = new JButton("Reopen");
		editorButtonPanel.add(reopenButton);
		reopenButton.addActionListener(this);
		currentFilenameLabel = new JLabel(fileComboBox.getSelectedItem() == null ? "File: none" : "File: " + fileComboBox.getSelectedItem().toString());
		editorButtonPanel.add(currentFilenameLabel);


		editorPanel.add(editorButtonPanel, BorderLayout.NORTH);
		editPane = new JEditorPane();	
		editPane.addKeyListener(this);
		editorPanel.add(new JScrollPane(editPane), BorderLayout.CENTER);		

		if (fileComboBox.getSelectedItem() != null) {
			openFileInEditor(fileComboBox.getSelectedItem().toString());
		}
	}

	private String[] getHistory(String filename) {
		Vector<String> lines = new Vector<String>(); 
		BufferedReader bufferedReader;
		String         nextLine;

		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filename))));

			while ((nextLine = bufferedReader.readLine()) != null) {
				lines.add(nextLine);
			}

			bufferedReader.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}	

		String[] results = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			results[i] = lines.elementAt(lines.size() - i - 1);
		}
		return results;
	}

	public void execute() {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		synchronized(this) {
			while (!run) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}	

		if (argsComboBox.getSelectedItem() != null) {
			addLineToFile(argsComboBox.getSelectedItem().toString(), ARGHISTORYFILENAME);			
		}

		if (fileComboBox.getSelectedItem() != null) { 
			addLineToFile(fileComboBox.getSelectedItem().toString(), FILEHISTORYFILENAME);			
		}

		frame.setVisible(false);
	}

	public String[] getArguments() {
		if (argsComboBox.getSelectedItem() == null) {
			return new String[0];
		} else {
			return argsComboBox.getSelectedItem().toString().trim().split(" ");
		}
	}	

	public String getConfFile() {
		if (fileComboBox.getSelectedItem() == null) {
			return new String("");
		}
		return fileComboBox.getSelectedItem().toString().trim();
	}

//	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseButton) {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			int returnVal = chooser.showOpenDialog(null);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				fileComboBox.setSelectedItem(chooser.getSelectedFile().getPath());
			}
		}

		if (e.getSource() == fileComboBox) {
			if (fileComboBox.getSelectedItem() != null) {
				openFileInEditor(fileComboBox.getSelectedItem().toString());
			}
		}

		if (e.getSource() == runButton) { // || e.getSource() == argsComboBox) {
			if (!editPane.getText().equals(originalFileContent)) {
				int ret = JOptionPane.showConfirmDialog(frame, "Save configuration file before running?", "Save before running?", JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					if (currentFilename.equals("")) {
						if (!saveAsAction())
							return;
					} else {
						if (!saveAction())
							return;
					}
				}
			}

			synchronized(this) {
				run = true;
				notify();
			}
		}

		if (e.getSource() == saveButton && !currentFilename.equals("")) { 
			saveAction();
		}

		if (e.getSource() == quitButton) {
			System.exit(0);
		}

		if (e.getSource() == reopenButton) {
			editPane.setText(originalFileContent);
		}

		if (e.getSource() == saveAsButton || (e.getSource() == saveButton && currentFilename.equals(""))) {
			saveAsAction();
		}
	}

	private boolean saveAsAction() {
		boolean result = true;
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setCurrentDirectory(new File("."));
		int returnVal = chooser.showSaveDialog(frame);

		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String newFilename = chooser.getSelectedFile().getPath();

			if (newFilename != null) {
				try {
					saveOptionsFile(newFilename);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(frame, "Cannot save file: " + newFilename + ", " + e1.getMessage());
					e1.printStackTrace();
					result = false;
				}
			}
		}
		return result;
	}

	private boolean saveAction() {
		boolean result = true;
		try {
			saveOptionsFile(currentFilename);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(frame, "Cannot save file: " + currentFilename + ", " + e1.getMessage());
			e1.printStackTrace();
			result = false;
		}		
		return result;
	}

	private void addLineToFile(String line, String filename) {
		String[] existingLines = getHistory(filename);
		line = line.trim();

		// Don't repeat the most recent line
		if (existingLines.length > 0) {
			if (existingLines[0].equals(line)) {
				return;
			}
		}

		BufferedWriter bw = null; 
		try {
			bw = new BufferedWriter(new FileWriter(filename, false));
			for (int i = 0; i < existingLines.length; i++) {
				if (!existingLines[0].equals(line)) {
					bw.write(existingLines[i]);
					bw.newLine();
				}
			}

			bw.write(line);
			bw.newLine();
			bw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {                       // always close the file
			if (bw != null) 
				try {
					bw.close();
				} catch (IOException ioe2) {
				}
		}
	}

	public void openFileInEditor(String filename) {
		if (filename == null)
			return;
		if (filename.trim().length() == 0)
			return;

		currentFilename = filename;
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filename))));					
			currentFilenameLabel.setText(fileComboBox.getSelectedItem() == null ? "File: none" : "File: " + fileComboBox.getSelectedItem().toString());
			String nextLine;
			StringBuffer sb = new StringBuffer();
			while ((nextLine = bufferedReader.readLine()) != null) {
				sb.append(nextLine + "\n");
			}
			editPane.setText(sb.toString());
			originalFileContent = sb.toString();
			saveButton.setEnabled(false);
			bufferedReader.close();
		} catch (Exception ex) {
			// Ignore
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

//	@Override
	public void keyPressed(KeyEvent arg0) {
	}

//	@Override
	public void keyReleased(KeyEvent arg0) {
	}

//	@Override
	public void keyTyped(KeyEvent arg0) {
		if (!originalFileContent.equals(editPane.getText()));
		saveButton.setEnabled(true);
	}		

	public void saveOptionsFile(String filename) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			bw.write(editPane.getText());
			bw.close();
			saveButton.setEnabled(false);
			currentFilename = filename;
			originalFileContent = editPane.getText();
			currentFilenameLabel.setText(filename);
			fileComboBox.setSelectedItem(filename);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e2) {
				}
			}
		}
	}	

}
