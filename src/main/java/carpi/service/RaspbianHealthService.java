package carpi.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import carpi.config.CarpiConfiguration;
import carpi.event.HealthStatusChangeEvent;
import carpi.model.HealthStatus;
import carpi.threading.HealthMonitor;

/**
 * Service for reading host health status.
 *
 * @author osterrath
 *
 */
@ApplicationScoped
public class RaspbianHealthService implements HealthService {
	/**
	 * Pattern for extracting the double value from the vcgencmd output.
	 */
	private final static Pattern VCGENCMD_VALUE_MATCHER = Pattern.compile("([0-9]*\\.?[0-9]*)[^0-9]*.*");

	/**
	 * Currently found health status.
	 */
	private final HealthStatus currentHealthStatus = new HealthStatus();

	/**
	 * Flag if this is a Linux system.
	 */
	private final boolean isLinux;

	/**
	 * Process builder for executing mpstat.
	 */
	private ProcessBuilder mpstatPB;

	/**
	 * Process builder for executing vcgencmd to read GPU temperature.
	 */
	private ProcessBuilder vcgencmdGPUTemperaturePB;

	/**
	 * Process builder for executing vcgencmd to read CPU voltage.
	 */
	private ProcessBuilder vcgencmdCPUVoltagePB;

	/**
	 * Process builder for executing lifepo4wered-cli to read input voltage.
	 */
	private ProcessBuilder lifepo4weredCliInputVoltagePB;

	/**
	 * Process builder for executing lifepo4wered-cli to read battery voltage.
	 */
	private ProcessBuilder lifepo4weredCliBatteryVoltagePB;

	/**
	 * Process builder for executing free.
	 */
	private ProcessBuilder freePB;

	/**
	 * Process builder for executing df.
	 */
	private ProcessBuilder dfPB;

	/**
	 * File for reading load average.
	 */
	private File loadAvgFile;

	/**
	 * File for reading CPU temperature.
	 */
	private File cpuTemperatureFile;

	/**
	 * Event when the health status has been changed.
	 */
	@Inject
	private Event<HealthStatusChangeEvent> healthStatusChangeEvent;

	/**
	 * Executor service for running monitor in background.
	 */
	@Inject
	@HealthMonitor
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
	 * Ctor.
	 *
	 */
	public RaspbianHealthService() {
		isLinux = StringUtils.equalsIgnoreCase(System.getProperty("os.name"), "Linux");
	}

