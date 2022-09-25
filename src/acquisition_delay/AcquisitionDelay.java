package acquisition_delay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AcquisitionDelay {

	public void calculate(String filename, int exposureDurationInMs) {
		List<MeasurePoint> measurePoints = readFile(filename);

		if (measurePoints == null || measurePoints.isEmpty()) {
			System.err.println("Nothing to measure");
			return;
		}

		calculate(measurePoints, exposureDurationInMs);
	}

	private List<MeasurePoint> readFile(String filename) {
		List<MeasurePoint> measurePoints = new ArrayList<>();

		// read CSV file (made with Tangra software) and put data in memory
		Path file = Path.of(filename);

		System.out.println("Reading " + file.toAbsolutePath().toString());

		int lineNumber = 0;

		try {
			List<String> lines = Files.readAllLines(file);

			for (String line : lines) {
				lineNumber++;
				// debug
				// System.out.println("Ligne " + lineNumber + " : " + line);

				String[] fields = line.split(",");
				// List<String> fields2 = Arrays.asList(fields);
				// System.out.println(fields2);

				if (fields.length < 4) {
					System.err.println("Line " + lineNumber
							+ " cannot be parsed as comma separated values. There must be at least 4 comma separated fields. Skipping this line.");
					continue;
				}

				try {
					int frameNumber = Integer.parseInt(fields[0]);
				} catch (NumberFormatException e) {
					System.err.println("Line " + lineNumber + " column 1 is not an integer. Skipping this line.");
					continue;
				}

				String time = fields[1];
				String[] timeSplit = time.split("\\.");
				if (timeSplit.length != 2) {
					System.err.println("Line " + lineNumber + " column 2"
							+ " cannot separate time in s and time in ms by a point. Skipping this line.");
					continue;
				}
				int timeInMs;
				try {
					timeInMs = Integer.valueOf(timeSplit[1].substring(0, 3));
				} catch (NumberFormatException e1) {
					System.err.println("Line " + lineNumber + " column 2"
							+ " cannot convert time in ms after the point to integer. Skipping this line.");
					continue;
				} catch (IndexOutOfBoundsException e2) {
					System.err.println("Line " + lineNumber + " column 2"
							+ " string after point is longer that 4 caracters. Skipping this line.");
					continue;
				}

				int signal;
				try {
					signal = convertDecimalStringToInt(fields[2]);
				} catch (NumberFormatException e) {
					System.err.println("Line " + lineNumber + " column 3 is not a decimal. Skipping this line.");
					continue;
				}

				int background;
				try {
					background = convertDecimalStringToInt(fields[3]);
				} catch (NumberFormatException e) {
					System.err.println("Line " + lineNumber + " column 4 is not a decimal. Skipping this line.");
					continue;
				}

				measurePoints.add(new MeasurePoint(timeInMs, signal, background));
			}
			System.out.println("Reading finished");
		} catch (IOException e) {
			System.err.println("Error reading file : " + e);
		}

		return measurePoints;
	}

	private void calculate(List<MeasurePoint> measurePoints, int exposureDurationInMs) {

		int halfExposureDuration = Math.round((float) exposureDurationInMs / 2);

		List<Integer> timePpsStart = new ArrayList<>();
		List<Integer> timePpsEnd = new ArrayList<>();

		int signalMin = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).min().getAsInt();

		int signalMax = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).max().getAsInt();

		float previousIlluminancePercentage = 0.0f;
		for (MeasurePoint measurePoint : measurePoints) {
			int signal = measurePoint.getSignalInAdu();

			float illuminancePercentage = (float) signal / signalMax;
			int illuminanceDuration = Math.round(exposureDurationInMs * illuminancePercentage);

			// we are interested by the values when the light is increasing or decreasing
			if (illuminancePercentage > 0.1 && illuminancePercentage < 0.9) {
				// if light is increasing
				if (illuminancePercentage > previousIlluminancePercentage) {
					int timeInMsPpsStart = measurePoint.getTimeInMs() + halfExposureDuration - illuminanceDuration;
					timePpsStart.add(timeInMsPpsStart);
				}
				// else if light if decreasing
				else {
					int timeInMsPpsStart = measurePoint.getTimeInMs() - halfExposureDuration + illuminanceDuration;
					timePpsEnd.add(timeInMsPpsStart);
				}
			}
			previousIlluminancePercentage = illuminancePercentage;
		}

		// TODO : check if timePpsStart or timePpsEnd are empty
		double preciseAverageTimePpsStart = timePpsStart.stream().mapToDouble(e -> e).average().getAsDouble();
		int averageTimePpsStart = Math.round((float) preciseAverageTimePpsStart);
		double preciseAverageTimePpsEnd = timePpsEnd.stream().mapToDouble(e -> e).average().getAsDouble();
		int averageTimePpsEnd = Math.round((float) preciseAverageTimePpsEnd);

		System.out.println("List of times PPS start: " + timePpsStart);
		System.out.println("List of times PPS end: " + timePpsEnd);

		System.out.println("Average time PPS start: " + averageTimePpsStart + " ms");
		System.out.println("Average time PPS end: " + averageTimePpsEnd + " ms");

	}

	private int convertDecimalStringToInt(String decimalString) throws NumberFormatException {
		double doubleValue;
		doubleValue = Double.parseDouble(decimalString);
		return (int) doubleValue;
	}

}
