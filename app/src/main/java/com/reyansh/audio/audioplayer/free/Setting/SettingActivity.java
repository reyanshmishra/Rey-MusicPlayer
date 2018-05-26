package com.reyansh.audio.audioplayer.free.Setting;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.reyansh.audio.audioplayer.free.LauncherActivity.MainActivity;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Services.AlbumsArtDownloadService;
import com.reyansh.audio.audioplayer.free.Services.ArtistArtDownloadService;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.Logger;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

public class SettingActivity extends PreferenceActivity {

    private AppCompatDelegate mDelegate;
    private Context mContext;
    Toolbar mToolbar;
    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        if (getIntent() != null && getIntent().getBooleanExtra(Constants.FROM_NOTIFICATION, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.downloading_artist_arts);
            builder.setMessage(R.string.artist_art_dialog_desc);
            builder.setPositiveButton(R.string.stop, (dialogInterface, i) -> {
                stopService(new Intent(getApplicationContext(), ArtistArtDownloadService.class));
                dialogInterface.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        } else if (getIntent() != null && getIntent().getBooleanExtra(Constants.FROM_ALBUMS_NOTIFICATION, false)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.downloading_album_arts);
            builder.setMessage(R.string.album_art_dialog_desc);
            builder.setPositiveButton(R.string.stop, (dialogInterface, i) -> {
                stopService(new Intent(getApplicationContext(), AlbumsArtDownloadService.class));
                dialogInterface.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
            builder.create().show();
        }

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_settings, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        mToolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        View view = LayoutInflater.from(this).inflate(R.layout.view, root, false);
        root.addView(mToolbar, 0);
        root.addView(view, 1);
        mToolbar.setNavigationOnClickListener(v -> {
            if (mFragment!=null && mFragment instanceof SettingArrangeTabsFragment){
                boolean tabsChanged =((SettingArrangeTabsFragment)mFragment).isChanged();
                if (tabsChanged){
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setTitle(R.string.restart_app);
                    builder.setMessage(R.string.restart_app_des);
                    builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                        startActivity(intent);
                    });
                    builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        onBackPressed();
                    });
                    builder.create().show();
                }else{
                    onBackPressed();
                }
            }else{
                onBackPressed();
            }
        });
    }
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mFragment=fragment;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * This is being used to add toolbar options into the activity which is not available
     * by default for preferenceActivity
     */

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }


    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void setToolbarTitle(String toolbarTitle) {
        if (getDelegate().getSupportActionBar() != null) {
            getDelegate().getSupportActionBar().setTitle(toolbarTitle);
            Logger.log(toolbarTitle);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getIntent() != null && getIntent().getBooleanExtra(Constants.FROM_NOTIFICATION, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        } else {
            finish();
        }
    }
}
