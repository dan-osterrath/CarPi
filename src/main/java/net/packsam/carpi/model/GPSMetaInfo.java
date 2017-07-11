package net.packsam.carpi.model;

/**
 * Model class for current GPS meta information.
 * 
 * @author osterrath
 *
 */
public class GPSMetaInfo {
	/**
	 * Number of used satellites.
	 */
	private int numSatellites;

	/**
	 * Getter method for the field "numSatellites".
	 *
	 * @return the numSatellites
	 */
	public int getNumSatellites() {
		return numSatellites;
	}

	/**
	 * Setter method for the field "numSatellites".
	 *
	 * @param numSatellites
	 *            the numSatellites to set
	 */
	public void setNumSatellites(int numSatellites) {
		this.numSatellites = numSatellites;
	}

}
