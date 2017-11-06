package com.boom.music.player.NowPlaying;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.boom.music.player.Adapters.QueueAdapter;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.R;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.helper.OnStartDragListener;
import com.boom.music.player.Utils.helper.SimpleItemTouchHelperCallback;
import com.boom.music.player.Views.FastScroller;

/**
 * Created by Reyansh on 11/06/2016.
 */
public class QueueFragment extends Fragment implements OnStartDragListener {

    private Common mApp;
    private QueueAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mView;
    private FastScroller mFastScroller;
    private ItemTouchHelper mItemTouchHelper;
    private Context mContext;
    private ImageButton mBackImageButton;
    private ImageButton mOverflowButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.layout_bottomsheet_queue, container, false);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) getActivity().getApplicationContext();

        setHasOptionsMenu(true);
        mApp = (Common) getActivity().getApplicationContext();

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recyclerView);
        mFastScroller = (FastScroller) mView.findViewById(R.id.fast_scroller);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mBackImageButton = (ImageButton) mView.findViewById(R.id.image_back_button);
        mOverflowButton = (ImageButton) mView.findViewById(R.id.image_button_overflow);

        mBackImageButton.setOnClickListener(v -> {
            getActivity().onBackPressed();
        });
        mOverflowButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getActivity(), v);
            popupMenu.inflate(R.menu.menu_playlist);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_clear) {
                    mApp.getService().getSongList().clear();
                    mApp.getService().setSongPos(0);
                    mApp.getService().stopSelf();
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_CURRENT_SEEK_DURATION, 0);
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.SONG_TOTAL_SEEK_DURATION, 0);
                    getActivity().finish();
                } else if (item.getItemId() == R.id.menu_save) {
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(mApp.getService().getSongList()));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
                }
                return false;
            });
            popupMenu.show();
        });


        mAdapter = new QueueAdapter((NowPlayingActivity) getActivity(), ((NowPlayingActivity) getActivity()).mSongs, this);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mFastScroller.setRecyclerView(mRecyclerView);
        if (mApp.isServiceRunning()) {
            mRecyclerView.getLayoutManager().scrollToPosition(mApp.getService().getCurrentSongIndex());
        } else {
            int pos = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);
            mRecyclerView.getLayoutManager().scrollToPosition(pos);
        }

        RelativeLayout relativeLayout = (RelativeLayout) mBackImageButton.getParent();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getActivity());
        relativeLayout.setLayoutParams(params);
        return mView;
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void removeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public QueueAdapter getAdapter() {
        return mAdapter;
    }
}
