package com.reyansh.audio.audioplayer.free.Services;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;

/**
 * Created by reyansh on 10/18/17.
 */

public class MediaPlayerManager {


    private MediaPlayer mMediaPlayer1;
    private MediaPlayer mMediaPlayer2;


    public MediaPlayerManager(Context context) {
        mMediaPlayer1 = new MediaPlayer();
        mMediaPlayer2 = new MediaPlayer();

        mMediaPlayer1 = new MediaPlayer();
        mMediaPlayer1.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);


        mMediaPlayer2 = new MediaPlayer();
        mMediaPlayer2.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);



    }
}
