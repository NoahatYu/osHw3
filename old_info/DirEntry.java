public class DirEntry {

    private String dirName;
    private int hiClus;
    private int loClus;
    private String nextClusHex;
    private int dirAttr;
    private String dirAttrName;
    private int fileSize;

    /**
     * Constructor
     * @param dirName short filename
     * @param hiClus hi cluster number
     * @param loClus lo cluster number
     * @param dirAttr directory attribute as a number
     * @param dirAttrName directory attribute name
     * @param fileSize file size
     */
    public DirEntry(String dirName, int hiClus, int loClus,int dirAttr, String dirAttrName, int fileSize){
        this.dirName = dirName;
        this.hiClus = hiClus;
        this.loClus = loClus;
        this.dirAttr = dirAttr;
        this.fileSize = fileSize;
        this.dirAttrName = dirAttrName;
        nextClusHex = getNextClusNum(this.hiClus,this.loClus);

    }

    /**
     * Gets the next cluster number value.
     * @param hiClus hi cluster
     * @param loClus lo cluster
     * @return
     */
    private String getNextClusNum(int hiClus,int loClus){
        String hiClusHex = Integer.toHexString(hiClus);
        String loClusHex = Integer.toHexString(loClus);
        String nextClusNumStr = "0x" + hiClusHex + loClusHex;
        //int nextClusL = (int)Long.parseLong(nextClusNumStr, 16);
        return nextClusNumStr;
    }

    /**
     * Get directory name
     * @return
     */
    public String getDirAttrName() {
        return dirAttrName;
    }

    /**
     * Gets the file/directory attribute
     * @return directory attribute
     */
    public int getDirAttr() {
        return dirAttr;
    }

    /**
     * Gets the directory short name
     * @return directory name
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * Gets the hi word cluster value
     * @return hi word
     */
    public int getHiClus() {
        return hiClus;
    }
    /**
     * Gets the lo word cluster value
     * @return lo word
     */
    public int getLoClus() {
        return loClus;
    }

    /**
     * Gets the file size
     * @return file size
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * Gets the next cluster number in hex
     * @return next cluster number in hex
     */
    public String getNextClusHex() {
        return nextClusHex;
    }
}
