package fr.walliang.astronomy.acquisitiondelay;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class BigDecimalUtilsTest {

	@Test
	public void testAverageWhenNoValue() {
		List<BigDecimal> list = new ArrayList<>();
		
		assertThrows(NoSuchElementException.class, () -> {
			BigDecimalUtils.average(list, 0);
		});
	}

}
