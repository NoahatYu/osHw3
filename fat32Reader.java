package com.company;
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
    //TODO: ADD helper methods!
    public static void main(String[] args) throws IOException {
        /* Parse args and open our image file */
        String fat32Img = args[0];
        MappedByteBuffer out = new RandomAccessFile("fat32Img", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, length);

        /*for(int i = 0; i < length; i++)
            out.put((byte)'x');
        System.out.println("Finished writing");*/
        for(int i = length/2; i < length/2 + 6; i++)
            System.out.print((char)out.get(i));


		/* Parse boot sector and get information */

		/* Get root directory address */
        //printf("Root addr is 0x%x\n", root_addr);


		/* Main loop.  You probably want to create a helper function
       		for each command besides quit. */


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
                    //run info helper method
                    break;
                case "open":
                    System.out.println("Going to open");
                    //run open helper method
                    break;
                case "close":
                    System.out.println("Going to close");
                    //run close helper method
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
}
