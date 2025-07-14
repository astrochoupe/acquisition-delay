package fr.walliang.astronomy.acquisitiondelay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

/**
 * Graphical user interface.
 */
public class Gui extends JFrame {

	private static final long serialVersionUID = 290801423057511060L;
	
	private JSpinner exposureField;
	
	private JTextArea textArea;

	@Override
	protected void frameInit() {
		super.frameInit();
		setTitle("Acquisition delay measurement");

		// close window when click on the cross
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setSize(500, 350);

		// create a label for exposure field
		JLabel exposureLabel = new JLabel("Exposure time (ms):");
		
		// create the exposure field
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
		spinnerModel.setMinimum(1);
		spinnerModel.setMaximum(99);
		exposureField = new JSpinner(spinnerModel);
		exposureField.setValue(40);
		
		// create a button
		JButton openFileButton = new JButton("Open CSV file from Tangra...");

		openFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

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
					
					readAndProcessFile(selectedFile);
				}
			}
		});

		// create a textarea
		textArea = new JTextArea(15,40);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		
		// create main panel
		JPanel panel = new JPanel();

		// add components to panel
		panel.add(exposureLabel);
		panel.add(exposureField);
		panel.add(openFileButton);
		panel.add(scrollPane);

		// add panel to window
		add(panel);

	}

	private void readAndProcessFile(File file) {
		textArea.setText("Reading file and processing...");
		
		Integer exposure = (Integer) exposureField.getValue();
		System.out.println("Exposure: " + exposure + " ms");
		
		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		String result = acquisitionDelay.calculate(file.getAbsolutePath(), exposure);
		
		textArea.setText(result);
	}

	public static void main(String[] args) {
		JFrame frame = new Gui();
		frame.setLocationRelativeTo(null); // center window
		frame.setVisible(true);
	}

}
