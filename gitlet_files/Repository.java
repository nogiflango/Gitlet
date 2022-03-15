package gitlet;

//import org.apache.commons.math3.analysis.function.Add;
//import org.checkerframework.checker.units.qual.A;
//import org.checkerframework.checker.units.qual.C;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Rajendra Goudar
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGING_AREA = join(GITLET_DIR, "STAGING");
    public static final File ADDED = join(STAGING_AREA, "ADDED");
    public static final File REMOVED = join(STAGING_AREA, "REMOVED.txt");
    public static final File COMMITS = join(GITLET_DIR, "COMMITS");
    public static final File BLOBS = join(GITLET_DIR, "BLOBS");
    public static final File HEAD = join(GITLET_DIR, "HEAD.txt");
    public static final File BRANCHES = join(GITLET_DIR, "BRANCHES");
    public static final File CURRENT_BRANCH = join(GITLET_DIR, "CURRENT_BRANCH.txt");
    public static final File REMOTES = join(GITLET_DIR, "REMOTES");

    public static void initRepository() throws IOException {
        if (GITLET_DIR.exists()) {
            String m = "A Gitlet version-control system already exists in the current directory.";
            System.out.println(m);
            System.exit(0);
        } else {
            GITLET_DIR.mkdir();
            COMMITS.mkdir();
            STAGING_AREA.mkdir();
            ADDED.mkdir();
            REMOVED.createNewFile();
            BLOBS.mkdir();
            HEAD.createNewFile();
            BRANCHES.mkdir();
            CURRENT_BRANCH.createNewFile();
            REMOTES.mkdir();
            String message = "initial commit";
            Date epoch = new Date(0);

            Commit initCommit = new Commit(message, null, null, epoch);
            String initCommitID = sha1(serialize(initCommit));
            initCommit.saveCommit(initCommitID);
            /*
            updateHead(initCommitID);
             */
            branch("master");
            setCurrentBranch("master");
            updateCurrentBranch(initCommitID);
        }
    }

    public static void add(String addFile) {
        File addFileObj = join(CWD, addFile);
        if (addFileObj.exists()) {
            unstageFromRemoval(addFile);
            FileBlob addBlob = new FileBlob(addFileObj);
            if (addToStagingRequired(addFile, addBlob)) {
                String addBlobID = sha1(serialize(addBlob));
                addBlob.saveBlob(addBlobID);
                File stageFile = join(ADDED, addFile);
                writeContents(stageFile, addBlobID);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    private static void unstageFromRemoval(String fileName) {
        String newLine = "\n";
        String empty = "";
        String removedFiles = readContentsAsString(REMOVED);
        String[] removedArr = removedFiles.split(newLine);
        String newRemoved = "";
        for (String removed: removedArr) {
            if (removed.equals(fileName)) {
                newRemoved = newRemoved + empty;
            } else {
                if (newRemoved.length() == 0) {
                    newRemoved = removed;
                } else {
                    newRemoved = newRemoved + newLine + removed;
                }
            }
        }
        writeContents(REMOVED, newRemoved);
    }


    private static boolean isStagedForRemoval(String fileName) {
        String newLine = "\n";
        String removedFiles = readContentsAsString(REMOVED);
        String[] removedArr = removedFiles.split(newLine);
        for (String removed: removedArr) {
            if (removed.equals(fileName)) {
                return true;
            }
        }
        return false;

    }

    private static boolean addToStagingRequired(String addFile, FileBlob addBlob) {
        Commit headCommit = getHeadCommit();
        HashMap<String, String> filesMap = headCommit.getFilesMap();
        if (filesMap.containsKey(addFile)) {
            String currBlobID = sha1(serialize(addBlob));
            String prevBlobID = filesMap.get(addFile);
            if (currBlobID.equals(prevBlobID)) {
                File changed = join(ADDED, addFile);
                if (changed.exists()) {
                    changed.delete();
                }
                return false;
            }
            return true;
        }
        return true;
    }

    private static Commit getHeadCommit() {
        String headCommitID = readContentsAsString(HEAD);
        File headCommitFile = join(COMMITS, headCommitID);
        Commit headCommit = readObject(headCommitFile, Commit.class);
        return headCommit;
    }

    /*
    private static void updateHead(String updatedHead) {
        writeContents(HEAD, updatedHead);
    }
     */

    private static void pointHeadToCurrBranch() {
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        File currentBranchFile = join(BRANCHES, currentBranchName);
        String currentBranchID = readContentsAsString(currentBranchFile);
        writeContents(HEAD, currentBranchID);
    }


    private static void updateCurrentBranch(String commitID) {
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        File currentBranchFile = join(BRANCHES, currentBranchName);
        writeContents(currentBranchFile, commitID);
        pointHeadToCurrBranch();
    }

    public static void commit(String message) {
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        HashMap<String, String> filesMapCopy = getFilesMapCopy();
        boolean changed = false;
        changed = commitAdded(filesMapCopy) || changed;
        changed = commitRemoved(filesMapCopy) || changed;
        if (changed) {
            createCommitAndSave(message, null, filesMapCopy);
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

    }

    private static boolean commitAdded(HashMap<String, String> filesMap) {
        File[] addedFiles = ADDED.listFiles();
        if (addedFiles.length > 0) {
            putAddedFilesIntoFilesMap(addedFiles, filesMap);
            clearAdded(addedFiles);
            return true;
        }
        return false;
    }

    private static boolean commitRemoved(HashMap<String, String> filesMap) {
        String removedFiles = readContentsAsString(REMOVED);
        if (removedFiles.length() > 0) {
            removeStagedInFilesMap(filesMap, removedFiles);
            clearRemoved();
            return true;
        }
        return false;
    }

    private static void removeStagedInFilesMap(HashMap<String, String> fM, String stagedRemovals) {
        String newLine = "\n";
        String[] allStaged = stagedRemovals.split(newLine);
        for (String stagedRemoval: allStaged) {
            fM.remove(stagedRemoval);
        }
    }


    private static void createCommitAndSave(String m, String secParID, HashMap<String, String> fM) {
        String firstParID = readContentsAsString(HEAD);
        int newDepth = getNewDepth(firstParID, secParID);
        Commit newCommit = new Commit(m, firstParID, secParID, fM, newDepth);
        String commitID = sha1(serialize(newCommit));
        newCommit.saveCommit(commitID);
        //updateHead(commitID);
        updateCurrentBranch(commitID);
    }

    private static int getNewDepth(String firstParID, String secondParID) {
        Commit firstParent = getCommit(firstParID);
        int firstParentDepth = firstParent.getDepth();
        if (secondParID == null) {
            return firstParentDepth + 1;
        }
        Commit secondParent = getCommit(secondParID);
        int secondParentDepth = secondParent.getDepth();
        if (firstParentDepth > secondParentDepth) {
            return firstParentDepth + 1;
        }
        return secondParentDepth + 1;
    }


    private static void clearAdded(File[] files) {
        for (File file: files) {
            file.delete();
        }
    }

    private static void clearRemoved() {
        String empty = "";
        writeContents(REMOVED, empty);
    }

    private static HashMap<String, String> getFilesMapCopy() {
        Commit headCommit = getHeadCommit();
        return (HashMap<String, String>) headCommit.getFilesMap().clone();
    }

    private static void putAddedFilesIntoFilesMap(File[] aF, HashMap<String, String> fileMap) {
        for (File added: aF) {
            String blobID = readContentsAsString(added);
            String fileName = added.getName();
            fileMap.put(fileName, blobID);
        }
    }

    public static void checkout(String fileName) {
        Commit headCommit = getHeadCommit();
        writeCommitContentsToFile(headCommit, fileName);
    }

    public static void checkout(String commitID, String fileName) {
        commitID = shortIDSearch(commitID);
        File commitFile = join(COMMITS, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = readObject(commitFile, Commit.class);
        writeCommitContentsToFile(commit, fileName);
    }

    private static String shortIDSearch(String shortID) {
        File[] allCommits = COMMITS.listFiles();
        for (File commit: allCommits) {
            String commitID = commit.getName();
            if (commitID.indexOf(shortID) == 0) {
                return commitID;
            }
        }
        return shortID;
    }

    public static void branchCheckout(String branchName) {
        branchName = branchName.replace('/', '_');
        File branch = join(BRANCHES, branchName);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);

        }

        String checkoutBranchID = readContentsAsString(branch);

        branchCheckoutHelper(checkoutBranchID);
        changeCurrentBranch(branchName);
        clearRemoved();
        clearAdded(ADDED.listFiles());
    }

    private static void branchCheckoutHelper(String checkoutID) {

        Commit currentCommit = getHeadCommit();
        Commit checkoutCommit = readObject(join(COMMITS, checkoutID), Commit.class);

        HashMap<String, String> currentCommitFiles = currentCommit.getFilesMap();
        HashMap<String, String> checkoutCommitFiles = checkoutCommit.getFilesMap();

        if (untrackedFile(currentCommitFiles, checkoutCommitFiles)) {
            String m = "There is an untracked file in the way; delete it, or add and commit it ";
            m = m + "first.";
            System.out.println(m);
            System.exit(0);
        }
        for (String fileName: currentCommitFiles.keySet()) {
            if (!checkoutCommitFiles.containsKey(fileName)) {
                File old = join(CWD, fileName);
                old.delete();
            }
        }
        for (String fileName: checkoutCommitFiles.keySet()) {
            writeCommitContentsToFile(checkoutCommit, fileName);
        }
        /*
        changeBranch(branchName);
        clearRemoved();
        clearAdded(ADDED.listFiles());
         */
    }
    private static void changeCurrentBranch(String newBranch) {
        setCurrentBranch(newBranch);
        pointHeadToCurrBranch();
    }

    private static boolean untrackedFile(HashMap<String, String> cu, HashMap<String, String> ch) {
        File[] cwdFiles = CWD.listFiles();
        for (File f: cwdFiles) {
            String fileName = f.getName();
            if (!cu.containsKey(fileName) && ch.containsKey(fileName)) {
                return true;
            }
        }
        return false;
    }

    private static void writeCommitContentsToFile(Commit commit, String fileName) {
        File source = join(CWD, fileName);
        String fileContentsInCommit = getFileContentsInCommit(fileName, commit);
        writeContents(source, fileContentsInCommit);
    }

    private static String getFileContentsInCommit(String fileName, Commit commit) {
        HashMap<String, String> filesMap = commit.getFilesMap();
        if (!filesMap.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String blobID = filesMap.get(fileName);
        File f = join(BLOBS, blobID);
        FileBlob fb = readObject(f, FileBlob.class);
        String contents = fb.getContents();
        return contents;
    }

    public static void log() {
        Commit com = getHeadCommit();
        String commitID = readContentsAsString(HEAD);
        while (commitID != null) {
            System.out.println("===");
            System.out.println("commit " + commitID);
            if (com.getFirstParentCommitID() != null && com.getSecondParentCommitID() != null) {
                String shortFirstParID = com.getFirstParentCommitID().substring(0, 7);
                String shortSecondParID = com.getSecondParentCommitID().substring(0, 7);
                System.out.println("Merge: " + shortFirstParID + " " + shortSecondParID);
            }
            System.out.println("Date: " + com.getDate());
            System.out.println(com.getMessage());
            System.out.println();
            String parentCommitID = com.getFirstParentCommitID();
            if (parentCommitID != null) {
                File parentCommitFile = join(COMMITS, parentCommitID);
                com = readObject(parentCommitFile, Commit.class);
            }
            commitID = parentCommitID;
        }

    }

    public static void remove(String removeFile) {
        boolean removed = false;
        removed = removeFromAdded(removeFile) || removed;
        removed = trackedInHeadCommit(removeFile) || removed;
        if (!removed) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }

    private static boolean trackedInHeadCommit(String removeFile) {
        Commit head = getHeadCommit();
        HashMap<String, String> filesMap = head.getFilesMap();
        if (filesMap.containsKey(removeFile)) {
            String newLine = "\n";
            String removedContents = readContentsAsString(REMOVED);
            if (removedContents.length() > 0) {
                writeContents(REMOVED, removedContents + newLine + removeFile);
            } else {
                writeContents(REMOVED, removeFile);
            }
            //Delete removeFile from CWD
            File f = join(CWD, removeFile);
            if (f.exists()) {
                f.delete();
            }
            return true;
        }
        return false;
    }

    private static boolean removeFromAdded(String removeFile) {
        File removeFileObj = join(ADDED, removeFile);
        if (removeFileObj.exists()) {
            removeFileObj.delete();
            return true;
        }
        return false;
    }

    public static void find(String message) {
        File[] allCommits = COMMITS.listFiles();
        boolean found = false;
        for (File fileCommit: allCommits) {
            Commit c = readObject(fileCommit, Commit.class);
            if (message.equals(c.getMessage())) {
                found = true;
                System.out.println(fileCommit.getName());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void globalLog() {
        File[] allCommits = COMMITS.listFiles();
        for (File fileCommit: allCommits) {
            String commitID = fileCommit.getName();
            Commit c = readObject(fileCommit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + commitID);
            System.out.println("Date: " + c.getDate());
            System.out.println(c.getMessage());
            System.out.println();
        }
    }

    public static void branch(String branchName) {
        File newBranch = join(BRANCHES, branchName);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String headCommitID = readContentsAsString(HEAD);
        writeContents(newBranch, headCommitID);
    }

    private static void setCurrentBranch(String branchName) {
        writeContents(CURRENT_BRANCH, branchName);
    }

    public static void status() {
        displayBranches();
        displayStagedForAddition();
        displayStagedForRemoval();
        displayModificationsNotStaged();
        displayUntracked();
        System.out.println();
    }

    private static void displayUntracked() {
        System.out.println("=== Untracked Files ===");
        Commit headCommit = getHeadCommit();
        Set<String> headFiles = headCommit.getFilesMap().keySet();
        Set<String> addedFiles = getAddedFiles();
        File[] cwdFiles = CWD.listFiles();
        for (File f: cwdFiles) {
            String fileName = f.getName();
            if (!headFiles.contains(fileName) && !addedFiles.contains(fileName)) {
                if (fileName.charAt(0) != '.') {
                    System.out.println(fileName);
                }
            } else if (isStagedForRemoval(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    private static void displayModificationsNotStaged() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit headCommit = getHeadCommit();
        Set<String> headFiles = headCommit.getFilesMap().keySet();
        Set<String> addedFiles = getAddedFiles();
        Set<String> filesInScope = getFilesInScope(headFiles, addedFiles);

        for (String f: filesInScope) {
            File cwdFile = join(CWD, f);
            if (addedFiles.contains(f)) {
                if (cwdFile.exists()) {
                    String cwdContents = readContentsAsString(cwdFile);
                    String addedContents = getAddedContents(f);
                    if (!addedContents.equals(cwdContents)) {
                        System.out.println(f + " " + "(modified)");
                    }
                } else {
                    System.out.println(f + " " + "(deleted)");
                }
            } else {
                if (cwdFile.exists()) {
                    String cwdContents = readContentsAsString(cwdFile);
                    String commitContents = getFileContentsInCommit(f, headCommit);
                    if (!commitContents.equals(cwdContents)) {
                        System.out.println(f + " " + "(modified)");
                    }
                } else if (!isStagedForRemoval(f)) {
                    System.out.println(f + " " + "(deleted)");
                }

            }
        }
        System.out.println();
    }

    private static String getAddedContents(String fileName) {
        String blobID = readContentsAsString(join(ADDED, fileName));
        File f = join(BLOBS, blobID);
        FileBlob fb = readObject(f, FileBlob.class);
        String contents = fb.getContents();
        return contents;
    }


    private static Set<String> getFilesInScope(Set<String> headFiles, Set<String> addedFiles) {
        Set<String> filesInScope = new HashSet<>();
        filesInScope.addAll(headFiles);
        filesInScope.addAll(addedFiles);
        return filesInScope;
    }


    private static Set<String> getAddedFiles() {
        Set<String> addedSet = new HashSet<>();
        File[] addedFiles = ADDED.listFiles();
        for (File f: addedFiles) {
            addedSet.add(f.getName());
        }
        return addedSet;
    }


    private static void displayBranches() {
        System.out.println("=== Branches ===");
        List<String> allBranches = plainFilenamesIn(BRANCHES);
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        for (String branchName: allBranches) {
            if (branchName.equals(currentBranchName)) {
                branchName = "*" + branchName;
            }
            System.out.println(branchName);
        }
        System.out.println();
    }
    private static void displayStagedForAddition() {
        System.out.println("=== Staged Files ===");
        List<String> allAdded = plainFilenamesIn(ADDED);
        for (String added: allAdded) {
            System.out.println(added);
        }
        System.out.println();
    }
    private static void displayStagedForRemoval() {
        System.out.println("=== Removed Files ===");
        String allRemovedStr = readContentsAsString(REMOVED);
        String[] allRemoved = allRemovedStr.split("\n");
        Arrays.sort(allRemoved);
        for (String removed : allRemoved) {
            if (removed.length() > 0) {
                System.out.println(removed);
            }
        }
        System.out.println();
    }

    public static void removeBranch(String branchName) {
        File branch = join(BRANCHES, branchName);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branch.delete();
    }

    public static void reset(String checkoutID) {
        File checkoutFile = join(COMMITS, checkoutID);
        if (!checkoutFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        branchCheckoutHelper(checkoutID);
        updateCurrentBranch(checkoutID);
        clearRemoved();
        clearAdded(ADDED.listFiles());
    }

    public static void merge(String givenBranchName) {
        if (!isStagingEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File givenBranchFile = join(BRANCHES, givenBranchName);
        if (!givenBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currentBranchName = readContentsAsString(CURRENT_BRANCH);
        if (givenBranchName.equals(currentBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String currentID = readContentsAsString(HEAD);
        String givenID = readContentsAsString(givenBranchFile);

        Commit currentBranch = getHeadCommit();
        Commit givenBranch = getCommit(readContentsAsString(givenBranchFile));
        String splitID = getSplitCommitID(currentID, givenID);
        Commit splitCommit = getCommit(splitID);


        HashMap<String, String> splitMap = splitCommit.getFilesMap();
        HashMap<String, String> currentMap = currentBranch.getFilesMap();
        HashMap<String, String> givenMap = givenBranch.getFilesMap();



        if (untrackedFile(currentMap, givenMap)) {
            String m = "There is an untracked file in the way; delete it, or add and commit it ";
            m = m + "first.";
            System.out.println(m);
            System.exit(0);
        }

        if (splitID.equals(givenID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitID.equals(currentID)) {
            String checkoutID = readContentsAsString(givenBranchFile);
            reset(checkoutID);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        HashSet<String> filesSet = getFilesSet(splitMap, currentMap, givenMap);
        boolean hadMergeConflict = false;
        for (String fileName: filesSet) {
            String splitBlobID = getBlobID(splitMap, fileName);
            String currentBlobID = getBlobID(currentMap, fileName);
            String givenBlobID = getBlobID(givenMap, fileName);
            oneMod(splitBlobID, currentBlobID, givenBlobID, fileName, givenBranch);
            boolean m = twoMod(splitBlobID, currentBlobID, givenBlobID, fileName);
            hadMergeConflict = m || hadMergeConflict;
        }
        givenBranchName = givenBranchName.replace('_', '/');
        String mergeMsg = "Merged " + givenBranchName + " into " + currentBranchName + ".";
        mergeCommit(mergeMsg, readContentsAsString(givenBranchFile));
        if (hadMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }

    }

    private static void mergeCommit(String message, String secondParentID) {
        HashMap<String, String> filesMapCopy = getFilesMapCopy();
        boolean changed = false;
        changed = commitAdded(filesMapCopy) || changed;
        changed = commitRemoved(filesMapCopy) || changed;
        if (changed) {
            createCommitAndSave(message, secondParentID, filesMapCopy);
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    private static boolean isStagingEmpty() {
        File[] additions = ADDED.listFiles();
        String removals = readContentsAsString(REMOVED);
        if (additions.length == 0 && removals.length() == 0) {
            return true;
        }
        return false;
    }

    private static Commit getCommit(String commitID) {
        File commitFile = join(COMMITS, commitID);
        return readObject(commitFile, Commit.class);
    }

    private static HashSet<String> getFilesSet(HashMap map1, HashMap map2, HashMap map3) {
        HashSet<String> filesSet = new HashSet<>();
        filesSet.addAll(map1.keySet());
        filesSet.addAll(map2.keySet());
        filesSet.addAll(map3.keySet());
        return filesSet;
    }


    private static String getBlobID(HashMap<String, String> filesMap, String fileName) {
        if (filesMap.containsKey(fileName)) {
            return filesMap.get(fileName);
        }
        return "empty";
    }

    private static void oneMod(String s, String c, String g, String file, Commit givenBranch) {
        boolean oneMod = c.equals(s) || g.equals(s);
        boolean oneUnmod =  !c.equals(s) || !g.equals(s);
        boolean qualify = oneMod && oneUnmod;
        if (qualify) {
            if (!g.equals(s)) {
                if (!g.equals("empty")) {
                    writeCommitContentsToFile(givenBranch, file);
                    add(file);
                } else {
                    remove(file);
                }
            }
        }
    }

    private static boolean twoMod(String s, String c, String g, String fileName) {
        boolean qualify = !c.equals(s) && !g.equals(s);
        if (qualify) {
            if (!c.equals(g)) {
                String currContents = "";
                String givenContents = "";
                if (!c.equals("empty")) {
                    currContents = readObject(join(BLOBS, c), FileBlob.class).getContents();
                }
                if (!g.equals("empty")) {
                    givenContents = readObject(join(BLOBS, g), FileBlob.class).getContents();
                }
                resolveMergeConflict(fileName, currContents, givenContents);
                add(fileName);
                return true;
            }
        }
        return false;
    }

    private static void resolveMergeConflict(String fileName, String currCont, String givenCont) {
        String start = "<<<<<<< HEAD\n";
        String mid = "=======\n";
        String end = ">>>>>>>\n";

        String replace = start + currCont + mid + givenCont + end;
        File source = join(CWD, fileName);
        writeContents(source, replace);
    }


    private static String getSplitCommitID(String currID, String givenID) {
        HashSet<String> ancestorSet1 = getAncestorSet(currID);
        HashSet<String> ancestorSet2 = getAncestorSet(givenID);
        ancestorSet1.retainAll(ancestorSet2);
        String deepestCommonAncestorID = getDeepestAncestorID(ancestorSet1);
        return deepestCommonAncestorID;
    }


    private static String getDeepestAncestorID(HashSet<String> ancestorSet) {
        Commit deepest = null;
        String id = null;
        for (String commitID: ancestorSet) {
            Commit c = getCommit(commitID);
            if (deepest == null) {
                deepest = c;
                id = commitID;
            } else if (c.getDepth() > deepest.getDepth()) {
                deepest = c;
                id = commitID;
            }
        }
        return id;
    }

    private static HashSet<String> getAncestorSet(String commitID) {
        HashSet<String> ancestorSet = new HashSet<>();
        ancestorSet.add(commitID);
        ancestorSet = ancestorHelper(commitID, ancestorSet);
        return ancestorSet;
    }

    private static HashSet<String> ancestorHelper(String commitID, HashSet<String> ancestorSet) {
        Commit c = getCommit(commitID);
        String firstParentID = c.getFirstParentCommitID();
        String secondParentID = c.getSecondParentCommitID();
        if (firstParentID == null && secondParentID == null) {
            return ancestorSet;
        }
        if (firstParentID != null) {
            ancestorSet.add(firstParentID);
            ancestorSet = ancestorHelper(firstParentID, ancestorSet);
        }
        if (secondParentID != null) {
            ancestorSet.add(secondParentID);
            ancestorSet = ancestorHelper(secondParentID, ancestorSet);
        }
        return ancestorSet;
    }


    public static void addRemote(String remoteName, String remoteDirPath) {
        File remoteFile = join(REMOTES, remoteName);
        if (remoteFile.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        } else {
            String cwdParent = CWD.getParent();
            remoteDirPath = cwdParent + remoteDirPath.substring(2);
            writeContents(remoteFile, remoteDirPath);
        }
    }

    public static void removeRemote(String remoteName) {
        File remoteFile = join(REMOTES, remoteName);
        if (!remoteFile.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        } else {
            remoteFile.delete();
        }
    }

    public static void push(String remoteName, String remoteBranchName) throws IOException {
        File remoteFile = join(REMOTES, remoteName);

        String remoteLocation = readContentsAsString(remoteFile);
        File remoteRepoDir = new File(remoteLocation);

        if (!remoteRepoDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File allRemoteBranches = join(remoteRepoDir, "BRANCHES");
        File remoteBranchFile = join(allRemoteBranches, remoteBranchName);
        File remoteHead = join(remoteRepoDir, "HEAD.txt");

        if (!remoteBranchFile.exists()) {
            writeContents(remoteBranchFile, readContents(remoteHead));
        }

        String remoteBranchID = readContentsAsString(remoteBranchFile);
        String currID = readContentsAsString(HEAD);
        Set<String> newCommits = new HashSet<>();
        writeContents(remoteBranchFile, currID);
        writeContents(remoteHead, currID);

        while (!currID.equals(remoteBranchID)) {
            newCommits.add(currID);
            Commit c = getCommit(currID);
            if (c.getFirstParentCommitID() == null) {
                System.out.println("Please pull down remote changes before pushing.");
                System.exit(0);
            }
            currID = c.getFirstParentCommitID();
        }

        for (String newCommit: newCommits) {
            File source = join(COMMITS, newCommit);
            File remoteCommits = join(remoteRepoDir, "COMMITS");
            File dest = join(remoteCommits, newCommit);
            Files.copy(source.toPath(), dest.toPath());
        }

    }

    public static void fetch(String remoteName, String remoteBranchName) throws IOException {
        File remoteFile = join(REMOTES, remoteName);

        String remoteLocation = readContentsAsString(remoteFile);
        File remoteRepoDir = new File(remoteLocation);

        if (!remoteRepoDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File allRemoteBranches = join(remoteRepoDir, "BRANCHES");
        File remoteBranchFile = join(allRemoteBranches, remoteBranchName);

        if (!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }


        String remoteCommitID = readContentsAsString(remoteBranchFile);
        File newCurrRepoBranch = join(BRANCHES, remoteName + "_" + remoteBranchName);
        writeContents(newCurrRepoBranch, remoteCommitID);

        File remoteCommits = join(remoteRepoDir, "COMMITS");
        File blobDir = join(remoteRepoDir, "BLOBS");
        while (remoteCommitID != null) {
            File source = join(remoteCommits, remoteCommitID);
            File dest = join(COMMITS, remoteCommitID);
            if (!dest.exists()) {
                Files.copy(source.toPath(), dest.toPath());
            }
            Commit c = getCommit(remoteCommitID);
            HashMap<String, String> fM = c.getFilesMap();
            for (String fileName: fM.keySet()) {
                String blobID = fM.get(fileName);
                File blobSource = join(blobDir, blobID);
                File blobDest = join(BLOBS, blobID);
                if (!blobDest.exists()) {
                    Files.copy(blobSource.toPath(), blobDest.toPath());
                }
            }
            remoteCommitID = c.getFirstParentCommitID();
        }

    }

    public static void pull(String remoteName, String remoteBranchName) throws IOException {
        fetch(remoteName, remoteBranchName);
        String otherBranch = remoteName + "_" + remoteBranchName;
        merge(otherBranch);
    }


}
