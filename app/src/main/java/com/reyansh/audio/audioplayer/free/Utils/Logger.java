package com.reyansh.audio.audioplayer.free.Utils;

import android.util.Log;

/**
 * Created by REYANSH on 8/5/2017.
 */

public class Logger {
    private static String TAG = "BOOOOOOOOOM:-";

    public static void log(String log) {
        Log.d(TAG, log);
    }

    public static void exp(String log) {
        Log.e(TAG, log);
    }
}
