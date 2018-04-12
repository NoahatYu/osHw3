import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

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
    private int N;
    /**
     * Constructor
     */
    public fat32Reader(String fat32) throws IOException {
        BPB_ResvdSecCnt = getBytesData(fat32,14,2);
        BPB_NumFATs = getBytesData(fat32,16,1);
        FATsz = getBytesData(fat32,36,4);
        BPB_RootEntCnt = getBytesData(fat32,17,2);
        BPB_BytsPerSec = getBytesData(fat32,11,2);
        RootDirSectors = ((BPB_RootEntCnt * 32) + (BPB_BytsPerSec -1)) / BPB_BytsPerSec;
        FirstDataSector = BPB_ResvdSecCnt + (BPB_NumFATs * FATsz) + RootDirSectors;
        BPB_SecPerClus = getBytesData(fat32,13,1);
        N = getBytesData(fat32,44,4);
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
        fat32Reader f32Reader = new fat32Reader(fat32Img);
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
                    //run stat helper method
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
        System.out.println("BPB_BytesPerSec: " + BPB_BytsPerSec + ", " + hexer(BPB_BytsPerSec));
        System.out.println("BPB_SecPerClus: " + BPB_SecPerClus + ", " + hexer(BPB_SecPerClus));
        System.out.println("BPB_RsvdSecCnt: " + BPB_ResvdSecCnt + ", " + hexer(BPB_ResvdSecCnt));
        System.out.println("BPB_NumFATS: " + BPB_NumFATs + ", " + hexer(BPB_NumFATs));
        System.out.println("BPB_FATSz32: " + FATsz + ", " + hexer(FATsz));

    }
    public String hexer(int num){
        return "0x" + Integer.toHexString(num);
    }



    /*public static void tester(){
        long fat_begin_lba = Partition_LBA_Begin + Number_of_Reserved_Sectors;
        long cluster_begin_lba = Partition_LBA_Begin + Number_of_Reserved_Sectors + (Number_of_FATs * Sectors_Per_FAT);
        long sectors_per_cluster = BPB_SecPerClus;
        long root_dir_first_cluster = BPB_RootClus;
    }*/


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
        MappedByteBuffer out = memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 10485760/*10MB*/);

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



    public void getRootDirectory(String fat32) throws IOException {
    //TODO: Make these fields to reference later
        int BPB_RootClus = N;
        int FirstSectorofCluster = ((N - 2) * BPB_SecPerClus) + FirstDataSector;
        int rootDir = FirstSectorofCluster * BPB_BytsPerSec;
    }

}

