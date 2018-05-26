package com.reyansh.audio.audioplayer.free.Utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

//Caches the custom fonts in memory to improve rendering performance.
public class TypefaceHelper {

    public static final String FUTURA_BOLD = "Futura-Bold-Font";
    public static final String FUTURA_CONDENSED = "Futura-Condensed-Font";
    public static final String TYPEFACE_FOLDER = "fonts";
    public static final String TYPEFACE_EXTENSION = ".ttf";
    public static final String FUTURA_BOOK = "Futura-Book-Font";

    private static Hashtable<String, Typeface> sTypeFaces = new Hashtable<String, Typeface>(4);

    public static Typeface getTypeface(Context context, String fileName) {
        Typeface tempTypeface = sTypeFaces.get(fileName);

        if (tempTypeface == null) {
            String fontPath = new StringBuilder(TYPEFACE_FOLDER).append('/')
                    .append(fileName)
                    .append(TYPEFACE_EXTENSION)
                    .toString();

            tempTypeface = Typeface.createFromAsset(context.getAssets(), fontPath);
            sTypeFaces.put(fileName, tempTypeface);
        }

        return tempTypeface;
    }

}
