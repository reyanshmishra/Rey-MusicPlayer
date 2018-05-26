package com.reyansh.audio.audioplayer.free.Adapters;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Models.Song;
import com.reyansh.audio.audioplayer.free.NowPlaying.NowPlayingActivity;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.BubbleTextGetter;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;
import com.reyansh.audio.audioplayer.free.Utils.helper.ItemTouchHelperAdapter;
import com.reyansh.audio.audioplayer.free.Utils.helper.ItemTouchHelperViewHolder;
import com.reyansh.audio.audioplayer.free.Utils.helper.OnStartDragListener;
import com.reyansh.audio.audioplayer.free.Views.MusicVisualizer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Reyansh on 11/06/2016.
 */
public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ItemHolder> implements ItemTouchHelperAdapter, BubbleTextGetter {

    private ArrayList<Song> mData;
    private NowPlayingActivity mNowPlayingActivity;
    private Common mApp;
    private String mSongName;
    private OnStartDragListener mDragStartListener;

    public QueueAdapter(NowPlayingActivity nowPlayingActivity, ArrayList<Song> mData, OnStartDragListener dragStartListener) {
        this.mData = mData;
        this.mNowPlayingActivity = nowPlayingActivity;
        mDragStartListener = dragStartListener;
        this.mApp = (Common) mNowPlayingActivity.getApplicationContext();
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_drawer_list_layout, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {

        mSongName = mData.get(position)._title;
        if (mApp.isServiceRunning()) {
            if (mApp.getService().getSongDataHelper().getTitle().equalsIgnoreCase(mSongName)) {
                holder.mMusicVisualizer.setVisibility(View.VISIBLE);
            } else {
                holder.mMusicVisualizer.setVisibility(View.INVISIBLE);
            }
        } else {
            int pos = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.CURRENT_SONG_POSITION, 0);

            if (mData.get(pos)._title.equalsIgnoreCase(mSongName)) {
                holder.mMusicVisualizer.setVisibility(View.VISIBLE);
            } else {
                holder.mMusicVisualizer.setVisibility(View.INVISIBLE);
            }
        }

        holder.mTitleTextView.setText(mData.get(position)._title);
        holder.mArtistTextView.setText(mData.get(position)._artist);


    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public boolean onItemMove(int from, int to) {
        if (mApp.isServiceRunning()) {
            Collections.swap(mApp.getService().getSongList(), from, to);
            if (from == mApp.getService().getCurrentSongIndex()) {
                mApp.getService().setCurrentSongIndex(to);
            } else if (from > mApp.getService().getCurrentSongIndex() && to <= mApp.getService().getCurrentSongIndex()) {
                int i = mApp.getService().getCurrentSongIndex();
                mApp.getService().setCurrentSongIndex(i + 1);
            } else if (from < mApp.getService().getCurrentSongIndex() && to >= mApp.getService().getCurrentSongIndex()) {
                int i = mApp.getService().getCurrentSongIndex();
                mApp.getService().setCurrentSongIndex(i - 1);
            }
        } else {
            ArrayList<Song> hashMaps = mApp.getDBAccessHelper().getQueue();
            Collections.swap(hashMaps, from, to);
            mApp.getDBAccessHelper().saveQueue(hashMaps);
        }
        notifyItemMoved(from, to);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        if (mApp.isServiceRunning()) {
            if (mApp.getService().getSongList().size() == 1) {
                if (mApp.getService() != null) {
                    mApp.getService().getSongList().clear();
                    mApp.getService().setSongPos(0);
                    mApp.getService().stopSelf();
                    mNowPlayingActivity.finish();
                }
            } else if (position == mApp.getService().getCurrentSongIndex()) {
                mApp.getService().nextSong();
                mApp.getService().getSongList().remove(position);
                mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex() - 1);
            } else if (position < mApp.getService().getCurrentSongIndex()) {
                mApp.getService().getSongList().remove(position);
                mApp.getService().setCurrentSongIndex(mApp.getService().getCurrentSongIndex() - 1);
            } else {
                mApp.getService().getSongList().remove(position);
            }
        } else {
            ArrayList<Song> hashMaps = mApp.getDBAccessHelper().getQueue();
            hashMaps.remove(position);
            mApp.getDBAccessHelper().saveQueue(hashMaps);
            if (hashMaps.size() == 0) {
                mNowPlayingActivity.finish();
            }

        }
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            return String.valueOf(mData.get(pos)._title.charAt(0));
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public TextView mTitleTextView, mArtistTextView;
        public ImageView mDraggerImageView;
        public MusicVisualizer mMusicVisualizer;
        public CardView mMainBackground;

        public ItemHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.queue_song_title);
            mArtistTextView = itemView.findViewById(R.id.song_artist);
            mTitleTextView.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), TypefaceHelper.FUTURA_BOOK));
            mArtistTextView.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), TypefaceHelper.FUTURA_BOOK));


            mDraggerImageView = itemView.findViewById(R.id.drag_handle);
            mMusicVisualizer = itemView.findViewById(R.id.visualizer);
            mMainBackground = itemView.findViewById(R.id.background);

            mDraggerImageView.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(this);
                }
                return false;
            });

            if (mApp.isServiceRunning()) {
                mMusicVisualizer.setColor(mApp.getService().getSongDataHelper().getColor());
            } else {
                mMusicVisualizer.setColor(ContextCompat.getColor(Common.getInstance(), R.color.pink));
            }
            itemView.setOnClickListener(v -> {
                if (mApp.isServiceRunning()) {
                    mApp.getService().setSelectedSong(getAdapterPosition());
                } else {
                    mApp.getPlayBackStarter().playSongs(mData, getAdapterPosition());
                }
                mNowPlayingActivity.onBackPressed();
            });

        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
