package com.boom.music.player.Setting;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boom.music.player.R;

/**
 * Created by reyansh on 11/9/17.
 */

public class CustomizeSectionFragment extends Fragment {

    private View mView;
    private RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.layout_cusotmize_section, container, false);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new CustomizeSectionAdapter());
        return mView;
    }


    class CustomizeSectionAdapter extends RecyclerView.Adapter<CustomizeSectionAdapter.ItemHolder> {

        @Override
        public CustomizeSectionAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.queue_drawer_list_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(CustomizeSectionAdapter.ItemHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class ItemHolder extends RecyclerView.ViewHolder {
            public ItemHolder(View itemView) {
                super(itemView);
            }
        }
    }

}
