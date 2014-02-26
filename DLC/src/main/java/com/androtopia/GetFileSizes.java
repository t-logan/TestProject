package com.androtopia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.DirectoryWalker;

public class GetFileSizes extends DirectoryWalker {

	private String url = null;
	private Connection con = null;
	private String targetDir;

	private String user;
	private String password;

	public static void main(String[] args) throws IOException, SQLException {
		GetFileSizes self = new GetFileSizes();

		// load properties
		Properties props = new Properties();
		ClassLoader cl = GenerateCSVFiles.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("dlc.properties");
		props.load(is);
		is.close();

		self.targetDir = props.getProperty("target.dir");

		self.url = props.getProperty("db.url");
		self.user = props.getProperty("db.user");
		self.password = props.getProperty("db.pw");
		self.con = DriverManager.getConnection(self.url, self.user,
				self.password);

		List results = new ArrayList();
		File startDirectory = new File(self.targetDir);
		System.out.println("Running ...");
		self.walk(startDirectory, results);
		System.out.println("Done!");
	}

	protected boolean handleDirectory(File directory, int depth,
			Collection results) {
		return true;
	}

	protected void handleFile(File file, int depth, Collection results) {
		Statement st = null;
		ResultSet rs = null;

		if (file.getName().endsWith(".xml") || file.getName().endsWith(".xmlc")
				|| file.getName().endsWith(".hdf5a")
				|| file.getName().endsWith(".hdf5b")) {

			String sql = "update Stats set sizeOnDiskInBytes = "
					+ file.length() + " where fileName = \"" + file.getName()
					+ "\"";

			try {
				st = con.createStatement();
				st.execute(sql);
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
					if (st != null) {
						st.close();
					}
				} catch (SQLException ex) {
					System.out.println(ex.getMessage());
				}
			}
			results.add(file);
		}
	}
}
