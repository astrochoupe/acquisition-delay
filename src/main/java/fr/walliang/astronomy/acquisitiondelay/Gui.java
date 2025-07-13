package fr.walliang.astronomy.acquisitiondelay;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Gui extends JFrame {

	private static final long serialVersionUID = 290801423057511060L;
	
	private JTextArea textArea;

	@Override
	protected void frameInit() {
		super.frameInit();
		setTitle("Acquisition delay measurement");

		// close window when click on the cross
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		setSize(500, 350);

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

		JPanel panel = new JPanel();
		//panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(openFileButton);

		textArea = new JTextArea(15,40);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane);

		getContentPane().add(panel);

	}

	private void readAndProcessFile(File file) {
		textArea.setText("Reading file and processing...");
		
		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		String result = acquisitionDelay.calculate(file.getAbsolutePath(), 40);
		
		textArea.setText(result);
	}

	public static void main(String[] args) {
		JFrame frame = new Gui();
		frame.setLocationRelativeTo(null); // center window
		frame.setVisible(true);
	}

}
