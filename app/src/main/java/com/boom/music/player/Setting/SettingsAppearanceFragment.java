package com.boom.music.player.Setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boom.music.player.Common;
import com.boom.music.player.R;

/**
 * Created by REYANSH on 8/16/2017.
 */

public class SettingsAppearanceFragment extends PreferenceFragment {
    private View mRootView;
    private Context mContext;
    private Common mApp;
    private ListPreference mStartUpScreenPreference;
    private PreferenceManager mPreferenceManager;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_appearance);
        mPreferenceManager = this.getPreferenceManager();

        mStartUpScreenPreference = (ListPreference) mPreferenceManager.findPreference("preference_key_startup_screen");
        mStartUpScreenPreference.setOnPreferenceChangeListener((preference, o) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.restart_app);
            builder.setMessage(R.string.restart_app_des);
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Intent intent = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                getActivity().finish();
                startActivity(intent);
            });
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();

            return true;
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle onSavedInstanceState) {
        mRootView = super.onCreateView(inflater, container, onSavedInstanceState);
        mContext = getActivity().getApplicationContext();
        ((SettingActivity) getActivity()).setToolbarTitle(getString(R.string.appearance));
        return mRootView;
    }

}
