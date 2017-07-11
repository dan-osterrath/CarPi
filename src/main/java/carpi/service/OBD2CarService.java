package carpi.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_01_20;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_21_40;
import com.github.pires.obd.commands.protocol.AvailablePidsCommand_41_60;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import carpi.config.CarpiConfiguration;
import carpi.model.CarData;
import carpi.threading.CarMonitor;

/**
 * Service for reading cara data from OBD2 dongle.
 * 
 * @author osterrath
 *
 */
@ApplicationScoped
public class OBD2CarService implements CarService {

	/**
	 * Last read car data.
	 */
	private CarData lastCarData;

	/**
	 * Lock object for handling {@link OBD2CarService#lastCarData}.
	 */
	private final Object LAST_CAR_DATA_LOCK = new Object();

	/**
	 * Lock object for handling {@link OBD2CarService#socket}.
	 */
	private final Object SOCKET_LOCK = new Object();

	/**
	 * Executor service for running monitor in background.
	 */
	@Inject
	@CarMonitor
	private ScheduledExecutorService executorService;

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
	 * Task for connecting.
	 */
	private ScheduledFuture<?> connectTask;

	/**
	 * Task for reading data.
	 */
	private ScheduledFuture<?> readTask;

	/**
	 * Socket for communicating with ODB2 dongle.
	 */
	private Socket socket;

	/**
	 * Output stream of socket.
	 */
	private OutputStream out;

	/**
	 * Input stream of socket.
	 */
	private InputStream in;

	/**
	 * Initializes the service.
	 */
	@PostConstruct
	private void initialize() {
		String host = config.getOBD2WLANHost();
		Integer port = config.getOBD2WLANPort();
		if (StringUtils.isNotEmpty(host) && port != null) {
			connectTask = executorService.scheduleAtFixedRate(() -> connect(), 0, 20, TimeUnit.SECONDS);
		}
	}

	/**
	 * Shuts down the service.
	 */
	@PreDestroy
	private void shutdown() {
		synchronized (SOCKET_LOCK) {
			closeConnection();
		}
	}

	/**
	 * Tries to connect to the ODB2 dongle.
	 */
	private void connect() {
		String host = config.getOBD2WLANHost();
		Integer port = config.getOBD2WLANPort();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "Connecting to ODB2 dongle at {0}:{1}", new Object[] { host, port });
		}
		synchronized (SOCKET_LOCK) {
			try {
				// open socket
				socket = new Socket(host, port);
				socket.setKeepAlive(true);
				out = socket.getOutputStream();
				in = socket.getInputStream();

				// initialize socket
				log.log(Level.FINE, "Connected. Initializing...");
				executeCommand(new EchoOffCommand());
				executeCommand(new LineFeedOffCommand());
				executeCommand(new TimeoutCommand(125));
				executeCommand(new SelectProtocolCommand(ObdProtocols.AUTO));

				// get available PIDs
				executeCommand(new AvailablePidsCommand_01_20());
				executeCommand(new AvailablePidsCommand_21_40());
				executeCommand(new AvailablePidsCommand_41_60());
			} catch (IOException e) {
				log.log(Level.FINE, "No connection to ODB2 dongle", e);
				closeConnection();
				return;
			} catch (InterruptedException e) {
				log.log(Level.FINE, "Initializig the connection was interrupted", e);
				closeConnection();
				return;
			}
		}

		connectTask.cancel(false);
		readTask = executorService.scheduleAtFixedRate(() -> readData(), 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Closes the connection to the ODB2 dongle.
	 */
	private void closeConnection() {
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
		IOUtils.closeQuietly(socket);

		in = null;
		out = null;
		socket = null;
	}

	/**
	 * Reads current car data from ODB2 dongle.
	 */
	private void readData() {
		synchronized (SOCKET_LOCK) {
			log.info("Reading data...");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				log.log(Level.INFO, "cancelled", e);
			}

			if (Math.random() > 0.75) {
				log.info("Disconnect!");
				closeConnection();

				readTask.cancel(false);
				connectTask = executorService.scheduleAtFixedRate(() -> connect(), 0, 10, TimeUnit.SECONDS);
			}
		}
	}

	/**
	 * Executes the given command.
	 * 
	 * @param command
	 *            command
	 * @return command result
	 * @throws IOException
	 *             IO error occurred
	 * @throws InterruptedException
	 *             task got interrupted
	 */
	private Object executeCommand(ObdCommand command) throws IOException, InterruptedException {
		try {
			log.log(Level.INFO, "Executing {0}", command.getName());
			command.run(in, out);
			String result = command.getResult();
			String resultUnit = command.getResultUnit();
			String calculatedResult = command.getCalculatedResult();
			Long responseTimeDelay = command.getResponseTimeDelay();
			log.log(Level.INFO, "Result: {0} {1}, calculated result: {2}, response time: {3}", new Object[] { result, resultUnit, calculatedResult, responseTimeDelay });
			return calculatedResult;
		} catch (IOException | InterruptedException e) {
			log.log(Level.INFO, "Received exception", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see carpi.service.CarService#getCarData()
	 */
	@Override
	public CarData getCarData() {
		synchronized (LAST_CAR_DATA_LOCK) {
			return lastCarData;
		}
	}

	/**
	 * Method for implementing @Startup with CDI 1.2.
	 * 
	 * @param init
	 *            init object
	 */
	void onInitApp(@Observes @Initialized(ApplicationScoped.class) Object init) {
	}
}
