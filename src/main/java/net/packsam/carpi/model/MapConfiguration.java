package net.packsam.carpi.model;

/**
 * Model class for the map configuration.
 * 
 * @author osterrath
 *
 */
public class MapConfiguration {
	/**
	 * Minimal zoom level.
	 */
	private int minZoom;

	/**
	 * Maximum zoom level.
	 */
	private int maxZoom;

	/**
	 * Tiles type.
	 */
	private TilesType type;

	/**
	 * Flag if we have a GeoJson file.
	 */
	private boolean withGeoJson;

	/**
	 * Getter method for the field "minZoom".
	 *
	 * @return the minZoom
	 */
	public int getMinZoom() {
		return minZoom;
	}

	/**
	 * Setter method for the field "minZoom".
	 *
	 * @param minZoom
	 *            the minZoom to set
	 */
	public void setMinZoom(int minZoom) {
		this.minZoom = minZoom;
	}

	/**
	 * Getter method for the field "maxZoom".
	 *
	 * @return the maxZoom
	 */
	public int getMaxZoom() {
		return maxZoom;
	}

	/**
	 * Setter method for the field "maxZoom".
	 *
	 * @param maxZoom
	 *            the maxZoom to set
	 */
	public void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}

	/**
	 * Getter method for the field "type".
	 *
	 * @return the type
	 */
	public TilesType getType() {
		return type;
	}

	/**
	 * Setter method for the field "type".
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(TilesType type) {
		this.type = type;
	}

	/**
	 * Getter method for the field "withGeoJson".
	 *
	 * @return the withGeoJson
	 */
	public boolean isWithGeoJson() {
		return withGeoJson;
	}

	/**
	 * Setter method for the field "withGeoJson".
	 *
	 * @param withGeoJson
	 *            the withGeoJson to set
	 */
	public void setWithGeoJson(boolean withGeoJson) {
		this.withGeoJson = withGeoJson;
	}

	/**
	 * Type of the map tiles.
	 * 
	 * @author osterrath
	 *
	 */
	public static enum TilesType {
		/**
		 * Tiles are jpeg images.
		 */
		JPEG,
		/**
		 * Tiles are png images.
		 */
		PNG,
		/**
		 * Tiles are vector data objects.
		 */
		VECTOR
	}
}
