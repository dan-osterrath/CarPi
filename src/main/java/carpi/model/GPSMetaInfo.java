/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.model;

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
