package net.packsam.carpi.service;

/**
 * Helper class for creating some jitter value.
 * 
 * @author osterrath
 *
 */
public class Jitter {
	/**
	 * Returns some random jitter.
	 * 
	 * @param jitterSize
	 *            jitter size
	 * @return random jitter
	 */
	public static double getJitter(double jitterSize) {
		return Math.random() * jitterSize * 2 - jitterSize;
	}

	/**
	 * Returns some random jitter.
	 * 
	 * @param base
	 *            base value
	 * @param jitterSize
	 *            jitter size
	 * @return random jitter
	 */
	public static double getJitter(double base, double jitterSize) {
		return base + getJitter(jitterSize);
	}
}
