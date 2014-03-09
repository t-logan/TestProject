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

	public void putStatsInfo(String key, StatsInfo info) {
		if (key == null || info == null)
			throw new IllegalArgumentException("key/info cannot be null.");
		stats.put(key, info);
	}

	public StatsInfo getStatsInfo(String key) {
		return stats.get(key);
	}

	public void toCsvFile(String fileName) throws IOException {

		File file = new File(fileName);
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		String header = "fileName,fileExt,numberOfPhotos,emissionsSamples,sizeOnDiskInBytes,binaryBytes,"
				+ "timeToCreateInMilliseconds,timeToReadInMilliseconds\n";
		bw.write(header);

		for (String key : stats.keySet()) {
			StatsInfo statsInfo = stats.get(key);
			bw.write(key + "," + statsInfo.getFileExt() + ","
					+ statsInfo.getNumberOfPhotos() + ","
					+ statsInfo.getEmissionsSamples() + ","
					+ statsInfo.getSizeOnDiskInBytes() + ","
					+ statsInfo.getBinaryBytes() + ","
					+ statsInfo.getTimeToCreateInMilliseconds() + ","
					+ statsInfo.getTimeToReadInMilliseconds() + "\n");
		}

		bw.close();
	}

	public class StatsInfo {

		private String fileExt;
		private int numberOfPhotos;
		private int emissionsSamples;
		private int sizeOnDiskInBytes;
		private int binaryBytes;
		private long timeToCreateInMilliseconds;
		private long timeToReadInMilliseconds;

		public String getFileExt() {
			return fileExt;
		}

		public void setFileExt(String fileExt) {
			this.fileExt = fileExt;
		}

		public int getNumberOfPhotos() {
			return numberOfPhotos;
		}

		public void setNumberOfPhotos(int numberOfPhotos) {
			this.numberOfPhotos = numberOfPhotos;
		}

		public int getEmissionsSamples() {
			return emissionsSamples;
		}

		public void setEmissionsSamples(int emissionsSamples) {
			this.emissionsSamples = emissionsSamples;
		}

		public int getSizeOnDiskInBytes() {
			return sizeOnDiskInBytes;
		}

		public void setSizeOnDiskInBytes(int sizeOnDiskInBytes) {
			this.sizeOnDiskInBytes = sizeOnDiskInBytes;
		}

		public int getBinaryBytes() {
			return binaryBytes;
		}

		public void setBinaryBytes(int binaryBytes) {
			this.binaryBytes = binaryBytes;
		}

		public long getTimeToCreateInMilliseconds() {
			return timeToCreateInMilliseconds;
		}

		public void setTimeToCreateInMilliseconds(
				long timeToCreateInMilliseconds) {
			this.timeToCreateInMilliseconds = timeToCreateInMilliseconds;
		}

		public long getTimeToReadInMilliseconds() {
			return timeToReadInMilliseconds;
		}

		public void setTimeToReadInMilliseconds(long timeToReadInMilliseconds) {
			this.timeToReadInMilliseconds = timeToReadInMilliseconds;
		}
	}
}