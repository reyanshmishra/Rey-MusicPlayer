package com.reyansh.audio.audioplayer.free.NowPlaying;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Activities.TracksSubFragment;
import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Dialogs.ABRepeatDialog;
import com.reyansh.audio.audioplayer.free.Equalizer.EqualizerActivity;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.SubGridViewFragment.TracksSubGridViewFragment;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Utils.SongDataHelper;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by REYANSH on 6/19/2017.
 */

public class PlaylistPagerFragment extends Fragment {


    /**
     * Current postion of song in the queue
     */
    private int mPosition;

    /**
     * Main View
     */
    private View mView;

    /**
     * TextViews for song name, artist or albums name, Lyrics, No Lyrics
     */

    private TextView mSongNameTextView;
    private TextView mAlbumOrArtistNameTextView;

    /**
     * PopUp menu button
     */

    private ImageButton mPopUpMenuButton;
    private PopupMenu mPopupMenu;

    /**
     * Album Art
     */

    private ImageView mAlbumArtImageView;

    /**
     * SongDataHelper to populate and retrieve the songs data
     */
    private SongDataHelper mSongDataHelper;

    private Context mContext;
    private Bitmap mBitmap;

    private Common mApp;
    /**
     * Overflow button click listener.
     */
    private View.OnClickListener overflowClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            //Hide the "Current queue" item if it's already visible.
//            if (mApp.isTabletInLandscape())

