// see here for some examples http://androidtrainningcenter.blogspot.com/2014/02/android-ftp-client-tutorial-with.html

package com.google.appinventor.components.runtime;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@DesignerComponent(version = YaVersion.FTP_COMPONENT_VERSION,
        description = "Non-visible component that communicates with a FTP server to send, receive, list files",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/ftpmanager.png")
@SimpleObject
@UsesLibraries(libraries = "commons-net-3.4.jar")
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
public class FtpManager extends AndroidNonvisibleComponent implements Component {
    private static final String LOG_TAG = "FtpManager";
    private android.content.Context context;
    private android.app.Activity activity;
    private ComponentContainer container;

    private String serverURL="";
    private String serverUserId="";
    private String serverPassword="";
    private boolean isRepl = false;
    private FTPClient ftpClient;
    private String systemType="";

    /**
     * Creates a new FtpManager component.
     *
     * @param container the Form that this component is contained in.
     */
    public FtpManager(ComponentContainer container) {
        super(container.$form());

        if (form instanceof ReplForm) { // Note: form is defined in our superclass
            Log.d(LOG_TAG, "Dealing with ReplForm");
            isRepl = true;
        }

        this.context = container.$context();
        this.activity = container.$context();
        this.container = container;

    }


    /**
     * Returns the URL of the web FTP server.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Retrieves ftp server URL")
    public String ServerURL() {
        return serverURL;
    }

    /**
     * Specifies the URL of the  ftp server.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "ftp.example.com")
    @SimpleProperty(description = "Sets ftp server URL")
    public void ServerURL(String serverURL) {
        this.serverURL = serverURL.trim();
    }

    /**
     * Returns the user id of FTP server.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Retrieves ftp server user id")
    public String ServerUserId() {
        return serverUserId;
    }

    /**
     * Specifies the user id of FTP server.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "anonymous")
    @SimpleProperty(description = "Sets ftp server user id")
    public void ServerUserId(String serverUserId) {
        this.serverUserId = serverUserId.trim();
    }

    /**
     * Returns the user id of FTP server.
     */
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Retrieves ftp server password")
    public String ServerPassword() {
        return serverPassword;
    }

    /**
     * Specifies the user id of FTP server.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "anonymous")
    @SimpleProperty(description = "Sets ftp server password")
    public void ServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

//    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Connects to FTP server. You should have already set ServerURL, ServerUserId, ServerPassword. ")
    private boolean connectToFTPServer() {
        Log.d(LOG_TAG, String.format("Starting connectToFTPServer to %s for user %s", serverURL, serverUserId));

        boolean isConnected = false;

        try {
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(10 * 1000);  // 66.147.242.184
//            String serverURL="unityforus.com";
//            String serverUserId="unityfor";

            ftpClient.connect(InetAddress.getByName(serverURL));
            isConnected = ftpClient.login(serverUserId, serverPassword);
            Log.d(LOG_TAG, "was connected? " + isConnected);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
            } else {
                Log.d(LOG_TAG, "FTPReply was not success");
            }
            if (!isConnected) {
                FtpConnectionError(String.format("Unable to connect to serverURL %s, user serverUserId of %s and serverPassword of %s.",
                        serverURL, serverUserId, serverPassword));
            } else {
                // Setup encoding for special chars
                ftpClient.setControlEncoding("UTF-8");

                // Should I get the systemType to find out what OS I'm dealing with? This way I could
                // determine if I have to use forward or backslashes
                this.systemType = ftpClient.getSystemType();

            }
        } catch (Exception e) {
            FtpConnectionError(String.format("Unable to connect to serverURL %s, user serverUserId of %s and serverPassword of %s. Error is: %s",
                    serverURL, serverUserId, serverPassword, e.getLocalizedMessage()));
        }
        Log.d(LOG_TAG, "Connected to FTP server?" +isConnected);

        return isConnected;
    }



    @SimpleFunction(description = "Uploads local device file(s) to ftp server remotePath. " +
            "Wildcard * could be used to upload all e.g. /data/* will upload all files from /data." +
            " If remote directory doesn't exists, you must first create directory.")
    public void Upload(final String localFileName, final String remotePath) {
//        localFileName = "1.html";
//        localFileName = "/DCIM/Camera/20150405_163057.jpg";
//        localFileName = "/DCIM/Facebook/FB_IMG_1439085587864.jpg";
//        localFileName = "/data/foo.ks";

        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncUpload(localFileName, remotePath);
            }
        });
    }

    @SimpleFunction(description = "Downloads remoteFile from server remotePath to device localPath. " +
            "If wildcard * is used for remoteFile, then all files from remotePath will be downloaded into localPath." +
            " If localPath(s) doesn't exist, then it will create folder(s)")
    public void Download(final String remotePath, final String remoteFile, final String localPath) {
//        localPath = "/data";
//        remoteFile="_headerMain.js";
//        remoteFile="*";
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncDownload(remotePath, remoteFile, localPath);
            }
        });
    }

 /*   @SimpleFunction(description = "Reports if local or remote fullPath exists. The fullPath can be either a folder or file")
    public void Exists(final String fullPath, final boolean isRemote) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncExists(fullPath, isRemote);
            }
        });
    }
*/
    @SimpleFunction(description = "Reports list of files in CSV format. If isRemote is true, then will show remote files else " +
            "will show local device files")
    public void ListFiles(final String fullPath, final boolean isRemote) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncListFiles(fullPath, isRemote);
            }
        });
    }

    @SimpleFunction(description = "Creates a new directory under the specified path. " +
            "If isRemote is true, action will be on remote server and only one dir can be made at a time. " +
            "If isRemote is false, multiple subfolders can be created at same time. " +
            "If dir exists, no action will be taken.")
    public void MakeDir(final String fullPath, final String newDirName, final boolean isRemote) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                asyncMakeDir(fullPath, newDirName, isRemote);
            }
        });
    }

    // Note: Unlike asyncDownload, We don't use localPath, localFile args because if user picks an
    // image using ImagePicker, we only have image name and user doesn't need to pass-in path
    private void asyncUpload(String localFileName, String remotePath) {
        if (!connectToFTPServer()) return;

        if (!remotePath.endsWith("/")) {
            remotePath += "/";
        }

        localFileName = localFileName.trim();

        if (localFileName.length() > 1 && localFileName.endsWith("/")) {
            localFileName = localFileName.substring(0, localFileName.length() -1);
        }

        // Does user wants to read from sd card?
        // If starts with file:///someFile.txt, then change to /someFile.txt
        if (localFileName.startsWith("file:///")) localFileName= localFileName.substring(7);
        else if (localFileName.startsWith("//")) localFileName= localFileName.substring(1);

        String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

        // ImagePicker.AfterPicking Selection will return something like:
        //    /storage/emulated/0/Pictures/_app_inventor_image_picker/picked_image584271329.jpeg
        // We check to see if we are dealing with this situation. If so, we just get rid of /storage/emulated/0
        if (localFileName.startsWith(externalStoragePath)) {
            localFileName = localFileName.replace(externalStoragePath, "");
        }

        List<String> listOfFilesToUpload = new ArrayList<String>();
        if (localFileName.endsWith("/*")) {
            // Get rid of star
            localFileName = localFileName.substring(0, localFileName.length() - 1);
            File file = new File(getFullPath(localFileName) + localFileName);
            File[] files = file.listFiles();
            if (files == null) {
                AfterAction(false, "No files found in: " + localFileName, "Upload");
                return;
            }

            for (File aFile : files) {
                if (aFile.isDirectory() || aFile.isHidden()) {
                    continue;
                }
                listOfFilesToUpload.add(aFile.getAbsolutePath());
            }
        } else {
            listOfFilesToUpload.add(getFullPath(localFileName) + localFileName);
        }

        FileInputStream srcFileStream = null;
        String errors="";
        String transferred="";
        String notTransferred="";
        boolean success = false;
        try {
            success = ftpClient.changeWorkingDirectory(remotePath);
        } catch (IOException e) {
            success=false;
        }

        if (!success) {
            AfterAction(false, "Unable to switch to remotePath. Did you specify it correctly?" + remotePath, "Upload");
            return;
        }

        for (String aFile : listOfFilesToUpload) {
            Uri uri  = Uri.parse(aFile);
            try {
                srcFileStream = new FileInputStream(uri.getPath());

//                if (!success) {
//                    FtpConnectionError("Remote folder does not exist: " + remotePath);
//                    break;
//                }
                if (aFile.contains("/")) {
                    success = ftpClient.storeFile(aFile.substring(aFile.lastIndexOf("/") + 1), srcFileStream);
                    if (success) transferred += ","+ aFile.substring(aFile.lastIndexOf("/") + 1);
                    else notTransferred += ","+ aFile.substring(aFile.lastIndexOf("/") + 1);
                } else {
                    success = ftpClient.storeFile(aFile, srcFileStream);
                    if (success) transferred += ","+ aFile;
                    else notTransferred += ","+ aFile;
                }

            } catch (FileNotFoundException e) {
                errors +=",ERR_FileNotFound:" + aFile;
            } catch (IOException e) {
                errors +=",ERR_IO_ERR:" + aFile;
            }
        }

        if (srcFileStream != null) {
            try {
                srcFileStream.close();
            } catch (IOException e) {
                // no-op;
            }
        }

        if (notTransferred.length() > 0) notTransferred = notTransferred.substring(1);   // get rid of 1st comma
        if (errors.length() > 0) errors = errors.substring(1);   // get rid of 1st comma
        if (transferred.length() > 0) transferred = transferred.substring(1);   // get rid of 1st comma

        // For now, let's forget about this
       /* if (errors.length() > 0 || notTransferred.length()>0) {
            FtpTransferError("Files Not Transferred: " + errors + " " + notTransferred);
        }*/

        if (transferred.length() > 0) {
//            FileSent(String.format("files %s were transferred to remote path %s", transferred, remotePath));
            AfterAction(true, transferred, "Upload");
        }

    }

    private String getFullPath(String thePath) {
        if (true)   return Environment.getExternalStorageDirectory().getPath();

        if (thePath.startsWith("/")) {
            return Environment.getExternalStorageDirectory().getPath();
        }

        File dirPath = activity.getFilesDir();

        if (isRepl) {
            String path = Environment.getExternalStorageDirectory().getPath() + "/AppInventor/data/";
            dirPath = new File(path);
        }
        return dirPath.getPath();

       /* if (true)   return Environment.getExternalStorageDirectory().getPath();

        // Should I use below? If user doesn't add a slash at begining, possibly he
        //  wants to store files into private application path
        java.io.File dirPath = activity.getFilesDir();

        if (isRepl) {
            return Environment.getExternalStorageDirectory().getPath() + "/AppInventor/assets";
        } else {
            return Environment.getExternalStorageDirectory().getPath();
        }*/
    }

    private void asyncMakeDir(String fullPath, String newDirName, boolean isRemote) {
        Log.d(LOG_TAG, "starting the asyncCreateRemoteFolder method");

        fullPath = fullPath.trim();
        if (fullPath.equals("")) {
            AfterAction(false, "fullPath is missing. Please specify fullPath such as /public_html/ftpfolder (creates ftpFolder)" +
                    " or /data/sub-folder1/sub-folder2 (for local device)", "Download");
            return;
        }
        if (!fullPath.endsWith(File.separator)) {
            fullPath += File.separator;
        }

        newDirName = newDirName.trim();
        if (newDirName.equals("")) {
            AfterAction(false, "newDirName is missing. Specify newDirName without slashes", "FtpMakeDir" );
            return;
        }

        newDirName = newDirName.replace("/", "");
        newDirName = newDirName.replace("\\", "");

        // Are we dealing with local device?
        if (!isRemote) {
            String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
            // If directory doesn't exist, then create it
            newDirName = newDirName.startsWith(File.separator)?newDirName:File.separator+newDirName;
            fullPath = fullPath.startsWith(File.separator)?fullPath:File.separator+fullPath;

            File file = new File(externalStoragePath + fullPath + newDirName);
            //If directory doesn't exist, then create it
            if (!file.exists()) {
                if (file.mkdirs()) {
                    AfterAction(true, "Folder(s) created on local device: " + fullPath + newDirName, "FtpMakeDir");
                } else {
                    AfterAction(false, "Unable to create folder(s) on local device: " + fullPath + newDirName, "FtpMakeDir");
                }
            } else {
                AfterAction(true, "Folder(s) already existed on local device: " + fullPath + newDirName, "FtpMakeDir");
            }
            return;
        }

        // We are dealing with ftp server.
        // Start connecting to ftp for file transfer
        if (!connectToFTPServer()) return;

        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean success = ftpClient.changeWorkingDirectory(fullPath);
            if (!success) {
                AfterAction(false, "fullPath is invalid: " + fullPath, "FtpMakeDir");
                return;
            }

//            // Checks to see if new directory exists under mentioned remotePath. If so, then error
//            if (checkDirectoryExists(remotePath, newDirName) ) {
//                AfterAction(false, "Unable to newDirName because it already exits: " + newDirName, "FtpMakeDir");
//                return;
//            }

            success = ftpClient.makeDirectory(newDirName);
            if (success) {
                AfterAction(true, String.format("New directory %s was created here: %s", newDirName, fullPath), "FtpMakeDir");
            } else {
                AfterAction(false, String.format("Unable to create new directory %s at: %s. " +
                        "Directory may already exist", newDirName, fullPath), "FtpMakeDir");
            }
        } catch (Exception ex) {
            AfterAction(false, "FTP error occured when making new directory. Error: " + ex.getLocalizedMessage(), "FtpMakeDir");
        }
    }

    /**
     * Checks to see if newDir exists or not
     * @param currentFolder
     * @param newDir
     * @return
     * @throws IOException
     */
    private boolean checkDirectoryExists(final String currentFolder, final String newDir) throws IOException {
        boolean doesDirExists = ftpClient.changeWorkingDirectory(newDir);
        ftpClient.changeWorkingDirectory(currentFolder);
        return doesDirExists;
    }
    private void asyncDownload(String remotePath, String remoteFile, String localPath) {
        Log.d(LOG_TAG, "starting the Download method");

        remotePath = remotePath.trim();
        if (remotePath.equals("")) {
            AfterAction(false, "remotePath is missing. Please specify remote path suc as /public_html/ftpfolder", "Download");
            return;
        }

        if (!remotePath.endsWith("/")) {
            remotePath += "/";
        }

        remoteFile = remoteFile.trim();
        if (remoteFile.equals("")) {
            AfterAction(false, "Remote file name is missing. You can use * for all files", "Download" );
            return;
        }

        localPath = localPath.trim();
        if (localPath.equals("")) {
            AfterAction(false, "localPath is missing. Please specify a local path such as /data", "Download");
            return;
        }

        if (!localPath.endsWith(File.separator)) {
            localPath += File.separator;
        }

        if (!localPath.startsWith(File.separator)) {
            localPath = File.separator + localPath;
        }
        // Start connecting to ftp for file transfer
        if (!connectToFTPServer()) return;

        OutputStream outputStream=null;
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean success = ftpClient.changeWorkingDirectory(remotePath);
            if (!success) {
                AfterAction(false, "Remote path is invalid: " + remotePath, "Download");
                return;
            }

            String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
            if (remoteFile.equals("*")) {
                // User wants to download all files in the folder
                FTPFile[] ftpFiles = ftpClient.listFiles();
                if (ftpFiles.length == 0) {
                    String finalRemotePath = remotePath;
//                    FtpTransferError("Remote folder didn't include any files:" + finalRemotePath);
                    AfterAction(false, "Remote folder didn't include any files:" + finalRemotePath, "Download");
                    return;
                }
                String filesDownloaded="";
                String filesNotDownloaded="";

                // If directory doesn't exist, then create it
                File file = new File(externalStoragePath + localPath);
                //If directory doesn't exist, then create it
                if (!file.exists()) {
                    file.mkdirs();      // using mkdirS not mdir so that user can specify nested subfolders
                }

                for (FTPFile anFtpFile : ftpFiles) {
                    if (!anFtpFile.isFile() ) {
                        continue;
                    }
                    remoteFile = anFtpFile.getName();
//                    String filepath = AbsoluteFileName(remoteFile);
                    String filepath = externalStoragePath + localPath + remoteFile;
                    outputStream = new BufferedOutputStream(new FileOutputStream(filepath));

                    success = ftpClient.retrieveFile(remoteFile, outputStream);
                    if (success) {
                        filesDownloaded+="," + remoteFile;
                    } else {
                        filesNotDownloaded+="," +remoteFile;
                    }
//                    Log.d(LOG_TAG, "retrieved: " + remoteFile);
                }
                if (filesDownloaded.startsWith(",")) filesDownloaded = filesDownloaded.substring(1);
                if (filesNotDownloaded.startsWith(",")) filesNotDownloaded = filesNotDownloaded.substring(1);

                // For now, let's forget about this
                /*if (!filesNotDownloaded.equals("")) {
                    FtpTransferError(String.format("Remote files not received: %s. Remote path: %s", filesNotDownloaded, remotePath));
                }*/

                if (!filesDownloaded.equals("")) {
                    AfterAction(true, filesDownloaded, "Download");
                }

            } else {
                // User wants to download single file

                File file = new File(externalStoragePath + localPath);
                //If directory doesn't exist, then create it
                if (!file.exists()) {
                    file.mkdirs();      //create /dir and subdirs if needed
                }

                String filepath = externalStoragePath + localPath + remoteFile;
                outputStream = new BufferedOutputStream(new FileOutputStream(filepath));

                success = ftpClient.retrieveFile(remoteFile, outputStream);
                if (success) {
                    AfterAction(true, remoteFile, "Download");
                } else {
                    AfterAction(false, String.format("Unable to download remote file %s from remote path %s to local path %s", remoteFile, remotePath, filepath), "Download");
                }
//                Log.e(LOG_TAG, "retrieved: " + remoteFile);
            }

//            forceRefreshSDCard();
        } catch (Exception ex) {
            AfterAction(false, "Transfer error during downloading file. Error: " + ex.getLocalizedMessage(), "Download");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    // no-op
                }
            }
        }
    }

    /*private boolean asyncExists(String fullPath, boolean isRemote) {
        if (!isRemote) {
            String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
            // If directory doesn't exist, then create it
            fullPath = fullPath.startsWith(File.separator)?fullPath:File.separator+fullPath;

            java.io.File file = new java.io.File(externalStoragePath + fullPath );
            //If directory doesn't exist, then create it
            return  file.exists();
        }

        // User is dealing with ftp server
        if (!connectToFTPServer()) {
            AfterAction(false, "Unable to connect to FTP server " , "Exists");
            return false;
        }

        // Have connected to ftp server. ow check to see if folder exists

    }
    */
    private void asyncListFiles(String fullPath, boolean isRemote) {
        String fileList="";
        if (!isRemote) {
            Log.d(LOG_TAG, "Starting to list local files of: " + fullPath);
            if (fullPath.endsWith(File.separator)) {
                fullPath = fullPath.substring(0, fullPath.length() - 1);
            }

            fullPath = fullPath.startsWith(File.separator)?fullPath:File.separator+fullPath;
            File file = new File(getFullPath(fullPath) + fullPath);
            File[] files = file.listFiles();
            if (files == null) {
                AfterAction(false, "No files found at local path of: " + fullPath, "ListFiles");
                return;
            }

            for (File aFile : files) {
                if (aFile.isHidden()) {
                    continue;
                } else if (aFile.isDirectory()) {
                    fileList += ",<DIR>" + aFile.getName();    //<DIR> for directory
                } else {
                    fileList += "," + aFile.getName();
                }
            }
            if (fileList.startsWith(",")) {
                fileList = fileList.substring(1); // get rid of 1st comma;
                AfterAction(true, fileList, "ListFiles");
            } else {
                AfterAction(false, "No files found at local path of: " + fullPath, "ListFiles");
            }

            return;
        }

        // User wants to get listing of remote server files
        if (!connectToFTPServer()) return;
        Log.d(LOG_TAG, "Starting to list remote files of: " + fullPath);

        fullPath = fullPath.trim();
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }

        FTPFile[] ftpFiles;
        try {
            if (!ftpClient.changeWorkingDirectory(fullPath) ) {
                AfterAction(false, "Remote path is invalid: " + fullPath, "ListFiles");
                return;
            }

            ftpFiles = ftpClient.listFiles();
        } catch (IOException e) {
            AfterAction(false, String.format("Unable to get list of remote files from %s. Error is %s" + fullPath, e.getMessage()), "ListFiles");
            return;
        }
        if (ftpFiles.length == 0) {
            AfterAction(false, "Remote folder didn't include any files:" + fullPath, "ListFiles");
            return;
        }

        for (FTPFile anFtpFile : ftpFiles) {
            if (!anFtpFile.isFile()) {
                fileList += ",<DIR>"+anFtpFile.getName();       //D for directory
            } else {
                fileList += ","+anFtpFile.getName();
            }
        }

        if (fileList.startsWith(",")) {
            fileList=fileList.substring(1); // Get rid of 1st comma
            AfterAction(true, fileList, "ListFiles");
        } else {
            // shouldn't get here, but safety check
            AfterAction(false, "Remote folder didn't include any files:" + fullPath, "ListFiles");
        }

    }

