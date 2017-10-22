
package com.boom.music.player;


import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;

import com.boom.music.player.Database.DataBaseHelper;
import com.boom.music.player.GoogleAnalytics.AnalyticsTrackers;
import com.boom.music.player.MusicService.MusicService;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.PlayBackStarter.PlayBackStarter;
import com.boom.music.player.Utils.PreferencesHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.L;


/**
 * Created by Reyansh on 27/12/2015.
 */

public class Common extends MultiDexApplication {


    /**
     * Enable or disable debugging and TAG
     */

    /**
     * Constant and service instance
     */
    private static Context mContext;
    private MusicService mService;

    //Device orientation constants.
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;


    //Device screen size/orientation identifiers.
    public static final String REGULAR = "regular";
    public static final String SMALL_TABLET = "small_tablet";
    public static final String LARGE_TABLET = "large_tablet";
    public static final String XLARGE_TABLET = "xlarge_tablet";

    public static final int REGULAR_SCREEN_PORTRAIT = 0;
    public static final int REGULAR_SCREEN_LANDSCAPE = 1;
    public static final int SMALL_TABLET_PORTRAIT = 2;
    public static final int SMALL_TABLET_LANDSCAPE = 3;
    public static final int LARGE_TABLET_PORTRAIT = 4;
    public static final int LARGE_TABLET_LANDSCAPE = 5;
    public static final int XLARGE_TABLET_PORTRAIT = 6;
    public static final int XLARGE_TABLET_LANDSCAPE = 7;


    /**
     * UIL options
     */

