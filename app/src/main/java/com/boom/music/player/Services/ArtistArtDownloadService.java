package com.boom.music.player.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.boom.music.player.Common;
import com.boom.music.player.Lastfmapi.ApiClient;
import com.boom.music.player.Lastfmapi.LastFmInterface;
import com.boom.music.player.Lastfmapi.Models.ArtistModel;
import com.boom.music.player.Models.Artist;
import com.boom.music.player.R;
import com.boom.music.player.Setting.SettingActivity;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by reyansh on 9/20/17.
 */

public class ArtistArtDownloadService extends Service {

    private CompositeDisposable mCompositeDisposable;
    private Common mApp;
    private ArrayList<Artist> mArtist;
    private LastFmInterface mApiInterface;
    private Notification.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int mNotificationId = 58;

    @Override
    public void onCreate() {
        super.onCreate();

        mCompositeDisposable = new CompositeDisposable();
        mApiInterface = ApiClient.getClient().create(LastFmInterface.class);
        mApp = (Common) getApplicationContext();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this);

        mNotificationBuilder.setContentTitle(getResources().getString(R.string.downloading_artist_arts))
                .setContentText(getResources().getString(R.string.downloading_art_for))
                .setSmallIcon(R.mipmap.ic_music_file);

        startForeground(mNotificationId, mNotificationBuilder.build());

        mCompositeDisposable.add(Observable.fromCallable(() -> downloadAndUpdateArts())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(downloadAndUpdateObserver));

    }

    DisposableObserver<Boolean> downloadAndUpdateObserver = new DisposableObserver<Boolean>() {
        @Override
        public void onNext(Boolean value) {
            mNotificationBuilder.setContentText(getString(R.string.download_complete)).setProgress(0, 0, false);
            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
            Toast.makeText(getApplicationContext(), R.string.artist_art_downloaded, Toast.LENGTH_SHORT).show();
            stopSelf();
            stopForeground(false);
        }

        @Override
        public void onError(Throwable e) {
            Logger.exp(e.getMessage());
        }

        @Override
        public void onComplete() {

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
        mCompositeDisposable.dispose();
        mNotificationBuilder.setContentText(getString(R.string.download_complete)).setProgress(0, 0, false);
        mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
        stopForeground(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Boolean downloadAndUpdateArts() {
        mArtist = mApp.getDBAccessHelper().getAllArtist();

        int incr = 0;
        for (Artist artist : mArtist) {
            incr++;
            mNotificationBuilder.setContentTitle(getResources().getString(R.string.downloading_artist_arts))
                    .setContentText(getResources().getString(R.string.downloading_art_for) + " '" + artist._artistName + "'")
                    .setSmallIcon(R.mipmap.ic_music_file);

            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            intent.putExtra(Constants.FROM_NOTIFICATION, true);
            intent.putExtra(Constants.FROM_ALBUMS_NOTIFICATION, false);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            mNotificationBuilder.setContentIntent(pendingIntent);

            mNotificationBuilder.setProgress(mArtist.size(), incr, false);
            mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
            try {
                String cachedUrl = updateArtistArtNow(artist._artistId, artist._artistName);
                Logger.log(cachedUrl);
            } catch (Exception e) {
                continue;
            }
        }

        return true;
    }

    private String updateArtistArtNow(long artistId, String artistName) {
        String cachedUrl = putBitmapInDiskCache(artistId, artistName, mApiInterface);
        if (cachedUrl != null && !cachedUrl.equals("") && cachedUrl.length() > 1)
            mApp.getDBAccessHelper().updateArtistAlbumArt(artistId, cachedUrl);
        return cachedUrl;
    }


    public String putBitmapInDiskCache(long artistId, String artistName, LastFmInterface lastFmInterface) {
        File cacheDir = new File(Common.getInstance().getCacheDir(), "artistThumbnails");
        Bitmap avatar = null;

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File cacheFile = new File(cacheDir, "" + artistId);
        try {
            if (cacheFile.exists()) {
                return "file://" + cacheFile.getPath();
            } else {
                cacheFile.createNewFile();
            }


            Response<ArtistModel> responseBodyCall = lastFmInterface.getArtist(artistName).execute();
            if (responseBodyCall.isSuccessful()) {
                avatar = ImageLoader.getInstance().loadImageSync(responseBodyCall.body().artist.image.get(4).url);
            }

            if (avatar != null) {
                FileOutputStream fos = new FileOutputStream(cacheFile);
                avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                return "file://" + cacheFile.getPath();
            } else {
                cacheFile.delete();
                return null;
            }
        } catch (Exception e) {
            Logger.log("" + e.getCause());
            return null;
        }
    }
}
