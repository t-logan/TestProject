package com.hdf5vxml;

import org.apache.commons.codec.binary.Base64

public class XmlFileGenerator implements IFileGenerator{

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
		fileDescriptor.setFileExt(FileDescriptor.XML_EXT);
		emitXml(fileDescriptor, builder)
		fileDescriptor.setFileExt(FileDescriptor.XMLC_EXT);
		emitXml(fileDescriptor, builder)
	}

	/**
	 * This is where the interesting XML is generated.
	 * @param v a vehicle object created from the parsed CSV file.
	 * @param xmlFile the file to write into.
	 * @param builder the MarkupBuilder used to output XML with Groovy.
	 */
	private void emitXml(FileDescriptor fd, groovy.xml.StreamingMarkupBuilder builder) {
		
		String photoFile

		HDF5vXML.DATA.createStatsInfo(fd.getFileName() + fd.getFileExt());
		HDF5vXML.DATA.setFileExt(fd.getFileName() + fd.getFileExt(), fd.getFileExt().substring(1));
		HDF5vXML.DATA.setNumberOfPhotos(fd.getFileName() + fd.getFileExt(), fd.getNumberOfPhotos());
		HDF5vXML.DATA.setEmissionsSamples(fd.getFileName() + fd.getFileExt(), fd.getRows());

		xmlWriteStartTime = System.currentTimeMillis();

		// output XML file - overwrite it when processing .xmlc files
		FileWriter xmlFile = new FileWriter(new File(HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + FileDescriptor.XML_EXT))

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
			HDF5vXML.PHOTOS.top();
			imageGroup {
				for(int i =0; i < fd.getNumberOfPhotos(); i++) {
					photoFile = HDF5vXML.PHOTOS.next()
					photo(id:photoFile) {
					unescaped << "<![CDATA[" + encodeImage(
							new FileInputStream(photoFile)) + "]]>"
					}
				}
			}
		}

		// write out the XML content
		xmlFile << builder.bind(xml)

		// write the final tag and close the file
		xmlFile.write("</testFile>");
		xmlFile.close()

		HDF5vXML.DATA.setBinaryBytes(fd.getFileName() + fd.getFileExt(), totalImageBytes);

		// compute times
		xmlWriteTime = System.currentTimeMillis() - xmlWriteStartTime;
		HDF5vXML.DATA.setTimeToCreateInMilliseconds(fd.getFileName() + fd.getFileExt(), xmlWriteTime);

		// time the zip process and add it to write time ...
		if(fd.getFileExt().equals(FileDescriptor.XMLC_EXT)) {
			cxmlWriteStartTime = System.currentTimeMillis();

			zipper.zip(HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + FileDescriptor.XML_EXT,
					HDF5vXML.CONFIG.getTargetDir() + fd.getFileName() + FileDescriptor.XMLC_EXT);

			cxmlWriteTime = (System.currentTimeMillis() - cxmlWriteStartTime) + xmlWriteTime;
			HDF5vXML.DATA.setTimeToCreateInMilliseconds(fd.getFileName() + fd.getFileExt(), cxmlWriteTime);
		}
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
		xmlFile.write("<testFile xmlns=\"http://www.epa.gov\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
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
