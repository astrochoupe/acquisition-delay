package acquisition_delay;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AcquisitionDelay {

	public void calculate(String filename, int exposureDurationInMs) {
		List<List<MeasurePoint>> measureArea = readFile(filename);
		
		if (measureArea == null || measureArea.isEmpty()) {
			System.err.println("Nothing to measure");
			return;
		}

		int numArea = 1;
		for(List<MeasurePoint> measurePoints : measureArea) {
			System.out.println("==== AREA " + numArea++ + " ====");
			//calculate(measurePoints, exposureDurationInMs);
			calculatePrecisely(measurePoints, exposureDurationInMs);
		}
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

	private void calculate(List<MeasurePoint> measurePoints, int exposureDurationInMs) {

		int halfExposureDuration = Math.round((float) exposureDurationInMs / 2);

		List<Integer> timePpsStart = new ArrayList<>();
		List<Integer> timePpsEnd = new ArrayList<>();

		int signalMin = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).min().getAsInt();

		int signalMax = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).max().getAsInt();

		float previousIlluminancePercentage = 0.0f;
		for (MeasurePoint measurePoint : measurePoints) {
			int signal = measurePoint.getSignalInAdu();

			float illuminancePercentage = (float) (signal - signalMin) / (signalMax - signalMin);
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
		
		if(timePpsStart.isEmpty() && timePpsEnd.isEmpty()) {
			System.err.println("No PPS detected!");
		}

		if(!timePpsStart.isEmpty()) {
			double preciseAverageTimePpsStart = timePpsStart.stream().mapToDouble(e -> e).average().getAsDouble();
			BigDecimal averageTimePpsStart = BigDecimal.valueOf(preciseAverageTimePpsStart).setScale(1, RoundingMode.HALF_UP);
			System.out.println("List of times PPS start: " + timePpsStart);
			System.out.println("Average time PPS start: " + averageTimePpsStart + " ms");
		}
		
		if(!timePpsEnd.isEmpty()) {
			double preciseAverageTimePpsEnd = timePpsEnd.stream().mapToDouble(e -> e).average().getAsDouble();
			BigDecimal averageTimePpsEnd = BigDecimal.valueOf(preciseAverageTimePpsEnd).setScale(1, RoundingMode.HALF_UP);
			System.out.println("List of times PPS end: " + timePpsEnd);
			System.out.println("Average time PPS end: " + averageTimePpsEnd + " ms");
		}
	}
	
	private void calculatePrecisely(List<MeasurePoint> measurePoints, int exposureDurationInMs) {

		BigDecimal exposureDurationInMsBd = BigDecimal.valueOf(exposureDurationInMs);
		BigDecimal halfExposureDuration = exposureDurationInMsBd.divide(BigDecimal.valueOf(2));

		List<BigDecimal> timePpsStart = new ArrayList<>();
		List<BigDecimal> timePpsEnd = new ArrayList<>();

		int signalMin = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).min().getAsInt();

		int signalMax = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).max().getAsInt();

		double previousIlluminancePercentage = 0.0f;
		for (MeasurePoint measurePoint : measurePoints) {
			int signal = measurePoint.getSignalInAdu();

			double illuminancePercentage = (double) (signal - signalMin) / (signalMax - signalMin);

			BigDecimal illuminanceDuration = BigDecimal.valueOf(exposureDurationInMs * illuminancePercentage);
			
			// we are interested by the values when the light is increasing or decreasing
			if (illuminancePercentage > 0.1 && illuminancePercentage < 0.9) {
				// if light is increasing
				if (illuminancePercentage > previousIlluminancePercentage) {
					BigDecimal timeInMsPpsStart = new BigDecimal(measurePoint.getTimeInMs());
					timeInMsPpsStart = timeInMsPpsStart.add(halfExposureDuration);
					timeInMsPpsStart = timeInMsPpsStart.subtract(illuminanceDuration);
					timePpsStart.add(timeInMsPpsStart);
				}
				// else if light if decreasing
				else {
					BigDecimal timeInMsPpsEnd = new BigDecimal(measurePoint.getTimeInMs());
					timeInMsPpsEnd = timeInMsPpsEnd.subtract(halfExposureDuration);
					timeInMsPpsEnd = timeInMsPpsEnd.add(illuminanceDuration);
					timePpsEnd.add(timeInMsPpsEnd);
				}
			}
			previousIlluminancePercentage = illuminancePercentage;
		}

		if (timePpsStart.isEmpty() && timePpsEnd.isEmpty()) {
			System.err.println("No PPS detected!");
		}

		if (!timePpsStart.isEmpty()) {
			BigDecimal averageTimePpsStart = average(timePpsStart, 1);
			System.out.print("List of times PPS start: ");
			print(timePpsStart, 1);
			System.out.println();
			System.out.println("Average time PPS start: " + averageTimePpsStart + " ms");
		}

		if (!timePpsEnd.isEmpty()) {
			BigDecimal averageTimePpsEnd = average(timePpsEnd, 1);
			System.out.print("List of times PPS end: ");
			print(timePpsEnd, 1);
			System.out.println();
			System.out.println("Average time PPS end: " + averageTimePpsEnd + " ms");
		}
	}

	private int convertDecimalStringToInt(String decimalString) throws NumberFormatException {
		double doubleValue;
		doubleValue = Double.parseDouble(decimalString);
		return (int) doubleValue;
	}
	
	private BigDecimal average(List<BigDecimal> list, int scale) {
		BigDecimal[] totalWithCount = list.stream().filter(bd -> bd != null)
				.map(bd -> new BigDecimal[] { bd, BigDecimal.ONE })
				.reduce((a, b) -> new BigDecimal[] { a[0].add(b[0]), a[1].add(BigDecimal.ONE) }).get();
		return totalWithCount[0].divide(totalWithCount[1], scale, RoundingMode.HALF_UP);
	}
	
	private void print(List<BigDecimal> list, int scale) {
		System.out.print("[");
		
		Iterator<BigDecimal> iterator = list.iterator();
		while(iterator.hasNext()) {
			BigDecimal bigDecimal = iterator.next();
			System.out.print(bigDecimal.setScale(scale, RoundingMode.HALF_UP));
			if(iterator.hasNext()) {
				System.out.print(", ");
			}
		}
		
		System.out.print("]");
	}

}
