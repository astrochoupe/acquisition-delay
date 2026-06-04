package fr.walliang.astronomy.acquisitiondelay.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.walliang.astronomy.acquisitiondelay.service.AcquisitionDelay;

/**
 * Command line interface to run the program.
 */
public class Cli {

	private static final Logger LOGGER = LogManager.getLogger();

	public static void main(String[] args) {
		int exposureDurationInMs = 40;
		String filename = "lightCurve.csv";
		int yPosition = 0;

		switch (args.length) {
		case 0:
			LOGGER.info("No argument passed as a parameter.");
			LOGGER.info("Default value for exposure duration will be used: {} ms.", exposureDurationInMs);
			LOGGER.info("Default value for filename will be used: {}", filename);
			LOGGER.info("Default value for Y position will be used: {}", yPosition);
			LOGGER.info("To use parameters: java acquisition_delay/Launch exposureDurationInMs or java acquisition_delay/Launch exposureDurationInMs filename or java acquisition_delay/Launch exposureDurationInMs filename YPosition");
			break;
		case 1:
		case 2:
		case 3:
			try {
				exposureDurationInMs = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				LOGGER.error("First argument must be an integer.", e);
				return;
			}

			if(args.length == 1) {
				LOGGER.info("No filename argument passed as a parameter.");
				LOGGER.info("Default value for filename will be used: {}", filename);
				LOGGER.info("To use filename parameter: java acquisition_delay/Launch exposureDurationInMs filename");
				filename = "lightCurve.csv";
			} else {
				filename = args[1];
			}
			
			if(args.length == 2) {
				LOGGER.info("No Y position argument passed as a parameter.");
				LOGGER.info("Default value for Y position will be used: {}", yPosition);
			} else if (args.length >= 3) {
				try {
					yPosition = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					LOGGER.error("Third argument (Y position) must be an integer.", e);
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
