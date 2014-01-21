# writeHDF5File.py

import sys
import h5py
import Image
import numpy
import os

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
  linesIn = 0
  for line in  open(file):                                  # read the CSV file
    linesIn += 1
    fields = line.split(",")                                # tokenize the input
    if(fields[0] != '""' and linesIn != 1):
        f = h5py.File(sys.argv[1] + "\\" + fields[0] + ".hdf5", "w")   # open the output HDF5 File
        
        grp = f.create_group(fields[0])                     # create vehicle group (VIN id)
        grp['manufacturer'] = fields[1]                     # populate the vehicle group ...
        grp['modelYear'] = int(fields[2])
        grp['vehicleType'] = fields[3]
        grp['oilChangeDistance'] = float(fields[4])
        grp['odometer'] = float(fields[5])
        grp['comments'] = fields[6]
        
        image = Image.open(sys.argv[2])                     # read the image file
        grp['photo'] = image								# add the image member to the group
        grp['photo'].attrs['CLASS'] = numpy.string_("IMAGE")
        grp['photo'].attrs['IMAGE_VERSION'] = numpy.string_("1.2")
        grp['photo'].attrs['IMAGE_SUBCLASS'] = numpy.string_("IMAGE_TRUECOLOR")
        grp['photo'].attrs["IMAGE_COLORMODEL"] = numpy.string_("RGB")
        grp['photo'].attrs['INTERLACE_MODE'] = numpy.string_("INTERLACE_PIXEL")

        ds = f.create_dataset(fields[0] + "/emissions", (int(fields[12]),), dtype=('a10,f4,f4,f4,f4'))
        emissionPtr = 0
    if(linesIn != 1):                                       # skip the header, write the emissions record ...
        ds[emissionPtr:emissionPtr+1] = [(fields[7], float(fields[8]), float(fields[9]), float(fields[10]), float(fields[11]))]
        emissionPtr += 1
                      
for file in DirectoryWalker(os.path.abspath('c:/tmp')):     # process all the input CSV files
    if(str.find(file, ".csv") != -1):
        processFile(file)
        
print "Done."