package carpi.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

/**
 * Bean for configuring the application.
 * 
 * @author osterrath
 */
@ApplicationScoped
public class CarpiConfiguration {
	/**
	 * Properties file name.
	 */
	private static final String PROPERTIES_FILE = "carpi.properties";

	/**
	 * Property key for the GPSd host.
	 */
	private static final String PROP_GPSD_HOST = "gpsd.host";

	/**
	 * Property key for the GPSd port.
	 */
	private static final String PROP_GPSD_PORT = "gpsd.port";

	/**
	 * Property key for the mpstat path.
	 */
	private static final String PROP_MPSTAT_PATH = "mpstat.path";

	/**
	 * Property key for the vcgencmd path.
	 */
	private static final String PROP_VCGENCMD_PATH = "vcgencmd.path";

	/**
	 * Property key for the free path.
	 */
	private static final String PROP_FREE_PATH = "free.path";

	/**
	 * Property key for the df path.
	 */
	private static final String PROP_DF_PATH = "df.path";

	/**
	 * Property key for the loadavg path.
	 */
	private static final String PROP_LOADAVG_PATH = "loadavg.path";

	/**
	 * Property key for the MBTiles file to use for the map.
	 */
	private static final String PROP_MBTILES_FILE = "mbtiles.file";

	/**
	 * Property key for the error threshold for latitude/longitude in m.
	 */
	private static final String PROP_ERROR_THRESHOLD_LONG_LAT = "threshold.error.lat-long";

	/**
	 * Property key for the error threshold for altitude in m.
	 */
	private static final String PROP_ERROR_THRESHOLD_ALT = "threshold.error.alt";

	/**
	 * Property key for the threshold for detecting movement in m.
	 */
	private static final String PROP_MOVEMENT_THRESHOLD = "threshold.movement";

	/**
	 * Property key for the threshold to detect pauses in ms.
	 */
	private static final String PROP_PAUSE_THRESHOLD = "threshold.pause";

	/**
	 * Property key for the arget directory for tracking files.
	 */
	private static final String PROP_TRACKING_TARGET_DIR = "tracks.dir";

	/**
	 * The configuration properties.
	 */
	private final Properties properties = new Properties();

	/**
	 * Class logger.
	 */
	@Inject
	private Logger log;

	/**
	 * Load properties from the properties file.
	 */
	@PostConstruct
	private void loadProperties() {
		File f = getPropertiesFile();
		if (f.exists()) {
			log.log(Level.INFO, "Reading configuration from {0}", f.getAbsolutePath());
			FileReader reader = null;
			try {
				reader = new FileReader(f);
				properties.load(reader);
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read configuration from properties file", e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
		} else {
			log.log(Level.INFO, "No configuration file in {0} found. Using defaults.", f.getAbsolutePath());
		}
	}

	/**
	 * Returns the properties file to read from and write to the configuration.
	 * 
	 * @return properties file
	 */
	private File getPropertiesFile() {
		return new File(PROPERTIES_FILE);
	}

	/**
	 * Returns the host for connecting to GPSd.
	 * 
	 * @return GPSd host
	 */
	public String getGPSdHost() {
		return properties.getProperty(PROP_GPSD_HOST, "localhost");
	}

	/**
	 * Returns the port for connecting to GPSd.
	 * 
	 * @return GPSd port
	 */
	public int getGPSdPort() {
		return Integer.parseInt(properties.getProperty(PROP_GPSD_PORT, "2947"), 10);
	}

	/**
	 * Returns the path to mpstat.
	 * 
	 * @return mpstat path
	 */
	public String getMpstatPath() {
		return properties.getProperty(PROP_MPSTAT_PATH, "/usr/bin/mpstat");
	}

	/**
	 * Returns the path to vcgencmd.
	 * 
	 * @return vcgencmd path
	 */
	public String getVcgencmdPath() {
		return properties.getProperty(PROP_VCGENCMD_PATH, "/usr/bin/vcgencmd");
	}

	/**
	 * Returns the path to free.
	 * 
	 * @return free path
	 */
	public String getFreePath() {
		return properties.getProperty(PROP_FREE_PATH, "/usr/bin/free");
	}

	/**
	 * Returns the path to df.
	 * 
	 * @return df path
	 */
	public String getDfPath() {
		return properties.getProperty(PROP_DF_PATH, "/bin/df");
	}

	/**
	 * Returns the path to loadavg file.
	 * 
	 * @return loadavg path
	 */
	public String getLoadavgPath() {
		return properties.getProperty(PROP_LOADAVG_PATH, "/proc/loadavg");
	}

	/**
	 * Returns the path to the mbtiles file.
	 * 
	 * @return mbtiles path
	 */
	public String getMBTilesFile() {
		return properties.getProperty(PROP_MBTILES_FILE, "carpi.mbtiles");
	}

	/**
	 * Returns the error threshold for latitude or longitude.
	 * 
	 * @return error threshold
	 */
	public double getLongitudeLatitudeErrorThreshold() {
		return Double.parseDouble(properties.getProperty(PROP_ERROR_THRESHOLD_LONG_LAT, "15"));
	}

	/**
	 * Returns the error threshold for latitude or longitude.
	 * 
	 * @return error threshold
	 */
	public double getAltitudeErrorThreshold() {
		return Double.parseDouble(properties.getProperty(PROP_ERROR_THRESHOLD_ALT, "50"));
	}

	/**
	 * Returns the threshold for detecting movement.
	 * 
	 * @return movement threshold
	 */
	public double getMovementThreshold() {
		return Double.parseDouble(properties.getProperty(PROP_MOVEMENT_THRESHOLD, "20"));
	}

	/**
	 * Returns the threshold for detecting pauses.
	 * 
	 * @return pause threshold
	 */
	public long getPauseThreshold() {
		return Long.parseLong(properties.getProperty(PROP_PAUSE_THRESHOLD, "1800000"), 10);
	}

	/**
	 * Returns the target directory for creating tracking files to.
	 * 
	 * @return target directory
	 */
	public String getTrackingTargetDirecory() {
		return properties.getProperty(PROP_TRACKING_TARGET_DIR);
	}
}
