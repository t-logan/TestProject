package com.hdf5vxml;

import static java.lang.System.out;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hdf5vxml.StatsData.StatsInfo;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.ScalarDS;

public class Hdf5FileGenerator implements IFileWriter {

	@Override
	public void writeFile(FileDescriptor fileDescriptor) throws Exception {

		String arrayExt = ".hdf5a";
		String binExt = ".hdf5b";

		String fileName = HDF5vXML.CONFIG.getTargetDir()
				+ fileDescriptor.getFileName() + arrayExt;

		// create an HDF5 dataset
		int fid = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		if (fid >= 0)
			H5.H5Fclose(fid);
		FileFormat ff = FileFormat.getInstance(fileName);

		// insert data entry for this file
		StatsData.StatsInfo info = HDF5vXML.DATA.new StatsInfo();
		info.setFileExt(arrayExt.substring(1));
		HDF5vXML.DATA.putStatsInfo(fileDescriptor.getFileName() + arrayExt,
				info);

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

		importOpaqueImageFile("photo0.jpg", ff, pGroup, FileFormat.FILE_TYPE_HDF5);

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
	private void importImageFile(String imgFileName, FileFormat hdfFile,
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
	private void importOpaqueImageFile(String imgFileName, FileFormat hdfFile,
			Group pGroup, String hdfFileType) throws Exception {
		
		String FILE = "opaque.h5";
		String DATASET = "DS1";
		int DIM0 = 4;
		int LEN = 7;
		
		int file, space, dtype, dset;
		int status;
		long[] dims = {DIM0};
		int len;
		byte[] wdata = new byte[DIM0 * LEN];
		String value = "OPAQUE";
		int ndims, i, j;
		
		wdata[0] = 'A';
		
		file = H5.H5Fcreate(FILE, HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		
		dtype = H5.H5Tcreate(HDF5Constants.H5T_OPAQUE, LEN);
		status = H5.H5Tset_tag(dtype, "Tag");
		
		space = H5.H5Screate_simple(1, dims, null);
		dset = H5.H5Dcreate(file, DATASET, dtype, space, HDF5Constants.H5P_DEFAULT);
		status = H5.H5Dwrite(dset, dtype, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, wdata);
		
		status = H5.H5Dclose(dset);
		status = H5.H5Sclose(space);
		status = H5.H5Tclose(dtype);
		status = H5.H5Fclose(file);
	}
}
