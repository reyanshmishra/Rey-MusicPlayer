package com.boom.music.player.Models;

import com.boom.music.player.Common;
import com.boom.music.player.R;

/**
 * Created by REYANSH on 8/17/2017.
 */

public class FileModel {
    public String fileName;
    public int fileType;
    public String fileSize;
    public String fileExtension;
    public String filePath;
    public String noOfItems;

    public String getfileName() {
        return fileName;
    }

    public void setfileName(String fileName) {
        this.fileName = fileName;
    }

    public int getfileType() {
        return fileType;
    }

    public void setfileType(int fileType) {
        this.fileType = fileType;
    }

    public String getfileSize() {
        return fileSize;
    }

    public void setfileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getfileExtension() {
        return fileExtension;
    }

    public void setfileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getfilePath() {
        return filePath;
    }

    public void setfilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getnoOfItems() {
        return noOfItems;
    }

    public void setnoTracksOfItems(int noOfItems) {
        if (noOfItems == 1) {
            this.noOfItems = noOfItems +" "+ Common.getInstance().getString(R.string.track);
        } else {
            this.noOfItems = noOfItems + " "+Common.getInstance().getString(R.string.tracks);
        }
    }


}
