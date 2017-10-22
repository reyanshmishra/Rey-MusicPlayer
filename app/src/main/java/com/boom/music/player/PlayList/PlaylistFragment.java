package com.boom.music.player.PlayList;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.ClearRecentTracks;
import com.boom.music.player.Dialogs.ClearTopPlayed;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Dialogs.RecentlyAddedDialog;
import com.boom.music.player.Dialogs.RenamePlayListDialog;
import com.boom.music.player.Dialogs.UnFavoriteDialog;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Playlist;
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.boom.music.player.Views.FastScroller;

import java.util.ArrayList;

/**
 * Created by Reyansh on 31/07/2016.
 */
public class PlaylistFragment extends Fragment implements MusicUtils.Defs {

    private RecyclerView mRecyclerView;
    private Context mContext;
    private Common mApp;
    private FastScroller mFastScroller;
    private PlaylistAdapter mAdapter;
    private ArrayList<Playlist> mData;
    private View view;
    private OnScrolledListener mOnScrolledListener;
    private Button mCreatePlaylistButton;
    private RelativeLayout mEmptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_album_layout, container, false);
        mContext = getContext();
        setHasOptionsMenu(true);
        mApp = (Common) mContext.getApplicationContext();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mFastScroller = (FastScroller) view.findViewById(R.id.fast_scroller);
        TextView emptyText = (TextView) view.findViewById(R.id.empty_text_view);
        mCreatePlaylistButton = (Button) view.findViewById(R.id.create_playlist);
        mEmptyStateLayout = (RelativeLayout) view.findViewById(R.id.empty_state);


        mCreatePlaylistButton.setTypeface(TypefaceHelper.getTypeface(mContext, "Futura-Bold-Font"));
        emptyText.setTypeface(TypefaceHelper.getTypeface(mContext, "Futura-Bold-Font"));

        mCreatePlaylistButton.setOnClickListener(v -> {
            PlaylistDialog playlistDialog = new PlaylistDialog();
            playlistDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
        });


        mFastScroller.setRecyclerView(mRecyclerView);
        mAdapter = new PlaylistAdapter(this);
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

    public void updateData(ArrayList<Playlist> data) {
        this.mData = data;
        mAdapter.updateData(data);
        mAdapter.notifyDataSetChanged();
        if (mData.size() == 0) {
            mEmptyStateLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyStateLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new AsyncFetchPlaylist(this).execute();
    }

    public void onPopUpMenuClickListener(View v, final int position) {
        final PopupMenu popUpMenu = new PopupMenu(mContext, v);
        popUpMenu.setOnMenuItemClickListener(item -> {
            ArrayList<Song> songs;
            switch (item.getItemId()) {
                case R.id.popup_playlist_clear_favorites:
                    UnFavoriteDialog unFavoriteDialog = new UnFavoriteDialog();
                    unFavoriteDialog.show(getChildFragmentManager(), "UN_FAVORITE");
                    break;
                case R.id.popup_playlist_clear_recently_played:
                    ClearRecentTracks clearRecentTracks = new ClearRecentTracks();
                    clearRecentTracks.show(getChildFragmentManager(), "RECENTLY_PLAYED_CLEAR");
                    break;
                case R.id.popup_playlist_edit_weeks:
                    RecentlyAddedDialog recentlyAddedDialog = new RecentlyAddedDialog();
                    recentlyAddedDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
                    break;
                case R.id.popup_playlist_clear_top_played:
                    ClearTopPlayed clearTopPlayed = new ClearTopPlayed();
                    clearTopPlayed.show(getChildFragmentManager(), "CLEAR_TOP_PLAYED");
                    break;
                case R.id.popup_playlist_play:
                    songs = CursorHelper.getTracksForSelection("PLAYLISTS", "" + mData.get(position)._id);
                    if (songs.size() > 0) {
                        mApp.getPlayBackStarter().playSongs(songs, 0);
                        mContext.startActivity(new Intent(mContext, NowPlayingActivity.class));
                    } else {
                        Toast.makeText(mContext, R.string.empty_playlist, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                case R.id.popup_playlist_play_next:
                    songs = CursorHelper.getTracksForSelection("PLAYLISTS", "" + mData.get(position)._id);
                    new AsyncAddTo(mData.get(position)._name, false, songs).execute();
                    return true;
                case R.id.popup_playlist_add_to_queue:
                    songs = CursorHelper.getTracksForSelection("PLAYLISTS", "" + mData.get(position)._id);
                    new AsyncAddTo(mData.get(position)._name, true, songs).execute();
                    return true;
                case R.id.popup_playlist_delete:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mData.get(position)._id);
                        mContext.getContentResolver().delete(uri, null, null);
                        onResume();
                        Toast.makeText(getActivity(), R.string.playlist_deleted, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                    builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
                    builder.setTitle(R.string.delete_playlist);
                    builder.setMessage(R.string.do_you_want_to_delete);
                    builder.create().show();

                    return true;
                case R.id.popup_playlist_rename:
                    RenamePlayListDialog renamePlayListDialog = new RenamePlayListDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLong("PLAYLIST_ID", mData.get(position)._id);
                    renamePlayListDialog.setArguments(bundle);
                    renamePlayListDialog.show(getChildFragmentManager(), "RENAME_PLAYLIST");

                    return true;
                default:
                    break;
            }
            return false;
        });
        popUpMenu.inflate(R.menu.popup_playlist);
        Menu menu = popUpMenu.getMenu();

        long playlistId = mData.get(position)._id;
        if (playlistId == -1) {
            menu.findItem(R.id.popup_playlist_delete).setVisible(false);
            menu.findItem(R.id.popup_playlist_rename).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_favorites).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_top_played).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_recently_played).setVisible(false);
            menu.findItem(R.id.popup_playlist_add_to_queue).setVisible(false);
        } else if (playlistId == -2) {
            menu.findItem(R.id.popup_playlist_delete).setVisible(false);
            menu.findItem(R.id.popup_playlist_rename).setVisible(false);
            menu.findItem(R.id.popup_playlist_edit_weeks).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_top_played).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_recently_played).setVisible(false);
        } else if (playlistId == -3) {
            menu.findItem(R.id.popup_playlist_delete).setVisible(false);
            menu.findItem(R.id.popup_playlist_edit_weeks).setVisible(false);
            menu.findItem(R.id.popup_playlist_rename).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_favorites).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_recently_played).setVisible(false);

        } else if (playlistId == -4) {
            menu.findItem(R.id.popup_playlist_edit_weeks).setVisible(false);
            menu.findItem(R.id.popup_playlist_delete).setVisible(false);
            menu.findItem(R.id.popup_playlist_rename).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_favorites).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_top_played).setVisible(false);
        } else {
            menu.findItem(R.id.popup_playlist_edit_weeks).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_recently_played).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_favorites).setVisible(false);
            menu.findItem(R.id.popup_playlist_clear_top_played).setVisible(false);
        }
        popUpMenu.show();
    }
}
