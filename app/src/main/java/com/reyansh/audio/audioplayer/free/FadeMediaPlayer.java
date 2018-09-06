package com.reyansh.audio.audioplayer.free;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.PowerManager;

import com.reyansh.audio.audioplayer.free.Services.MusicService;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.Logger;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;

import java.io.IOException;

/**
 * Created by reyansh on 1/8/18.
 */

public class FadeMediaPlayer {

    private MediaPlayer mMediaPlayer1;
    private MediaPlayer mMediaPlayer2;
    private MusicService mMusicService;


    //Volume variables that handle the crossfade effect.

    private float mFadeOutVolume = 1.0f;
    private float mFadeInVolume = 0.0f;
    private Handler mHandler;
    private int mCurrentMediaPlayer = 1;

    public FadeMediaPlayer(MusicService musicService) {
        mMusicService = musicService;

        mMediaPlayer1 = new MediaPlayer();
        mMediaPlayer2 = new MediaPlayer();
        mHandler = new Handler();

        mMediaPlayer1.setWakeMode(mMusicService, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer2.setWakeMode(mMusicService, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            startSong();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startSong() throws IOException {

        if (PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_SONG) {
            mMediaPlayer1.setLooping(true);
            mMediaPlayer2.setLooping(true);
        } else {
            mMediaPlayer1.setLooping(false);
            mMediaPlayer2.setLooping(false);
        }

        mMediaPlayer1.setDataSource(mMusicService, mMusicService.getUri(mMusicService.getSongList().get(mMusicService.getCurrentSongIndex())._id));
        mMediaPlayer1.setOnPreparedListener(onPreparedListener);
        mMediaPlayer1.setOnErrorListener(onErrorListener);
        mMediaPlayer1.prepareAsync();

        mMediaPlayer2.setDataSource(mMusicService, mMusicService.getUri(mMusicService.getSongList().get(mMusicService.getCurrentSongIndex() + 1)._id));
        mMediaPlayer2.setOnPreparedListener(onPreparedListener);
        mMediaPlayer2.setOnErrorListener(onErrorListener);
        mMediaPlayer2.prepareAsync();


    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            mHandler.postDelayed(mCrossfadeRunnable, 100);
        }
    };


    /**
     * When MediaPlayer is done playing music.
     */

    MediaPlayer.OnCompletionListener mOnCompletionListener = mp -> {


    };

    public MediaPlayer.OnErrorListener onErrorListener = ((mp, what, extra) -> {
         /* This error listener might seem like it's not doing anything.
     * However, removing this will cause the mMediaPlayer1 object to go crazy
     * and skip around. The key here is to make this method return true. This
     * notifies the mMediaPlayer1 object that we've handled all errors and that
     * it shouldn't do anything else to try and remedy the situation.
     *
     * TL;DR: Don't touch this interface. Ever.
     */
        return true;
    });

    Runnable mCrossfadeRunnable = () -> {
        Logger.exp("AAAAAA    " + (getDuration() - getCurrentPosition()));
        if (getDuration() - getCurrentPosition() < 30) {

        } else {

        }
    };


    public int getCurrentPosition() {
        if (mCurrentMediaPlayer == 1) {
            return mMediaPlayer1.getCurrentPosition() / 1000;
        } else {
            return mMediaPlayer2.getCurrentPosition() / 1000;
        }
    }


    public int getDuration() {
        if (mCurrentMediaPlayer == 1) {
            return mMediaPlayer1.getDuration() / 1000;
        } else {
            return mMediaPlayer2.getDuration() / 1000;
        }
    }
}
