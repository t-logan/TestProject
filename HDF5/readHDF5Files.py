# readHDF5File.py

import sys
import h5py
import Image
import numpy
import os
import time
import MySQLdb
import string

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
    f = h5py.File(file, "r")                                # open the input HDF5 File
    
    # PROCESS IMAGES AND EMISSIONS DATA ...
    
    f.close()

if __name__ == '__main__':  
    userid = raw_input('Enter User: ')                          # get DB user and password information
    password = raw_input('Enter Password: ')                    # and connect to the DB ...
    db = MySQLdb.connect(host="localhost", user=userid, passwd=password, db="DLC")
    print "Running ..."
                      
    for file in DirectoryWalker(os.path.abspath('c:/tmp')):     # read all the HDF5 files
        if(str.find(file, ".hdf5") != -1):
            startTime = int(round(time.time() * 1000))
            processFile(file)
            readTime = int(round(time.time() * 1000)) - startTime
            sql = "update stats set timeToReadInMilliseconds = " + str(readTime) + " where fileName = \"" + string.lstrip(file,"c:\\tmp\\") + "\""
            cur = db.cursor()                                   # record statistics
            cur.execute(sql)
            cur.execute("commit")
        
    print "Done."