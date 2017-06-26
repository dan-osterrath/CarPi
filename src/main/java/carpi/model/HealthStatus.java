package carpi.model;

/**
 * Model class for the current health status.
 * 
 * @author osterrath
 *
 */
public class HealthStatus {
	/**
	 * Current CPU temperature in °C.
	 */
	private double cpuTemperature;
	/**
	 * Current CPU voltage in V.
	 */
	private double cpuVoltage;
	/**
	 * Current CPU usage in percent.
	 */
	private double cpuUsage;
	/**
	 * System load of the last minute.
	 */
	private double systemLoad;
	/**
	 * Free disc space in byte.
	 */
	private long discFree;
	/**
	 * Total disc space in byte.
	 */
	private long discTotal;
	/**
	 * Free memory in byte.
	 */
	private long memFree;
	/**
	 * Total memory in byte.
	 */
	private long memTotal;

	/**
	 * Getter method for the field "cpuTemperature".
	 *
	 * @return the cpuTemperature
	 */
	public double getCpuTemperature() {
		return cpuTemperature;
	}

	/**
	 * Setter method for the field "cpuTemperature".
	 *
	 * @param cpuTemperature
	 *            the cpuTemperature to set
	 */
	public void setCpuTemperature(double cpuTemperature) {
		this.cpuTemperature = cpuTemperature;
	}

	/**
	 * Getter method for the field "cpuVoltage".
	 *
	 * @return the cpuVoltage
	 */
	public double getCpuVoltage() {
		return cpuVoltage;
	}

	/**
	 * Setter method for the field "cpuVoltage".
	 *
	 * @param cpuVoltage
	 *            the cpuVoltage to set
	 */
	public void setCpuVoltage(double cpuVoltage) {
		this.cpuVoltage = cpuVoltage;
	}

	/**
	 * Getter method for the field "cpuUsage".
	 *
	 * @return the cpuUsage
	 */
	public double getCpuUsage() {
		return cpuUsage;
	}

	/**
	 * Setter method for the field "cpuUsage".
	 *
	 * @param cpuUsage
	 *            the cpuUsage to set
	 */
	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	/**
	 * Getter method for the field "systemLoad".
	 *
	 * @return the systemLoad
	 */
	public double getSystemLoad() {
		return systemLoad;
	}

	/**
	 * Setter method for the field "systemLoad".
	 *
	 * @param systemLoad
	 *            the systemLoad to set
	 */
	public void setSystemLoad(double systemLoad) {
		this.systemLoad = systemLoad;
	}

	/**
	 * Getter method for the field "discFree".
	 *
	 * @return the discFree
	 */
	public long getDiscFree() {
		return discFree;
	}

	/**
	 * Setter method for the field "discFree".
	 *
	 * @param discFree
	 *            the discFree to set
	 */
	public void setDiscFree(long discFree) {
		this.discFree = discFree;
	}

	/**
	 * Getter method for the field "discTotal".
	 *
	 * @return the discTotal
	 */
	public long getDiscTotal() {
		return discTotal;
	}

	/**
	 * Setter method for the field "discTotal".
	 *
	 * @param discTotal
	 *            the discTotal to set
	 */
	public void setDiscTotal(long discTotal) {
		this.discTotal = discTotal;
	}

	/**
	 * Getter method for the field "memFree".
	 *
	 * @return the memFree
	 */
	public long getMemFree() {
		return memFree;
	}

	/**
	 * Setter method for the field "memFree".
	 *
	 * @param memFree
	 *            the memFree to set
	 */
	public void setMemFree(long memFree) {
		this.memFree = memFree;
	}

	/**
	 * Getter method for the field "memTotal".
	 *
	 * @return the memTotal
	 */
	public long getMemTotal() {
		return memTotal;
	}

	/**
	 * Setter method for the field "memTotal".
	 *
	 * @param memTotal
	 *            the memTotal to set
	 */
	public void setMemTotal(long memTotal) {
		this.memTotal = memTotal;
	}
}
