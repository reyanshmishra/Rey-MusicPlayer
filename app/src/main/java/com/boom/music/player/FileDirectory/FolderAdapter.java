package com.boom.music.player.FileDirectory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boom.music.player.Common;
import com.boom.music.player.R;
import com.boom.music.player.Utils.TypefaceHelper;

import java.io.File;
import java.util.List;

/**
 * Created by REYANSH on 11/09/2016.
 */
public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Common mApp;
    private FolderFragment mFragment;

    private List<String> mFileFolderNameList;
    private List<Integer> mFileFolderTypeList;
    private List<String> mFileFolderSizeList;
    private List<String> mFileFolderPathsList;

    FolderAdapter(FolderFragment fragment) {
        mContext = fragment.getContext();
        mApp = (Common) mContext.getApplicationContext();
        mFragment = fragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_view_layout, parent, false);
                return new ItemHolder(itemView);
            case 1:
                return new UpHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_view_up, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case 0:
                ItemHolder holder = (ItemHolder) viewHolder;
                holder.mTitle.setText(mFileFolderNameList.get(position));
                holder.mSubTitle.setText(mFileFolderSizeList.get(position));

                if (mFileFolderTypeList.get(position) == FolderFragment.FOLDER) {
                    holder.mIconImage.setImageResource(R.drawable.icon_folderblue);
                } else if (mFileFolderTypeList.get(position) == FolderFragment.AUDIO_FILE) {
                    holder.mIconImage.setImageResource(R.drawable.icon_mp3);
                } else {
                    holder.mIconImage.setImageResource(R.drawable.icon_default);
                }
                break;
            case 1:
                UpHolder upHolder = (UpHolder) viewHolder;
                upHolder.mTextView.setText("...." + new File(mFragment.getCurrentDir()).getName());
                upHolder.mImageView.setImageResource(R.drawable.drawable_up_folder);

                break;
        }

    }

    @Override
    public int getItemCount() {
        return mFileFolderPathsList == null ? 0 : mFileFolderPathsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mFileFolderTypeList.get(position) == 5) {
            return 1;
        } else {
            return 0;
        }
    }

    public void updateData(List<String> nameList,
                           List<Integer> fileFolderTypeList,
                           List<String> sizeList,
                           List<String> fileFolderPathsList) {
        mFileFolderNameList = nameList;
        mFileFolderTypeList = fileFolderTypeList;
        mFileFolderSizeList = sizeList;
        mFileFolderPathsList = fileFolderPathsList;

        mFileFolderNameList.add(0, "ad");
        mFileFolderTypeList.add(0, 5);
        mFileFolderSizeList.add(0, "ad");
        mFileFolderPathsList.add(0, "aa");
        notifyDataSetChanged();

    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitle;
        private TextView mSubTitle;
        private TextView duration;
        private ImageView mOverFlow;
        private ImageView mIconImage;

        public ItemHolder(View itemView) {
            super(itemView);

            mIconImage = (ImageView) itemView.findViewById(R.id.listViewLeftIcon);

            mTitle = (TextView) itemView.findViewById(R.id.listViewTitleText);
            mSubTitle = (TextView) itemView.findViewById(R.id.listViewSubText);
            duration = (TextView) itemView.findViewById(R.id.listViewRightSubText);
            duration.setVisibility(View.INVISIBLE);

            mTitle.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));
            mSubTitle.setTypeface(TypefaceHelper.getTypeface(itemView.getContext().getApplicationContext(), "Futura-Book-Font"));

            mOverFlow = (ImageView) itemView.findViewById(R.id.listViewOverflow);

            mOverFlow.setOnClickListener(this);
            mOverFlow.setVisibility(View.VISIBLE);
            mOverFlow.setOnClickListener(this);
            itemView.setOnClickListener(this);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.height = (int) mApp.convertDpToPixels(72.0f, mContext);
            itemView.setLayoutParams(params);

            mIconImage.setScaleX(0.55f);
            mIconImage.setScaleY(0.55f);

        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.listViewOverflow) {
                if (mFragment != null) {
                    if (mFileFolderTypeList.get(getAdapterPosition()) == FolderFragment.AUDIO_FILE) {
                        int fileIndex = 0;
                        for (int i = 0; i < getAdapterPosition(); i++) {
                            if (mFileFolderTypeList.get(i) == FolderFragment.AUDIO_FILE)
                                fileIndex++;
                        }
                        mFragment.onPopUpMenuClickListener(v, fileIndex);
                    } else {
                        mFragment.onFilePopUpClicked(v, getAdapterPosition());
                    }
                    return;
                }
            }
            mFragment.onItemClicked(getAdapterPosition());
        }
    }

    public class UpHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mTextView;

        public UpHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.listViewLeftIcon);
            mTextView = (TextView) itemView.findViewById(R.id.listViewTitleText);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFragment.onUpClick();
                }
            });
        }
    }

}
