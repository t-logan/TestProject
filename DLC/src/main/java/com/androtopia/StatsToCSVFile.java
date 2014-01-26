package com.androtopia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class StatsToCSVFile {

	public static void main(String[] args) throws IOException, SQLException {

		String url = "jdbc:mysql://localhost:3306/DLC";
		Connection con = null;
		String user;
		String password;
		Statement st = null;
		ResultSet rs = null;

		Scanner input = new Scanner(System.in);
		System.out.println("Enter User: ");
		user = input.nextLine();
		System.out.println("Enter Password: ");
		password = input.nextLine();
		con = DriverManager.getConnection(url, user, password);

		System.out.println("Running ...");

		File file = new File("c:/tmp/stats.csv");
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		String header = "fileName,fileExt,numberOfPhotos,emissionsSamples,sizeOnDiskInBytes,binaryBytes,"
				+ "timeToCreateInMilliseconds,timeToReadInMilliseconds\n";
		bw.write(header);

		String sql = "select fileName, numberOfPhotos, emissionsSamples, sizeOnDiskInBytes, binaryBytes, "
				+ "timeToCreateInMilliseconds, timeToReadInMilliseconds from stats";

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
