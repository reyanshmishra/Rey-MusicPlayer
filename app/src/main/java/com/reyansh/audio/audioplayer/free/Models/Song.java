package com.reyansh.audio.audioplayer.free.Models;

/**
 * Created by REYANSH on 8/10/2017.
 * Song Model Class.
 */

public class Song  implements Cloneable{

    public long _id;
    public String _title;
    public String _album;
    public long _albumId;
    public String _artist;
    public long _artistId;
    public String _path;
    public int _trackNumber;
    public long _duration;

    public Song(long _id, String _title, String _album, long _albumId, String _artist, long _artistId, String _path, int _trackNumber, long _duration) {
        this._id = _id;
        this._title = _title;
        this._album = _album;
        this._albumId = _albumId;
        this._artist = _artist;
        this._artistId = _artistId;
        this._path = _path;
        this._trackNumber = _trackNumber;
        this._duration = _duration;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
