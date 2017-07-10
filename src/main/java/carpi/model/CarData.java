package carpi.model;

/**
 * Model object for current car data.
 * 
 * @author osterrath
 *
 */
public class CarData {
	/**
	 * Vehicle identification number.
	 */
	private String vin;

	/**
	 * Speed in km/h.
	 */
	private double speed;

	/**
	 * Fuel level in %.
	 */
	private double fuelLevel;

	/**
	 * Fuel type.
	 */
	private FuelType fuelType;

	/**
	 * Fuel rate in l/h.
	 */
	private double fuelRate;

	/**
	 * Approximated reach in km.
	 */
	private double reach;

	/**
	 * Engine temperature in °C.
	 */
	private double engineTemperature;

	/**
	 * Oil temperature in °C.
	 */
	private double oilTemperature;

	/**
	 * Ambient temperature in °C.
	 */
	private double ambientTemperature;

	/**
	 * Getter method for the field "vin".
	 *
	 * @return the vin
	 */
	public String getVin() {
		return vin;
	}

	/**
	 * Setter method for the field "vin".
	 *
	 * @param vin
	 *            the vin to set
	 */
	public void setVin(String vin) {
		this.vin = vin;
	}

	/**
	 * Getter method for the field "speed".
	 *
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * Setter method for the field "speed".
	 *
	 * @param speed
	 *            the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Getter method for the field "fuelLevel".
	 *
	 * @return the fuelLevel
	 */
	public double getFuelLevel() {
		return fuelLevel;
	}

	/**
	 * Setter method for the field "fuelLevel".
	 *
	 * @param fuelLevel
	 *            the fuelLevel to set
	 */
	public void setFuelLevel(double fuelLevel) {
		this.fuelLevel = fuelLevel;
	}

	/**
	 * Getter method for the field "fuelType".
	 *
	 * @return the fuelType
	 */
	public FuelType getFuelType() {
		return fuelType;
	}

	/**
	 * Setter method for the field "fuelType".
	 *
	 * @param fuelType
	 *            the fuelType to set
	 */
	public void setFuelType(FuelType fuelType) {
		this.fuelType = fuelType;
	}

	/**
	 * Getter method for the field "fuelRate".
	 *
	 * @return the fuelRate
	 */
	public double getFuelRate() {
		return fuelRate;
	}

	/**
	 * Setter method for the field "fuelRate".
	 *
	 * @param fuelRate
	 *            the fuelRate to set
	 */
	public void setFuelRate(double fuelRate) {
		this.fuelRate = fuelRate;
	}

	/**
	 * Getter method for the field "reach".
	 *
	 * @return the reach
	 */
	public double getReach() {
		return reach;
	}

	/**
	 * Setter method for the field "reach".
	 *
	 * @param reach
	 *            the reach to set
	 */
	public void setReach(double reach) {
		this.reach = reach;
	}

	/**
	 * Getter method for the field "engineTemperature".
	 *
	 * @return the engineTemperature
	 */
	public double getEngineTemperature() {
		return engineTemperature;
	}

	/**
	 * Setter method for the field "engineTemperature".
	 *
	 * @param engineTemperature
	 *            the engineTemperature to set
	 */
	public void setEngineTemperature(double engineTemperature) {
		this.engineTemperature = engineTemperature;
	}

	/**
	 * Getter method for the field "oilTemperature".
	 *
	 * @return the oilTemperature
	 */
	public double getOilTemperature() {
		return oilTemperature;
	}

	/**
	 * Setter method for the field "oilTemperature".
	 *
	 * @param oilTemperature
	 *            the oilTemperature to set
	 */
	public void setOilTemperature(double oilTemperature) {
		this.oilTemperature = oilTemperature;
	}

	/**
	 * Getter method for the field "ambientTemperature".
	 *
	 * @return the ambientTemperature
	 */
	public double getAmbientTemperature() {
		return ambientTemperature;
	}

	/**
	 * Setter method for the field "ambientTemperature".
	 *
	 * @param ambientTemperature
	 *            the ambientTemperature to set
	 */
	public void setAmbientTemperature(double ambientTemperature) {
		this.ambientTemperature = ambientTemperature;
	}

	/**
	 * Enumeration for the fuel type.
	 * 
	 * @author osterrath
	 *
	 */
	public static enum FuelType {
		GASOLINE, //
		DIESEL, //
	}
}
