package logManaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import commoninterface.dataobjects.GPSData;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.EntityManipulation;
import gui.PositionPlot;

public class FileUtils {
	public static ExperiencesDataOnFile loadDataFromParsedFile(String file) {
		File inputFile = new File(file);
		if (inputFile.exists()) {
			FileInputStream fin = null;
			ObjectInputStream ois = null;

			System.out.printf("[%s] Loading data from file %s%n", PositionPlot.class.getSimpleName(), file);
			try {
				fin = new FileInputStream(inputFile);
				ois = new ObjectInputStream(fin);

				Object obj = ois.readObject();

				if (obj instanceof ExperiencesDataOnFile) {
					return (ExperiencesDataOnFile) obj;
				} else {
					return null;
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.printf("[%s] Error reading object from file! %s%n", PositionPlot.class.getSimpleName(),
						e.getMessage());
				return null;
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						System.err.printf("[%s] Error closing file input stream %s%n",
								PositionPlot.class.getSimpleName(), e.getMessage());
					}
				}

				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
						System.err.printf("[%s] Error closing object input stream %s%n",
								PositionPlot.class.getSimpleName(), e.getMessage());
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Data file does not exist", "Error reading file!",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public static boolean saveDataToFile(ExperiencesDataOnFile data, String file, boolean askoverride) {
		File outputFile = new File(file);
		if (askoverride && outputFile.exists()) {
			int result = JOptionPane.showConfirmDialog(null, "Output file already exists. Override?", "Question",
					JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		boolean toReturn = true;

		System.out.printf("[%s] Saving data to file %s%n", PositionPlot.class.getSimpleName(), file);
		try {
			fout = new FileOutputStream(outputFile);
			oos = new ObjectOutputStream(fout);

			oos.writeObject(data);
		} catch (IOException e) {
			System.err.printf("[%s] Error writting object to file! %s%n", PositionPlot.class.getSimpleName(),
					e.getMessage());
			toReturn = false;
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing file output stream %s%n", PositionPlot.class.getSimpleName(),
							e.getMessage());
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing object output stream %s%n",
							PositionPlot.class.getSimpleName(), e.getMessage());
				}
			}
		}

		return toReturn;
	}

	public static class ExperiencesDataOnFile implements Serializable {
		private static final long serialVersionUID = -895303111895917088L;
		private HashMap<Integer, List<GPSData>> gpsData;
		private HashMap<Integer, List<EntityManipulation>> entitiesManipulationData;
		private HashMap<Integer, List<DecodedLog>> decodedLogData;
		private HashMap<Integer, ExperimentLogParser.ExperimentData> experimentsData;

		public ExperiencesDataOnFile() {
			this.gpsData = null;
			this.entitiesManipulationData = null;
			this.decodedLogData = null;
			this.experimentsData = null;
		}

		public void setGPSData(HashMap<Integer, List<GPSData>> gpsData) {
			this.gpsData = gpsData;
		}

		public HashMap<Integer, List<GPSData>> getGPSData() {
			return gpsData;
		}

		public void setEntitiesManipulationData(HashMap<Integer, List<EntityManipulation>> entitiesManipulationData) {
			this.entitiesManipulationData = entitiesManipulationData;
		}

		public HashMap<Integer, List<EntityManipulation>> getEntitiesManipulationData() {
			return entitiesManipulationData;
		}

		public void setDecodedLogData(HashMap<Integer, List<DecodedLog>> decodedLogData) {
			this.decodedLogData = decodedLogData;
		}

		public HashMap<Integer, List<DecodedLog>> getDecodedLogData() {
			return decodedLogData;
		};

		public void setExperimentsData(HashMap<Integer, ExperimentLogParser.ExperimentData> experimentsData) {
			this.experimentsData = experimentsData;
		}

		public HashMap<Integer, ExperimentLogParser.ExperimentData> getExperimentsData() {
			return experimentsData;
		}
	}
}
