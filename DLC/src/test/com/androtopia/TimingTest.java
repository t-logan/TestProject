package com.androtopia;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimingTest {

	@Test
	public void test() {

		long startTime;
		long endTime = 0;

		synchronized (this) {
			for (int i = 1; i <= 500; i++) {
				startTime = System.currentTimeMillis();
				for (int j = 1; j <= i * 10000; j++) {
					quadraticEquationRootMax(5.5, 6.0, 3.5);
				}
				endTime = System.currentTimeMillis() - startTime;
				System.out.println(i + "," + endTime);
			}
		}
	}

	public static double quadraticEquationRootMax(double a, double b, double c) {
		double root1, root2;
		root1 = (-b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
		root2 = (-b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
		return Math.max(root1, root2);
	}
}
