/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.event;

import carpi.model.GPSMetaInfo;

/**
 * Event when the GPS meta infos have been changed.
 * 
 * @author osterrath
 *
 */
public class GPSMetaInfoChangeEvent {
	private final GPSMetaInfo metaInfo;

	/**
	 * Ctor.
	 *
	 * @param metaInfo
	 *            current GPS meta info
	 */
	public GPSMetaInfoChangeEvent(GPSMetaInfo metaInfo) {
		super();
		this.metaInfo = metaInfo;
	}

	/**
	 * Getter method for the field "metaInfo".
	 *
	 * @return the metaInfo
	 */
	public GPSMetaInfo getMetaInfo() {
		return metaInfo;
	}

}
