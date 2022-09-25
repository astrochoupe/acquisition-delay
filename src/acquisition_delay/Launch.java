package acquisition_delay;

public class Launch {

	public static void main(String[] args) {
		String filename = "lightCurve.csv";
		int exposureDurationInMs = 40;
		
		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		acquisitionDelay.calculate(filename, exposureDurationInMs);
	}

}
