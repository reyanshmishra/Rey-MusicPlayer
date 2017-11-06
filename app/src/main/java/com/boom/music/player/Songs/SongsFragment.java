package com.boom.music.player.Songs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Interfaces.OnAdapterItemClicked;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.TagEditor.Id3TagEditorActivity;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.SortOrder;
import com.boom.music.player.Views.DividerItemDecoration;
import com.boom.music.player.Views.FastScroller;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Reyansh on 30/07/2016.
 */
public class SongsFragment extends Fragment implements MusicUtils.Defs, OnAdapterItemClicked, OnTaskCompleted {

    private ArrayList<Song> mSongList;
    private OnScrolledListener mOnScrolledListener;
    private RecyclerView mRecyclerView;

    private SongsAdapter mAdapter;
    private Context mContext;

    private Common mApp;
    private FastScroller mFastScroller;
    private int mSelectedPosition;
    private View mView;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_album_layout, container, false);
        mContext = getContext();
        setHasOptionsMenu(true);
        mCompositeDisposable = new CompositeDisposable();
        mApp = (Common) mContext.getApplicationContext();
        mSongList = new ArrayList<>();
        mFastScroller = (FastScroller) mView.findViewById(R.id.fast_scroller);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFastScroller.setRecyclerView(mRecyclerView);
        mAdapter = new SongsAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, 85, 20));

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


    public void shuffleSongs() {
        mApp.getPlayBackStarter().shuffleUp(mSongList);
    }

    private void loadData() {
        mCompositeDisposable.add(Observable.fromCallable(() -> CursorHelper.getTracksForSelection("SONGS", ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<Song>>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ArrayList<Song> data) {
                        mSongList.clear();
                        mSongList.addAll(data);
                        mAdapter.update(mSongList);
                    }
                }));
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    mApp.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(mSongList.get(mSelectedPosition));
                    try {
                        MusicUtils.deleteFile(SongsFragment.this, song, SongsFragment.this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constants.EDIT_TAGS:
                if (resultCode == Activity.RESULT_OK) {
                    onResume();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        loadData();
        super.onResume();
    }

    @Override
    public void OnPopUpMenuClicked(View view, int position) {
        mSelectedPosition = position;
        PopupMenu menu = new PopupMenu(getActivity(), view);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getContext(), sub, 0);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_song_play_next:
                    new AsyncAddTo(mSongList.get(position)._title, false, mSongList.get(position)).execute();
                    break;
                case R.id.popup_song_addto_queue:
                    new AsyncAddTo(mSongList.get(position)._title, true, mSongList.get(position)).execute();
                    break;
                case R.id.popup_song_add_to_favs:
                    mApp.getDBAccessHelper().addToFavorites(mSongList.get(position));
                    break;
                case R.id.popup_song_delete:
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(mSongList.get(mSelectedPosition));
                    try {
                        MusicUtils.deleteFile(SongsFragment.this, song, SongsFragment.this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.popup_song_use_as_phone_ringtone:
                    MusicUtils.setRingtone((AppCompatActivity) getActivity(), mSongList.get(mSelectedPosition)._id);
                    break;
                case R.id.popup_song_share:
                    MusicUtils.shareTheMusic(SongsFragment.this.getActivity(), mSongList.get(mSelectedPosition)._path);
                    break;
                case R.id.popup_edit_songs_tags:
                    Intent intent = new Intent(getActivity(), Id3TagEditorActivity.class);
                    intent.putExtra("SONG_PATH", mSongList.get(mSelectedPosition)._path);
                    intent.putExtra("ALBUM_ID", mSongList.get(mSelectedPosition)._albumId);
                    startActivityForResult(intent, Constants.EDIT_TAGS);
                    break;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", new long[]{mSongList.get(mSelectedPosition)._id});
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    long[] list = new long[]{mSongList.get(mSelectedPosition)._id};
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getContext(), list, playlist);
                    return true;
            }
            return false;
        });
        menu.inflate(R.menu.popup_song);
        menu.show();
    }

    @Override
    public void OnShuffledClicked() {
        shuffleSongs();
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
        mCompositeDisposable.dispose();
        mOnScrolledListener = null;
    }

    public void launchNowPlaying() {
        mApp.getPlayBackStarter().launchNowPlaying(mSongList);
    }


    @Override
    public void onSongDeleted() {
        onResume();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_song, menu);

        if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
            menu.findItem(R.id.sort_song_type).setChecked(true);
        } else {
            menu.findItem(R.id.sort_song_type).setChecked(false);
        }

        String songSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_DEFAULT);

        if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DEFAULT)) {
            menu.findItem(R.id.sort_song_default).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DISPLAY_NAME)) {
            menu.findItem(R.id.sort_song_name).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_TRACK_NO)) {
            menu.findItem(R.id.sort_song_track_no).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DURATION)) {
            menu.findItem(R.id.sort_song_duration).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_YEAR)) {
            menu.findItem(R.id.sort_song_year).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_DATE)) {
            menu.findItem(R.id.sort_song_date_added).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_ALBUM)) {
            menu.findItem(R.id.sort__song_album_name).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_ARTIST)) {
            menu.findItem(R.id.sort_song_artist_name).setChecked(true);
        } else if (songSortOrder.equalsIgnoreCase(SortOrder.SongSortOrder.SONG_FILENAME)) {
            menu.findItem(R.id.sort_song_file_name).setChecked(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_song_default:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_DEFAULT);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_DISPLAY_NAME);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_track_no:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_TRACK_NO);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_duration:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_DURATION);

                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_year:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_YEAR);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_date_added:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_DATE);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort__song_album_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_ALBUM);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_artist_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_ARTIST);

                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_file_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_FILENAME);
                onResume();
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.sort_song_type:
                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.SONG_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_TYPE, Constants.DESCENDING);
                } else {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_SORT_TYPE, Constants.ASCENDING);
                }

                onResume();
                getActivity().invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
