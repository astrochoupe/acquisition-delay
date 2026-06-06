package fr.walliang.astronomy.acquisitiondelay.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.walliang.astronomy.acquisitiondelay.service.AcquisitionDelay;

/**
 * Graphical user interface.
 */
public class Gui extends Application {

	private static final Logger LOGGER = LogManager.getLogger(Gui.class);

	private Spinner<Integer> exposureField;
	private Spinner<Integer> yPositionField;
	private TextArea textArea;
	private Label fileLabel;
	private Button openFileButton;

	private static final String PROPERTIES_FILE_NAME = ".acquisition-delay.properties";
	private static final String LAST_DIR_KEY = "lastDirectory";
	private final File propertiesFile = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Acquisition delay measurement");

		GridPane form = new GridPane();
		form.setHgap(8);
		form.setVgap(8);

		Label exposureLabel = new Label("Exposure time (ms):");
		exposureField = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 40));
		exposureField.setEditable(true);
		exposureField.setPrefWidth(90);

		Label yPositionLabel = new Label("Y position:");
		yPositionField = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 9999, 0));
		yPositionField.setEditable(true);
		yPositionField.setPrefWidth(90);

		form.add(exposureLabel, 0, 0);
		form.add(exposureField, 1, 0);
		form.add(yPositionLabel, 0, 1);
		form.add(yPositionField, 1, 1);

		openFileButton = new Button("Open CSV file from Tangra...");
		fileLabel = new Label("No file selected");
		fileLabel.setStyle("-fx-text-fill: gray;");
		HBox fileRow = new HBox(10, openFileButton, fileLabel);
		fileRow.setAlignment(Pos.CENTER_LEFT);

		openFileButton.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open CSV file");
			fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("CSV files", "*.csv")
			);

			File lastDir = loadLastDirectory();
			if (lastDir != null) {
				fileChooser.setInitialDirectory(lastDir);
			}

			File selectedFile = fileChooser.showOpenDialog(primaryStage);
			if (selectedFile != null) {
				LOGGER.info("Selected file: {}", selectedFile.getAbsolutePath());
				File parent = selectedFile.getParentFile();
				if (parent != null && parent.exists() && parent.isDirectory() && parent.canRead()) {
					saveLastDirectory(parent);
				} else {
					LOGGER.error("Selected file parent directory is not valid for saving properties: {}", parent);
				}
				fileLabel.setText(selectedFile.getName());
				fileLabel.setStyle("-fx-text-fill: black;");
				readAndProcessFile(selectedFile);
			}
		});

		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setPrefRowCount(18);
		textArea.setPrefColumnCount(40);
		ScrollPane scrollPane = new ScrollPane(textArea);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);

		VBox root = new VBox(10,
			form,
			fileRow,
			scrollPane
		);
		root.setPadding(new Insets(10));

		primaryStage.setScene(new Scene(root, 550, 450));
		primaryStage.setMinWidth(400);
		primaryStage.setMinHeight(300);
		primaryStage.show();
	}

	private void readAndProcessFile(File file) {
		textArea.setText("Processing...");
		openFileButton.setDisable(true);

		int exposure = exposureField.getValue();
		int yPosition = yPositionField.getValue();
		LOGGER.info("Exposure: {} ms, Y position: {}", exposure, yPosition);

		Task<String> task = new Task<String>() {
			@Override
			protected String call() {
				return new AcquisitionDelay().calculate(file.getAbsolutePath(), exposure, yPosition);
			}
		};

		task.setOnSucceeded(e -> {
			textArea.setText(task.getValue());
			openFileButton.setDisable(false);
		});

		task.setOnFailed(e -> {
			Throwable ex = task.getException();
			LOGGER.error("Calculation failed", ex);
			textArea.setText("Error: " + ex.getMessage());
			openFileButton.setDisable(false);
		});

		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Load last directory from properties file located in user's home directory.
	 * Returns null if no valid directory is found or on error.
	 */
	private File loadLastDirectory() {
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
			LOGGER.error("Unable to read properties file: {}", e.getMessage());
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
				LOGGER.error("Directory is not valid to save: {}", canonical);
				return;
			}

			Properties props = new Properties();
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
			LOGGER.error("Unable to save properties file: {}", e.getMessage());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

}
