package net.packsam.carpi.event;

import net.packsam.carpi.model.GPSMetaInfo;

/**
 * Event when the GPS meta infos have been changed.
 * 
 * @author osterrath
 *
 */
public class GPSMetaInfoChangeEvent {
	/**
	 * Meta info.
	 */
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
