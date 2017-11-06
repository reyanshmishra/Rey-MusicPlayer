package com.boom.music.player.Setting;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.boom.music.player.R;

/**
 * Created by reyansh on 9/13/17.
 */

public class SettingArrangeTabs extends Fragment {


    private View mView;
    private RecyclerView mRecyclerView;
    private TabsAdapter mTabsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tab_setting, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mTabsAdapter = new TabsAdapter(getActivity().getApplicationContext());
        mRecyclerView.setAdapter(mTabsAdapter);

        return mView;
    }


    class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.ItemHolder> {

        private String[] tabs;

        public TabsAdapter(Context context) {
            this.tabs = context.getResources().getStringArray(R.array.fragments_titles);
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tabs_layout, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.mTitle.setText(tabs[position]);
        }

        @Override
        public int getItemCount() {
            return tabs.length;
        }

        public class ItemHolder extends RecyclerView.ViewHolder {
            private TextView mTitle;
            private ImageButton mDragButton;

            public ItemHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.title_text);
                mDragButton = (ImageButton) itemView.findViewById(R.id.drag_handle);

            }
        }
    }
}
