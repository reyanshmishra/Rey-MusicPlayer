package com.boom.music.player.NowPlaying;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.boom.music.player.Activities.TracksSubFragment;
import com.boom.music.player.Animations.FadeAnimation;
import com.boom.music.player.Common;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.SubGridViewActivity.TracksSubGridViewFragment;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.VelocityViewPager.VelocityViewPager;
import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;

import java.util.ArrayList;

/**
 * Created by REYANSH on 6/19/2017.
 */

public class NowPlayingActivity extends AppCompatActivity implements HmsPickerDialogFragment.HmsPickerDialogHandler {


    private QueueFragment mQueueFragment;


    private FrameLayout mParentLayout;

    /**
     * Application context
     */
    private Common mApp;
    private Context mContext;


    /**
     * VelocityViewPager and its adapter
     */

    private VelocityViewPager mVelocityViewPager;
    private PlaylistPagerAdapter mViewPagerAdapter;


    /**
     * Buttons to control playback
     */

    private ImageButton mPlayPauseButton;
    private RelativeLayout mPlayPauseButtonBackground;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;


    /**
     * Seekbar indicator
     */
    private CardView mSeekBarIndicatorCardView;
    private TextView mSeekBarIndicatorTextView;
    private SeekBar mSeekBar;


    /**
     * Layouts
     */
    private RelativeLayout mRootRelativeLayout;
    private RelativeLayout mNowPlayingLayout;

    /**
     * Toolbar and appbar
     */

    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;

    /**
     * Handler
     */
    private Handler mHandler;


    /**
     * Control and its parts
     */

    private CardView mControlsHolderCardView;


    //Differentiates between a user's scroll input and a programmatic scroll.
    private boolean USER_SCROLL = true;

    private RelativeLayout mNowPlayingContainer;
    private RelativeLayout mQueueFragmentContainer;

    public ArrayList<Song> mSongs;
    private int mRemainingTimeToPause;


