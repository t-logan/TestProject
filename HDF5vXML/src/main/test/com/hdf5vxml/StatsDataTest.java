package com.hdf5vxml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class StatsDataTest {
	
	@Test
	public void putTest() throws IllegalAccessException {
		String key = "ABC";
		StatsData.INSTANCE.createStatsInfo(key);
		StatsData.INSTANCE.setFileExt(key, "ext");
		StatsData.INSTANCE.setNumberOfPhotos(key, 3);
		StatsData.INSTANCE.setEmissionsSamples(key, 6);
		StatsData.INSTANCE.setSizeOnDiskInBytes(key, 50000);
		StatsData.INSTANCE.setBinaryBytes(key, 1500);
		StatsData.INSTANCE.setTimeToCreateInMilliseconds(key, 445566);
		StatsData.INSTANCE.setTimeToReadInMilliseconds(key, 778899);
	}

	@Test
	public void getTest() {
		String key = "ABC";
		assertTrue(StatsData.INSTANCE.getFileExt(key) == "ext");
		assertTrue(StatsData.INSTANCE.getNumberOfPhotos(key) == 3);
		assertTrue(StatsData.INSTANCE.getTimeToReadInMilliseconds(key) == 778899);
	}

	@Test
	public void csvTest() throws IOException {
		StatsData.INSTANCE.toCsvFile("stats.csv");;
	}
}
