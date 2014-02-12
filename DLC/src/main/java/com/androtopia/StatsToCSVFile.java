package com.androtopia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class StatsToCSVFile {

	public static void main(String[] args) throws IOException, SQLException {

		String url = null;
		Connection con = null;
		String user;
		String password;
		Statement st = null;
		ResultSet rs = null;

		// load properties
		Properties props = new Properties();
		ClassLoader cl = GenerateCSVFiles.class.getClassLoader();
		InputStream is = cl.getResourceAsStream("dlc.properties");
		props.load(is);
		is.close();

		url = props.getProperty("db.url");
		user = props.getProperty("db.user");
		password = props.getProperty("db.pw");
		con = DriverManager.getConnection(url, user, password);

		System.out.println("Running ...");

		File file = new File(props.getProperty("target.dir") + "stats.csv");
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		String header = "fileName,fileExt,numberOfPhotos,emissionsSamples,sizeOnDiskInBytes,binaryBytes,"
				+ "timeToCreateInMilliseconds,timeToReadInMilliseconds\n";
		bw.write(header);

		String sql = "select fileName, numberOfPhotos, emissionsSamples, sizeOnDiskInBytes, binaryBytes, "
				+ "timeToCreateInMilliseconds, timeToReadInMilliseconds from Stats";

		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);

			while (rs.next()) {
				String fileName = rs.getString("fileName");
				Integer numberOfPhotos = rs.getInt("numberOfPhotos");
				Integer emissionsSamples = rs.getInt("emissionsSamples");
				Integer sizeOnDiskInBytes = rs.getInt("sizeOnDiskInBytes");
				Integer binaryBytes = rs.getInt("binaryBytes");
				Long timeToCreateInMilliseconds = rs
						.getLong("timeToCreateInMilliseconds");
				Long timeToReadInMilliseconds = rs
						.getLong("timeToReadInMilliseconds");
				bw.write(String.format(
						"%s,%s,%d,%d,%d,%d,%d,%d\n",
						fileName,
						fileName.substring(fileName.indexOf(".") + 1,
								fileName.length()), numberOfPhotos,
						emissionsSamples, sizeOnDiskInBytes, binaryBytes,
						timeToCreateInMilliseconds, timeToReadInMilliseconds));
			}
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

		bw.close();

		System.out.println("Done!");
	}
}