	/**
	 * Initializes the service.
	 */
	@PostConstruct
	private void initialize() {
		if (isLinux) {
			mpstatPB = new ProcessBuilder(config.getMpstatPath());
			vcgencmdGPUTemperaturePB = new ProcessBuilder(config.getVcgencmdPath(), "measure_temp");
			vcgencmdCPUVoltagePB = new ProcessBuilder(config.getVcgencmdPath(), "measure_volts");
			freePB = new ProcessBuilder(config.getFreePath(), "-b");
			dfPB = new ProcessBuilder(config.getDfPath(), "-l", "-T", "-BK", "/");
			lifepo4weredCliInputVoltagePB = new ProcessBuilder(config.getLifepo4weredCliPath(), "get", "vin");
			lifepo4weredCliBatteryVoltagePB = new ProcessBuilder(config.getLifepo4weredCliPath(), "get", "vbat");
			loadAvgFile = new File(config.getLoadavgPath());
			cpuTemperatureFile = new File(config.getCPUTemperaturePath());
		}

		executorService.scheduleAtFixedRate(() -> readCPUUtilisation(), 0, 10, TimeUnit.SECONDS);
		executorService.scheduleAtFixedRate(() -> readCPUStatus(), 0, 60, TimeUnit.SECONDS);
		executorService.scheduleAtFixedRate(() -> readMemoryUsage(), 0, 60, TimeUnit.SECONDS);
		executorService.scheduleAtFixedRate(() -> readDiscUsage(), 0, 10, TimeUnit.MINUTES);
		executorService.scheduleAtFixedRate(() -> readLifepo4weredStatus(), 5, 10, TimeUnit.SECONDS);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see carpi.service.HealthService#getHealthStatus()
	 */
	@Override
	public HealthStatus getHealthStatus() {
		synchronized (currentHealthStatus) {
			return currentHealthStatus;
		}
	}

	/**
	 * Reads the current CPU usage and system load and updates the health status.
	 */
	private void readCPUUtilisation() {
		log.fine("Reading CPU utilisation");
		double systemLoad = readSystemLoad();
		double cpuUsage = readCPUUsage();

		boolean changed = false;
		synchronized (currentHealthStatus) {
			if (!nearlyEqual(currentHealthStatus.getSystemLoad(), systemLoad)) {
				currentHealthStatus.setSystemLoad(systemLoad);
				changed = true;
			}
			if (!nearlyEqual(currentHealthStatus.getCpuUsage(), cpuUsage)) {
				currentHealthStatus.setCpuUsage(cpuUsage);
				changed = true;
			}
		}

		if (changed) {
			dispatchChangeEvent(currentHealthStatus);
		}
	}

	/**
	 * Reads the CPU status (temperature and voltage) and updates the health status.
	 */
	private void readCPUStatus() {
		log.fine("Reading CPU/GPU status");
		double gpuTemperature = readPiStatus(vcgencmdGPUTemperaturePB);
		double cpuVoltage = readPiStatus(vcgencmdCPUVoltagePB);
		double cpuTemperature = readCPUTemperature();

		boolean changed = false;
		synchronized (currentHealthStatus) {
			if (!nearlyEqual(currentHealthStatus.getGpuTemperature(), gpuTemperature)) {
				currentHealthStatus.setGpuTemperature(gpuTemperature);
				changed = true;
			}
			if (!nearlyEqual(currentHealthStatus.getCpuVoltage(), cpuVoltage)) {
				currentHealthStatus.setCpuVoltage(cpuVoltage);
				changed = true;
			}
			if (!nearlyEqual(currentHealthStatus.getCpuTemperature(), cpuTemperature)) {
				currentHealthStatus.setCpuTemperature(cpuTemperature);
				changed = true;
			}
		}

		if (changed) {
			dispatchChangeEvent(currentHealthStatus);
		}
	}

	/**
	 * Reads the memory usage and updates the health status.
	 */
	private void readMemoryUsage() {
		log.fine("Reading memory usage");
		long[] memInfo = readMemoryInfo();
		long memTotal = memInfo[0];
		long memFree = memInfo[1];

		boolean changed = false;
		synchronized (currentHealthStatus) {
			if (currentHealthStatus.getMemTotal() != memTotal) {
				currentHealthStatus.setMemTotal(memTotal);
				changed = true;
			}
			if (currentHealthStatus.getMemFree() != memFree) {
				currentHealthStatus.setMemFree(memFree);
				changed = true;
			}
		}

		if (changed) {
			dispatchChangeEvent(currentHealthStatus);
		}
	}

	/**
	 * Reads the disc usage and updates the health status.
	 */
	private void readDiscUsage() {
		log.fine("Reading disc usage");
		long[] discInfo = readDiscInfo();
		long discTotal = discInfo[0];
		long discFree = discInfo[1];

		boolean changed = false;
		synchronized (currentHealthStatus) {
			if (currentHealthStatus.getDiscTotal() != discTotal) {
				currentHealthStatus.setDiscTotal(discTotal);
				changed = true;
			}
			if (currentHealthStatus.getDiscFree() != discFree) {
				currentHealthStatus.setDiscFree(discFree);
				changed = true;
			}
		}

		if (changed) {
			dispatchChangeEvent(currentHealthStatus);
		}
	}

	/**
	 * Reads the status from the LiFePO4wered module and updates the health status.
	 */
	private void readLifepo4weredStatus() {
		log.fine("Reading LiFePO4wered status");
		double batteryVoltage = readLifepo4weredBatteryVoltage();
		double inputVoltage = readLifepo4weredInputVoltage();

		boolean changed = false;
		synchronized (currentHealthStatus) {
			if (!nearlyEqual(currentHealthStatus.getBatteryVoltage(), batteryVoltage)) {
				currentHealthStatus.setBatteryVoltage(batteryVoltage);
				changed = true;
			}
			if (!nearlyEqual(currentHealthStatus.getInputVoltage(), inputVoltage)) {
				currentHealthStatus.setInputVoltage(inputVoltage);
				changed = true;
			}
		}

		if (changed) {
			dispatchChangeEvent(currentHealthStatus);
		}
	}

	/**
	 * Reads the system load from /proc/loadavg.
	 * 
	 * @return system load
	 */
	private double readSystemLoad() {
		if (isLinux) {
			// get load average from /proc/loadavg
			double ret = -1;
			BufferedReader r = null;
			try {
				// read 1st line of file
				r = new BufferedReader(new FileReader(loadAvgFile));
				String line = r.readLine();

				// split into parts
				String[] parts = line.split("\\s+");
				if (parts.length >= 3) {
					ret = Double.parseDouble(parts[0]);
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read system load", e);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "Could not parse system load", e);
			} finally {
				closeReader(r);
			}

			return ret;
		}

		// on other OS we fake
		return 0.3;
	}

	/**
	 * Reads the system load from /proc/loadavg.
	 * 
	 * @return system load
	 */
	private double readCPUTemperature() {
		if (isLinux) {
			// get load average from /sys/class/thermal/thermal_zone0/temp
			double ret = -1;
			BufferedReader r = null;
			try {
				// read 1st line of file
				r = new BufferedReader(new FileReader(cpuTemperatureFile));
				String line = StringUtils.trimToEmpty(r.readLine());

				// parse
				ret = Double.parseDouble(line) / 1000;
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read system load", e);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "Could not parse system load", e);
			} finally {
				closeReader(r);
			}

			return ret;
		}

		// on other OS we fake
		return 50.0;
	}

	/**
	 * Reads the CPU usage from mpstat process.
	 * 
	 * @return CPU usage in percent
	 */
	private double readCPUUsage() {
		if (isLinux) {
			// get load average from mpstat
			Process mpstat = null;
			BufferedReader br = null;
			double ret = -1;
			try {
				mpstat = mpstatPB.start();

				// read stdout
				br = new BufferedReader(new InputStreamReader(mpstat.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (StringUtils.containsIgnoreCase(line, "all")) {
						String[] parts = line.split("\\s+");
						String idleString = parts[parts.length - 1];
						ret = 100 - NumberFormat.getInstance().parse(idleString).doubleValue();
					}
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read CPU usage", e);
			} catch (ParseException e) {
				log.log(Level.WARNING, "Could not parse CPU usage", e);
			} finally {
				if (mpstat != null) {
					try {
						mpstat.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return 30.1;
	}

	/**
	 * Reads some Raspberry Pi status from vcgencmd process.
	 * 
	 * @param pb
	 *            process builder for vcgencmd
	 * @return parsed value
	 */
	private double readPiStatus(ProcessBuilder pb) {
		if (isLinux) {
			Process p = null;

			BufferedReader br = null;
			double ret = -1;
			try {
				// get parameter from vcgencmd
				p = pb.start();

				// read stdout
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (StringUtils.contains(line, "=")) {
						String[] parts = line.split("=", 2);
						Matcher matcher = VCGENCMD_VALUE_MATCHER.matcher(parts[1]);
						if (matcher.matches()) {
							ret = NumberFormat.getInstance(Locale.US).parse(matcher.group(1)).doubleValue();
						}
					}
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read vcgencmd data", e);
			} catch (ParseException e) {
				log.log(Level.WARNING, "Could not parse vcgencmd data", e);
			} finally {
				if (p != null) {
					try {
						p.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return 0.5;
	}

	/**
	 * Reads the memory usage from free process.
	 * 
	 * @return memory values (total, free)
	 */
	private long[] readMemoryInfo() {
		if (isLinux) {
			// get memory usage from free
			Process free = null;
			BufferedReader br = null;
			long[] ret = new long[] { -1, -1 };
			try {
				free = freePB.start();

				// read stdout
				br = new BufferedReader(new InputStreamReader(free.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (StringUtils.containsIgnoreCase(line, "Mem:")) {
						String[] parts = line.split("\\s+");
						String totalString = parts[1];
						ret[0] = Long.parseLong(totalString);
					} else if (StringUtils.containsIgnoreCase(line, "buffers/cache")) {
						String[] parts = line.split("\\s+");
						String freeString = parts[3];
						ret[1] = Long.parseLong(freeString);
					}
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read memory usage", e);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "Could not parse memory usage", e);
			} finally {
				if (free != null) {
					try {
						free.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return new long[] { 10 * 1024 * 1024, 3 * 1024 * 1024 };
	}

	/**
	 * Reads the memory usage from free process.
	 * 
	 * @return memory values (total, free)
	 */
	private long[] readDiscInfo() {
		if (isLinux) {
			// get disc usage from df
			Process df = null;
			BufferedReader br = null;
			long[] ret = new long[] { -1, -1 };
			try {
				df = dfPB.start();

				// read stdout
				br = new BufferedReader(new InputStreamReader(df.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					if (StringUtils.containsIgnoreCase(line, "/dev/root")) {
						String[] parts = line.split("\\s+");
						String totalString = parts[2];
						String freeString = parts[4];
						ret[0] = Long.parseLong(totalString.substring(0, totalString.length() - 1)) * 1024;
						ret[1] = Long.parseLong(freeString.substring(0, freeString.length() - 1)) * 1024;
					}
				}
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read disc usage", e);
			} finally {
				if (df != null) {
					try {
						df.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return new long[] { 10 * 1024 * 1024, 3 * 1024 * 1024 };
	}

	/**
	 * Reads the battery voltage from LiFePO4wered.
	 *
	 * @return battery voltage
	 */
	private double readLifepo4weredBatteryVoltage() {
		if (isLinux) {
			// get voltage from lifepo4wered-cli
			Process df = null;
			BufferedReader br = null;
			double ret = -1;
			try {
				df = lifepo4weredCliBatteryVoltagePB.start();

				// read 1st line
				br = new BufferedReader(new InputStreamReader(df.getInputStream()));
				String line = StringUtils.trimToEmpty(br.readLine());

				// parse
				ret = ((double)Long.parseLong(line, 10)) / 1000;
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read battery voltage", e);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "Could not parse battery voltage", e);
			} finally {
				if (df != null) {
					try {
						df.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return 5.0;
	}

	/**
	 * Reads the input voltage from LiFePO4wered.
	 *
	 * @return input voltage
	 */
	private double readLifepo4weredInputVoltage() {
		if (isLinux) {
			// get voltage from lifepo4wered-cli
			Process df = null;
			BufferedReader br = null;
			double ret = -1;
			try {
				df = lifepo4weredCliInputVoltagePB.start();

				// read 1st line
				br = new BufferedReader(new InputStreamReader(df.getInputStream()));
				String line = StringUtils.trimToEmpty(br.readLine());

				// parse
				ret = ((double)Long.parseLong(line, 10)) / 1000;
			} catch (IOException e) {
				log.log(Level.WARNING, "Could not read input voltage", e);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "Could not parse input voltage", e);
			} finally {
				if (df != null) {
					try {
						df.waitFor();
					} catch (InterruptedException e) {
					}
				}
				closeReader(br);
			}

			return ret;
		}

		// on other OS we fake
		return 5.0;
	}

	/**
	 * Compares tow double values and checks if they are nearly equal.
	 * 
	 * @param val1
	 *            value 1
	 * @param val2
	 *            value 2
	 * @return <code>true</code> if both values are nearly equal
	 */
	private boolean nearlyEqual(double val1, double val2) {
		double dist = Math.abs(val1 - val2);
		return dist < 0.1;
	}

	/**
	 * Dispatches the change event.
	 * 
	 * @param healthStatus
	 *            current health status
	 */
	private void dispatchChangeEvent(HealthStatus healthStatus) {
		healthStatusChangeEvent.fire(new HealthStatusChangeEvent(healthStatus));
	}

	/**
	 * Consumes all data from given reader and closes it.
	 * 
	 * @param r
	 *            reader
	 */
	private void closeReader(Reader r) {
		if (r != null) {
			// consume all data from IS
			try {
				IOUtils.readLines(r);
			} catch (IOException e) {
			}
			// close
			IOUtils.closeQuietly(r);
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
