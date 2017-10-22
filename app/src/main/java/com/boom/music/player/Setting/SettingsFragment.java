package com.boom.music.player.Setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.boom.music.player.Common;
import com.boom.music.player.R;
import com.boom.music.player.Utils.PreferencesHelper;

public class SettingsFragment extends android.preference.PreferenceFragment {


    private PreferenceManager preferenceManager;

    private Preference rescanFoldersPreference;
    private Preference scanFrequencyPreference;
    private Activity mActivity;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        preferenceManager = this.getPreferenceManager();
        mActivity = getActivity();
        rescanFoldersPreference = preferenceManager.findPreference("preference_key_rescan_folders");
        scanFrequencyPreference = preferenceManager.findPreference("preference_key_scan_frequency");


        rescanFoldersPreference.setOnPreferenceClickListener(preference -> {

            //Setting the "REBUILD_LIBRARY" flag to true will force MainActivity to rescan the folders.
            PreferencesHelper.getInstance(Common.getInstance()).put(PreferencesHelper.Key.REBUILD_LIBRARY, true);

            //Restart the app.
            final Intent i = mActivity.getPackageManager().getLaunchIntentForPackage(mActivity.getBaseContext().getPackageName());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mActivity.finish();
            startActivity(i);
            return false;
        });

        scanFrequencyPreference.setOnPreferenceClickListener(arg0 -> {

            /*Intent intent = new Intent(mActivity, PreferenceDialogLauncherActivity.class);
            intent.putExtra("INDEX", 7);
            startActivity(intent);*/

            return false;
        });

    }

}