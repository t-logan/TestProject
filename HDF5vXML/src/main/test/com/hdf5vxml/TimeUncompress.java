package com.hdf5vxml;

import static java.lang.System.out;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Test the consistency of the time to perform an uncompress operation, using
 * File000000.xmlc written by HDF5vXML (which must be run before running this
 * test).
 * 
 */
public class TimeUncompress {

	private static final List<Long> samples = new ArrayList<Long>();

	/**
	 * Time the compress operation 1000 times and store the results in an array.
	 * 
	 * @param args
	 *            Must provide properties file name.
	 * @throws IOException
	 *             if there is a problem creating the output file:
	 *             File000000.txt.
	 */
	public static void main(String[] args) throws IOException {

		if (args.length == 1) {
			// use properties to configure the run
			if (!RunConfig.INSTANCE.init(args[0]))
				System.exit(-1);
			;
		} else {
			out.println("Must provide properties file name on the command line. Quiting.");
			System.exit(-1);
		}

		String inputFile = RunConfig.INSTANCE.getTargetDir() + "File000000.xml";

		Zipper zipper = new Zipper();
		long cxmlReadStartTime = 0;
		long cxmlReadTime = 0;

		for (int i = 0; i < 1000; i++) {
			cxmlReadStartTime = System.currentTimeMillis();
			zipper.unzip(inputFile + "c");
			cxmlReadTime = System.currentTimeMillis() - cxmlReadStartTime;
			samples.add(cxmlReadTime);
		}
		writeSamples();
	}

	/**
	 * Dumps the timing samples from the array to a file.
	 * 
	 * @throws IOException
	 *             if there is a problem creating the output file:
	 *             File000000.txt.
	 */
	private static void writeSamples() throws IOException {

		File file = new File(RunConfig.INSTANCE.getTargetDir()
				+ "File000000.txt");
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		for (Iterator iterator = samples.iterator(); iterator.hasNext();) {
			Long sample = (Long) iterator.next();
			bw.write(sample + "\n");
		}
		bw.close();
	}
}