            mPopupMenu.show();
        }

    };
    /**
     * "Go to" popup menu item click listener.
     */
    private PopupMenu.OnMenuItemClickListener goToMenuClickListener = item -> {
        Bundle bundle = new Bundle();
        NowPlayingActivity nowPlayingActivity = (NowPlayingActivity) getActivity();
        Fragment fragment;

        switch (item.getItemId()) {
            case R.id.go_to_this_artist:
                fragment = new TracksSubGridViewFragment();
                bundle.putString(Constants.HEADER_TITLE, nowPlayingActivity.mSongs.get(mPosition)._artist);
                bundle.putString(Constants.HEADER_SUB_TITLE, nowPlayingActivity.mSongs.get(mPosition)._album);
                bundle.putString(Constants.FROM_WHERE, "ARTIST");
                bundle.putLong(Constants.SELECTION_VALUE, nowPlayingActivity.mSongs.get(mPosition)._artistId);
                bundle.putString(Constants.COVER_PATH, (MusicUtils.getAlbumArtUri(nowPlayingActivity.mSongs.get(mPosition)._albumId).toString()));
                fragment.setArguments(bundle);
                ((NowPlayingActivity) getActivity()).addFragment(fragment);
                break;
            case R.id.go_to_this_album:
                fragment = new TracksSubFragment();
                bundle.putString(Constants.HEADER_TITLE, nowPlayingActivity.mSongs.get(mPosition)._album);
                bundle.putString(Constants.HEADER_SUB_TITLE, nowPlayingActivity.mSongs.get(mPosition)._artist);
                bundle.putString(Constants.FROM_WHERE, "ALBUMS");
                bundle.putLong(Constants.SELECTION_VALUE, nowPlayingActivity.mSongs.get(mPosition)._albumId);
                fragment.setArguments(bundle);
                ((NowPlayingActivity) getActivity()).addFragment(fragment);
                break;

        }

        return false;
    };
    /**
     * Menu item click listener for the overflow pop up menu.
     */
    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.item_set_timer:
                    ((NowPlayingActivity) getActivity()).setTimer();
                    break;
                case R.id.equalizer:
                    Intent intent = new Intent(getActivity(), EqualizerActivity.class);
                    startActivity(intent);
                    break;
                case R.id.save_clear_current_position:
                  /*  String songId = mApp.getService().getCurrentSong().getId();
                    if (item.getTitle().equals(mContext.getResources().getString(R.string.save_current_position))) {
                        item.setTitle(R.string.clear_saved_position);

                        long currentPositionMillis = mApp.getService().getCurrentMediaPlayer().getCurrentPosition();
                        String message = mContext.getResources().getString(R.string.track_will_resume_from);
                        message += " " + mApp.convertMillisToMinsSecs(currentPositionMillis);
                        message += " " + mContext.getResources().getString(R.string.next_time_you_play_it);

                        mApp.getDBAccessHelper().setLastPlaybackPosition(songId, currentPositionMillis);
                        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();

                    } else {
                        item.setTitle(R.string.save_current_position);
                        mApp.getDBAccessHelper().setLastPlaybackPosition(songId, -1);
                        Toast.makeText(mContext, R.string.track_start_from_beginning_next_time_play, Toast.LENGTH_LONG).show();

                    }

                    //Requery the database and update the service cursor.
                    mApp.getPlaybackKickstarter().updateServiceCursor();*/

                    break;
                /*case R.id.show_embedded_lyrics:

                    if (item.getTitle().equals(mContext.getResources().getString(R.string.show_embedded_lyrics))) {
                        item.setTitle(R.string.hide_lyrics);
                    } else {
                        item.setTitle(R.string.show_embedded_lyrics);
                    }
*/
                case R.id.a_b_repeat:

                    if (mApp.isServiceRunning()) {
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ABRepeatDialog dialog = new ABRepeatDialog();
                        dialog.show(ft, "repeatSongRangeDialog");
                    } else {
                        Toast.makeText(mContext, R.string.start_playback_to_activate_ab_repeat, Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.go_to:
                    PopupMenu goToPopupMenu = new PopupMenu(getActivity(), mPopUpMenuButton);
                    goToPopupMenu.inflate(R.menu.show_more_menu);
                    goToPopupMenu.setOnMenuItemClickListener(goToMenuClickListener);
                    goToPopupMenu.show();
                    break;
            }

            return false;
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_playlist_pager_fill, container, false);
        mContext = getActivity().getApplicationContext();
        mPosition = getArguments().getInt("POSITION");

        mSongNameTextView = mView.findViewById(R.id.songName);
        mAlbumOrArtistNameTextView = mView.findViewById(R.id.artistAlbumName);

        mAlbumArtImageView = mView.findViewById(R.id.coverArt);

        mSongNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));
        mAlbumOrArtistNameTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOOK));

        mApp = (Common) getActivity().getApplicationContext();

        mPopUpMenuButton = mView.findViewById(R.id.now_playing_overflow_icon);
        mPopupMenu = new PopupMenu(getActivity(), mPopUpMenuButton);
        mPopupMenu.getMenuInflater().inflate(R.menu.now_playing_overflow_menu, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(menuItemClickListener);

        mSongDataHelper = new SongDataHelper();

        mSongDataHelper.populateSongData(getContext(), ((NowPlayingActivity) getActivity()).mSongs, mPosition);

        ImageLoader.getInstance().displayImage(mSongDataHelper.getAlbumArtPath(), mAlbumArtImageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                int padding = MusicUtils.getDPFromPixel(215);
                mAlbumArtImageView.setImageResource(R.drawable.ic_placeholder);
                mAlbumArtImageView.setPadding(padding, padding, padding, padding);
            }


            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mAlbumArtImageView.setImageBitmap(loadedImage);
                mSongDataHelper.setAlbumArt(loadedImage);
                mBitmap = loadedImage;
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                Log.d("TAG-", "" + "CANCELLED " + mSongDataHelper.getTitle());
            }
        });

        mSongNameTextView.setText(mSongDataHelper.getTitle());
        mAlbumOrArtistNameTextView.setText(mSongDataHelper.getAlbum() + " - " + mSongDataHelper.getArtist());

        mSongNameTextView.setSelected(true);
        mAlbumOrArtistNameTextView.setSelected(true);
        mPopUpMenuButton.setOnClickListener(overflowClickListener);
        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBitmap != null) {
//            mBitmap.recycle();
//            mBitmap = null;
        }
        if (mSongDataHelper.getAlbumArt() != null) {
//            mSongDataHelper.getAlbumArt().recycle();
//            mSongDataHelper.setAlbumArt(null);
//            mSongDataHelper = null;
        }
    }
}
