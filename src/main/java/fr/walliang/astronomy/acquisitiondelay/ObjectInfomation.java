package fr.walliang.astronomy.acquisitiondelay;

import java.util.ArrayList;
import java.util.List;

/**
 * Informations about an object measured by Tangra.
 */
public class ObjectInfomation {
	
	private final List<MeasurePoint> measurePoints = new ArrayList<>();
	
	/**
	 * Y coordinate of the object.
	 * 
	 * Useful for rolling shutter camera.
	 */
	private final int y;

	/**
	 * ObjectInfomation constructor.
	 * 
	 * @param y Y coordinate of the object.
	 */
	public ObjectInfomation(int y) {
		this.y = y;
	}
	
	public void addMeasure(MeasurePoint measurePoint) {
		measurePoints.add(measurePoint);
	}

	public List<MeasurePoint> getMeasures() {
		// TODO return a copy of the list or immutable list to avoid mutability
		return measurePoints;
	}

	public int getY() {
		return y;
	}
	
}
