package com.androtopia;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.IllegalFormatWidthException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

public class GenerateCSVFile {

	private static final String HEADER = "VIN_NUMBER,MANUFACTURER,MODEL_YEAR,VEHICLE_TYPE,OIL_CHANGE_DISTANCE,"
			+ "ODOMETER,COMMENTS,DATE_TESTED,EXHAUST_HC,NON_EXHAUST_HC,EXHAUST_CO,EXHAUST_NO2,SAMPLE_COUNT\n";

	// default variables that control the volume of output
	private int numberOfVehicles = 100;
	private int maxSamples = 10;

	private static String dataPath = "";
	private static String csvFile = "";
	private static int numberOfSamples = 0;

	// used to access rows in the emissionData table
	private static final int EX_HC_LIGHT_DUTY_VEHICLE = 0;
	private static final int NONEX_HC_LIGHT_DUTY_VEHICLE = 1;
	private static final int EX_CO_LIGHT_DUTY_VEHICLE = 2;
	private static final int EX_NO2_LIGHT_DUTY_VEHICLE = 3;
	private static final int EX_HC_LIGHT_DUTY_TRUCK = 4;
	private static final int NONEX_HC_LIGHT_DUTY_TRUCK = 5;
	private static final int EX_CO_LIGHT_DUTY_TRUCK = 6;
	private static final int EX_NO2_LIGHT_DUTY_TRUCK = 7;
	private static final int EX_HC_HEAVY_DUTY_VEHICLE = 8;
	private static final int NONEX_HC_HEAVY_DUTY_VEHICLE = 9;
	private static final int EX_CO_HEAVY_DUTY_VEHICLE = 10;
	private static final int EX_NO2_HEAVY_DUTY_VEHICLE = 11;
	private static final int EX_HC_MOTORCYCLE = 12;
	private static final int NONEX_HC_MOTORCYCLE = 13;
	private static final int EX_CO_MOTORCYCLE = 14;
	private static final int EX_NO2_MOTORCYCLE = 15;

	// vehicle types
	private static final int LIGHT_DUTY_VEHICLE = 0;
	private static final int LIGHT_DUTY_TRUCK = 1;
	private static final int HEAVY_DUTY_VEHICLE = 2;
	private static final int MOTORCYCLE = 3;

	// used to access columns in the emissionData table.
	private static Map<Integer, Integer> yearIndexMap = new HashMap<Integer, Integer>();

	// emissionData table
	private static int EMISSION_DATA_ROWS = 16;
	private static final int EMISSION_DATA_COLS = 21;
	private static float[][] emissionData = new float[EMISSION_DATA_ROWS][EMISSION_DATA_COLS];

	// World Manufacturer Identification (WMI) number map.
	private static Map<String, String> wmiTable = new HashMap<String, String>();

	// model year VIN encoding map.
	private static Map<String, Integer> modelYearTable = new HashMap<String, Integer>();

	private Random randomGenerator = new Random();
	private RandomGaussianGenerator normalDistGenerator = new RandomGaussianGenerator();

	private int modelYear = 0;
	private String dateTested;
	private String manufacturer;
	private String vehicleType;
	private double exhaustHC;
	private double nonExhaustHC;
	private double exhaustCO;
	private double exhaustNO2;
	
	private static final String COMMENT = 
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"Fluxum flap dum dum. Foo bar zany somethingnew any words will do. Just trying to make some 123456789" +
	"realistic text to plug into the comment field. Llijihgph wpokrepoj slkaligihe; lkjslijlie jsill. jjj";

	String emissionsDataFile;
	FileWriter outFile;

	public GenerateCSVFile() {
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 *            vehicles sample_ceiling data_path
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String vin;
		if (args.length != 4)
			throw new IllegalArgumentException(
					"Must pass <numberOfVehicles> <maxSamples> <emissions data file name> <output path> on the command line.");
		GenerateCSVFile self = new GenerateCSVFile();
		self.numberOfVehicles = Integer.parseInt(args[0]);
		self.maxSamples = Integer.parseInt(args[1]);
		self.emissionsDataFile = args[2];
		self.dataPath = args[3];
		self.init();
		for (int i = 0; i < self.numberOfVehicles; i++) {
			vin = self.buildVin();
			self.outFile = new FileWriter(dataPath + vin + ".csv");
			self.outFile.write(HEADER);
			numberOfSamples = self.getVariableNumberOfSamples();
			self.generate(numberOfSamples, vin);
			self.outFile.close();
		}
		out.println("Generation done.");
	}

