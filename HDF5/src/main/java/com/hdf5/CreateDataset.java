package com.hdf5;

//Creating a file and dataset.
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

public class CreateDataset {
	private static void CreateDataset() throws Exception {
		int fid = -1, sid = -1, did = -1;
		long[] dims = { 4, 6 };
		// Create a new file using default properties.
		fid = H5.H5Fcreate("dset.h5", HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		// Create the data space for the dataset.
		sid = H5.H5Screate_simple(2, dims, null);
		// Create the dataset.
		if ((fid >= 0) && (sid >= 0))
			did = H5.H5Dcreate(fid, "/dset", HDF5Constants.H5T_STD_I32BE, sid,
					HDF5Constants.H5P_DEFAULT);
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

	public static void main(String[] args) {
		try {
			CreateDataset.CreateDataset();
			System.out.println("Success!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
