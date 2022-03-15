package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

public class Playground {
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static void main(String args[]) {
        /*
        String parentCommitID = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String message = "Dummy commit";
        HashMap<String, String> filesMap = new HashMap<>();
        String blobID1 = "3e8bf1d794ca2e9ef8a4007275acf3751c7170ff";
        String blobID2 = "e881c9575d180a215d1a636545b8fd9abfb1d2bb";
        filesMap.put("stupid.txt", blobID1);
        filesMap.put("dumb.txt", blobID2);

        Commit dummyCommit1 = new Commit(message, parentCommitID);
        dummyCommit1.setFilesMap(filesMap);
        */

        /*
        File f1 = new File("/Users/rajendra/cs61b/sp21-s1591/proj2/originalstory.txt");
        File f2 = new File("/Users/rajendra/cs61b/sp21-s1591/proj2/copystory.txt");

        FileBlob b1 = new FileBlob(f1);
        FileBlob b2 = new FileBlob(f2);

        String b1ID = sha1(serialize(b1));
        String b2ID = sha1(serialize(b2));

        System.out.println("b1ID = "+b1ID);
        System.out.println("b2ID = "+b2ID);

        System.out.println("Are b1ID and b2ID equal? "+b1ID.equals(b2ID));
         */

        /*
        File f1 = new File("/Users/rajendra/cs61b/sp21-s1591/proj2/story.txt");
        String contents = readContentsAsString(f1);
         */
        //String [] lines = contents.split("\n");
        /*
        String removedFiles = "wug.txt"+"\n"+"notwug.txt"+"\n"+"fire.txt";
        unstageFromRemoval("wug.txt", removedFiles);

         */
        /*
        String commitID = "a0da1ea5a15ab613bf9961fd86f010cf74c7ee48";
        String shortID = "a0da1e";
        if (commitID.indexOf(shortID) == 0) {
            System.out.println("success");
        }

         */
        File f = join(CWD, "hello");
        String path = f.getParent();
    }

    private static void unstageFromRemoval(String fileName, String removedFiles) {
        String newLine = "\n";
        String empty = "";
        /*
        if (removedFiles.contains(newLine + fileName + newLine)) {
            //String target = newLine + fileName;
            removedFiles = removedFiles.replace(newLine + fileName, empty);
        }
        else if (removedFiles.contains(newLine + fileName)) {
            //String target = newLine + fileName;
            removedFiles = removedFiles.replace(newLine + fileName, empty);
        }
        else if (removedFiles.contains(fileName + newLine)) {
            //String target = fileName + newLine;
            removedFiles = removedFiles.replace(fileName + newLine, empty);
        }

         */
        String[] removedArr = removedFiles.split(newLine);
        String newRemoved = "";
        for(String removed: removedArr) {
            if(removed.equals(fileName)) {
                newRemoved = newRemoved + empty;
            } else {
                if (newRemoved.length() == 0) {
                    newRemoved = removed;
                } else {
                    newRemoved = newRemoved + newLine + removed;
                }
            }

        }

    }
}
