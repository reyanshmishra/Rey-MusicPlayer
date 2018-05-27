package com.reyansh.audio.audioplayer.free.Setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.CursorHelper;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;
import com.reyansh.audio.audioplayer.free.Utils.helper.ItemTouchHelperAdapter;
import com.reyansh.audio.audioplayer.free.Utils.helper.ItemTouchHelperViewHolder;
import com.reyansh.audio.audioplayer.free.Utils.helper.OnStartDragListener;
import com.reyansh.audio.audioplayer.free.Utils.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by reyansh on 11/9/17.
 */

public class SettingArrangeTabsFragment extends DialogFragment implements OnStartDragListener {

    private View mView;
    private RecyclerView mRecyclerView;
    private ArrayList<String> mTabTitles;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mChanged;

    private OnDismis mOnDismiss;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_cusotmize_section, null);

        Gson gson = new Gson();
        String jsonText = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.TITLES);
        String[] text = gson.fromJson(jsonText, String[].class);
        mTabTitles = new ArrayList<>(Arrays.asList(text));

        mRecyclerView = mView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        CustomizeSectionAdapter customizeSectionAdapter = new CustomizeSectionAdapter();
        mRecyclerView.setAdapter(customizeSectionAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(customizeSectionAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        builder.setView(mView);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setTitle(R.string.arrage_tabs);
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismiss != null && mChanged) {
            mOnDismiss.onDismised();
        }
    }

    public void setOnDismissListener(OnDismis onDismissListener) {
        mOnDismiss = onDismissListener;
    }

    public interface OnDismis {
        void onDismised();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public boolean isChanged() {
        return mChanged;
    }


    class CustomizeSectionAdapter extends RecyclerView.Adapter<CustomizeSectionAdapter.ItemHolder> implements ItemTouchHelperAdapter {

        @Override
        public CustomizeSectionAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_drawer_list_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(CustomizeSectionAdapter.ItemHolder holder, int position) {
            holder.mTitleTextView.setText(mTabTitles.get(position));
        }

        @Override
        public int getItemCount() {
            return mTabTitles.size();
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(mTabTitles, fromPosition, toPosition);
            String[] titles = new String[mTabTitles.size()];
            mTabTitles.toArray(titles);
            CursorHelper.saveTabTitles(titles);
            notifyItemMoved(fromPosition, toPosition);
            if (fromPosition != toPosition) {
                mChanged = true;
            }
            return false;
        }

        @Override
        public void onItemDismiss(int position) {

        }

        public class ItemHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            public TextView mTitleTextView, mArtistTextView;
            public ImageView mDragImageView;
            public CardView mMainBackground;

            public ItemHolder(View itemView) {
                super(itemView);
                mTitleTextView = itemView.findViewById(R.id.queue_song_title);
                mArtistTextView = itemView.findViewById(R.id.song_artist);
                mTitleTextView.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), TypefaceHelper.FUTURA_BOLD));
                mArtistTextView.setVisibility(View.GONE);

                mDragImageView = itemView.findViewById(R.id.drag_handle);
                mMainBackground = itemView.findViewById(R.id.background);
                mDragImageView.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        onStartDrag(this);
                    }
                    return false;
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

}
