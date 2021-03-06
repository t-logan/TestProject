package com.hdf5vxml;

import static java.lang.System.out;

import java.io.IOException;
import java.text.NumberFormat;

import com.hdf5vxml.RandomGaussianGenerator;

public class HDF5vXML {

	private final static String VERSION = "1.0";
	private final static String RESULTS_FILE_NAME = "HDF5vXML.csv";

	private final IFileGenerator xmlFileGenerator;
	private final IFileReader xmlFileReader;
	private final IFileGenerator hdf5FileGenerator;
	private final IFileReader hdf5FileReader;
	
	private final FileSizeReader fileSizeReader = new FileSizeReader();
	private final static WorkspaceCleaner workspaceCleaner = new WorkspaceCleaner();

	private RandomGaussianGenerator rowsRandGenerator;
	private RandomGaussianGenerator photosRandGenerator;

	// globals
	public final static RunConfig CONFIG = RunConfig.INSTANCE;
	public final static StatsData DATA = StatsData.INSTANCE;
	public static PhotoDispenser PHOTOS = PhotoDispenser.INSTANCE;

	public HDF5vXML() {
		xmlFileGenerator = new XmlFileGenerator();
		xmlFileReader = new XmlFileReader();
		hdf5FileGenerator = new Hdf5FileGenerator();
		hdf5FileReader = new Hdf5FileReader();
	}

	public static void main(String[] args) {
		HDF5vXML self = new HDF5vXML();
		out.println("HDF5vXML version " + VERSION);
		
		if (args.length == 1) {
			// use properties to configure the run
			if (!CONFIG.init(args[0]))
				System.exit(-1);
			;
		} else {
			out.println("Must provide properties file name on the command line. Quiting.");
			System.exit(-1);
		}
		
		try {
			PHOTOS.init();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 
		self.rowsRandGenerator = new RandomGaussianGenerator(
				CONFIG.getMeanRows(), CONFIG.getSdRows());
		self.photosRandGenerator = new RandomGaussianGenerator(
				CONFIG.getMeanPhotos(), CONFIG.getSdPhotos());

		try {
			// clean the workspace
			out.println("Cleaning ... ");
			workspaceCleaner.cleanWorkingDir();
			
			// generate XML and HDF5 files
			out.print("File count is " + (CONFIG.getFileCount() - 1) + ".\nProcessing ");
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

		for (int i = 0; i < CONFIG.getFileCount(); i++) {

			fd.setFileName("File" + nfFileNum.format(i));
			fd.setRows(getVariableNumberOfRows());
			fd.setCols(CONFIG.getCols());
			fd.setNumberOfPhotos(getVariableNumberOfPhotos());
			
			// display progress every 50 files
			if(i % 50 == 1)
				out.print(".");

			// generate and read the XML files
			xmlFileGenerator.generate(fd);
			xmlFileReader.read(fd);
			
			// generate and read the HDF5 files
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
			return 0;

		Double x = rowsRandGenerator.getNextScaledGaussian();
		if (x < 1)
			return 0;
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
		if (x < 1)
			return 0;
		else
			return (int) Math.round(x);
	}
}
