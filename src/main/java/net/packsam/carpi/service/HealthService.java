package net.packsam.carpi.service;

import net.packsam.carpi.model.HealthStatus;

/**
 * Interface for reading current CarPi status.
 * 
 * @author osterrath
 *
 */
public interface HealthService {

	/**
	 * Returns the current health status.
	 * 
	 * @return health status
	 */
	HealthStatus getHealthStatus();

}
