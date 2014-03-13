package com.hdf5vxml;

public class FileDescriptor {

	private String fileName;
	private long rows;
	private int cols;
	private long numberOfPhotos;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getRows() {
		return rows;
	}

	public void setRows(long rows) {
		this.rows = rows;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public long getNumberOfPhotos() {
		return numberOfPhotos;
	}

	public void setNumberOfPhotos(long numberOfPhotos) {
		this.numberOfPhotos = numberOfPhotos;
	}

	public String toString() {
		return "fileName=" + fileName + ", rows=" + rows + ", cols=" + cols
				+ ", numberOfPhotos=" + numberOfPhotos;
	}
}
