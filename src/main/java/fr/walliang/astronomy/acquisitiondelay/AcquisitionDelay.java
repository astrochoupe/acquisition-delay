package fr.walliang.astronomy.acquisitiondelay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AcquisitionDelay {

	public String calculate(String filename, int exposureDurationInMs) {
		StringBuilder result = new StringBuilder();
		
		TangraCsvFileReader tangraFile = new TangraCsvFileReader(filename);
		List<ObjectInfomation> objects = tangraFile.parseFile();
		
		if (objects == null || objects.isEmpty()) {
			String errorMessage = "Nothing to measure";
			System.err.println(errorMessage);
			return errorMessage;
		}

		int numObject = 1;
		for(ObjectInfomation object : objects) {
			List<MeasurePoint> measurePoints = object.getMeasures();
			result.append("==== OBJECT " + numObject++ + " ====");
			result.append("\n");
			result.append(calculatePrecisely(measurePoints, exposureDurationInMs));
		}
		
		System.out.print(result);
		return result.toString();
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

}
