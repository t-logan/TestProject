REM Execute this batch file in the /DLC/run directory

cd ..\bin
java -cp .;..\..\Utility\bin com.androtopia.GenerateCSVFiles
cd ..\run
java -jar ./WriteXMLFiles.jar
cd ..\..\HDF5
python writeHDF5Files.py
cd ..\DLC\run
java -jar ./GetFileSizes.jar
java -jar ./ReadXMLFiles.jar
cd ..\..\HDF5
python readHDF5Files.py
cd ..\DLC\run
java -jar ./StatsToCSVFile.jar
