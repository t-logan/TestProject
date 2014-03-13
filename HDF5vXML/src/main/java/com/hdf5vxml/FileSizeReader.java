package com.hdf5vxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;

public class FileSizeReader extends DirectoryWalker {

	public void updateFileSizes() throws IOException {
		List results = new ArrayList();
		File startDirectory = new File(HDF5vXML.CONFIG.getTargetDir());
		walk(startDirectory, results);
	}

	protected boolean handleDirectory(File directory, int depth,
			Collection results) {
		return true;
	}

	protected void handleFile(File file, int depth, Collection results) {

		if (file.getName().endsWith(".xml") || file.getName().endsWith(".xmlc")
				|| file.getName().endsWith(".hdf5a")
				|| file.getName().endsWith(".hdf5b")) {

			HDF5vXML.DATA.setSizeOnDiskInBytes(file.getName(), file.length());
			results.add(file);
		}
	}
}
