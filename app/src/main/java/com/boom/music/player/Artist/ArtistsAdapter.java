package com.boom.music.player.Artist;

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

import com.boom.music.player.Common;
import com.boom.music.player.Lastfmapi.ApiClient;
import com.boom.music.player.Lastfmapi.LastFmInterface;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Artist;
import com.boom.music.player.R;
import com.boom.music.player.SubGridViewActivity.TracksSubGridViewFragment;
import com.boom.music.player.Utils.BubbleTextGetter;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Reyansh on 31/07/2016.
 */
public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ItemHolder> implements BubbleTextGetter {
    private ArrayList<Artist> mData;
    private FragmentArtist mFragmentArtist;
    private Common mApp;
    private LastFmInterface mApiInterface;

    public ArtistsAdapter(FragmentArtist mFragmentArtist) {
        this.mFragmentArtist = mFragmentArtist;
        mApp = (Common) Common.getInstance().getApplicationContext();
        mApiInterface = ApiClient.getClient().create(LastFmInterface.class);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
        return new ItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        holder.mArtistName.setText(mData.get(position)._artistName);

        ImageLoader.getInstance().displayImage(mData.get(position)._artistAlbumArt, holder.mArtistImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.mArtistImage.setImageResource(R.drawable.ic_placeholder);
                int padding = MusicUtils.getDPFromPixel(45);
                holder.mArtistImage.setPadding(padding, padding, padding, padding);
                holder.mArtistImage.setBackgroundColor(R.color.teal);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.mArtistImage.setPadding(0, 0, 0, 0);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                Logger.log("CANCELLED:-" + imageUri);
            }
        });

       /* if (mData.get(position)._artistAlbumArt.startsWith("content://media/")) {
            updateArtistArt("" + mData.get(position)._artistId, mData.get(position)._artistName);
        }*/

        String nooftracks = MusicUtils.makeLabel(mFragmentArtist.getActivity().getApplicationContext(), R.plurals.Nsongs, mData.get(position)._noOfTracksByArtist);
        String noofalbums = MusicUtils.makeLabel(mFragmentArtist.getActivity().getApplicationContext(), R.plurals.Nalbums, mData.get(position)._noOfAlbumsByArtist);
        holder.mDetails.setText(nooftracks + " | " + noofalbums);


    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            return String.valueOf(mData.get(pos)._artistName.charAt(0));
        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }


    public void updateData(ArrayList<Artist> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mArtistName;
        private TextView mDetails;
        private ImageView mArtistImage;
        private ImageView mOverFlowImageView;

        public ItemHolder(View itemView) {
            super(itemView);
            int mWidth;

            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            mWidth = (metrics.widthPixels) / Common.getNumberOfColms();


            mArtistName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            mDetails = (TextView) itemView.findViewById(R.id.gridViewSubText);
            mArtistImage = (ImageView) itemView.findViewById(R.id.gridViewImage);

            mArtistName.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));
            mDetails.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mArtistImage.getLayoutParams();
            params.width = mWidth;
            params.height = mWidth;
            mArtistImage.setLayoutParams(params);


            mOverFlowImageView = (ImageView) itemView.findViewById(R.id.overflow);
            mOverFlowImageView.setVisibility(View.VISIBLE);
            mOverFlowImageView.setOnClickListener(this);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.overflow) {
                mFragmentArtist.onPopUpMenuClickListener(v, getAdapterPosition());
                return;
            }
            if (mFragmentArtist.checkAlbumsEmpty(CursorHelper.getAlbumsForSelection("ARTIST", "" + mData.get(getAdapterPosition())._artistId), getAdapterPosition()))
                return;

            Bundle bundle = new Bundle();
            bundle.putString(Constants.HEADER_TITLE, mData.get(getAdapterPosition())._artistName);
            bundle.putInt(Constants.HEADER_SUB_TITLE, mData.get(getAdapterPosition())._noOfAlbumsByArtist);
            bundle.putString(Constants.FROM_WHERE, "ARTIST");
            bundle.putString(Constants.COVER_PATH, mData.get(getAdapterPosition())._artistAlbumArt);
            bundle.putLong(Constants.SELECTION_VALUE, mData.get(getAdapterPosition())._artistId);

            TracksSubGridViewFragment tracksSubGridViewFragment = new TracksSubGridViewFragment();
            tracksSubGridViewFragment.setArguments(bundle);
            ((MainActivity) mFragmentArtist.getActivity()).addFragment(tracksSubGridViewFragment);
        }
    }


    private void updateArtistArt(String artistId, String artistName) {
        Observable.fromCallable(() -> updateArtistArtNow(artistId, artistName))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String cachedUrl) {
                        Logger.log("" + cachedUrl);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.log("" + e.getCause());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private String updateArtistArtNow(String artistId, String artistName) {
        String cachedUrl = MusicUtils.putBitmapInDiskCache(artistId, artistName, mApiInterface);
        mApp.getDBAccessHelper().updateArtistAlbumArt(artistId, cachedUrl);
        return cachedUrl;
    }
}
