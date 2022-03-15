package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Rajendra Goudar
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException{
        // TODO: what if args is empty?
        int numArgs = args.length;
        if (numArgs == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if (numArgs != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                } else {
                    Repository.initRepository();
                    break;
                }
            case "add":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                else {
                    String filename = args[1];
                    Repository.add(filename);
                    break;
                }

            case "commit":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String message = args[1];
                    Repository.commit(message);
                    break;
                }
            case "checkout":
                if (numArgs < 2 || numArgs > 4) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                }
                if (numArgs == 2) {
                    String branchName = args[1];
                    Repository.branchCheckout(branchName);
                    break;
                }
                if (numArgs == 3) {
                    String dashes = args[1];
                    String filename = args[2];
                    if (dashes.equals("--")) {
                        Repository.checkout(filename);
                        break;
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                }
                if (numArgs == 4) {
                    String commitID = args[1];
                    String dashes = args[2];
                    String filename = args[3];
                    if(dashes.equals("--")) {
                        Repository.checkout(commitID, filename);
                        break;
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                }
            case "log":
                if (numArgs != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    Repository.log();
                    break;
                }

            case "rm":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String removeFile = args[1];
                    Repository.remove(removeFile);
                    break;
                }
            case "global-log":
                if (numArgs != 1) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    Repository.globalLog();
                    break;
                }
            case "find":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String commitMessage = args[1];
                    Repository.find(commitMessage);
                    break;
                }
            case "status":
                if (numArgs != 1) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    Repository.status();
                    break;
                }
            case "branch":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String branchName = args[1];
                    Repository.branch(branchName);
                    break;
                }
            case "rm-branch":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String branchName = args[1];
                    Repository.removeBranch(branchName);
                    break;
                }
            case "reset":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String checkoutID = args[1];
                    Repository.reset(checkoutID);
                    break;
                }
            case "merge":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String branchName = args[1];
                    Repository.merge(branchName);
                    break;
                }

            case "add-remote":
                if (numArgs != 3) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String remoteName = args[1];
                    String remoteDir = args[2];
                    Repository.addRemote(remoteName, remoteDir);
                    break;
                }
            case "rm-remote":
                if (numArgs != 2) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String remoteName = args[1];
                    Repository.removeRemote(remoteName);
                    break;
                }
            case "push":
                if (numArgs != 3) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String remoteName = args[1];
                    String remoteBranchName = args[2];
                    Repository.push(remoteName, remoteBranchName);
                    break;
                }
            case "fetch":
                if (numArgs != 3) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String remoteName = args[1];
                    String remoteBranchName = args[2];
                    Repository.fetch(remoteName, remoteBranchName);
                    break;
                }
            case "pull":
                if (numArgs != 3) {
                    System.out.println("Incorrect operands");
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    System.exit(0);
                } else {
                    String remoteName = args[1];
                    String remoteBranchName = args[2];
                    Repository.pull(remoteName, remoteBranchName);
                    break;
                }
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}