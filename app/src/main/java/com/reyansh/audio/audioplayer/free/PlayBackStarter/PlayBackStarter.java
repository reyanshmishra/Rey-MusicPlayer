package com.reyansh.audio.audioplayer.free.PlayBackStarter;

import android.content.Context;
import android.content.Intent;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Equalizer.EqualizerActivity;
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.reyansh.audio.audioplayer.free.Services.MusicService;
import com.reyansh.audio.audioplayer.free.NowPlaying.NowPlayingActivity;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;

import java.util.ArrayList;


/**
 * Created by REYANSH on 4/21/2017.
 */

public class PlayBackStarter implements MusicService.PrepareServiceListener {

    private Context mContext;
    private Common mApp;
    private ArrayList<Song> mSongs;
    private Song mSong;

    private int mPos;
    private int WHICH_CASE = 0;

    public PlayBackStarter(Context context) {
        mContext = context;
        mApp = (Common) mContext.getApplicationContext();
    }


    @Override
    public void onServiceRunning(MusicService musicService) {
        mApp = (Common) mContext.getApplicationContext();
        mApp.getService().setPrepareServiceListener(this);

        switch (WHICH_CASE) {
            case Constants.START_EQUALIZER:
                /*if (!mApp.getPreferencesUtility().isSupportsEqualizer()) {
                    Toast.makeText(mContext, R.string.sorry_your_phone_does_not_support_equalizer, Toast.LENGTH_SHORT).show();
                } else {*/
                mContext.startActivity(new Intent(mContext, EqualizerActivity.class));
//                }
                break;
            case Constants.ADD_ONE_SONG_TO_QUEUE:
                mApp.getService().addOneSongToQueue(mSong);
                break;
            case Constants.ADD_ONE_SONG_TO_PLAY_NEXT:
                mApp.getService().addOneSongToPlayNext(mSong);
                break;
            case Constants.ADD_SONG_TO_QUEUE:
                mApp.getService().addSongsToQueue(mSongs);
                break;
            case Constants.SHUFFLE_UP:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_ON);
                mApp.getService().setSongList(mSongs);
                mApp.getService().setShuffledOne();
                mApp.getService().setSelectedSong(0);
                break;
            case Constants.PLAY_PAUSE_SONG:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
                mApp.getService().setSelectedSong(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0));
                break;
            case Constants.PLAY_PAUSE_SONG_FROM_BOTTOM_BAR:
                mApp.getService().setSelectedSong(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0));
                break;
            case Constants.PLAY_SONGS:
                mApp.getService().setSongList(mSongs);
                mApp.getService().setSelectedSong(mPos);
                break;
            case Constants.LAUNCH_NOW_PLAYING:
                if (mApp.getService().getSongList().size() > 0) {
                    Intent intent = new Intent(mContext, NowPlayingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } else {

                }
                break;
            case Constants.ADD_SONGS_TO_PLAY_NEXT:
                mApp.getService().addSongsToPlayNext(mSongs);
                break;
            case Constants.NEXT_SONG:
                mApp.getService().nextSong();
                break;
            case Constants.PREVIOUS_SONG:
                mApp.getService().previousSong();
                break;
            case Constants.PAUSE_SONG:
                mApp.getService().playPauseSong();
                break;
            case Constants.PLAY_SONG:
                mApp.getService().playPauseSong();
                break;
            default:
                break;
        }
    }

    public void playSongs(ArrayList<Song> songs, int position) {
        mSongs = songs;
        mPos = position;
        PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
        WHICH_CASE = Constants.PLAY_SONGS;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().setSongList(mSongs);
            mApp.getService().setSelectedSong(mPos);
        }
    }

    public void playPauseSongs() {
        WHICH_CASE = Constants.PLAY_PAUSE_SONG;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().playPauseSong();
        }
    }

    public void playSongs() {
        WHICH_CASE = Constants.PLAY_SONG;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().startPlaying();
        }
    }

    public void shuffleUp(ArrayList<Song> songsList) {
        WHICH_CASE = Constants.SHUFFLE_UP;
        mSongs = songsList;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().setSongList(mSongs);
            mApp.getService().setShuffledOne();
            mApp.getService().setSelectedSong(0);
            PreferencesHelper.getInstance().put(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_ON);
        }
    }

    private void startService() {
        Intent intent = new Intent(mContext, MusicService.class);
        mContext.startService(intent);
    }

    public void addToQueue(ArrayList<Song> songList) {
        mSongs = songList;
        WHICH_CASE = Constants.ADD_SONG_TO_QUEUE;

        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().addSongsToQueue(mSongs);
        }
    }

    public void addToPlayNext(ArrayList<Song> songList) {
        mSongs = songList;
        WHICH_CASE = Constants.ADD_SONGS_TO_PLAY_NEXT;

        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().addSongsToPlayNext(mSongs);
        }
    }


    public void addOneSongToQueue(Song song) {
        mSong = song;
        WHICH_CASE = Constants.ADD_ONE_SONG_TO_QUEUE;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().addOneSongToQueue(song);
        }
    }

    public void addOneSongToPlayNext(Song song) {
        mSong = song;
        WHICH_CASE = Constants.ADD_ONE_SONG_TO_PLAY_NEXT;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().addOneSongToPlayNext(song);
        }
    }

    public void startEqualizer() {
        WHICH_CASE = Constants.START_EQUALIZER;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            /*if (!mApp.getPreferencesUtility().isSupportsEqualizer()) {
                Toast.makeText(mContext, R.string.sorry_your_phone_does_not_support_equalizer, Toast.LENGTH_SHORT).show();
            } else {
                mContext.startActivity(new Intent(mContext, EqualizerActivity.class));
            }*/
        }
    }

    public void launchNowPlaying(ArrayList<Song> songsList) {
        mSongs = songsList;
        WHICH_CASE = Constants.LAUNCH_NOW_PLAYING;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            if (mApp.getService().getSongList().size() > 0) {
                Intent intent = new Intent(mContext, NowPlayingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {

            }
        }
    }

    public void nextSong() {
        WHICH_CASE = Constants.NEXT_SONG;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().nextSong();
        }
    }

    public void previousSong() {
        WHICH_CASE = Constants.PREVIOUS_SONG;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().previousSong();
        }
    }

    public void pauseSong() {
        if (!mApp.isServiceRunning()) {
            mApp.getService().stopPlaying();
        }
    }

    public void playPauseFromBottomBar() {
        WHICH_CASE = Constants.PLAY_PAUSE_SONG_FROM_BOTTOM_BAR;
        if (!mApp.isServiceRunning()) {
            startService();
        } else {
            mApp.getService().playPauseSong();
        }
    }
}