	/**
	 * Initialize lookup tables.
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {
		initEmissionData(emissionsDataFile);
		initYearIndexMap();
		initWmiTable();
		initModelYearTable();
		out.println("Initialization done.");
	}

	/**
	 * Generate the CSV file
	 * 
	 * @param numberOfSamples
	 *            the ceiling on the number of emission samples/vehicle.
	 * @throws IOException
	 */
	private void generate(int numberOfSamples, String vin) throws IOException {
		int randomVehicleType = 0;
		boolean supress = true;
		for (int i = 0; i < numberOfSamples; i++) {
			if (i == 0)
				supress = false;
			else
				supress = true;
			randomVehicleType = getRandomVehicleType();
			writeVehicleData(outFile, randomVehicleType, vin, supress);
			writeEmissionData(outFile, randomVehicleType);
			outFile.write("\n");
		}
	}

	private void writeVehicleData(FileWriter outFile, int randomVehicleType, String vin, boolean blank)
			throws IOException {
		if (blank) {
			outFile.write("\"\",\"\",\"\",\"\",\"\",\"\",\"\",");
			return;
		}
		outFile.write(vin + ",");
		outFile.write("\"" + manufacturer + "\",");
		outFile.write(modelYear + ",");
		switch (randomVehicleType) {
		case LIGHT_DUTY_VEHICLE:
			vehicleType = "LIGHT_DUTY_VEHICLE";
			break;
		case LIGHT_DUTY_TRUCK:
			vehicleType = "LIGHT_DUTY_TRUCK";
			break;
		case HEAVY_DUTY_VEHICLE:
			vehicleType = "HEAVY_DUTY_VEHICLE";
			break;
		case MOTORCYCLE:
			vehicleType = "MOTORCYCLE";
		}
		outFile.write(vehicleType + ",");
		outFile.write(getRandomOilChangeDistance() + ",");
		outFile.write(getRandomOdometerReading() + ",");
		outFile.write(getRandomComment() +",");
	}

