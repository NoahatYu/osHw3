package com.company;

public class DirEntry {
    private String dirName;
    private int hiLoClus;
    private int dirAttr;

    public DirEntry(String dirName,int hiLoClus,int dirAttr){
        this.dirName = dirName;
        this.hiLoClus = hiLoClus;
        this.dirAttr = dirAttr;
    }


    public int getDirAttr() {
        return dirAttr;
    }

    public String getDirName() {
        return dirName;
    }

    public int getHiLoClus() {
        return hiLoClus;
    }
}
