package com.boom.music.player.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.boom.music.player.Common;

/*
 * A Singleton for managing your SharedPreferences.
 *
 * You should make sure to change the WODROB_PREFERENCE to what you want
 * and choose the operating made that suits your needs, the default is
 * MODE_PRIVATE.
 *
 * IMPORTANT: The class is not thread safe. It should work fine in most
 * circumstances since the write and read operations are fast. However
 * if you call edit for bulk updates and do not commit your changes
 * there is a possibility of data loss if a background thread has modified
 * preferences at the same time.
 *
 * Usage:
 *
 * int sampleInt = PreferencesHelper.getInstance().getInt(Key.SAMPLE_INT);
 * PreferencesHelper.getInstance().set(Key.SAMPLE_INT, sampleInt);
 *
 * If PreferencesHelper.getInstance() has been called once, you can
 * simple use PreferencesHelper.getInstance() to save some precious line space.
 */
public class PreferencesHelper {

    private static final String MUSIC_PLAYER_PREFERENCE = "MUSIC_PLAYER_PREFERENCES";
    private static PreferencesHelper sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;
    private PreferencesHelper(Context context) {
        mPref = context.getSharedPreferences(MUSIC_PLAYER_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static PreferencesHelper getInstance() {
        if (sSharedPrefs == null) {
            sSharedPrefs = new PreferencesHelper(Common.getInstance().getApplicationContext());
        }
        return sSharedPrefs;
    }

    public SharedPreferences getPref() {
        return mPref;
    }

    public void put(Key key, String val) {
        doEdit();
        mEditor.putString(key.name(), val);
        doCommit();
    }

    public void put(Key key, int val) {
        doEdit();
        mEditor.putInt(key.name(), val);
        doCommit();
    }

    public void put(Key key, boolean val) {
        doEdit();
        mEditor.putBoolean(key.name(), val);
        doCommit();
    }

    public void put(Key key, float val) {
        doEdit();
        mEditor.putFloat(key.name(), val);
        doCommit();
    }

    /**
     * Convenience method for storing doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to store.
     * @param val The new value for the preference.
     */


    public void put(Key key, double val) {
        doEdit();
        mEditor.putString(key.name(), String.valueOf(val));
        doCommit();
    }

    public void put(Key key, long val) {
        doEdit();
        mEditor.putLong(key.name(), val);
        doCommit();
    }

    public String getString(Key key, String defaultValue) {
        return mPref.getString(key.name(), defaultValue);
    }

    public String getString(Key key) {
        return mPref.getString(key.name(), null);
    }

    public int getInt(Key key) {
        return mPref.getInt(key.name(), 0);
    }

    public int getInt(Key key, int defaultValue) {
        return mPref.getInt(key.name(), defaultValue);
    }

    public long getLong(Key key) {
        return mPref.getLong(key.name(), 0);
    }

    public long getLong(Key key, long defaultValue) {
        return mPref.getLong(key.name(), defaultValue);
    }

    public float getFloat(Key key) {
        return mPref.getFloat(key.name(), 0);
    }

    public float getFloat(Key key, float defaultValue) {
        return mPref.getFloat(key.name(), defaultValue);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to fetch.
     */
    public double getDouble(Key key) {
        return getDouble(key, 0);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to fetch.
     */
    public double getDouble(Key key, double defaultValue) {
        try {
            return Double.valueOf(mPref.getString(key.name(), String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(Key key, boolean defaultValue) {
        return mPref.getBoolean(key.name(), defaultValue);
    }

    public boolean getBoolean(Key key) {
        return mPref.getBoolean(key.name(), false);
    }

    /**
     * Remove keys from SharedPreferences.
     *
     * @param keys The enum of the key(s) to be removed.
     */

    public void remove(Key... keys) {
        doEdit();
        for (Key key : keys) {
            mEditor.remove(key.name());
        }
        doCommit();
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }



    /*public ArrayList<HashMap<String, String>> getSongQueue() {

        Type type = new TypeToken<ArrayList<HashMap<String, String>>>() {
        }.getType();

        try {
            return new Gson().fromJson(PreferencesHelper.getInstance().getString(Key.SONG_QUEUE, "[]"), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/


    public enum Key {


        /**
         * Sorting orders
         */

        ARTIST_SORT_ORDER,
        ALBUM_SORT_ORDER,
        SONG_SORT_ORDER,


        SONG_SORT_TYPE,
        ARTIST_SORT_TYPE,
        ALBUM_SORT_TYPE,

        SONG_QUEUE,
        REPEAT_MODE,
        SHUFFLE_MODE,

        SONG_POSITION,
        SONG_SEEK_POSITION,
        CURRENT_SONG_POSITION,

        SONG_CURRENT_SEEK_DURATION,
        PREVIOUS_ROOT_DIR,
        LAST_PRESET_NAME,
        SONG_TOTAL_SEEK_DURATION,
        RECENTLY_ADDED_WEEKS,
        FIRST_LAUNCH,
        GENRE_SORT_ORDER,
        GENRE_SORT_TYPE,
        COLORS,
        TABS, LAUNCH_COUNT, IS_EQUALIZER_ACTIVE, RECENT_SEARCH
    }
}