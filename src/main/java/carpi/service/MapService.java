package carpi.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import carpi.config.CarpiConfiguration;
import carpi.model.MapConfiguration;
import carpi.model.MapConfiguration.TilesType;
import carpi.model.StreamedResource;

/**
 * Service for reading tiles of a map from an MBTiles file.<br/>
 * You can download your MBTiles files with vector data from <a href="https://openmaptiles.org/downloads/">https://openmaptiles.org/downloads/</a>.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class MapService {

	/**
	 * Regexp pattern to extract index columns.
	 */
	private final Pattern INDEX_COLUMNS = Pattern.compile(".+ ON .+ \\((.+)\\).*", Pattern.CASE_INSENSITIVE);

	/**
	 * Lock object to access {@link MapService#lastMatchedMapFile}.
	 */
	private final Object LAST_MATCHED_MAP_FILE_LOCK = new Object();

	/**
	 * List of used map files.
	 */
	private List<MapFile> mapFiles;

	/**
	 * Last used map file to speed up map file search.
	 */
	private MapFile lastMatchedMapFile;

	/**
	 * Map configuration for client.
	 */
	private MapConfiguration mapConfig;

	/**
	 * Class logger.
	 */
	@Inject
	private Logger log;

	/**
	 * Application configuration.
	 */
	@Inject
	private CarpiConfiguration config;

	/**
	 * Initializes the map service.
	 */
	@PostConstruct
	void initialize() {
		// open tiles files
		this.mapFiles = Arrays.asList(config.getMBTilesFiles().split(",")).stream() //
				.map(StringUtils::trimToNull) //
				.filter(Objects::nonNull) //
				.map(this::openMBTilesFile) //
				.filter(Objects::nonNull) //
				.collect(Collectors.toList());

		// create map configuration
		this.mapConfig = createMapConfig();
	}

	/**
	 * Opens the MBTiles file with the given name.
	 * 
	 * @param fileName
	 *            MBTile file name
	 * @return map tiles wrapper object
	 */
	private MapFile openMBTilesFile(String fileName) {
		File tilesFile = new File(fileName);
		if (tilesFile.exists() && tilesFile.canRead() && tilesFile.isFile()) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				log.log(Level.SEVERE, "JDBC driver not found", e);
				return null;
			}

			MapFile mapFile = new MapFile();
			try {
				mapFile.sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
				mapFile.psTileData = mapFile.sqlConnection.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?");

				// check indices
				checkIndices(mapFile);

				// read tiles type from metadata table
				readTilesFormat(mapFile);

				// read min and max values from tiles table
				readMinMaxValues(mapFile);

				// get creation data
				mapFile.mapFileDate = new Date(tilesFile.lastModified());

				return mapFile;
			} catch (SQLException e) {
				log.log(Level.WARNING, "Could not open MBTiles file", e);
			}
		} else {
			log.log(Level.WARNING, "Could not open MBTiles file {0}", fileName);
		}

		return null;
	}

	/**
	 * Checks if the map file has the suggested indizes and creates them if not.
	 * 
	 * @param mapFile
	 *            map file to validate
	 */
	private void checkIndices(MapFile mapFile) {
		Statement stmt = null;
		ResultSet rs = null;
		boolean hasZoomIndex = false;
		boolean hasTileIndex = false;
		try {
			stmt = mapFile.sqlConnection.createStatement();
			rs = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type='index' AND tbl_name='tiles'");
			while (rs.next()) {
				String sql = rs.getString(1);
				Matcher m = INDEX_COLUMNS.matcher(sql);
				if (m.matches()) {
					Set<String> columns = Arrays.asList(m.group(1).split(",")).stream().map(StringUtils::trim).collect(Collectors.toSet());
					if (columns.size() == 1 && columns.contains("zoom_level")) {
						hasZoomIndex = true;
					} else if (columns.size() == 3 && columns.containsAll(Arrays.asList("zoom_level", "tile_row", "tile_column"))) {
						hasTileIndex = true;
					}
				}
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Cold not load tiles indices", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}

		if (!hasZoomIndex) {
			log.log(Level.INFO, "Index for zoom level not found. Creating new...");
			createIndex("CREATE INDEX IF NOT EXISTS idx_tiles_zoom_level ON tiles (zoom_level)", mapFile);
		}
		if (!hasTileIndex) {
			log.log(Level.INFO, "Index for tiles not found. Creating new...");
			createIndex("CREATE UNIQUE INDEX IF NOT EXISTS idx_tiles_tile ON tiles (zoom_level, tile_column, tile_row)", mapFile);
		}
	}

	/**
	 * Creates the index with the given SQL in the given map file.
	 * 
	 * @param sql
	 *            create SQL for index
	 * @param mapFile
	 *            map file
	 */
	private void createIndex(String sql, MapFile mapFile) {
		long start = System.currentTimeMillis();
		Statement stmt = null;
		try {
			stmt = mapFile.sqlConnection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			log.log(Level.WARNING, "Cold not create tiles index", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
		long end = System.currentTimeMillis();
		log.log(Level.INFO, "Creating index took {0}s", (double)(end-start) / 1000.0);
	}

	/**
	 * Reads the tiles format from the database.
	 * 
	 * @param mapFile
	 *            map file to read
	 */
	private void readTilesFormat(MapFile mapFile) {
		Statement stmt = null;
		String format = null;
		ResultSet rs = null;
		try {
			stmt = mapFile.sqlConnection.createStatement();
			rs = stmt.executeQuery("SELECT value FROM metadata WHERE name = 'format'");
			if (rs.next()) {
				format = rs.getString(1);
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Cold not load tiles format", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}

		if (StringUtils.equalsIgnoreCase("png", format)) {
			mapFile.tilesMimeType = "image/png";
			mapFile.tilesFileExtension = ".png";
		} else if (StringUtils.equalsIgnoreCase("pbf", format)) {
			mapFile.tilesMimeType = "application/octet-stream";
			mapFile.tilesFileExtension = ".pbf";
		} else {
			log.log(Level.WARNING, "Unknown MBTiles format: {0}", format);
			mapFile.tilesMimeType = "application/octet-stream";
			mapFile.tilesFileExtension = "";
		}
	}

	/**
	 * Reads the min and max values from the database.
	 * 
	 * @param mapFile
	 *            map file to read
	 */
	private void readMinMaxValues(MapFile mapFile) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = mapFile.sqlConnection.prepareStatement("SELECT min(zoom_level), max(zoom_level) FROM tiles");
			rs = stmt.executeQuery();
			if (rs.next()) {
				mapFile.minZ = rs.getInt(1);
				mapFile.maxZ = rs.getInt(2);
			}
		} catch (SQLException e) {
			log.log(Level.WARNING, "Cold not load min/max values", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Extracts the client map configuration from the tiles. If there are nno tiles available it will return <code>null</code>.
	 * 
	 * @return map configuration
	 */
	private MapConfiguration createMapConfig() {
		if (mapFiles == null || mapFiles.isEmpty()) {
			return null;
		}

		MapConfiguration c = new MapConfiguration();
		mapFiles.forEach(mf -> {
			c.setMinZoom(c.getMinZoom() > 0 ? Math.min(c.getMinZoom(), mf.minZ) : mf.minZ);
			c.setMaxZoom(c.getMaxZoom() > 0 ? Math.max(c.getMaxZoom(), mf.maxZ) : mf.maxZ);
			MapConfiguration.TilesType t = getTitlesType(mf);
			if (c.getType() == null) {
				c.setType(t);
			} else if (c.getType() != t) {
				if (t != null) {
					log.log(Level.WARNING, "Not all map files have the same tiles type! This will cause trouble on the client!");
				}
			}
		});

		return c;
	}

	/**
	 * Returns the tiles type for the given map file.
	 * 
	 * @param mf
	 *            map file.
	 * @return tiles type
	 */
	private MapConfiguration.TilesType getTitlesType(MapFile mf) {
		if (StringUtils.endsWithIgnoreCase(mf.tilesFileExtension, "png")) {
			return TilesType.PNG;
		} else if (StringUtils.endsWithIgnoreCase(mf.tilesFileExtension, "pbf")) {
			return TilesType.VECTOR;
		} else {
			return null;
		}
	}

	/**
	 * Closes the map service.
	 */
	@PreDestroy
	void destroy() {
		mapFiles.forEach(mapFile -> {
			if (mapFile.psTileData != null) {
				try {
					mapFile.psTileData.close();
				} catch (SQLException e) {
				}
			}
			if (mapFile.sqlConnection != null) {
				try {
					mapFile.sqlConnection.close();
				} catch (SQLException e) {
				}
			}
		});
	}

	/**
	 * Returns the map configuration.
	 * 
	 * @return map configuration
	 */
	public MapConfiguration getMapConfig() {
		return mapConfig;
	}

	/**
	 * Loads the tile with the given coordinates and zoom level as streamed resource.
	 * 
	 * @param z
	 *            zoom level
	 * @param x
	 *            x position
	 * @param y
	 *            y position
	 * @return streamed resource
	 */
	public StreamedResource getTile(int z, int x, int y) {
		// calculate adapted y from MBTiles
		int ay = (int) (Math.pow(2, z) - y - 1);

		byte[] imageData = null;

		// try lst map file first
		MapFile mapFile = null;
		MapFile lastMapFile = null;
		synchronized (LAST_MATCHED_MAP_FILE_LOCK) {
			if (lastMatchedMapFile != null) {
				lastMapFile = lastMatchedMapFile;
			}
		}
		if (lastMapFile != null) {
			imageData = readTileData(lastMapFile, z, x, ay);
		}

		if (imageData != null) {
			mapFile = lastMapFile;
		} else {
			// search other map files
			List<MapFile> mapFiles = getMatchingMapFiles(z, x, ay);
			for (MapFile mf : mapFiles) {
				if (mf == lastMapFile) {
					// already tried
					continue;
				}
				imageData = readTileData(mf, z, x, ay);
				if (imageData != null) {
					// found match
					mapFile = mf;
					synchronized (LAST_MATCHED_MAP_FILE_LOCK) {
						lastMatchedMapFile = mf;
					}
					break;
				}
			}
		}

		// create streamed resource
		final byte[] imageDataFinal = imageData;
		final MapFile mapFileFinal = mapFile;
		return new StreamedResource() {
			@Override
			public InputStream getInputStream() throws IOException {
				if (imageDataFinal != null) {
					return new ByteArrayInputStream(imageDataFinal);
				} else {
					throw new FileNotFoundException();
				}
			}

			@Override
			public Integer getMaxCachingAge() {
				return 24 * 3600; // 1d
			}

			@Override
			public Date getLastModified() {
				return mapFileFinal != null ? mapFileFinal.mapFileDate : null;
			}

			@Override
			public String getMimeType() {
				return mapFileFinal != null ? mapFileFinal.tilesMimeType : null;
			}

			@Override
			public String getFileName() {
				return mapFileFinal != null ? z + "_" + x + "_" + y + mapFileFinal.tilesFileExtension : null;
			}
		};
	}

	/**
	 * Tries to read the given tile from the given map file. If the map file does not contain this tile it will return <code>null</<code>.
	 * 
	 * @param mapFile
	 *            map file
	 * @param z
	 *            zoom level
	 * @param x
	 *            x position
	 * @param y
	 *            y position
	 * @return tile data or <code>null</code>
	 */
	private byte[] readTileData(MapFile mapFile, int z, int x, int y) {
		if (mapFile != null && mapFile.psTileData != null) {
			synchronized (mapFile.psTileData) {
				ResultSet rs = null;
				try {
					mapFile.psTileData.setInt(1, z);
					mapFile.psTileData.setInt(2, x);
					mapFile.psTileData.setInt(3, y);
					rs = mapFile.psTileData.executeQuery();
					if (rs.next()) {
						return rs.getBytes(1);
					}
				} catch (SQLException e) {
					log.log(Level.WARNING, "Cold not load tile data", e);
				} finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (SQLException e) {
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Searches the map file that contains the given tile. If multiple map files contain this tile any of these will be used. If no map file contains this tile, <code>null</code>
	 * will be returned.
	 * 
	 * @param z
	 *            zoom level
	 * @param x
	 *            x position
	 * @param y
	 *            y position
	 * @return map file or <code>null</code>
	 */
	private List<MapFile> getMatchingMapFiles(int z, int x, int y) {
		return mapFiles.stream() //
				.filter(mf -> mf.minZ <= z && mf.maxZ >= z) //
				.collect(Collectors.toList());
	}

	/**
	 * Model wrapper for the connection to a map file.
	 * 
	 * @author osterrath
	 *
	 */
	private class MapFile {
		/**
		 * SQL connection to MBTiles file.
		 */
		private Connection sqlConnection;

		/**
		 * Prepared statement for loading tile data.
		 */
		private PreparedStatement psTileData;

		/**
		 * Date when the map file has been created.
		 */
		private Date mapFileDate;

		/**
		 * Mime type of tiles.
		 */
		private String tilesMimeType;

		/**
		 * File extension of tiles.
		 */
		private String tilesFileExtension;

		/**
		 * Minimum value of Z in tiles file.
		 */
		private int minZ;

		/**
		 * Maximum value of Z in tiles file.
		 */
		private int maxZ;
	}
}