	private void writeEmissionData(FileWriter outFile, int randomVehicleType) throws IOException {
		int yearIndex = getModelYearEmissionIndex(modelYear);
		exhaustHC = 0;
		nonExhaustHC = 0;
		exhaustCO = 0;
		exhaustNO2 = 0;
		switch (randomVehicleType) {
		case LIGHT_DUTY_VEHICLE:
			exhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[EX_HC_LIGHT_DUTY_VEHICLE][yearIndex], .3);
			nonExhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[NONEX_HC_LIGHT_DUTY_VEHICLE][yearIndex], .3);
			exhaustCO = normalDistGenerator.getNextGaussian(
					emissionData[EX_CO_LIGHT_DUTY_VEHICLE][yearIndex], 2);
			exhaustNO2 = normalDistGenerator.getNextGaussian(
					emissionData[EX_NO2_LIGHT_DUTY_VEHICLE][yearIndex], .5);
			break;
		case LIGHT_DUTY_TRUCK:
			exhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[EX_HC_LIGHT_DUTY_TRUCK][yearIndex], .3);
			nonExhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[NONEX_HC_LIGHT_DUTY_TRUCK][yearIndex], .3);
			exhaustCO = normalDistGenerator.getNextGaussian(
					emissionData[EX_CO_LIGHT_DUTY_TRUCK][yearIndex], 2);
			exhaustNO2 = normalDistGenerator.getNextGaussian(
					emissionData[EX_NO2_LIGHT_DUTY_TRUCK][yearIndex], .5);
			break;
		case HEAVY_DUTY_VEHICLE:
			exhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[EX_HC_HEAVY_DUTY_VEHICLE][yearIndex], .3);
			nonExhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[NONEX_HC_HEAVY_DUTY_VEHICLE][yearIndex], .3);
			exhaustCO = normalDistGenerator.getNextGaussian(
					emissionData[EX_CO_HEAVY_DUTY_VEHICLE][yearIndex], 2);
			exhaustNO2 = normalDistGenerator.getNextGaussian(
					emissionData[EX_NO2_HEAVY_DUTY_VEHICLE][yearIndex], .5);
			break;
		case MOTORCYCLE:
			exhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[EX_HC_MOTORCYCLE][yearIndex], 0);
			nonExhaustHC = normalDistGenerator.getNextGaussian(
					emissionData[NONEX_HC_MOTORCYCLE][yearIndex], 0);
			exhaustCO = normalDistGenerator.getNextGaussian(
					emissionData[EX_CO_MOTORCYCLE][yearIndex], 0);
			exhaustNO2 = normalDistGenerator.getNextGaussian(
					emissionData[EX_NO2_MOTORCYCLE][yearIndex], 0);
		}
		dateTested = getRandomTestDate();
		outFile.write(dateTested + ",");
		if (exhaustHC < 0)
			exhaustHC = -1 * exhaustHC;
		if (nonExhaustHC < 0)
			nonExhaustHC = -1 * nonExhaustHC;
		if (exhaustCO < 0)
			exhaustCO = -1 * exhaustCO;
		if (exhaustNO2 < 0)
			exhaustNO2 = -1 * exhaustNO2;
		outFile.write(exhaustHC + ",");
		outFile.write(nonExhaustHC + ",");
		outFile.write(exhaustCO + ",");
		outFile.write(exhaustNO2 + ",");
		outFile.write("" + numberOfSamples);
	}

	/**
	 * Loads emission data from DOT table 04-43. The input is trusted, so there
	 * is not the usual bounds checking.
	 * 
	 * @param inputFile
	 *            the 04-43 table data.
	 * @throws IOException
	 *             if the file is not found.
	 */
	private void initEmissionData(String inputFile) throws IOException {
		String line;
		int row = 0, col = 0;
		FileInputStream fis = new FileInputStream(inputFile);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis,
				Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			col = 0;
			while (st.hasMoreTokens()) {
				emissionData[row][col] = Float.parseFloat(st.nextToken(","));
				col++;
			}
			++row;
		}
		br.close();
	}

	/**
	 * Populate the map that associates model year with the column index for
	 * table 04-43.
	 */
	private void initYearIndexMap() {
		int startingYear = 1990;
		for (int i = 0; i < EMISSION_DATA_COLS; i++) {
			yearIndexMap.put(startingYear++, i);
		}
	}

	private void initWmiTable() {
		wmiTable.put("1H", "Honda");
		wmiTable.put("1J", "Jeep");
		wmiTable.put("1L", "Lincoln");
		wmiTable.put("1M", "Mercury");
		wmiTable.put("1N", "Nissan");
		wmiTable.put("1VW", "Volkswegen");
		wmiTable.put("1YV", "Mazda");
		wmiTable.put("2F", "Ford (Canada)");
		wmiTable.put("2G", "General Motors (Canada)");
		wmiTable.put("2G1", "Chevrolet (Canada)");
		wmiTable.put("2G2", "Pontiac (Canada)");
		wmiTable.put("2H", "Honda (Canada)");
		wmiTable.put("2HM", "Hyundai (Canada)");
		wmiTable.put("2M", "Mercury (Canada)");
		wmiTable.put("3F", "Ford (Mexico)");
		wmiTable.put("3G", "General Motors (Mexico)");
		wmiTable.put("3VW", "Volkswegen (Mexico)");
		wmiTable.put("4F", "Mazda");
		wmiTable.put("4M", "Mercury");
		wmiTable.put("4S", "Subaru");
		wmiTable.put("4T", "Toyota");
		wmiTable.put("4US", "BMW");
		wmiTable.put("5F", "Honda");
		wmiTable.put("5L", "Lincoln");
		wmiTable.put("5YJ", "Tesla");
	}

	private void initModelYearTable() {
		modelYearTable.put("L", 1990);
		modelYearTable.put("M", 1991);
		modelYearTable.put("N", 1992);
		modelYearTable.put("P", 1993);
		modelYearTable.put("R", 1994);
		modelYearTable.put("S", 1995);
		modelYearTable.put("T", 1996);
		modelYearTable.put("V", 1997);
		modelYearTable.put("W", 1998);
		modelYearTable.put("X", 1999);
		modelYearTable.put("Y", 2000);
		modelYearTable.put("1", 2001);
		modelYearTable.put("2", 2002);
		modelYearTable.put("3", 2003);
		modelYearTable.put("4", 2004);
		modelYearTable.put("5", 2005);
		modelYearTable.put("6", 2006);
		modelYearTable.put("7", 2007);
		modelYearTable.put("8", 2008);
		modelYearTable.put("9", 2009);
		modelYearTable.put("A", 2010);
	}

	/**
	 * Constructs a pseudo Vehicle Identification Number.
	 * 
	 * @return the VIN.
	 */
	private String buildVin() {

		String wmi = getRandomWMI();
		String vin = wmi;
		// pad WMI, if necessary
		if (vin.length() == 2)
			vin += getRandomVinCharacter();
		// build VDS
		for (int i = 0; i < 6; i++)
			vin += getRandomVinCharacter();
		// model year
		vin += getRandomModelYearCode();
		// fill the rest
		for (int i = 0; i < 7; i++)
			vin += getRandomVinCharacter();
		// 17 characters in all
		if (vin.length() != 17)
			throw new IllegalFormatWidthException(17);
		return vin;
	}

	/**
	 * Selects a random WMI value from the wmiTable.
	 * 
	 * @return a randomly selected WMI.
	 */
	private String getRandomWMI() {
		String key = "";
		int i = 0;
		int randomIndex = randomGenerator.nextInt(wmiTable.size());
		Iterator<String> keys = wmiTable.keySet().iterator();
		while (keys.hasNext() && i <= randomIndex) {
			key = keys.next();
			i++;
		}
		manufacturer = wmiTable.get(key);
		return key;
	}

	/**
	 * Selects a random model year code from the modelYearTable.
	 * 
	 * @return a randomly selected model year.
	 */
	private String getRandomModelYearCode() {
		String code = "";
		int i = 0;
		int randomIndex = randomGenerator.nextInt(modelYearTable.size());
		Iterator<String> codes = modelYearTable.keySet().iterator();
		while (codes.hasNext() && i <= randomIndex) {
			code = codes.next();
			i++;
		}
		modelYear = modelYearTable.get(code);
		return code;
	}

	/**
	 * Return a single randomly generated VIN character.
	 * 
	 * @return the character.
	 */
	private String getRandomVinCharacter() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		int randomInt = randomGenerator.nextInt(35);
		return characters.substring(randomInt, randomInt + 1);
	}

	/**
	 * Return a single randomly generated VIN character.
	 * 
	 * @return the character.
	 */
	private int getRandomVehicleType() {
		// insure that there is at least one
		return randomGenerator.nextInt(4);
	}

	private int getRandomOilChangeDistance() {
		// insure that there is at least one
		return randomGenerator.nextInt(3001);
	}

	private int getRandomOdometerReading() {
		// insure that there is at least one
		return randomGenerator.nextInt(150000);
	}
	
	private String getRandomComment() {
		String comment = "\"";
		comment += COMMENT.substring(0, randomGenerator.nextInt(2000));
		return comment + "\"";
	}

	/**
	 * Return a single randomly generated VIN character.
	 * 
	 * @return the character.
	 */
	private int getVariableNumberOfSamples() {
		// insure that there is at least one
		return randomGenerator.nextInt(maxSamples) + 1;
	}

	private int getModelYearEmissionIndex(int modelYear) {
		switch (modelYear) {
		case 1990:
			return 0;
		case 1991:
			return 1;
		case 1992:
			return 2;
		case 1993:
			return 3;
		case 1994:
			return 4;
		case 1995:
			return 5;
		case 1996:
			return 6;
		case 1997:
			return 7;
		case 1998:
			return 8;
		case 1999:
			return 9;
		case 2000:
			return 10;
		case 2001:
			return 11;
		case 2002:
			return 12;
		case 2003:
			return 13;
		case 2004:
			return 14;
		case 2005:
			return 15;
		case 2006:
			return 16;
		case 2007:
			return 17;
		case 2008:
			return 18;
		case 2009:
			return 19;
		case 2010:
			return 20;
		}
		throw new IllegalArgumentException("Invalid model year.");
	}

	private String getRandomTestDate() {
		String date = "" + modelYear + "-";
		date += randomGenerator.nextInt(13);
		date += "-";
		date += randomGenerator.nextInt(29);
		return date;
	}
}