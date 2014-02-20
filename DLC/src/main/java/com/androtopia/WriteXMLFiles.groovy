package com.androtopia;

import static java.lang.System.out

import java.io.Console;
import java.nio.charset.Charset
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.DirectoryWalker

/**
 * This is a Groovy program that writes the vehicleEmissions.xml file from CSV input 
 * created by the GenerateCSVFile.java program.
 * @author Tim Logan
 *
 */
public class WriteXMLFiles extends DirectoryWalker {

	private String url = null
	private Connection con = null
	private int fileCount = 0

	private Vehicle vehicle
	private groovy.xml.StreamingMarkupBuilder builder
	private Emissions emissions
	private String user;
	private String password;

	private String encodedConverterPic
	private Zipper zipper = new Zipper();
	private long xmlWriteStartTime = 0;
	private long xmlWriteTime = 0;
	private long cxmlWriteStartTime = 0;
	private long cxmlWriteTime = 0;

	private String outputDataPath = ""
	private int emissionsSamples = 0
	private int photoCopies = 0
	private int totalImageBytes = 0
	
	private Properties props
	private PhotoRegnerator photoRegnerator;

	public WriteXMLFiles() {
		super();
		builder = new groovy.xml.StreamingMarkupBuilder()
		builder.encoding = "UTF-8"
	}

	/**
	 * Write out an XML file to support Ted's use case of vehicle emissions.
	 * @param args the dataPath is passed as a parameter.
	 */
	public static void main(String[] args) {
		WriteXMLFiles self = new WriteXMLFiles();

		// load properties
		self.props = new Properties();
		ClassLoader cl = GenerateCSVFiles.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("dlc.properties");
		self.props.load(is);
		is.close();

		self.outputDataPath = self.props.getProperty("target.dir");

		self.url = self.props.getProperty("db.url");
		self.user = self.props.getProperty("db.user");
		self.password = self.props.getProperty("db.pw");
		self.con = DriverManager.getConnection(self.url, self.user,
				self.password);

		self.photoRegnerator = new PhotoRegnerator();
			
		List results = new ArrayList();
		File startDirectory = new File(self.props.getProperty("target.dir"));
		println "Running ..."
		self.walk(startDirectory, results);

		// pick up the last one
		self.emitXml(self.vehicle, self.builder)
		self.updateDatabase();

		if (self.con != null) {
			self.con.close();
		}

		println "Done!"
	}

	// callback routine
	protected boolean handleDirectory(File directory, int depth, Collection results) {
		return true;
	}

	// callback routine
	protected void handleFile(File file, int depth, Collection results) {
		if(file.getName().endsWith(".csv")) {
			photoRegnerator.regenPhotos();
			generateFiles(file.getAbsolutePath());
			//updateDatabase();
			results.add(file);
		}
	}

	private void updateDatabase() {
		Statement st = null;
		ResultSet rs = null;

		String xmlSql = "insert into Stats (fileName, numberOfPhotos, emissionsSamples, binaryBytes, timeToCreateInMilliseconds) values (\"" +
				vehicle.vin + ".xml\"," + photoCopies + "," + emissionsSamples + "," + totalImageBytes + "," +
				xmlWriteTime + ")"

		String cxmlSql = "insert into Stats (fileName, numberOfPhotos, emissionsSamples, binaryBytes, timeToCreateInMilliseconds) values (\"" +
				vehicle.vin + ".xmlc\"," + photoCopies + "," + emissionsSamples + "," + totalImageBytes + "," +
				cxmlWriteTime + ")"
		try {
			st = con.createStatement();
			// record XML stats
			st.execute(xmlSql);
			// record CXML stats
			st.execute(cxmlSql);
			
			totalImageBytes = 0
			
			//totalImageBytes = 0
		} catch (SQLException ex) {
			println ex.getMessage()
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (st != null) {
					st.close();
				}
			} catch (SQLException ex) {
				println ex.getMessage()
			}
		}
	}

	/**
	 * Write the XML file by parsing a CSV file containing the data and expressing it as XML.
	 * @param inputFile the name of the CSV file to read.
	 */
	private void generateFiles(String inputFile) {

		boolean isVehicleRec
		String line
		int lineCount = 0
		int tokenCount = 0

		println inputFile + ": " + ++fileCount

		// about to start reading the CSV file
		xmlWriteStartTime = System.currentTimeMillis();

		FileInputStream fis = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis,
				Charset.forName("UTF-8")))

		// read the CSV file one line at a time
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line)
			lineCount++
			// skip header record
			if(lineCount == 1)
				continue
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
								updateDatabase()
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
						emissions.exhaustHC = Float.parseFloat(tok)
						break
					case 10:
						emissions.nonExhaustHC = Float.parseFloat(tok)
						break
					case 11:
						emissions.exhaustCO = Float.parseFloat(tok)
						break
					case 12:
						emissions.exhaustNO2 = Float.parseFloat(tok)
						break
					case 13:
						emissionsSamples = Integer.parseInt(tok)
						break
					case 14:
						photoCopies = Integer.parseInt(tok)
				}
				// inject the emissions data into the vehicle information

			}
			vehicle.emissions.add(emissions)
		}
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
				for(int i =0; i < photoCopies; i++) {
					//unescaped << "<photo><![CDATA[" + WriteXMLFiles.encodeImage("catalytic-converter-6.jpg") + "]]></photo>"
					unescaped << "<photo><![CDATA[" + encodeImage(
						new FileInputStream(props.getProperty("image.dir") + "photo" + i + ".jpg")) + "]]></photo>"
				}
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

		// compute times
		xmlWriteTime = System.currentTimeMillis() - xmlWriteStartTime;

		// time the zip process ...
		cxmlWriteStartTime = System.currentTimeMillis();
		zipper.zip(outputDataPath + v.vin + ".xml", outputDataPath + v.vin + ".xmlc");
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
		xmlFile.write("<vehicleEmissions xmlns=\"http://www.epa.gov\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
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