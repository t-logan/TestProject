# readHDF5File.py

import sys
import h5py
from PIL import Image
import numpy
import os
import time
import MySQLdb
import string
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
    f = h5py.File(file, "r")                                # open the input HDF5 File
    
    # OPEN DATASETS ...
    grpName = file.lstrip(targetDir)
    grpName = grpName.rstrip('.hdf5')
    if(len(grpName) == 16):                                 # see if a trailing 5 was stripped from the VIN number (corner case) ...
        grpName = grpName + "5"
    if(len(grpName) == 15):                                
        grpName = grpName + "55"
    if(len(grpName) == 14):
        grpName = grpName + "555"
    grp = f[grpName]                                        # group name is the VIN number
    photoCount = len(grp) - 7                               # there are seven non-photo datasets in the VIN # group
    
    comments = grp['comments']                              # get the 7 non-photo datasets
    emissions = grp['emissions']
    manufacturer = grp['manufacturer']
    modelYear = grp['modelYear']
    odometer = grp['odometer']
    oilChangeDistance = grp['oilChangeDistance']
    vehicleType = grp['vehicleType']
    for i in range(photoCount):                             # read every photo
        photo = grp['photo' + str(i)]
    f.close()

if __name__ == '__main__':  
    config = ConfigParser.ConfigParser()                    # get configuration
    config.read("hdf5.ini")

    targetDir = config.get("misc", "targetdir")
    
    host = config.get("db", "host")                         # get DB config information
    userid = config.get("db", "userid")                      
    password = config.get("db", "password")        
    database = config.get("db", "database")        
    db = MySQLdb.connect(host=host, user=userid, passwd=password, db=database)
    print "Running ..."
                      
    for file in DirectoryWalker(os.path.abspath(targetDir)):     # read all the HDF5 files
        if(str.find(file, ".hdf5") != -1):
            startTime = int(round(time.time() * 1000))
            processFile(file)
            readTime = int(round(time.time() * 1000)) - startTime
            sql = "update Stats set timeToReadInMilliseconds = " + str(readTime) + " where fileName = \"" + string.lstrip(file,targetDir) + "\""
            cur = db.cursor()                                   # record statistics
            cur.execute(sql)
            cur.execute("commit")
        
    print "Done."