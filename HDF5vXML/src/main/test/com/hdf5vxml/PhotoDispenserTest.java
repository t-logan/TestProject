package com.hdf5vxml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class PhotoDispenserTest {
	
	private static PhotoDispenser PHOTOS = PhotoDispenser.INSTANCE;
	private static RunConfig CONFIG = RunConfig.INSTANCE;
	
	@Test
	public void getCount() throws IOException {
		RunConfig.INSTANCE.init(System.getProperty("prop.file"));
		PhotoDispenser.INSTANCE.init();
		assertTrue(PhotoDispenser.INSTANCE.getCountOfPhotos() == Integer.parseInt(System.getProperty("file.count")));
	}
	
	@Test
	public void top() throws IOException {
		RunConfig.INSTANCE.init(System.getProperty("prop.file"));
		PhotoDispenser.INSTANCE.init();
		
		PhotoDispenser.INSTANCE.next();
		PhotoDispenser.INSTANCE.next();
		PhotoDispenser.INSTANCE.top();
		
		assertTrue(PhotoDispenser.INSTANCE.next().equals(System.getProperty("first.file")));
	}
	
	@Test
	public void cycle() throws IOException {
		RunConfig.INSTANCE.init(System.getProperty("prop.file"));
		PhotoDispenser.INSTANCE.init();
		
		int fileCount = Integer.parseInt(System.getProperty("file.count"));
		PhotoDispenser.INSTANCE.top();
		
		for(int i = 0; i < fileCount; i++) {
			// go to the button
			PhotoDispenser.INSTANCE.next();
		}
		assertTrue(PhotoDispenser.INSTANCE.next().equals(System.getProperty("first.file")));
	}
}
