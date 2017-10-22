package com.boom.music.player.Adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.NowPlaying.NowPlayingBottomBarFragment;
import com.boom.music.player.R;
import com.boom.music.player.Utils.MusicUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by REYANSH on 8/14/2017.
 */

public class NowPlayingBottomBarAdapter extends RecyclerView.Adapter<NowPlayingBottomBarAdapter.ItemHolder> {
    NowPlayingBottomBarFragment mNowPlayingBottomBarFragment;
    private ArrayList<Song> mSongs;

    public NowPlayingBottomBarAdapter(NowPlayingBottomBarFragment nowPlayingBottomBarFragment) {
        mNowPlayingBottomBarFragment = nowPlayingBottomBarFragment;
    }

    @Override
    public NowPlayingBottomBarAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.now_playing_bottom_item, parent, false));
    }

    @Override
    public void onBindViewHolder(NowPlayingBottomBarAdapter.ItemHolder holder, int position) {
        ImageLoader.getInstance().displayImage(MusicUtils.getAlbumArtUri(mSongs.get(position)._albumId).toString(), holder.mImageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.mImageView.setImageResource(R.drawable.ic_placeholder);
                int padding = MusicUtils.getDPFromPixel(25);
                holder.mImageView.setPadding(padding, padding, padding, padding);
                holder.mImageView.setBackgroundColor(R.color.textColorSecondary);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.mImageView.setPadding(0, 0, 0, 0);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        holder.subtitle.setText(mSongs.get(position)._artist);
        holder.title.setText(mSongs.get(position)._title);

    }

    @Override
    public int getItemCount() {
        return mSongs == null ? 0 : mSongs.size();
    }

    public void updateData(ArrayList<Song> songList) {
        mSongs = songList;
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView subtitle;
        private ImageView mImageView;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_text);
            subtitle = (TextView) itemView.findViewById(R.id.album_text);
            mImageView = (ImageView) itemView.findViewById(R.id.album_art);
            itemView.setOnClickListener(v -> mNowPlayingBottomBarFragment.startActivity(new Intent(mNowPlayingBottomBarFragment.getActivity(), NowPlayingActivity.class)));
        }
    }
}
