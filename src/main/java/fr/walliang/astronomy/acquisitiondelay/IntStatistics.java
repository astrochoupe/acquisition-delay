package fr.walliang.astronomy.acquisitiondelay;

import java.util.List;

/**
 * Class that represent the statistics of a Collection of Integer
 */
public class IntStatistics {
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
	private long count = 0;
	private long sum = 0;
	private double average;
	private double variance;
	private double standardDeviation;
	private double uncertainty;
	
	public IntStatistics(List<Integer> integers) {
		if(integers == null) {
			throw new IllegalArgumentException("integers cannot be null");
		}
		
		for (int integer : integers) {
			++count;
			sum += integer;
	        min = Math.min(min, integer);
	        max = Math.max(max, integer);
		}
		
		// average
		average = (double) sum / count;
		
		
		// variance
		for (int integer : integers) {
			double difference = integer - average;
			double differenceSquared = Math.pow(difference, 2);
			variance += differenceSquared;
		}
		variance = variance / count;
		
		// standard deviation
		standardDeviation = Math.sqrt(variance);
		
		// uncertainty
		int k = 2; // 95%
		double sqrtNbItems = Math.sqrt(count);
		uncertainty = k * standardDeviation / sqrtNbItems;
		
	}

	@Override
	public String toString() {
		return "IntStatistics [min=" + min + ", max=" + max + ", count=" + count + ", sum=" + sum + ", average="
				+ average + ", variance=" + variance + ", standardDeviation=" + standardDeviation + ", uncertainty="
				+ uncertainty + "]";
	}

	public double getAverage() {
		return average;
	}

	public double getUncertainty() {
		return uncertainty;
	}
	
}
