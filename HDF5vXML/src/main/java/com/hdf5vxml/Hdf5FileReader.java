package com.hdf5vxml;

import static java.lang.System.out;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class Hdf5FileReader implements IFileReader {

	@Override
	public void read(FileDescriptor fileDescriptor) throws Exception {
		readImageArrayFile(fileDescriptor);
		readOpaqueFile(fileDescriptor);
	}

	/**
	 * Reads all the data sets in an image array file.
	 * @param fileDescriptor
	 * @throws Exception
	 */
	private void readImageArrayFile(FileDescriptor fileDescriptor)
			throws Exception {

		String arrayExt = ".hdf5a";

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + arrayExt;

		FileFormat ff = FileFormat.getInstance(fileName);

		long elapsedTime;
		long startTime = System.currentTimeMillis();

		// read numeric array data
		Group aGroup = (Group) ff.get("ArrayGroup");
		read2DArrayDataset(2, 2, "2x2", ff, aGroup);
//		read2DArrayDataset(3, 3, "3x3", ff, aGroup);
//		read2DArrayDataset(10, 7, "10x7", ff, aGroup);

		// TODO: Here
		// read image array data
		Group pGroup = (Group) ff.get("ImageGroup");
		readImageFile("photo0.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
//		readImageFile("photo1.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
//		readImageFile("photo116.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);

		elapsedTime = System.currentTimeMillis() - startTime;

		// update read timing
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName()
				+ arrayExt, elapsedTime);

		ff.close();
	}

	/**
	 * Reads all the data sets in an opaque image file.
	 * @param fileDescriptor
	 * @throws Exception
	 */
	private void readOpaqueFile(FileDescriptor fileDescriptor) throws Exception {

		String binExt = ".hdf5b";

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + binExt;

		FileFormat ff = FileFormat.getInstance(fileName);

		long elapsedTime;
		long startTime = System.currentTimeMillis();

		// array
		Group aGroup = (Group) ff.get("ArrayGroup");
		read2DArrayDataset(2, 2, "2x2", ff, aGroup);
		read2DArrayDataset(3, 3, "3x3", ff, aGroup);
		read2DArrayDataset(10, 7, "10x7", ff, aGroup);

		// read three images in opaque format
		Group pGroup = (Group) ff.get("ImageGroup");
		readOpaqueImageDataset("photo0.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		readOpaqueImageDataset("photo1.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		readOpaqueImageDataset("photo116.jpg", ff, pGroup,
				FileFormat.FILE_TYPE_HDF5);

		elapsedTime = System.currentTimeMillis() - startTime;

		// update read timing
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName()
				+ binExt, elapsedTime);

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
	private void read2DArrayDataset(int rows, int cols, String dsName,
			FileFormat hdfFile, Group pGroup) throws Exception {
		int fid = -1, did = -1, rid = -1;
		int[][] readValues = new int[rows][cols];

		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(hdfFile.getAbsolutePath(),
					HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);

			// get the id for the dataset
			did = H5.H5Dopen(fid, pGroup + "/" + dsName);

			// Read the array using default transfer properties.
			rid = H5.H5Dread(did, HDF5Constants.H5T_NATIVE_INT,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, readValues);
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
	private void readImageFile(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {
				
		HObject ho = hdfFile.get("/" + pGroup + "/" + imgFileName);
//		System.out.println(ho.getFullName());
//		System.out.println(ho.getMetadata());
//		System.out.println("Class=" + ho.getClass());
		
		// read the byte array
		Object data = ((ncsa.hdf.object.h5.H5ScalarDS) ho).getData();
//		System.out.println(data.getClass());
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

		int space, dtype, dset;
		int status;
		long[] DIMS = { 1 };

		// read image file
		dset = H5.H5Dopen(hdfFile.getFID(), "/ImageGroup/" + imgFileName,
				HDF5Constants.H5P_DEFAULT);
		dtype = H5.H5Dget_type(dset);
		int len = H5.H5Tget_size(dtype);
		//System.out.println("? len=" + len);
		String tag = H5.H5Tget_tag(dtype);

		space = H5.H5Dget_space(dset);
		int ndims = H5.H5Sget_simple_extent_dims(space, DIMS, null);
		// TODO: setting limit?
		int[] data = new int[500000];
		status = H5.H5Dread(dset, dtype, HDF5Constants.H5S_ALL,
				HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);

		status = H5.H5Dclose(dset);
		status = H5.H5Sclose(space);
		status = H5.H5Tclose(dtype);
	}
}
