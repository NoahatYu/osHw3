package com.company;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
/*********************************************************
 * TODO: Fill in this area and delete this line
 * Name of program:
 * Authors:
 * Description:
 **********************************************************/
public class fat32_reader {
    private int byteCount = 10485760;//10MB

    /**
     * Constructor
     */
    public fat32Reader(){

    }

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        /* Parse args and open our image file */
        //System.Text.Encoding.ASCII.GetString(buf);
        String fat32Img = args[0];
        fat32Reader f32Reader = new fat32Reader();
        //f32Reader.getBytesData(fat32Img,11,2);

        //TODO: ADD helper methods!

        /* Parse boot sector and get information */

        /* Get root directory address */
        //printf("Root addr is 0x%x\n", root_addr);


        /* Main loop. */
        while(true) {
            System.out.print("/] ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String cmdLine = null;
            try {
                cmdLine = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* Start comparing input */
            switch (cmdLine.toLowerCase()) {
                case "info":
                    System.out.println("Go to display info");
                    f32Reader.printInfo(fat32Img);
                    f32Reader.getRootDirectory(fat32Img);
                    break;
                case "stat":
                    System.out.println("Going to stat");
                    //run open helper method
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
                    //run ls helper method
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
        System.out.print("BPB_BytesPerSec: ");
        System.out.println(getBytesData(fat32,11,2));
        System.out.print("BPB_SecPerClus: ");
        System.out.println(getBytesData(fat32,13,1));
        System.out.print("BPB_RsvdSecCnt: ");
        System.out.println(getBytesData(fat32,14,2));
        System.out.print("BPB_NumFATS: ");
        System.out.println(getBytesData(fat32,16,1));
        System.out.print("BPB_FATSz32: ");
        System.out.println(getBytesData(fat32,36,4));

    }

    /**
     * Gets byte data from fat32 Image
     * @param fat32Img
     * @param offset
     * @param size
     * @throws IOException
     */
    public int getBytesData(String fat32Img,int offset, int size) throws IOException {

        RandomAccessFile memoryMappedFile = new RandomAccessFile(fat32Img, "rw");

        //Mapping a file into memory
        MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, byteCount);

        //reading from memory file in Java little endian
        double exp = Math.pow(256,size - 1);
        int eBit = 0;
        for (int i = offset + size - 1; i >= offset; i--){
            //Covert number to unsigned if negative.
            int unsignedInt = out.get(i) & 0xFF;
            eBit += unsignedInt * exp;
            exp = exp/256;
        }
        //System.out.print(eBit + ", ");
        //little endian hex
        System.out.print("0x" + Integer.toHexString(eBit) + ", ");
        return eBit;
    }



    public void getRootDirectory(String fat32) throws IOException {
    //TODO: Make these fields to reference later
        int BPB_ResvdSectCnt = getBytesData(fat32,14,2);
        int BPB_NumFATs = getBytesData(fat32,16,1);
        int FATsz = getBytesData(fat32,36,4);
        int BPB_RootEntCnt = getBytesData(fat32,17,2);
        int BPB_BytsPerSec = getBytesData(fat32,11,2);
        int RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec -1)) / BPB_BytsPerSec;
        int FirstDataSector = BPB_ResvdSectCnt + (BPB_NumFATs * FATsz) + RootDirSectors;
        int BPB_SecPerClus = getBytesData(fat32,13,1);
        int N = getBytesData(fat32,44,4);
        int BPB_RootClus = N;
        int FirstSectorofCluster = ((N - 2) * BPB_SecPerClus) + FirstDataSector;
        int rootDir = FirstSectorofCluster * BPB_BytsPerSec;
    }







     /*int length = 0x8FFFFFF;
        FileChannel fc = new FileInputStream(new File(fat32Img)).getChannel();
        MappedByteBuffer ib = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
        while(ib.hasRemaining())
            System.out.println(ib.get());
        fc.close();*/


       /* FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(fat32Img);
            out = new FileOutputStream("outagain.txt");
            int c;

            while ((c = in.read()) != -1) {
                System.out.println(c);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }*/

}
