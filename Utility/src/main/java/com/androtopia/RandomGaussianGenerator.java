package com.androtopia;

import java.util.Random;
import static java.lang.System.out;

/**
 * Generate pseudo-random floating point values, with an approximately Gaussian
 * (normal) distribution.
 * 
 * Many physical measurements have an approximately Gaussian distribution; this
 * provides a way of simulating such values.
 */
public final class RandomGaussianGenerator {

	private Double mean = null;
	private Double variance = null;
	private Random generator = null;

	public RandomGaussianGenerator() {
		generator = new Random();
	}

	/**
	 * Seed the Gaussian random number generator when constructed.
	 * 
	 * @param mean
	 *            the "base" average value.
	 * @param variance
	 *            the amount to vary the mean value (+/-)
	 */
	public RandomGaussianGenerator(double mean, double variance) {
		this.mean = mean;
		this.variance = variance;
		generator = new Random();
	}

	/**
	 * Generates the specified number of random Gaussian numbers using the
	 * passed mean and variance.
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		RandomGaussianGenerator self;
		int resultCount;
		if (args.length != 3)
			throw new IllegalArgumentException(
					"arguments: resultCount, mean, variance");
		try {
			self = new RandomGaussianGenerator(Double.parseDouble(args[1]),
					Double.parseDouble(args[2]));
			resultCount = Integer.parseInt(args[0]);
		} catch (Throwable t) {
			throw new IllegalArgumentException(t.getMessage());
		}
		// generate values
		for (int idx = 1; idx <= resultCount; ++idx) {
			out.println(self.getNextGaussian());
		}
	}

	/**
	 * Gets the next pseudo-random floating point value using the seeded mean
	 * and variance.
	 * 
	 * @return a pseudo-random floating point value using the mean and variance
	 *         passed in the constructor.
	 */
	public double getNextGaussian() {
		if (mean == null || variance == null) {
			throw new IllegalStateException(
					"Mean and/or variance not initialized;\nuse RandomGaussianGenerator(double mean, double variance) constructor.");
		}
		return mean + generator.nextGaussian() * variance;
	}

	/**
	 * Gets the next pseudo-random floating point value for the specified mean
	 * and variance.
	 * 
	 * @return a pseudo-random floating point value using the passed mean and
	 *         variance.
	 */
	public double getNextGaussian(double mean, double variance) {
		return mean + generator.nextGaussian() * variance;
	}
}