# osHw3 Fat32 Utility Reader
Operating Systems Homework3 first submission
by Noah Potash and Shalom Azar

 
Files/directories:
	• fat32Reader.java - Main class that reads the fat32 image. The fat32 utility reader.
	• DirectoryObj.java - Class Object of the entire current directory. A list of all the directory entries/files.
	• DirEntry.java - Class Object that contains all the info for each individual directory entry.
 
Instructions for compiling program:
	• Step1: Once inside of the directory that contains the files of the project.
	type ```javac *.java``` to compile all the files.
	• Step2: To run type ```java -cp . fat32Reader <absolute_path of fat32 Image>```.

Challenges encountered along the way:
	• Not understanding the fat32 spec without reading it over several time.
	• Figuring out where the root directory and the FAT was.
	• Dealing with signed and unsigned ints and converting to little endian in java.
Sources used:
	• https://www.pjrc.com/tech/8051/ide/fat32.html
	• Fat32 spec pdf
	• Used a hexeditor to look at the fat32 image

