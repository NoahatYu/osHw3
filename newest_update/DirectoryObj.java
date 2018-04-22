import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectoryObj {
    private List<DirEntry> dEntryLst;
    private HashMap<Integer,String> dirAttrMap;
    private int theN;

    /**
     * Constructor to create Directory Object for non-root
     * @param fat32
     * @param f32
     * @param dir
     */
    public DirectoryObj(String fat32,fat32Reader f32,int dir,int n) {
        dEntryLst = new ArrayList<DirEntry>();
        //map attributes number values to their string names
        dirAttrMap = new HashMap<Integer, String>();
        dirAttrMap.put(1,"ATTR_READ_ONLY");
        dirAttrMap.put(2,"ATTR_HIDDEN");
        dirAttrMap.put(4,"ATTR_SYSTEM");
        dirAttrMap.put(8,"ATTR_VOLUME_ID");
        dirAttrMap.put(16,"ATTR_DIRECTORY");
        dirAttrMap.put(32,"ATTR_ARCHIVE");
        try {
            theN = n;
            getDirInfo(fat32,f32,dir,n);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Loop through fat32 image of the root directory and create DirEntry objects and add them to a list
     * @param fat32
     * @param f32
     * @param currentDir
     * @throws IOException
     */
    public void getClusInfo(String fat32,fat32Reader f32,int currentDir)throws IOException {
        //FIXME: Check if spans multiple clusters and collect all that info into one directory object
        int offNum = 0;
        int varNum = 0;
        boolean done = false;
        int getDot = f32.getBytesData(fat32,currentDir + varNum + offNum,1);
        if(theN != 2 && getDot != 46){
            varNum += 32;
        }
        while(!done) {
            int dirNameNumStart = f32.getBytesData(fat32,currentDir + varNum + offNum,1);
            getDot = f32.getBytesData(fat32,currentDir + varNum + offNum,1);
            int getDotDot = f32.getBytesData(fat32,currentDir + varNum + offNum,2);
            String DIR_Name = f32.getBytesChar(fat32, currentDir + varNum + offNum, 8);
            offNum += 8;
            String DIR_Name_ext = f32.getBytesChar(fat32, currentDir + varNum + offNum, 3);
            offNum += 3;
            int DIR_Attr = f32.getBytesData(fat32, currentDir + varNum + offNum, 1);
            offNum += 9;
            int DIR_FstClusHI = f32.getBytesData(fat32, currentDir + varNum + offNum, 2);
            offNum += 6;
            int DIR_FstClusLO = f32.getBytesData(fat32, currentDir + varNum + offNum, 2);
            offNum += 2;
            int DIR_fileSize = f32.getBytesData(fat32,currentDir + varNum + offNum,4);

            //If there are 32 bytes of 0s then that is the end of the directory entries
            if (DIR_Name.equals("") || varNum >= 512) {
                int endOfNames = f32.getBytesData(fat32, currentDir + varNum + offNum, 32);
                if (endOfNames == 0 || varNum >= 512 ) {
                    done = true;
                }
            } else {
                String DirNameFull = "";
                //If it is 8 then it is a volume ID, so don't add it to lists
                if (DIR_Attr != 0) {
                    //if it is a directory then it has no extension
                    if (DIR_Attr == 16 || DIR_Attr == 8) {
                        DirNameFull = DIR_Name;
                    } else {
                        DirNameFull = DIR_Name + "." + DIR_Name_ext;
                    }
                    //add info to lists
                    //parse the short name directory before it is added to the list.
                    DirNameFull = DirNameFull.toLowerCase().replaceAll(" ", "");
                    String dirEntryStr = dirAttrMap.get(DIR_Attr);
                    int nextClusNum = getNextClusNum(DIR_FstClusHI,DIR_FstClusLO);
                    //N = nextClusNum;
                    int fileLoc = getFileLocation(f32,nextClusNum);
                    DirEntry dEntry = new DirEntry(DirNameFull, DIR_FstClusHI, DIR_FstClusLO, DIR_Attr, dirEntryStr, DIR_fileSize, nextClusNum, fileLoc);
                    dEntryLst.add(dEntry);

                }
                offNum = 0;//reset offset number
                if(getDot == 46 && getDotDot != 11822){
                    varNum += 32;
                }else {
                    varNum += 64;//update varNum to move onto the next short name dir
                }
            }
        }
    }

    /**
     * Gets all directory info across clusters
     * @param fat32
     * @param f32
     * @param currentDir
     * @param n
     * @throws IOException
     */
    public void getDirInfo(String fat32,fat32Reader f32,int currentDir,int n) throws IOException {
        //int locationLocation = getFileLocation(f32,N);
        List<Integer> clusters = getClusters(fat32,f32,n);
        int numOfCluses = clusters.size();
        int currentClus = 0;
        for(int i = 0; i < numOfCluses;i++){
            //update currentClus
            currentClus = getFileLocation(f32, clusters.get(i));
            getClusInfo(fat32,f32,currentClus);
        }
    }

    /**
     * Gets the string from the start byte to the end byte
     * @param fat32
     * @param f32
     * @param n
     * @param start
     * @param end
     * @return read
     * @throws IOException
     */
    public String getReadInfo(String fat32,fat32Reader f32,int n, int start, int end) throws IOException {
        List<Integer> clusters = getClusters(fat32,f32,n);
        int numOfCluses = clusters.size();
        int x = f32.getBPB_BytsPerSec();
        int loc = 0;
        int o = start % x;
        int z = end % x;
        int s = (int) Math.floor(start/x);
        int e = (int) Math.floor(end /x) + 1;
        String read = "";
        if(e > numOfCluses){
            System.out.println("Error: attempt to read beyond end of file");
        }
        else {
            for (int i = s; i < e; i++) {
                //update currentClus
                if(i != e - 1) {
                    loc = getFileLocation(f32, clusters.get(i));
                    read += f32.getBytesChar(fat32, loc + o, x);
                    o = 0;
                }
                else {
                    loc = getFileLocation(f32, clusters.get(i));
                    read += f32.getBytesChar(fat32, loc + o, z);
                }
            }
        }
        return read;
    }

    /**
     * Gets the list of clusters
     * @param fat32img
     * @param f32
     * @param N
     * @return clusterSpan
     * @throws IOException
     */
    public List<Integer> getClusters(String fat32img,fat32Reader f32,int N) throws IOException {
        List<Integer> clustersSpan = new ArrayList<Integer>();
        int BPB_NumFATs = f32.getBPB_NumFATs();
        int BPB_ResvdSecCnt = f32.getBPB_ResvdSecCnt();
        int RootDirSectors = f32.getRootDirSectors();
        int Fatsz = f32.getFATsz();
        int BPB_BytsPerSec = f32.getBPB_BytsPerSec();
        int eoc = 268435448;
        while(N < eoc) {
            //d.addToClusterList(n);
            //add to clusters list
            clustersSpan.add(N);
            //System.out.println("location value: " + getFileLocation(d, n));
            int FATOffset = N * 4;
            int FirstDataSector =  BPB_ResvdSecCnt + (BPB_NumFATs * Fatsz) + RootDirSectors;
            int ThisFATSecNum = BPB_ResvdSecCnt + (FATOffset / BPB_BytsPerSec);
            int thisFATEntOffset = (FATOffset % BPB_BytsPerSec);
            int fatTable = ThisFATSecNum * BPB_BytsPerSec;
            //update n
            N = f32.getBytesData(fat32img, fatTable + thisFATEntOffset, 4);

        }
        return clustersSpan;

    }
    
    /**
     * Get entry obj by name of directory entry
     * @param dirName
     * @return
     */
    public DirEntry getDirEntryByName(String dirName){
        for(DirEntry dE : dEntryLst){
            if (dE.getDirName().equalsIgnoreCase(dirName)){
                return dE;
            }
        }
        return null;
    }

    /**
     * Gets the dEntry list
     * @return
     */
    public List<DirEntry> getdEntryLst() {
        return dEntryLst;
    }

    /**
     * Get directory entry by number in the list
     * @param dirNum
     * @return
     */
    public DirEntry getDirEntry(int dirNum){
        return dEntryLst.get(dirNum);
    }
    
    /**
     * Gets the next cluster number value.
     * @param hiClus hi cluster
     * @param loClus lo cluster
     * @return
     */
    private int getNextClusNum(int hiClus,int loClus){
        String hiClusHex = Integer.toHexString(hiClus);
        String loClusHex = Integer.toHexString(loClus);
        String nextClusNumStr = "0x" + hiClusHex + loClusHex;
        int nextClusNum = Integer.parseInt(nextClusNumStr.split("0x")[1],16);
        //int nextClusL = (int)Long.parseLong(nextClusNumStr, 16);
        return nextClusNum;
    }

    /**
     * Gets the location in bytes of the beginning of a cluster
     * @param f32
     * @param n
     * @return cluster location types bytes per second
     */
    public int getFileLocation(fat32Reader f32, int n){
        int BPB_BytsPerSec = f32.getBPB_BytsPerSec();
        int BPB_SecPerClus = f32.getBPB_SecPerClus();
        int FirstDataSector = f32.getFirstDataSector();
        int FATOffset = n * 4;
        int FirstSectorofCluster = ((n - 2) * BPB_SecPerClus) + FirstDataSector;
        return FirstSectorofCluster * BPB_BytsPerSec;
    }

}
