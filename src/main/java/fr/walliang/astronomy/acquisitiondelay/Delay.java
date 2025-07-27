package fr.walliang.astronomy.acquisitiondelay;

import java.math.BigDecimal;

public class Delay {

	private final BigDecimal delay;
	
	private final BigDecimal yPosition;

	public Delay(BigDecimal delay, BigDecimal yPosition) {
		super();
		this.delay = delay;
		this.yPosition = yPosition;
	}

	public BigDecimal getDelay() {
		return delay;
	}

	public BigDecimal getyPosition() {
		return yPosition;
	}
	
}
