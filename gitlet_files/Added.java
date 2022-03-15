package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Added implements Serializable {
    private String fileName;
    private String blobID;
    public static int fileNo = 1;
    public Added(String fileName, String blobID) {
        this.fileName = fileName;
        this.blobID = blobID;
        fileNo++;
    }

    public void saveAdded() throws IOException {
        String addedID = "added"+fileNo;
        File addedObj = Utils.join(Repository.STAGING_AREA, addedID);
        addedObj.createNewFile();
        Utils.writeObject(addedObj, this);
    }

}
