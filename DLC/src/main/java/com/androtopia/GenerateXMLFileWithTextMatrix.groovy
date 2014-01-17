package com.androtopia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.StringTokenizer;
import static java.lang.System.out;

import org.apache.commons.codec.binary.Base64;

/**
 * This is a Groovy program that writes the vehicleEmissions.xml file from CSV input 
 * created by the GenerateCSVFile.java program.
 * @author Tim Logan
 *
 */
public class GenerateXMLFileWithTextMatrix {

	private String encodedConverterPic

	// inputCSVFile and inputDataPath are passed as a command line parameters
	private String inputCSVFile = "";
	private String outputDataPath = "";

	/**
	 * Write out an XML file to support Ted's use case of vehicle emissions.
	 * @param args the dataPath is passed as a parameter.
	 */
	public static void main(String[] args) {
		GenerateXMLFileWithTextMatrix self = new GenerateXMLFileWithTextMatrix();
		if(args.size() != 2)
			throw new IllegalArgumentException("Must pass input file name and output XML file data path on command line.");
		self.inputCSVFile = args[0];
		self.outputDataPath = args[1];
		self.generateFiles(self.inputCSVFile);
		println "Done!"
	}

	/**
	 * Write the XML file by parsing a CSV file containing the data and expressing it as XML.
	 * @param inputFile the name of the CSV file to read.
	 */
	private void generateFiles(String inputFile) {

		groovy.xml.StreamingMarkupBuilder builder = new groovy.xml.StreamingMarkupBuilder()
		builder.encoding = "UTF-8"

		Vehicle vehicle
		Emissions emissions
		boolean isVehicleRec
		String line
		int lineCount = 0
		int tokenCount = 0
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis,
				Charset.forName("UTF-8")))

		// read the seed file one line at a time
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line)
			lineCount++
			// skip header record
			if(lineCount == 1)
				continue
			//println line
			tokenCount = 0
			emissions = new Emissions()
			while (st.hasMoreTokens()) {
				String tok = st.nextToken(",")
				tokenCount++;
				switch(tokenCount) {
					// vin
					case 1:
						if(tok.equals("\"\""))
							isVehicleRec = false;
						else {
							if(vehicle != null) {
								emitXml(vehicle, builder)
							}
							vehicle = new Vehicle()
							vehicle.vin = tok
							isVehicleRec = true;
						}
						break
					case 2:
						if(isVehicleRec)
							vehicle.manufacturer = tok.substring(1, tok.length() - 1)
						break
					case 3:
						if(isVehicleRec)
							vehicle.modelYear = Integer.parseInt(tok)
						break
					case 4:
						if(isVehicleRec)
							vehicle.vehicleType = tok
						break
					case 5:
						if(isVehicleRec)
							vehicle.oilChangeDistance = Float.parseFloat(tok)
						break
					case 6:
						if(isVehicleRec)
							vehicle.odometer = Float.parseFloat(tok)
						break
					case 7:
						if(isVehicleRec)
							vehicle.comments = tok.substring(1, tok.length() - 1)
						break
					case 8:
						emissions.dateTested = tok
						break
					case 9:
						emissions.exhaustHC = Float.parseFloat(tok);
						break
					case 10:
						emissions.nonExhaustHC = Float.parseFloat(tok);
						break
					case 11:
						emissions.exhaustCO = Float.parseFloat(tok);
						break
					case 12:
						emissions.exhaustNO2 = Float.parseFloat(tok);
						break
				}
				// inject the emissions data into the vehicle information
			}
			vehicle.emissions.add(emissions)
		}
		// pick up the last vehicle
		emitXml(vehicle, builder)
		br.close();
	}

	/**
	 * This is where the interesting XML is generated. 
	 * @param v a vehicle object created from the parsed CSV file.
	 * @param xmlFile the file to write into.
	 * @param builder the MarkupBuilder used to output XML with Groovy.
	 */
	private void emitXml(Vehicle v, groovy.xml.StreamingMarkupBuilder builder) {
		
		// output XML file
		FileWriter xmlFile = new FileWriter(new File(outputDataPath + v.vin + ".xml"))
		
		// encode the catalytic converter picture included in every vehicle entity
		encodedConverterPic = GenerateXMLFileWithTextMatrix.encodeImage("converter.jpg")

		// add reference to the schema
		genXmlHeader(xmlFile, builder);

		// build the XML
		def vehicleXml = {
			vehicle() {
				vin(v.vin )
				manufacturer(v.manufacturer )
				modelYear(v.modelYear )
				vehicleType(v.vehicleType)
				oilChangeDistance(v.oilChangeDistance)
				odometer(v.odometer)
				comments(v.comments)
				unescaped << "<photo><![CDATA[" + encodedConverterPic + "]]></photo>"
				v.emissions.each{ e->
					emission() {
						dateTested(e.dateTested)
						exhaustHC(e.exhaustHC)
						nonExhaustHC(e.nonExhaustHC)
						exhaustCO(e.exhaustCO)
						exhaustNO2(e.exhaustNO2) }
				}
			}
		}
		
		// write out the XML content
		xmlFile << builder.bind(vehicleXml)
		
		// write the final tag and close the file
		xmlFile.write("</vehicleEmissions>");
		xmlFile.close()
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
		xmlFile.write("<vehicleEmissions xmlns=\"http://www.epa.gov\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:schemaLocation=\"http://www.epa.gov vehicleEmissions.xsd\">");
	}

	/**
	 * Reads an image file and apples base-64 encoding to the contents.
	 * @param inputFile the image file to encode
	 * @return the encoded String
	 * @throws IOException when file errors occur
	 */
	static String encodeImage(String inputFile) throws IOException {

		// chunksize must be divisible by 3, or the image gets corrupted because
		// filler bytes will be inserted into the middle of the file, as three
		// octets map to four characters. This is the reason that the encoded data
		// is about 1/3 bigger than the binary image data.
		int chunkSize = 72
		int bufferSize = 144
		int read = 0
		byte[] imageChunk = new byte[chunkSize]
		byte[] encoded = new byte[bufferSize]
		String encodedString = ""

		InputStream imageFile = GenerateXMLFileWithTextMatrix.class.getClassLoader()
				.getResourceAsStream(inputFile);

		if (imageFile == null) {
			throw new IOException("Input file '" + inputFile
			+ "' not found on class path.");
		}

		// build up encoded string
		while ((read = imageFile.read(imageChunk)) != -1) {
			encoded = Base64.encodeBase64(imageChunk)
			encodedString += (new String(encoded) + "\n")
		}
		imageFile.close()
		return encodedString
	}

	private class Vehicle {
		String vin
		String manufacturer
		Integer modelYear
		String vehicleType
		Float oilChangeDistance
		Float odometer
		String comments
		List<Emissions> emissions = new ArrayList<String>()

		public String toString() {
			String s = "\nvin=" + vin + ",\nmanufacturer=" + manufacturer + ",\nmodelYear=" + modelYear + ",\nvehicleType=" + vehicleType +
					",\noilChangeDistance=" + oilChangeDistance + ",\nodometer=" + odometer + ",\ncomments=" + comments
			for(int i = 0; i < emissions.size(); i++) {
				s+= emissions.get(i).toString()
			}
			return s;
		}
	}

	private class Emissions {
		String dateTested
		Float exhaustHC
		Float nonExhaustHC
		Float exhaustCO
		Float exhaustNO2

		public String toString() {
			return "\ndateTested=" + dateTested + ", exhaustHC=" + exhaustHC + ", nonExhaustHC=" + nonExhaustHC +
			", exhaustCO=" + exhaustCO + ", exhaustNO2=" + exhaustNO2
		}
	}
}