//    @SimpleEvent(description = "Triggered after file(s) are sent to FTP server")
//    public void FileSent(final String message) {
//        ftpDisconnect();
//        activity.runOnUiThread(new Runnable() {
//            public void run() {
//                EventDispatcher.dispatchEvent(FtpTransfer.this, "FileSent", message);
//            }
//        });
//    }

    @SimpleEvent(description = "Triggered after an actions such as Download, Upload, MakeDir, ListFiles. " +
            "wasSuccess indicates status, and action indicates the command; e.g. Upload, Download, ListFiles, etc. " +
            "that triggered this event.")
    public void AfterAction(final boolean wasSuccess, final String message, final String action) {
        ftpDisconnect();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                EventDispatcher.dispatchEvent(FtpManager.this, "AfterAction", wasSuccess, message, action);
            }
        });
    }

    /**
     * Disconnects from ftp server
     */
    private void ftpDisconnect() {
        // logout

        if (ftpClient == null) {
            return;
        }
        try {
            ftpClient.logout();
            ftpClient.disconnect();
        } catch (IOException e) {
            // no-op
        }
    }
    /**
     * Indicates that the communication with the FTP server signaled an error
     *
     * @param message the error message
     */
    @SimpleEvent(description = "Triggered if there are any FTP connection errors")
    public void FtpConnectionError(final String message) {
        ftpDisconnect();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                EventDispatcher.dispatchEvent(FtpManager.this, "FtpConnectionError", message);
            }
        });
    }

    private void forceRefreshSDCard() {
        // force refresh
        container.$form().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" +  Environment.getExternalStorageDirectory())));
    }

}
