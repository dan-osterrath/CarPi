package carpi.event;

import java.util.List;

import carpi.model.GPSPathElement;
import carpi.model.GPSTrack;

/**
 * Event when the GPS track changed.
 * 
 * @author osterrath
 *
 */
public class GPSTrackChangeEvent {
	/**
	 * GPS track.
	 */
	private GPSTrack track;

	/**
	 * Raw GPS path.
	 */
	private List<GPSPathElement> path;

	/**
	 * 
	 * Ctor.
	 *
	 * @param track
	 *            new track
	 */
	public GPSTrackChangeEvent(GPSTrack track) {
		super();
		this.track = track;
	}

	/**
	 * Ctor.
	 *
	 * @param path
	 *            raw path elements
	 */
	public GPSTrackChangeEvent(List<GPSPathElement> path) {
		super();
		this.path = path;
	}

	/**
	 * Getter method for the field "track".
	 *
	 * @return the track
	 */
	public GPSTrack getTrack() {
		if (track == null && path != null) {
			track = new GPSTrack(path);
		}
		return track;
	}

}
