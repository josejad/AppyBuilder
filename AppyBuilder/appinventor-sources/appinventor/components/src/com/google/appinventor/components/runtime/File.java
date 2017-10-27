// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2018 AppyBuilder.com, All Rights Reserved - Info@AppyBuilder.com
// https://www.gnu.org/licenses/gpl-3.0.en.html

// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.res.AssetManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A Component for working with files and directories on the device.
 *
 */
@DesignerComponent(version = YaVersion.FILE_COMPONENT_VERSION,
    description = "Non-visible component for storing and retrieving files. Use this component to " +
    "write or read files on your device. The default behaviour is to write files to the " +
    "private data directory associated with your App. The Companion is special cased to write " +
    "files to /sdcard/AppInventor/data to facilitate debugging. " +
    "If the file path starts with a slash (/), then the file is created relative to /sdcard. " +
    "For example writing a file to /myFile.txt will write the file in /sdcard/myFile.txt.",
    category = ComponentCategory.STORAGE,
    nonVisible = true,
    iconName = "images/file.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE, android.permission.READ_EXTERNAL_STORAGE")
@UsesLibraries(libraries = "commons-io-2.0.1.jar")
public class File extends AndroidNonvisibleComponent implements Component {
  public static final String NO_ASSETS = "No_Assets";
  private final Activity activity;
  private boolean isRepl = false;
  private final int BUFFER_LENGTH = 4096;
  private static final String LOG_TAG = "FileComponent";
  private AssetManager assetManager = null;

  /**
   * Creates a new File component.
   * @param container the Form that this component is contained in.
   */
  public File(ComponentContainer container) {
    super(container.$form());
    if (form instanceof ReplForm) { // Note: form is defined in our superclass
      isRepl = true;
    }
    activity = (Activity) container.$context();
    assetManager = activity.getAssets();

  }

  /**
   * Stores the text to a specified file on the phone.
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Saves text to a file. If the filename " +
      "begins with a slash (/) the file is written to the sdcard. For example writing to " +
      "/myFile.txt will write the file to /sdcard/myFile.txt. If the filename does not start " +
      "with a slash, it will be written in the programs private data directory where it will " +
      "not be accessible to other programs on the phone. There is a special exception for the " +
      "AI Companion where these files are written to /sdcard/AppInventor/data to facilitate " +
      "debugging. Note that this block will overwrite a file if it already exists." +
      "\n\nIf you want to add content to a file use the append block.")
  public void SaveFile(String text, String fileName) {
    if (fileName.startsWith("/")) {
      FileUtil.checkExternalStorageWriteable(); // Only check if writing to sdcard
    }
    Write(fileName, text, false);
  }

  /**
   * Appends text to a specified file on the phone.
   * Calls the Write function to write to the file asynchronously to prevent
   * the UI from hanging when there is a large write.
   *
   * @param text the text to be stored
   * @param fileName the file to which the text will be stored
   */
  @SimpleFunction(description = "Appends text to the end of a file storage, creating the file if it does not exist. " +
      "See the help text under SaveFile for information about where files are written.")
  public void AppendToFile(String text, String fileName) {
    if (fileName.startsWith("/")) {
      FileUtil.checkExternalStorageWriteable(); // Only check if writing to sdcard
    }
    Write(fileName, text, true);
  }

