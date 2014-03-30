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
import ncsa.hdf.object.ScalarDS;

public class Hdf5FileGenerator implements IFileGenerator {

	private final static String ARRAY_EXT = ".hdf5a";
	private final static String OPAQUE_EXT = ".hdf5b";

	private int totalBinaryData = 0;

	/**
	 * Generate opaque and array image HDF5 files, based on the information
	 * passed in the fileDescriptor.
	 * 
	 * @param fileDescriptor
	 *            information about the file being processed.
	 * @throws Exception
	 *             when an HDF5 error occurs.
	 */
	@Override
	public void generate(FileDescriptor fileDescriptor) throws Exception {

		// write opaque image file
		long startTime = System.currentTimeMillis();
		writeOpaqueBinaryFile(fileDescriptor);
		long elapsedTime = System.currentTimeMillis() - startTime;
		HDF5vXML.DATA.setTimeToCreateInMilliseconds(
				fileDescriptor.getFileName() + OPAQUE_EXT, elapsedTime);
		HDF5vXML.DATA.setNumberOfPhotos(fileDescriptor.getFileName()
				+ OPAQUE_EXT, fileDescriptor.getNumberOfPhotos());
		HDF5vXML.DATA.setDataArrayRows(fileDescriptor.getFileName()
				+ OPAQUE_EXT, fileDescriptor.getRows());

		// write array image file
		startTime = System.currentTimeMillis();
		writeArrayImageFile(fileDescriptor);
		elapsedTime = System.currentTimeMillis() - startTime;
		HDF5vXML.DATA.setTimeToCreateInMilliseconds(
				fileDescriptor.getFileName() + ARRAY_EXT, elapsedTime);
		HDF5vXML.DATA.setNumberOfPhotos(fileDescriptor.getFileName()
				+ ARRAY_EXT, fileDescriptor.getNumberOfPhotos());
		HDF5vXML.DATA.setDataArrayRows(fileDescriptor.getFileName()
				+ ARRAY_EXT, fileDescriptor.getRows());
	}

	/**
	 * Create HDF5 files containing a 2D array 3D array image data that can be
	 * displayed in the HDFView and other tools.
	 * 
	 * @param fileDescriptor
	 *            information about the file being processed.
	 * @throws Exception
	 *             when an HDF5 error occurs.
	 */
	private void writeArrayImageFile(FileDescriptor fileDescriptor)
			throws Exception {

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + ARRAY_EXT;

		// create an HDF5 dataset
		int fid = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		if (fid >= 0)
			H5.H5Fclose(fid);
		FileFormat ff = FileFormat.getInstance(fileName);

		// insert data entry for this file
		HDF5vXML.DATA.createStatsInfo(fileDescriptor.getFileName() + ARRAY_EXT);
		HDF5vXML.DATA.setFileExt(fileDescriptor.getFileName() + ARRAY_EXT,
				ARRAY_EXT.substring(1));

		// 2D array
		if (HDF5vXML.CONFIG.getMeanRows() > 0) {
			ff.createGroup("ArrayGroup", null);
			Group aGroup = (Group) ff.get("ArrayGroup");
			write2DArray(fileDescriptor.getRows(), fileDescriptor.getCols(),
					"2DArray", ff, aGroup);
		}

		// import the images in array format
		if (HDF5vXML.CONFIG.getMeanPhotos() > 0) {
			ff.createGroup("ImageGroup", null);
			Group pGroup = (Group) ff.get("ImageGroup");
			totalBinaryData = 0;
			HDF5vXML.PHOTOS.top();
			for (int i = 0; i < fileDescriptor.getNumberOfPhotos(); i++) {
				totalBinaryData += importImageDataset(HDF5vXML.PHOTOS.next(),
						ff, pGroup, FileFormat.FILE_TYPE_HDF5);
			}
			HDF5vXML.DATA.setBinaryBytes(fileDescriptor.getFileName()
					+ ARRAY_EXT, totalBinaryData);
		}
		ff.close();
	}

