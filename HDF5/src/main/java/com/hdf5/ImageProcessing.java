package com.hdf5;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.ScalarDS;
import ncsa.hdf.object.h5.H5Datatype;

public class ImageProcessing {

	private static FileFormat fileFormat;

	public static void main(String[] args) throws Exception {
		//createHDFimage();
		importImage();
	}

	private static Dataset createHDFimage() throws Exception {

		Dataset dataset = null;

		String name = "FileName.h5";

		int fid = -1, sid = -1, did = -1;
		long[] dims = { 4, 6 };
		// Create a new file using default properties.
		fid = H5.H5Fcreate(name, HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		// Create the data space for the dataset.
		sid = H5.H5Screate_simple(2, dims, null);
		// Create the dataset.
		if ((fid >= 0) && (sid >= 0))
			did = H5.H5Dcreate(fid, "/dset", HDF5Constants.H5T_STD_I32BE, sid,
					HDF5Constants.H5P_DEFAULT);

		// Group pgroup = (Group)
		// groupList.get(parentChoice.getSelectedIndex());
		// Group pgroup = null;
		//fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
		fileFormat = FileFormat.getInstance(name);

		int w = 10, h = 10;

		// long[] dims = null;
		int tclass = Datatype.CLASS_CHAR;
		int tsign = Datatype.SIGN_NONE;
		int tsize = 1;
		int torder = Datatype.NATIVE;
		int interlace = ScalarDS.INTERLACE_PIXEL;
		int ncomp = 2;

		// HDF5 true color image
		// if (checkInterlacePixel.isSelected()) {
		// long[] tmpdims = { h, w, 3 };
		// dims = tmpdims;
		// } else {
		interlace = ScalarDS.INTERLACE_PLANE;
		long[] tmpdims = { 3, h, w };
		dims = tmpdims;
		// }

		int gcpl = 0;
		Group parent = null;
		Group pgroup = fileFormat.createGroup("/dset", parent, 0, gcpl);
		if (pgroup == null)
			System.out.println("pgroup is NULL");

		try {
			Datatype datatype = fileFormat.createDatatype(tclass, tsize,
					torder, tsign);
			System.out.println(name);
			dataset = fileFormat.createImage(name, pgroup, datatype, dims,
					dims, null, -1, ncomp, interlace, null);
			dataset.init();
		} catch (Exception ex) {
			throw ex;
		} finally {
			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Terminate access to the data space.
			if (sid >= 0)
				H5.H5Sclose(sid);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		}
		return dataset;
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
		Datatype dtype = new H5Datatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE,
				Datatype.SIGN_NONE);
		long[] chunks = null; // no chunking
		int gzip = 0; // no compression
		int ncomp = 3; // RGB true color image
		int interlace = ScalarDS.INTERLACE_PIXEL;
		
		// read the image file
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File("photo116.jpg"));
		} catch (IOException e) {
			System.out.println("I/O error when reading the image file");
		}
		int w = img.getWidth();
		int h = img.getHeight();
		int[] dataBuffInt = img.getRGB(0, 0, w, h, null, 0, w);

		long[] dims = { h, w };
		long[] maxdims = dims;	
		
		// create the image dataset
		Dataset d = ff.createImage(name, pgroup, dtype, dims, maxdims, chunks, gzip, ncomp, interlace, dataBuffInt);
		ff.close();
	}


	private static void writeAnEmptyImage() throws Exception {
		
		FileFormat ff = FileFormat.getInstance("dset.h5");
		ff.create("dset.h5");
		ff.createGroup("ImageGroup", null);
		//System.out.println(((DefaultMutableTreeNode) ff.getRootNode().getChildAt(0)).getUserObject());
		ff.close();
		ff.open();
		
		int gcpl = 0;
		Group parent = null;
		Group pgroup = (Group) ff.get("ImageGroup");
		String name = "2D image";
		Datatype dtype = new H5Datatype(Datatype.CLASS_INTEGER, 1, Datatype.NATIVE,
				Datatype.SIGN_NONE);
		long[] dims = { 100, 100 };
		long[] maxdims = dims;
		long[] chunks = null; // no chunking
		int gzip = 0; // no compression
		int ncomp = 3; // RGB true color image
		int interlace = ScalarDS.INTERLACE_PIXEL;
		Object data = null; // no initial data values
		Dataset d = ff.createImage(name, pgroup, dtype, dims, maxdims, chunks, gzip, ncomp, interlace, data);
		ff.close();
	}
}
