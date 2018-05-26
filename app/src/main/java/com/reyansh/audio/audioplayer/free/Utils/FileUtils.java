package com.reyansh.audio.audioplayer.free.Utils;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Dialogs.PermissionToEditSdCardDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by REYANSH on 8/3/2017.
 */

public class FileUtils {


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static DocumentFile getDocumentFile(final File file) {
        String baseFolder = getExtSdCardFolder(file);
        String relativePath = null;

        if (baseFolder == null) {
            return null;
        }

        try {
            String fullPath = file.getCanonicalPath();
            relativePath = fullPath.substring(baseFolder.length() + 1);
        } catch (IOException e) {
            Logger.log(e.getMessage());
            return null;
        }
        Uri treeUri = Common.getInstance().getContentResolver().getPersistedUriPermissions().get(0).getUri();

        if (treeUri == null) {
            return null;
        }

        // start with root of SD card and then parse through document tree.
        DocumentFile document = DocumentFile.fromTreeUri(Common.getInstance(), treeUri);

        String[] parts = relativePath.split("\\/");

        for (String part : parts) {
            DocumentFile nextDocument = document.findFile(part);
            if (nextDocument != null) {
                document = nextDocument;
            }
        }

        return document;
    }

    public static String getExtSdCardFolder(File file) {
        String[] extSdPaths = getExtSdCardPaths();
        try {
            for (int i = 0; i < extSdPaths.length; i++) {
                if (file.getCanonicalPath().startsWith(extSdPaths[i])) {
                    return extSdPaths[i];
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : Common.getInstance().getExternalFilesDirs("external")) {

            if (file != null && !file.equals(Common.getInstance().getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            if (fileOrDirectory.listFiles() != null)
                for (File child : fileOrDirectory.listFiles()) {
                    return deleteRecursive(child);
                }

        if (MusicUtils.isKitkat() || !MusicUtils.isFromSdCard(fileOrDirectory.getAbsolutePath())) {
            if (fileOrDirectory.delete()) {
                return MusicUtils.deleteViaContentProvider(fileOrDirectory.getAbsolutePath());
            }

        } else {
            DocumentFile documentFile = getDocumentFile(fileOrDirectory);
            if (documentFile.delete()) {
                return MusicUtils.deleteViaContentProvider(fileOrDirectory.getAbsolutePath());
            }
        }
        return false;
    }

    public static String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = Common.getInstance().getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void copyFile(File sourceFile, FileOutputStream outputStream) throws IOException {

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = outputStream.getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {

        FileChannel destination = null;
        FileChannel source = null;
        ParcelFileDescriptor pfd = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();

            if (MusicUtils.isFromSdCard(destFile.getAbsolutePath())) {
                DocumentFile documentFile = FileUtils.getDocumentFile(destFile);

                pfd = Common.getInstance().getContentResolver().openFileDescriptor(documentFile.getUri(), "w");
                FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
                destination = outputStream.getChannel();
            } else {
                destination = new FileOutputStream(destFile).getChannel();
            }


            if (!destFile.getParentFile().exists())
                destFile.getParentFile().mkdirs();

            if (!destFile.exists()) {
                destFile.createNewFile();
            }


            destination.transferFrom(source, 0, source.size());

        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("MESSAGE -" + e.getMessage() + "CAUSE " + e.getCause());
        } finally {
            if (pfd != null) {
                pfd.close();
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void cutFile(File sourceFile, File destFile) throws IOException {

        FileChannel destination = null;
        FileChannel source = null;
        ParcelFileDescriptor pfd = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();

            if (MusicUtils.isFromSdCard(destFile.getAbsolutePath())) {
                DocumentFile documentFile = FileUtils.getDocumentFile(destFile);
                pfd = Common.getInstance().getContentResolver().openFileDescriptor(documentFile.getUri(), "w");
                FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
                destination = outputStream.getChannel();
            } else {
                destination = new FileOutputStream(destFile).getChannel();
            }


            if (!destFile.getParentFile().exists())
                destFile.getParentFile().mkdirs();

            if (!destFile.exists()) {
                destFile.createNewFile();
            }


            destination.transferFrom(source, 0, source.size());

        } catch (Exception e) {
            e.printStackTrace();
            Logger.log("MESSAGE -" + e.getMessage() + "CAUSE " + e.getCause());
        } finally {
            deleteRecursive(sourceFile);
            if (pfd != null) {
                pfd.close();
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void rename(File file, String fileName) {

        if (MusicUtils.isFromSdCard(file.getAbsolutePath())) {
            DocumentFile documentFile = getDocumentFile(file);
            documentFile.renameTo(fileName);
        } else {
            File parentFile = file.getParentFile();

            if (!parentFile.exists())
                parentFile.mkdir();

            File file1 = new File(parentFile, fileName);
            file.renameTo(file1);
            file.delete();
        }

    }

    public static String getFileExtension(String fileName) {
        String fileNameArray[] = fileName.split("\\.");
        String extension = fileNameArray[fileNameArray.length - 1];
        return extension;
    }

    public static ArrayList<Uri> getListFiles(File parentDir) {
        ArrayList<Uri> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    inFiles.add(Uri.fromFile(file));
                }
            }
        }
        return inFiles;
    }

    public static String getRealFilePath(String filePath) {
        if (filePath.equals("/storage/emulated/0") ||
                filePath.equals("/storage/emulated/0/") ||
                filePath.equals("/storage/emulated/legacy") ||
                filePath.equals("/storage/emulated/legacy/") ||
                filePath.equals("/storage/sdcard0") ||
                filePath.equals("/storage/sdcard0/") ||
                filePath.equals("/sdcard") ||
                filePath.equals("/sdcard/") ||
                filePath.equals("/mnt/sdcard") ||
                filePath.equals("/mnt/sdcard/")) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return filePath;
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            throw new NullPointerException("File can't be null");
        }
        if (MusicUtils.isFromSdCard(file.getAbsolutePath())) {
            DocumentFile documentFile = getDocumentFile(file);
            if (documentFile.isDirectory()) {
                deleteDirectory(documentFile);
            } else {
                Logger.log("Deleted File Name  " + file.getName());
                return documentFile.delete();
            }
        } else {
            return file.delete();
        }
        return false;
    }

    private static boolean deleteDirectory(DocumentFile documentFile) {
        DocumentFile files[] = documentFile.listFiles();
        for (DocumentFile file : files) {
            Logger.log("File Name :-  " + file.getName());
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                return file.delete();
            }
        }
        return false;
    }

    public static int getNoOfFile(File directory) {
        return org.apache.commons.io.FileUtils.listFiles(directory, new String[]{"mp3", "m4a", "oog", "wav"}, true).size();
    }

    private boolean cutFile(Fragment fragment, File file) {
        if (!MusicUtils.isKitkat() && MusicUtils.isFromSdCard(file.getAbsolutePath()) && !MusicUtils.hasPermission()) {
            PermissionToEditSdCardDialog takePermissionDialog = new PermissionToEditSdCardDialog(fragment);
            takePermissionDialog.show(fragment.getActivity().getSupportFragmentManager(), "PERMISSION_DIALOG");
        } else {

        }
        return false;
    }

}
