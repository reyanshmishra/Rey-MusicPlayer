package com.boom.music.player.NowPlaying;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boom.music.player.Adapters.NowPlayingBottomBarAdapter;
import com.boom.music.player.Common;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.TypefaceHelper;

import java.util.ArrayList;

/**
 * Created by REYANSH on 7/31/2017.
 */

public class NowPlayingBottomBarFragment extends Fragment {

    private View mView;
    private FloatingActionButton mFloatingActionButton;
    private RelativeLayout mLinearLayout;
    private SeekBar mSeekBar;
    private TextView mDurationTextView;
    private Common mApp;
    private Handler mHandler;
    private RecyclerView mRecyclerView;
    private NowPlayingBottomBarAdapter mNowPlayingBottomBarAdapter;
    private ArrayList<Song> songs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_bottom_bar, container, false);
        mApp = (Common) getActivity().getApplicationContext();
        mView.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mNowPlayingBottomBarAdapter = new NowPlayingBottomBarAdapter(this);
        mRecyclerView.setAdapter(mNowPlayingBottomBarAdapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        mDurationTextView = (TextView) mView.findViewById(R.id.duration_text);
        mDurationTextView.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), "Futura-Bold-Font"));


        mSeekBar = (SeekBar) mView.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);

        try {
            mSeekBar.setThumb(ContextCompat.getDrawable(Common.getInstance(), R.drawable.transparent_drawable));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mHandler = new Handler();

        mFloatingActionButton = (FloatingActionButton) mView.findViewById(R.id.fab);
        mLinearLayout = (RelativeLayout) mView.findViewById(R.id.main_background);
        updateUI();
        mFloatingActionButton.setOnClickListener(v -> {
            mApp.getPlayBackStarter().playPauseFromBottomBar();
            try {

                setSeekbarDuration(mApp.getService().getMediaPlayer().getDuration());
            } catch (NullPointerException e) {
                e.printStackTrace();
                mFloatingActionButton.setImageResource(R.drawable.play);
            }

        });
        mLinearLayout.setOnClickListener(v -> {

        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                    int newPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                    int oldPosition;
                    if (mApp.isServiceRunning()) {
                        oldPosition = mApp.getService().getCurrentSongIndex();
                    } else {
                        oldPosition = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION);
                    }
                    if (newPosition != -1)
                        if (mApp.isServiceRunning() && newPosition != oldPosition) {
                            PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
                            mApp.getService().setSelectedSong(newPosition);
                        } else if (oldPosition != newPosition) {
                            PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
                            mApp.getPlayBackStarter().playSongs(songs, newPosition);
                        }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(Common.getInstance()).registerReceiver((mUpdateUIReceiver), new IntentFilter(Constants.ACTION_UPDATE_NOW_PLAYING_UI));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(Common.getInstance()).unregisterReceiver((mUpdateUIReceiver));
    }

    /**
     * Update UI when track completes it self and goes to next one
     */


    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(Constants.JUST_UPDATE_UI)) {
                if (intent.hasExtra(Constants.ACTION_PLAY_PAUSE)){
                    if (mApp.getService().getMediaPlayer().isPlaying()) {
                        mFloatingActionButton.setImageResource(R.drawable.pause);
                    } else {
                        mFloatingActionButton.setImageResource(R.drawable.play);
                    }
                }else{
                    updateUI();
                }
            }
        }
    };


    SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mApp.isServiceRunning()) {
                try {
                    long currentSongDuration = mApp.getService().getMediaPlayer().getDuration();
                    seekBar.setMax((int) currentSongDuration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(seekbarUpdateRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int seekBarPosition = seekBar.getProgress();
            if (mApp.isServiceRunning()) {
                mApp.getService().getMediaPlayer().seekTo(seekBarPosition);
                mHandler.post(seekbarUpdateRunnable);
            } else {
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, seekBarPosition);
                mDurationTextView.setText(Common.convertMillisToMinsSecs(mSeekBar.getProgress()));
            }
        }
    };

    private void updateUI() {

        if (mApp.isServiceRunning()) {
            mNowPlayingBottomBarAdapter.updateData(mApp.getService().getSongList());

            if (mApp.getService().getMediaPlayer().isPlaying()) {
                mFloatingActionButton.setImageResource(R.drawable.pause);
            } else {
                mFloatingActionButton.setImageResource(R.drawable.play);
            }

            mView.setVisibility(View.VISIBLE);
            try {
                setSeekbarDuration(mApp.getService().getMediaPlayer().getDuration());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRecyclerView.scrollToPosition(mApp.getService().getCurrentSongIndex());
        } else {
            new AsyncTask<Void, Void, Void>() {
                int position;
                int seekPosition;

                @Override
                protected Void doInBackground(Void... params) {
                    songs = mApp.getDBAccessHelper().getQueue();
                    position = MusicUtils.getSongPosition();
                    seekPosition = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (songs.size() > 0) {
                        mNowPlayingBottomBarAdapter.updateData(songs);
                        mSeekBar.setMax(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.SONG_TOTAL_SEEK_DURATION, 0));
                        mSeekBar.setProgress(seekPosition);
                        mRecyclerView.scrollToPosition(position);
                        mDurationTextView.setText(Common.convertMillisToMinsSecs(mSeekBar.getProgress()));
                        mView.setVisibility(View.VISIBLE);
                    } else {
                        mView.setVisibility(View.GONE);
                    }
                }
            }.execute();
        }
    }

    /**
     * Create a new Runnable to update the seekbar and time every 100ms.
     */
    public Runnable seekbarUpdateRunnable = new Runnable() {

        public void run() {
            try {
                long currentPosition = mApp.getService().getMediaPlayer().getCurrentPosition();
                int currentPositionInSecs = (int) currentPosition;
                mSeekBar.setProgress(currentPositionInSecs);
                mDurationTextView.setText(Common.convertMillisToMinsSecs(mSeekBar.getProgress()));
                if (mApp.isServiceRunning()) {
                    if (mApp.getService().getMediaPlayer().isPlaying()) {
                        mHandler.postDelayed(seekbarUpdateRunnable, 1000);
                    } else {
                        mHandler.removeCallbacks(seekbarUpdateRunnable);
                    }
                } else {
                    mHandler.removeCallbacks(seekbarUpdateRunnable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void setSeekbarDuration(int duration) {
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(mApp.getService().getMediaPlayer().getCurrentPosition());
        mHandler.postDelayed(seekbarUpdateRunnable, 1000);
        mDurationTextView.setText(Common.convertMillisToMinsSecs(mSeekBar.getProgress()));
    }
}
