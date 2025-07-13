package fr.walliang.astronomy.acquisitiondelay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

/**
 * Provides utility methods for {@link BigDecimal} instances.
 */
public class BigDecimalUtils {

	/**
	 * <code>BigDecimalUtils</code> should not normally be instantiated.
	 */
	private BigDecimalUtils() {
	}

	/**
	 * Calculate the average of a list of BigDecimal.
	 * 
	 * @param list  a list of BigDecimal
	 * @param scale scale of the BigDecimal quotient to be returned
	 * @return the average of the list passed as parameter.
	 */
	public static BigDecimal average(List<BigDecimal> list, int scale) {
		BigDecimal[] totalWithCount = list.stream().filter(bd -> bd != null)
				.map(bd -> new BigDecimal[] { bd, BigDecimal.ONE })
				.reduce((a, b) -> new BigDecimal[] { a[0].add(b[0]), a[1].add(BigDecimal.ONE) }).get();
		return totalWithCount[0].divide(totalWithCount[1], scale, RoundingMode.HALF_UP);
	}

	/**
	 * Calculate the root mean square of a list of BigDecimal.
	 * 
	 * @param list  a list of BigDecimal
	 * @param mean  the average of the list
	 * @param scale scale of the BigDecimal quotient to be returned
	 * @return zero if list is null or empty or if mean if null
	 */
	public static BigDecimal rootMeanSquare(List<BigDecimal> list, BigDecimal mean, int scale) {
		if (list != null && !list.isEmpty() && mean != null) {
			BigDecimal variance = variance(list, mean);
			BigDecimal rootMeanSquare = variance.divide(BigDecimal.valueOf(list.size()), scale, RoundingMode.HALF_UP);
			return rootMeanSquare;
		} else {
			return BigDecimal.ZERO;
		}
	}

	/**
	 * Calculate the variance of a list of BigDecimal.
	 * 
	 * @param list a list of BigDecimal
	 * @param mean the average of the list
	 * @return zero if list is null or empty or if mean if null
	 */
	public static BigDecimal variance(List<BigDecimal> list, BigDecimal mean) {
		BigDecimal variance = BigDecimal.ZERO;

		if (list != null && !list.isEmpty() && mean != null) {
			for (BigDecimal number : list) {
				variance = variance.add(number.subtract(mean).pow(2));
			}
		}

		return variance;
	}

	/**
	 * Return a String that represent a list of BigDecimal.
	 * 
	 * @param list  a list of BigDecimal to print
	 * @param scale scale of the BigDecimal quotient to be returned
	 * @return String the String reprensenting the list of BigDecimal
	 */
	public static String toString(List<BigDecimal> list, int scale) {
		StringBuffer result = new StringBuffer();
		result.append("[");

		Iterator<BigDecimal> iterator = list.iterator();
		while (iterator.hasNext()) {
			BigDecimal bigDecimal = iterator.next();
			result.append(bigDecimal.setScale(scale, RoundingMode.HALF_UP));
			if (iterator.hasNext()) {
				result.append(", ");
			}
		}

		result.append("]");
		return result.toString();
	}
}
