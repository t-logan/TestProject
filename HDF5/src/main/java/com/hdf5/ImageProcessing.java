package com.hdf5;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.tree.DefaultMutableTreeNode;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.ScalarDS;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.view.Tools;

public class ImageProcessing {

	private static FileFormat fileFormat;
	public static final String FILE_TYPE_IMAGE = "IMG";

	private static Tools tools;

	public static void main(String[] args) throws Exception {
		// createHDFimage();
		// importImage();
		saveImageAsHDF("photo116.jpg", "dset.h5", Tools.FILE_TYPE_IMAGE,
				FileFormat.FILE_TYPE_HDF5);
	}

	private static void importImage() throws Exception {

		// create a dataset
		FileFormat ff = FileFormat.getInstance("dset.h5");
		ff.create("dset.h5");
		ff.createGroup("ImageGroup", null);
		ff.close();
		ff.open();

		int gcpl = 0;
		Group parent = null;
		Group pgroup = (Group) ff.get("ImageGroup");
		String name = "2D image";
		Datatype dtype = new H5Datatype(Datatype.CLASS_INTEGER, 1,
				Datatype.NATIVE, Datatype.SIGN_NONE);
		long[] chunks = null; // no chunking
		int gzip = 0; // no compression
		int ncomp = 3; // RGB true color image
		int interlace = ScalarDS.INTERLACE_PIXEL;

		// read the image file
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File("photo0.jpg"));
		} catch (IOException e) {
			System.out.println("I/O error when reading the image file");
		}
		int w = img.getWidth();
		int h = img.getHeight();
		int[] dataBuffInt = img.getRGB(0, 0, w, h, null, 0, w);

		long[] dims = { h, w };
		long[] maxdims = dims;

		// create the image dataset
		Dataset d = ff.createImage(name, pgroup, dtype, dims, maxdims, chunks,
				gzip, ncomp, interlace, dataBuffInt);
		ff.close();
	}

	private static void writeAnEmptyImage() throws Exception {

		FileFormat ff = FileFormat.getInstance("dset.h5");
		ff.create("dset.h5");
		ff.createGroup("ImageGroup", null);
		// System.out.println(((DefaultMutableTreeNode)
		// ff.getRootNode().getChildAt(0)).getUserObject());
		ff.close();
		ff.open();

		int gcpl = 0;
		Group parent = null;
		Group pgroup = (Group) ff.get("ImageGroup");
		String name = "2D image";
		Datatype dtype = new H5Datatype(Datatype.CLASS_INTEGER, 1,
				Datatype.NATIVE, Datatype.SIGN_NONE);
		long[] dims = { 100, 100 };
		long[] maxdims = dims;
		long[] chunks = null; // no chunking
		int gzip = 0; // no compression
		int ncomp = 3; // RGB true color image
		int interlace = ScalarDS.INTERLACE_PIXEL;
		Object data = null; // no initial data values
		Dataset d = ff.createImage(name, pgroup, dtype, dims, maxdims, chunks,
				gzip, ncomp, interlace, data);
		ff.close();
	}

	/**
	 * Converts an image file into HDF4/5 file.
	 * 
	 * @param imgFileName
	 *            the input image file.
	 * @param hdfFileName
	 *            the name of the HDF4/5 file.
	 * @param fromType
	 *            the type of image.
	 * @param toType
	 *            the type of file converted to.
	 */
	public static void saveImageAsHDF(String imgFileName, String hdfFileName,
			String fromType, String toType) throws Exception {
		File imgFile = null;

		if (imgFileName == null) {
			throw new NullPointerException("The source image file is null.");
		} else if (!(imgFile = new File(imgFileName)).exists()) {
			throw new NullPointerException(
					"The source image file does not exist.");
		} else if (hdfFileName == null) {
			throw new NullPointerException("The target HDF file is null.");
		}

		if (!fromType.equals(FILE_TYPE_IMAGE)) {
			throw new UnsupportedOperationException("Unsupported image type.");
		} else if (!(toType.equals(FileFormat.FILE_TYPE_HDF4) || toType
				.equals(FileFormat.FILE_TYPE_HDF5))) {
			throw new UnsupportedOperationException(
					"Unsupported destination file type.");
		}

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

		try {
			data = new byte[3 * h * w];
		} catch (OutOfMemoryError err) {
			err.printStackTrace();
			throw new RuntimeException("Out of memory error.");
		}

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
		Group pgroup = null;
		String imgName = imgFile.getName();
		FileFormat newfile = null, thefile = null;
		if (toType.equals(FileFormat.FILE_TYPE_HDF5)) {
			thefile = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
			long[] h5dims = { h, w, 3 }; // RGB pixel interlace
			dims = h5dims;
		} else if (toType.equals(FileFormat.FILE_TYPE_HDF4)) {
			thefile = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
			long[] h4dims = { w, h, 3 }; // RGB pixel interlace
			dims = h4dims;
		} else {
			thefile = null;
		}

		if (thefile != null) {
			newfile = thefile.createInstance(hdfFileName, FileFormat.CREATE);
			newfile.open();
			pgroup = (Group) ((DefaultMutableTreeNode) newfile.getRootNode())
					.getUserObject();
			type = newfile.createDatatype(Datatype.CLASS_CHAR, 1,
					Datatype.NATIVE, Datatype.SIGN_NONE);
			newfile.createImage(imgName, pgroup, type, dims, null, null, -1, 3,
					ScalarDS.INTERLACE_PIXEL, data);
			newfile.close();
		}

		// clean up memory
		data = null;
		image = null;
		Runtime.getRuntime().gc();
	}
}
