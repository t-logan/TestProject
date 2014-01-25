select * from stats;

delete from stats;

select fileName, numberOfPhotos, emissionsSamples, sizeOnDiskInBytes, binaryBytes, timeToCreateInMilliseconds, timeToReadInMilliseconds from stats;

insert into stats (fileName, numberOfPhotos, emissionsSamples,
binaryBytes, timeToCreateInMilliseconds) values ("ABC.xml", 12, 50, 10000, 12367)

update stats set sizeOnDiskInBytes = 1476880 where fileName = "2FWOMHI3J1625DPOC.hdf5";

update stats set sizeOnDiskInBytes = 1476880 where fileName = "2FWOMHI3J1625DPOC.hdf5";
update stats set sizeOnDiskInBytes = 155603 where fileName = "2FWOMHI3J1625DPOC.xml";
update stats set sizeOnDiskInBytes = 114649 where fileName = "2FWOMHI3J1625DPOC.xmlc";
update stats set sizeOnDiskInBytes = 2941360 where fileName = "2G2WV7L0R31J15ASM.hdf5";
update stats set sizeOnDiskInBytes = 309250 where fileName = "2G2WV7L0R31J15ASM.xml";
update stats set sizeOnDiskInBytes = 228433 where fileName = "2G2WV7L0R31J15ASM.xmlc";
update stats set sizeOnDiskInBytes = 2941360 where fileName = "2MHR2QLVP6KC2GGUL.hdf5";
update stats set sizeOnDiskInBytes = 310142 where fileName = "2MHR2QLVP6KC2GGUL.xml";
update stats set sizeOnDiskInBytes = 228398 where fileName = "2MHR2QLVP6KC2GGUL.xmlc";
update stats set sizeOnDiskInBytes = 2941360 where fileName = "3VWYTL4CCY62N7C8R.hdf5";
update stats set sizeOnDiskInBytes = 310035 where fileName = "3VWYTL4CCY62N7C8R.xml";
update stats set sizeOnDiskInBytes = 228435 where fileName = "3VWYTL4CCY62N7C8R.xmlc";
update stats set sizeOnDiskInBytes = 2941360 where fileName = "4FN3PBBS0RQU2HZKA.hdf5";
update stats set sizeOnDiskInBytes = 308805 where fileName = "4FN3PBBS0RQU2HZKA.xml";
update stats set sizeOnDiskInBytes = 228307 where fileName = "4FN3PBBS0RQU2HZKA.xmlc";