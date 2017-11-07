package com.boom.music.player.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaScannerConnection;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.DeleteAlertDialog;
import com.boom.music.player.Dialogs.PermissionToEditSdCardDialog;
import com.boom.music.player.Dialogs.PopupWritePermission;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.Lastfmapi.LastFmInterface;
import com.boom.music.player.Lastfmapi.Models.ArtistModel;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;

/**
 * Created by Reyansh on 03/01/2016.
 */
public class MusicUtils {

    public final static int URI_REQUEST_CODE_DELETE = 29;
    static ArrayList<HashMap<String, String>> ok = new ArrayList<HashMap<String, String>>();
    private static ContentValues[] sContentValuesCache = null;


    public static void addToPlaylist(Context context, long[] ids, long playlistid) {
        if (ids == null) {
            Log.e("MusicBase", "ListSelection null");
        } else {
            int size = ids.length;
            ContentResolver resolver = context.getContentResolver();
            String[] cols = new String[]{"count(*)"};
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
            Cursor cur = resolver.query(uri, cols, null, null, null);
            cur.moveToFirst();
            int base = cur.getInt(0);
            cur.close();
            int numinserted = 0;
            for (int i = 0; i < size; i += 1000) {
                makeInsertItems(ids, i, 1000, base);
                numinserted += resolver.bulkInsert(uri, sContentValuesCache);
            }
        }
    }

