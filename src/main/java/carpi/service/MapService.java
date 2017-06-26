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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import carpi.config.CarpiConfiguration;
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
		File tilesFile = new File(config.getMBTilesFile());
		if (tilesFile.exists() && tilesFile.canRead() && tilesFile.isFile()) {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				log.log(Level.SEVERE, "JDBC driver not found", e);
				return;
			}
			try {
				sqlConnection = DriverManager.getConnection("jdbc:sqlite:" + config.getMBTilesFile());
				psTileData = sqlConnection.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?;");

				// read tiles type from metadata table
				String format = readTilesFormat();

				if (StringUtils.equalsIgnoreCase("png", format)) {
					tilesMimeType = "image/png";
					tilesFileExtension = ".png";
				} else if (StringUtils.equalsIgnoreCase("pbf", format)) {
					tilesMimeType = "application/octet-stream";
					tilesFileExtension = ".pbf";
				} else {
					log.log(Level.WARNING, "Unknown MBTiles format: {0}", format);
					tilesMimeType = "application/octet-stream";
					tilesFileExtension = "";
				}

				// get creation data
				mapFileDate = new Date(tilesFile.lastModified());
			} catch (SQLException e) {
				log.log(Level.WARNING, "Could not open MBTiles file", e);
			}
		}
	}

	/**
	 * Reads the tiles format from the database.
	 * 
	 * @return tiles format or <code>null</code> if none found
	 */
	private String readTilesFormat() {
		PreparedStatement stmt = null;
		String format = null;
		ResultSet rs = null;
		try {
			stmt = sqlConnection.prepareStatement("SELECT value FROM metadata WHERE name = ?");
			stmt.setString(1, "format");
			rs = stmt.executeQuery();
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
		return format;
	}

	/**
	 * Closes the map service.
	 */
	@PreDestroy
	void destroy() {
		if (psTileData != null) {
			try {
				psTileData.close();
			} catch (SQLException e) {
			}
		}
		if (sqlConnection != null) {
			try {
				sqlConnection.close();
			} catch (SQLException e) {
			}
		}
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
		byte[] imageData = null;
		if (psTileData != null) {
			synchronized (psTileData) {
				ResultSet rs = null;
				try {
					psTileData.setInt(1, z);
					psTileData.setInt(2, x);
					psTileData.setInt(3, y);
					rs = psTileData.executeQuery();
					if (rs.next()) {
						imageData = rs.getBytes(1);
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

		final byte[] imageDataFinal = imageData;
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
				return mapFileDate;
			}

			@Override
			public String getMimeType() {
				return tilesMimeType;
			}

			@Override
			public String getFileName() {
				return z + "_" + x + "_" + y + tilesFileExtension;
			}
		};
	}
}
