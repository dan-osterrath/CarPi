package carpi.service;

import carpi.model.HealthStatus;

/**
 * Interface for the health service.
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
