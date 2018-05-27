package com.reyansh.audio.audioplayer.free.Setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;


import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuGeneralPublicLicense20;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;


/**
 * Created by REYANSH on 8/16/2017.
 */

public class SettingsAboutFragment extends PreferenceFragment {


    private View mRootView;
    private Context mContext;
    private Common mApp;
    private ListView mListView;
    private PreferenceManager mPreferenceManager;
    private Preference mContactsUsPreference;    private Preference mAboutUsPreference;

    private Preference mLicensesPreference;

    @Override
    public void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        addPreferencesFromResource(R.xml.settings_about);
        ((SettingActivity) getActivity()).setToolbarTitle(getString(R.string.licenses_and_about));
        mPreferenceManager = getPreferenceManager();

        mContactsUsPreference = mPreferenceManager.findPreference("preference_key_contact_us");
        mAboutUsPreference=mPreferenceManager.findPreference("preference_key_about_us");
        mAboutUsPreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder  builder    =new AlertDialog.Builder(getActivity());
            View view  =LayoutInflater.from(getActivity()).inflate(R.layout.layout_about,null);
            ((TextView)view.findViewById(R.id.version_name)).setText(Common.getVersionName());
            builder.setView(view);
            builder.setTitle(R.string.about);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
            builder.create().show();
            return false;
        });

        mLicensesPreference = mPreferenceManager.findPreference("preference_key_licenses");

        mContactsUsPreference.setOnPreferenceClickListener(preference -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "reyanshmishra@outlook.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Boom Music Player Support");
            startActivity(Intent.createChooser(emailIntent, "Send email"));
            return false;
        });

        mLicensesPreference.setOnPreferenceClickListener(preference -> {
            final Notices notices = new Notices();
            notices.addNotice(new Notice("SeekArc", "https://github.com/neild001/SeekArc", "Neil Davies", new MITLicense()));
            notices.addNotice(new Notice("RangeSliderView", "https://github.com/channguyen/range-slider-view", "Chan Nguyen", new MITLicense()));
            notices.addNotice(new Notice("range-seek-bar", "https://github.com/anothem/android-range-seek-bar", "Neil Davies", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("jaudiotagger", "https://bitbucket.org/ijabz/jaudiotagger", "Paul Taylor", new GnuGeneralPublicLicense20()));
            notices.addNotice(new Notice("Universal Image Loader", "https://github.com/nostra13/Android-Universal-Image-Loader", "Sergey Tarasevich", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("android-betterpickers", "https://github.com/code-troopers/android-betterpickers", "Derek Brameyer", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("VerticalSeekBar", "https://github.com/h6ah4i/android-verticalseekbar", "Derek Brameyer", new ApacheSoftwareLicense20()));


            new LicensesDialog.Builder(getActivity())
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show();
            return false;
        });

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
