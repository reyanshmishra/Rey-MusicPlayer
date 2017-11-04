package com.boom.music.player.MusicService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.boom.music.player.AppWidget.SmallWidgetProvider;
import com.boom.music.player.BroadcastReceivers.HeadsetNotificationBroadcast;
import com.boom.music.player.BroadcastReceivers.HeadsetPlugBroadcastReceiver;
import com.boom.music.player.Common;
import com.boom.music.player.Equalizer.EqualizerHelper;
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.AudioManagerHelper;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.SongDataHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicService extends Service {


    /**
     * Notification Id
     */
    public static final int NOTIFICATION_ID = 1056;


    private boolean mMediaPlayerPrepared = false;
    private Uri mSongUri;

    /**
     * On ServicePrepared listener.
     */
    private PrepareServiceListener mPrepareServiceListener;


    /**
     * Song position.
     */
    private int mSongPos = 0;
    private Bundle mBundle;


    private Intent mMediaIntent;
    private Intent mPlayPauseIntent;


    //Current context.
    private Context mContext;


    private MediaPlayer mMediaPlayer;


    private PowerManager.WakeLock mWakeLock;

    //Equalizer to manage the equalizer.
    private EqualizerHelper mEqualizerHelper;


    /**
     * AudioHelpers
     */
    private AudioManager mAudioManager;
    private AudioManagerHelper mAudioManagerHelper;


    private Handler mHandler;
    private Common mApp;
    private boolean notification = true;
    private NotificationCompat.Builder mNotificationBuilder;


    /**
     * First time playing flag
     */

    private boolean mPlayingForFirstTime = true;


    //Broadcast receiver to catch the headphone buttons clicks.
    private HeadsetNotificationBroadcast mHeadsetNotificationBroadcast;

    //Broadcast receiver to catch the plugin and out of the headset.
    private HeadsetPlugBroadcastReceiver mHeadsetPlugReceiver;
    private Service mService;

    //A-B Repeat variables.
    private int mRepeatSongRangePointA = 0;
    private int mRepeatSongRangePointB = 0;
    private Song mSong;

    /**
     * List of song which will be played in here.
     */

    private ArrayList<Song> mListSongs;

    /**
     * List of shuffled songs
     */
    private ArrayList<Song> mShuffledSongList;

    /**
     * List of original songs used in shuffling the songs and vise versa.
     */
    private ArrayList<Song> mOriginalSongList;
    private MediaSessionCompat mMediaSession;

    //Song data helper class which hands the data related to the song.
    private SongDataHelper mSongDataHelper;


    //When audio can be ducked eg. a notification comes the volume will go down  a lil bit and come to original volume.
    private Runnable duckUpVolumeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mAudioManagerHelper.getCurrentVolume() < mAudioManagerHelper.getTargetVolume()) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        (mAudioManagerHelper.getCurrentVolume() + mAudioManagerHelper.getStepUpIncrement()), 0);

                mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                mHandler.postDelayed(this, 50);
            }
        }

    };


    private Runnable duckDownVolumeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mAudioManagerHelper.getCurrentVolume() > mAudioManagerHelper.getTargetVolume()) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (mAudioManagerHelper.getCurrentVolume() - mAudioManagerHelper.getStepDownIncrement()), 0);
                mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                mHandler.postDelayed(this, 50);
            }
        }
    };

    //Broadcast to for other apps like MusixMatch of the current position and data of the song.
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            sendMediaIntentData();
            mHandler.postDelayed(this, 500);
        }
    };


    /**
     * Logic to when one song is done with playing what to do next when shuffle mode is on when repeat is on
     * different states.
     */

    MediaPlayer.OnCompletionListener mOnCompletionListener = mp -> {

        PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);

        if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_OFF) {
            if (mSongPos < mListSongs.size() - 1) {
                mSongPos++;
                startSong();
            } else {
                stopSelf();
            }
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_PLAYLIST) {
            if (mSongPos < mListSongs.size() - 1) {
                mSongPos++;
                startSong();
            } else {
                mSongPos = 0;
                startSong();
            }
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_SONG) {
            startSong();
        }
    };


    //Audio focus gain and loss when call or messages comes up.
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                try {
                    stopPlaying();
                    mAudioManagerHelper.setHasAudioFocus(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mAudioManagerHelper.setAudioDucked(true);
                mAudioManagerHelper.setTargetVolume(5);
                mAudioManagerHelper.setStepDownIncrement(1);
                mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                mAudioManagerHelper.setOriginalVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                mHandler.post(duckDownVolumeRunnable);

            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (mAudioManagerHelper.isAudioDucked()) {
                    mAudioManagerHelper.setTargetVolume(mAudioManagerHelper.getOriginalVolume());
                    mAudioManagerHelper.setStepUpIncrement(1);
                    mAudioManagerHelper.setCurrentVolume(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
                    mHandler.post(duckUpVolumeRunnable);
                    mAudioManagerHelper.setAudioDucked(false);
                } else {
                    mAudioManagerHelper.setHasAudioFocus(true);
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (mMediaPlayer != null) {
                    stopPlaying();
                }
                mAudioManagerHelper.setHasAudioFocus(false);
            }

        }

    };

    //Runnable to check if the media player is prepared and it then play.
    private Runnable startMediaPlayerIfPrepared = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayerPrepared) {
                startPlaying();
            } else {
                mHandler.postDelayed(this, 100);
            }
        }
    };


    /**
     * Called when the MediaPlayer is prepared to play the music.
     */

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mMediaPlayerPrepared = true;
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.seekTo(PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION));
            if (mPlayingForFirstTime) {
                mPlayingForFirstTime = false;
            }
            applyMediaPlayerEQ(getSongDataHelper().getId());
            startPlaying();
            Intent intent = new Intent(Constants.ACTION_UPDATE_NOW_PLAYING_UI);
            intent.putExtra(Constants.JUST_UPDATE_UI, true);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }
    };

    /**
     * Retrieves the EQ values for mMediaPlayer's current song and
     * applies them to the EQ engine.
     *
     * @param songId The id of the song that mMediaPlayer is current handling.
     */
    private void applyMediaPlayerEQ(long songId) {

        if (mEqualizerHelper == null)
            return;

        short fiftyHertzBand = mEqualizerHelper.getEqualizer().getBand(50000);
        short oneThirtyHertzBand = mEqualizerHelper.getEqualizer().getBand(130000);
        short threeTwentyHertzBand = mEqualizerHelper.getEqualizer().getBand(320000);
        short eightHundredHertzBand = mEqualizerHelper.getEqualizer().getBand(800000);
        short twoKilohertzBand = mEqualizerHelper.getEqualizer().getBand(2000000);
        short fiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(5000000);
        short twelvePointFiveKilohertzBand = mEqualizerHelper.getEqualizer().getBand(9000000);

        //Get the equalizer/audioFX settings for this specific song.
        int[] eqValues = mApp.getDBAccessHelper().getEQValues();

        //50Hz Band.
        if (eqValues[0] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) 0);
        } else if (eqValues[0] < 16) {

            if (eqValues[0] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) (-(16 - eqValues[0]) * 100));
            }

        } else if (eqValues[0] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiftyHertzBand, (short) ((eqValues[0] - 16) * 100));
        }

        //130Hz Band.
        if (eqValues[1] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) 0);
        } else if (eqValues[1] < 16) {

            if (eqValues[1] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) (-(16 - eqValues[1]) * 100));
            }

        } else if (eqValues[1] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(oneThirtyHertzBand, (short) ((eqValues[1] - 16) * 100));
        }

        //320Hz Band.
        if (eqValues[2] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) 0);
        } else if (eqValues[2] < 16) {

            if (eqValues[2] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) (-(16 - eqValues[2]) * 100));
            }

        } else if (eqValues[2] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(threeTwentyHertzBand, (short) ((eqValues[2] - 16) * 100));
        }

        //800Hz Band.
        if (eqValues[3] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) 0);
        } else if (eqValues[3] < 16) {

            if (eqValues[3] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) (-(16 - eqValues[3]) * 100));
            }

        } else if (eqValues[3] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(eightHundredHertzBand, (short) ((eqValues[3] - 16) * 100));
        }

        //2kHz Band.
        if (eqValues[4] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) 0);
        } else if (eqValues[4] < 16) {

            if (eqValues[4] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) (-(16 - eqValues[4]) * 100));
            }

        } else if (eqValues[4] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twoKilohertzBand, (short) ((eqValues[4] - 16) * 100));
        }

        //5kHz Band.
        if (eqValues[5] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) 0);
        } else if (eqValues[5] < 16) {

            if (eqValues[5] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) (-(16 - eqValues[5]) * 100));
            }

        } else if (eqValues[5] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(fiveKilohertzBand, (short) ((eqValues[5] - 16) * 100));
        }

        //12.5kHz Band.
        if (eqValues[6] == 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) 0);
        } else if (eqValues[6] < 16) {

            if (eqValues[6] == 0) {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) -1500);
            } else {
                mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) (-(16 - eqValues[6]) * 100));
            }

        } else if (eqValues[6] > 16) {
            mEqualizerHelper.getEqualizer().setBandLevel(twelvePointFiveKilohertzBand, (short) ((eqValues[6] - 16) * 100));
        }

        //Set the audioFX values.
        mEqualizerHelper.getVirtualizer().setStrength((short) eqValues[7]);
        mEqualizerHelper.getBassBoost().setStrength((short) eqValues[8]);
        float volume = ((float) eqValues[10]) / 100f;

        mMediaPlayer.setVolume(volume, volume);

        if (eqValues[9] == 0) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_NONE);
        } else if (eqValues[9] == 1) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEHALL);
        } else if (eqValues[9] == 2) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_LARGEROOM);
        } else if (eqValues[9] == 3) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMHALL);
        } else if (eqValues[9] == 4) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_MEDIUMROOM);
        } else if (eqValues[9] == 5) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_SMALLROOM);
        } else if (eqValues[9] == 6) {
            mEqualizerHelper.getReverb().setPreset(PresetReverb.PRESET_PLATE);
        }

    }

    /**
     * Called repetitively to check for A-B repeat markers.
     */
    private Runnable checkABRepeatRange = new Runnable() {

        @Override
        public void run() {
            try {
                if (getMediaPlayer().isPlaying()) {
                    if (getMediaPlayer().getCurrentPosition() >= (mRepeatSongRangePointB)) {
                        getMediaPlayer().seekTo(mRepeatSongRangePointA);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.A_B_REPEAT) == Constants.A_B_REPEAT) {
                mHandler.postDelayed(checkABRepeatRange, 100);
            }

        }

    };


    /**
     * Pretty obvious overriden method.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mService = this;

        mApp = (Common) getApplicationContext();
        mApp.setIsServiceRunning(true);

        mListSongs = new ArrayList<>();
        mListSongs.addAll(mApp.getDBAccessHelper().getQueue());

        mShuffledSongList = new ArrayList<>(mListSongs.size());
        mOriginalSongList = new ArrayList<>(mListSongs.size());

        //Set the previously played position of the song.
        mSongPos = PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);

        //Create two clones of the list on for normal play other for shuffle.

        for (Song song : mListSongs) {
            try {
                mOriginalSongList.add((Song) song.clone());
                mShuffledSongList.add((Song) song.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                Logger.log(e.getMessage());
            }
        }
        //Check if shuffle is on add shuffle version of the current queue.
        if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_OFF) == Constants.SHUFFLE_ON) {
            setShuffledOne();
        }


        mHandler = new Handler();

        /**
         *Play pause intent to display the correct UI throughout the entire app.
         */
        mPlayPauseIntent = new Intent(Constants.ACTION_PLAY_PAUSE);

        /**
         *Take the wakeup lock to stop CPU from sleeping cause we are DJing here.
         */

        mWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, getClass().getName());
        mWakeLock.setReferenceCounted(false);

        /**
         *Headset Connect and disconnect receiver
         */
        registerHeadsetPlugReceiver();

        /**
         *Init the leader of the app "MediaPlayer"
         */


        initPlayer();

        /**
         *This is equalizer its pain in a** to manage.
         */

        initAudioFX();

        mBundle = new Bundle();

        mMediaIntent = new Intent();
        mMediaIntent.setAction(Constants.MEDIA_INTENT);

        mApp.setService(this);

        /**
         *Headset receivers
         */

        mHeadsetNotificationBroadcast = new HeadsetNotificationBroadcast();
        registerReceiver(mHeadsetNotificationBroadcast, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));


        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManagerHelper = new AudioManagerHelper();

        //MediaSession it took a lot of time to figure out how to use it and still not so clear.
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer", new ComponentName(this, HeadsetNotificationBroadcast.class), null);
        mMediaSession.setActive(true);


        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 2, 1)
                .build());
    }

    //Here you will receive some of the button strokes and broadcasts.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setPrepareServiceListener(mApp.getPlayBackStarter());
        getPrepareServiceListener().onServiceRunning(this);
        return START_NOT_STICKY;
    }

    /**
     * Get the current media player.
     *
     * @return {@link MediaPlayer}
     */

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }


    /**
     * Get the audio focus before playing the song.
     */
    private boolean requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Toast.makeText(getApplicationContext(), R.string.unable_to_get_audio_focus, Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }


    public void addOneSongToQueue(Song song) {
        mListSongs.add(song);
    }

    public void addOneSongToPlayNext(Song song) {
        try {
            mListSongs.add(mSongPos + 1, song);
        } catch (Exception e) {
            mListSongs.add(mSongPos, song);
        }
    }

    public void addSongsToQueue(ArrayList<Song> song) {
        mListSongs.addAll(song);
    }

    public void addSongsToPlayNext(ArrayList<Song> song) {
        try {
            mListSongs.addAll(mSongPos + 1, song);
        } catch (Exception e) {
            mListSongs.addAll(mSongPos, song);
        }
    }

    public void headsetDisconnected() {
        if (mMediaPlayer.isPlaying()) {
            stopPlaying();
        }
    }

    public void headsetIsConnected() {
        if (!mMediaPlayer.isPlaying() && !mPlayingForFirstTime) {
            startPlaying();
        }
    }


    private void initAudioFX() {
        try {
            mEqualizerHelper = new EqualizerHelper(mMediaPlayer.getAudioSessionId(), PreferencesHelper.getInstance(mContext).getBoolean(PreferencesHelper.Key.IS_EQUALIZER_ACTIVE, false));
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (mListSongs.size() == 0) return;
        startSong();
    }

    private void startSong() {
        mHandler.removeCallbacks(sendUpdatesToUI);
        mMediaPlayerPrepared = false;

        try {
            mSongUri = getUri(mListSongs.get(mSongPos)._id);
            mSong = mListSongs.get(mSongPos);
        } catch (Exception e) {
            return;
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) throws NullPointerException {


                if (mMediaPlayer == null) return null;
                mMediaPlayer.reset();

                if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_SONG)
                    mMediaPlayer.setLooping(true);

                try {
                    SongDataHelper songDataHelper = new SongDataHelper();
                    setSongDataHelper(songDataHelper);
                    songDataHelper.populateSongData(mContext, null, mSongPos);
                    mApp.getDBAccessHelper().insertSongCount(mListSongs.get(mSongPos));
                    mApp.getDBAccessHelper().addToRecentlyPlayed(mListSongs.get(mSongPos));


                    mMediaPlayer.setDataSource(mContext, mSongUri);
                    mMediaPlayer.setOnPreparedListener(onPreparedListener);
                    mMediaPlayer.setOnErrorListener(onErrorListener);
                    mMediaPlayer.prepareAsync();


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    /**
     * Notification updater.
     */

    public void updateNotification() {
        if (notification) {
            startForeground(NOTIFICATION_ID, buildNotification());
            notification = false;
        } else {
            Notification notification = buildNotification();
            NotificationManager notificationManager = (NotificationManager) mApp.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        updateWidgets();
        mMediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getSongDataHelper().getAlbumArt())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getSongDataHelper().getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getSongDataHelper().getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSongDataHelper().getTitle())
                .build());
    }


    /**
     * Widgets updater.
     */
    public void updateWidgets() {
        Intent smallWidgetIntent = new Intent(mContext, SmallWidgetProvider.class);
        smallWidgetIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int smallWidgetIds[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, SmallWidgetProvider.class));
        smallWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, smallWidgetIds);
        sendBroadcast(smallWidgetIntent);
    }

    public void stopNotify() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        stopSelf();
    }

    /**
     * Every time you start playing ask for the AudioFocus or else it would start playing with and you would be furious.
     */

    public void startPlaying() {

        if (mMediaPlayerPrepared) {
            if (!mMediaPlayer.isPlaying() && requestAudioFocus()) {
                mMediaPlayer.start();
                mHandler.removeCallbacks(startMediaPlayerIfPrepared);
                mHandler.postDelayed(sendUpdatesToUI, 600);
            }
        } else {
            mHandler.post(startMediaPlayerIfPrepared);
        }

        sendBroadcast(mPlayPauseIntent);
        updateNotification();
    }


    public void stopPlaying() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mAudioManager.abandonAudioFocus(audioFocusChangeListener);
            mHandler.removeCallbacks(sendUpdatesToUI);
        }
        sendBroadcast(mPlayPauseIntent);
        updateNotification();
    }

    public void playPauseSong() {
        if (!mMediaPlayer.isPlaying()) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    /**
     * Save the current queue while stopping the service.
     */
    private void saveQueue() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mApp.getDBAccessHelper().saveQueue(mListSongs);
                PreferencesHelper.getInstance(Common.getInstance()).put(PreferencesHelper.Key.CURRENT_SONG_POSITION, mSongPos);
                PreferencesHelper.getInstance(Common.getInstance()).put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, mMediaPlayer.getCurrentPosition());
                PreferencesHelper.getInstance(Common.getInstance()).put(PreferencesHelper.Key.SONG_TOTAL_SEEK_DURATION, mMediaPlayer.getDuration());

                mMediaPlayer.pause();
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                return null;
            }
        }.execute();
    }


    /**
     * Let system know we are playing music and this intent is used my musixmatch also so kinda connection.
     */

    public void sendMediaIntentData() throws NumberFormatException {
        mBundle.putString("track", getSongDataHelper().getTitle());
        mBundle.putString("artist", getSongDataHelper().getArtist());
        mBundle.putString("album", getSongDataHelper().getAlbum());
        try {
            mBundle.putLong("duration", getSongDataHelper().getDuration());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mBundle.putLong("position", mMediaPlayer.getCurrentPosition());
        } catch (Exception e) {
            e.printStackTrace();
            mBundle.putLong("position", 0);
        }

        mBundle.putBoolean("playing", true);
        mBundle.putString("scrobbling_source", "com.boom.music.player");
        mMediaIntent.putExtras(mBundle);
        sendBroadcast(mMediaIntent);
    }

    //Go to next song.
    public void nextSong() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (mListSongs.size() != 1) {
                    if (mSongPos < mListSongs.size() - 1) {
                        mSongPos = mSongPos + 1;
                        startSong();
                    } else {
                        mSongPos = 0;
                        startSong();
                    }
                }
                return null;
            }
        }.execute();

    }

    //Go to previous song.
    public void previousSong() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (mMediaPlayer.getCurrentPosition() >= 5000) {
                    mMediaPlayer.seekTo(0);
                } else {
                    if (mListSongs.size() > 1) {
                        if (mSongPos > 0) {
                            mSongPos--;
                            startSong();
                        } else {
                            mSongPos = mListSongs.size() - 1;
                            startSong();
                        }
                    }
                }
                return null;
            }
        }.execute();

    }

    //Register the headset plug receiver.
    public void registerHeadsetPlugReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        mHeadsetPlugReceiver = new HeadsetPlugBroadcastReceiver();
        mService.registerReceiver(mHeadsetPlugReceiver, filter);
    }

    public ArrayList<Song> getSongList() {
        return mListSongs;
    }


    public void setSongList(ArrayList<Song> listSong) {
        mListSongs.clear();
        mShuffledSongList.clear();
        mOriginalSongList.clear();

        mListSongs.addAll(listSong);
        for (Song oneSong : listSong) {
            try {
                mShuffledSongList.add((Song) oneSong.clone());
                mOriginalSongList.add((Song) oneSong.clone());
            } catch (CloneNotSupportedException e) {
                Logger.log(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void setSelectedSong(int pos) {
        mSongPos = pos;
        if (mListSongs.size() != 0) {
            startSong();
        }
    }

    public int getCurrentSongIndex() {
        return mSongPos;
    }

    public void setCurrentSongIndex(int currentSongIndex) {
        mSongPos = currentSongIndex;
    }


    //Clear things up.
    @Override
    public void onDestroy() {
        saveQueue();
        mApp.setIsServiceRunning(false);
        mApp.getService().clearABRepeatRange();
        updateWidgets();

        sendBroadcast(mPlayPauseIntent);

        mHandler.removeCallbacks(sendUpdatesToUI);

        mMediaSession.release();
        unregisterReceiver(mHeadsetPlugReceiver);
        unregisterReceiver(mHeadsetNotificationBroadcast);

        mWakeLock.release();

        try {
            mEqualizerHelper.releaseEQObjects();
            mEqualizerHelper = null;
        } catch (Exception e1) {
            e1.printStackTrace();
            mEqualizerHelper = null;
        }

        mAudioManagerHelper.setHasAudioFocus(false);
        mAudioManager.abandonAudioFocus(audioFocusChangeListener);

        stopNotify();

        mApp.setService(null);
    }

    /*
    * Get the list from the shuffle list shuffle it again
    * and add it to the playing song list.
    *
    * */

    public void setShuffledOne() {
        mListSongs.clear();
        Collections.shuffle(mShuffledSongList, new Random(System.nanoTime()));
        Collections.shuffle(mShuffledSongList, new Random(System.nanoTime()));
        mListSongs.addAll(mShuffledSongList);
    }

    /*Clear the playing song list and add the orignal song list*/
    public void setOriginalOne() {
        mListSongs.clear();
        mListSongs.addAll(mOriginalSongList);
    }

    private Notification buildNotification() {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        Intent intent;
        PendingIntent pendingIntent;
        mNotificationBuilder = new NotificationCompat.Builder(mContext);
        mNotificationBuilder.setOngoing(true);
        mNotificationBuilder.setAutoCancel(false);
        mNotificationBuilder.setSmallIcon(R.mipmap.small_launcher);
        intent = new Intent();

        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        notificationBuilder.setContentIntent(pendingIntent);

        final RemoteViews notificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_layout);
        final RemoteViews expNotificationView = new RemoteViews(mContext.getPackageName(), R.layout.notification_custom_expanded_layout);


        PendingIntent previousTrackPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.ACTION_PREVIOUS), 0);

        notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);


        PendingIntent playPauseTrackPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.ACTION_PAUSE), 0);
        notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);


        PendingIntent nextTrackPendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.ACTION_NEXT), 0);
        notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);


        PendingIntent stopServicePendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(Constants.ACTION_STOP), 0);
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, pendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_base_collapse, pendingIntent);


        intent = new Intent(mContext, NowPlayingActivity.class);
        intent.putExtra("LAUNCHED_FROM_NOTIFICATION", true);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        expNotificationView.setOnClickPendingIntent(R.id.notification_base_image, pendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_image, pendingIntent);
        mNotificationBuilder.setContentIntent(pendingIntent);

        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_one, getSongDataHelper().getTitle());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_two, getSongDataHelper().getArtist());
        expNotificationView.setTextViewText(R.id.notification_expanded_base_line_three, getSongDataHelper().getAlbum());

        if (mMediaPlayer.isPlaying()) {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_pause_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
        } else {
            notificationView.setImageViewResource(R.id.notification_base_play, R.drawable.btn_playback_play_light);
            expNotificationView.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
        }
        expNotificationView.setImageViewBitmap(R.id.notification_expanded_base_image, getSongDataHelper().getAlbumArt());
        notificationView.setImageViewBitmap(R.id.notification_base_image, getSongDataHelper().getAlbumArt());


        notificationView.setTextViewText(R.id.notification_base_line_one, getSongDataHelper().getTitle());
        notificationView.setTextViewText(R.id.notification_base_line_two, getSongDataHelper().getAlbum());

        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_collapse, stopServicePendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_collapse, stopServicePendingIntent);

        expNotificationView.setViewVisibility(R.id.notification_expanded_base_previous, View.VISIBLE);
        expNotificationView.setViewVisibility(R.id.notification_expanded_base_next, View.VISIBLE);

        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_play, playPauseTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextTrackPendingIntent);
        expNotificationView.setOnClickPendingIntent(R.id.notification_expanded_base_previous, previousTrackPendingIntent);

        notificationView.setViewVisibility(R.id.notification_base_previous, View.VISIBLE);
        notificationView.setViewVisibility(R.id.notification_base_next, View.VISIBLE);

        notificationView.setOnClickPendingIntent(R.id.notification_base_play, playPauseTrackPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_next, nextTrackPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.notification_base_previous, previousTrackPendingIntent);

        mNotificationBuilder.setContent(notificationView);
        Notification notification = mNotificationBuilder.build();
        notification.bigContentView = expNotificationView;
        notification.flags = Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        return notification;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getRepeatSongRangePointA() {
        return mRepeatSongRangePointA;
    }

    /**
     * Returns point B in milliseconds for A-B repeat.
     */
    public int getRepeatSongRangePointB() {
        return mRepeatSongRangePointB;
    }

    /**
     * Sets the A-B Repeat song markers.
     *
     * @param pointA The duration to repeat from (in millis).
     * @param pointB The duration to repeat until (in millis).
     */

    public void setRepeatSongRange(int pointA, int pointB) {
        mRepeatSongRangePointA = pointA;
        mRepeatSongRangePointB = pointB;
        mMediaPlayer.seekTo(pointA);
        startPlaying();
        mHandler.postDelayed(checkABRepeatRange, 100);
    }

    /**
     * Clears the A-B Repeat song markers.
     */

    public void clearABRepeatRange() {
        mHandler.removeCallbacks(checkABRepeatRange);
        mRepeatSongRangePointA = 0;
        mRepeatSongRangePointB = 0;
    }


    public void setPrepareServiceListener(PrepareServiceListener prepareServiceListener) {
        mPrepareServiceListener = prepareServiceListener;
    }


    public void setSongPos(int songPos) {
        mSongPos = songPos;
    }

    /**
     * Indicates if music is currently playing.
     */
    public boolean isPlayingMusic() {
        try {
            return getMediaPlayer().isPlaying();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface PrepareServiceListener {
        void onServiceRunning(MusicService musicService);
    }

    public MediaPlayer.OnErrorListener onErrorListener = ((mp, what, extra) -> {
         /* This error listener might seem like it's not doing anything.
     * However, removing this will cause the mMediaPlayer object to go crazy
     * and skip around. The key here is to make this method return true. This
     * notifies the mMediaPlayer object that we've handled all errors and that
     * it shouldn't do anything else to try and remedy the situation.
     *
     * TL;DR: Don't touch this interface. Ever.
     */
        return true;
    });


    private Uri getUri(long audioId) {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId);
    }

    public EqualizerHelper getEqualizerHelper() {
        return mEqualizerHelper;
    }

    public Song getSong() {
        return mSong;
    }

    public PrepareServiceListener getPrepareServiceListener() {
        return mPrepareServiceListener;
    }

    public SongDataHelper getSongDataHelper() {
        return mSongDataHelper;
    }

    public void setSongDataHelper(SongDataHelper songDataHelper) {
        mSongDataHelper = songDataHelper;
    }

    public boolean isMediaPlayerPrepared() {
        return mMediaPlayerPrepared;
    }
}
