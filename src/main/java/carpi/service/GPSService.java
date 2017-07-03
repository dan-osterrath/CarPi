package carpi.service;

import carpi.model.GPSMetaInfo;
import carpi.model.GPSPosition;

public interface GPSService {

	/**
	 * Getter method for the field "lastPosition".
	 *
	 * @return the lastPosition
	 */
	GPSPosition getLastPosition();

	/**
	 * Getter method for the field "lastMetaInfo".
	 *
	 * @return the lastMetaInfo
	 */
	GPSMetaInfo getLastMetaInfo();

}
