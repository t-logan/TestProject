package com.hdf5vxml;

import static java.lang.System.out;

import java.io.IOException;
import java.text.NumberFormat;

public class HDF5vXML {

	private final IFileGenerator xmlFileGenerator;
	private final IFileGenerator hdf5FileGenerator;
	private final Hdf5FileReader hdf5FileReader;

	// globals
	public final static RunConfig CONFIG = RunConfig.INSTANCE;
	public final static StatsData DATA = StatsData.INSTANCE;

	public HDF5vXML() {
		xmlFileGenerator = new XmlFileGenerator();
		hdf5FileGenerator = new Hdf5FileGenerator();
		hdf5FileReader = new Hdf5FileReader();
	}

	public static void main(String[] args) {
		HDF5vXML self = new HDF5vXML();
		if (args.length == 1) {
			// use properties to configure the run
			if (!CONFIG.init(args[0]))
				System.exit(-1);
			;
		} else {
			out.println("Must provide properties file name on the command line. Quiting.");
			System.exit(-1);
		}
		// generate XML and HDF5 files
		self.generateFiles();
		
		// read XML and HDF5 files
		try {
			self.readFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// generate statistics CSV file
		try {
			DATA.toCsvFile(CONFIG.getTargetDir() + "HDF5vXML.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void generateFiles() {

		FileDescriptor fd = new FileDescriptor();
		fd.setCols(CONFIG.getCols());

		NumberFormat nfFileNum = NumberFormat.getIntegerInstance();
		nfFileNum.setGroupingUsed(false);
		nfFileNum.setMinimumIntegerDigits(6);

		for (int i = 1; i <= CONFIG.getFileCount(); i++) {
			fd.setFileName("File" + nfFileNum.format(i));
			writeXmlFile(fd);
			writeHdf5File(fd);
		}
	}
	
	private void readFiles() throws Exception {
		
		NumberFormat nfFileNum = NumberFormat.getIntegerInstance();
		nfFileNum.setGroupingUsed(false);
		nfFileNum.setMinimumIntegerDigits(6);

		int i = 1;
		FileDescriptor fd = new FileDescriptor();
		fd.setFileName("File" + nfFileNum.format(i));
		fd.setCols(CONFIG.getCols());

		hdf5FileReader.read(fd);
	}

	private void writeXmlFile(FileDescriptor fileName) {
		// TODO
	}

	private void writeHdf5File(FileDescriptor fileName) {
		try {
			hdf5FileGenerator.generate(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