    public DisplayImageOptions options = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.EXACTLY)
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .build();


    /**
     * DataBase instance
     */

    private boolean mIsServiceRunning = false;


    /**
     * {@link PlayBackStarter} handles all the playing and pausing adding to queue etc oprations
     */
    private PlayBackStarter mPlayBackStarter;

    /**
     * NowPlaying activity
     */
    private NowPlayingActivity mNowPlayingActivity2;


    /**
     * Firebase instance for crash report and logging
     */
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Context getInstance() {
        return mContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mPlayBackStarter = new PlayBackStarter(mContext);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        initImageLoader();

        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);

        /**
         *disable UIL Logs
         */
        L.writeDebugLogs(false);
    }


    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }

    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();
        // Set screen name.
        t.setScreenName(screenName);
        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(new StandardExceptionParser(this, null).getDescription(Thread.currentThread().getName(), e))
                    .setFatal(false)
                    .build()
            );
        }
    }

    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
    }

    public void setIsServiceRunning(boolean running) {
        mIsServiceRunning = running;
    }

    public MusicService getService() {
        return mService;
    }


    public void setService(MusicService service) {
        mService = service;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);

        L.writeDebugLogs(false);
        L.disableLogging();
        L.writeLogs(false);
    }

    public DataBaseHelper getDBAccessHelper() {
        return DataBaseHelper.getDatabaseHelper(mContext);
    }


    public PlayBackStarter getPlayBackStarter() {
        return mPlayBackStarter;
    }

    public void setPlayBackStarter(PlayBackStarter playBackStarter) {
        mPlayBackStarter = playBackStarter;
    }

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */

    public float convertDpToPixels(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }


    /**
     * Returns the orientation of the device.
     */
    public static int getOrientation() {
        if (mContext.getResources().getDisplayMetrics().widthPixels >
                mContext.getResources().getDisplayMetrics().heightPixels) {
            return ORIENTATION_LANDSCAPE;
        } else {
            return ORIENTATION_PORTRAIT;
        }
    }


    /**
     * Returns the current screen configuration of the device.
     */
    public static int getDeviceScreenConfiguration() {
        String screenSize = mContext.getResources().getString(R.string.screen_size);
        boolean landscape = false;
        if (getOrientation() == ORIENTATION_LANDSCAPE) {
            landscape = true;
        }

        if (screenSize.equals(REGULAR) && !landscape)
            return REGULAR_SCREEN_PORTRAIT;
        else if (screenSize.equals(REGULAR) && landscape)
            return REGULAR_SCREEN_LANDSCAPE;
        else if (screenSize.equals(SMALL_TABLET) && !landscape)
            return SMALL_TABLET_PORTRAIT;
        else if (screenSize.equals(SMALL_TABLET) && landscape)
            return SMALL_TABLET_LANDSCAPE;
        else if (screenSize.equals(LARGE_TABLET) && !landscape)
            return LARGE_TABLET_PORTRAIT;
        else if (screenSize.equals(LARGE_TABLET) && landscape)
            return LARGE_TABLET_LANDSCAPE;
        else if (screenSize.equals(XLARGE_TABLET) && !landscape)
            return XLARGE_TABLET_PORTRAIT;
        else if (screenSize.equals(XLARGE_TABLET) && landscape)
            return XLARGE_TABLET_LANDSCAPE;
        else
            return REGULAR_SCREEN_PORTRAIT;
    }

    public static String convertMillisToMinsSecs(int milliseconds) {

        int secondsValue = milliseconds / 1000 % 60;
        int minutesValue = (milliseconds / (1000 * 60)) % 60;
        int hoursValue = (milliseconds / (1000 * 60 * 60)) % 24;

        String seconds = "";
        String minutes = "";
        String hours = "";

        if (secondsValue < 10) {
            seconds = "0" + secondsValue;
        } else {
            seconds = "" + secondsValue;
        }

        if (minutesValue < 10) {
            minutes = "0" + minutesValue;
        } else {
            minutes = "" + minutesValue;
        }

        if (hoursValue < 10) {
            hours = "0" + hoursValue;
        } else {
            hours = "" + hoursValue;
        }

        String output = "";
        if (hoursValue != 0) {
            output = hours + ":" + minutes + ":" + seconds;
        } else {
            output = minutes + ":" + seconds;
        }

        return output;
    }

    /**
     * Returns the no of column which will be applied to the grids on different devices
     */
    public static int getNumberOfColms() {

        if (getDeviceScreenConfiguration() == Common.REGULAR_SCREEN_PORTRAIT) {
            return 2;
        } else if (getDeviceScreenConfiguration() == Common.REGULAR_SCREEN_LANDSCAPE) {
            return 4;
        }
        return 2;
    }

    /**
     * Returns the sharedpreferences instance
     */
    public PreferencesHelper getSharedPreferencesHelper() {
        return PreferencesHelper.getInstance(mContext);
    }


    /*
     * Returns the status bar height for the current layout configuration.
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

    /*
    * Returns the navigation bar height for the current layout configuration.
    */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    /*public NowPlayingActivity getNowPlayingActivity2() {
        return mNowPlayingActivity2;
    }*/

  /*  public void setNowPlayingActivity2(NowPlayingActivity NowPlayingActivity2) {
        mNowPlayingActivity2 = NowPlayingActivity2;
    }*/

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        mFirebaseAnalytics = firebaseAnalytics;
    }

    public static String getAndroidId() {
        return Settings.Secure.ANDROID_ID;
    }

    public boolean isTabletInLandscape() {
        int screenConfig = getDeviceScreenConfiguration();
        return screenConfig == SMALL_TABLET_LANDSCAPE ||
                screenConfig == LARGE_TABLET_LANDSCAPE ||
                screenConfig == XLARGE_TABLET_LANDSCAPE;

    }

    public boolean isTabletInPortrait() {
        int screenConfig = getDeviceScreenConfiguration();
        return screenConfig == SMALL_TABLET_PORTRAIT ||
                screenConfig == LARGE_TABLET_PORTRAIT ||
                screenConfig == XLARGE_TABLET_PORTRAIT;

    }

    public boolean isPhoneInLandscape() {
        int screenConfig = getDeviceScreenConfiguration();
        return screenConfig == REGULAR_SCREEN_LANDSCAPE;
    }

    public boolean isPhoneInPortrait() {
        int screenConfig = getDeviceScreenConfiguration();
        return screenConfig == REGULAR_SCREEN_PORTRAIT;
    }

}