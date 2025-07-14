package fr.walliang.astronomy.acquisitiondelay;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AcquisitionDelay {

	public String calculate(String filename, int exposureDurationInMs) {
		StringBuilder result = new StringBuilder();
		
		List<List<MeasurePoint>> measureArea = readFile(filename);
		
		if (measureArea == null || measureArea.isEmpty()) {
			String errorMessage = "Nothing to measure";
			System.err.println(errorMessage);
			return errorMessage;
		}

		int numArea = 1;
		for(List<MeasurePoint> measurePoints : measureArea) {
			result.append("==== AREA " + numArea++ + " ====");
			result.append("\n");
			result.append(calculatePrecisely(measurePoints, exposureDurationInMs));
		}
		
		System.out.print(result);
		return result.toString();
	}

	private List<List<MeasurePoint>> readFile(String filename) {
		List<List<MeasurePoint>> measureArea = new ArrayList<>();
		
		// read CSV file (made with Tangra software) and put data in memory
		Path file = Path.of(filename);

		System.out.println("Reading " + file.toAbsolutePath().toString());

		int lineNumber = 0;

		try {
			List<String> lines = Files.readAllLines(file);

			for (String line : lines) {
				lineNumber++;
				// debug
				// System.out.println("Line " + lineNumber + " : " + line);

				String[] fields = line.split(",");
				// debug
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

				boolean skipLine = false;
				int numArea = 0;
				
				for (int col = 2; col < fields.length; col=col+2) {
					int signal;
					try {
						signal = convertDecimalStringToInt(fields[col]);
					} catch (NumberFormatException e) {
						System.err.println("Line " + lineNumber + " column " + col+1 + " is not a decimal. Skipping this line.");
						skipLine = true;
						break;
					}

					int background;
					try {
						background = convertDecimalStringToInt(fields[col+1]);
					} catch (NumberFormatException e) {
						System.err.println("Line " + lineNumber + " column " + col+2 + " is not a decimal. Skipping this line.");
						skipLine = true;
						break;
					}

					if(measureArea.size() <= numArea) {
						measureArea.add(new ArrayList<>());
					}
					List<MeasurePoint> measurePoints = measureArea.get(numArea);
					measurePoints.add(new MeasurePoint(timeInMs, signal, background));
					numArea++;
				}
				
				if(skipLine) {
					continue;
				}

			}
			System.out.println("Reading finished");
		} catch (IOException e) {
			System.err.println("Error reading file : " + e);
		}

		return measureArea;
	}
	
	private String calculatePrecisely(List<MeasurePoint> measurePoints, int exposureDurationInMs) {
		StringBuilder result = new StringBuilder();
		int nbMeasurement = 1;

		BigDecimal exposureDurationInMsBd = BigDecimal.valueOf(exposureDurationInMs);
		BigDecimal halfExposureDuration = exposureDurationInMsBd.divide(BigDecimal.valueOf(2));

		List<BigDecimal> timesPpsStart = new ArrayList<>();
		List<BigDecimal> timesPpsEnd = new ArrayList<>();

		int signalMin = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).min().getAsInt();

		int signalMax = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).max().getAsInt();

		double previousIlluminancePercentage = 0.0f;
		for (MeasurePoint measurePoint : measurePoints) {
			int signal = measurePoint.getSignalInAdu();

			double illuminancePercentage = (double) (signal - signalMin) / (signalMax - signalMin);

			BigDecimal illuminanceDuration = BigDecimal.valueOf(exposureDurationInMs * illuminancePercentage);
			
			// we are interested by the values when the light is increasing or decreasing
			// skip first measurement because it can't be compared to the previous
			if (illuminancePercentage > 0.1 && illuminancePercentage < 0.9  && nbMeasurement > 1) {
				// if light is increasing
				if (illuminancePercentage > previousIlluminancePercentage) {
					int timeInMs = measurePoint.getTimeInMs();
					
					// if the time in ms is > 900
					// then we substract 1000 ms to have a led firing time value between 0 and 999 ms
					if(timeInMs > 900) {
						timeInMs -= 1000;
					}
					
					BigDecimal timeInMsPpsStart = new BigDecimal(timeInMs);
					timeInMsPpsStart = timeInMsPpsStart.add(halfExposureDuration);
					timeInMsPpsStart = timeInMsPpsStart.subtract(illuminanceDuration);
					timesPpsStart.add(timeInMsPpsStart);
				}
				// else if light if decreasing
				else {
					BigDecimal timeInMsPpsEnd = new BigDecimal(measurePoint.getTimeInMs());
					timeInMsPpsEnd = timeInMsPpsEnd.subtract(halfExposureDuration);
					timeInMsPpsEnd = timeInMsPpsEnd.add(illuminanceDuration);
					timesPpsEnd.add(timeInMsPpsEnd);
				}
			}
			previousIlluminancePercentage = illuminancePercentage;
			nbMeasurement++;
		}

		if (timesPpsStart.isEmpty() && timesPpsEnd.isEmpty()) {
			System.err.println("No PPS detected!");
		}

		if (!timesPpsStart.isEmpty()) {
			BigDecimal averageTimePpsStart = BigDecimalUtils.average(timesPpsStart, 1);
			BigDecimal rmsTimePpsStart = BigDecimalUtils.rootMeanSquare(timesPpsStart, averageTimePpsStart, 1);
			result.append("List of times PPS start: ");
			result.append(BigDecimalUtils.toString(timesPpsStart, 1));
			result.append("\n");
			result.append("Average time PPS start: " + averageTimePpsStart + " ms ± " + rmsTimePpsStart);
			result.append("\n");
		}

		if (!timesPpsEnd.isEmpty()) {
			BigDecimal averageTimePpsEnd = BigDecimalUtils.average(timesPpsEnd, 1);
			BigDecimal rmsTimePpsEnd = BigDecimalUtils.rootMeanSquare(timesPpsEnd, averageTimePpsEnd, 1);
			result.append("List of times PPS end: ");
			result.append(BigDecimalUtils.toString(timesPpsEnd, 1));
			result.append("\n");
			result.append("Average time PPS end: " + averageTimePpsEnd + " ms ± " + rmsTimePpsEnd);
			result.append("\n");
		}
		
		return result.toString();
	}

	private int convertDecimalStringToInt(String decimalString) throws NumberFormatException {
		double doubleValue;
		doubleValue = Double.parseDouble(decimalString);
		return (int) doubleValue;
	}

}
