# writeHDF5File.py

import sys
import h5py
import Image

linesIn = 0

if len(sys.argv) != 4:                                      # check number of command line args
    print "Argument error, need: <output directory> <image file name> <CSV file name>. Quitting."
    raise SystemExit(1)
else:
    print "Output directory: " + sys.argv[1]                # display output directory name                             
    print "Image file: " + sys.argv[2]                      # display image file name                             
    print "Input CSV file: " + sys.argv[3]                  # display CSV file name                             

for line in  open(sys.argv[3]):                             # read the CSV file
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
        print image.size, image.format, image.mode
        print grp['photo']
        grp['photo'].attrs['CLASS'] = "IMAGE"
        grp['photo'].attrs['IMAGE_VERSION'] = "1.2"
        grp['photo'].attrs['IMAGE_SUBCLASS'] = "IMAGE_TRUECOLOR"
        grp['photo'].attrs['INTERLACE_MODE'] = "INTERLACE_PIXEL"

        ds = f.create_dataset(fields[0] + "/emissions", (100,), dtype=('a10,f4,f4,f4,f4'))
        emissionPtr = 0
    if(linesIn != 1):                                       # skip the header, write the emissions record ...
        ds[emissionPtr:emissionPtr+1] = [(fields[7], float(fields[8]), float(fields[9]), float(fields[10]), float(fields[11]))]
        emissionPtr += 1
        
print "Done."