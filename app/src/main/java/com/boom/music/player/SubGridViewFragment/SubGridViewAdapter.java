package com.boom.music.player.SubGridViewFragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boom.music.player.Activities.TracksSubFragment;
import com.boom.music.player.Common;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Album;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Search.SearchActivity;
import com.boom.music.player.Utils.BubbleTextGetter;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by REYANSH on 6/16/2017.
 */

public class SubGridViewAdapter extends RecyclerView.Adapter<SubGridViewAdapter.ItemHolder> implements BubbleTextGetter {

    private ArrayList<Album> mData;
    private TracksSubGridViewFragment mTracksSubActivity;

    public SubGridViewAdapter(TracksSubGridViewFragment tracksSubActivity) {
        mTracksSubActivity = tracksSubActivity;
    }

    @Override
    public SubGridViewAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
        return new SubGridViewAdapter.ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SubGridViewAdapter.ItemHolder holder, int position) {
        holder.albumName.setText(mData.get(position)._albumName);
        holder.artistName.setText(mData.get(position)._artistName);
        ImageLoader.getInstance().displayImage(MusicUtils.getAlbumArtUri(mData.get(position)._Id).toString(), holder.albumart, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.albumart.setImageResource(R.drawable.ic_placeholder);
                int padding = MusicUtils.getDPFromPixel(45);
                holder.albumart.setPadding(padding, padding, padding, padding);
                holder.albumart.setBackgroundColor(R.color.blue_gray);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.albumart.setPadding(0, 0, 0, 0);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void updateData(ArrayList<Album> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            return String.valueOf(mData.get(pos)._albumName.charAt(0));
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }

    public void update(ArrayList<Album> songList) {
        mData = songList;
        notifyDataSetChanged();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView albumName;
        private TextView artistName;
        private ImageView albumart;
        private ImageView mOverFlow;

        public ItemHolder(View itemView) {
            super(itemView);
            int mWidth;
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            mWidth = (metrics.widthPixels) / Common.getNumberOfColms();

            albumName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            artistName = (TextView) itemView.findViewById(R.id.gridViewSubText);
            albumart = (ImageView) itemView.findViewById(R.id.gridViewImage);


            albumName.setTypeface(TypefaceHelper.getTypeface(itemView.getContext(), "Futura-Book-Font"));
            artistName.setTypeface(TypefaceHelper.getTypeface(itemView.getContext(), "Futura-Book-Font"));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) albumart.getLayoutParams();
            params.width = mWidth;
            params.height = mWidth;
            albumart.setLayoutParams(params);

            mOverFlow = (ImageView) itemView.findViewById(R.id.overflow);
            mOverFlow.setOnClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.overflow) {
                mTracksSubActivity.onPopUpMenuClickListener(v, getAdapterPosition());
                return;
            }
            if (mTracksSubActivity.checkSongsEmpty(CursorHelper.getTracksForSelection("ALBUMS", "" + mData.get(getAdapterPosition())._Id), getAdapterPosition())) {
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(Constants.HEADER_TITLE, mData.get(getAdapterPosition())._albumName);
            bundle.putString(Constants.HEADER_SUB_TITLE, mData.get(getAdapterPosition())._artistName);
            bundle.putString(Constants.FROM_WHERE, "ALBUMS");
            bundle.putLong(Constants.SELECTION_VALUE, mData.get(getAdapterPosition())._Id);
            TracksSubFragment tracksSubFragment = new TracksSubFragment();
            tracksSubFragment.setArguments(bundle);

            if (mTracksSubActivity.getActivity() instanceof MainActivity)
                ((MainActivity) mTracksSubActivity.getActivity()).addFragment(tracksSubFragment);

            if (mTracksSubActivity.getActivity() instanceof SearchActivity)
                ((SearchActivity) mTracksSubActivity.getActivity()).addFragment(tracksSubFragment);

            if (mTracksSubActivity.getActivity() instanceof NowPlayingActivity)
                ((NowPlayingActivity) mTracksSubActivity.getActivity()).addFragment(tracksSubFragment);
        }
    }
}
