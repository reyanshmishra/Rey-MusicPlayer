package com.boom.music.player.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.boom.music.player.Common;
import com.boom.music.player.Interfaces.OnProgressUpdate;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.TypefaceHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class SplashActivity extends AppCompatActivity implements OnProgressUpdate {

    private CompositeDisposable mCompositeDisposable;
    private Common mApp;
    private ProgressBar mProgressBar;
    private RelativeLayout mProgressBarHolder;
    private TextView mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mApp = (Common) Common.getInstance().getApplicationContext();

        mTitle = (TextView) findViewById(R.id.title);
        mTitle.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Bold-Font"));
        ((TextView) findViewById(R.id.building_library_task)).setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Condensed-Font"));

        mProgressBar = (ProgressBar) findViewById(R.id.building_library_progress);
        mProgressBarHolder = (RelativeLayout) findViewById(R.id.progress_elements_container);
        mCompositeDisposable = new CompositeDisposable();
        int launchCount = PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.LAUNCH_COUNT, 0);
        launchCount++;
        PreferencesHelper.getInstance().put(PreferencesHelper.Key.LAUNCH_COUNT, launchCount);

        launchMainActivity();
    }

    public void launchMainActivity() {
        if (!MusicUtils.isKitkat()) {
            if (checkAndRequestPermissions()) {
                buildLibrary();
            }
        } else {
            buildLibrary();
        }
    }

    private boolean checkAndRequestPermissions() {
        int modifyAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (modifyAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), Constants.REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Constants.REQUEST_PERMISSIONS == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                buildLibrary();
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.grant_permission);
                builder.setMessage(R.string.grant_permission_message);
                builder.setNegativeButton(R.string.no, (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });


                builder.setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);

                });
                builder.create().show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

    private void buildLibrary() {
        fadeInFadeOut();
        mCompositeDisposable.add(Observable.fromCallable(() -> CursorHelper.buildMusicLibrary(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        SplashActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.exp("" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                })
        );
    }

    private void fadeInFadeOut() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mTitle.startAnimation(fadeOutAnimation);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mProgressBarHolder.startAnimation(fadeInAnimation);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mProgressBarHolder.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public void onProgressed(int progress) {
        mProgressBar.setProgress(progress);
    }

    @Override
    public void maxProgress(int max) {
        mProgressBar.setMax(max);
    }
}
