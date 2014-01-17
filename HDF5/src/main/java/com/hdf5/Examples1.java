package com.hdf5;

import static java.lang.System.out;

import java.nio.ByteBuffer;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

public class Examples1 {

	private final static String FILE_NAME = "examples1.h5";
	private final static String INT_DATASET_NAME = "/int_3x3_array";
	private final static String STR_DATASET_NAME = "/a_string";
	private final static String STRING_VALUE = "This is a fairly long string. String arrays are not supported.";

	/*
	 * Create a new file using H5ACC_TRUNC to truncate and overwrite any file of
	 * the same name, default file creation properties, and default file access
	 * properties. Then close the file.
	 */
	private static void createFile() throws Exception {
		int fid = -1;
		// overwrite if it exists
		fid = H5.H5Fcreate(FILE_NAME, HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		int status = H5.H5Fclose(fid);
		if (fid >= 0 && status >= 0)
			out.println("createFile OK");
		else
			out.println("> createFile FAILED, status=" + status);
	}

	/*
	 * Prevent overwriting the new file by using the H5F_ACC_EXCL flag instead
	 * of H5F_ACC_TRUNC. The exception does not indicate what the problem is
	 * (just "library exception").
	 */
	private static void failOnFileOverwrite() throws Exception {
		try {
			H5.H5Fcreate(FILE_NAME, HDF5Constants.H5F_ACC_EXCL,
					HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		} catch (Throwable t) {
			out.println("failOnDatasetOverwrite OK");
			return;
		}
		// fall through
		out.println("> failOnDatasetOverwrite FAILED");
	}

	/*
	 * Create a file and dataset containing a three dimensional int array.
	 */
	private static void writeIntArrayToDataset() throws Exception {
		int fid = -1, sid = -1, did = -1, wid = -1;
		int RANK = 3;
		long[] DIMENSIONS = { 3, 3, 3 };
		int[][][] VALUES = { { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } },
				{ { 10, 11, 12 }, { 13, 14, 15 }, { 16, 17, 18 } },
				{ { 19, 20, 21 }, { 22, 23, 24 }, { 25, 26, 27 } } };
		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);

			// Create the data space for the int dataset.
			sid = H5.H5Screate_simple(RANK, DIMENSIONS, null);

			// Create the int array dataset.
			if ((fid >= 0) && (sid >= 0))
				did = H5.H5Dcreate(fid, INT_DATASET_NAME,
						HDF5Constants.H5T_STD_I32BE, sid,
						HDF5Constants.H5P_DEFAULT);
			else
				out.println("> writeIntArrayToDataset FAILED while creating the int array dataset. fid="
						+ fid + ", sid=" + sid);

			// Write 3x3 int array to the dataset using default transfer
			// properties.
			wid = H5.H5Dwrite(did, HDF5Constants.H5T_NATIVE_INT,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, VALUES);
			if (wid < 0)
				out.println("> writeIntArrayToDataset write FAILED to 3x3 int array, wid="
						+ wid);

			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Terminate access to the data space.
			if (sid >= 0)
				H5.H5Sclose(sid);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> writeIntArrayToDataset FAILED, exception=" + t);
			return;
		}
		out.println("writeIntArrayToDataset OK");
	}

	/*
	 * Read the three dimensional int array back from the file.
	 */
	private static void readIntArray() throws Exception {
		int fid = -1, did = -1, rid = -1;
		int[][][] readValues = new int[3][3][3];

		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);

			// get the id for the dataset
			did = H5.H5Dopen(fid, INT_DATASET_NAME);

			// Read the 3x3 int array using default transfer properties.
			rid = H5.H5Dread(did, HDF5Constants.H5T_NATIVE_INT,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, readValues);
			if (rid < 0)
				out.println("> readIntArray read FAILED on the 3x3 int array, rid="
						+ rid);

			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> readIntArray FAILED, exception=" + t);
			t.printStackTrace();
			return;
		}
		if (readValues[0][0][0] == 1 && readValues[2][2][2] == 27)
			out.println("readIntArray OK");
		else
			out.println("> readIntArray FAILED");
	}

	/*
	 * Create a file and dataset containing a three dimensional int array.
	 */
	private static void addString() throws Exception {
		int fid = -1, sid = -1, did = -1, wid = -1;
		int RANK = 1;
		// 4 characters per byte
		long[] DIMENSIONS = { 16 };
		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);

			// Create the data space for the int dataset.
			sid = H5.H5Screate_simple(RANK, DIMENSIONS, null);

			// Create the string array dataset.
			if ((fid >= 0) && (sid >= 0))
				did = H5.H5Dcreate(fid, STR_DATASET_NAME,
						HDF5Constants.H5T_STD_I32BE, sid,
						HDF5Constants.H5P_DEFAULT);
			else
				out.println("> addString FAILED while creating the int array dataset. fid="
						+ fid + ", sid=" + sid);

			// Write 3x3 int array to the dataset using default transfer
			// properties.
			wid = H5.H5Dwrite(did, HDF5Constants.H5T_NATIVE_INT,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, STRING_VALUE.getBytes());
			if (wid < 0)
				out.println("> addString write FAILED to 3x3 int array, wid="
						+ wid);

			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Terminate access to the data space.
			if (sid >= 0)
				H5.H5Sclose(sid);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> addString FAILED, exception=" + t);
			t.printStackTrace();
			return;
		}
		out.println("addString OK");
	}

	/*
	 * Read the three dimensional int array back from the file.
	 */
	private static void readString() throws Exception {
		int fid = -1, did = -1, rid = -1;
		byte[] readValues = new byte[STRING_VALUE.length()];

		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);

			// get the id for the dataset
			did = H5.H5Dopen(fid, STR_DATASET_NAME);

			// Read the 3x3 int array using default transfer properties.
			rid = H5.H5Dread(did, HDF5Constants.H5T_NATIVE_INT,
					HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
					HDF5Constants.H5P_DEFAULT, readValues);
			if (rid < 0)
				out.println("> readString read FAILED on the 3x3 int array, rid="
						+ rid);

			// End access to the dataset and release resources used by it.
			if (did >= 0)
				H5.H5Dclose(did);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> readString FAILED, exception=" + t);
			t.printStackTrace();
			return;
		}
		if (readValues.length == 62 && readValues.length == STRING_VALUE.length()) {
			out.println("readString OK");
		} else
			out.println("> readString FAILED");
	}

	/*
	 * Read the three dimensional int array back from the file.
	 */
	private static void createGroups() throws Exception {
		int fid = -1, grp = -1, grp2 = -1;
		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);

			grp = H5.H5Gcreate(fid, "/MyGroup", HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
			grp2 = H5.H5Gcreate(fid, "/MyGroup/Child",
					HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT);

			// End access to the group and release resources used by it.
			if (grp >= 0)
				H5.H5Gclose(grp);
			if (grp2 >= 0)
				H5.H5Gclose(grp2);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> createGroups FAILED, exception=" + t);
			t.printStackTrace();
			return;
		}
		out.println("createGroups OK");
	}

	/*
	 * Read the three dimensional int array back from the file.
	 */
	private static void createDatasetInGroup() throws Exception {
		int fid = -1, dataspace = -1, dataset = -1, plist = -1;
		long[] dims = new long[2];
		long[] cdims = new long[2];
		
		try {
			// Open the existing file using default properties.
			fid = H5.H5Fopen(FILE_NAME, HDF5Constants.H5F_ACC_RDWR,
					HDF5Constants.H5P_DEFAULT);
			dims[0] = 1000;
			dims[1] = 20;
			cdims[0] = 20;
			cdims[1] = 20;
			int RANK = 2;
			dataspace = H5.H5Screate_simple(RANK, dims, null);
			plist = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
			H5.H5Pset_chunk(plist, 2, cdims);
			H5.H5Pset_deflate(plist, 6);
			dataset = H5.H5Dcreate(fid, "/MyGroup/Child/Compressed_Data", 
					HDF5Constants.H5T_NATIVE_INT, dataspace, HDF5Constants.H5P_DEFAULT, plist, HDF5Constants.H5P_DEFAULT);
			
			// End access to the group and release resources used by it.
			if (dataset >= 0)
				H5.H5Dclose(dataset);
			if (dataspace >= 0)
				H5.H5Sclose(dataspace);
			// Close the file.
			if (fid >= 0)
				H5.H5Fclose(fid);
		} catch (Throwable t) {
			out.println("> createDatasetInGroup FAILED, exception=" + t);
			t.printStackTrace();
			return;
		}
		out.println("createDatasetInGroup OK");
	}

	//
	/*
	 * Convert C examples from the HDF5 User's Guide to Java and test them.
	 */
	public static void main(String[] args) {
		try {
			// create an HDF5 file
			Examples1.createFile();

			// test file protection (against overwriting)
			Examples1.failOnFileOverwrite();

			// write a 3x3 array of integers to the file
			Examples1.writeIntArrayToDataset();

			// read the 3x3 array of integers back and validate
			Examples1.readIntArray();

			// add a string to the file; string arrays are not supported
			Examples1.addString();

			// read the string back
			Examples1.readString();

			// add a group
			Examples1.createGroups();
			
			Examples1.createDatasetInGroup();

			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