    private static void makeInsertItems(long[] ids, int offset, int len, int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }
        if (sContentValuesCache == null || sContentValuesCache.length != len) {
            sContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (sContentValuesCache[i] == null) {
                sContentValuesCache[i] = new ContentValues();
            }
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver == null) {
                return null;
            }
            if (limit > 0) {
                uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
            }
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (UnsupportedOperationException ex) {
            return null;
        }

    }

    public static Cursor makeArtistSongCursor(Context context, long artistID) {
        ContentResolver contentResolver = context.getContentResolver();
        final String artistSongSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_ORDER);
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String string = "is_music=1 AND title != '' AND artist_id=" + artistID;
        return contentResolver.query(uri, new String[]{"_id",
                "title", "artist",
                "album",
                "duration",
                "track",
                "album_id",
                "_data"}, string, null, artistSongSortOrder);
    }


    public static String convertMillisToMinsSecs(long milliseconds) {

        int secondsValue = (int) (milliseconds / 1000) % 60;
        int minutesValue = (int) ((milliseconds / (1000 * 60)) % 60);
        int hoursValue = (int) ((milliseconds / (1000 * 60 * 60)) % 24);

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

        String output = " ";

        if (hoursValue != 0) {
            output = hours + ":" + minutes + ":" + seconds;
        } else {
            output = minutes + ":" + seconds;
        }

        return output;
    }

    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    public static void setRingtone(AppCompatActivity context, long id) {
        if (!checkSystemWritePermission(context)) return;

        ContentResolver resolver = context.getContentResolver();
        Uri ringUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        try {
            ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.Media.IS_ALARM, "1");
            resolver.update(ringUri, values, null, null);
        } catch (UnsupportedOperationException ex) {
            Log.e("Notset", "couldn't set ringtone flag for id " + id);
            return;
        }

        String[] cols = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE
        };

        String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                cols, where, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, ringUri.toString());
                String message = context.getString(R.string.ringtone_set);
                String filename = '"' + cursor.getString(2) + '"';
                Toast.makeText(context, filename + " " + message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public static int idForPlaylist(Context context, String name) {
        Cursor c = context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name},
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }


    private static boolean checkSystemWritePermission(AppCompatActivity appCompatActivity) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(appCompatActivity);
            if (retVal) {
            } else {
                PopupWritePermission dialog = new PopupWritePermission();
                dialog.show(appCompatActivity.getSupportFragmentManager(), "FRAGMENT_TAG");
            }
        }
        return retVal;
    }

    public static void removeFromPlaylist(Context resolver, Long audioId, Long plid, String songPath) {
        if (plid != -1 && plid != -2 && plid != -3 && plid != -4) {
            String[] cols = new String[]{"count(*)"};
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
            Cursor cur = resolver.getContentResolver().query(uri, cols, null, null, null);
            cur.moveToFirst();
            cur.close();
            resolver.getContentResolver().delete(uri, MediaStore.Audio.Playlists.Members._ID + "=" + audioId, null);

        } else if (plid == -2) {
        } else if (plid == -3) {
        } else if (plid == -4) {
        } else {
            deleteSongs(songPath, resolver);
        }
    }

    public static void makePlaylistMenu(Context context, SubMenu sub, int groupdId) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            sub.clear();
            sub.add(groupdId, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    Intent intent = new Intent();
                    intent.putExtra("playlist", cur.getLong(0));
                    sub.add(groupdId, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void overflowsubmenu(Context context, SubMenu subMenu) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            subMenu.clear();
            subMenu.add(0, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    Intent intent = new Intent();
                    intent.putExtra("playlist", cur.getLong(0));
                    subMenu.add(0, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void deleteSongs(String path, final Context context) {

        try {
            MediaScannerConnection.scanFile(context, new String[]{path},
                    null, (path1, uri) -> context.getContentResolver().delete(uri, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "Song Deleted", Toast.LENGTH_SHORT).show();

    }

    public static ArrayList<Song> searchSongs(Context context, String searchString) {
        return getSongsForCursor(makeSongCursor(context, "title LIKE ?", new String[]{"%" + searchString + "%"}));
    }

    public static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        String selectionStatement = "is_music=1 AND title != ''";
        final String songSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_ORDER);

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.TRACK,
                        MediaStore.Audio.Media.ARTIST_ID,}
                , selectionStatement, paramArrayOfString, songSortOrder);

        return cursor;
    }

    public static Cursor getSongForID(Context context, long id) {
        return makeSongCursor(context, "_id=" + String.valueOf(id), null);
    }

    public static ArrayList<Song> getSongsForCursor(Cursor cursor) {
        ArrayList<Song> songs = new ArrayList<>();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)),
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                );
                songs.add(song);
            } while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return songs;
    }

    public static int getIntPref(Context context, String name, int def) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return prefs.getInt(name, def);
    }

    public static void animate(final ImageView imageView, final int images[], final int imageIndex, final boolean forever) {

        //imageView <-- The View which displays the images
        //images[] <-- Holds R references to the images to display
        //imageIndex <-- index of the first image to show in images[]
        //forever <-- If equals true then after the last image it starts all over again with the first image resulting in an infinite loop. You have been warned.

        int fadeInDuration = 5000; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 5000;

        imageView.setVisibility(View.INVISIBLE);    //Visible or invisible by default - this will apply when the animation ends
        imageView.setImageResource(images[imageIndex]);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        imageView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                if (images.length - 1 > imageIndex) {
                    animate(imageView, images, imageIndex + 1, forever); //Calls itself until it gets to the end of the array
                } else {
                    if (forever == true) {
                        animate(imageView, images, 0, forever);  //Calls itself to start the animation all over again in a loop if forever = true
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
            }
        });
    }

    public static void deletePlaylist(Context context, String playlistid) {
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {playlistid};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
        return;
    }


    public static void shareTheMusic(Context context, String songPath) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse("file://" + songPath);
        sharingIntent.setType("audio/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_tracks_using)));
    }


    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }

    public static ArrayList<Song> playPlaylist(long plid) {
        ArrayList<Song> songs = CursorHelper.getTracksForSelection("PLAYLISTS", "" + plid);
        return songs;
    }

    public static void deleteTracks(Context context, long[] list) {

        String[] cols = new String[]{MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};
        StringBuilder where = new StringBuilder();
        where.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            where.append(list[i]);
            if (i < list.length - 1) {
                where.append(",");
            }
        }
        where.append(")");
        Cursor c = query(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cols,
                where.toString(), null, null);

        if (c != null) {

            // step 1: remove selected tracks from the current playlist, as well
            // as from the album art cache
           /*     try {
                    c.moveToFirst();
                    while (! c.isAfterLast()) {
                        // remove from current playlist
                        long id = c.getLong(0);
                        sService.removeTrack(id);
                        // remove from album art cache
                        long artIndex = c.getLong(2);
                        synchronized(sArtCache) {
                            sArtCache.remove(artIndex);
                        }
                        c.moveToNext();
                    }
                } catch (RemoteException ex) {
                }*/

            // step 2: remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where.toString(), null);

            // step 3: remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                String name = c.getString(1);
                File f = new File(name);
                try {  // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

          /*  String message = context.getResources().getQuantityString(
                 //   R.plurals.NNNtracksdeleted, list.length, Integer.valueOf(list.length));*/

        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number of things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean deleteViaContentProvider(String fullname) {
        Uri uri = getFileUri(Common.getInstance(), fullname);
        if (uri == null) {
            return false;
        }
        try {
            ContentResolver resolver = Common.getInstance().getContentResolver();
            // change type to image, otherwise nothing will be deleted
            ContentValues contentValues = new ContentValues();
            int media_type = 1;
            contentValues.put("media_type", media_type);
            resolver.update(uri, contentValues, null, null);

            return resolver.delete(uri, null, null) > 0;
        } catch (Throwable e) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static Uri getFileUri(Context context, String fullname) {
        Uri uri = null;
        Cursor cursor = null;
        ContentResolver contentResolver = null;
        try {
            contentResolver = context.getContentResolver();
            if (contentResolver == null)
                return null;
            uri = MediaStore.Files.getContentUri("external");
            String[] projection = new String[2];
            projection[0] = "_id";
            projection[1] = "_data";
            String selection = "_data = ? ";    // this avoids SQL injection
            String[] selectionParams = new String[1];
            selectionParams[0] = fullname;
            String sortOrder = "_id";
            cursor = contentResolver.query(uri, projection, selection, selectionParams, sortOrder);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) // file present!
                    {
                        cursor.moveToFirst();
                        int dataColumn = cursor.getColumnIndex("_data");
                        String s = cursor.getString(dataColumn);
                        if (!s.equals(fullname))
                            return null;
                        int idColumn = cursor.getColumnIndex("_id");
                        long id = cursor.getLong(idColumn);
                        uri = MediaStore.Files.getContentUri("external", id);
                    } else // file isn't in the media database!
                    {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("_data", fullname);
                        uri = MediaStore.Files.getContentUri("external");
                        uri = contentResolver.insert(uri, contentValues);
                    }
                } catch (Throwable e) {
                    uri = null;
                } finally {
                    cursor.close();
                }
            }
        } catch (Throwable e) {
            uri = null;
        }
        return uri;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String[] getExtSdCardPaths() {
        List<String> paths = new ArrayList<>();
        for (File file : Common.getInstance().getExternalFilesDirs("external")) {
            if (file != null && !file.equals(Common.getInstance().getExternalFilesDir("external"))) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("asd", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        return paths.toArray(new String[paths.size()]);
    }

    public static void insertIntoPlayList(final Context context, final MenuItem item, final ArrayList<Song> data) {
        final long[] list = new long[data.size()];
        new AsyncTask<Void, Void, long[]>() {
            @Override
            protected long[] doInBackground(Void... params) {
                for (int i = 0; i < data.size(); i++) {
                    list[i] = data.get(i)._id;
                }
                return list;
            }

            @Override
            protected void onPostExecute(long[] longs) {
                super.onPostExecute(longs);
                long playlist = item.getIntent().getLongExtra("playlist", 0);
                MusicUtils.addToPlaylist(context, longs, playlist);
                String message = MusicUtils.makeLabel(context, R.plurals.Nsongs, longs.length) + " " + context.getString(R.string.added_to_playlist);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

    public static void deleteFile(AppCompatActivity activity, ArrayList<Song> files, OnTaskCompleted onTaskCompleted) {
        if (!MusicUtils.isKitkat() && MusicUtils.isFromSdCard(files.get(0)._path) && !MusicUtils.hasPermission()) {
            PermissionToEditSdCardDialog takePermissionDialog = new PermissionToEditSdCardDialog(activity);
            takePermissionDialog.show(activity.getSupportFragmentManager(), "PERMISSION_DIALOG");
        } else {
            MusicUtils.showConfirmationDialog(activity, files, onTaskCompleted);
        }
    }

    public static void deleteFile(Fragment fragment, ArrayList<Song> files, OnTaskCompleted onTaskCompleted) throws IndexOutOfBoundsException {
        if (files.size() == 0) throw new IndexOutOfBoundsException();
        if (!MusicUtils.isKitkat() && MusicUtils.isFromSdCard(files.get(0)._path) && !MusicUtils.hasPermission()) {
            PermissionToEditSdCardDialog takePermissionDialog = new PermissionToEditSdCardDialog(fragment);
            takePermissionDialog.show(fragment.getActivity().getSupportFragmentManager(), "PERMISSION_DIALOG");
        } else {
            MusicUtils.showConfirmationDialog((AppCompatActivity) fragment.getActivity(), files, onTaskCompleted);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean hasPermission() {
        List<UriPermission> uriPermission = Common.getInstance().getContentResolver().getPersistedUriPermissions();
        if (uriPermission != null && uriPermission.size() > 0) {
            return true;
        }
        return false;
    }

    public static void showConfirmationDialog(AppCompatActivity activity, ArrayList<Song> files, OnTaskCompleted onTaskCompleted) {
        DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog();
        deleteAlertDialog.setFiles(files);
        deleteAlertDialog.setTaskCompletionListener(onTaskCompleted);
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.add(deleteAlertDialog, "FRAGMENT_TAG");
        transaction.commitAllowingStateLoss();
    }

    public static void animate(final ImageView imageView, Drawable drawable1, Drawable drawable2) {
        Drawable[] layers = new Drawable[2];
        layers[0] = drawable1;
        layers[1] = drawable2;

        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(5000);
    }

    public static boolean isHTC() {
        return Build.PRODUCT.contains("HTC")
                || Build.PRODUCT.contains("J2")
                || Build.PRODUCT.contains("J7")
                || Build.PRODUCT.contains("J1");
    }


    public static void changeTabsFont(Context context, TabLayout mTabs) {
        Common common = (Common) context.getApplicationContext();
        ViewGroup vg = (ViewGroup) mTabs.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(TypefaceHelper.getTypeface(context, TypefaceHelper.FUTURA_BOLD));
                }
            }
        }
    }


    public static long[] getPlayListIds(ArrayList<Song> songs) {
        long[] longs = new long[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            longs[i] = songs.get(i)._id;
        }
        return longs;
    }


    public static boolean isEqualizerSupported() {
        int noOfBands = 0;
        int noOfPresents = 0;
        try {
            Equalizer equalizer = new Equalizer(0, 0);
            noOfBands = equalizer.getNumberOfBands();
            noOfPresents = equalizer.getNumberOfPresets();
            equalizer.release();
            equalizer = null;
        } catch (Exception e) {

        }

        return noOfBands > 0 && noOfPresents > 0 && !isHTC();

    }

    public static int getDPFromPixel(int pixel) {
        Resources r = Common.getInstance().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, r.getDisplayMetrics());
    }

    public static void applyFontForToolbarTitle(Activity context) {
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;

                if (tv.getText().equals(toolbar.getTitle())) {
                    tv.setTypeface(TypefaceHelper.getTypeface(context, TypefaceHelper.FUTURA_BOLD));
                    break;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isFromSdCard(String filepath) {
        String path1 = Environment.getExternalStorageDirectory().toString();
        if (filepath.startsWith(path1)) {
            return false;
        }
        return true;

    }

    public static String putBitmapInDiskCache(String artistId, String artistName, LastFmInterface lastFmInterface) {
        File cacheDir = new File(Common.getInstance().getCacheDir(), "artistThumbnails");
        Bitmap avatar = null;

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File cacheFile = new File(cacheDir, "" + artistId);
        try {
            if (!cacheFile.exists()) {
                cacheFile.createNewFile();
            } else {
                return "file://" + cacheFile.getPath();
            }

            Response<ArtistModel> responseBodyCall = lastFmInterface.getArtist(artistName).execute();
            if (responseBodyCall.isSuccessful()) {
                avatar = ImageLoader.getInstance().loadImageSync(responseBodyCall.body().artist.image.get(4).url);
            }

            FileOutputStream fos = new FileOutputStream(cacheFile);
            avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return "file://" + cacheFile.getPath();
        } catch (Exception e) {
            Logger.log("" + e.getCause());
            return "";
        }
    }


    public interface Defs {

        int ADD_TO_PLAYLIST = 1;
        int USE_AS_RINGTONE = 2;
        int PLAYLIST_SELECTED = 3;
        int NEW_PLAYLIST = 4;
        int SET_TIMER = 5;
        int GOTO = 7;
        int GOTO_SETTINGS = 8;
        int ALBUM = 9;
        int DELETE_ITEM = 10;
        int GERNE = 11;
        int ARTIST = 12;
        int ADD_TO_QUEUE = 15;
        int PLAY_NEXT = 16;
        int PLAY_THIS = 17;
        int RENAME_PLAYLIST = 19;
        int DELETE_PLAYLIST = 20;
        int PLAY = 21;
        int RECENTLY_ADDED_WEEK = 22;
        int SHARE_ITEM = 23;
        int CLEAR_FAVORITES = 24;
        int ADD_TO_FAVORITES = 25;
        int CLEAR_TOPTRACKS = 26;
        int RECENTLY_PLAYED = 27;
        int CLEAR_RECENTLY_PLAYED = 28;
        int A_B_REPEAT = 29;
        int EDIT_TAGS = 32;
        int REQUEST_WRITE_SETTINGS = 34;

    }


    public static Drawable createBlurredImageFromBitmap(Bitmap bitmap, Context context) {
        int inSampleSize = 9;
        android.support.v8.renderscript.RenderScript rs = android.support.v8.renderscript.RenderScript.create(context);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
        Bitmap blurTemplate = BitmapFactory.decodeStream(bis, null, options);
        final android.support.v8.renderscript.Allocation input = android.support.v8.renderscript.Allocation.createFromBitmap(rs, blurTemplate);
        final android.support.v8.renderscript.Allocation output = android.support.v8.renderscript.Allocation.createTyped(rs, input.getType());
        final android.support.v8.renderscript.ScriptIntrinsicBlur script = android.support.v8.renderscript.ScriptIntrinsicBlur.create(rs, android.support.v8.renderscript.Element.U8_4(rs));
        script.setRadius(8f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurTemplate);
        return new BitmapDrawable(context.getResources(), blurTemplate);
    }


    public static int getSongPosition() {
        return PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);
    }

}