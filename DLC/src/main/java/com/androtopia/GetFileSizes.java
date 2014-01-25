package com.androtopia;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.DirectoryWalker;

public class GetFileSizes extends DirectoryWalker {

	private String url = "jdbc:mysql://localhost:3306/DLC";
	private Connection con = null;

	private String user;
	private String password;

	public static void main(String[] args) throws IOException, SQLException {
		GetFileSizes self = new GetFileSizes();
		if (args.length != 0)
			throw new IllegalArgumentException(
					"Must pass input file name and output XML file data path on command line.");
		// self.inputCSVFile = args[0];
		// self.outputDataPath = args[1];

		Scanner input = new Scanner(System.in);
		System.out.println("Enter User: ");
		self.user = input.nextLine();
		System.out.println("Enter Password: ");
		self.password = input.nextLine();
		self.con = DriverManager.getConnection(self.url, self.user,
				self.password);

		List results = new ArrayList();
		File startDirectory = new File("C:\\tmp");
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
				|| file.getName().endsWith(".hdf5")) {

			String sql = "update stats set sizeOnDiskInBytes = "
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
