package fr.walliang.astronomy.acquisitiondelay.dao;

import java.util.List;

public interface FileReader {

	/**
	 * Parse the file.
	 * 
	 * @return 
	 */
	public List<ObjectInfomation> parseFile();
	
}
