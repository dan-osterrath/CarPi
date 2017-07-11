package net.packsam.carpi.service;

import net.packsam.carpi.model.CarData;

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