	/**
	 * Create HDF5 files containing a 2D array opaque image data.
	 * 
	 * @param fileDescriptor
	 *            information about the file being processed.
	 * @throws Exception
	 *             when an HDF5 error occurs.
	 */
	private void writeOpaqueBinaryFile(FileDescriptor fileDescriptor)
			throws Exception {

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + OPAQUE_EXT;

		// create an HDF5 dataset
		int fid = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		if (fid >= 0)
			H5.H5Fclose(fid);
		FileFormat ff = FileFormat.getInstance(fileName);

		// insert data entry for this file
		HDF5vXML.DATA
				.createStatsInfo(fileDescriptor.getFileName() + OPAQUE_EXT);
		HDF5vXML.DATA.setFileExt(fileDescriptor.getFileName() + OPAQUE_EXT,
				OPAQUE_EXT.substring(1));

		// 2D array
		if (HDF5vXML.CONFIG.getMeanRows() > 0) {
			ff.createGroup("ArrayGroup", null);
			Group aGroup = (Group) ff.get("ArrayGroup");
			write2DArray(fileDescriptor.getRows(), fileDescriptor.getCols(),
					"2DArray", ff, aGroup);
		}

		// import three images in opaque format
		if (HDF5vXML.CONFIG.getMeanPhotos() > 0) {
			ff.createGroup("ImageGroup", null);
			Group pGroup = (Group) ff.get("ImageGroup");
			HDF5vXML.PHOTOS.top();
			for (int i = 0; i < fileDescriptor.getNumberOfPhotos(); i++) {
				importOpaqueImageDataset(HDF5vXML.PHOTOS.next(), ff, pGroup,
						FileFormat.FILE_TYPE_HDF5);
			}
			// the same set of images is processed for both array and binary
			// files
			HDF5vXML.DATA.setBinaryBytes(fileDescriptor.getFileName()
					+ OPAQUE_EXT, totalBinaryData);
		}
		ff.close();
	}

	/**
	 * @param rows
	 *            the number of rows in the output array.
	 * @param cols
	 *            the number of columns in the output array.
	 * @param dsName
	 *            the array dataset name.
	 * @param hdfFile
	 *            the HDF FileFormat descriptor.
	 * @param pGroup
	 *            the parent group name.
	 */
	private void write2DArray(int rows, int cols, String dsName,
			FileFormat hdfFile, Group pGroup) throws Exception {

		int fid = -1, sid = -1, did = -1, wid = -1;
		int rank = 2;
		long[] dimensions = { rows, cols };
		double[][] values = new double[rows][cols];

		// nothing to write if rows=0
		if (rows == 0) {
			return;
		}

		// populate the array
		double value = 1.0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				values[row][col] = value++;
			}
		}

		try {
			fid = hdfFile.getFID();

			// Create the data space for the dataset.
			sid = H5.H5Screate_simple(rank, dimensions, null);

			// Create the int array dataset.
			if ((fid >= 0) && (sid >= 0))
				did = H5.H5Dcreate(fid, pGroup + "/" + dsName,
						HDF5Constants.H5T_IEEE_F64BE, sid,
						HDF5Constants.H5P_DEFAULT);
			else
				out.println("FAILED while creating the array dataset. fid="
						+ fid + ", sid=" + sid);

			// Write array to the dataset using default transfer properties.
			wid = H5.H5Dwrite(did, HDF5Constants.H5T_NATIVE_DOUBLE,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, values);
			if (wid < 0)
				out.println("write FAILED to 2x2 array, wid=" + wid);

			// End access to the data set and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Terminate access to the data space.
			if (sid >= 0)
				H5.H5Sclose(sid);
		} catch (Throwable t) {
			out.println("> Error writing array in Hdf5FileGenerator: "
					+ t.getMessage());
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
	private int importImageDataset(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {
		File imgFile = null;
		int imgFileBytes = 0;

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
			imgFileBytes = in.available();
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
			hdfFile.createImage(imgName + "." + HDF5vXML.PHOTOS.getSequence(),
					pGroup, type, dims, null, null, -1, 3,
					ScalarDS.INTERLACE_PIXEL, data);
		}

		// free memory
		data = null;
		image = null;
		// Runtime.getRuntime().gc();

		return imgFileBytes;
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
	private void importOpaqueImageDataset(String imgFileName,
			FileFormat hdfFile, Group pGroup, String hdfFileType)
			throws Exception {

		int space, dtype, dset, byteCount = 0;
		int status;

		// read image file
		BufferedImage image = null;
		try {
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(imgFileName));
			byteCount = in.available();
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
		long[] dims = { byteCount };
		int[] data = new int[h * w];

		// copy the image data
		int idx = 0;
		int rgb = 0;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				rgb = image.getRGB(j, i);
				data[idx++] = rgb;
			}
		}

		// think 1 should be h * w, but does not work (equals length)
		dtype = H5.H5Tcreate(HDF5Constants.H5T_OPAQUE, 1);
		status = H5.H5Tset_tag(dtype, "Tag");

		space = H5.H5Screate_simple(1, dims, null);
		dset = H5.H5Dcreate(hdfFile.getFID(),
				pGroup + imgFileName.substring(imgFileName.lastIndexOf('/'))
						+ "." + HDF5vXML.PHOTOS.getSequence(), dtype, space,
				HDF5Constants.H5P_DEFAULT);
		status = H5.H5Dwrite(dset, dtype, HDF5Constants.H5S_ALL,
				HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, data);

		status = H5.H5Dclose(dset);
		status = H5.H5Sclose(space);
		status = H5.H5Tclose(dtype);
	}
}
