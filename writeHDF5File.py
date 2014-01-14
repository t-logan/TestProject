# writeHDF5File.py

import sys
import numpy as np
import h5py
import tables
import io
import Image
from array import array

linesIn = 0

if len(sys.argv) != 2:                                      # check number of command line args
    print "Please supply the output directory name. Quitting."
    raise SystemExit(1)
else:
    print "Output directory is " + sys.argv[1]              # display output file name                             

for line in  open(r'\tmp\seed.csv'):                        # read and dump the csv file
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
        
        byteCnt = 0
        bytes = bytearray()
        with open("DLCSchema.png", "rb") as f2:             # read the image / TODO: parameterize
            byte = f2.read(1)
            while byte != b"":
                bytes.extend(byte)                          # bytes contains the image data (TODO: inefficient, but works)
                byteCnt += 1
                byte = f2.read(1)    
        image = Image.open(io.BytesIO(bytes))               # convert byte array to an image
        grp['photo'] = image								# add the image member to the group

        ds = f.create_dataset(fields[0] + "/emissions", (100,), dtype=('a10,f4,f4,f4'))
        emissionPtr = 0
    if(linesIn != 1):                                       # skip the header, write the emissions record ...
        ds[emissionPtr:emissionPtr+1] = [(fields[7], float(fields[8]), float(fields[9]), float(fields[10]))]
        emissionPtr += 1
        
print "Done."
