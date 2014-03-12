package com.hdf5vxml;

import static java.lang.System.out;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;

public class Hdf5FileReader implements IFileReader {

	@Override
	public void read(FileDescriptor fileDescriptor) throws Exception {
		readImageArrayFile(fileDescriptor);
		readOpaqueFile(fileDescriptor);
	}

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
		read2DDoubleArray(2, 2, "2x2", ff, aGroup);
		read2DDoubleArray(3, 3, "3x3", ff, aGroup);
		read2DDoubleArray(10, 7, "10x7", ff, aGroup);

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

	private void readOpaqueFile(FileDescriptor fileDescriptor) throws Exception {

		String binExt = ".hdf5b";

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + binExt;

		FileFormat ff = FileFormat.getInstance(fileName);

		long elapsedTime;
		long startTime = System.currentTimeMillis();

		// array
		Group aGroup = (Group) ff.get("ArrayGroup");
		read2DDoubleArray(2, 2, "2x2", ff, aGroup);
		read2DDoubleArray(3, 3, "3x3", ff, aGroup);
		read2DDoubleArray(10, 7, "10x7", ff, aGroup);

		// read three images in opaque format
		Group pGroup = (Group) ff.get("ImageGroup");
		readOpaqueImageFile("photo0.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		readOpaqueImageFile("photo1.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		readOpaqueImageFile("photo116.jpg", ff, pGroup,
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
	private void read2DDoubleArray(int rows, int cols, String dsName,
			FileFormat hdfFile, Group pGroup) throws Exception {
		int fid = -1, did = -1, rid = -1;
		int[][] readValues = new int[rows][cols];

		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(hdfFile.getAbsolutePath(),
					HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);

			// get the id for the dataset
			did = H5.H5Dopen(fid, pGroup + "/" + dsName);

			// Read the 3x3 int array using default transfer properties.
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
		File imgFile = null;

		if (imgFileName == null) {
			throw new NullPointerException("Source image file is null.");
		} else if (!(imgFile = new File(imgFileName)).exists()) {
			throw new NullPointerException("Source image file does not exist.");
		} else if (hdfFile == null) {
			throw new NullPointerException("Target HDF file is null.");
		}

		if (!(hdfFileType.equals(FileFormat.FILE_TYPE_HDF4) || hdfFileType
				.equals(FileFormat.FILE_TYPE_HDF5))) {
			throw new UnsupportedOperationException(
					"Unsupported destination file type.");
		}

		// read image file
		BufferedImage image = null;
		try {
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(imgFileName));
			image = ImageIO.read(in);
			in.close();
		} catch (Throwable err) {
			image = null;
		}

		if (image == null)
			throw new UnsupportedOperationException("Failed to read image: "
					+ imgFileName);

		int h = image.getHeight();
		int w = image.getWidth();
		byte[] data = null;

		// allocate buffer
		try {
			data = new byte[3 * h * w];
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
			throw new RuntimeException("Out of memory error.");
		}

		// construct HDF image in memory
		int idx = 0;
		int rgb = 0;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				rgb = image.getRGB(j, i);
				data[idx++] = (byte) (rgb >> 16);
				data[idx++] = (byte) (rgb >> 8);
				data[idx++] = (byte) rgb;
			}
		}

		long[] dims = null;
		Datatype type = null;
		String imgName = imgFile.getName();

		if (hdfFileType.equals(FileFormat.FILE_TYPE_HDF5)) {
			hdfFile = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
			long[] h5dims = { h, w, 3 }; // RGB pixel interlace
			dims = h5dims;
		} else if (hdfFileType.equals(FileFormat.FILE_TYPE_HDF4)) {
			hdfFile = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
			long[] h4dims = { w, h, 3 }; // RGB pixel interlace
			dims = h4dims;
		} else {
			hdfFile = null;
		}

		if (hdfFile != null) {
			type = hdfFile.createDatatype(Datatype.CLASS_CHAR, 1,
					Datatype.NATIVE, Datatype.SIGN_NONE);
//			hdfFile.createImage(imgName, pGroup, type, dims, null, null, -1, 3,
//					ScalarDS.INTERLACE_PIXEL, data);
		}

		// free memory
		data = null;
		image = null;
		Runtime.getRuntime().gc();
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
	private void readOpaqueImageFile(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {

		int space, dtype, dset;
		int status;
		long[] DIMS = { 1 };

		// read image file
		dset = H5.H5Dopen(hdfFile.getFID(), "/ImageGroup/" + imgFileName,
				HDF5Constants.H5P_DEFAULT);
		dtype = H5.H5Dget_type(dset);
		int len = H5.H5Tget_size(dtype);
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
