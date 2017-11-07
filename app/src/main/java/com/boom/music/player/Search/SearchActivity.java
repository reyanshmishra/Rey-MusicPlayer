package com.boom.music.player.Search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.Models.Album;
import com.boom.music.player.Models.Artist;
import com.boom.music.player.Models.Genre;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.TagEditor.Id3TagEditorActivity;
import com.boom.music.player.Activities.TracksSubFragment;
import com.boom.music.player.SubGridViewFragment.TracksSubGridViewFragment;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class SearchActivity extends AppCompatActivity implements MusicUtils.Defs, OnTaskCompleted {

    private InputMethodManager mImm;
    private String queryString;
    private ImageButton mImageButtonClear;
    private ImageButton mBackImageButton;
    private Common mApp;
    private Context mContext;
    private SearchAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List mSearchResults;
    private long mSelectedId;
    private RelativeLayout mMainParent;
    private int mSelectedPosition;
    private EditText mSearchEditText;
    private ArrayList<Fragment> mFragments;

    private RelativeLayout mRelativeLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mApp = (Common) getApplicationContext();
        mContext = getApplicationContext();
        mSearchResults = new ArrayList<>();
        mImageButtonClear = (ImageButton) findViewById(R.id.image_button_cross);
        mBackImageButton = (ImageButton) findViewById(R.id.image_back_button);
        mBackImageButton.setOnClickListener(v -> finish());
        mFragments = new ArrayList<>();
        mMainParent = (RelativeLayout) findViewById(R.id.main_parent);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.best_matches);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRelativeLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(this);
        mRelativeLayout.setLayoutParams(params);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mSearchEditText = (EditText) findViewById(R.id.edit_text_search);
        mSearchEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext().getApplicationContext(), "Futura-Book-Font"));

        mImageButtonClear.setOnClickListener(v -> mSearchEditText.setText(""));

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                Logger.log("" + mAdapter.getItemViewType(position));
                if (mAdapter.getItemViewType(position) == 0 || mAdapter.getItemViewType(position) == 2) {
                    return 3;
                } else {
                    return 1;
                }

            }
        });

        mRecyclerView.setLayoutManager(gridLayoutManager);


        mAdapter = new SearchAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        RxTextView.textChangeEvents(mSearchEditText)
                .debounce(175, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getSearchObserver());
    }

    private DisposableObserver<TextViewTextChangeEvent> getSearchObserver() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
                String query = onTextChangeEvent.text().toString().trim();
                if (query.toString().length() == 0) {
                    mImageButtonClear.setVisibility(View.INVISIBLE);
                } else {
                    mImageButtonClear.setVisibility(View.VISIBLE);
                }
                onQueryTextChange(query);
            }
        };
    }


    public boolean onQueryTextChange(final String newText) {
        if (newText.equals(queryString)) {
            return true;
        }
        queryString = newText;
        if (!queryString.trim().equals("")) {
            this.mSearchResults = new ArrayList();

            ArrayList<Song> songs = MusicUtils.searchSongs(this, queryString);
            ArrayList<Album> albums = CursorHelper.searchAlbums(mContext, queryString);
            ArrayList<Artist> artists = mApp.getDBAccessHelper().searchArtist(queryString);
            ArrayList<Genre> genres = mApp.getDBAccessHelper().searchGenre(queryString);

            if (!songs.isEmpty()) {
                mSearchResults.add("Songs");
                mSearchResults.addAll((Collection) (songs));
            }

            if (!albums.isEmpty()) {
                mSearchResults.add("Albums");
                mSearchResults.addAll((Collection) (albums));
            }

            if (!artists.isEmpty()) {
                mSearchResults.add("Artists");
                mSearchResults.addAll((Collection) (artists));
            }

            if (!genres.isEmpty()) {
                mSearchResults.add("Genres");
                mSearchResults.addAll((Collection) (genres));
            }

        } else {
            mSearchResults.clear();
            mAdapter.update(mSearchResults);
            mAdapter.notifyDataSetChanged();
        }
        mAdapter.update(mSearchResults);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        hideInputManager();
        return super.dispatchTouchEvent(ev);
    }

    public void hideInputManager() {
        if (mSearchEditText != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
            }
            mSearchEditText.clearFocus();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    mApp.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    deleteSong();
                }
                break;
        }
    }

    private void deleteSong() {
        ArrayList<Song> songs = new ArrayList<>();

        if (mSearchResults.get(mSelectedPosition) instanceof Song) {
            songs.add((Song) mSearchResults.get(mSelectedPosition));
        }
        if (mSearchResults.get(mSelectedPosition) instanceof Album) {
            songs.addAll(CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(mSelectedPosition))._Id));
        }
        if (mSearchResults.get(mSelectedPosition) instanceof Artist) {
            songs.addAll(CursorHelper.getTracksForSelection("ARTIST", "" + ((Artist) mSearchResults.get(mSelectedPosition))._artistId));
        }
        if (mSearchResults.get(mSelectedPosition) instanceof Genre) {
            songs.addAll(CursorHelper.getTracksForSelection("GENRES", "" + ((Genre) mSearchResults.get(mSelectedPosition))._genreId));
        }
        try {
            MusicUtils.deleteFile(SearchActivity.this, songs, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onArtistPopUpMenuClickListener(View v, int position) {
        mSelectedPosition = position;
        final PopupMenu menu = new PopupMenu(SearchActivity.this, v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(mContext, sub, 0);
        mSelectedId = ((Artist) mSearchResults.get(position))._artistId;

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {

                case R.id.popup_album_play_next:
                    new AsyncAddTo(((Artist) mSearchResults.get(position))._artistName, false, CursorHelper.getTracksForSelection("ARTIST", "" + mSelectedId)).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(((Artist) mSearchResults.get(position))._artistName, true, CursorHelper.getTracksForSelection("ARTIST", "" + mSelectedId)).execute();
                    return true;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(CursorHelper.getTracksForSelection("ARTIST", "" + mSelectedId)));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getSupportFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, CursorHelper.getTracksForSelection("ARTIST", "" + mSelectedId));
                    return true;
                case R.id.popup_album_delete:

                    try {
                        MusicUtils.deleteFile(SearchActivity.this, CursorHelper.getTracksForSelection("ARTIST", "" + ((Artist) mSearchResults.get(position))._artistId), this);
                    } catch (Exception e) {
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

    public void onSongPopUpMenuClicked(View view, final int position) {
        mSelectedPosition = position;
        final PopupMenu menu = new PopupMenu(SearchActivity.this, view);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(mContext, sub, 0);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_song_play_next:
                    new AsyncAddTo(((Song) mSearchResults.get(position))._title, false, (Song) mSearchResults.get(position)).execute();
                    break;
                case R.id.popup_song_addto_queue:
                    new AsyncAddTo(((Song) mSearchResults.get(position))._title, true, (Song) mSearchResults.get(position)).execute();
                    break;
                case R.id.popup_song_add_to_favs:
                    break;
                case R.id.popup_song_delete:
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(((Song) mSearchResults.get(position)));
                    try {
                        MusicUtils.deleteFile(SearchActivity.this, song, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.popup_edit_songs_tags:
                    Intent intent = new Intent(SearchActivity.this, Id3TagEditorActivity.class);
                    intent.putExtra("SONG_PATH", ((Song) mSearchResults.get(mSelectedPosition))._path);
                    intent.putExtra("ALBUM_ID", ((Song) mSearchResults.get(mSelectedPosition))._albumId);
                    startActivityForResult(intent, Constants.EDIT_TAGS);
                    break;
                case R.id.popup_song_use_as_phone_ringtone:
                    MusicUtils.setRingtone(SearchActivity.this, ((Song) mSearchResults.get(mSelectedPosition))._id);
                    break;
                case R.id.popup_song_share:
                    MusicUtils.shareTheMusic(SearchActivity.this, ((Song) mSearchResults.get(mSelectedPosition))._path);
                    break;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", new long[]{((Song) mSearchResults.get(mSelectedPosition))._id});
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getSupportFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    long[] list = new long[]{((Song) mSearchResults.get(mSelectedPosition))._id};
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(mContext, list, playlist);
                    return true;
            }
            return false;
        });
        menu.inflate(R.menu.popup_song);
        menu.show();
    }

    public void onAlbumPopUpMenuClickListener(View v, final int position) {
        mSelectedPosition = position;
        final PopupMenu menu = new PopupMenu(SearchActivity.this, v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(mContext, sub, 0);
        mSelectedId = ((Album) mSearchResults.get(position))._Id;
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_album_play_next:
                    new AsyncAddTo(((Album) mSearchResults.get(position))._albumName, false, CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(position))._Id)).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(((Album) mSearchResults.get(position))._albumName, true, CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(position))._Id)).execute();
                    return true;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(position))._Id)));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getSupportFragmentManager(), "FRAGMENT_TAG");

                    return true;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(position))._Id));
                    return true;
                case R.id.popup_album_delete:
                    try {
                        MusicUtils.deleteFile(SearchActivity.this, CursorHelper.getTracksForSelection("ALBUMS", "" + ((Album) mSearchResults.get(position))._Id), this);
                    } catch (Exception e) {
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

    @Override
    public void onSongDeleted() {

    }

    public void onGenrePopUpMenuClickListener(View v, int adapterPosition) {

    }

    @Override
    public void onBackPressed() {
        if (mFragments.size() > 0) {
            Fragment fragment = mFragments.get(mFragments.size() - 1);
            if (fragment instanceof TracksSubFragment) {
                ((TracksSubFragment) fragment).removeFragment();
            }
            if (fragment instanceof TracksSubGridViewFragment) {
                ((TracksSubGridViewFragment) fragment).removeFragment();
            }
            mFragments.remove(fragment);
            return;
        }
        super.onBackPressed();
    }

    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_parent, fragment);
        fragmentTransaction.commitAllowingStateLoss();
        mFragments.add(fragment);
    }
}
