package com.reyansh.audio.audioplayer.free.Setting;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;

/**
 * Created by REYANSH on 8/16/2017.
 */

public class SettingsAudioFragment extends PreferenceFragment {


    private View mRootView;
    private Context mContext;
    private Common mApp;
    private ListView mListView;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_audio);
        ((SettingActivity) getActivity()).setToolbarTitle(getString(R.string.audio_settings));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceState) {
        mRootView = super.onCreateView(inflater, container, onSavedInstanceState);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mListView = (ListView) mRootView.findViewById(android.R.id.list);
        return mRootView;
    }

}
