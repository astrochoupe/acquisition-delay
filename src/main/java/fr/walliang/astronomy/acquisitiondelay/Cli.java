package fr.walliang.astronomy.acquisitiondelay;

/**
 * Command line interface to run the program.
 */
public class Cli {

	public static void main(String[] args) {
		int exposureDurationInMs = 40;
		String filename = "lightCurve.csv";
		int yPosition = 0;

		switch (args.length) {
		case 0:
			System.out.println("No argument passed as a parameter.");
			System.out.println("Default value for exposure duration will be used: " + exposureDurationInMs + " ms.");
			System.out.println("Default value for filename will be used: " + filename);
			System.out.println("Default value for Y position will be used: " + yPosition);
			System.out.print("To use parameters: java acquisition_delay/Launch exposureDurationInMs");
			System.out.println(" or java acquisition_delay/Launch exposureDurationInMs filename");
			System.out.println(" or java acquisition_delay/Launch exposureDurationInMs filename YPosition");
			break;
		case 1:
		case 2:
		case 3:
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
			
			if(args.length == 2) {
				System.out.println("No Y position argument passed as a parameter.");
				System.out.println("Default value for Y position will be used: " + yPosition);
			} else if (args.length >= 3) {
				try {
					yPosition = Integer.parseInt(args[3]);
				} catch (NumberFormatException e) {
					System.err.println("Third argument must be an integer.");
					return;
				}
			}

			break;
		default:
			throw new IllegalArgumentException("Unexpected value:");
		}

		AcquisitionDelay acquisitionDelay = new AcquisitionDelay();
		acquisitionDelay.calculate(filename, exposureDurationInMs, yPosition);
	}

}
