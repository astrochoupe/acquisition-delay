package fr.walliang.astronomy.acquisitiondelay.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import fr.walliang.astronomy.acquisitiondelay.service.AcquisitionDelay;

/**
 * Graphical user interface.
 */
public class Gui extends JFrame {

	private static final long serialVersionUID = 290801423057511060L;
	
	private JSpinner exposureField;
	
	private JSpinner yPositionField;
	
	private JTextArea textArea;

	private static final String PROPERTIES_FILE_NAME = ".acquisition-delay.properties";
	private static final String LAST_DIR_KEY = "lastDirectory";
	private final File propertiesFile = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);

	@Override
	protected void frameInit() {
		super.frameInit();
		setTitle("Acquisition delay measurement");

		// close window when click on the cross
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setSize(500, 400);

		// create a label for exposure field
		JLabel exposureLabel = new JLabel("Exposure time (ms):");
		
		// create the exposure field
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
		spinnerModel.setMinimum(1);
		spinnerModel.setMaximum(99);
		exposureField = new JSpinner(spinnerModel);
		exposureField.setValue(40);
		
		// create a label for exposure field
		JLabel yPositionLabel = new JLabel("Y position:");
		
		// create the exposure field
		SpinnerNumberModel spinnerModel2 = new SpinnerNumberModel();
		spinnerModel2.setMinimum(-1);
		spinnerModel2.setMaximum(9999);
		yPositionField = new JSpinner(spinnerModel2);
		yPositionField.setValue(0);
		
		// create a button
		JButton openFileButton = new JButton("Open CSV file from Tangra...");

		openFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				// try to set last used directory from properties
				File lastDir = loadLastDirectory();
				if (lastDir != null) {
					fileChooser.setCurrentDirectory(lastDir);
				}

				// select file only
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				// file filter for CSV
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
				// not all file types accepted
				fileChooser.setAcceptAllFileFilterUsed(false);

				int returnValue = fileChooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();

					System.out.println("Selected file: " + selectedFile.getAbsolutePath());
					// save last directory (with security checks)
					File parent = selectedFile.getParentFile();
					if (parent != null && parent.exists() && parent.isDirectory() && parent.canRead()) {
						saveLastDirectory(parent);
					} else {
						System.err.println("Selected file parent directory is not valid for saving properties: " + parent);
					}
					
					readAndProcessFile(selectedFile);
				}
			}
		});

		// create a textarea
		textArea = new JTextArea(18,40);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		
		// create main panel
		JPanel panel = new JPanel();

		// add components to panel
		panel.add(exposureLabel);
		panel.add(exposureField);
		panel.add(yPositionLabel);
		panel.add(yPositionField);
		panel.add(openFileButton);
		panel.add(scrollPane);

		// add panel to window
		add(panel);

	}

	private void readAndProcessFile(File file) {
		textArea.setText("Reading file and processing...");
		
		Integer exposure = (Integer) exposureField.getValue();
		System.out.println("Exposure: " + exposure + " ms");
		
		Integer yPosition = (Integer) yPositionField.getValue();
		System.out.println("Y position: " + yPosition);
		
		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		String result = acquisitionDelay.calculate(file.getAbsolutePath(), exposure, yPosition);
		
		textArea.setText(result);
	}

	/**
	 * Load last directory from properties file located in user's home directory.
	 * Returns null if no valid directory is found or on error.
	 */
	private File loadLastDirectory() {
		if (propertiesFile == null) {
			return null;
		}
		if (!propertiesFile.exists() || !propertiesFile.canRead()) {
			return null;
		}
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(propertiesFile)) {
			props.load(fis);
			String path = props.getProperty(LAST_DIR_KEY);
			if (path == null || path.trim().isEmpty()) {
				return null;
			}
			File dir = new File(path);
			try {
				dir = dir.getCanonicalFile();
			} catch (IOException e) {
				// ignore and use original
			}
			if (dir.exists() && dir.isDirectory() && dir.canRead()) {
				return dir;
			}
		} catch (IOException e) {
			System.err.println("Unable to read properties file: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Save last directory to properties file in user's home directory.
	 * Performs basic security checks (exists, is directory, readable).
	 */
	private void saveLastDirectory(File dir) {
		if (dir == null) {
			return;
		}
		try {
			File canonical = dir.getCanonicalFile();
			if (!canonical.exists() || !canonical.isDirectory() || !canonical.canRead()) {
				System.err.println("Directory is not valid to save: " + canonical);
				return;
			}

			Properties props = new Properties();
			// if properties file exists, try to keep existing properties
			if (propertiesFile.exists() && propertiesFile.canRead()) {
				try (FileInputStream fis = new FileInputStream(propertiesFile)) {
					props.load(fis);
				} catch (IOException e) {
					// ignore and overwrite
				}
			}

			props.setProperty(LAST_DIR_KEY, canonical.getAbsolutePath());
			try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
				props.store(fos, "Acquisition Delay properties");
			}
		} catch (IOException e) {
			System.err.println("Unable to save properties file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		JFrame frame = new Gui();
		frame.setLocationRelativeTo(null); // center window
		frame.setVisible(true);
	}

}
