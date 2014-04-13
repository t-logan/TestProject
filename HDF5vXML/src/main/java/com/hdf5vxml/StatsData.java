package com.hdf5vxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatsData {

	public static final StatsData INSTANCE = new StatsData();
	private static final Map<String, StatsInfo> stats = new HashMap<String, StatsInfo>();

	private StatsData() {
	}

	/**
	 * 
	 * @param key
	 * @param info
	 * @throws IllegalAccessException
	 */
	public void createStatsInfo(String key) throws IllegalAccessException {
		if (key == null)
			throw new IllegalArgumentException("key cannot be null.");
		if (stats.get(key) == null) {
			stats.put(key, new StatsInfo());
		} else {
			throw new IllegalAccessException(
					"Key is already present on create: " + key);
		}
	}

	public String getFileExt(String key) {
		return getInfo(key).fileExt;
	}

	public void setFileExt(String key, String fileExt) {
		getInfo(key).fileExt = fileExt;
	}

	public int getNumberOfPhotos(String key) {
		return getInfo(key).numberOfPhotos;
	}

	public void setNumberOfPhotos(String key, int numberOfPhotos) {
		getInfo(key).numberOfPhotos = notNegative(numberOfPhotos);
	}

	public int getDataArrayRows(String key) {
		return getInfo(key).dataArrayRows;
	}

	public void setDataArrayRows(String key, int dataArrayRows) {
		getInfo(key).dataArrayRows = notNegative(dataArrayRows);
	}

	public long getSizeOnDiskInBytes(String key) {
		return getInfo(key).sizeOnDiskInBytes;
	}

	public void setSizeOnDiskInBytes(String key, long sizeOnDiskInBytes) {
		getInfo(key).sizeOnDiskInBytes = notNegative(sizeOnDiskInBytes);
	}

	public int getBinaryBytes(String key) {
		return getInfo(key).binaryBytes;
	}

	public void setBinaryBytes(String key, int binaryBytes) {
		getInfo(key).binaryBytes = notNegative(binaryBytes);
	}

	public long getTimeToCreateInMilliseconds(String key) {
		return getInfo(key).timeToCreateInMilliseconds;
	}

	public void setTimeToCreateInMilliseconds(String key,
			long timeToCreateInMilliseconds) {
		getInfo(key).timeToCreateInMilliseconds = notNegative(timeToCreateInMilliseconds);
	}

	public long getTimeToReadInMilliseconds(String key) {
		return getInfo(key).timeToReadInMilliseconds;
	}

	public void setTimeToReadInMilliseconds(String key,
			long timeToReadInMilliseconds) {
		getInfo(key).timeToReadInMilliseconds = notNegative(timeToReadInMilliseconds);
	}

	private StatsInfo getInfo(String key) {
		StatsInfo info = stats.get(key);
		if (info == null)
			throw new IllegalAccessError("Key not found: " + key);
		else
			return info;
	}

	public void toCsvFile(String fileName) throws IOException {

		File file = new File(fileName);
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		String header = "fileName,fileExt,numberOfPhotos,dataArrayRows,sizeOnDiskInBytes,binaryBytes,"
				+ "timeToCreateInMilliseconds,timeToReadInMilliseconds\n";
		bw.write(header);

		for (String key : stats.keySet()) {
			// ignore the results for the first file
			if (key.contains("File000000.")) {
				continue;
			}
			StatsInfo statsInfo = stats.get(key);
			bw.write(key + "," + statsInfo.fileExt + ","
					+ statsInfo.numberOfPhotos + "," + statsInfo.dataArrayRows
					+ "," + statsInfo.sizeOnDiskInBytes + ","
					+ statsInfo.binaryBytes + ","
					+ statsInfo.timeToCreateInMilliseconds + ","
					+ statsInfo.timeToReadInMilliseconds + "\n");
		}
		bw.close();
	}

	public String toString(String key) {
		StatsInfo statsInfo = stats.get(key);
		if (statsInfo == null) {
			return "key='" + key + "' not found.";
		}
		return "key=" + key + "fileEx=" + statsInfo.fileExt
				+ ",numberOfPhotos=" + statsInfo.numberOfPhotos
				+ ",dataArrayRows=" + statsInfo.dataArrayRows
				+ ",sizeOnDiskInBytes=" + statsInfo.sizeOnDiskInBytes
				+ ",binaryBytes=" + statsInfo.binaryBytes
				+ ",timeToCreateInMilliseconds="
				+ statsInfo.timeToCreateInMilliseconds
				+ ",timeToReadInMilliseconds="
				+ statsInfo.timeToReadInMilliseconds;
	}

	public String toString() {
		throw new RuntimeException("Use toString(String key) method.");
	}

	private class StatsInfo {
		private String fileExt;
		private int numberOfPhotos;
		private int dataArrayRows;
		private long sizeOnDiskInBytes;
		private int binaryBytes;
		private long timeToCreateInMilliseconds;
		private long timeToReadInMilliseconds;
	}

	private int notNegative(int i) {
		if (i < 0)
			throw new IllegalArgumentException(i
					+ " must be a positive number.");
		return i;
	}

	private long notNegative(long l) {
		if (l < 0)
			throw new IllegalArgumentException(l
					+ " must be a positive number.");
		return l;
	}
}