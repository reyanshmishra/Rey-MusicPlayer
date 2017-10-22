package com.boom.music.player.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;

import com.boom.music.player.Common;
import com.boom.music.player.Models.Song;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


/**
 * Created by REYANSH on 6/19/2017.
 */

public class SongDataHelper {


    private SongDataHelper mSongHelper;
    private Common mApp;
    private int mIndex;

    //Song parameters.
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private String mAlbumArtist;


    private long mDuration;
    private String mFilePath;
    private String mGenre;
    private long mId;
    private String mAlbumArtPath;
    private String mSource;
    private String mLocalCopyPath;
    private long mSavedPosition;
    private Bitmap mAlbumArt;
    private int mColor;


    /**
     * Moves the specified cursor to the specified index and populates this
     * helper object with new song data.
     *
     * @param context Context used to get a new Common object.
     * @param index   The index of the song.
     */

    public void populateSongData(Context context, ArrayList<Song> songs, int index) {

        mSongHelper = this;
        mApp = (Common) context.getApplicationContext();
        mIndex = index;

        if (songs == null && mApp.isServiceRunning()) {
            this.setId(mApp.getService().getSongList().get(index)._id);
            this.setTitle(mApp.getService().getSongList().get(index)._title);
            this.setAlbum(mApp.getService().getSongList().get(index)._album);
            this.setArtist(mApp.getService().getSongList().get(index)._artist);
            this.setDuration(mApp.getService().getSongList().get(index)._duration);
            this.setFilePath(mApp.getService().getSongList().get(index)._path);
            this.setAlbumArtPath(String.valueOf(MusicUtils.getAlbumArtUri(mApp.getService().getSongList().get(index)._albumId)));

            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(getAlbumArtPath());
            setAlbumArt(bitmap);
            try {
                setColor(Palette.from(bitmap).generate().getDominantColor(Color.parseColor("#FFFFFF")));
            } catch (Exception e) {
                setColor(Color.parseColor("#FFFFFF"));
            }
        } else {
            this.setId(songs.get(index)._id);
            this.setTitle(songs.get(index)._title);
            this.setAlbum(songs.get(index)._album);
            this.setArtist(songs.get(index)._artist);
            this.setDuration(songs.get(index)._duration);
            this.setFilePath(songs.get(index)._path);
            this.setAlbumArtPath(String.valueOf(MusicUtils.getAlbumArtUri(songs.get(index)._albumId)));
        }
    }


    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public String getAlbumArtist() {
        return mAlbumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        mAlbumArtist = albumArtist;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    public String getGenre() {
        return mGenre;
    }

    public void setGenre(String genre) {
        mGenre = genre;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getAlbumArtPath() {
        return mAlbumArtPath;
    }

    public void setAlbumArtPath(String albumArtPath) {
        mAlbumArtPath = albumArtPath;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public String getLocalCopyPath() {
        return mLocalCopyPath;
    }

    public void setLocalCopyPath(String localCopyPath) {
        mLocalCopyPath = localCopyPath;
    }

    public long getSavedPosition() {
        return mSavedPosition;
    }

    public void setSavedPosition(long savedPosition) {
        mSavedPosition = savedPosition;
    }

    public Bitmap getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        mAlbumArt = albumArt;
    }


    public void setColor(int color) {
        mColor = color;
    }


    public int getColor() {
        return mColor;
    }

}
