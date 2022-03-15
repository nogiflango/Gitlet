package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Rajendra Goudar
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private static String dateOrder = "EEE MMM d HH:mm:ss yyyy Z";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateOrder);
    private String message;
    private Date date;
    private String firstParent;
    private String secondParent;

    private HashMap<String, String> filesMap;
    private int depth;

    public Commit(String m, String firstPar, String secPar, HashMap<String, String> fM, int depth) {
        date = new Date();
        this.message = m;
        this.firstParent = firstPar;
        this.secondParent = secPar;
        this.filesMap = fM;
        this.depth = depth;
    }

    public Commit(String message, String firstParent, String secondParent, Date date) {
        this.message = message;
        this.firstParent = firstParent;
        this.secondParent = secondParent;
        this.date = date;
        filesMap = new HashMap<>();
        this.depth = 0;
    }

    public String getFirstParentCommitID() {
        return firstParent;
    }
    public String getSecondParentCommitID() {
        return secondParent;
    }


    public  String getMessage() {
        return message;
    }
    public String getDate() {
        return simpleDateFormat.format(date);
    }
    public HashMap<String, String> getFilesMap() {
        return filesMap;
    }
    public int getDepth() {
        return depth;
    }

    public void saveCommit(String commitID) {
        File commitFileObj = join(Repository.COMMITS, commitID);
        writeObject(commitFileObj, this);

    }

}
