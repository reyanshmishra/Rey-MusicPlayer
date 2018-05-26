package com.reyansh.audio.audioplayer.free.Album;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.CursorHelper;
import com.reyansh.audio.audioplayer.free.Utils.HidingScrollListener;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Views.FastScroller;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


/**
*This is {@link Fragment} class to display the list of fragments available in the phone.
*/

public class AlbumFragment extends Fragment implements MusicUtils.Defs, OnTaskCompleted {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private ArrayList<Album> mAlbums;
    private AlbumsAdapter mAdapter;
    private FastScroller mFastScroller;
    private Common mApp;
    private int mPosition;
    private View mView;
    private OnScrolledListener mOnScrolledListener;
    private CompositeDisposable mCompositeDisposable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_album_layout, container, false);
        mContext = getContext();
        setHasOptionsMenu(true);
        mCompositeDisposable = new CompositeDisposable();
        mAlbums = new ArrayList<>();
        mAdapter = new AlbumsAdapter(this);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mFastScroller = (FastScroller) mView.findViewById(R.id.fast_scroller);

        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, Common.getNumberOfColms()));

        mFastScroller.setRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mAdapter);
        mApp = (Common) mContext.getApplicationContext();

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
        loadAlbums();
        super.onResume();
    }

    public void onPopUpMenuClickListener(View v, final int position) {
        final PopupMenu menu = new PopupMenu(mContext, v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getContext(), sub, 0);
        mPosition = position;
        ArrayList<Song> songs = CursorHelper.getTracksForSelection("ALBUMS", "" + mAlbums.get(position)._Id);

        if (checkIfAlbumsEmpty(songs, position)) return;

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_album_play_next:
                    new AsyncAddTo(mAlbums.get(position)._albumName, false, songs).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(mAlbums.get(position)._albumName, true, songs).execute();
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
                        MusicUtils.deleteFile(AlbumFragment.this, songs, AlbumFragment.this);
                    } catch (IndexOutOfBoundsException e) {
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

    public boolean checkIfAlbumsEmpty(ArrayList<Song> songs, int pos) {
        if (songs.size() == 0) {
            mAlbums.remove(pos);
            mAdapter.updateData(mAlbums);
            Toast.makeText(mContext, R.string.no_songs_in_this_album, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    mApp.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    try {
                        MusicUtils.deleteFile(AlbumFragment.this, CursorHelper.getTracksForSelection("ALBUMS", "" + mAlbums.get(mPosition)._Id), AlbumFragment.this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
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
        mCompositeDisposable.clear();
        mCompositeDisposable.dispose();
    }


    @Override
    public void onSongDeleted() {
        mAlbums.remove(mPosition);
        mAdapter.updateData(mAlbums);
    }

    private void loadAlbums() {
        mCompositeDisposable.add(Observable.fromCallable(() -> CursorHelper.getAlbumsList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<Album>>() {
                    @Override
                    public void onNext(ArrayList<Album> data) {
                        mAlbums = data;
                        mAdapter.updateData(data);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("FAILED", "" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}