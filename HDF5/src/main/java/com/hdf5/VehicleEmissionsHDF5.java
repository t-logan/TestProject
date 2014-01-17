package com.hdf5;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class VehicleEmissionsHDF5 {
	
	private final static String FILE_NAME = "examples1.h5";

	// inputCSVFile and inputDataPath are passed as a commandline parameters
	private String outputDataPath = "";

	/**
	 * Write the XML file by parsing a CSV file containing the data and
	 * expressing it as XML.
	 * 
	 * @param inputFile
	 *            the name of the file to read (seed.csv).
	 * @throws Exception 
	 */
	private void generateFiles(String inputFile) throws Exception {

		Vehicle vehicle = null;
		Emissions emissions = null;
		boolean isVehicleRec = false;
		String line;
		int lineCount = 0;
		int tokenCount = 0;
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis,
				Charset.forName("UTF-8")));

		// read the seed file one line at a time
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			lineCount++;
			// skip header record
			if (lineCount == 1)
				continue;
			System.out.println(line);
			tokenCount = 0;
			emissions = new Emissions();
			while (st.hasMoreTokens()) {
				String tok = st.nextToken(",");
				tokenCount++;
				switch (tokenCount) {
				// vin
				case 1:
					if (tok.equals("\"\""))
						isVehicleRec = false;
					else {
						if (vehicle != null) {
							emitHdf5(vehicle);
						}
						vehicle = new Vehicle();
						vehicle.vin = tok;
						isVehicleRec = true;
					}
					break;
				case 2:
					if (isVehicleRec)
						vehicle.manufacturer = tok.substring(1,
								tok.length() - 1);
					break;
				case 3:
					if (isVehicleRec)
						vehicle.modelYear = Integer.parseInt(tok);
					break;
				case 4:
					if (isVehicleRec)
						vehicle.vehicleType = tok;
					break;
				case 5:
					if (isVehicleRec)
						vehicle.oilChangeDistance = Float.parseFloat(tok);
					break;
				case 6:
					if (isVehicleRec)
						vehicle.odometer = Float.parseFloat(tok);
					break;
				case 7:
					if (isVehicleRec)
						vehicle.comments = tok.substring(1, tok.length() - 1);
					break;
				case 8:
					emissions.dateTested = tok;
					break;
				case 9:
					emissions.exhaustHC = Float.parseFloat(tok);
					break;
				case 10:
					emissions.nonExhaustHC = Float.parseFloat(tok);
					break;
				case 11:
					emissions.exhaustCO = Float.parseFloat(tok);
					break;
				case 12:
					emissions.exhaustNO2 = Float.parseFloat(tok);
					break;
				}
				// inject the emissions data into the vehicle information
			}
			vehicle.emissions.add(emissions);
		}
		// pick up the last vehicle
		emitHdf5(vehicle);
		br.close();
	}

	private void emitHdf5(Vehicle vehicle) throws NullPointerException, IOException, HDF5Exception {
		
		// create the output HDF5 file for this vehicle
		int fid = -1, vingrpid = -1, status = -1;
		// overwrite if it exists
		fid = H5.H5Fcreate(outputDataPath + vehicle.vin + ".hdf5", HDF5Constants.H5F_ACC_TRUNC,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

		// create the VIN number group
		vingrpid = H5.H5Gcreate(fid, "/" + vehicle.vin, HDF5Constants.H5P_DEFAULT,
				HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

		// add vin group members
//	       	grp['manufacturer'] = fields[1]                     # populate the vehicle group
//	    		   grp['modelYear'] = int(fields[2])
//	    	        grp['vehicleType'] = fields[3]
//	    	        grp['oilChangeDistance'] = float(fields[4])
//	    	        grp['odometer'] = float(fields[5])
//	    	        grp['comments'] = fields[6]
		
		// the rank of a scalar dataspace is always zero
		int RANK = 0;
		long[] DIMENSIONS = { 1 };
		int did = -1;

		// Create the data space for the int dataset.
		 int sid = H5.H5Screate_simple(RANK, DIMENSIONS, null);

		// Create the int array dataset.
		if ((fid >= 0) && (sid >= 0))
			 did = H5.H5Dcreate(fid, "intDataset",
					HDF5Constants.H5T_INTEGER, sid,
					HDF5Constants.H5P_DEFAULT);
		else
			out.println("> writeIntArrayToDataset FAILED while creating the int array dataset. fid="
					+ fid + ", sid=" + sid);

		// Write 3x3 int array to the dataset using default transfer
		// properties.
		int wid = H5.H5Dwrite(did, HDF5Constants.H5T_NATIVE_INT,
				HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
				HDF5Constants.H5P_DEFAULT, 123);
		if (wid < 0)
			out.println("> writeIntArrayToDataset write FAILED, wid="
					+ wid);

		// End access to the dataset and release resources used by it.
		if (did >= 0)
			H5.H5Dclose(did);
		// Terminate access to the data space.
		if (sid >= 0)
			H5.H5Sclose(sid);
		
		
		// close the VIN group
		if (vingrpid >= 0)
			H5.H5Gclose(vingrpid);

		// close the file
		status = H5.H5Fclose(fid);
		if (!(fid >= 0 && status >= 0))
			throw new IOException("Could not close the HDF5 file properly.");
	}
	
	/**
	 * Reads an image file and apples base-64 encoding to the contents.
	 * 
	 * @param inputFile
	 *            the image file to encode
	 * @return the encoded String
	 * @throws IOException
	 *             when file errors occur
	 */
	static String embedImage(String inputFile) throws IOException {

		// chunksize must be divisible by 3, or the image gets corrupted because
		// filler bytes will be inserted into the middle of the file, as three
		// octets map to four characters. This is the reason that the encoded
		// data
		// is about 1/3 bigger than the binary image data.
		int chunkSize = 72;
		int bufferSize = 144;
		int read = 0;
		byte[] imageChunk = new byte[chunkSize];
		byte[] encoded = new byte[bufferSize];
		String encodedString = "";

		InputStream imageFile = VehicleEmissionsHDF5.class.getClassLoader()
				.getResourceAsStream(inputFile);

		if (imageFile == null) {
			throw new IOException("Input file '" + inputFile
					+ "' not found on class path.");
		}

		// build up encoded string
		while ((read = imageFile.read(imageChunk)) != -1) {
			// encoded = Base64.encodeBase64(imageChunk);
			encodedString += (new String(encoded) + "\n");
		}
		imageFile.close();
		return encodedString;
	}
	
	public static void main(String[] args) throws Exception {
		VehicleEmissionsHDF5 self = new VehicleEmissionsHDF5();
		if(args.length != 2)
			throw new IllegalArgumentException("Must pass input file name and output XML file data path on command line.");
		self.outputDataPath = args[1];
		self.generateFiles(args[0]);
	}

	private class Vehicle {
		String vin;
		String manufacturer;
		Integer modelYear;
		String vehicleType;
		Float oilChangeDistance;
		Float odometer;
		String comments;
		List<Emissions> emissions = new ArrayList<Emissions>();

		public String toString() {
			String s = "\nvin=" + vin + ",\nmanufacturer=" + manufacturer
					+ ",\nmodelYear=" + modelYear + ",\nvehicleType="
					+ vehicleType + ",\noilChangeDistance=" + oilChangeDistance
					+ ",\nodometer=" + odometer + ",\ncomments=" + comments;
			for (int i = 0; i < emissions.size(); i++) {
				s += emissions.get(i).toString();
			}
			return s;
		}
	}

	private class Emissions {
		String dateTested;
		Float exhaustHC;
		Float nonExhaustHC;
		Float exhaustCO;
		Float exhaustNO2;

		public String toString() {
			return "\ndateTested=" + dateTested + ", exhaustHC=" + exhaustHC
					+ ", nonExhaustHC=" + nonExhaustHC + ", exhaustCO="
					+ exhaustCO + ", exhaustNO2=" + exhaustNO2;
		}
	}

}