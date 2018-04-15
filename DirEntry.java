package com.company;

public class DirEntry {

    private String dirName;
    private int hiClus;
    private int loClus;
    private String nextClusHex;
    private int dirAttr;
    private String dirAttrName;
    private int fileSize;

    public DirEntry(String dirName, int hiClus, int loClus,int dirAttr, String dirAttrName, int fileSize){
        this.dirName = dirName;
        this.hiClus = hiClus;
        this.loClus = loClus;
        this.dirAttr = dirAttr;
        this.fileSize = fileSize;
        this.dirAttrName = dirAttrName;
        nextClusHex = getNextClusNum(this.hiClus,this.loClus);

    }


    private String getNextClusNum(int hiClus,int loClus){
        String hiClusHex = Integer.toHexString(hiClus);
        String loClusHex = Integer.toHexString(loClus);
        String nextClusNumStr = "0x" + hiClusHex + loClusHex;
        //int nextClusL = (int)Long.parseLong(nextClusNumStr, 16);
        return nextClusNumStr;
    }


    public String getDirAttrName() {
        return dirAttrName;
    }

    public int getDirAttr() {
        return dirAttr;
    }

    public String getDirName() {
        return dirName;
    }

   public int getHiClus() {
        return hiClus;
   }
    public int getLoClus() {
        return loClus;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getNextClusHex() {
        return nextClusHex;
    }
}
