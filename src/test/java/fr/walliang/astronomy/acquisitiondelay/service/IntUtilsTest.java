package fr.walliang.astronomy.acquisitiondelay.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import fr.walliang.astronomy.acquisitiondelay.service.IntUtils;

class IntUtilsTest {

	@Test
	public void testWithArrayOfLength3() {
		//given
		IntStream intStream = IntStream.of(0, 1, 2);
		
		// when
		int actual = IntUtils.median(intStream);
		
		// then
		int expected = 1;
		assertEquals(expected, actual);
	}
	
	@Test
	public void testWithArrayOfLength2() {
		//given
		IntStream intStream = IntStream.of(0, 1);
		
		// when
		int actual = IntUtils.median(intStream);
		
		// then
		int expected = 1;
		assertEquals(expected, actual);
	}
	
	@Test
	public void testWithArrayOfLength1() {
		//given
		IntStream intStream = IntStream.of(0);
		
		// when
		int actual = IntUtils.median(intStream);
		
		// then
		int expected = 0;
		assertEquals(expected, actual);
	}
	
	@Test
	public void testWithArrayOfLength0() {
		IntStream intStream = IntStream.of();
		
		assertThrows(IllegalArgumentException.class, () -> {
			IntUtils.median(intStream);
		});
	}
	
}
