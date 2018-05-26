package com.reyansh.audio.audioplayer.free.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Models.Artist;
import com.reyansh.audio.audioplayer.free.Models.Genre;
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.Logger;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.reyansh.audio.audioplayer.free.Utils.SortOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Boom.db";
    public static final int DATABASE_VERSION = 1;
    public static final String RECENTLY_PLAYED_TABLE = "RecentlyPlayedTable";
    public static final String DATE = "date";
    public static final String SONGS_TABLE = "SongsTable";
    /**
     * Favorites Tables and song columns.
     */
    public static final String FAVORITES_TABLE = "FavoritesTable";
    public static final String SONG_ID = "songId";
    public static final String SONG_TITLE = "songTitle";
    public static final String SONG_ARTIST = "songArtist";
    public static final String SONG_DURATION = "songDuration";
    public static final String SONG_PATH = "songPath";
    public static final String SONG_ALBUM = "songAlbum";
    public static final String ALBUM_ID = "albumId";
    public static final String TRACK_NO = "tackNo";
    public static final String ARTIST_ID = "artistId";
    /**
     * Top tracks table.
     */
    public static final String TOP_TRACKS_TABLE = "TopTracksTable";
    public static final String SONG_COUNT = "songCount";
    //Equalizer presets table.
    public static final String EQUALIZER_PRESETS_TABLE = "EqualizerPresetsTable";
    public static final String PRESET_NAME = "preset_name";
    public static final String EQUALIZER_TABLE = "EqualizerTable";
    public static final String _ID = "_id";
    public static final String EQ_50_HZ = "eq_50_hz";
    public static final String EQ_130_HZ = "eq_130_hz";
    public static final String EQ_320_HZ = "eq_320_hz";
    public static final String EQ_800_HZ = "eq_800_hz";
    public static final String EQ_2000_HZ = "eq_2000_hz";
    public static final String EQ_5000_HZ = "eq_5000_hz";
    public static final String EQ_12500_HZ = "eq_12500_hz";
    public static final String VIRTUALIZER = "eq_virtualizer";
    public static final String BASS_BOOST = "eq_bass_boost";
    public static final String REVERB = "eq_reverb";
    public static final String VOLUME = "eq_volume";
    public static final String FILE_DIRECTORY_TABLE = "FileDirectoryTable";
    /**
     * Genre table and columns.
     */
    public static final String GENRES_TABLE = "GenresTable";
    public static final String GENRE_ID = "genreId";
    public static final String GENRE_NAME = "genreName";
    public static final String NO_OF_ALBUMS_IN_GENRE = "noOfAlbumsInGenre";
    public static final String GENRE_ALBUM_ART = "genreAlbumArt";
    /**
     * Artist table and columns.
     */


    public static final String ARTIST_TABLE = "ArtistTable";
    public static final String ARTIST_NAME = "artistName";
    public static final String NO_OF_ALBUMS_BY_ARTIST = "noOfAlbumsByArtist";
    public static final String NO_OF_TRACKS_BY_ARTIST = "noOfTracksByArtist";
    public static final String ARTIST_ALBUM_ART = "artistAlbumArt";
    /**
     * Tabs table this table used to rearraged the tabs on home activity.
     */
    public static final String TABS_TABLE = "TabsTable";
    public static final String TAB_NAME = "tabName";
    public static final String TAB_POSITION = "tabPosition";
    private static DataBaseHelper mDatabaseHelper;
    private Context mContext;
    private SQLiteDatabase mDatabase;


    private DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    public static synchronized DataBaseHelper getDatabaseHelper(Context context) {
        if (mDatabaseHelper == null)
            mDatabaseHelper = new DataBaseHelper(context.getApplicationContext());
        return mDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Equalizer table.
        String[] equalizerTableCols = {EQ_50_HZ, EQ_130_HZ,
                EQ_320_HZ, EQ_800_HZ, EQ_2000_HZ,
                EQ_5000_HZ, EQ_12500_HZ, VIRTUALIZER,
                BASS_BOOST, REVERB, VOLUME};

        String[] equalizerTableColTypes = {
                "TEXT", "TEXT",
                "TEXT", "TEXT",
                "TEXT", "TEXT",
                "TEXT", "TEXT",
                "TEXT", "TEXT",
                "TEXT"};

        String createEqualizerTable = buildCreateStatement(EQUALIZER_TABLE,
                equalizerTableCols,
                equalizerTableColTypes);

        //Equalizer presets table.
        String[] equalizerPresetsTableCols = {PRESET_NAME, EQ_50_HZ, EQ_130_HZ,
                EQ_320_HZ, EQ_800_HZ, EQ_2000_HZ,
                EQ_5000_HZ, EQ_12500_HZ, VIRTUALIZER,
                BASS_BOOST, REVERB};

        String[] equalizerPresetsTableColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT"};


        String createEqualizerPresetsTable = buildCreateStatement(EQUALIZER_PRESETS_TABLE,
                equalizerPresetsTableCols,
                equalizerPresetsTableColTypes);

        String[] favoritesTableCols = {
                SONG_ID, SONG_TITLE, SONG_ARTIST,
                SONG_ALBUM, SONG_DURATION, ALBUM_ID,
                ARTIST_ID, TRACK_NO, SONG_PATH};
        String[] favoritesColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT"};

        String createFavoritesTable = buildCreateStatement(FAVORITES_TABLE,
                favoritesTableCols,
                favoritesColTypes);

        String[] topPlayedTableCols = {
                SONG_ID, SONG_TITLE, SONG_ARTIST,
                SONG_ALBUM, SONG_DURATION, ALBUM_ID,
                ARTIST_ID, TRACK_NO, SONG_PATH,
                SONG_COUNT};

        String[] topPlayedColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT", "INTEGER"};

        String createTopPlayedTable = buildCreateStatement(TOP_TRACKS_TABLE,
                topPlayedTableCols,
                topPlayedColTypes);


        String[] genresTableCols = {GENRE_ID, GENRE_NAME,
                NO_OF_ALBUMS_IN_GENRE,
                GENRE_ALBUM_ART};
        String[] genresColTypes = {"TEXT", "TEXT", "TEXT", "TEXT"};

        String createGenresTable = buildCreateStatement(GENRES_TABLE,
                genresTableCols,
                genresColTypes);

        String[] recentlyPlayedTableCols = {
                SONG_ID, SONG_TITLE, SONG_ARTIST,
                SONG_ALBUM, SONG_DURATION, ALBUM_ID,
                ARTIST_ID, TRACK_NO, SONG_PATH, DATE};

        String[] recentlyPlayedColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT", "TEXT"};

        String createRecentlyPlayedTable = buildCreateStatement(RECENTLY_PLAYED_TABLE,
                recentlyPlayedTableCols,
                recentlyPlayedColTypes);


        String[] artistTableCols = {
                ARTIST_ID, ARTIST_NAME, NO_OF_ALBUMS_BY_ARTIST,
                NO_OF_TRACKS_BY_ARTIST,
                ARTIST_ALBUM_ART
        };

        String[] artistColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT",};

        String createArtistPlayedTable = buildCreateStatement(ARTIST_TABLE,
                artistTableCols,
                artistColTypes);


        String[] songsTableCols = {
                SONG_ID, SONG_TITLE, SONG_ARTIST,
                SONG_ALBUM, SONG_DURATION, ALBUM_ID,
                ARTIST_ID, TRACK_NO, SONG_PATH};
        String[] songsColTypes = {"TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT",
                "TEXT", "TEXT", "TEXT"};

        String createSongsTable = buildCreateStatement(SONGS_TABLE,
                songsTableCols,
                songsColTypes);


        //TABS table.
        String[] tabsTableCols = {TAB_NAME, TAB_POSITION};
        String[] tabsTableColTypes = {
                "TEXT", "INTEGER"};

        String createTabTable = buildCreateStatement(TABS_TABLE,
                tabsTableCols,
                tabsTableColTypes);


        db.execSQL(createEqualizerTable);
        db.execSQL(createEqualizerPresetsTable);
        db.execSQL(createFavoritesTable);
        db.execSQL(createTopPlayedTable);
        db.execSQL(createGenresTable);
        db.execSQL(createRecentlyPlayedTable);
        db.execSQL(createArtistPlayedTable);
        db.execSQL(createSongsTable);
        db.execSQL(createTabTable);

        Logger.log("EQ TABLE CREATED");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    @Override
    protected void finalize() {
        try {
            getDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized SQLiteDatabase getDatabase() {
        if (mDatabase == null)
            mDatabase = getWritableDatabase();
        return mDatabase;
    }

    public ArrayList<HashMap<String, Integer>> getTabs() {
        ArrayList<HashMap<String, Integer>> hashMaps = new ArrayList<>();
        String query = "SELECT * FROM " + TABS_TABLE;
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Integer> stringIntegerHashMap = new HashMap<>();
                    stringIntegerHashMap.put(cursor.getString(cursor.getColumnIndex(TAB_NAME)), cursor.getInt(cursor.getColumnIndex(TAB_POSITION)));
                    hashMaps.add(stringIntegerHashMap);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return hashMaps;

        } else {
            String[] tabs = Common.getInstance().getResources().getStringArray(R.array.fragments_titles);

            for (int i = 0; i < tabs.length; i++) {
                ContentValues values = new ContentValues();
                values.put(TAB_NAME, tabs[i]);
                values.put(TAB_POSITION, i);
                try {
                    getDatabase().insertOrThrow(TABS_TABLE, null, values);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return getTabs();
        }
    }


    public int[] getEQValues() {
        /*String[] columnsToReturn = {
                EQ_50_HZ, EQ_130_HZ,
                EQ_320_HZ, EQ_800_HZ,
                EQ_2000_HZ, EQ_5000_HZ,
                EQ_12500_HZ, VIRTUALIZER,
                BASS_BOOST, REVERB,
                VOLUME};
*/
        String query = "SELECT * FROM " + EQUALIZER_TABLE;

        Cursor cursor = getDatabase().rawQuery(query, null);
        int[] eqValues = new int[12];
        Logger.log("COUNT " + cursor.getCount());
        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            eqValues[0] = cursor.getInt(cursor.getColumnIndex(EQ_50_HZ));
            eqValues[1] = cursor.getInt(cursor.getColumnIndex(EQ_130_HZ));
            eqValues[2] = cursor.getInt(cursor.getColumnIndex(EQ_320_HZ));
            eqValues[3] = cursor.getInt(cursor.getColumnIndex(EQ_800_HZ));
            eqValues[4] = cursor.getInt(cursor.getColumnIndex(EQ_2000_HZ));
            eqValues[5] = cursor.getInt(cursor.getColumnIndex(EQ_5000_HZ));
            eqValues[6] = cursor.getInt(cursor.getColumnIndex(EQ_12500_HZ));
            eqValues[7] = cursor.getInt(cursor.getColumnIndex(VIRTUALIZER));
            eqValues[8] = cursor.getInt(cursor.getColumnIndex(BASS_BOOST));
            eqValues[9] = cursor.getInt(cursor.getColumnIndex(REVERB));
            eqValues[10] = cursor.getInt(cursor.getColumnIndex(VOLUME));
            eqValues[11] = 1; //The song id exists in the EQ table.

            cursor.close();

        } else {
            eqValues[0] = 16;
            eqValues[1] = 16;
            eqValues[2] = 16;
            eqValues[3] = 16;
            eqValues[4] = 16;
            eqValues[5] = 16;
            eqValues[6] = 16;
            eqValues[7] = 0;
            eqValues[8] = 0;
            eqValues[9] = 0;
            eqValues[10] = 100;
            eqValues[11] = 0; //The song id doesn't exist in the EQ table.

        }

        return eqValues;
    }


    /**
     * Constructs a fully formed CREATE statement using the input
     * parameters.
     */

    private String buildCreateStatement(String tableName, String[] columnNames, String[] columnTypes) {
        String createStatement = "";
        if (columnNames.length == columnTypes.length) {
            createStatement += "CREATE TABLE IF NOT EXISTS " + tableName + "("
                    +
                    _ID + " INTEGER PRIMARY KEY, ";

            for (int i = 0; i < columnNames.length; i++) {

                if (i == columnNames.length - 1) {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ")";
                } else {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ", ";
                }
            }
        }
        return createStatement;
    }

    /**
     * Saves a song's equalizer/audio effect settings to the database.
     */

    public void addEQValues(int fiftyHertz,
                            int oneThirtyHertz,
                            int threeTwentyHertz,
                            int eightHundredHertz,
                            int twoKilohertz,
                            int fiveKilohertz,
                            int twelvePointFiveKilohertz,
                            int virtualizer,
                            int bassBoost,
                            int reverb,
                            int volume) {

        ContentValues values = new ContentValues();
        values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, oneThirtyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
        values.put(VIRTUALIZER, virtualizer);
        values.put(BASS_BOOST, bassBoost);
        values.put(REVERB, reverb);
        values.put(VOLUME, volume);


        try {
            getDatabase().insertOrThrow(EQUALIZER_TABLE, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * Updates the equalizer/audio effects for the specified song.
     */
    public void updateSongEQValues(
            int fiftyHertz,
            int oneThirtyHertz,
            int threeTwentyHertz,
            int eightHundredHertz,
            int twoKilohertz,
            int fiveKilohertz,
            int twelvePointFiveKilohertz,
            int virtualizer,
            int bassBoost,
            int reverb, int volume) {

        ContentValues values = new ContentValues();
        values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, oneThirtyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
        values.put(VIRTUALIZER, virtualizer);
        values.put(BASS_BOOST, bassBoost);
        values.put(REVERB, reverb);
        values.put(VOLUME, volume);

        Logger.log("Updated" + getDatabase().update(EQUALIZER_TABLE, values, null, null));

    }


    /***********************************************************
     * EQUALIZER PRESETS TABLE METHODS.
     ***********************************************************/

    /**
     * Adds a new EQ preset to the table.
     */
    public void addNewEQPreset(String presetName,
                               int fiftyHertz,
                               int oneThirtyHertz,
                               int threeTwentyHertz,
                               int eightHundredHertz,
                               int twoKilohertz,
                               int fiveKilohertz,
                               int twelvePointFiveKilohertz,
                               short virtualizer,
                               short bassBoost,
                               short reverb) {

        ContentValues values = new ContentValues();
        values.put(PRESET_NAME, presetName);
        values.put(EQ_50_HZ, fiftyHertz);
        values.put(EQ_130_HZ, oneThirtyHertz);
        values.put(EQ_320_HZ, threeTwentyHertz);
        values.put(EQ_800_HZ, eightHundredHertz);
        values.put(EQ_2000_HZ, twoKilohertz);
        values.put(EQ_5000_HZ, fiveKilohertz);
        values.put(EQ_12500_HZ, twelvePointFiveKilohertz);
        values.put(VIRTUALIZER, virtualizer);
        values.put(BASS_BOOST, bassBoost);
        values.put(REVERB, reverb);

        getDatabase().insert(EQUALIZER_PRESETS_TABLE, null, values);

    }

    /**
     * Returns a cursor with all EQ presets in the table.
     */
    public Cursor getAllEQPresets() {
        String query = "SELECT * FROM " + EQUALIZER_PRESETS_TABLE;
        return getDatabase().rawQuery(query, null);
    }


    /**
     * Add songs to favorites table if it is already there remove it.
     */
    private boolean isAlreadyInFavorites(long songId) {
        String rawQuery = "SELECT DISTINCT " + SONG_ID + " FROM " + FAVORITES_TABLE + " WHERE " + SONG_ID + "=" + "'"
                + songId + "'";
        Cursor cursor = getDatabase().rawQuery(rawQuery, null);
        return !(cursor != null && cursor.getCount() == 0);
    }


    public void addToFavorites(Song song) {
        ContentValues values = new ContentValues();
        values.put(SONG_ID, song._id);
        values.put(SONG_TITLE, song._title);
        values.put(SONG_ARTIST, song._artist);
        values.put(SONG_DURATION, song._duration);
        values.put(SONG_PATH, song._path);
        values.put(SONG_ALBUM, song._album);
        values.put(ALBUM_ID, song._albumId);
        values.put(TRACK_NO, song._trackNumber);
        values.put(ARTIST_ID, song._artistId);


        if (!isAlreadyInFavorites(song._id)) {
            getDatabase().insertOrThrow(FAVORITES_TABLE, null, values);
            Toast.makeText(mContext, R.string.song_added_to_favorites_playlist, Toast.LENGTH_SHORT).show();
        } else {
            getDatabase().delete(FAVORITES_TABLE, SONG_ID + "= " + song._id, null);
            Toast.makeText(mContext, R.string.song_removed_from_favorites_playlist, Toast.LENGTH_SHORT).show();
        }
    }


    public void saveQueue(ArrayList<Song> songs) {
        getDatabase().beginTransaction();
        getDatabase().delete(SONGS_TABLE, null, null);
        for (int i = 0; i < songs.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(SONG_ID, songs.get(i)._id);
            values.put(SONG_TITLE, songs.get(i)._title);
            values.put(SONG_ARTIST, songs.get(i)._artist);
            values.put(SONG_DURATION, songs.get(i)._duration);
            values.put(SONG_PATH, songs.get(i)._path);
            values.put(SONG_ALBUM, songs.get(i)._album);
            values.put(ALBUM_ID, songs.get(i)._albumId);
            values.put(TRACK_NO, songs.get(i)._trackNumber);
            values.put(ARTIST_ID, songs.get(i)._artistId);
            getDatabase().insert(SONGS_TABLE, null, values);
        }
        getDatabase().setTransactionSuccessful();
        getDatabase().endTransaction();
    }


    public ArrayList<Song> getQueue() {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + SONGS_TABLE, null);
        if (cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SONG_ALBUM)),
                        cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_ARTIST)),
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_PATH)),
                        cursor.getInt(cursor.getColumnIndex(TRACK_NO)),
                        cursor.getLong(cursor.getColumnIndex(SONG_DURATION))
                );

                songs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }


    public void addToRecentlyPlayed(Song song) {
        ContentValues values = new ContentValues();
        values.put(SONG_ID, song._id);
        values.put(SONG_TITLE, song._title);
        values.put(SONG_ARTIST, song._artist);
        values.put(SONG_DURATION, song._duration);
        values.put(SONG_PATH, song._path);
        values.put(SONG_ALBUM, song._album);
        values.put(ALBUM_ID, song._albumId);
        values.put(TRACK_NO, song._trackNumber);
        values.put(ARTIST_ID, song._artistId);

        values.put(DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new java.util.Date()));
        getDatabase().insertOrThrow(RECENTLY_PLAYED_TABLE, null, values);
    }


    public ArrayList<Song> getRecentlyPlayed() {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = getDatabase().query(RECENTLY_PLAYED_TABLE, new String[]{"*"}, null, null, null, null, "datetime(date) DESC");
        if (cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SONG_ALBUM)),
                        cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_ARTIST)),
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_PATH)),
                        cursor.getInt(cursor.getColumnIndex(TRACK_NO)),
                        cursor.getLong(cursor.getColumnIndex(SONG_DURATION))
                );

                songs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }


    public ArrayList<Song> getFavorites() {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + FAVORITES_TABLE + " limit 40", null);
        if (cursor.moveToFirst()) {
            do {


                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SONG_ALBUM)),
                        cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_ARTIST)),
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_PATH)),
                        cursor.getInt(cursor.getColumnIndex(TRACK_NO)),
                        cursor.getLong(cursor.getColumnIndex(SONG_DURATION))
                );

                songs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }


    /**
     * Just remove songs from favorites table.
     */

    public void removeFromFavorites(Long songId) {
        getDatabase().delete(FAVORITES_TABLE, SONG_ID + "= " + songId, null);
    }


    /**
     * Remove song from the top tacks playlist.
     */

    public void removeFromTopTracks(Long songId) {
        getDatabase().delete(TOP_TRACKS_TABLE, "songId= " + songId, null);
    }


    /**
     * Insert how many times the current song is played and based on this count,
     * retrieve them and show into the top played list.
     */

    public void insertSongCount(Song song) {
        ContentValues values = new ContentValues();
        Cursor cursor = getDatabase().rawQuery("SELECT * FROM " + TOP_TRACKS_TABLE + " WHERE " + SONG_ID + "= " + song._id, null);

        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(SONG_COUNT)) != null) {
                int songCount = cursor.getInt(1) + 1;
                values.put(SONG_ID, song._id);
                values.put(SONG_TITLE, song._title);
                values.put(SONG_ARTIST, song._artist);
                values.put(SONG_DURATION, song._duration);
                values.put(SONG_PATH, song._path);
                values.put(SONG_ALBUM, song._album);
                values.put(ALBUM_ID, song._albumId);
                values.put(TRACK_NO, song._trackNumber);
                values.put(ARTIST_ID, song._artistId);
                values.put(SONG_COUNT, songCount);
                getDatabase().update(TOP_TRACKS_TABLE, values, SONG_ID + "= " + song._id, null);
            }
        } else {
            values.put(SONG_ID, song._id);
            values.put(SONG_TITLE, song._title);
            values.put(SONG_ARTIST, song._artist);
            values.put(SONG_DURATION, song._duration);
            values.put(SONG_PATH, song._path);
            values.put(SONG_ALBUM, song._album);
            values.put(ALBUM_ID, song._albumId);
            values.put(TRACK_NO, song._trackNumber);
            values.put(ARTIST_ID, song._artistId);
            values.put(SONG_COUNT, 0);
            try {
                getDatabase().insertOrThrow(TOP_TRACKS_TABLE, null, values);
            } catch (Exception e) {
            }
        }
    }


    /**
     * Get all the top played tracks from the database.
     */

    public ArrayList<Song> getTopTracks() {
        ArrayList<Song> songs = new ArrayList<>();
        Cursor cursor = getDatabase().query(TOP_TRACKS_TABLE, new String[]{"*"}, null, null, null, null, SONG_COUNT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Song song = new Song(
                        cursor.getLong(cursor.getColumnIndex(SONG_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_TITLE)),
                        cursor.getString(cursor.getColumnIndex(SONG_ALBUM)),
                        cursor.getLong(cursor.getColumnIndex(ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_ARTIST)),
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(SONG_PATH)),
                        cursor.getInt(cursor.getColumnIndex(TRACK_NO)),
                        cursor.getLong(cursor.getColumnIndex(SONG_DURATION))
                );

                songs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }


    public ArrayList<Genre> getAllGenres() {
        ArrayList<Genre> genres = new ArrayList<>();

        String query = "SELECT * FROM " + GENRES_TABLE + " ORDER BY " +
                PreferencesHelper.getInstance().getString(PreferencesHelper.Key.GENRE_SORT_ORDER, GENRE_NAME)
                + PreferencesHelper.getInstance().getString(PreferencesHelper.Key.GENRE_SORT_TYPE, Constants.ASCENDING);

        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Genre genre = new Genre(cursor.getLong(cursor.getColumnIndex(GENRE_ID)),
                        cursor.getString(cursor.getColumnIndex(GENRE_NAME)),
                        cursor.getString(cursor.getColumnIndex(GENRE_ALBUM_ART)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_ALBUMS_IN_GENRE)));
                genres.add(genre);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return genres;
    }

    public ArrayList<Genre> searchGenre(String name) {
        ArrayList<Genre> genres = new ArrayList<>();
        String query = "SELECT * FROM " + GENRES_TABLE + " WHERE " + GENRE_NAME + " LIKE '%" + name + "%'";
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Genre genre = new Genre(cursor.getLong(cursor.getColumnIndex(GENRE_ID)),
                        cursor.getString(cursor.getColumnIndex(GENRE_NAME)),
                        cursor.getString(cursor.getColumnIndex(GENRE_ALBUM_ART)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_ALBUMS_IN_GENRE)));
                genres.add(genre);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return genres;
    }


    public ArrayList<Artist> getAllArtist() {
        ArrayList<Artist> artists = new ArrayList<>();
        String query = "SELECT * FROM " + ARTIST_TABLE + " ORDER BY " +
                PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NAME)
                + PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.ASCENDING);
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Artist artist = new Artist(
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(ARTIST_NAME)),
                        cursor.getString(cursor.getColumnIndex(ARTIST_ALBUM_ART)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_TRACKS_BY_ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_ALBUMS_BY_ARTIST)));

                artists.add(artist);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artists;
    }


    public ArrayList<Artist> searchArtist(String name) {
        ArrayList<Artist> artists = new ArrayList<>();
        String query = "SELECT * FROM " + ARTIST_TABLE + " WHERE " + ARTIST_NAME + " LIKE '%" + name + "%'"
                + " ORDER BY " +
                PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NAME)
                + PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.ASCENDING);
        Cursor cursor = getDatabase().rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Artist artist = new Artist(
                        cursor.getLong(cursor.getColumnIndex(ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(ARTIST_NAME)),
                        cursor.getString(cursor.getColumnIndex(ARTIST_ALBUM_ART)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_TRACKS_BY_ARTIST)),
                        cursor.getInt(cursor.getColumnIndex(NO_OF_ALBUMS_BY_ARTIST)));

                artists.add(artist);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return artists;
    }


    public void updateArtistAlbumArt(long artistId, String artistArtUrl) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ARTIST_ALBUM_ART, artistArtUrl);
        getDatabase().update(ARTIST_TABLE, contentValues, ARTIST_ID + "= ?", new String[]{"" + artistId});
    }

    public void updateGenreTable(String genreId) {
        Cursor cursor = Common
                .getInstance()
                .getContentResolver()
                .query(MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genreId)),
                        new String[]{MediaStore.Audio.Media._ID,
                        }, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            getDatabase().delete(GENRES_TABLE, GENRE_ID + "= " + genreId, null);
        }
    }

    public void updateArtist(long artistId) {
        Cursor cursor = MusicUtils.makeArtistSongCursor(Common.getInstance(), artistId);
        if (cursor == null || cursor.getCount() == 0) {
            getDatabase().delete(ARTIST_TABLE, ARTIST_ID + "= " + artistId, null);
        }
    }

}
