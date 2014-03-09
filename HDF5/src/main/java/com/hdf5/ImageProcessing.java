package com.hdf5;

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

public class ImageProcessing {

	public static void main(String[] args) throws Exception {
		populate();
	}

	private static void populate() throws Exception {

		// create an HDF5 dataset
		FileFormat ff = FileFormat.getInstance("dset.h5");
		ff.create("dset.h5");

		// array
		ff.createGroup("ArrayGroup", null);
		Group aGroup = (Group) ff.get("ArrayGroup");
		write2DArray(2, 2, "2x2", ff, aGroup);
		write2DArray(3, 3, "3x3", ff, aGroup);
		write2DArray(10, 7, "10x7", ff, aGroup);

		// import three images
		ff.createGroup("ImageGroup", null);
		Group pGroup = (Group) ff.get("ImageGroup");
		importImageFile("photo0.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		importImageFile("photo1.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);
		importImageFile("photo116.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);

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
	private static void write2DArray(int rows, int cols, String dsName,
			FileFormat hdfFile, Group pGroup) throws Exception {

		int fid = -1, sid = -1, did = -1, wid = -1;
		int rank = 2;
		long[] dimensions = { rows, cols };
		double[][] values = new double[rows][cols]; // { { 1.123456789, 2.2 }, {
													// 3.3, 4.4 } };

		// populate the array
		double value = 1.0;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				values[row][col] = value++;
			}
		}

		try {
			fid = hdfFile.getFID();

			// Create the data space for the int dataset.
			sid = H5.H5Screate_simple(rank, dimensions, null);

			// Create the int array dataset.
			if ((fid >= 0) && (sid >= 0))
				did = H5.H5Dcreate(fid, pGroup + "/" + dsName,
						HDF5Constants.H5T_IEEE_F64BE, sid,
						HDF5Constants.H5P_DEFAULT);
			else
				out.println("> writeIntArrayToDataset FAILED while creating the int array dataset. fid="
						+ fid + ", sid=" + sid);

			// Write array to the dataset using default transfer properties.
			wid = H5.H5Dwrite(did, HDF5Constants.H5T_NATIVE_DOUBLE,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, values);
			if (wid < 0)
				out.println("> writeIntArrayToDataset write FAILED to 3x3 int array, wid="
						+ wid);

			// End access to the data set and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Terminate access to the data space.
			if (sid >= 0)
				H5.H5Sclose(sid);
		} catch (Throwable t) {
			out.println("> Error writing array: " + t.getMessage());
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
	public static void importImageFile(String imgFileName, FileFormat hdfFile,
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
			hdfFile.createImage(imgName, pGroup, type, dims, null, null, -1, 3,
					ScalarDS.INTERLACE_PIXEL, data);
		}

		// free memory
		data = null;
		image = null;
		Runtime.getRuntime().gc();
	}
}
