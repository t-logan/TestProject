package com.hdf5vxml;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.hdf5vxml.StatsData;

public class StatsDataTest {
	
	@Test
	public void putTest() {
		StatsData.StatsInfo info = StatsData.INSTANCE.new StatsInfo();
		info.setFileExt("ext");
		info.setNumberOfPhotos(3);
		info.setEmissionsSamples(6);
		info.setSizeOnDiskInBytes(50000);
		info.setBinaryBytes(1500);
		info.setTimeToCreateInMilliseconds(445566);
		info.setTimeToReadInMilliseconds(778899);
		StatsData.INSTANCE.putStatsInfo("ABC", info);
	}

	@Test
	public void getTest() {
		StatsData.StatsInfo info = StatsData.INSTANCE.getStatsInfo("ABC");
		assertTrue(info.getFileExt() == "ext");
		assertTrue(info.getNumberOfPhotos() == 3);
		assertTrue(info.getTimeToReadInMilliseconds() == 778899);
	}

	@Test
	public void csvTest() throws IOException {
		StatsData.INSTANCE.toCsvFile("stats.csv");;
	}
}
