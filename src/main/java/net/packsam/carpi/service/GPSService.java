package net.packsam.carpi.service;

import net.packsam.carpi.model.GPSMetaInfo;
import net.packsam.carpi.model.GPSPosition;

/**
 * Interface for the services reading curent GPS status and position.
 *
 * @author osterrath
 *
 */
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
