package com.hdf5vxml;

import static java.lang.System.out;

import java.text.NumberFormat;

import com.androtopia.RandomGaussianGenerator;

public class HDF5vXML {

	private final static String RESULTS_FILE_NAME = "HDF5vXML.csv";

	private final IFileGenerator xmlFileGenerator;
	private final IFileReader xmlFileReader;
	private final IFileGenerator hdf5FileGenerator;
	private final IFileReader hdf5FileReader;
	
	private final FileSizeReader fileSizeReader = new FileSizeReader();

	private RandomGaussianGenerator rowsRandGenerator;
	private RandomGaussianGenerator photosRandGenerator;

	// globals
	public final static RunConfig CONFIG = RunConfig.INSTANCE;
	public final static StatsData DATA = StatsData.INSTANCE;

	public HDF5vXML() {
		xmlFileGenerator = new XmlFileGenerator();
		xmlFileReader = new XmlFileReader();
		hdf5FileGenerator = new Hdf5FileGenerator();
		hdf5FileReader = new Hdf5FileReader();
	}

	public static void main(String[] args) {
		HDF5vXML self = new HDF5vXML();
		out.println("HDF5vXML version 1.0");
		
		if (args.length == 1) {
			// use properties to configure the run
			if (!CONFIG.init(args[0]))
				System.exit(-1);
			;
		} else {
			out.println("Must provide properties file name on the command line. Quiting.");
			System.exit(-1);
		}
		
		self.rowsRandGenerator = new RandomGaussianGenerator(
				CONFIG.getMeanRows(), CONFIG.getSdRows());
		self.photosRandGenerator = new RandomGaussianGenerator(
				CONFIG.getMeanPhotos(), CONFIG.getSdPhotos());

		try {
			// generate XML and HDF5 files
			out.print("Processing " + CONFIG.getFileCount() + " files.\nRunning ");
			self.processFiles();
			
			// update file size information
			self.fileSizeReader.updateFileSizes();

			// generate statistics file
			DATA.toCsvFile(CONFIG.getTargetDir() + RESULTS_FILE_NAME);
			System.out.println("\nResults can be found in: "
					+ CONFIG.getTargetDir() + RESULTS_FILE_NAME);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processFiles() throws Exception {

		FileDescriptor fd = new FileDescriptor();

		NumberFormat nfFileNum = NumberFormat.getIntegerInstance();
		nfFileNum.setGroupingUsed(false);
		nfFileNum.setMinimumIntegerDigits(6);

		for (int i = 1; i <= CONFIG.getFileCount(); i++) {

			fd.setFileName("File" + nfFileNum.format(i));
			fd.setRows(getVariableNumberOfRows());
			fd.setCols(CONFIG.getCols());
			fd.setNumberOfPhotos(getVariableNumberOfPhotos());
			
			// display progress every 50 files
			if(i % 50 == 1)
				out.print(".");

			xmlFileGenerator.generate(fd);
			xmlFileReader.read(fd);
			hdf5FileGenerator.generate(fd);
			hdf5FileReader.read(fd);
		}
	}

	/**
	 * Return a single randomly generated number of rows.
	 * 
	 * @return the number of samples.
	 */
	private int getVariableNumberOfRows() {
		// rows suppressed?
		if (CONFIG.getMeanRows() == 0)
			return 1;

		Double x = rowsRandGenerator.getNextScaledGaussian();
		// insure that there is at least one
		if (x < 1)
			return 1;
		else
			return (int) Math.round(x);
	}

	/**
	 * Return a single randomly generated number of photos.
	 * 
	 * @return the number of photos.
	 */
	private int getVariableNumberOfPhotos() {
		// photos suppressed?
		if (CONFIG.getMeanPhotos() == 0)
			return 0;

		Double x = photosRandGenerator.getNextScaledGaussian();
		// insure that there is at least one
		if (x < 1)
			return 1;
		else
			return (int) Math.round(x);
	}
}
