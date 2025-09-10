package fr.walliang.astronomy.acquisitiondelay;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ObjectResult {

	BigDecimal averageTimePpsStart;

	BigDecimal rmsTimePpsStart;
	
	BigDecimal uncertaintyPpsStart;

	BigDecimal averageTimePpsEnd;

	BigDecimal rmsTimePpsEnd;
	
	BigDecimal uncertaintyPpsEnd;

	List<BigDecimal> timesPpsStart = new ArrayList<>();

	List<BigDecimal> timesPpsEnd = new ArrayList<>();

	public ObjectResult() {
		super();
	}

	public BigDecimal getAverageTimePpsStart() {
		return averageTimePpsStart;
	}

	public void setAverageTimePpsStart(BigDecimal averageTimePpsStart) {
		this.averageTimePpsStart = averageTimePpsStart;
	}

	public BigDecimal getRmsTimePpsStart() {
		return rmsTimePpsStart;
	}

	public void setRmsTimePpsStart(BigDecimal rmsTimePpsStart) {
		this.rmsTimePpsStart = rmsTimePpsStart;
	}

	public BigDecimal getAverageTimePpsEnd() {
		return averageTimePpsEnd;
	}

	public void setAverageTimePpsEnd(BigDecimal averageTimePpsEnd) {
		this.averageTimePpsEnd = averageTimePpsEnd;
	}

	public BigDecimal getRmsTimePpsEnd() {
		return rmsTimePpsEnd;
	}

	public void setRmsTimePpsEnd(BigDecimal rmsTimePpsEnd) {
		this.rmsTimePpsEnd = rmsTimePpsEnd;
	}

	public List<BigDecimal> getTimesPpsStart() {
		return timesPpsStart;
	}

	public void setTimesPpsStart(List<BigDecimal> timesPpsStart) {
		this.timesPpsStart = timesPpsStart;
	}

	public List<BigDecimal> getTimesPpsEnd() {
		return timesPpsEnd;
	}

	public void setTimesPpsEnd(List<BigDecimal> timesPpsEnd) {
		this.timesPpsEnd = timesPpsEnd;
	}

	public BigDecimal getUncertaintyPpsStart() {
		return uncertaintyPpsStart;
	}

	public void setUncertaintyPpsStart(BigDecimal uncertaintyPpsStart) {
		this.uncertaintyPpsStart = uncertaintyPpsStart;
	}

	public BigDecimal getUncertaintyPpsEnd() {
		return uncertaintyPpsEnd;
	}

	public void setUncertaintyPpsEnd(BigDecimal uncertaintyPpsEnd) {
		this.uncertaintyPpsEnd = uncertaintyPpsEnd;
	}
	
	@Override
	public String toString() {
		if (timesPpsStart.isEmpty() && timesPpsEnd.isEmpty()) {
			return "No PPS detected!";
		}
		
		StringBuilder result = new StringBuilder();
		
		if (!timesPpsStart.isEmpty()) {
			result.append("List of times PPS start: ");
			result.append(BigDecimalUtils.toString(timesPpsStart, 1));
			result.append("\n");
			result.append("Average time PPS start: " + averageTimePpsStart + " ms ± " + uncertaintyPpsStart + " (sigma = " + rmsTimePpsStart + ")");
			result.append("\n");
		}

		if (!timesPpsEnd.isEmpty()) {
			result.append("List of times PPS end: ");
			result.append(BigDecimalUtils.toString(timesPpsEnd, 1));
			result.append("\n");
			result.append("Average time PPS end: " + averageTimePpsEnd + " ms ± " + uncertaintyPpsEnd + " (sigma = " + rmsTimePpsEnd + ")");
			result.append("\n");
		}
		
		return result.toString();
	}
	
	

}