  /**
   * Retrieve the text stored in a specified file.
   *
   * @param fileName the file from which the text is read
   * @throws FileNotFoundException if the file cannot be found
   * @throws IOException if the text cannot be read from the file
   */
  @SimpleFunction(description = "Reads text from a file in storage. " +
      "Prefix the filename with / to read from a specific file on the SD card. " +
      "for instance /myFile.txt will read the file /sdcard/myFile.txt. To read " +
      "assets packaged with an application (also works for the Companion) start " +
      "the filename with // (two slashes). If a filename does not start with a " +
      "slash, it will be read from the applications private storage (for packaged " +
      "apps) and from /sdcard/AppInventor/data for the Companion.")
  public void ReadFrom(final String fileName) {
    try {
      InputStream inputStream;
      if (fileName.startsWith("//")) {
        if (isRepl) {
          inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() +
              "/AppInventor/assets/" + fileName);
        } else {
          inputStream = form.getAssets().open(fileName.substring(2));
        }
      } else {
        String filepath = AbsoluteFileName(fileName);
        Log.d(LOG_TAG, "filepath = " + filepath);
        inputStream = new FileInputStream(filepath);
      }

      final InputStream asyncInputStream = inputStream;
      AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            AsyncRead(asyncInputStream, fileName);
          }
        });
    } catch (FileNotFoundException e) {
      Log.e(LOG_TAG, "FileNotFoundException", e);
      form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
          ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
    } catch (IOException e) {
      Log.e(LOG_TAG, "IOException", e);
      form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
          ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
    }
  }


  /**
   * Delete the specified file.
   *
   * @param fileName the file to be deleted
   */
  @SimpleFunction(description = "Deletes a file from storage. " +
      "Prefix the filename with / to delete a specific file in the SD card, for instance /myFile.txt. " +
      "will delete the file /sdcard/myFile.txt. If the file does not begin with a /, then the file " +
      "located in the programs private storage will be deleted. Starting the file with // is an error " +
      "because assets files cannot be deleted. This block will trigger AfterAction")
  public void Delete(String fileName) {
    if (fileName.startsWith("//")) {
      form.dispatchErrorOccurredEvent(File.this, "DeleteFile",
          ErrorMessages.ERROR_CANNOT_DELETE_ASSET, fileName);
      return;
    }

    String fromFullPath = fileName;
    String action="Delete";
    fromFullPath = fromFullPath.trim();

    if (fromFullPath.length() > 1 && fromFullPath.endsWith("/")) {
      fromFullPath = fromFullPath.substring(0, fromFullPath.length() -1);
    }

    // Does user wants to read from sd card?
    // If starts with file:///someFile.txt, then change to /someFile.txt
    if (fromFullPath.startsWith("file:///")) fromFullPath= fromFullPath.substring(7);
    else if (fromFullPath.startsWith("//")) fromFullPath= fromFullPath.substring(1);

    String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

    // ImagePicker.AfterPicking Selection will return something like:
    //    /storage/emulated/0/Pictures/_app_inventor_image_picker/picked_image584271329.jpeg
    // We check to see if we are dealing with this situation. If so, we just get rid of /storage/emulated/0
    if (fromFullPath.startsWith(externalStoragePath)) {
      fromFullPath = fromFullPath.replace(externalStoragePath, "");
    }

    fromFullPath = externalStoragePath +fromFullPath;

    java.io.File file = new java.io.File(fromFullPath);
    if (!file.exists()) {
      AfterAction(false, "Source file doesn't exists: " + fromFullPath, action);
      return;
    }

    Boolean isDeleted = file.delete();
    AfterAction(isDeleted, fromFullPath, action);
  }

  @SimpleFunction(description = "Unzips source zip file into destination folder")
  public void Unzip(final String fromFullPath, final String toDestFolder) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        asyncUnzip(fromFullPath, toDestFolder);
      }
    });
  }

  @SimpleFunction(description = "Zips source files in fromDir into toDir with the given zipFileName. " +
          "Using matching, you can use wildcard (*) to select certain files. If left blank, all files " +
          "will be selected. If toDir is left empty, then zipping will be done in same folder as fromDir")
  public void Zip(final String fromDir, final String matching, final String toDir, final String zipFileName) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        asyncZip(fromDir, matching, toDir, zipFileName);
      }
    });
  }



  @SimpleFunction(description = "Reports list of files in CSV format")
  public void ListFiles(final String fullPath) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        asyncListFiles(fullPath);
      }
    });
  }

  @SimpleFunction(description = "Tests whether the file or directory denoted by this fullPath exists. " +
          "You should use full path; e.g. /data/myFile.txt")
  public boolean Exists(String fullPath) {
    if (fullPath.endsWith(java.io.File.separator)) {
      fullPath = fullPath.substring(0, fullPath.length() - 1);
    }

    fullPath = fullPath.startsWith(java.io.File.separator) ? fullPath : java.io.File.separator + fullPath;
    try {
      java.io.File file = new java.io.File(Environment.getExternalStorageDirectory().getPath() + fullPath);

      return file.exists();
    } catch (Exception e) {
      Log.d(LOG_TAG, String.format("Unable to check if file %s exists. Error is: %s ", fullPath, e.getMessage()));
      return false;
    }
  }

  @SimpleFunction(description = "Moves source file to destination (source will be deleted). " +
          "If destination toFullPath directories don't exist, then any/all subdirectories will be created.")
  public void MoveFile(String fromFullPath, String toFullPath, boolean shouldOverwrite) {
    String action = "MoveFile";
    transferFile(action, fromFullPath, toFullPath, shouldOverwrite);
  }


  @SimpleFunction(description = "Copies the contents of the specified source file to the specified destination file. " +
          "The directory holding the destination file is created if it does not exist. " +
          "If the destination file exists, then this method will overwrite it IF it shouldOverwrite is true")
  public void CopyFile(String fromFullPath, String toFullPath, boolean shouldOverwrite) {
    String action = "CopyFile";
    transferFile(action, fromFullPath, toFullPath, shouldOverwrite);
  }

  private void transferFile(String action, String fromFullPath, String toFullPath, boolean shouldOverwrite) {
    fromFullPath = fromFullPath.trim();

    if (fromFullPath.length() > 1 && fromFullPath.endsWith("/")) {
      fromFullPath = fromFullPath.substring(0, fromFullPath.length() -1);
    }

    // Does user wants to read from sd card?
    // If starts with file:///someFile.txt, then change to /someFile.txt
    if (fromFullPath.startsWith("file:///")) fromFullPath= fromFullPath.substring(7);
    else if (fromFullPath.startsWith("//")) fromFullPath= fromFullPath.substring(1);

    String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

    // ImagePicker.AfterPicking Selection will return something like:
    //    /storage/emulated/0/Pictures/_app_inventor_image_picker/picked_image584271329.jpeg
    // We check to see if we are dealing with this situation. If so, we just get rid of /storage/emulated/0
    if (fromFullPath.startsWith(externalStoragePath)) {
      fromFullPath = fromFullPath.replace(externalStoragePath, "");
    }

    fromFullPath = externalStoragePath +fromFullPath;

    java.io.File file = new java.io.File(fromFullPath);
    if (!file.exists()) {
      AfterAction(false, "Source file doesn't exists: " + fromFullPath, action);
      return;
    }

    if (toFullPath.endsWith(java.io.File.separator)) {
      toFullPath = toFullPath.substring(0, toFullPath.length() - 1);
    }

    toFullPath = toFullPath.startsWith(java.io.File.separator) ? toFullPath : java.io.File.separator + toFullPath;
    toFullPath = externalStoragePath +toFullPath;

    // Now check for the destination path that user has mentioned
    file = new java.io.File(toFullPath);

    //Are we dealing with directory?? Do I need below?? Test it out
    if (file.isDirectory()) {
      AfterAction(false, "The toFullPath doesn't include fileName " + toFullPath, action);
      return;
    }

    if (!shouldOverwrite && file.exists()) {
      AfterAction(false, "shouldOverwrite is false but destination file exists: " + toFullPath, action);
      return;
    }

    try {
      if (action.equalsIgnoreCase("MoveFile")) {
        // This check isn't needed for FileUtils.copyFile because it will automatically overwrite
        if (shouldOverwrite && file.exists()) {
          file.delete();
        }
        FileUtils.moveFile(new java.io.File(fromFullPath), new java.io.File(toFullPath));
        AfterAction(true, String.format(action + " from %s to %s was success", fromFullPath, toFullPath), action);
      } else if (action.equalsIgnoreCase("CopyFile")) {
        FileUtils.copyFile(new java.io.File(fromFullPath), new java.io.File(toFullPath));
        AfterAction(true, String.format(action + " from %s to %s was success", fromFullPath, toFullPath), action);
      }

    } catch (IOException e) {
      AfterAction(false, "Exception happened during " + action + e.getMessage(), action);
    }
  }

  @SimpleFunction(description = "Returns the size of the specified file or directory")
  public long FileSize(String fullPath) {
    String action = "FileSize";

    fullPath = fullPath.trim();

    if (fullPath.length() > 1 && fullPath.endsWith("/")) {
      fullPath = fullPath.substring(0, fullPath.length() -1);
    }

    // Does user wants to read from sd card?
    // If starts with file:///someFile.txt, then change to /someFile.txt
    if (fullPath.startsWith("file:///")) fullPath= fullPath.substring(7);
    else if (fullPath.startsWith("//")) fullPath= fullPath.substring(1);

    String externalStoragePath = Environment.getExternalStorageDirectory().getPath();

    // ImagePicker.AfterPicking Selection will return something like:
    //    /storage/emulated/0/Pictures/_app_inventor_image_picker/picked_image584271329.jpeg
    // We check to see if we are dealing with this situation. If so, we just get rid of /storage/emulated/0
    if (fullPath.startsWith(externalStoragePath)) {
      fullPath = fullPath.replace(externalStoragePath, "");
    }

    fullPath = externalStoragePath +fullPath;

    java.io.File file = new java.io.File(fullPath);
    if (!file.exists()) {
      AfterAction(false, "Source file doesn't exists: " + fullPath, action);
      return 0;
    }

    return file.length();
  }

  private void asyncZip(String fromDir, String matching, String toDir, String zipFileName) {
    Log.d(LOG_TAG, "Starting to zip: " + fromDir);

    matching = matching.trim();
    matching = matching.equals("")?"*":matching;

    zipFileName = zipFileName.trim();
    if (zipFileName.equals("")) {
      AfterAction(false, "Please specify a valid zip file name for zipFileName", "Zip");
      return;
    }

    // If toDir is empty, set it to same as fromDir; i.e. zip to same location
    toDir = toDir.trim();
    toDir = toDir.equals("")?fromDir:toDir;

    if (fromDir.endsWith(java.io.File.separator)) {
      fromDir = fromDir.substring(0, fromDir.length() - 1);
    }
    fromDir = fromDir.startsWith(java.io.File.separator) ? fromDir : java.io.File.separator + fromDir;

    toDir = toDir.trim();

    if (!toDir.endsWith(java.io.File.separator)) {
      toDir = toDir + java.io.File.separator;
    }
    toDir = toDir.startsWith(java.io.File.separator) ? toDir : java.io.File.separator + toDir;

    java.io.File inDir = new java.io.File(Environment.getExternalStorageDirectory().getPath() + fromDir);
    java.io.File outDir = new java.io.File(Environment.getExternalStorageDirectory().getPath() + toDir);

    // inDir validation
    if (!inDir.exists()) {
      AfterAction(false, "Specified fromDir doesn't exist", "Zip");
      return;
    }

    if (!inDir.isDirectory()) {
      AfterAction(false, "Specified fromDir isn't a directory.", "Zip");
      return;
    }

    // outDir validation
    if (outDir.exists() && !outDir.isDirectory()) {
      AfterAction(false, "Specified toDir isn't a directory.", "Zip");
      return;
    }

    zipFileName = outDir + java.io.File.separator + zipFileName;
    java.io.File zipFile = new java.io.File(zipFileName);
    if (zipFile.exists() && !zipFile.isFile()) {
      AfterAction(false, "Specified zipFileName seems to exist and is not a file.", "Zip");
      return;
    }

    // If needed, create all the folders
    if (!outDir.exists()) {
      Log.d(LOG_TAG, "Creating necessary folders: " + Environment.getExternalStorageDirectory().getPath() + outDir);
      outDir.mkdirs();
    }

    // If the zipfile already exists, then delete it
    if (zipFile.exists()) {
      zipFile.delete();
    }


    // Now get all the files matching the matchPattern
//        FileFilter filter = new org.apache.commons.io.filefilter. RegexFileFilter(matching);
    FileFilter fileFilter = new WildcardFileFilter(matching);
    java.io.File[] files = inDir.listFiles(fileFilter);

//        java.io.File[] files = inDir.listFiles();

    String result = zip(files, zipFileName);
    if (result == null) {
      AfterAction(true, String.format("Successfully zipped files from %s to %s matching %s", fromDir, toDir, matching), "Unzip");
    } else {
      AfterAction(false, String.format("Error zipping files. Error is: %s", result), "Zip");
    }
  }


  // http://kiritbhayani.blogspot.com/2014/04/how-to-programmatically-zip-and-unzip.html
  private String zip(java.io.File[] _files, String zipFileName) {
    try {
      BufferedInputStream origin = null;
      FileOutputStream dest = new FileOutputStream(zipFileName);
      ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
      byte data[] = new byte[BUFFER_LENGTH];
      for (int i = 0; i < _files.length; i++) {
        Log.d(LOG_TAG, "Adding: " + _files[i].getAbsolutePath());
        FileInputStream fi = new FileInputStream(_files[i]);
        origin = new BufferedInputStream(fi, BUFFER_LENGTH);

        ZipEntry entry = new ZipEntry(_files[i].getAbsolutePath().substring(_files[i].getAbsolutePath().lastIndexOf(java.io.File.separator) + 1));
        out.putNextEntry(entry);
        int count;

        while ((count = origin.read(data, 0, BUFFER_LENGTH)) != -1) {
          out.write(data, 0, count);
        }
        origin.close();
      }

      out.close();
      return null;
    } catch (Exception e) {
      return e.getMessage();
    }
  }


  private void asyncUnzip(String zipFileFullPath, String toDestFolder) {
    Log.d(LOG_TAG, "Starting to unzip: " + zipFileFullPath);
    if (!Exists(zipFileFullPath)) {
      AfterAction(false, "Source zip file doesn't exists: " + zipFileFullPath, "Unzip");
      return;
    }

    if (zipFileFullPath.endsWith(java.io.File.separator)) {
      zipFileFullPath = zipFileFullPath.substring(0, zipFileFullPath.length() - 1);
    }
    zipFileFullPath = zipFileFullPath.startsWith(java.io.File.separator) ? zipFileFullPath : java.io.File.separator + zipFileFullPath;

    toDestFolder = toDestFolder.trim();

    if (!toDestFolder.endsWith(java.io.File.separator)) {
//      toDestFolder = toDestFolder.substring(0, toDestFolder.length() - 1);
      toDestFolder = toDestFolder + java.io.File.separator;
    }
    toDestFolder = toDestFolder.startsWith(java.io.File.separator) ? toDestFolder : java.io.File.separator + toDestFolder;


    try {
      java.io.File outFolder = new java.io.File(Environment.getExternalStorageDirectory().getPath() + toDestFolder);
      if (outFolder.exists() && !outFolder.isDirectory()) {
        AfterAction(false, "Specified toDestFolder is NOT a directory", "Unzip");
        return;
      }

      // If needed, create all the folders
      if (!outFolder.exists()) {
        Log.d(LOG_TAG, "Creating necessary folders: " + Environment.getExternalStorageDirectory().getPath() + toDestFolder);
        outFolder.mkdirs();
      }

      FileInputStream fin = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + zipFileFullPath);
      ZipInputStream zipIn = new ZipInputStream(fin);
      ZipEntry entry = zipIn.getNextEntry();

      String unzippedFiles = "";
      while (entry != null) {
        String filePath = Environment.getExternalStorageDirectory().getPath() + toDestFolder + java.io.File.separator + entry.getName();
        Log.d(LOG_TAG, "Unzipping " + filePath);
        if (!entry.isDirectory()) {
          // if the entry is a file, extracts it
          extractFile(zipIn, filePath);
          unzippedFiles += "," + entry.getName();
        } else {
          // if the entry is a directory, make the directory
          java.io.File dir = new java.io.File(filePath);
          dir.mkdir();
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
      zipIn.close();
      if (!unzippedFiles.equals("")) {
        unzippedFiles = unzippedFiles.substring(1); // get rid of 1st comma
        AfterAction(true, unzippedFiles, "Unzip");
      }
    } catch (Exception e) {
      AfterAction(false, String.format("Error unzipping source file of %s to dest folder of %s. Error: %s", zipFileFullPath, toDestFolder, e.getLocalizedMessage()), "Unzip");
    }

  }

  /**
   * Extracts a zip entry (file entry)
   * @param zipIn
   * @param filePath
   * @throws IOException
   */
  private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[BUFFER_LENGTH];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  private void _dirChecker(String destFolder, String dir) {
    java.io.File f = new java.io.File(destFolder + dir);

    if(!f.isDirectory()) {
      f.mkdirs();
    }
  }

  private void asyncListFiles(String fullPath) {
    String fileList = "";

      Log.d(LOG_TAG, "Starting to list local files of: " + fullPath);
      if (fullPath.endsWith(java.io.File.separator)) {
        fullPath = fullPath.substring(0, fullPath.length() - 1);
      }

      fullPath = fullPath.startsWith(java.io.File.separator) ? fullPath : java.io.File.separator + fullPath;
      java.io.File file = new java.io.File(Environment.getExternalStorageDirectory().getPath() + fullPath);
      java.io.File[] files = file.listFiles();
      if (files == null) {
        AfterAction(false,  "No files found at local path of: " + fullPath, "ListFiles");
        return;
      }

      for (java.io.File aFile : files) {
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
  }

  @SimpleEvent(description = "Triggered after an actions such as Download, Upload, MakeDir, ListFiles. " +
          "wasSuccess indicates status, and action indicates the command; e.g. Upload, Download, ListFiles, etc. " +
          "that triggered this event.")
  public void AfterAction(final boolean wasSuccess, final String message, final String action) {
    activity.runOnUiThread(new Runnable() {
      public void run() {
        EventDispatcher.dispatchEvent(File.this, "AfterAction", wasSuccess, message, action);
      }
    });
  }

  /**
   * Writes to the specified file.
   * @param filename the file to write
   * @param text to write to the file
   * @param append determines whether text should be appended to the file,
   * or overwrite the file
   */
  private void Write(final String filename, final String text, final boolean append) {
    if (filename.startsWith("//")) {
      if (append) {
        form.dispatchErrorOccurredEvent(File.this, "AppendTo",
            ErrorMessages.ERROR_CANNOT_WRITE_ASSET, filename);
      } else {
        form.dispatchErrorOccurredEvent(File.this, "SaveFile",
            ErrorMessages.ERROR_CANNOT_WRITE_ASSET, filename);
      }
      return;
    }
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        final String filepath = AbsoluteFileName(filename);
        final java.io.File file = new java.io.File(filepath);

        if(!file.exists()){
          try {
            file.createNewFile();
          } catch (IOException e) {
            if (append) {
              form.dispatchErrorOccurredEvent(File.this, "AppendTo",
                  ErrorMessages.ERROR_CANNOT_CREATE_FILE, filepath);
            } else {
              form.dispatchErrorOccurredEvent(File.this, "SaveFile",
                  ErrorMessages.ERROR_CANNOT_CREATE_FILE, filepath);
            }
            return;
          }
        }
        try {
          FileOutputStream fileWriter = new FileOutputStream(file, append);
          OutputStreamWriter out = new OutputStreamWriter(fileWriter);
          out.write(text);
          out.flush();
          out.close();
          fileWriter.close();

          activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              AfterFileSaved(filename);
            }
          });
        } catch (IOException e) {
          if (append) {
            form.dispatchErrorOccurredEvent(File.this, "AppendTo",
                ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE, filepath);
          } else {
            form.dispatchErrorOccurredEvent(File.this, "SaveFile",
                ErrorMessages.ERROR_CANNOT_WRITE_TO_FILE, filepath);
          }
        }
      }
    });
  }

  /**
   * Replace Windows-style CRLF with Unix LF as String. This allows
   * end-user to treat Windows text files same as Unix or Mac. In
   * future, allowing user to choose to normalize new lines might also
   * be nice - in case someone really wants to detect Windows-style
   * line separators, or save a file which was read (and expect no
   * changes in size or checksum).
   * @param string to convert
   */

  private String normalizeNewLines(String s) {
    return s.replaceAll("\r\n", "\n");
  }


  /**
   * Asynchronously reads from the given file. Calls the main event thread
   * when the function has completed reading from the file.
   * @param filepath the file to read
   * @throws FileNotFoundException
   * @throws IOException when the system cannot read the file
   */
  private void AsyncRead(InputStream fileInput, final String fileName) {
    InputStreamReader input = null;
    try {
      input = new InputStreamReader(fileInput);
      StringWriter output = new StringWriter();
      char [] buffer = new char[BUFFER_LENGTH];
      int offset = 0;
      int length = 0;
      while ((length = input.read(buffer, offset, BUFFER_LENGTH)) > 0) {
        output.write(buffer, 0, length);
      }

      // Now that we have the file as a String,
      // normalize any line separators to avoid compatibility between Windows and Mac
      // text files. Users can expect \n to mean a line separator regardless of how
      // file was created. Currently only doing this for files opened locally - not files we pull
      // from other places like URLs.

      final String text = normalizeNewLines(output.toString());

      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          GotText(text);
        }
      });
    } catch (FileNotFoundException e) {
      Log.e(LOG_TAG, "FileNotFoundException", e);
      form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
          ErrorMessages.ERROR_CANNOT_FIND_FILE, fileName);
    } catch (IOException e) {
      Log.e(LOG_TAG, "IOException", e);
      form.dispatchErrorOccurredEvent(File.this, "ReadFrom",
          ErrorMessages.ERROR_CANNOT_READ_FILE, fileName);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          // do nothing...
        }
      }
    }
  }

  /**
   * Event indicating that a request has finished.
   *
   * @param text read from the file
   */
  @SimpleEvent (description = "Event indicating that the contents from the file have been read.")
  public void GotText(String text) {
    // invoke the application's "GotText" event handler.
    EventDispatcher.dispatchEvent(this, "GotText", text);
  }

  /**
   * Event indicating that a request has finished.
   *
   * @param text write to the file
   */
  @SimpleEvent (description = "Event indicating that the contents of the file have been written.")
  public void AfterFileSaved(String fileName) {
    // invoke the application's "AfterFileSaved" event handler.
    EventDispatcher.dispatchEvent(this, "AfterFileSaved", fileName);
  }

  /**
   * Returns absolute file path.
   *
   * @param filename the file used to construct the file path
   */
  private String AbsoluteFileName(String filename) {
    if (filename.startsWith("/")) {
      return Environment.getExternalStorageDirectory().getPath() + filename;
    } else {
      java.io.File dirPath = activity.getFilesDir();
      if (isRepl) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/AppInventor/data/";
        dirPath = new java.io.File(path);
        if (!dirPath.exists()) {
          dirPath.mkdirs();           // Make sure it exists
        }
      }
      return dirPath.getPath() + "/" + filename;
    }
  }

}
