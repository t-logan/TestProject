package com.hdf5vxml;

import static java.lang.System.out;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class Hdf5FileReader implements IFileReader {

	private final static String ARRAY_EXT = ".hdf5a";
	private final static String BINARY_EXT = ".hdf5b";

	@Override
	public void read(FileDescriptor fileDescriptor) throws Exception {

		// read array image file
		long startTime = System.currentTimeMillis();
		readArrayFile(fileDescriptor);
		long elapsedTime = System.currentTimeMillis() - startTime;
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName()
				+ ARRAY_EXT, elapsedTime);

		// read opaque image file
		startTime = System.currentTimeMillis();
		readBinaryFile(fileDescriptor);
		elapsedTime = System.currentTimeMillis() - startTime;
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName()
				+ BINARY_EXT, elapsedTime);
	}

	/**
	 * Reads all the data sets in an image array file.
	 * 
	 * @param fileDescriptor
	 * @throws Exception
	 */
	private void readArrayFile(FileDescriptor fileDescriptor) throws Exception {

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + ARRAY_EXT;

		FileFormat ff = FileFormat.getInstance(fileName);

		// read 2D data
		if (HDF5vXML.CONFIG.getMeanRows() > 0) {
			Group aGroup = (Group) ff.get("ArrayGroup");
			read2DArray(fileDescriptor.getRows(), fileDescriptor.getCols(),
					"2DArray", ff, aGroup);
		}

		// read image array data
		HDF5vXML.PHOTOS.top();
		if (HDF5vXML.CONFIG.getMeanPhotos() > 0) {
			Group pGroup = (Group) ff.get("ImageGroup");
			for (int i = 0; i < fileDescriptor.getNumberOfPhotos(); i++) {
				readImageDataset(
						HDF5vXML.PHOTOS.next() + "."
								+ HDF5vXML.PHOTOS.getSequence(), ff, pGroup,
						FileFormat.FILE_TYPE_HDF5);
			}
		}

		ff.close();
	}

	/**
	 * Reads all the data sets in an opaque image file.
	 * 
	 * @param fileDescriptor
	 * @throws Exception
	 */
	private void readBinaryFile(FileDescriptor fileDescriptor) throws Exception {

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + BINARY_EXT;

		FileFormat ff = FileFormat.getInstance(fileName);

		// 2D array
		if (HDF5vXML.CONFIG.getMeanRows() > 0) {
			Group aGroup = (Group) ff.get("ArrayGroup");
			read2DArray(fileDescriptor.getRows(), fileDescriptor.getCols(),
					"2DArray", ff, aGroup);
		}

		// read three images in opaque format
		HDF5vXML.PHOTOS.top();
		if (HDF5vXML.CONFIG.getMeanPhotos() > 0) {
			Group pGroup = (Group) ff.get("ImageGroup");
			for (int i = 0; i < fileDescriptor.getNumberOfPhotos(); i++) {
				readOpaqueImageDataset(HDF5vXML.PHOTOS.next() + "."
						+ HDF5vXML.PHOTOS.getSequence(), ff, pGroup,
						FileFormat.FILE_TYPE_HDF5);
			}
		}

		ff.close();
	}

	/**
	 * @param rows
	 *            the number of rows in the output array.
	 * @param cols
	 *            the number of columns in the output array.
	 * @param dsName
	 *            the array data set name.
	 * @param hdfFile
	 *            the HDF FileFormat descriptor.
	 * @param pGroup
	 *            the parent group name.
	 */
	private void read2DArray(int rows, int cols, String dsName,
			FileFormat hdfFile, Group pGroup) throws Exception {
		int fid = -1, did = -1, rid = -1;
		int[][] readValues = new int[rows][cols];

		// nothing to read if rows=0
		if (rows == 0) {
			return;
		}

		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(hdfFile.getAbsolutePath(),
					HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);

			// get the id for the dataset
			did = H5.H5Dopen(fid, pGroup + "/" + dsName);

			// Read the array using default transfer properties.
			try {
				rid = H5.H5Dread(did, HDF5Constants.H5T_NATIVE_INT,
						HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
						HDF5Constants.H5P_DEFAULT, readValues);
			} catch (Exception e) {
				System.out.println("row=" + rows + ", col=" + cols + ": "
						+ e.getMessage());
				throw e;
			}

			if (rid < 0)
				out.println("Read FAILED on " + dsName + " array, rid=" + rid);

			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			// TODO: stub routine
			t.printStackTrace();
			return;
		}
	}

	/**
	 * Imports an image file into a HDF4/5 file in the specified parent group.
	 * 
	 * JPEG, TIFF, PNG, GIF, and BMP are supported.
	 * 
	 * @param imgFileName
	 *            the input image file.
	 * @param hdfFile
	 *            the name of the HDF4/5 file being imported to.
	 * @param pGroup
	 *            the parent Group of the imported image.
	 * @param hdfFileType
	 *            the type of file converted to.
	 */
	private void readImageDataset(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {

		HObject ho = hdfFile.get("/" + pGroup
				+ imgFileName.substring(imgFileName.lastIndexOf('/')));
		// System.out.println(ho.getFullName());
		// System.out.println(ho.getMetadata());
		// System.out.println("Class=" + ho.getClass());

		// read the byte array
		Object data = ((ncsa.hdf.object.h5.H5ScalarDS) ho).getData();
		// System.out.println(data.getClass());
	}

	/**
	 * Imports an image file into a HDF4/5 file in the specified parent group.
	 * 
	 * JPEG, TIFF, PNG, GIF, and BMP are supported.
	 * 
	 * @param imgFileName
	 *            the input image file.
	 * @param hdfFile
	 *            the name of the HDF4/5 file being imported to.
	 * @param pGroup
	 *            the parent Group of the imported image.
	 * @param hdfFileType
	 *            the type of file converted to.
	 */
	private void readOpaqueImageDataset(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {

		int dtype, dset;

		// read image file into integer array
		dset = H5.H5Dopen(hdfFile.getFID(),
				pGroup + imgFileName.substring(imgFileName.lastIndexOf('/')),
				HDF5Constants.H5P_DEFAULT);
		dtype = H5.H5Dget_type(dset);

		long buffSize = H5.H5Dget_storage_size(dset);
		byte[] data = new byte[(int) buffSize];
		H5.H5Dread(dset, dtype, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
				HDF5Constants.H5P_DEFAULT, data);

		// check for equality between file on disk and HDF5 dataset
//		InputStream stream = new ByteArrayInputStream(data);		
//		File file = new File("/home/hadoop/tmp/images/photo0.jpg");  
//	    FileInputStream fis = new FileInputStream(file); 
//	    for(int i=0; i<21; i++)
//	    	System.out.print(fis.read() + ",");
//	    System.out.println();
//	    for(int i=0; i<21; i++)
//	    	System.out.print(stream.read() + ",");
//	    System.out.println();
//		fis.close();
		
		if (dset >= 0)
			H5.H5Dclose(dset);
		if (dtype >= 0)
			H5.H5Tclose(dtype);
	}
}
