package fr.walliang.astronomy.acquisitiondelay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.walliang.astronomy.acquisitiondelay.dao.FileReader;
import fr.walliang.astronomy.acquisitiondelay.dao.MeasurePoint;
import fr.walliang.astronomy.acquisitiondelay.dao.ObjectInfomation;
import fr.walliang.astronomy.acquisitiondelay.dao.TangraCsvFileReader;

public class AcquisitionDelay {

	/**
	 * Calculate acquisition delay.
	 * 
	 * @param filename
	 * @param exposureDurationInMs
	 * @param yPosition -1 to disable linear trend
	 * @return a string with the detailed result
	 */
	public String calculate(String filename, int exposureDurationInMs, int yPosition) {
		StringBuilder result = new StringBuilder();
		
		FileReader tangraFile = new TangraCsvFileReader(filename);
		List<ObjectInfomation> objects = tangraFile.parseFile();
		
		if (objects == null || objects.isEmpty()) {
			String errorMessage = "Nothing to measure";
			System.err.println(errorMessage);
			return errorMessage;
		}

		int numObject = 1;
		List<Delay> delays = new ArrayList<>();
		for(ObjectInfomation object : objects) {
			List<MeasurePoint> measurePoints = object.getMeasures();
			ObjectResult objectResult = calculatePrecisely(measurePoints, exposureDurationInMs);
			
			BigDecimal delay = objectResult.getAverageTimePpsStart();
			int y = object.getY();
			BigDecimal yBigDecimal = new BigDecimal(y);
			delays.add(new Delay(delay, yBigDecimal));
			
			result.append("==== OBJECT ");
			result.append( numObject++);
			result.append(" (y=");
			result.append(object.getY());
			result.append(")");
			result.append(" ====");
			result.append("\n");
			result.append(objectResult);
		}
		
		// if 2 objects or more, we can calculate a linear trend for rolling shutter sensors
		// and yPosition is set (>= 0)
		if(objects.size() >= 2 && yPosition >= 0) {
			LinearTrend linearTrend = estimateLinearTrend(delays);
			int scale = 1;
			BigDecimal delayAtYPosition = linearTrend.getY(yPosition).setScale(scale, RoundingMode.HALF_UP);
			
			result.append("==== LINEAR TREND ====");
			result.append("\n");
			result.append("Acquisition delay at Y position ");
			result.append(yPosition);
			result.append(": ");
			result.append(delayAtYPosition);
			result.append(" ms");
		}
		
		System.out.print(result);
		return result.toString();
	}

	private ObjectResult calculatePrecisely(List<MeasurePoint> measurePoints, int exposureDurationInMs) {
		ObjectResult objectResult = new ObjectResult();
		int nbMeasurement = 1;

		BigDecimal exposureDurationInMsBd = BigDecimal.valueOf(exposureDurationInMs);
		BigDecimal halfExposureDuration = exposureDurationInMsBd.divide(BigDecimal.valueOf(2));

		List<BigDecimal> timesPpsStart = new ArrayList<>();
		List<BigDecimal> timesPpsEnd = new ArrayList<>();

		int signalMin = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).min().getAsInt();

