package com.hdf5vxml;

import static java.lang.System.out;

import java.io.IOException;

public class HDF5vXML {

	private final XmlFileGenerator xmlFileGenerator;
	private final Hdf5FileGenerator hdf5FileGenerator;

	// globals
	public final static RunConfig CONFIG = RunConfig.INSTANCE;
	public final static StatsData DATA = StatsData.INSTANCE;

	public HDF5vXML() {
		xmlFileGenerator = new XmlFileGenerator();
		hdf5FileGenerator = new Hdf5FileGenerator();
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
	}

	private void generateFiles() {

		FileDescriptor fd = new FileDescriptor();
		fd.setCols(CONFIG.getCols());

		for (int i = 0; i < CONFIG.getFileCount(); i++) {
			fd.setFileName("test" + i);
			writeXmlFile(fd);
			writeHdf5File(fd);
		}
		try {
			DATA.toCsvFile(CONFIG.getTargetDir() + "stats.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeXmlFile(FileDescriptor fileName) {

	}

	private void writeHdf5File(FileDescriptor fileName) {
		try {
			hdf5FileGenerator.writeFile(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
