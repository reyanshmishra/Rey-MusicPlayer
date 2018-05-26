package com.reyansh.audio.audioplayer.free.Artist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.AsyncTasks.AsyncAddTo;
import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Dialogs.PlaylistDialog;
import com.reyansh.audio.audioplayer.free.Interfaces.OnScrolledListener;
import com.reyansh.audio.audioplayer.free.Interfaces.OnTaskCompleted;
import com.reyansh.audio.audioplayer.free.LauncherActivity.MainActivity;
import com.reyansh.audio.audioplayer.free.Models.Album;
import com.reyansh.audio.audioplayer.free.Models.Artist;
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.CursorHelper;
import com.reyansh.audio.audioplayer.free.Utils.HidingScrollListener;
import com.reyansh.audio.audioplayer.free.Utils.Logger;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Views.FastScroller;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
*The fragment class to display the list of artist in grid view.
*/
public class FragmentArtist extends Fragment implements MusicUtils.Defs, OnTaskCompleted {


    private ArrayList<Artist> mArtistList;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private FastScroller mFastScroller;
    private ArtistsAdapter mAdapter;
    private int mPosition;
    private View mView;
    private Common mApp;
    private OnScrolledListener mOnScrolledListener;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_album_layout, container, false);
        mContext = getContext();
        setHasOptionsMenu(true);

        mCompositeDisposable = new CompositeDisposable();
        mApp = (Common) mContext.getApplicationContext();
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mFastScroller = (FastScroller) mView.findViewById(R.id.fast_scroller);
        mFastScroller.setRecyclerView(mRecyclerView);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, Common.getNumberOfColms()));

        mAdapter = new ArtistsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                mOnScrolledListener.onScrolledUp();
            }

            @Override
            public void onShow() {
                mOnScrolledListener.onScrolledDown();
            }

        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeDisposable.add(Observable.fromCallable(() -> mApp.getDBAccessHelper().getAllArtist())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<ArrayList<Artist>>() {
                    @Override
                    public void onNext(ArrayList<Artist> hashMaps) {
                        mArtistList = hashMaps;
                        mAdapter.updateData(mArtistList);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.log("" + e.getCause());
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }


    public void onPopUpMenuClickListener(View v, int position) {
        final PopupMenu menu = new PopupMenu(mContext, v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getContext(), sub, 0);
        mPosition = position;
        ArrayList<Song> songs = CursorHelper.getTracksForSelection("ARTIST", "" + mArtistList.get(position)._artistId);
        if (checkIfAlbumsEmpty(songs, mPosition)) return;
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_album_play_next:
                    new AsyncAddTo(mArtistList.get(position)._artistName, false, songs).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(mArtistList.get(position)._artistName, true, songs).execute();
                    return true;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(songs));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, songs);
                    return true;
                case R.id.popup_album_delete:
                    try {
                        MusicUtils.deleteFile(FragmentArtist.this, songs, this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    return true;
                default:
                    break;
            }
            return false;
        });
        menu.inflate(R.menu.popup_album);
        menu.show();


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    mApp.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    ArrayList<Song> songs = CursorHelper.getTracksForSelection("ARTIST", "" + mArtistList.get(mPosition)._artistId);
                    try {
                        MusicUtils.deleteFile(FragmentArtist.this, songs, this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public boolean checkAlbumsEmpty(ArrayList<Album> albums, int pos) {
        if (albums.size() == 0) {
            mArtistList.remove(pos);
            mAdapter.updateData(mArtistList);
            Toast.makeText(mContext, R.string.no_albums_by_this_artist, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public boolean checkIfAlbumsEmpty(ArrayList<Song> songs, int pos) {
        if (songs.size() == 0) {
            mArtistList.remove(pos);
            mAdapter.updateData(mArtistList);
            Toast.makeText(mContext, R.string.no_albums_by_this_artist, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mOnScrolledListener = (OnScrolledListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnScrolledListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }


    @Override
    public void onSongDeleted() {
        onResume();
    }
}