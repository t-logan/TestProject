# writeHDF5File.py

import sys
import h5py
from PIL import Image
import numpy as np
import os
import time
import MySQLdb
import ConfigParser

class DirectoryWalker:
# a forward iterator that traverses a directory tree

  def __init__(self, directory):
    self.stack = [directory]
    self.files = []
    self.index = 0

  def __getitem__(self, index):
    while 1:
        try:
            file = self.files[self.index]
            self.index = self.index + 1
        except IndexError:
            # pop next directory from stack
            self.directory = self.stack.pop()
            self.files = os.listdir(self.directory)
            self.index = 0
        else:
            # got a filename
            fullname = os.path.join(self.directory, file)
            if os.path.isdir(fullname) and not os.path.islink(fullname):
                self.stack.append(fullname)
            return fullname
                
def processFile(file):
  global BINARY_IMAGE_SIZE, photoCopies, emissionsSamples, vin
  BINARY_IMAGE_SIZE = 114173
  photoCopies = 0
  emissionsSamples = 0
  vin = ""
  
  linesIn = 0
  for line in  open(file):                                  # read the CSV file
    linesIn += 1
    fields = line.split(",")                                # tokenize the input
    if(fields[0] != '""' and linesIn != 1):
        if(opaqueImage == "0"):
            f = h5py.File(targetDir + fields[0] + ".hdf5a", "w")   # open the output HDF5 File
        else:
            f = h5py.File(targetDir + fields[0] + ".hdf5b", "w")   # open the output HDF5 File
        
        grp = f.create_group(fields[0])                     # create vehicle group (VIN id)
        grp['manufacturer'] = str.strip(fields[1], '"')     # populate the vehicle group ...
        grp['modelYear'] = int(fields[2])
        grp['vehicleType'] = fields[3]
        grp['oilChangeDistance'] = float(fields[4])
        grp['odometer'] = float(fields[5])
        grp['comments'] = str.strip(fields[6], '"')
        
        photoCopies = int(fields[13])
        if(opaqueImage == "0"):
            # write as image
            for i in range(photoCopies):
                image = Image.open(imageFile)               # read the image file
                grp['photo' + str(i)] = image               # add the image member to the group
                grp['photo' + str(i)].attrs['CLASS'] = np.string_("IMAGE")
                grp['photo' + str(i)].attrs['IMAGE_VERSION'] = np.string_("1.2")
                grp['photo' + str(i)].attrs['IMAGE_SUBCLASS'] = np.string_("IMAGE_TRUECOLOR")
                grp['photo' + str(i)].attrs["IMAGE_COLORMODEL"] = np.string_("RGB")
                grp['photo' + str(i)].attrs['INTERLACE_MODE'] = np.string_("INTERLACE_PIXEL")
        else:
            # write as opaque binary data
            dtOpaque = np.dtype('V' + str(BINARY_IMAGE_SIZE))
            for i in range(photoCopies):
                with open(imageFile, "rb") as f2:
                    imageBytes = f2.read(BINARY_IMAGE_SIZE)
                    opd = f.create_dataset((fields[0] + '/photo' + str(i)), (BINARY_IMAGE_SIZE,), dtype=dtOpaque)
                    opd = imageBytes

        vin = fields[0]                                     # preallocate compound array
        emissionsSamples = fields[12]
        dt = ([("Date", np.dtype('a10')), ("Ex HC", np.dtype('f4')), ("Non-Ex HC", np.dtype('f4')), ("Ex CO", np.dtype('f4')), ("Ex NO2", np.dtype('f4'))])
        ds = f.create_dataset(fields[0] + "/emissions", (int(emissionsSamples),), dtype=dt)
        emissionPtr = 0
        
    if(linesIn != 1):                                       # skip the header, write the emissions record ...
        ds[emissionPtr:emissionPtr+1] = [(fields[7], float(fields[8]), float(fields[9]), float(fields[10]), float(fields[11]))]
        emissionPtr += 1

if __name__ == '__main__':    
    config = ConfigParser.ConfigParser()                    # get configuration
    config.read("hdf5.ini")

    targetDir = config.get("misc", "targetdir")
    imageFile = config.get("misc", "image")
    opaqueImage = config.get("misc", "opaque")
    
    host = config.get("db", "host")                         # get DB config information
    userid = config.get("db", "userid")                      
    password = config.get("db", "password")        
    database = config.get("db", "database")        
    db = MySQLdb.connect(host=host, user=userid, passwd=password, db=database)
    print "Running ..."
                      
    for file in DirectoryWalker(os.path.abspath(targetDir)):     # process all the input CSV files
        if(str.find(file, ".csv") != -1):
            print file
            startTime = int(round(time.time() * 1000))
            processFile(file)
            if(opaqueImage == "0"):
                fileExt = ".hdf5a"
            else:
                fileExt = ".hdf5b"
            writeTime = int(round(time.time() * 1000)) - startTime
            sql = "insert into Stats (fileName, numberOfPhotos, emissionsSamples, binaryBytes, timeToCreateInMilliseconds) values (\"" + \
                vin + fileExt +"\"," + str(photoCopies) + "," + emissionsSamples + "," + str((BINARY_IMAGE_SIZE * photoCopies)) + "," + \
                str(writeTime) + ")"
            cur = db.cursor()                                   # record statistics
            cur.execute(sql)
            cur.execute("commit")
        
    print "Done."