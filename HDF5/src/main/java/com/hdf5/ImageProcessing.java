package com.hdf5;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.ScalarDS;

public class ImageProcessing {

	public static void main(String[] args) throws Exception {
		importImages();
	}

	private static void importImages() throws Exception {

		// create an HDF5 dataset
		FileFormat ff = FileFormat.getInstance("dset.h5");
		ff.create("dset.h5");
		ff.createGroup("ImageGroup", null);
		Group pgroup = (Group) ff.get("ImageGroup");

		// import three images
		importImageFile("photo0.jpg", ff, pgroup, FileFormat.FILE_TYPE_HDF5);
		importImageFile("photo1.jpg", ff, pgroup, FileFormat.FILE_TYPE_HDF5);
		importImageFile("photo116.jpg", ff, pgroup, FileFormat.FILE_TYPE_HDF5);

		ff.close();
	}

	/**
	 * JPEG, TIFF, PNG, GIF, and BMP are supported.
	 * 
	 * Converts an image file into HDF4/5 file.
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
