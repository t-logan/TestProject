package com.androtopia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static java.lang.System.out;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;

public class PhotoRegnerator extends DirectoryWalker {

	private String outputDataPath = "";
	private File imageDir;
	private Properties props;

	public PhotoRegnerator() {
		super();
	}

	public void regenPhotos() throws IOException {

		// load properties
		props = new Properties();
		ClassLoader cl = GenerateCSVFiles.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("dlc.properties");
		props.load(is);
		is.close();

		List results = new ArrayList();
		imageDir = new File(props.getProperty("image.dir"));
		walk(imageDir, results);
		regen();
	}

	// callback routine
	protected boolean handleDirectory(File directory, int depth,
			Collection results) {
		return true;
	}

	// delete existing files routine
	protected void handleFile(File file, int depth, Collection results)
			throws IOException {
		if (file.getName().startsWith("photo")) {
			file.delete();
			results.add(file);
		}
	}

	private void regen() throws IOException {

		for (int i = 0; i < 11; i++) {
			File resDir = new File(props.getProperty("resource.dir"));
			FileUtils.copyDirectory(resDir, imageDir);
		}

	}
}