		int signalMax = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).max().getAsInt();
		
		int median = IntUtils.median(measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu));
		
		int baselineUpperLimit = median + (median - signalMin);
		List<Integer> signalsWhenLedTurnedOff = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).filter(e -> e < baselineUpperLimit).boxed().collect(Collectors.toList());
		IntStatistics baselineStats = new IntStatistics(signalsWhenLedTurnedOff);
		
		//System.out.println("median = " + median);
		//System.out.println("baselineUpperLimit = " + baselineUpperLimit);
		//System.out.println(baselineStats);
		
		int baseLine = (int) baselineStats.getAverage();
		int stdDev = (int) baselineStats.getStandardDeviation();
		
		int topLineLowerLimit = signalMax - 5 * stdDev;
		List<Integer> signalsWhenLedTurnedOn = measurePoints.stream().mapToInt(MeasurePoint::getSignalInAdu).filter(e -> e > topLineLowerLimit).boxed().collect(Collectors.toList());
		IntStatistics topLineStats = new IntStatistics(signalsWhenLedTurnedOn);
		
		//System.out.println(topLineStats);
		int topLine = (int) topLineStats.getAverage();
		
		double previousIlluminancePercentage = 0.0f;
		for (MeasurePoint measurePoint : measurePoints) {
			int signal = measurePoint.getSignalInAdu();

			double illuminancePercentage = (double) (signal - baseLine) / (topLine - baseLine);
			
			// uncertainty calculation
			double a = signal - baseLine;
			double b = topLine - baseLine;
			double uncertaintyA = 2 * baselineStats.getUncertainty();
			double uncertaintyB = topLineStats.getUncertainty() + baselineStats.getUncertainty();
			
			double uncertaintyIlluminancePercentage = illuminancePercentage * (uncertaintyA/a + uncertaintyB/b);
			
			//System.out.println("Uncertainties :");
			//System.out.println("illuminancePercentage = " + illuminancePercentage + "%");
			//System.out.println("incertitudeIlluminancePercentage = " + incertitudeIlluminancePercentage + "%");
			double uncertaintyExposureDurationInMs = exposureDurationInMs * uncertaintyIlluminancePercentage;
			//System.out.println("incertitudeExposureDurationInMs = " + incertitudeExposureDurationInMs + "ms");

			BigDecimal illuminanceDuration = BigDecimal.valueOf(exposureDurationInMs * illuminancePercentage);
			
			// we are interested by the values when the light is increasing or decreasing
			// skip first measurement because it can't be compared to the previous
			if (illuminancePercentage > 0.2 && illuminancePercentage < 0.8  && nbMeasurement > 1) {
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

		if (!timesPpsStart.isEmpty()) {
			BigDecimal averageTimePpsStart = BigDecimalUtils.average(timesPpsStart, 1);
			BigDecimal rmsTimePpsStart = BigDecimalUtils.rootMeanSquare(timesPpsStart, averageTimePpsStart, 1);
			BigDecimal uncertaintyPpsStart = BigDecimalUtils.uncertainty(rmsTimePpsStart, timesPpsStart.size());
			
			objectResult.setAverageTimePpsStart(averageTimePpsStart);
			objectResult.setRmsTimePpsStart(rmsTimePpsStart);
			objectResult.setTimesPpsStart(timesPpsStart);
			objectResult.setUncertaintyPpsStart(uncertaintyPpsStart);
		}

		if (!timesPpsEnd.isEmpty()) {
			BigDecimal averageTimePpsEnd = BigDecimalUtils.average(timesPpsEnd, 1);
			BigDecimal rmsTimePpsEnd = BigDecimalUtils.rootMeanSquare(timesPpsEnd, averageTimePpsEnd, 1);
			BigDecimal uncertaintyPpsEnd = BigDecimalUtils.uncertainty(rmsTimePpsEnd, timesPpsEnd.size());
			
			objectResult.setAverageTimePpsEnd(averageTimePpsEnd);
			objectResult.setRmsTimePpsEnd(rmsTimePpsEnd);
			objectResult.setTimesPpsEnd(timesPpsEnd);
			objectResult.setUncertaintyPpsEnd(uncertaintyPpsEnd);
		}
		
		return objectResult;
	}
	
	private LinearTrend estimateLinearTrend(List<Delay> delays) {
		List<BigDecimal> listyPositions = new ArrayList<>();
		List<BigDecimal> listDelays = new ArrayList<>();
		int scale = 3;
		
		for (Delay delay : delays) {
			listyPositions.add(delay.getyPosition());
			listDelays.add(delay.getDelay());
		}
		
		BigDecimal a = estimateA(listyPositions, listDelays, scale);
		BigDecimal b = estimateB(a, listyPositions, listDelays, scale);
		
		return new LinearTrend(a, b);
	}

	private BigDecimal estimateA(List<BigDecimal> x, List<BigDecimal> y, int scale) {
		BigDecimal avgX = BigDecimalUtils.average(x, scale);
	    BigDecimal avgY = BigDecimalUtils.average(y, scale);
		
		BigDecimal covariance = BigDecimalUtils.covariance(x, avgX, y, avgY, scale);
		BigDecimal variance = BigDecimalUtils.variance(x, avgX);
		
		return covariance.divide(variance, scale, RoundingMode.HALF_UP);
	}
	
	private BigDecimal estimateB(BigDecimal a, List<BigDecimal> x, List<BigDecimal> y, int scale) {
		BigDecimal avgX = BigDecimalUtils.average(x, scale);
		BigDecimal avgY = BigDecimalUtils.average(y, scale);
		
		return avgY.subtract(a.multiply(avgX));
	}
}