    private ArrayList<Fragment> mFragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing_2);

        /**
         *Initialisations
         */
        mFragments = new ArrayList<>();

        mContext = getApplicationContext();
        mApp = (Common) mContext;
        if (mApp.isServiceRunning()) {
            mSongs = mApp.getService().getSongList();
        } else {
            mSongs = mApp.getDBAccessHelper().getQueue();
        }

        mHandler = new Handler();

        mVelocityViewPager = (VelocityViewPager) findViewById(R.id.nowPlayingPlaylistPager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        mAppBarLayout = (AppBarLayout) findViewById(R.id.id_toolbar_container);
        /**
         *Seekbar Related
         */
        mSeekBarIndicatorCardView = (CardView) findViewById(R.id.seekbarIndicatorParent);
        mSeekBarIndicatorTextView = (TextView) findViewById(R.id.seekbarIndicatorText);
        mSeekBar = (SeekBar) findViewById(R.id.nowPlayingSeekBar);


        /**
         *Adding QueueFragment
         */

        mQueueFragmentContainer = (RelativeLayout) findViewById(R.id.queue_fragment_container);
        mNowPlayingContainer = (RelativeLayout) findViewById(R.id.nowPlayingRootContainer);

        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            int width = (metrics.widthPixels) / 2;

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mQueueFragmentContainer.getLayoutParams();
            params.width = width;
            mQueueFragmentContainer.setLayoutParams(params);

            RelativeLayout.LayoutParams paramss = (RelativeLayout.LayoutParams) mNowPlayingContainer.getLayoutParams();
            paramss.width = width;
            mNowPlayingContainer.setLayoutParams(paramss);


        }

        /**
         *set toolbar height
         */

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(this);
        params.bottomMargin = 0;
        mAppBarLayout.setLayoutParams(params);


        /**
         *All Buttons
         */
        mPlayPauseButtonBackground = (RelativeLayout) findViewById(R.id.playPauseButtonBackground);
        mPlayPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        mNextButton = (ImageButton) findViewById(R.id.nextButton);
        mPreviousButton = (ImageButton) findViewById(R.id.previousButton);
        mShuffleButton = (ImageButton) findViewById(R.id.shuffleButton);
        mRepeatButton = (ImageButton) findViewById(R.id.repeatButton);

        mControlsHolderCardView = (CardView) findViewById(R.id.now_playing_controls_header_parent);

        initViewPager();


        /**
         *Listeners
         */

        mSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mPlayPauseButtonBackground.setOnClickListener(onPlayPauseListener);
        mPlayPauseButton.setOnClickListener(onPlayPauseListener);

        mNextButton.setOnClickListener(onNextListener);
        mPreviousButton.setOnClickListener(onPreviousListener);
        mShuffleButton.setOnClickListener(onShuffleListener);
        mRepeatButton.setOnClickListener(onRepeatListener);

        mHandler.postDelayed(() -> animateInControlsBar(), 500);

    }


    View.OnClickListener onShuffleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_OFF) == Constants.SHUFFLE_OFF) {
                if (mApp.isServiceRunning()) {
                    mApp.getService().setShuffledOne();
                } else {
                }
                PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_ON);
            } else {
                if (mApp.isServiceRunning()) {
                    mApp.getService().setOriginalOne();
                } else {
                }
                PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_OFF);
            }
            applyShuffleIcon();
        }
    };

    private void applyShuffleIcon() {
        if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.SHUFFLE_MODE, Constants.SHUFFLE_OFF) == Constants.SHUFFLE_OFF) {
            mShuffleButton.setImageResource(R.drawable.shuffle);
        } else {
            mShuffleButton.setImageResource(R.drawable.shuffle_on);
        }
    }


    View.OnClickListener onRepeatListener = v -> {
        /**
         *Set repeat logic
         */
        if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_OFF) {
            PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_PLAYLIST);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_PLAYLIST) {
            PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_SONG);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_SONG) {
            PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.A_B_REPEAT) {
            mApp.getService().clearABRepeatRange();
            PreferencesHelper.getInstance(mContext).put(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF);
        }
        applyRepeatButton();
    };

    View.OnClickListener onPreviousListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Remove the seekbar update runnable.


			/*
             * Scrolling the pager will automatically call the skipToTrack() method.
			 * Since we're passing true for the dispatchToListener parameter, the
			 * onPageSelected() listener will receive a callback once the scrolling
			 * animation completes. This has the side-benefit of letting the animation
			 * finish before starting playback (keeps the animation buttery smooth).
			 */

            int newPosition = mVelocityViewPager.getCurrentItem() - 1;
            if (newPosition > -1) {
                scrollViewPager(newPosition, true, 1, true);
            } else {
                mVelocityViewPager.setCurrentItem(0, false);
            }
        }
    };
    /**
     * Create a new Runnable to update the seekbar and time every 100ms.
     */
    public Runnable seekbarUpdateRunnable = new Runnable() {
        public void run() {
            try {
                long currentPosition = mApp.getService().getMediaPlayer().getCurrentPosition();
                int currentPositionInSecs = (int) currentPosition / 1000;
                mSeekBar.setProgress(currentPositionInSecs);
                mHandler.postDelayed(this, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    View.OnClickListener onNextListener = v -> {
        //Remove the seekbar update runnable.

        /*
             * Scrolling the pager will automatically call the skipToTrack() method.
			 * Since we're passing true for the dispatchToListener parameter, the
			 * onPageSelected() listener will receive a callback once the scrolling
			 * animation completes. This has the side-benefit of letting the animation
			 * finish before starting playback (keeps the animation buttery smooth).
			 */
        int newPosition = mVelocityViewPager.getCurrentItem() + 1;
        if (newPosition < mViewPagerAdapter.getCount()) {
            scrollViewPager(newPosition, true, 1, true);
        } else {
            if (mApp.getSharedPreferencesHelper().getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_PLAYLIST)
                mVelocityViewPager.setCurrentItem(0, false);
            else
                Toast.makeText(mContext, R.string.no_songs_to_skip_to, Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Scrolls the ViewPager programmatically. If dispatchToListener
     * is true, USER_SCROLL will be set to true.
     */
    private void scrollViewPager(int newPosition,
                                 boolean smoothScroll,
                                 int velocity,
                                 boolean dispatchToListener) {

        USER_SCROLL = dispatchToListener;
        mVelocityViewPager.scrollToItem(newPosition, smoothScroll, velocity, dispatchToListener);

    }


    View.OnClickListener onPlayPauseListener = v -> {

        //BZZZT! Give the user a brief haptic feedback touch response.
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        //Update the playback UI elements.
        if (mApp.isServiceRunning()) {
            if (mApp.getService().isPlayingMusic()) {
                animatePauseToPlay();
                mHandler.removeCallbacks(seekbarUpdateRunnable);
            } else {
                animatePlayToPause();
                mHandler.post(seekbarUpdateRunnable);
            }
        } else {
            animatePlayToPause();
            mHandler.postDelayed(seekbarUpdateRunnable, 1500);
        }

            /*
             * Toggle the playback state in a separate thread. This
             * will allow the play/pause button animation to remain
             * buttery smooth.
             */
        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                mApp.getPlayBackStarter().playPauseFromBottomBar();
                return null;
            }

        }.execute();


    };


    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                if (mApp.getService().isMediaPlayerPrepared()) {
                    long currentSongDuration = mApp.getService().getMediaPlayer().getDuration();
                    seekBar.setMax((int) currentSongDuration / 1000);
                    if (fromUser) {
                        mSeekBarIndicatorTextView.setText(Common.convertMillisToMinsSecs(seekBar.getProgress() * 1000));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(fadeOutSeekbarIndicator);
            mSeekBarIndicatorCardView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int seekBarPosition = seekBar.getProgress();
            if (mApp.isServiceRunning())
                mApp.getService().getMediaPlayer().seekTo(seekBarPosition * 1000);
            //Re-initiate the handler.
            //Fade out the indicator after 1000ms.
            mHandler.postDelayed(fadeOutSeekbarIndicator, 1000);

        }
    };
    /**
     * Seekbar change indicator.
     */
    private Runnable fadeOutSeekbarIndicator = () -> {
        FadeAnimation fadeOut = new FadeAnimation(mSeekBarIndicatorCardView,
                300, 0.9f, 0.0f, null);
        fadeOut.animate();
    };


    /**
     * Update UI when track completes it self and goes to next one
     */


    BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(Constants.JUST_UPDATE_UI)) {
                try {
                    int newPosition = mApp.getService().getCurrentSongIndex();
                    int currentPosition = mVelocityViewPager.getCurrentItem();
                    if (currentPosition != newPosition) {
                        if (newPosition > 0 && Math.abs(newPosition - currentPosition) <= 5) {
                            scrollViewPager(newPosition, true, 1, false);
                        } else {
                            mVelocityViewPager.setCurrentItem(newPosition, false);
                        }
                        mSeekBar.setMax(mApp.getService().getMediaPlayer().getDuration() / 1000);
                        mSeekBar.setProgress(0);
                        if (mQueueFragment != null) {
                            mQueueFragment.getAdapter().notifyDataSetChanged();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mHandler.post(seekbarUpdateRunnable);
                mPlayPauseButton.setImageResource(R.drawable.pause);
            }
        }
    };


    /**
     * Slides in the controls bar from the bottom along with a
     * slight rotation.
     */
    private void animateInControlsBar() {
        android.view.animation.TranslateAnimation slideUp =
                new android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 2.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
        slideUp.setDuration(300);
        slideUp.setInterpolator(new DecelerateInterpolator(2.0f));

        slideUp.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mControlsHolderCardView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mControlsHolderCardView.startAnimation(slideUp);
    }


    /**
     * Animates the pause button to a play button.
     */
    private void animatePauseToPlay() {

        //Check to make sure the current icon is the pause icon.


        //Scale out the pause button.
        final ScaleAnimation scaleOut = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                mPlayPauseButton.getWidth() / 2,
                mPlayPauseButton.getHeight() / 2);
        scaleOut.setDuration(150);
        scaleOut.setInterpolator(new AccelerateInterpolator());


        //Scale in the play button.
        final ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                mPlayPauseButton.getWidth() / 2,
                mPlayPauseButton.getHeight() / 2);
        scaleIn.setDuration(150);
        scaleIn.setInterpolator(new DecelerateInterpolator());

        scaleOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setImageResource(R.drawable.play);
                mPlayPauseButton.setPadding(0, 0, -5, 0);
                mPlayPauseButton.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        scaleIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setScaleX(1.0f);
                mPlayPauseButton.setScaleY(1.0f);
                mPlayPauseButton.setId(R.drawable.play);
                mPlayPauseButton.setScaleX(1.2f);
                mPlayPauseButton.setScaleY(1.2f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mPlayPauseButton.startAnimation(scaleOut);
    }


    /**
     * Animates the play button to a pause button.
     */
    private void animatePlayToPause() {

        //Fade out the play button.
        final ScaleAnimation scaleOut = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                mPlayPauseButton.getWidth() / 2,
                mPlayPauseButton.getHeight() / 2);
        scaleOut.setDuration(150);
        scaleOut.setInterpolator(new AccelerateInterpolator());


        //Scale in the pause button.
        final ScaleAnimation scaleIn = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                mPlayPauseButton.getWidth() / 2,
                mPlayPauseButton.getHeight() / 2);
        scaleIn.setDuration(150);
        scaleIn.setInterpolator(new DecelerateInterpolator());

        scaleOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setImageResource(R.drawable.pause);
                mPlayPauseButton.setPadding(0, 0, 0, 0);
                mPlayPauseButton.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        scaleIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayPauseButton.setScaleX(1.0f);
                mPlayPauseButton.setScaleY(1.0f);
                mPlayPauseButton.setId(R.drawable.pause);
                mPlayPauseButton.setScaleX(1.2f);
                mPlayPauseButton.setScaleY(1.2f);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        mPlayPauseButton.startAnimation(scaleOut);
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(mContext).registerReceiver((mUpdateUIReceiver), new IntentFilter(Constants.ACTION_UPDATE_NOW_PLAYING_UI));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver((mUpdateUIReceiver));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Update the seekbar.
        try {
//            setSeekbarDuration(mApp.getService().getMediaPlayer().getDuration() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mApp.isServiceRunning()) {
            if (mApp.getService().isPlayingMusic()) {
                mPlayPauseButton.setImageResource(R.drawable.pause);
                mHandler.post(seekbarUpdateRunnable);
            } else {
                mPlayPauseButton.setImageResource(R.drawable.play);
                mHandler.removeCallbacks(seekbarUpdateRunnable);
            }
        } else {
            mPlayPauseButton.setImageResource(R.drawable.play);
        }
        applyRepeatButton();
        applyShuffleIcon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
        }
        mViewPagerAdapter = null;
        Log.d("DESTROYED", "DESTROYED");
    }

    /**
     * Apply repeat button drawable
     */
    public void applyRepeatButton() {

        if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_OFF) {
            mRepeatButton.setImageResource(R.drawable.repeat_off);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_SONG) {
            mRepeatButton.setImageResource(R.drawable.repeat_once);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.REPEAT_PLAYLIST) {
            mRepeatButton.setImageResource(R.drawable.repeat);
        } else if (PreferencesHelper.getInstance(mContext).getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.A_B_REPEAT) {
            mRepeatButton.setImageResource(R.drawable.repeat_a_b);
        }
    }


    @Override
    public void onBackPressed() {
        if (mFragments.size() > 0) {
            Fragment fragment = mFragments.get(mFragments.size() - 1);
            if (fragment instanceof TracksSubFragment) {
                ((TracksSubFragment) fragment).removeFragment();
            }
            if (fragment instanceof TracksSubGridViewFragment) {
                ((TracksSubGridViewFragment) fragment).removeFragment();
            }

            if (fragment instanceof QueueFragment) {
                ((QueueFragment) fragment).removeFragment();
            }
            mFragments.remove(fragment);
            return;
        }

        if (getIntent().getBooleanExtra(Constants.FROM_NOTIFICATION, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }

    private void initViewPager() {
        try {

            mVelocityViewPager.setVisibility(View.INVISIBLE);
            mViewPagerAdapter = new PlaylistPagerAdapter(this, getSupportFragmentManager());
            mVelocityViewPager.setAdapter(mViewPagerAdapter);
            mVelocityViewPager.setOffscreenPageLimit(0);
            mVelocityViewPager.setOnPageChangeListener(mPageChangeListener);

            if (mApp.isServiceRunning())
                mVelocityViewPager.setCurrentItem(mApp.getService().getCurrentSongIndex(), false);
            else {
                int pos = PreferencesHelper.getInstance(Common.getInstance()).getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);
                mVelocityViewPager.setCurrentItem(pos, false);
            }

            FadeAnimation fadeAnimation = new FadeAnimation(mVelocityViewPager, 600, 0.0f, 1.0f, new DecelerateInterpolator(2.0f));
            fadeAnimation.animate();

        } catch (IllegalStateException e) {
        }
        //Delay loading extra fragments by 1000ms.
        new Handler().postDelayed(() -> mVelocityViewPager.setOffscreenPageLimit(10), 1000);


    }

    VelocityViewPager.OnPageChangeListener mPageChangeListener = new VelocityViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int pagerPosition, float swipeVelocity, int offsetFromCurrentPosition) {
            if (mApp.isServiceRunning() && mApp.getService().getSongList().size() != 1) {
                if (swipeVelocity == 0.0f && pagerPosition != mApp.getService().getCurrentSongIndex()) {
                    if (USER_SCROLL) {
                        mHandler.postDelayed(() -> mApp.getService().setSelectedSong(pagerPosition), 200);
                    }
                }
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int scrollState) {
            if (scrollState == VelocityViewPager.SCROLL_STATE_DRAGGING) USER_SCROLL = true;
        }
    };

    /**
     * Smoothly scrolls the seekbar to the indicated position.
     */
    private void smoothScrollSeekbar(int progress) {
        ObjectAnimator animation = ObjectAnimator.ofInt(mSeekBar, "progress", progress);
        animation.setDuration(200);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    TextView mTimerText;

    public void setTimer() {
        if (mRemainingTimeToPause > 0) {
            View view = getLayoutInflater().inflate(R.layout.dialog_timer, null);
            mTimerText = (TextView) view.findViewById(R.id.text_view_timer_dialog);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.timer_is_running)
                    .setView(view)
                    .setPositiveButton(R.string.stop, (dialog, which) -> {
                        mHandler.removeCallbacks(pauseTimer);
                        mRemainingTimeToPause = 0;
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {

                    })
                    .show();
        } else {
            HmsPickerBuilder hpb = new HmsPickerBuilder()
                    .setFragmentManager(getSupportFragmentManager())
                    .setStyleResId(R.style.MyCustomBetterPickerTheme);
            hpb.show();
        }
    }

    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
        mRemainingTimeToPause = (hours * 60 * 60) + (minutes * 60) + seconds;
        Toast.makeText(mContext, R.string.pause_timer_is_set, Toast.LENGTH_SHORT).show();
        mHandler.post(pauseTimer);
    }


    private Runnable pauseTimer = new Runnable() {
        public void run() {
            mRemainingTimeToPause--;

            if (mTimerText != null)
                mTimerText.setText(MusicUtils.makeShortTimeString(mContext, mRemainingTimeToPause));

            mHandler.postDelayed(this, 1000);
            if (mRemainingTimeToPause == 0) {
                if (mApp.isServiceRunning())
                    mApp.getPlayBackStarter().playPauseSongs();
                Toast.makeText(mContext, R.string.paused_by_timer, Toast.LENGTH_SHORT).show();
                mHandler.removeCallbacks(this);
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            mQueueFragment = new QueueFragment();
            addFragment(mQueueFragment);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.now_playing_drawer_frame_root, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        mFragments.add(fragment);
    }
}
