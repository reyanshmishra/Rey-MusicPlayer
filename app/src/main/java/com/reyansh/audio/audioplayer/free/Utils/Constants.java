package com.reyansh.audio.audioplayer.free.Utils;

import android.provider.MediaStore;

/**
 * Created by REYANSH on 4/21/2017.
 */

public class Constants {

    /**
     * Created by REYANSH on 4/21/2017.
     */

    public static String EXTERNAL_CONTENTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();


    public static final int EDIT_TAGS = 3565;
    public static final int REQUEST_PERMISSIONS = 33;
    public static final int START_EQUALIZER = 225;
    public static final int ADD_ONE_SONG_TO_QUEUE = 226;
    public static final int ADD_ONE_SONG_TO_PLAY_NEXT = 227;
    public static final int ADD_SONG_TO_QUEUE = 228;
    public static final int SHUFFLE_UP = 229;
    public static final int PLAY_PAUSE_SONG = 230;
    public static final int PLAY_SONGS = 231;
    public static final int LAUNCH_NOW_PLAYING = 232;
    public static final int ADD_SONGS_TO_PLAY_NEXT = 233;

    public static final int PREVIOUS_SONG = 234;
    public static final int NEXT_SONG = 235;
    public static final int PLAY_SONG = 236;
    public static final int PAUSE_SONG = 237;
    public static final int PLAY_PAUSE_SONG_FROM_BOTTOM_BAR = 238;
    public static final int PICK_FROM_GALLERY = 239;

    public static final String HEADER_TITLE = "HEADER_TITLE";
    public static final String HEADER_SUB_TITLE = "HEADER_SUB_TITLE";
    public static final String FROM_WHERE = "FROM_WHERE";
    public static final String SELECTION_VALUE = "SELECTION_VALUE";


    //NowPlaying launched from notification

    public static String defaultArtUrl = "http://www.flat-e.com/flate5/wp-content/uploads/cover-960x857.jpg";


    /**
     * BroadcastReceivers flags
     */

    public static final String MEDIA_INTENT = "com.android.music.metachanged";

    public static final String ACTION_PLAY_PAUSE = "com.reyansh.audio.audioplayer.free.action.PLAY_PAUSE";
    public static final String ACTION_STOP = "com.reyansh.audio.audioplayer.free.action.STOP";
    public static final String ACTION_NEXT = "com.reyansh.audio.audioplayer.free.action.NEXT";
    public static final String ACTION_PREVIOUS = "com.reyansh.audio.audioplayer.free.action.PREVIOUS";
    public static final String ACTION_PAUSE = "com.reyansh.audio.audioplayer.free.action.PAUSE";

    public static final String ACTION_UPDATE_NOW_PLAYING_UI = "com.reyansh.audio.audioplayer.free.action.UPDATE_NOW_PLAYING_UI";


    //NowPlaying launched from notification
    public static final String FROM_NOTIFICATION = "LAUNCHED_FROM_NOTIFICATION";


    //Repeat mode constants.
    public static final int REPEAT_OFF = 0;
    public static final int REPEAT_PLAYLIST = 1;
    public static final int REPEAT_SONG = 2;
    public static final int A_B_REPEAT = 3;

    public static final int SHUFFLE_OFF = 0;
    public static final int SHUFFLE_ON = 1;

    public static final String JUST_UPDATE_UI = "JUST_UPDATE_UI";

    public static final String DESCENDING = " DESC";
    public static final String ASCENDING = " ASC";
    public static final String ARTIST = "ARTIST";
    public static final String ENTITY_SONG = "song";

    public static final String COVER_PATH = "COVER_PATH";

    public static final String LAUNCHED_FROM_NOTIFICATION = "LAUNCHED_FROM_NOTIFICATION";

    public static final String FROM_ALBUMS_NOTIFICATION = "LAUNCHED_FROM_ALBUMS_NOTIFICATION";

}

