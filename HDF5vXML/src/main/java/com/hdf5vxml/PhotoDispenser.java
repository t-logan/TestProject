package com.hdf5vxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;

/**
 * JPEG images only, for now.
 * 
 */
public class PhotoDispenser extends DirectoryWalker {

	public static final PhotoDispenser INSTANCE = new PhotoDispenser();
	private static final List<String> photos = new ArrayList<String>();
	private int pointer = 0;

	private PhotoDispenser() {
	}

	public synchronized void init() throws IOException {

		if (photos.size() > 0)
			return;
		
		File startDirectory = new File(HDF5vXML.CONFIG.getPhotoDir());
		walk(startDirectory, photos);
		Collections.sort(photos);
	}

	protected boolean handleDirectory(File directory, int depth,
			Collection results) {
		return true;
	}

	protected void handleFile(File file, int depth, Collection photos) throws IOException {
		if (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG")
				|| file.getName().endsWith(".jpeg")
				|| file.getName().endsWith(".JPEG")) {
			photos.add(file.getAbsoluteFile().getCanonicalPath().replaceAll("\\\\", "/"));
		}
	}

	/**
	 * Go to the top of the list of photo data.
	 */
	public synchronized void top() {
		pointer = 0;
	}

	/**
	 * 
	 * @return the name of the next file in natural sequence. Cycles when it get
	 *         to the end of the list of photo files.
	 */
	public synchronized String next() {
		
		// cycle
		if (pointer == photos.size())
			pointer = 0;
		
		return photos.get(pointer++);
	}
	
	public int getCount() {
		return photos.size();
	}
}
