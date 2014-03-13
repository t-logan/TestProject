package com.hdf5vxml;

import static java.lang.System.out;

import java.io.FileReader;
import java.util.Properties;

public class RunConfig {

	public static final RunConfig INSTANCE = new RunConfig();

	private int fileCount;
	private int meanRows;
	private int sdRows;
	private int cols;
	private int meanPhotos;
	private int sdPhotos;

	private String targetDir;
	private String photoDir;

	private static final String FILE_COUNT = "file.count";
	private static final String ARRAY_ROW_MEAN = "array.row.mean";
	private static final String ARRAY_ROW_SD = "array.row.sd";
	private static final String ARRAY_COL = "array.col";
	private static final String PHOTO_MEAN = "photo.mean";
	private static final String PHOTO_SD = "photo.sd";
	private static final String PHOTO_DIR = "photo.dir";
	private static final String TARGET_DIR = "target.dir";

	private RunConfig() {
	}

	/**
	 * Initializes the run configuration from the specified properites file.
	 * 
	 * @param propFileName
	 *            the name of the property file to read.
	 * @return true if the run configuration was successfully initialized.
	 */
	public boolean init(String propFileName) {
		try {
			Properties props = new Properties();
			props.load(new FileReader(propFileName));

			if (props.getProperty(FILE_COUNT) == null
					|| props.getProperty(ARRAY_ROW_MEAN) == null
					|| props.getProperty(ARRAY_ROW_SD) == null
					|| props.getProperty(ARRAY_COL) == null
					|| props.getProperty(PHOTO_MEAN) == null
					|| props.getProperty(PHOTO_SD) == null) {
				throw new Exception("Missing properties.");
			}

			fileCount = Integer.parseInt(props.getProperty(FILE_COUNT).trim());
			meanRows = Integer.parseInt(props.getProperty(ARRAY_ROW_MEAN)
					.trim());
			sdRows = Integer.parseInt(props.getProperty(ARRAY_ROW_SD).trim());
			cols = Integer.parseInt(props.getProperty(ARRAY_COL).trim());
			meanPhotos = Integer.parseInt(props.getProperty(PHOTO_MEAN).trim());
			sdPhotos = Integer.parseInt(props.getProperty(PHOTO_SD).trim());

			targetDir = props.getProperty(TARGET_DIR);
			photoDir = props.getProperty(PHOTO_DIR);

			if (targetDir == null || photoDir == null) {
				throw new Exception("Missing properties.");
			}

			// normalize file paths
			targetDir = targetDir.replaceAll("\\\\", "/");
			photoDir = photoDir.replaceAll("\\\\", "/");

			if (!targetDir.endsWith("/"))
				targetDir += "/";
			if (!photoDir.endsWith("/"))
				photoDir += "/";

			return true;
		} catch (Exception e) {
			out.println("Unable to load properties from file: " + propFileName
					+ ", because " + e.getMessage() + ". Quitting.");
			return false;
		}
	}

	public int getFileCount() {
		return fileCount;
	}

	public int getMeanRows() {
		return meanRows;
	}

	public int getSdRows() {
		return sdRows;
	}

	public int getCols() {
		return cols;
	}

	public int getMeanPhotos() {
		return meanPhotos;
	}

	public int getSdPhotos() {
		return sdPhotos;
	}

	public String getTargetDir() {
		return targetDir;
	}

	public String getPhotoDir() {
		return photoDir;
	}

	public String toString() {
		return "fileCount=" + fileCount + ", meanRows=" + meanRows
				+ ", sdRows=" + sdRows + ", cols=" + cols + ", meanPhotos="
				+ meanPhotos + ", sdPhotos=" + sdPhotos + ", targetDir="
				+ targetDir + ", photoDir=" + photoDir;
	}
}
