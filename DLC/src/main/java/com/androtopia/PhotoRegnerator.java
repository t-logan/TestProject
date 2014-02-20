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
	private File startDirectory;
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
		startDirectory = new File(props.getProperty("image.dir"));
		out.println("Running ...");
		walk(startDirectory, results);
		regen();
		out.println("Done!");
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
			out.println(file.getName());
			file.delete();
			results.add(file);
		}
	}

	private void regen() throws IOException {

		for (int i = 0; i < 11; i++) {
			FileUtils
					.copyFile(
							new File(startDirectory + "\\"
									+ props.getProperty("image")), new File(
									startDirectory + "\\" + "photo" + i
											+ ".jpg"));
		}

	}
}
