package acquisition_delay;

public class Launch {

	public static void main(String[] args) {
		String filename = "lightCurve.csv";
		int exposureDurationInMs = 40;

		switch (args.length) {
		case 0:
			System.out.println("No argument passed as a parameter.");
			System.out.println("Default value for exposure duration will be used: " + exposureDurationInMs + " ms.");
			System.out.println("Default value for filename will be used: " + filename);
			System.out.print("To use parameters: java acquisition_delay/Launch exposureDurationInMs");
			System.out.println(" or java acquisition_delay/Launch exposureDurationInMs filename");
			break;
		case 1:
		case 2:
			try {
				exposureDurationInMs = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("First argument must be an integer.");
				return;
			}

			if(args.length == 1) {
				System.out.println("No filename argument passed as a parameter.");
				System.out.println("Default value for filename will be used: " + filename);
				System.out.println("To use filename parameter: java acquisition_delay/Launch exposureDurationInMs filename");
				filename = "lightCurve.csv";
			} else {
				filename = args[1];
			}

			break;
		default:
			throw new IllegalArgumentException("Unexpected value:");
		}

		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		acquisitionDelay.calculate(filename, exposureDurationInMs);
	}

}
