package com.hdf5vxml;

public class FileDescriptor {

	public final static String XML_EXT = ".xml";
	public final static String XMLC_EXT = ".xmlc";
	public final static String HDF5_ARRAY_EXT = ".hdf5a";
	public final static String HDF5_BIN_EXT = ".hdf5b";

	private String fileName;
	private String fileExt;
	private int rows;
	private int cols;
	private int numberOfPhotos;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileExt() {
		return fileExt;
	}

	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public int getNumberOfPhotos() {
		return numberOfPhotos;
	}

	public void setNumberOfPhotos(int numberOfPhotos) {
		this.numberOfPhotos = numberOfPhotos;
	}

	public String toString() {
		return "fileName=" + fileName + ", fileExt=" + fileExt + ", rows="
				+ rows + ", cols=" + cols + ", numberOfPhotos="
				+ numberOfPhotos;
	}
}
