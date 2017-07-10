package carpi.service;

import carpi.model.CarData;

/**
 * Interface for the service reading current car parameters.
 * 
 * @author osterrath
 *
 */
public interface CarService {
	/**
	 * Returns the current car data.
	 * 
	 * @return car data
	 */
	CarData getCarData();
}
