package com.example;


import org.omg.CORBA.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TagEditor {


    /**
     * If the file belogns to the external storage we won'e be able to edit it,
     * so the work around is to copy the file to internal storage edit it and cut and
     * paste it back to its place this the the solution I can think of which I took
     * from shuttle music Player here. If it belogs to internal storage edit it there only.
     */
    private void moveFile(File file, File dir) throws IOException {
        dir = Environment.getExternalStorageDirectory().getPath();
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    public static boolean cutAndPasteToExternalStorage(String filePath) {

    }


}
