package com.reyansh.audio.audioplayer.free.Setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.Logger;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;

/**
 * Created by REYANSH on 8/16/2017.
 */

public class SettingsMusicLibraryFragment extends PreferenceFragment {


    private View mRootView;
    private Context mContext;
    private Preference mRebuildMusicLibrary;
    private ListPreference mScanFrequencyPreference;
    private Preference mSelectMusicFolders;
    private Common mApp;
    private ListView mListView;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_music_library);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceState) {
        mRootView = super.onCreateView(inflater, container, onSavedInstanceState);
        mContext = getActivity().getApplicationContext();
        mApp = (Common) mContext;
        mListView = (ListView) mRootView.findViewById(android.R.id.list);
        mScanFrequencyPreference=(ListPreference)getPreferenceManager().findPreference("preference_key_scan_frequency");
        mScanFrequencyPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            Logger.log(""+newValue);
            return true;
        });


        mRebuildMusicLibrary = getPreferenceManager().findPreference("preference_key_rebuild_music_library");
        mRebuildMusicLibrary.setOnPreferenceClickListener(preference -> {
            PreferencesHelper.getInstance().put(PreferencesHelper.Key.FIRST_LAUNCH, true);
            //Restart the app.
            Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            getActivity().finish();
            startActivity(i);
            return false;
        });
        ((SettingActivity) getActivity()).setToolbarTitle(getString(R.string.music_library));

        return mRootView;
    }

}
