package com.boom.music.player.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boom.music.player.Lastfmapi.Models.BestMatchesModel;
import com.boom.music.player.R;
import com.boom.music.player.TagEditor.Id3TagEditorActivity;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

/**
 * Created by REYANSH on 7/29/2017.
 */

public class BestMatchesAdapter extends RecyclerView.Adapter<BestMatchesAdapter.ItemHolder> {

    private Id3TagEditorActivity mId3TagEditorActivity;
    private List<BestMatchesModel.Results> mBestMatchesModels;

    public BestMatchesAdapter(Id3TagEditorActivity id3TagEditorActivity, List<BestMatchesModel.Results> bestMatchesModels) {
        mId3TagEditorActivity = id3TagEditorActivity;
        mBestMatchesModels = bestMatchesModels;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        String url = mBestMatchesModels.get(position).artworkUrl100.replace("100x100", "300x300");
        ImageLoader.getInstance().displayImage(url, holder.albumart, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.albumart.setImageResource(R.drawable.drawable_image);
                int padding = MusicUtils.getDPFromPixel(20);
                holder.albumart.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.albumart.setImageResource(R.drawable.drawable_image);
                int padding = MusicUtils.getDPFromPixel(20);
                holder.albumart.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                int padding = MusicUtils.getDPFromPixel(0);
                holder.albumart.setPadding(padding, padding, padding, padding);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        holder.albumName.setText(mBestMatchesModels.get(position).trackName);
        holder.artistName.setText(mBestMatchesModels.get(position).artistName);

    }

    @Override
    public int getItemCount() {
        return mBestMatchesModels == null ? 0 : mBestMatchesModels.size();
    }

    public void updateData(List<BestMatchesModel.Results> resultses) {
        this.mBestMatchesModels = resultses;
        notifyDataSetChanged();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        private TextView albumName;
        private TextView artistName;
        private ImageView albumart;
        private RelativeLayout mFooter;


        public ItemHolder(View itemView) {
            super(itemView);

            albumName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            artistName = (TextView) itemView.findViewById(R.id.gridViewSubText);
            albumart = (ImageView) itemView.findViewById(R.id.gridViewImage);
            itemView.findViewById(R.id.overflow).setVisibility(View.INVISIBLE);

            albumName.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));
            artistName.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));
            mFooter = (RelativeLayout) itemView.findViewById(R.id.linear_layout_footer);
            mFooter.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> mId3TagEditorActivity.updateAlbumArt(
                    mBestMatchesModels.get(getAdapterPosition()).artworkUrl100.replace("100x100", "500x500")
                    /*,mBestMatchesModels.get(getAdapterPosition()).trackName,
                    mBestMatchesModels.get(getAdapterPosition()).collectionName,
                    mBestMatchesModels.get(getAdapterPosition()).artistName,
                    mBestMatchesModels.get(getAdapterPosition()).primaryGenreName,
                    mBestMatchesModels.get(getAdapterPosition()).releaseDate,
                    mBestMatchesModels.get(getAdapterPosition()).trackNumber,
                    mBestMatchesModels.get(getAdapterPosition()).trackCount*/
            ));
        }
    }
}
