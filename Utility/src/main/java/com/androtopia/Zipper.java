package com.androtopia;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

	private final int BUFFER = 2048;

	public Zipper() {
	}

	public void zip(String inputFileName, String outputFileName) {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(outputFileName);
			CheckedOutputStream checksum = new CheckedOutputStream(dest,
					new Adler32());
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					checksum));
			byte data[] = new byte[BUFFER];
			FileInputStream fi = new FileInputStream(inputFileName);
			origin = new BufferedInputStream(fi, BUFFER);
			ZipEntry entry = new ZipEntry(inputFileName);
			out.putNextEntry(entry);
			int count;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
			}
			origin.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
