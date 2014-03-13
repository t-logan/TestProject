package com.hdf5vxml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.System.out;

import org.apache.commons.io.DirectoryWalker;

public class WorkspaceCleaner extends DirectoryWalker {

	public void cleanWorkingDir() throws IOException, InterruptedException {
		List results = new ArrayList();
		File startDirectory = new File(HDF5vXML.CONFIG.getTargetDir());
		walk(startDirectory, results);
		
		// let deletes complete
		Thread.sleep(10000);
	}

	protected boolean handleDirectory(File directory, int depth,
			Collection results) {
		return true;
	}

	protected void handleFile(File file, int depth, Collection results) throws IOException {

		if (file.getName().endsWith(".xml") || file.getName().endsWith(".xmlc")
				|| file.getName().endsWith(".hdf5a")
				|| file.getName().endsWith(".hdf5b")
				|| file.getName().endsWith(".csv")) {

			if(!file.delete())
				throw new IOException("Unable to clean the workspace.");
		}
	}
}
