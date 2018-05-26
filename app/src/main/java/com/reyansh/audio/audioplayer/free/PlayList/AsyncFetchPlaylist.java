package com.reyansh.audio.audioplayer.free.PlayList;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.reyansh.audio.audioplayer.free.Models.Playlist;
import com.reyansh.audio.audioplayer.free.R;

import java.util.ArrayList;

/**
 * Created by Reyansh on 31/07/2016.
 */
public class AsyncFetchPlaylist extends AsyncTask<Void, Void, ArrayList<Playlist>> {
    private ArrayList<Playlist> mPlayList;
    private Cursor mCursor;
    private Context mContext;
    private PlaylistFragment mFragmentPlaylist;

    public AsyncFetchPlaylist(PlaylistFragment fragmentPlaylist) {
        mContext = fragmentPlaylist.getContext();
        mFragmentPlaylist = fragmentPlaylist;
        mPlayList = new ArrayList<>();
    }


    @Override
    protected ArrayList<Playlist> doInBackground(Void... params) {
        try {
            String[] columns = {
                    BaseColumns._ID,
                    MediaStore.Audio.Playlists._ID,
                    MediaStore.Audio.Playlists.NAME,
                    MediaStore.Audio.Playlists.DATA

            };
            mCursor = mContext.getContentResolver().query(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    columns,
                    null,
                    null,
                    MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);


            addDefaultPlayLists();
            if (mCursor != null && mCursor.moveToFirst()) {
                do {
                    Playlist song = new Playlist(mCursor.getLong(1),mCursor.getString(2));
                    mPlayList.add(song);
                } while (mCursor.moveToNext());
            }
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mPlayList;
    }

    @Override
    protected void onPostExecute(ArrayList<Playlist> hashMaps) {
        super.onPostExecute(hashMaps);
        mFragmentPlaylist.updateData(hashMaps);
    }

    private void addDefaultPlayLists() {

        Playlist recently = new Playlist(-1, mContext.getString(R.string.recentyl_added));
        mPlayList.add(0, recently);

        Playlist favorites = new Playlist(-2, mContext.getString(R.string.favorites));
        mPlayList.add(1, favorites);

        Playlist topTracks = new Playlist(-3, mContext.getString(R.string.top_played));
        mPlayList.add(2, topTracks);

        Playlist recentlyPlayed = new Playlist(-4, mContext.getString(R.string.recently_played));
        mPlayList.add(3, recentlyPlayed);

    }
}
