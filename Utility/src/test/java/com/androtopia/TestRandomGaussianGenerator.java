package com.androtopia;

import static java.lang.System.out;
import junit.framework.Assert;

import org.junit.Test;

public class TestRandomGaussianGenerator {

	@Test
	public void seededTest() {
		RandomGaussianGenerator generator = new RandomGaussianGenerator(100, 5);
		out.println(generator.getNextGaussian());
	}

	@Test
	public void unseededTest() {
		RandomGaussianGenerator generator = new RandomGaussianGenerator();
		out.println(generator.getNextGaussian(100, 5));
	}

	@Test
	public void badUnseededTest() {
		try {
			RandomGaussianGenerator generator = new RandomGaussianGenerator();
			out.println(generator.getNextGaussian());
		} catch (Exception e) {
			if (!(e instanceof IllegalStateException))
				Assert.fail();
			else
				out.println("Got expected IllegalStateException");

		}
	}
}
