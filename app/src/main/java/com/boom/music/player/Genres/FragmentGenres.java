package com.boom.music.player.Genres;

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

import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Album;
import com.boom.music.player.Models.Genre;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Views.FastScroller;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.boom.music.player.Utils.Constants.HEADER_TITLE;


public class FragmentGenres extends Fragment implements MusicUtils.Defs, OnTaskCompleted {

    private ArrayList<Genre> mGenres;
    private RecyclerView mRecyclerView;
    private FastScroller mFastScroller;
    private AdapterGenres mAdapter;
    private Context mContext;
    private int mPosition;
    private Common mApp;
    private View view;
    private OnScrolledListener mOnScrolledListener;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_layout, container, false);
        mContext = getContext();
        setHasOptionsMenu(true);
        mApp = (Common) mContext.getApplicationContext();
        mFastScroller = (FastScroller) view.findViewById(R.id.fast_scroller);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, Common.getNumberOfColms()));

        mCompositeDisposable = new CompositeDisposable();
        mAdapter = new AdapterGenres(this);

        mFastScroller.setRecyclerView(mRecyclerView);
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
        return view;
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
    public void onResume() {
        super.onResume();
        mCompositeDisposable
                .add(Observable.fromCallable(() -> mApp.getDBAccessHelper().getAllGenres())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<ArrayList<Genre>>() {
                            @Override
                            public void onNext(ArrayList<Genre> hashMaps) {
                                mGenres = hashMaps;
                                mAdapter.updateData(hashMaps);
                                mAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        }));
    }


    public void onPopUpMenuClickListener(View v, int position) {
        final PopupMenu menu = new PopupMenu(mContext, v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getContext(), sub, 0);
        mPosition = position;
        ArrayList<Song> songs = CursorHelper.getTracksForSelection("GENRES", "" + mGenres.get(mPosition)._genreId);
        if (checkIfSongsEmpty(songs, position)) return;

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_album_play_next:
                    new AsyncAddTo(HEADER_TITLE, false, songs).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(HEADER_TITLE, true, songs).execute();
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
                        MusicUtils.deleteFile(FragmentGenres.this, songs, this);
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
                    try {
                        MusicUtils.deleteFile(FragmentGenres.this, CursorHelper.getTracksForSelection("GENRES", "" + mGenres.get(mPosition)._genreId), this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    public boolean checkIfAlbumsEmpty(ArrayList<Album> albums, int pos) {
        if (albums.size() == 0) {
            mGenres.remove(pos);
            mAdapter.updateData(mGenres);
            Toast.makeText(mContext, R.string.no_songs_in_this_album, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public boolean checkIfSongsEmpty(ArrayList<Song> songs, int pos) {
        if (songs.size() == 0) {
            mGenres.remove(pos);
            mAdapter.updateData(mGenres);
            Toast.makeText(mContext, R.string.no_songs_in_this_album, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


    @Override
    public void onSongDeleted() {
        mGenres.remove(mPosition);
        mAdapter.updateData(mGenres);
    }
}
