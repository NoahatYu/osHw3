package com.company;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;

/*********************************************************
 * TODO: Fill in this area and delete this line
 * Name of program:
 * Authors:
 * Description:
 **********************************************************/
public class fat32Reader {
    private int BPB_ResvdSecCnt;
    private int BPB_NumFATs;
    private int FATsz;
    private int BPB_RootEntCnt;
    private int BPB_BytsPerSec;
    private int RootDirSectors;
    private int FirstDataSector;
    private int BPB_SecPerClus;
    private int BPB_RootClus;
    private int N;
    private int RootClus;
    private int ThisFATSecNum;
    private int ThisFATEntOffset;
    private int FirstSectorofCluster;
    private int rootDir;
    private int currentDir;
    private static DirectoryObj directoryObj;
    private MappedByteBuffer out;
    //TODO: Clean up constructor and make it into a method
    /**
     * Constructor
     */
    public fat32Reader(String fat32) throws IOException {
        RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32, "rw");
        //Mapping a file into memory
        out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);
        BPB_ResvdSecCnt = getBytesData(fat32,14,2);
        BPB_NumFATs = getBytesData(fat32,16,1);
        FATsz = getBytesData(fat32,36,4);
        BPB_RootEntCnt = getBytesData(fat32,17,2);
        BPB_BytsPerSec = getBytesData(fat32,11,2);
        BPB_SecPerClus = getBytesData(fat32,13,1);
        RootClus = N = getBytesData(fat32,44,4);
        int BPB_TotSec32 = getBytesData(fat32,32,4);
        int DataSec = BPB_TotSec32 - (BPB_ResvdSecCnt + (BPB_NumFATs * FATsz) + RootDirSectors);
        int CountofClusters = (int)Math.floor(DataSec / BPB_SecPerClus);
        //the count of sectors occupied by the root directory.
        RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec -1)) / BPB_BytsPerSec;
        int FATOffset = N * 4;
        FirstDataSector = BPB_ResvdSecCnt + (BPB_NumFATs * FATsz) + RootDirSectors;
        /* ThisFATSecNum is the sector number of the FAT sector that contains the entry for
        cluster N in the first FAT. If you want the sector number in the second FAT, you add FATSz to
        ThisFATSecNum; for the third FAT, you add (2 * FATSz), and so on. */
        ThisFATSecNum = BPB_ResvdSecCnt + (FATOffset / BPB_BytsPerSec);
        ThisFATEntOffset = (FATOffset % BPB_BytsPerSec);
        /* Given any valid data cluster number N, the sector number of the first sector of that cluster(again
           relative to sector 0 of the FAT volume) is computed as follows*/
        FirstSectorofCluster = ((N - 2) * BPB_SecPerClus) + FirstDataSector;
        rootDir = FirstSectorofCluster * BPB_BytsPerSec;
    }


    /*public int getBPBInfo() {
        return ;
    }*/

    /**
     * Main Method
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /* Parse args and open our image file */
        //System.Text.Encoding.ASCII.GetString(buf);
        String fat32Img = args[0];
        fat32Reader f32Reader = new fat32Reader(fat32Img);

        /* Get root directory address */
        int rootDirM = f32Reader.getRootDir();
        //Set current directory to root
        int currentDir = rootDirM;
        //f32Reader.getBytesData(fat32Img,11,2);
        List<DirEntry> dirInfo;



        /* Main loop. */
        while(true) {
            System.out.print("/] ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String cmdLine = null;
            String[] cmdLineArgs = null;
            try {
                cmdLine = br.readLine();
                cmdLineArgs = cmdLine.split(" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* Start comparing input */
            switch (cmdLineArgs[0].toLowerCase()) {
                case "info":
                    //run info helper method
                    System.out.println("Go to display info");
                    f32Reader.printInfo(fat32Img);
                    break;
                case "stat":
                    System.out.println("Going to stat");
                    //run stat helper method
                    directoryObj = new DirectoryObj(fat32Img,f32Reader,currentDir);
                    dirInfo = f32Reader.getDirInfoLst(fat32Img,directoryObj,f32Reader,currentDir);
                    if(cmdLineArgs.length > 1) {
                        f32Reader.doStat(cmdLineArgs[1], directoryObj);
                    }else {
                        System.out.println("Error: no file/directory was inputted");
                    }
                    break;
                case "size":
                    System.out.println("Going to size");
                    //run size helper method

                    break;
                case "cd":
                    System.out.println("Going to cd");
                    //run cd helper method
                    break;
                case "ls":
                    System.out.println("Going to ls");
                    //run ls helper methods
                    //Get current directory info
                    directoryObj = new DirectoryObj(fat32Img,f32Reader,currentDir);
                    dirInfo = f32Reader.getDirInfoLst(fat32Img,directoryObj,f32Reader,currentDir);
                    //print all the short names of the current directory
                    f32Reader.printLsInfo(dirInfo);
                    System.out.println();
                    break;
                case "read":
                    System.out.println("Going to read");
                    //run read helper method
                    break;
                case "volume":
                    System.out.println("Going to volume");
                    //run read helper method
                    break;
                case "quit":
                    System.out.println("Quitting");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unrecognized command");
            }

            /* Close the file */

            //return 0; /* Success */
        }
    }

    /**
     * Prints info when info command is called.
     * @param fat32
     * @throws IOException
     */
    public void printInfo(String fat32) throws IOException {
        System.out.println("BPB_BytesPerSec: " + BPB_BytsPerSec + ", " + hexer(BPB_BytsPerSec));
        System.out.println("BPB_SecPerClus: " + BPB_SecPerClus + ", " + hexer(BPB_SecPerClus));
        System.out.println("BPB_RsvdSecCnt: " + BPB_ResvdSecCnt + ", " + hexer(BPB_ResvdSecCnt));
        System.out.println("BPB_NumFATS: " + BPB_NumFATs + ", " + hexer(BPB_NumFATs));
        System.out.println("BPB_FATSz32: " + FATsz + ", " + hexer(FATsz));

    }


    /**
     * Gets byte data from fat32 Image
     * @param fat32Img
     * @param offset
     * @param size
     * @throws IOException
     */
    public int getBytesData(String fat32Img,int offset, int size) throws IOException {

        //RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32Img, "rw");

        //Mapping a file into memory
        //MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);

        //reading from memory file in Java little endian
        double exp = Math.pow(256,size - 1);
        int eBit = 0;
        for (int i = offset + size - 1; i >= offset; i--){
            //Covert number to unsigned if negative.
            int unsignedInt = out.get(i) & 0xFF;
            eBit += unsignedInt * exp;
            exp = exp/256;
        }

        return eBit;
    }

    /**
     * Gets the bytes from the image and converts them to chars
     * @param fat32Img string fat 32 image
     * @param offset offset in image
     * @param size number of bytes
     * @return String that was coverts from bytes
     * @throws IOException
     */
    public String getBytesChar(String fat32Img, int offset, int size) throws IOException {

        //RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32Img, "rw");

        //Mapping a file into memory
        //MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);

        //reading from memory file in Java little endian
        String name = "";
        for (int i = offset; i < offset + size; i++) {
            //Covert number to unsigned if negative.
            int x = out.get(i);
            if (x == 0) {
                break;
            }
            char c = (char) (x & 0xFF);
            name += c;
        }
        return name;
    }


    /**
     * Get current directory info
     * @param fat32
     * @param f32Reader
     * @param currentDir
     * @return
     * @throws IOException
     */
    public List<DirEntry> getDirInfoLst(String fat32,DirectoryObj dObj,fat32Reader f32Reader, int currentDir) throws IOException {
        List<DirEntry> dEntryList = dObj.getdEntryLst();
        return dEntryList;
    }

    /**
     * Print the ls info
     * @param dEntryList
     */
    public void printLsInfo(List<DirEntry> dEntryList){
        int dEntryLength = dEntryList.size();
        for(int i =0;i < dEntryLength;i++){
            DirEntry dE = dEntryList.get(i);
            char attrNameFirstChar = dE.getDirName().charAt(0);
            int attrNameFirstCharInt = (int)attrNameFirstChar;
            //make sure not to print the volume id or secret/hidden files
            if(dE.getDirAttr() != 8 && dE.getDirAttr() != 2 && attrNameFirstCharInt != 229){
                System.out.print(dE.getDirName() + " ");
            }

        }
    }


    public void doStat(String dirFile,DirectoryObj dirObj){
        DirEntry dirEntry = dirObj.getDirEntryByName(dirFile.toLowerCase());
        //if dir file name not there don't print
        if(dirEntry != null) {
            System.out.println("Size is " + dirEntry.getFileSize());
            System.out.println("Attributes " + dirEntry.getDirAttrName());
            System.out.println("Next cluster number is " + dirEntry.getNextClusHex());
        }else{
            //print error message
            System.out.println("Error: file/directory does not exist");
        }
    }

    /**
     * Converts decimal number to hex number string
     * @param num any number
     * @return String hex of num
     */
    public String hexer(int num){
        return "0x" + Integer.toHexString(num);
    }

    /**
     * Get root directory offset
     * @return root dir offset
     */
    public int getRootDir() {
        return rootDir;
    }


    public int getCurrentDir(){
        return currentDir;
    }

    public void getLS(String fat32) throws IOException {
        //TODO: Make these fields to reference later
        int BPB_RootClus = N;
        /* Given any valid data cluster number N, the sector number of the first sector of that cluster (again
        relative to sector 0 of the FAT volume) is computed as follows:*/
        int FirstSectorofCluster = ((N - 2) * BPB_SecPerClus) + FirstDataSector;
        int rootDir = FirstSectorofCluster * BPB_BytsPerSec;
        int x = 0;
        String s = "";
        ArrayList<String> b = new ArrayList<String>();
        while(!getBytesChar(fat32, rootDir + x, 32).equals("")){
            b.add(getBytesChar(fat32, rootDir + x, 11));
            x += 64;
        }//12884901882
        //ArrayList<Integer> ba = getBytesArray(fat32, rootDir + x);
        for(int i = 1; i < b.size(); i++){
            System.out.println(b.get(i));
        }
        System.out.println(s);
    }
}
