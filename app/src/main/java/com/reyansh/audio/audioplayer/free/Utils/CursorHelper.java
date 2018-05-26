package com.reyansh.audio.audioplayer.free.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Database.DataBaseHelper;
import com.reyansh.audio.audioplayer.free.Interfaces.OnProgressUpdate;
import com.reyansh.audio.audioplayer.free.Models.Album;
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by REYANSH on 8/10/2017.
 */

public class CursorHelper {


    public static String getGenreCursorForSong(String songId) {
        String[] genresProjection = {
                MediaStore.Audio.Genres._ID
        };

        Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", Integer.parseInt(songId));
        Cursor cursor = Common.getInstance().getContentResolver().query(uri, genresProjection, null, null, null);
        if (cursor.moveToFirst()) {

            do {
                Logger.log("I " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres._ID)));
            } while (cursor.moveToNext());
        }
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Genres._ID));
        }
        return "";
    }


    public static ArrayList<Song> getTracksForSelection(String from, String selectionCondition) {
        Common mApp = (Common) Common.getInstance();

        String[] columns = {
                BaseColumns._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ARTIST_ID,

        };

        String selection = null;
        Uri uri = null;
        String selectionArgs[] = null;
        String sortBy = null;

        if (from.equalsIgnoreCase("ALBUMS")) {
            selection = "is_music=1 AND title != '' AND " + MediaStore.Audio.Media.ALBUM_ID + "=?";
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            selectionArgs = new String[]{selectionCondition};
            sortBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("ARTIST")) {
            selection = "is_music=1 AND title != '' AND " + MediaStore.Audio.Media.ARTIST_ID + "=?";
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            selectionArgs = new String[]{selectionCondition};
            sortBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("GENRES")) {
            uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(selectionCondition));
            sortBy = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("PLAYLISTS")) {
            if (selectionCondition.equalsIgnoreCase("-1")) {
                int x = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1) * (3600 * 24 * 7);
                selection = MediaStore.MediaColumns.DATE_ADDED + ">" + (System.currentTimeMillis() / 1000 - x) + " AND is_music=1";
                sortBy = MediaStore.Audio.Media.DATE_ADDED + " DESC";
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else if (selectionCondition.equalsIgnoreCase("-2")) {
                return mApp.getDBAccessHelper().getFavorites();
            } else if (selectionCondition.equalsIgnoreCase("-3")) {
                return mApp.getDBAccessHelper().getTopTracks();
            } else if (selectionCondition.equalsIgnoreCase("-4")) {
                return mApp.getDBAccessHelper().getRecentlyPlayed();
            } else {
                columns[0] = MediaStore.Audio.Playlists.Members.AUDIO_ID;
                uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(selectionCondition));
            }
        } else if (from.equalsIgnoreCase("SONGS")) {
            selection = "is_music=1 AND title != ''";
            uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            sortBy = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_ORDER, MediaStore.Audio.Media.DEFAULT_SORT_ORDER) +
                    PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_TYPE, " ASC");
        }


        ArrayList<Song> songs = new ArrayList<>();

        Cursor cursor = Common.getInstance().getContentResolver().query(
                uri,
                columns,
                selection,
                selectionArgs,
                sortBy);

        int audioIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

        if (!selectionCondition.equalsIgnoreCase("-1") && from.equalsIgnoreCase("PLAYLISTS")) {
            audioIndex = cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getLong(audioIndex),
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
        }
        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    public static ArrayList<Album> getAlbumsList() {

        String sort = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_DEFAULT)
                + PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.ASCENDING);

        String[] columns = {
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART
        };
        Cursor cursor = Common.getInstance().getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                columns,
                null,
                null,
                sort);

        ArrayList<Album> albums = new ArrayList<>();


        if (cursor != null && cursor.moveToFirst()) {
            do {
                Album album = new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                albums.add(album);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        return albums;

    }

    public static ArrayList<Album> getAlbumsForCursor(Cursor cursor) {
        ArrayList<Album> albums = new ArrayList();

        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                Album album = new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                albums.add(album);
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return albums;
    }

    public static ArrayList<Album> searchAlbums(Context context, String paramString) {
        return getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", new String[]{"%" + paramString + "%"}));
    }

    public static Cursor makeAlbumCursor(Context context, String selection, String[] paramArrayOfString) {
        String sort = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_DEFAULT)
                + PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.ASCENDING);
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, new String[]{
                "_id", "album", "artist", "album_art"}, selection, paramArrayOfString, sort);
        return cursor;
    }

    public static Boolean buildMusicLibrary(OnProgressUpdate onProgressUpdate) {

        int progress = 0;
        Common mApp = (Common) Common.getInstance().getApplicationContext();
        if (shouldBeScanned()) {
        } else {
            return false;
        }

        try {
            //Query to filter out the genre with no songs in them.
            saveEQPresets(mApp);
            saveTabTitles(new String[]{"ALBUMS", "ARTISTS", "SONGS", "GENRES", "PLAYLISTS", "DIRECTORY"});
            String query = "_id in (select genre_id from audio_genres_map where audio_id in (select _id from audio_meta where is_music != 0))";

            //Initialize the database transaction manually (improves performance).
            mApp.getDBAccessHelper().getWritableDatabase().beginTransaction();


            //Genre Projection.
            String[] columns = {
                    MediaStore.Audio.Genres._ID,
                    MediaStore.Audio.Genres.NAME,
            };


            Cursor cursor = Common.getInstance()
                    .getContentResolver()
                    .query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                            columns,
                            query,
                            null,
                            MediaStore.Audio.Genres.NAME);


            try {
                mApp.getDBAccessHelper().getWritableDatabase().delete(DataBaseHelper.GENRES_TABLE, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        ContentValues genre = new ContentValues();
                        genre.put(DataBaseHelper.GENRE_ID, cursor.getString(0));
                        genre.put(DataBaseHelper.GENRE_NAME, cursor.getString(1));

                        ArrayList<Album> albums = getAlbumsForSelection("GENRES", cursor.getString(0));
                        if (albums != null && albums.size() > 0) {
                            genre.put(DataBaseHelper.GENRE_ALBUM_ART, MusicUtils.getAlbumArtUri(albums.get(0)._Id).toString());
                            genre.put(DataBaseHelper.NO_OF_ALBUMS_IN_GENRE, "" + albums.size());
                        }
                        mApp.getDBAccessHelper().getWritableDatabase().insert(DataBaseHelper.GENRES_TABLE, null, genre);
                    } while (cursor.moveToNext());
                }


                String[] artistCols = {
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.Artists.ARTIST,
                        MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                };

                Cursor artistCursor = Common.getInstance()
                        .getContentResolver()
                        .query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                                artistCols,
                                null,
                                null,
                                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);

                mApp.getDBAccessHelper().getWritableDatabase().delete(DataBaseHelper.ARTIST_TABLE, null, null);

                if (artistCursor != null && artistCursor.moveToFirst()) {
                    if (onProgressUpdate != null)
                        onProgressUpdate.maxProgress(artistCursor.getCount());
                    String path = new File(Common.getInstance().getCacheDir(), "artistThumbnails").getAbsolutePath() + "/";
                    do {
                        ContentValues artist = new ContentValues();

                        artist.put(DataBaseHelper.ARTIST_ID, artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID)));
                        artist.put(DataBaseHelper.ARTIST_NAME, artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));
                        artist.put(DataBaseHelper.NO_OF_TRACKS_BY_ARTIST, artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)));
                        artist.put(DataBaseHelper.NO_OF_ALBUMS_BY_ARTIST, artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)));

                        ArrayList<Album> albums = getAlbumsForSelection("ARTIST", artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID)));

                        if (albums != null && albums.size() > 0) {
                            File cacheFile = new File(path + artistCursor.getString(artistCursor.getColumnIndex(MediaStore.Audio.Artists._ID)));
                            if (cacheFile.exists()) {
                                artist.put(DataBaseHelper.ARTIST_ALBUM_ART, "file://" + cacheFile.getAbsolutePath());
                            } else {
                                artist.put(DataBaseHelper.ARTIST_ALBUM_ART, MusicUtils.getAlbumArtUri(albums.get(0)._Id).toString());
                            }
                        }

                        mApp.getDBAccessHelper().getWritableDatabase().insert(DataBaseHelper.ARTIST_TABLE, null, artist);
                        if (onProgressUpdate != null)
                            onProgressUpdate.onProgressed(progress++);
                    } while (artistCursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.log("CAUSE " + e.getCause());
            } finally {
                mApp.getDBAccessHelper().getWritableDatabase().setTransactionSuccessful();
                mApp.getDBAccessHelper().getWritableDatabase().endTransaction();
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.FIRST_LAUNCH, false);
            }

            if (cursor != null) {
                cursor.close();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveTabTitles(String[] tabs) {
        Gson gson = new Gson();
        String jsonText = gson.toJson(tabs);
        PreferencesHelper.getInstance().put(PreferencesHelper.Key.TITLES, jsonText);
    }

    private static void saveEQPresets(Common mApp) {
        Cursor eqPresetsCursor = mApp.getDBAccessHelper().getAllEQPresets();

        //Check if this is the first startup (eqPresetsCursor.getCount() will be 0).
        if (eqPresetsCursor != null && eqPresetsCursor.getCount() == 0) {
            mApp.getDBAccessHelper().addNewEQPreset("Flat", 16, 16, 16, 16, 16, 16, 16, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Bass Only", 31, 31, 31, 0, 0, 0, 31, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Treble Only", 0, 0, 0, 31, 31, 31, 0, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Rock", 16, 18, 16, 17, 19, 20, 22, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Grunge", 13, 16, 18, 19, 20, 17, 13, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Metal", 12, 16, 16, 16, 20, 24, 16, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Dance", 14, 18, 20, 17, 16, 20, 23, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Country", 16, 16, 18, 20, 17, 19, 20, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Jazz", 16, 16, 18, 18, 18, 16, 20, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Speech", 14, 16, 17, 14, 13, 15, 16, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Classical", 16, 18, 18, 16, 16, 17, 18, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Blues", 16, 18, 19, 20, 17, 18, 16, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Opera", 16, 17, 19, 20, 16, 24, 18, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Swing", 15, 16, 18, 20, 18, 17, 16, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("Acoustic", 17, 18, 16, 19, 17, 17, 14, (short) 0, (short) 0, (short) 0);
            mApp.getDBAccessHelper().addNewEQPreset("New Age", 16, 19, 15, 18, 16, 16, 18, (short) 0, (short) 0, (short) 0);


        }
        //Close the cursor.
        if (eqPresetsCursor != null)
            eqPresetsCursor.close();
    }

    private static boolean shouldBeScanned() {
        if (PreferencesHelper.getInstance().getBoolean(PreferencesHelper.Key.FIRST_LAUNCH, true)) {
            return true;
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Common.getInstance());
            int scanAt = Integer.parseInt(sharedPreferences.getString("preference_key_scan_frequency", "5"));
            int launchCount = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.LAUNCH_COUNT, 0);
            if (scanAt == 5) {
                return false;
            } else if (scanAt == 0) {
                return true;
            } else if (scanAt == launchCount) {
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.LAUNCH_COUNT, 0);
                return true;
            } else {
                return false;
            }
        }
    }

    public static Cursor makeArtistCursor(Context context, String selection, String[] paramArrayOfString) {
        final String artistSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_ORDER);
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, new String[]{
                "_id",
                "artist",
                "number_of_albums",
                "number_of_tracks"}, selection, paramArrayOfString, artistSortOrder);
        return cursor;
    }

    public static ArrayList<Album> getAlbumsForSelection(String from, String selectionCondition) {

        String selection = null;
        Uri uri = null;
        String selectionArgs[] = null;
        String sortBy = null;

        if (from.equalsIgnoreCase("ALBUMS")) {
            selection = "is_music=1 AND title != '' AND " + MediaStore.Audio.Media.ALBUM_ID + "=?";
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
            selectionArgs = new String[]{selectionCondition};
            sortBy = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("ARTIST")) {
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", Long.parseLong(selectionCondition));
            sortBy = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("GENRES")) {
            uri = MediaStore.Audio.Albums.getContentUri("external");
            selection = "album_info._id IN "
                    + "(SELECT (audio_meta.album_id) album_id FROM audio_meta, audio_genres_map "
                    + "WHERE audio_genres_map.audio_id=audio_meta._id AND audio_genres_map.genre_id=?)";
            selectionArgs = new String[]{String.valueOf(selectionCondition)};
            sortBy = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        } else if (from.equalsIgnoreCase("PLAYLISTS")) {
            if (selectionCondition.equalsIgnoreCase("-1")) {
                int X = 18 * (3600 * 24 * 7);
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                selection = MediaStore.MediaColumns.DATE_ADDED + ">" + (System.currentTimeMillis() / 1000 - X);
                sortBy = MediaStore.Audio.Media.DATE_ADDED + " DESC";
            } else {
                uri = MediaStore.Audio.Playlists.Members.getContentUri("external", Long.parseLong(selectionCondition));
            }
        }


        ArrayList<Album> albums = new ArrayList<>();

        String[] columns = {
                BaseColumns._ID,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
        };


        Cursor cursor = Common.getInstance().getContentResolver().query(
                uri,
                columns,
                selection,
                selectionArgs,
                sortBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Album album = new Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                albums.add(album);
            } while (cursor.moveToNext());

        }
        if (cursor != null) {
            cursor.close();
        }
        return albums;
    }


    public interface Projections {
        String[] songProjection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION
        };
    }


}
