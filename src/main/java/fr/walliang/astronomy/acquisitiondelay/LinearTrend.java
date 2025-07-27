package fr.walliang.astronomy.acquisitiondelay;

import java.math.BigDecimal;

public class LinearTrend {

	private final BigDecimal a;
	
	private final BigDecimal b;

	public LinearTrend(BigDecimal a, BigDecimal b) {
		super();
		this.a = a;
		this.b = b;
	}

	public BigDecimal getA() {
		return a;
	}

	public BigDecimal getB() {
		return b;
	}
	
	public BigDecimal getY(int x) {
		BigDecimal xBigDecimal = new BigDecimal(x);
		return xBigDecimal.multiply(a).add(b);
	}
	
}
