package fr.walliang.astronomy.acquisitiondelay;

import java.util.List;

public interface FileReader {

	/**
	 * Parse the file.
	 * 
	 * @return 
	 */
	public List<ObjectInfomation> parseFile();
	
}
