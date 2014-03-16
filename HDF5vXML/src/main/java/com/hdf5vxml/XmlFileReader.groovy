package com.hdf5vxml;

import org.apache.commons.codec.binary.Base64

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
		def parsedData = new XmlParser().parse(file)

		// parse the 2D array values
		parsedData.arrayGroup.array.val.each{value ->
			Double.parseDouble(value.text())
		}

		// decode the image data
		parsedData.imageGroup.photo.each{ photo ->
			Base64.decodeBase64(photo.text())
		}
		
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
