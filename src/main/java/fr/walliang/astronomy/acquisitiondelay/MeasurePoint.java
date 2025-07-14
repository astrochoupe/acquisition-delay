package fr.walliang.astronomy.acquisitiondelay;

/**
 * Class that represent a line in the CSV file.
 */
public class MeasurePoint {
	private int timeInMs;
	private int signalInAdu;
	private int backgroundInAdu;

	public MeasurePoint(int timeInMs, int signalInAdu, int backgroundInAdu) {
		super();
		this.timeInMs = timeInMs;
		this.signalInAdu = signalInAdu;
		this.backgroundInAdu = backgroundInAdu;
	}

	@Override
	public String toString() {
		return "MeasurePoint [timeInMs=" + timeInMs + ", signalInAdu=" + signalInAdu + ", backgroundInAdu="
				+ backgroundInAdu + "]";
	}

	public int getTimeInMs() {
		return timeInMs;
	}

	public int getSignalInAdu() {
		return signalInAdu;
	}

	public int getBackgroundInAdu() {
		return backgroundInAdu;
	}
	
}
