package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class FileBlob implements Serializable {
    private String contents;
    public FileBlob(File fileObj) {
        contents = readContentsAsString(fileObj);
    }

    public String getContents() {
        return contents;
    }

    public void saveBlob(String blobID) {
        File blobFileObj = join(Repository.BLOBS, blobID);
        writeObject(blobFileObj, this);
    }
}
