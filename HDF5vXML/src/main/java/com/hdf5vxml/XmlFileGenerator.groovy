package com.hdf5vxml;

import org.apache.commons.codec.binary.Base64

public class XmlFileGenerator implements IFileGenerator{

	private final static String XML_EXT = ".xml";
	private final static String XMLC_EXT = ".xmlc";

	private groovy.xml.StreamingMarkupBuilder builder

	private String encodedPic
	private Zipper zipper = new Zipper()
	private long xmlWriteStartTime = 0
	private long xmlWriteTime = 0
	private long cxmlWriteStartTime = 0
	private long cxmlWriteTime = 0

	private int totalImageBytes = 0

	public XmlFileGenerator() {
		super();
		builder = new groovy.xml.StreamingMarkupBuilder()
		builder.encoding = "UTF-8"
	}

	@Override
	public void generate(FileDescriptor fileDescriptor) throws Exception {
		writeXmlFiles(fileDescriptor)
	}

	private void writeXmlFiles(FileDescriptor fileDescriptor) throws Exception {
		emitXml(fileDescriptor, builder)
	}


	/**
	 * This is where the interesting XML is generated.
	 * @param v a vehicle object created from the parsed CSV file.
	 * @param xmlFile the file to write into.
	 * @param builder the MarkupBuilder used to output XML with Groovy.
	 */
	private void emitXml(FileDescriptor fd, groovy.xml.StreamingMarkupBuilder builder) {

		// output XML file
		FileWriter xmlFile = new FileWriter(new File(HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + ".xml"))
		HDF5vXML.DATA.createStatsInfo(fd.getFileName() + XML_EXT);
		HDF5vXML.DATA.setFileExt(fd.getFileName() + XML_EXT, XML_EXT.substring(1));

		// add reference to the schema
		genXmlHeader(xmlFile, builder);

		// build the XML
		def xml = {
			arrayGroup {
				double value = 1.0;
				for (int row = 0; row < fd.getRows(); row++) {
					array {
						for (int col = 0; col < fd.getCols(); col++) {
							val(value++);
						}
					}
				}
			}
			imageGroup {
				for(int i =0; i < fd.getNumberOfPhotos(); i++) {
					unescaped << "<photo><![CDATA[" + encodeImage(
							new FileInputStream(HDF5vXML.CONFIG.getPhotoDir() + "photo" + i + ".jpg")) + "]]></photo>"
				}
			}
		}

		// write out the XML content
		xmlFile << builder.bind(xml)

		// write the final tag and close the file
		xmlFile.write("</testFile>");
		xmlFile.close()

		// compute times
		xmlWriteTime = System.currentTimeMillis() - xmlWriteStartTime;

		// time the zip process ...
		cxmlWriteStartTime = System.currentTimeMillis();
		zipper.zip(HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + ".xml",
				HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + ".xmlc");
		HDF5vXML.DATA.createStatsInfo(fd.getFileName() + XMLC_EXT);
		HDF5vXML.DATA.setFileExt(fd.getFileName() + XMLC_EXT, XMLC_EXT.substring(1));

		cxmlWriteTime = (System.currentTimeMillis() - cxmlWriteStartTime) + xmlWriteTime;
	}

	/**
	 * Write the XML header information and the root tag.
	 * @param xmlFile
	 * @param builder
	 */
	private void genXmlHeader(FileWriter xmlFile, groovy.xml.StreamingMarkupBuilder builder) {
		def xmlHeader = { mkp.xmlDeclaration() }
		xmlFile << builder.bind(xmlHeader)
		// write root tag with schema reference
		xmlFile.write("<testFile xmlns=\"http://www.epa.gov\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:schemaLocation=\"http://www.epa.gov vehicleEmissions.xsd\">");
	}

	/**
	 * Reads an image file and apples base-64 encoding to the contents.
	 * @param inputFile the image file to encode
	 * @return the encoded String
	 * @throws IOException when file errors occur
	 */
	String encodeImage(FileInputStream imageFile) throws IOException {

		// chunksize must be divisible by 3, or the image gets corrupted because
		// filler bytes will be inserted into the middle of the file, as three
		// octets map to four characters. This is the reason that the encoded data
		// is about 1/3 bigger than the binary image data.
		int chunkSize = 720000
		int bufferSize = 1440000
		int read = 0
		int sizeInBytes = 0
		byte[] imageChunk = new byte[chunkSize]
		byte[] encoded = new byte[bufferSize]
		String encodedString = ""

		// build up encoded string
		while ((read = imageFile.read(imageChunk)) != -1) {
			byte[] buf = new byte[read]
			for(int i=0; i < read; i++)
				buf[i] = imageChunk[i]
			encoded = Base64.encodeBase64(buf)
			encodedString += (new String(encoded) + "\n")
			sizeInBytes += read
		}
		imageFile.close()
		totalImageBytes += sizeInBytes
		return encodedString
	}
}
