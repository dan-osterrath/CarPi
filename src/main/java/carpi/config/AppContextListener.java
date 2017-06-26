/**
 * (C) 2017 by 3m5. Media GmbH. http://www.3m5.de
 */
package carpi.config;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Servlet context listener for initializing/shutting down app.
 * 
 * @author osterrath
 *
 */
@WebListener
public class AppContextListener implements ServletContextListener {

	/**
	 * Class logger.
	 */
	@Inject
	private Logger log;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// deregister JDBC drivers in our own class loader to prevent memory leaks
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					log.fine(String.format("Deregistering JDBC driver %s", driver.getClass().getName()));
					DriverManager.deregisterDriver(driver);
				} catch (SQLException e) {
					log.log(Level.WARNING, String.format("Could not deregiser JDBC driver %s", driver.getClass().getName()), e);
				}
			}
		}
	}
}
