package com.hdf5vxml;

import static java.lang.System.out;

import java.text.NumberFormat;

public class HDF5vXML {

	private final IFileGenerator xmlFileGenerator;
	private final IFileReader xmlFileReader;
	private final IFileGenerator hdf5FileGenerator;
	private final IFileReader hdf5FileReader;

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
			// generate XML and HDF5 files
			self.processFiles();

			// generate statistics file
			DATA.toCsvFile(CONFIG.getTargetDir() + "HDF5vXML.csv");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processFiles() throws Exception {

		FileDescriptor fd = new FileDescriptor();
		fd.setCols(CONFIG.getCols());

		NumberFormat nfFileNum = NumberFormat.getIntegerInstance();
		nfFileNum.setGroupingUsed(false);
		nfFileNum.setMinimumIntegerDigits(6);

		for (int i = 1; i <= CONFIG.getFileCount(); i++) {
			fd.setFileName("File" + nfFileNum.format(i));
			hdf5FileGenerator.generate(fd);
			hdf5FileReader.read(fd);
		}
	}
}
