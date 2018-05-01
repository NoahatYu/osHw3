import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.math.BigInteger;
import java.nio.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*********************************************************
 * Name of program: Fat32 Utility reader
 * Authors: Noah Potash and Shalom Azar
 * Description: Fat32 Utility reader
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
    private int fatTable;
    private int fatTableTwo;
    private String fat32img;
    private int BytesPerClus;
    private String volIDName;
    //private static DirectoryObj directoryObj;
    private MappedByteBuffer out;

    /**
     * Constructor
     */
    public fat32Reader(String fat32) throws IOException {
        RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32, "rw");
        //Mapping a file into memory
        out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);
        //TODO: Clean up constructor and make it into a method
        BPB_ResvdSecCnt = getBytesData(fat32, 14, 2);
        BPB_NumFATs = getBytesData(fat32, 16, 1);
        FATsz = getBytesData(fat32, 36, 4);
        BPB_RootEntCnt = getBytesData(fat32, 17, 2);
        BPB_BytsPerSec = getBytesData(fat32, 11, 2);
        BPB_SecPerClus = getBytesData(fat32, 13, 1);
	BytesPerClus = BPB_BytsPerSec * BPB_SecPerClus;
        RootClus = N = getBytesData(fat32, 44, 4);
        int BPB_TotSec32 = getBytesData(fat32, 32, 4);
        int DataSec = BPB_TotSec32 - (BPB_ResvdSecCnt + (BPB_NumFATs * FATsz) + RootDirSectors);
        int CountofClusters = (int) Math.floor(DataSec / BPB_SecPerClus);
        //the count of sectors occupied by the root directory.
        RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec - 1)) / BPB_BytsPerSec;
        int FATOffset = N * 4;
        FirstDataSector = BPB_ResvdSecCnt + (BPB_NumFATs * FATsz) + RootDirSectors;
        /* Given any valid data cluster number N, the sector number of the first sector of that cluster(again
           relative to sector 0 of the FAT volume) is computed as follows*/
        FirstSectorofCluster = ((N - 2) * BPB_SecPerClus) + FirstDataSector;
        rootDir = FirstSectorofCluster * BytesPerClus;

        /* ThisFATSecNum is the sector number of the FAT sector that contains the entry for
        cluster N in the first FAT. If you want the sector number in the second FAT, you add FATSz to
        ThisFATSecNum; for the third FAT, you add (2 * FATSz), and so on. */
        ThisFATSecNum = BPB_ResvdSecCnt + (FATOffset / BPB_BytsPerSec);
        ThisFATEntOffset = (FATOffset % BPB_BytsPerSec);
        fatTable = ThisFATSecNum * BytesPerClus;
        fatTableTwo = (getFATsz() + ThisFATSecNum) * BytesPerClus;
    }

    /**
     * Main Method
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /* Parse args and open our image file */
        //System.Text.Encoding.ASCII.GetString(buf);
        String fat32Img = args[0];
        fat32Reader f32Reader = new fat32Reader(fat32Img);
        f32Reader.setFat32img(fat32Img);

        /* Get root directory address */
        int rootDir = f32Reader.getRootDir();
        //Set current directory to root
        int currentDir = rootDir;
        //f32Reader.getBytesData(fat32Img,11,2);
        List<DirEntry> dirInfo;
        DirectoryObj directoryObj = new DirectoryObj(fat32Img, f32Reader, currentDir, f32Reader.N);
        f32Reader.getVolumeInfo(directoryObj.getdEntryLst());
        List<String> workingDirLst = new ArrayList<String>();
        String currentWorkingDirName = "";
        workingDirLst.add(currentWorkingDirName);
        int workingDir = 0;
        /* Main loop. */
        while (true) {
            System.out.print("/" + workingDirLst.get(workingDir) + "] ");
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
                    f32Reader.getDirInfoLst(fat32Img, directoryObj, f32Reader, currentDir);
                    if (cmdLineArgs.length > 1) {
                        f32Reader.doStat(cmdLineArgs[1], directoryObj);
                    } else {
                        System.out.println("Error: no file/directory was inputted");
                    }
                    break;
                case "size":
                    System.out.println("Going to size");
                    //run size helper method
                    if (cmdLineArgs.length > 1 && directoryObj.getDirEntryByName(cmdLineArgs[1]) != null) {
                        System.out.println("Size is " + directoryObj.getDirEntryByName(cmdLineArgs[1]).getFileSize());
                    } else {
                        System.out.println("Error: File/directory does not exist");
                    }
                    break;
                case "cd":
                    System.out.println("Going to cd");
                    currentWorkingDirName = cmdLineArgs[1].toUpperCase();
                    DirEntry dirEntry = directoryObj.getDirEntryByName(cmdLineArgs[1].toLowerCase());
                    if (dirEntry == null) {
                        System.out.println("Error: File/Directory does not exist");
                    } else if (dirEntry.getDirAttr() == 16) {
                        if (currentWorkingDirName.equals("..")) {
                            if(!currentWorkingDirName.equals("")){
                                workingDir = workingDir - 2;
                            }

                        } else if (currentWorkingDirName.equals(".")) {
                        } else {
                            boolean isInArr = f32Reader.strContainsArr(workingDirLst,currentWorkingDirName);
                            if(!isInArr) {
                                workingDirLst.add(currentWorkingDirName);
                            }
                        }
                        f32Reader.setN(dirEntry.getNextClusNum());
                        // If we cd .. back into root directory, we set n = 2, because the root directory is 2.
                        if (dirEntry.getDirName().equals("..") && f32Reader.getN() == 0) {
                            f32Reader.setN(2);
                            currentDir = rootDir;
                        } else {
                            currentDir = dirEntry.getLocation();
                        }
                        //update directory object
                        directoryObj = new DirectoryObj(fat32Img, f32Reader, currentDir, f32Reader.getN());
                        if(!currentWorkingDirName.equals(".")){
                            workingDir++;
                        }
                    } else {
                        System.out.println("Error: File is not a directory");
                    }
                    break;
                case "ls":
                    System.out.println("Going to ls");
                    //run ls helper methods
                    //Get current directory info
                    dirInfo = f32Reader.getDirInfoLst(fat32Img, directoryObj, f32Reader, currentDir);
                    //print all the short names of the current directory
                    List<String> sortedLS = f32Reader.sortLsInfo(dirInfo);
                    f32Reader.printLsInfo(sortedLS);
                    System.out.println();
                    break;
                case "read":
                    System.out.println("Going to read");
                    String file = cmdLineArgs[1];
                    dirEntry = directoryObj.getDirEntryByName(file.toLowerCase());
                    if (dirEntry == null) {
                        System.out.println("Error: File/Directory does not exist");
                    }
                    else if(cmdLineArgs.length < 4){
                        System.out.println("Error: Not enough arguements");
                    }
                    else {
                        int loc = dirEntry.getLocation();
                        String offset = cmdLineArgs[2];
                        int o = Integer.parseInt(offset);
                        String size = cmdLineArgs[3];
                        int s = Integer.parseInt(size);
                        List<Integer> clus = directoryObj.getClusters(fat32Img, f32Reader, dirEntry.getNextClusNum());
                        String read = directoryObj.getReadInfo(fat32Img, f32Reader, dirEntry.getNextClusNum(), o, s, dirEntry);
                        //String read = f32Reader.getBytesChar(fat32Img, loc + o, s);
                        System.out.println(read);
                        //run read helper method
                    }
                    break;
                case "volume":
                    System.out.println("Going to volume");
                    //run read helper method
                    //directoryObj = new DirectoryObj(fat32Img,f32Reader,currentDir);
                    f32Reader.getDirInfoLst(fat32Img, directoryObj, f32Reader, rootDir);
                    //f32Reader.volumeInfo(dirInfo);
                    System.out.println(f32Reader.getVolIDName().toUpperCase());
                    break;
                case "freelist":
                    System.out.println("Going to freelist");
                    List<Integer> FreeClus = f32Reader.getFreeList(fat32Img, directoryObj, f32Reader);
                    for(int i = 0; i < FreeClus.size() && i < 3; i++) {
                        System.out.println("Free Cluster#" + i + ": " + FreeClus.get(i));
                    }
                    System.out.println("Total number of Free Clusters: " + FreeClus.size());
                    break;
                case "delete":
                    System.out.println("Going to delete");
                    file = cmdLineArgs[1];
                    dirEntry = directoryObj.getDirEntryByName(file.toLowerCase());
                    int loc = dirEntry.getOffSetShortName();
                    int n = dirEntry.getNextClusNum();
                    List<Integer> clus = directoryObj.getClusters(fat32Img, f32Reader, n);
                    int fatTable = f32Reader.getFatTable();
                    int fatTableTwo = f32Reader.getFatTableTwo();
                    f32Reader.deleteInFat(f32Reader, fatTable, clus);
                    f32Reader.deleteInFat(f32Reader, fatTableTwo, clus);
                    f32Reader.out.put(loc, (byte) 0xE5);
                    int clusNum = dirEntry.getNextClusNum();
                    directoryObj = new DirectoryObj(fat32Img, f32Reader, currentDir, f32Reader.getN());
                    System.out.println("done");
                    break;
                case "newfile":
                    System.out.println("Going to newfile");
                    file = cmdLineArgs[1];
                    String s = cmdLineArgs[2];
                    int size = Integer.parseInt(s);
                    FreeClus = f32Reader.getFreeList(fat32Img, directoryObj, f32Reader);
                    f32Reader.writeToFat(f32Reader, f32Reader.getFatTable(), FreeClus,size);
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
     *
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
     *
     * @param fat32Img
     * @param offset
     * @param size
     * @throws IOException
     */
    public int getBytesData(String fat32Img, int offset, int size) throws IOException {

        //RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32Img, "rw");

        //Mapping a file into memory
        //MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);

        //reading from memory file in Java little endian
        double exp = Math.pow(256, size - 1);
        int eBit = 0;
        for (int i = offset + size - 1; i >= offset; i--) {
            //Covert number to unsigned if negative.
            int unsignedInt = out.get(i) & 0xFF;
            eBit += unsignedInt * exp;
            exp = exp / 256;
        }

        return eBit;
    }

    /**
     * Gets the bytes from the image and converts them to chars
     *
     * @param fat32Img string fat 32 image
     * @param offset   offset in image
     * @param size     number of bytes
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
     *
     * @param fat32
     * @param f32Reader
     * @param currentDir
     * @return
     * @throws IOException
     */
    public List<DirEntry> getDirInfoLst(String fat32, DirectoryObj dObj, fat32Reader f32Reader, int currentDir) throws IOException {
        List<DirEntry> dEntryList = dObj.getdEntryLst();
        return dEntryList;
    }

    /**
     * Print the ls info
     *
     * @param dEntryList
     */
    public void printLsInfo(List<String> dEntryList) {
        for (int i = 0; i < dEntryList.size(); i++) {
            System.out.print(dEntryList.get(i) + " ");
        }
    }

    /**
     * Print the ls info
     *
     * @param dEntryList
     */
    public List<String> sortLsInfo(List<DirEntry> dEntryList) {
        int dEntryLength = dEntryList.size();
        List<String> sorted = new ArrayList<String>();
        for (int i = 0; i < dEntryLength; i++) {
            DirEntry dE = dEntryList.get(i);
            char attrNameFirstChar = dE.getDirName().charAt(0);
            int attrNameFirstCharInt = (int) attrNameFirstChar;
            //make sure not to print the volume id or secret/hidden files
            if (dE.getDirAttr() != 8 && dE.getDirAttr() != 2 && attrNameFirstCharInt != 229) {
                sorted.add(dE.getDirName());
            }

        }
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Print the volume id
     *
     * @param dEntryList
     */
    public void getVolumeInfo(List<DirEntry> dEntryList) {
        int dEntryLength = dEntryList.size();
        boolean volID = false;
        for (int i = 0; i < dEntryLength; i++) {
            DirEntry dE = dEntryList.get(i);
            //make sure to print the volume id
            if (dE.getDirAttr() == 8) {
                volID = true;
                volIDName = dE.getDirName();
                break;
            }
        }
        if (!volID) {
            System.out.println("Error: Volume ID does not exist");
        }
    }

    /**
     * Prints the info for stat
     *
     * @param dirFile file input
     * @param dirObj  directory it us in
     */
    public void doStat(String dirFile, DirectoryObj dirObj) throws IOException {
        DirEntry dirEntry = dirObj.getDirEntryByName(dirFile.toLowerCase());
        //int x = getFileLocation(dirEntry);
        //getClusters(fat32img, dirEntry);
        //if dir file name not there don't print
        if (dirEntry != null) {
            System.out.println("Size is " + dirEntry.getFileSize());
            System.out.println("Attributes " + dirEntry.getDirAttrName());
            System.out.println("Next cluster number is " + dirEntry.getNextClusNumHex());
        } else {
            //print error message
            System.out.println("Error: file/directory does not exist");
        }
    }


    /**
     * Get root directory offset
     *
     * @return root dir offset
     */
    public int getRootDir() {
        return rootDir;
    }

    /**
     * Gets the current working directory
     *
     * @return current working directory
     */
    public int getCurrentDir() {
        return currentDir;
    }

    /**
     * Converts decimal number to hex number string
     *
     * @param num any number
     * @return String hex of num
     */
    public String hexer(int num) {
        return "0x" + Integer.toHexString(num);
    }

    /**
     * Set root directory
     * @param rootDir
     */
    public void setRootDir(int rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * Set current directory
     * @param currentDir
     */
    public void setCurrentDir(int currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * Gets the fat32img field
     * @return fat32img
     */
    public String getFat32img() {
        return fat32img;
    }

    /**
     * Sets the fat32img field
     * @param fat32img
     */
    public void setFat32img(String fat32img) {
        this.fat32img = fat32img;
    }

    /**
     * Gets BPB_ResvdSecCnt
     * @return BPB_ResvdSecCnt
     */
    public int getBPB_ResvdSecCnt() {
        return BPB_ResvdSecCnt;
    }

    /**
     * Gets BPB_NumFATs
     * @return BPB_NumFATs
     */
    public int getBPB_NumFATs() {
        return BPB_NumFATs;
    }

    /**
     * Gets FATsz
     * @return FATsz
     */
    public int getFATsz() {
        return FATsz;
    }

    /**
     * Gets BPB_RootEntCnt
     * @return BPB_RootEntCnt
     */
    public int getBPB_RootEntCnt() {
        return BPB_RootEntCnt;
    }

    /**
     * Gets BPB_BytsPerSec
     * @return BPB_BytsPerSec
     */
    public int getBPB_BytsPerSec() {
        return BPB_BytsPerSec;
    }

    /**
     * Gets BytesPerClus
     * @return BytesPerClus
     */
    public int getBytesPerClus() {
        return BytesPerClus;
    }

    /**
     * Gets RootDirSectors
     * @return RootDirSectors
     */
    public int getRootDirSectors() {
        return RootDirSectors;
    }

    /**
     * Gets FirstDataSector
     * @return FirstDataSector
     */
    public int getFirstDataSector() {
        return FirstDataSector;
    }

    /**
     * Gets BPB_SecPerClus
     * @return BPB_SecPerClus
     */
    public int getBPB_SecPerClus() {
        return BPB_SecPerClus;
    }

    /**
     * Gets BPB_RootClus
     * @return BPB_RootClus
     */
    public int getBPB_RootClus() {
        return BPB_RootClus;
    }

    /**
     * Gets N
     * @return N
     */
    public int getN() {
        return N;
    }

    /**
     * Sets N
     * @param n
     */
    public void setN(int n) {
        N = n;
    }

    /**
     * Gets RootClus
     * @return RootClus
     */
    public int getRootClus() {
        return RootClus;
    }

    /**
     * Sets rootClus
     * @param rootClus
     */
    public void setRootClus(int rootClus) {
        RootClus = rootClus;
    }

    /**
     * Gets ThisFATSecNum
     * @return ThisFATSecNum
     */
    public int getThisFATSecNum() {
        return ThisFATSecNum;
    }

    /**
     * Gets ThisFATEntOffset
     * @return ThisFATEntOffset
     */
    public int getThisFATEntOffset() {
        return ThisFATEntOffset;
    }

    /**
     * Gets FirstSectorofCluster
     * @return FirstSectorofCluster
     */
    public int getFirstSectorofCluster() {
        return FirstSectorofCluster;
    }

    /**
     * Gets fatTable
     * @return fatTable
     */
    public int getFatTable() {
        return fatTable;
    }

    /**
     * Gets fatTableTwo
     * @return fatTableTwo
     */
    public int getFatTableTwo() {
        return fatTableTwo;
    }

    /**
     * Checks if it already exists in the array
     * @param list
     * @param name
     * @return true if exist, if not it returns false
     */
    private boolean strContainsArr(List<String> list, String name) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(name)){
                return true;
            }

        }
        return false;
    }
/**
     * Gets the volumeID
     * @return volume id
     */
    public String getVolIDName() {
        return volIDName;
    }

    public List<Integer> getFreeList(String fat32img, DirectoryObj directoryObj, fat32Reader f32Reader) throws IOException {
        List<Integer> firstThreeFreeClusters = new ArrayList<Integer>();
        int fat_table = directoryObj.getFatTable(f32Reader);
        int x = fat_table;
        int index = 0;
        while(x < fatTableTwo){
            BigInteger val = BigInteger.valueOf(f32Reader.getBytesData(fat32img, x,4));
            if(val.equals(268435456L) || val.equals(4026531840L) || val.equals(BigInteger.ZERO)){
                firstThreeFreeClusters.add(index);
            }
            x += 4;
            index++;
        }
        return firstThreeFreeClusters;
    }

    /**
     * Deletes all fat entries of given file
     * @param f32
     * @param fatTable
     * @param Clus
     */
    public void deleteInFat(fat32Reader f32, int fatTable, List<Integer> Clus){
        int p = 0;
        for(int c : Clus){
            for(int i = 0; i < 4; i++) {
                p = fatTable + (c * 4) + i;
                f32.out.put(p, (byte) 0x00);
            }
        }
    }


    public void writeToFat(fat32Reader f32, int fatTable, List<Integer> Clus, int size){
        int bytes_per_clus = f32.getBytesPerClus();
        int total_clus = (int) Math.floor(size/bytes_per_clus) + 1;
        int current_clus = 0;
        int pos = 0;
        //int s = 0;
        //Integer eoc = 268435448;
        //String EOC = "0" + Integer.toHexString(eoc);
        //byte[] b = new BigInteger(EOC,16).toByteArray();
        byte[] b = new byte[4];
        b[0] = (byte) 0x0F;
        b[1] = (byte) 0xFF;
        b[2] = (byte) 0xFF;
        b[3] = (byte) 0xF8;
        int p = 16755;
        f32.out.put(p, (byte) 0x00);
        while(current_clus < total_clus){
            pos = fatTable + (Clus.get(current_clus) * 4);
            for(int i = 0; i < b.length; i++) {
                f32.out.put(pos + i, b[i]);
            }
            current_clus++;
        }
    }

    /*
    public void writeToDirectory(fat32Reader f32, int currentDir, String name, String ext){

    }
    */

    public void writeToFileLocation(fat32Reader f32, int fatTable, List<Integer> Clus){
    }

}
