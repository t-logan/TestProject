package com.hdf5vxml;

public class FileDescriptor {

	private String fileName;
	private int rows;
	private int cols;
	private int numberOfPhotos;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
}
