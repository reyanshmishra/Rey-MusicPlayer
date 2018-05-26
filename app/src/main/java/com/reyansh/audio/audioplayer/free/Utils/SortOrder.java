package com.reyansh.audio.audioplayer.free.Utils;/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */


import android.provider.MediaStore;

import com.reyansh.audio.audioplayer.free.Database.DataBaseHelper;

/**
 * Holds all of the sort orders for each list type.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public final class SortOrder {

    /**
     * This class is never instantiated
     */
    private SortOrder() {
    }

    /**
     * Artist sort order entries.
     */
    public interface ArtistSortOrder {

        String ARTIST_NAME = DataBaseHelper.ARTIST_NAME;
        String ARTIST_NUMBER_OF_SONGS = DataBaseHelper.NO_OF_TRACKS_BY_ARTIST;
        String ARTIST_NUMBER_OF_ALBUMS = DataBaseHelper.NO_OF_ALBUMS_BY_ARTIST;
    }
    public interface GenreSortOrder {
        String GENRE_NAME = DataBaseHelper.GENRE_NAME;
        String GENRE_NUMBER_OF_ALBUMS = DataBaseHelper.NO_OF_ALBUMS_IN_GENRE;
    }

    /**
     * Album sort order entries.
     */
    public interface AlbumSortOrder {
        String ALBUM_DEFAULT = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        String ALBUM_NAME = MediaStore.Audio.Albums.ALBUM;
        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS;
        String ALBUM_ARTIST = MediaStore.Audio.Albums.ARTIST;
        String ALBUM_YEAR = MediaStore.Audio.Albums.FIRST_YEAR;
    }

    /**
     * Song sort order entries.
     */
    public interface SongSortOrder {
        String SONG_DEFAULT = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
        String SONG_DISPLAY_NAME = MediaStore.Audio.Media.DISPLAY_NAME;
        String SONG_TRACK_NO = MediaStore.Audio.Media.TRACK;
        String SONG_DURATION = MediaStore.Audio.Media.DURATION;
        String SONG_YEAR = MediaStore.Audio.Media.YEAR;
        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED;
        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;
        String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;
        String SONG_FILENAME = MediaStore.Audio.Media.DATA;
    }


}
