# writeHDF5File.py

import sys
import h5py
import Image
import numpy
import os
import time
import MySQLdb

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
        
if len(sys.argv) != 3:                                      # check number of command line args
    print "Argument error, need: <output directory> <image file name>. Quitting."
    raise SystemExit(1)
else:
    print "Output directory: " + sys.argv[1]                # display output directory name                             
    print "Image file: " + sys.argv[2]                      # display image file name                             
        
def processFile(file):
  BINARY_IMAGE_SIZE = 114173
  startTime = int(round(time.time() * 1000))
  linesIn = 0
  for line in  open(file):                                  # read the CSV file
    linesIn += 1
    fields = line.split(",")                                # tokenize the input
    if(fields[0] != '""' and linesIn != 1):
        f = h5py.File(sys.argv[1] + "\\" + fields[0] + ".hdf5", "w")   # open the output HDF5 File
        
        grp = f.create_group(fields[0])                     # create vehicle group (VIN id)
        grp['manufacturer'] = str.strip(fields[1], '"')     # populate the vehicle group ...
        grp['modelYear'] = int(fields[2])
        grp['vehicleType'] = fields[3]
        grp['oilChangeDistance'] = float(fields[4])
        grp['odometer'] = float(fields[5])
        grp['comments'] = str.strip(fields[6], '"')
        
        photoCopies = int(fields[13])
        for i in range(photoCopies):
            image = Image.open(sys.argv[2])                 # read the image file
            grp['photo' + str(i)] = image                            # add the image member to the group
            grp['photo' + str(i)].attrs['CLASS'] = numpy.string_("IMAGE")
            grp['photo' + str(i)].attrs['IMAGE_VERSION'] = numpy.string_("1.2")
            grp['photo' + str(i)].attrs['IMAGE_SUBCLASS'] = numpy.string_("IMAGE_TRUECOLOR")
            grp['photo' + str(i)].attrs["IMAGE_COLORMODEL"] = numpy.string_("RGB")
            grp['photo' + str(i)].attrs['INTERLACE_MODE'] = numpy.string_("INTERLACE_PIXEL")

        ds = f.create_dataset(fields[0] + "/emissions", (int(fields[12]),), dtype=('a10,f4,f4,f4,f4'))
        emissionPtr = 0

        writeTime = int(round(time.time() * 1000)) - startTime
        sql = "insert into stats (fileName, numberOfPhotos, emissionsSamples, binaryBytes, timeToCreateInMilliseconds) values (\"" + \
            fields[0] + ".hdf5\"," + str(photoCopies) + "," + fields[12] + "," + str((BINARY_IMAGE_SIZE * photoCopies)) + "," + \
            str(writeTime) + ")"
        cur = db.cursor()                                   # record statistics
        cur.execute(sql)
        cur.execute("commit")
        
    if(linesIn != 1):                                       # skip the header, write the emissions record ...
        ds[emissionPtr:emissionPtr+1] = [(fields[7], float(fields[8]), float(fields[9]), float(fields[10]), float(fields[11]))]
        emissionPtr += 1

userid = raw_input('Enter User: ')                          # get DB user and password information
password = raw_input('Enter Password: ')                    # and connect to the DB ...
db = MySQLdb.connect(host="localhost", user=userid, passwd=password, db="DLC")
print "Running ..."
                      
for file in DirectoryWalker(os.path.abspath('c:/tmp')):     # process all the input CSV files
    if(str.find(file, ".csv") != -1):
        processFile(file)
        
print "Done."