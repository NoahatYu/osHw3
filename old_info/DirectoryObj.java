package com.company;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectoryObj {
    /*private List<String> dirNames;
    private List<Integer> hiLoClus;
    private List<Integer> dirAttr;*/
    private List<DirEntry> dEntryLst;
    private HashMap<Integer,String> dirAttrMap;


    /**
     * Constructor to create Directory Object
     * @param fat32
     * @param f32
     * @param dir
     */
    public DirectoryObj(String fat32,fat32Reader f32,int dir) {
        /*dirNames = new ArrayList<String>();
        hiLoClus = new ArrayList<Integer>();
        dirAttr = new ArrayList<Integer>();
        */
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
            getDirInfo(fat32,f32,dir);
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
    public void getDirInfo(String fat32,fat32Reader f32,int currentDir)throws IOException {

        int offNum = 0;
        int varNum = 0;
        boolean done = false;
        while(!done) {
            int dirNameNumStart = f32.getBytesData(fat32,currentDir + varNum + offNum,1);
            int getDot = f32.getBytesData(fat32,currentDir + varNum + offNum,1);
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
                    DirEntry dEntry = new DirEntry(DirNameFull,DIR_FstClusHI, DIR_FstClusLO,DIR_Attr,dirEntryStr,DIR_fileSize);
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

}
