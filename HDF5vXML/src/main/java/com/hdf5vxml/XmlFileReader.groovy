package com.hdf5vxml;

public class XmlFileReader implements IFileReader {

	@Override
	public void read(FileDescriptor fileDescriptor) throws Exception {

		Zipper zipper = new Zipper()
		long xmlReadStartTime = 0;
		long xmlReadTime = 0;
		long cxmlReadStartTime = 0;
		long cxmlReadTime = 0;

		String inputFile = HDF5vXML.CONFIG.getTargetDir() + fileDescriptor.getFileName() + ".xml"

		// about to start reading the CSV file
		xmlReadStartTime = System.currentTimeMillis()

		// parse the file
		def file = new File(inputFile)
		def emissions = new XmlParser().parse(file)

		// compute times
		xmlReadTime = System.currentTimeMillis() - xmlReadStartTime
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName() + ".xml", xmlReadTime)

		// time the unzip process ...
		cxmlReadStartTime = System.currentTimeMillis()
		zipper.unzip(inputFile + "c")
		cxmlReadTime = (System.currentTimeMillis() - cxmlReadStartTime) + xmlReadTime
		HDF5vXML.DATA.setTimeToReadInMilliseconds(fileDescriptor.getFileName() + ".xmlc", cxmlReadTime)
	}
}
