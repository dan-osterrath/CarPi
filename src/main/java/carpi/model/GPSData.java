/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.model;

/**
 * Model class for all GPS data.
 * 
 * @author osterrath
 *
 */
public class GPSData {
	/**
	 * Position.
	 */
	private GPSPosition position;

	/**
	 * Meta info.
	 */
	private GPSMetaInfo meta;

	/**
	 * GPS track.
	 */
	private GPSTrack track;

	/**
	 * Getter method for the field "position".
	 *
	 * @return the position
	 */
	public GPSPosition getPosition() {
		return position;
	}

	/**
	 * Setter method for the field "position".
	 *
	 * @param position
	 *            the position to set
	 */
	public void setPosition(GPSPosition position) {
		this.position = position;
	}

	/**
	 * Getter method for the field "meta".
	 *
	 * @return the meta
	 */
	public GPSMetaInfo getMeta() {
		return meta;
	}

	/**
	 * Setter method for the field "meta".
	 *
	 * @param meta
	 *            the meta to set
	 */
	public void setMeta(GPSMetaInfo meta) {
		this.meta = meta;
	}

	/**
	 * Getter method for the field "track".
	 *
	 * @return the track
	 */
	public GPSTrack getTrack() {
		return track;
	}

	/**
	 * Setter method for the field "track".
	 *
	 * @param track
	 *            the track to set
	 */
	public void setTrack(GPSTrack track) {
		this.track = track;
	}

}
