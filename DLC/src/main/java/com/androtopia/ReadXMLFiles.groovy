package com.androtopia;

import static java.lang.System.out

import java.io.Console;
import java.io.InputStream;
import java.nio.charset.Charset
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.DirectoryWalker

/**
 * This is a Groovy program that writes the vehicleEmissions.xml file from CSV input 
 * created by the GenerateCSVFile.java program.
 * @author Tim Logan
 *
 */
public class ReadXMLFiles extends DirectoryWalker {

	private String url = null
	private Connection con = null

	private String user
	private String password
	private String targetDir

	private String encodedConverterPic
	private Zipper zipper = new Zipper()
	private long xmlReadStartTime = 0;
	private long xmlReadTime = 0;
	private long cxmlReadStartTime = 0;
	private long cxmlReadTime = 0;

	public ReadXMLFiles() {
		super();
	}

	/**
	 * Write out an XML file to support Ted's use case of vehicle emissions.
	 * @param args the dataPath is passed as a parameter.
	 */
	public static void main(String[] args) {
		ReadXMLFiles self = new ReadXMLFiles();
		
		// load properties
		Properties props = new Properties();
		ClassLoader cl = GenerateCSVFiles.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("dlc.properties");
		props.load(is);
		is.close();

		self.url = props.getProperty("db.url");
		self.user = props.getProperty("db.user");
		self.password = props.getProperty("db.pw");
		self.con = DriverManager.getConnection(self.url, self.user,
				self.password);

		List results = new ArrayList();
		self.targetDir = props.getProperty("target.dir")
		File startDirectory = new File(self.targetDir);
		println "Running ..."
		self.walk(startDirectory, results);

		if (self.con != null) {
			self.con.close();
		}
		println "Done!"
	}

	protected boolean handleDirectory(File directory, int depth, Collection results) {
		return true;
	}

	protected void handleFile(File file, int depth, Collection results) {
		if(file.getName().endsWith(".xml")) {
			readFiles(file.getAbsolutePath());
			results.add(file);
		}
	}

	/**
	 * Write the XML file by parsing a CSV file containing the data and expressing it as XML.
	 * @param inputFile the name of the CSV file to read.
	 */
	private void readFiles(String inputFile) {
		
		String xmlFileName = ""
		String cxmlFileName = ""
		
		// about to start reading the CSV file
		xmlReadStartTime = System.currentTimeMillis();

		// parse the file
		def file = new File(inputFile)
		def emissions = new XmlParser().parse(file)
		
		// compute times
		xmlReadTime = System.currentTimeMillis() - xmlReadStartTime;

		// time the unzip process ...
		cxmlReadStartTime = System.currentTimeMillis();
		zipper.unzip(inputFile + "c");
		cxmlReadTime = (System.currentTimeMillis() - cxmlReadStartTime) + xmlReadTime;

		xmlFileName = inputFile.substring(targetDir.length())
		cxmlFileName = xmlFileName + "c"
		updateDatabase(xmlFileName, cxmlFileName);
	}

	private void updateDatabase(String xmlFileName, String cxmlFileName) {
		Statement st = null;
		ResultSet rs = null;
		
		String xmlSql = "update Stats set timeToReadInMilliseconds = " + xmlReadTime + " where fileName = \"" + xmlFileName + "\""
		String cxmlSql = "update Stats set timeToReadInMilliseconds = " + cxmlReadTime + " where fileName = \"" + cxmlFileName + "\""
		try {
			st = con.createStatement();
			// record XML stats
			st.execute(xmlSql)
			st.execute("commit")
			// record CXML stats
			st.execute(cxmlSql)
			st.execute("commit")
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
}