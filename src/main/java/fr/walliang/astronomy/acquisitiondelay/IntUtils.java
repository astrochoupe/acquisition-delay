package fr.walliang.astronomy.acquisitiondelay;

import java.util.stream.IntStream;

public class IntUtils {

	public static int median(IntStream intStream) {
		if(intStream == null) {
			throw new IllegalArgumentException("intStream cannot be null");
		}
			
		IntStream intStreamSorted = intStream.sorted();
		int[] ints = intStreamSorted.toArray();
		int length = ints.length;
		
		if(length == 0) {
			throw new IllegalArgumentException("intStream cannot be empty");
		}
		
		if(length % 2 == 0) {
			return ints[length/2];
		} else {
			return ints[length/2];
		}
	}
	
}
