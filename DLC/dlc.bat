REM Execute this batch file in the /DLC/bin directory

java -cp .;..\..\Utility\bin com.androtopia.GenerateCSVFiles
java -jar ./WriteXMLFiles.jar
cd ..\..\HDF5
python writeHDF5Files.py
cd ..\DLC\bin
java -jar ./GetFileSizes.jar
java -jar ./ReadXMLFiles.jar
cd ..\..\HDF5
python readHDF5Files.py
cd ..\DLC\bin
java -jar ./StatsToCSVFile.jar
