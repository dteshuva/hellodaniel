import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static java.lang.System.exit;

public class fat32_reader {
    private static File file;
    private static byte[] FAT;
    private static Directory dir;
    private static RandomAccessFile raf;

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.printf("error: please input the file name.%n");
            // System.out.println("error: please input the file name.");
            return;
        }
        try {
            file = new File(args[0]);
        } catch (Exception e) {
            System.out.printf("Error: file not found.%n");
            // System.out.println("Error: file not found.");
            return;
        }

        raf = new RandomAccessFile(args[0], "r");

        dir = new Directory();

        setup();

        dir.setup();

        run();
    }

    public static void setup() throws IOException {
        raf.seek(dir.offsetOfStartOfFatRegion());
        FAT = new byte[dir.BPB_FATSz32 * dir.BPB_BytesPerSec];
        raf.read(FAT);
    }

    public static void run() throws IOException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            dir.printDirectory();

            String input = scanner.nextLine();

            String[] commandLine = input.split(" ");
            int length = commandLine.length;

            // To upper case?
            String command = commandLine[0].toLowerCase();

            dir.seekToStartOfCurrentDir();

            if (!list(command, length) && !changeDirectory(command, commandLine) && !info(command, length) && !stat(command, commandLine) && !stop(command, length) && !size(command, commandLine) && !read(command, commandLine)) {
                System.out.printf("error: unknown command.%n");
                // System.out.println("error: unknown command.");
            }
        }
    }

    private static boolean stop(String command, int length) {
        if (!command.equals("stop")) {
            return false;
        }
        if (length != 1) {
            numberOfArgs();
            return true;
        }
        // stop
        try {
            raf.close();
        } catch (Exception e) {
            System.out.printf("Error closing file reader%n");
            // System.out.println("Error closing file reader");
        }
        exit(0);
        return true;
    }

    private static boolean info(String command, int length) {
        if (!command.equals("info")) {
            return false;
        }
        if (length != 1) {
            numberOfArgs();
            return true;
        }
        dir.printInfo();
        return true;
    }

    private static boolean stat(String command, String[] commandLine) throws IOException {
        if (!command.equals("stat")) {
            return false;
        }
        if (commandLine.length != 2) {
            numberOfArgs();
            return true;
        }

        String fileName = getFileName(commandLine);
        if (isCurrentDirectory(fileName) || (dir.isRootDirectory() && isParentDirectory(fileName))) {
            byte[] directoryEntry = new byte[32];
            raf.read(directoryEntry);
            printDirectoryInfo(directoryEntry);
            return true;
        }
        byte[] directoryEntry;
        if (isParentDirectory(fileName)) {
            // Duplicate logic?
            directoryEntry = new byte[32];
            seekNextEntry();
            raf.read(directoryEntry);
        }
        else if ((directoryEntry = findFile(fileName)) == null) {
            System.out.printf("Error: file/directory does not exist%n");
            // System.out.println("Error: file/directory does not exist");
            return true;
        }
        printDirectoryInfo(directoryEntry);
        return true;
    }

    // Fix me
    private static void printDirectoryInfo(byte[] directoryEntry) throws IOException {
        byte[] cluster = new byte[4];

        setClusters(cluster, directoryEntry);

        int firstCluster = endianToInt(cluster);
        byte attributes = directoryEntry[11];

        if (getBit(4, attributes) != 1) {
            outputFileEntryInformation(directoryEntry, firstCluster, attributes);
        } else {
            System.out.printf("Size is 0%nAttributes ATTR_DIRECTORY%nNext cluster number is 0x%04X%n", firstCluster);
            // System.out.println(String.format("Size is 0%nAttributes ATTR_DIRECTORY%nNext cluster number is 0x%s", String.format("%04X", firstCluster)));
        }
    }

    private static void outputFileEntryInformation(byte[] directoryEntry, int firstCluster, byte attributes) {
        System.out.printf("Size is %d%nAttributes ", endianToInt(Arrays.copyOfRange(directoryEntry, 28, 32)));

        StringBuilder sb = new StringBuilder();
        if (getBit(5, attributes) == 1) sb.append("ATTR_ARCHIVE ");
        if (getBit(4, attributes) == 1) sb.append("ATTR_DIRECTORY ");
        if (getBit(3, attributes) == 1) sb.append("ATTR_VOLUME_LABEL ");
        if (getBit(2, attributes) == 1) sb.append("ATTR_SYSTEM ");
        if (getBit(1, attributes) == 1) sb.append("ATTR_HIDDEN ");
        if (getBit(0, attributes) == 1) sb.append("ATTR_READ_ONLY ");

        // System.out.printf(sb.isEmpty() ? "NONE%n" : "%s%n", sb.toString());
        System.out.printf(sb.length() == 0 ? "NONE%n" : "%s%n", sb.toString());

        // What should it be padded to
        System.out.printf("Next cluster number is 0x%04d%n", firstCluster);
    }

    public static boolean list(String command, int length) throws IOException {
        if (!command.equals("ls")) {
            return false;
        }
        if (length != 1) {
            numberOfArgs();
            return true;
        }
        int currentCluster = (int) dir.currentClusterNumber;

        int clusterBytesLeft = dir.getBytesPerCluster() - 32;

        byte[] directoryEntry;

        List<String> names = new ArrayList<>();

        // Skip first entry it's the parent directory entry
        seekNextEntry();

        // Logic is very similar to findFile
        while (true) {
            directoryEntry = new byte[32];
            clusterBytesLeft -= 32;

            raf.read(directoryEntry);

            if (!isLongEntry(directoryEntry[11]) && !erasedFile(directoryEntry)) {
                String name = getName(directoryEntry).toString();
                if (!name.isBlank()) {
                    names.add(name);
                }
            }
            // Need to go to next cluster
            if (clusterBytesLeft == 0) {
                currentCluster = nextCluster(currentCluster);
                if (currentCluster == -1) {
                    return true;
                }
                clusterBytesLeft = dir.getBytesPerCluster();
            }

            if (breakLoop(directoryEntry, currentCluster)) {
                break;
            }
        }

        // Duplicate logic?
        if (!names.contains(".")) {
            names.add(".");
        }
        if (!names.contains("..")) {
            names.add("..");
        }
        Collections.sort(names);

        for (int i = 0; i < names.size(); i++) {
            System.out.printf("%s%s", (i != 0 ? " " : ""), names.get(i));
            // System.out.print((i != 0 ? " " : "") + names.get(i));
        }
        System.out.printf("%n");
        // System.out.println();
        return true;
    }

    // Change to return a string?
    // list, findFile
    private static StringBuilder getName(byte[] directoryEntry) {
        StringBuilder fileName = new StringBuilder();
        StringBuilder fileType = new StringBuilder();
        buildFileName(fileName, directoryEntry);
        for (int i = 0; i < 3; i++) {
            byte b = directoryEntry[8 + i];
            if (b >= 32 && b <= 126) {
                char c = (char) b;
                if (c != ' ') {
                    fileType.append(c);
                }
            }
        }
        if (!isBlank(fileType)) {
            fileName.append(".");
            fileName.append(fileType);
        }
        return fileName;
    }

    public static boolean size(String command, String[] commandLine) throws IOException {
        if (!command.equals("size")) {
            return false;
        }
        if (commandLine.length != 2) {
            numberOfArgs();
            return true;
        }

        String fileName = getFileName(commandLine);

        byte[] directoryEntry = findFile(fileName);

        // fileName.contains(".")
        // duplicate logic
        if (isCurrentDirectory(fileName) || isParentDirectory(fileName) || !fileName.contains(".") || directoryEntry == null || directoryEntry[0] == 0x00 || fileName.equals("..")) {
            System.out.printf("Error: %s is not a file%n", fileName);
            // System.out.println("Error: " + fileName + " is not a file");
        } else {
            System.out.printf("Size of %s is %d bytes%n", fileName, endianToInt(Arrays.copyOfRange(directoryEntry, 28, 32)));
            // System.out.println("Size of " + fileName + " is " + endianToInt(Arrays.copyOfRange(directoryEntry, 28, 32)) + " bytes");
        }
        return true;
    }

    public static boolean changeDirectory(String command, String[] commandLine) throws IOException {
        if (!command.equals("cd")) {
            return false;
        }
        if (commandLine.length != 2) {
            numberOfArgs();
            return true;
        }

        String directory = getFileName(commandLine);
        if (isCurrentDirectory(directory)) {
            return true;
        }

        byte[] directoryEntry = new byte[32];
        byte[] cluster = new byte[4];

        if (!isParentDirectory(directory)) {
            if (directory.contains(".") || (directoryEntry = findFile(directory)) == null) {
                System.out.printf("Error: %s is not a directory%n", directory);
                // System.out.println("Error: " + directory + " is not a directory");
                return true;
            }
        } else if (!dir.isRootDirectory()) {
            // Duplicate logic
            directoryEntry = new byte[32];
            seekNextEntry();
            raf.read(directoryEntry);
        }
        setClusters(cluster, directoryEntry);

        dir.currentClusterNumber = endianToInt(cluster);

        dir.checkClusterNumber();

        dir.updateStartOfCurrentDir();

        if (isParentDirectory(directory)) {
            dir.updateDirectory();
        } else {
            dir.appendToDirectory(directory);
        }
        dir.seekToStartOfCurrentDir();
        return true;
    }

    // Should work on directory or file
    // Called in read, stat, cd, size
    public static byte[] findFile(String fileName) throws IOException {
        int currentCluster = (int) dir.currentClusterNumber;

        int clusterBytesLeft = dir.getBytesPerCluster();

        byte[] directoryEntry;

        // Logic is very similar to list
        while (true) {
            clusterBytesLeft -= 32;
            directoryEntry = new byte[32];

            // Should read in new short name every time and stop at the correct point
            raf.read(directoryEntry);

            if (!isLongEntry(directoryEntry[11]) && !erasedFile(directoryEntry)) {
                String name = getName(directoryEntry).toString();
                // Found file, return the entry.
                if (name.equalsIgnoreCase(fileName)) {
                    return directoryEntry;
                }
                // Need to go to next cluster
                if (clusterBytesLeft == 0) {
                    currentCluster = nextCluster(currentCluster);
                    if (currentCluster == -1) {
                        return null;
                    }
                    clusterBytesLeft = dir.getBytesPerCluster();
                }
            }
            if (breakLoop(directoryEntry, currentCluster)) {
                break;
            }
        }
        return null;
    }

    public static boolean read(String command, String[] commandLine) throws IOException {
        if (!command.equals("read")) {
            return false;
        }
        String fileName = getFileName(commandLine);
        if (commandLine.length != 4) {
            // Upper case E?
            System.out.printf("error: incorrect number of arguments.%n");
            // System.out.println("error: incorrect number of arguments.");
            return true;
        }
        int offset;
        try {
            offset = Integer.parseInt(commandLine[2]);
        } catch (Exception e) {
            // Upper case E?
            System.out.printf("error: offset must be a number.%n");
            // System.out.println("error: offset must be a number.");
            return true;
        }

        int numBytes;
        try {
            numBytes = Integer.parseInt(commandLine[3]);
        } catch (Exception e) {
            // Upper case E?
            System.out.printf("error: numbytes must be a number.%n");
            // System.out.println("error: numbytes must be a number.");
            return true;
        }

        if (offset < 0) {
            System.out.printf("Error: OFFSET must be a positive value%n");
            // System.out.println("Error: OFFSET must be a positive value");
            return true;
        }

        if (numBytes <= 0) {
            System.out.printf("Error: NUM_BYTES must be greater than zero%n");
            // System.out.println("Error: NUM_BYTES must be a greater than zero");
            return true;
        }

        int leftToOutput = numBytes;

        int bytesLeftInCluster = dir.getBytesPerCluster() - offset;

        byte[] directoryEntry = findFile(fileName);

        // duplicate logic
        if (directoryEntry == null || directoryEntry[0] == 0) {
            System.out.printf("Error: file/directory does not exist%n");
            // System.out.println("Error: file/directory does not exist");
            return true;
        }

        // "endianToInt(Arrays.copyOfRange(directoryEntry, 28, 32))" is repeated 3 times
        if (offset + numBytes > endianToInt(Arrays.copyOfRange(directoryEntry, 28, 32))) {
            System.out.printf("Error: attempt to read data outside of file bounds%n");
            // System.out.println("Error: attempt to read data outside of file bounds");
            return true;
        }
        byte[] cluster = new byte[4];

        setClusters(cluster, directoryEntry);

        int currentCluster = endianToInt(cluster);

        while (bytesLeftInCluster > 0) {
            int currentClusterBytes = Math.min(numBytes, bytesLeftInCluster);

            // Turn into helper method
            long position = dir.FirstDataByte + ((long) (currentCluster - dir.BPB_RootClus) * dir.getBytesPerCluster()) + offset;

            raf.seek(position);

            byte[] message = new byte[currentClusterBytes];

            try {
                raf.read(message);
            } catch (Exception e) {
                System.out.printf("Error: attempt to read data outside of file bounds%n");
                // System.out.println("Error: attempt to read data outside of file bounds");
                return true;
            }

            for (byte byt : message) {
                int b = Byte.toUnsignedInt(byt);
                //for each byte, if the byte is less than decimal 127, print the corresponding ascii character
                //Else, print " 0xNN", where NN is the hex value of the byte.
                if (b < 127) {
                    System.out.printf("%c", b);
                    // System.out.print((char) b);
                } else {
                    System.out.printf("0x%02d%n", b);
                    // System.out.println(String.format("0x%02d", b));
                }
                leftToOutput--;
            }
            if (leftToOutput <= 0) {
                // End on a newline
                System.out.printf("%n");
                // System.out.println();
                return true;
            }
            // Subtract bytes that were just read
            bytesLeftInCluster -= currentClusterBytes;
            if (bytesLeftInCluster == 0) {
                currentCluster = getClusterFromFAT(currentCluster);
            }
        }
        System.out.printf("%n");
        // System.out.println();
        return true;
    }

    /*******************
     * Helper Methods
     *******************/
    public static void seekNextEntry() throws IOException {
        raf.seek(raf.getFilePointer() + 32);
    }

    public static int getClusterFromFAT(int cluster) {
        return endianToInt(Arrays.copyOfRange(FAT, cluster * 4, (cluster * 4) + 4));
    }

    private static boolean isParentDirectory(String fileName) {
        return fileName.equals("..");
    }

    private static boolean isCurrentDirectory(String fileName) {
        return fileName.equals(".");
    }

    public static boolean isBlank(StringBuilder stringBuilder) {
        if (stringBuilder == null || stringBuilder.length() == 0) {
            return true;
        }

        for (int i = 0; i < stringBuilder.length(); i++) {
            if (!Character.isWhitespace(stringBuilder.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static String getFileName(String[] commandLine) {
        return commandLine[1].toUpperCase();
    }


    private static void setClusters(byte[] cluster, byte[] directoryEntry) {
        cluster[0] = directoryEntry[26];
        cluster[1] = directoryEntry[27];
        cluster[2] = directoryEntry[20];
        cluster[3] = directoryEntry[21];
    }

    private static boolean breakLoop(byte[] directoryEntry, int cluster) {
        return directoryEntry[0] == 0 || (cluster >= 0x0FFFFFF8 && cluster <= 0x0FFFFFFF); // cluster >= 0x0FFFFFF8 && cluster <= 0x0FFFFFFF
    }

    // Get new cluster
    public static int nextCluster(int currentCluster) throws IOException {
        currentCluster = getClusterFromFAT(currentCluster);
        // Check if broken cluster
        if (currentCluster == 0x0FFFFFF7) {
            System.out.printf("Error: broken cluster%n");
            // System.out.println("Error: broken cluster");
            return -1;
        }
        dir.seekCurrentCluster(currentCluster);
        return currentCluster;
    }

    private static void buildFileName(StringBuilder s, byte[] directoryEntry) {
        for (int i = 0; i < 8; i++) {
            byte b = directoryEntry[i];
            if (b >= 32 && b <= 126) {
                char c = (char) b;
                if (c != ' ') {
                    s.append(c);
                }
            }
        }
    }

    private static boolean isLongEntry(byte attributes) {
        for (int i = 0; i < 4; i++) {
            if (getBit(i, attributes) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean erasedFile(byte[] directoryEntry) {
        return directoryEntry[0] == 0xE5;
    }

    /**
     * Convert little-endian byte array into an int
     */
    public static int endianToInt(byte[] bytes) {
        int len = bytes.length;

        int[] values = new int[len];

        for (int i = 0; i < len; i++) {
            values[i] = Byte.toUnsignedInt(bytes[i]);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = len - 1; i >= 0; i--) {
            // sb.append(toHex(values[i]));

            if (values[i] == 0) {
                sb.append("00");
            } else {
                sb.append(toHex(values[i]));
            }

        }

        return Integer.parseInt(sb.toString(), 16);
        /*
        int sum = 0;
        for (int i = 0; i < bytes.length; i++) {
            sum += Byte.toUnsignedInt(bytes[i]) * Math.pow(256, i);
        }
        return sum;
        */
    }

    public static int getBit(int position, byte b) {
        return (b >> position) & 1;
    }

    private static void numberOfArgs() {
        System.out.printf("error: incorrect number of arguments.%n");
        // System.out.println("error: incorrect number of arguments.");
    }

    public static String toHex(final int n) {
        return Integer.toHexString(n);
    }

    public static class Directory {
        // workingDirectory should be a stringbuilder
        public static String workingDirectory;

        public static int BPB_BytesPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATS, BPB_FATSz32, BPB_RootClus;

        public static long FirstDataByte, startOfCurrentDir,  currentClusterNumber; // StartOfFAT,

        Directory() {
            workingDirectory = "/";

            try {
                BPB_SecPerClus = readBytes(13, 1);
                BPB_NumFATS = readBytes(16, 1);
                BPB_RsvdSecCnt = readBytes(14, 2);

                // shouldn't be 11, 2
                BPB_BytesPerSec = readBytes(11, 2);
                // BPB_BytesPerSec = readBytes(10, 3);

                BPB_FATSz32 = readBytes(36, 4);
                BPB_RootClus = readBytes(44, 4);
            } catch (Exception IOException) {}

            // StartOfFAT = offsetOfStartOfFatRegion();
            FirstDataByte = BPB_BytesPerSec * sectorOfStartOfDataRegion();
        }

        public static void setup() {
            startOfCurrentDir = FirstDataByte;
            currentClusterNumber = BPB_RootClus;
        }

        public static long offsetOfStartOfFatRegion() {
            return BPB_RsvdSecCnt * BPB_BytesPerSec;
        }

        private static int sectorOfStartOfDataRegion() {
            return BPB_RsvdSecCnt + (BPB_NumFATS * BPB_FATSz32);
        }

        public static boolean isRootDirectory() {
            return "/".equals(workingDirectory);
        }

        public static boolean lastCharIsSlash() {
            return workingDirectory.charAt(workingDirectory.length() - 1) == '/';
        }

        public static void updateDirectory() {
            workingDirectory = workingDirectory.substring(0, workingDirectory.lastIndexOf('/') + 1);
            if (!isRootDirectory() && lastCharIsSlash()) {
                workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
            }
        }

        public static void appendToDirectory(String directory) {
            if (!lastCharIsSlash()) {
                workingDirectory += "/";
            }
            workingDirectory += directory;
        }

        public static void printDirectory() {
            System.out.printf("%s] ", workingDirectory);
            // System.out.print(workingDirectory + "] ");
        }

        /**
         * Method to read a specified number of bytes from a given offset in the file.
         * @param offset The offset in the file to start reading from.
         * @param numBytes The number of bytes to read.
         * @return The byte array containing the read bytes.
         * @throws IOException If there is an IO exception while reading the file.
         */
        public static int readBytes(long offset, int numBytes) throws IOException {
            //set the file pointer to the specified offset
            raf.seek(offset);
            //read the specified number of bytes into a byte array
            byte[] bytes = new byte[numBytes];
            raf.read(bytes);
            return endianToInt(bytes);
        }

        public static int getBytesPerCluster() {
            return BPB_BytesPerSec * BPB_SecPerClus;
        }

        public static void seekCurrentCluster(int cluster) throws IOException {
            raf.seek(calculatePosition(cluster));
        }

        public static long calculatePosition(int cluster) {
            return FirstDataByte + (long) getBytesPerCluster() * (cluster - 2);
            /*
			long getFirstSectorOfCluster = ((long)(clusterNumber - 2) * BPB_SecPerClus) + sectorOfStartOfDataRegion();
			return getFirstSectorOfCluster * BPB_BytesPerSec;
             */
        }

        public static void checkClusterNumber() {
            if (currentClusterNumber == 0) {
                currentClusterNumber = 2;
            }
        }

        // 2x check that the 'f' can be lower case
        public static void printInfo() {
            System.out.printf("BPB_BytesPerSec is 0x%X, %d%n" +
                            "BPB_SecPerClus is 0x%X, %d%n" +
                            "BPB_RsvdSecCnt is 0x%X, %d%n" +
                            "BPB_NumFATS is 0x%X, %d%n" +
                            "BPB_FATSz32 is 0x%X, %d%n",
                    BPB_BytesPerSec, BPB_BytesPerSec,
                    BPB_SecPerClus, BPB_SecPerClus,
                    BPB_RsvdSecCnt, BPB_RsvdSecCnt,
                    BPB_NumFATS, BPB_NumFATS,
                    BPB_FATSz32, BPB_FATSz32);
            /*
            System.out.println("BPB_BytesPerSec is 0x" + toHex(BPB_BytesPerSec) + ", " + BPB_BytesPerSec + "\n" +
                               "BPB_SecPerClus is 0x" + toHex(BPB_SecPerClus) + ", " + BPB_SecPerClus + "\n" +
                               "BPB_RsvdSecCnt is 0x" + toHex(BPB_RsvdSecCnt) + ", " + BPB_RsvdSecCnt + "\n" +
                               "BPB_NumFATS is 0x" + toHex(BPB_NumFATS) + ", " + BPB_NumFATS + "\n" +
                               "BPB_FATSz32 is 0x" + toHex(BPB_FATSz32) + ", " + BPB_FATSz32);
             */
        }

        public static void seekToStartOfCurrentDir() throws IOException {
            raf.seek(startOfCurrentDir);
        }

        public static void updateStartOfCurrentDir() {
            startOfCurrentDir = FirstDataByte + (currentClusterNumber - BPB_RootClus) * getBytesPerCluster();
        }
    }
}
