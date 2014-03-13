package com.androtopia;

import static java.lang.System.out;
import junit.framework.Assert;

import org.junit.Test;

import com.hdf5vxml.RandomGaussianGenerator;

public class TestRandomGaussianGenerator {

	@Test
	public void seededTest() {
		RandomGaussianGenerator generator = new RandomGaussianGenerator(100, 5);
		out.println(generator.getNextScaledGaussian());
	}

	@Test
	public void unseededTest() {
		RandomGaussianGenerator generator = new RandomGaussianGenerator();
		out.println(generator.getNextScaledGaussian(100, 5));
	}

	@Test
	public void badUnseededTest() {
		try {
			RandomGaussianGenerator generator = new RandomGaussianGenerator();
			out.println(generator.getNextScaledGaussian());
		} catch (Exception e) {
			if (!(e instanceof IllegalStateException))
				Assert.fail();
			else
				out.println("Got expected IllegalStateException");

		}
	}
}
