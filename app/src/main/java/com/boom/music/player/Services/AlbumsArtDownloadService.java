package com.boom.music.player.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.boom.music.player.Common;
import com.boom.music.player.Lastfmapi.ApiClient;
import com.boom.music.player.Lastfmapi.LastFmInterface;
import com.boom.music.player.Lastfmapi.Models.AlbumModel;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.Setting.SettingActivity;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.FileUtils;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class AlbumsArtDownloadService extends Service {

    private CompositeDisposable mCompositeDisposable;
    private Common mApp;
    private ArrayList<Song> mSongs;
    private LastFmInterface mApiInterface;
    private Notification.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int mNotificationId = 158;

    @Override
    public void onCreate() {
        super.onCreate();

        mCompositeDisposable = new CompositeDisposable();
        mApiInterface = ApiClient.getClient().create(LastFmInterface.class);
        mApp = (Common) getApplicationContext();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new Notification.Builder(this);

        mNotificationBuilder.setContentTitle(getResources().getString(R.string.downloading_album_arts))
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
            Toast.makeText(getApplicationContext(), R.string.album_art_downloaded, Toast.LENGTH_SHORT).show();
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
        stopSelf();
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
        mSongs = CursorHelper.getTracksForSelection("SONGS", "");
        Bitmap avatar = null;

        for (int i = 0; i < mSongs.size(); i++) {
            showNotification(i);

            try {
                AudioFile audioFile = AudioFileIO.read(new File(mSongs.get(i)._path));
                Tag tag = audioFile.getTagOrCreateAndSetDefault();
                if (tag.getFirstArtwork() != null) {
                    continue;
                }

                Response<AlbumModel> responseBodyCall = mApiInterface.getAlbum(mSongs.get(i)._album, mSongs.get(i)._artist).execute();
                try {
                    if (responseBodyCall.isSuccessful()) {
                        avatar = ImageLoader.getInstance().loadImageSync(responseBodyCall.body().album.image.get(4).url);
                    }
                } catch (Exception e) {
                    avatar = ImageLoader.getInstance().loadImageSync(Constants.defaultArtUrl);
                }

                File originalFile = new File(mSongs.get(i)._path);

                if (MusicUtils.isFromSdCard(mSongs.get(i)._path)) {
                    File tempFile;

                    /**
                     *Create a temp file the internal storage where there is no boundry of editing any file.
                     */

                    tempFile = new File(Common.getInstance().getExternalCacheDir().getPath(), originalFile.getName());

                    /**
                     *Copy the sdcard file to internal storage where you can edit it freely.
                     */
                    FileUtils.copyFile(originalFile, tempFile);
                    /**
                     *Set tags or edit the temp file.
                     */
                    setAlbumArt(tempFile, avatar, mSongs.get(i));
                    /**
                     *Copy it back to its original position i.e. in sdcard.
                     */
                    FileUtils.cutFile(tempFile, originalFile);

                } else {
                    File orgFile = new File(mSongs.get(i)._path);
                    setAlbumArt(orgFile, avatar, mSongs.get(i));
                }
                MediaScannerConnection.scanFile(Common.getInstance(), new String[]{originalFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                    @Override
                    public void onMediaScannerConnected() {

                    }

                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Logger.log("SUCCESSFULL TAGGED");
                    }
                });


            } catch (Exception e) {
                continue;
            }
        }
        return true;
    }


    private void showNotification(int i) {
        mNotificationBuilder.setContentTitle(getResources().getString(R.string.downloading_album_arts))
                .setContentText(getResources().getString(R.string.downloading_art_for) + " '" + mSongs.get(i)._album + "'")
                .setSmallIcon(R.mipmap.ic_music_file);

        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        intent.putExtra(Constants.FROM_ALBUMS_NOTIFICATION, true);
        intent.putExtra(Constants.FROM_NOTIFICATION, false);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        mNotificationBuilder.setContentIntent(pendingIntent);

        mNotificationBuilder.setProgress(mSongs.size(), i, false);
        mNotificationManager.notify(mNotificationId, mNotificationBuilder.build());
    }

    public void setAlbumArt(File file, Bitmap artworkBitmap, Song song) throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException {

        artworkBitmap = Bitmap.createScaledBitmap(artworkBitmap, 500, 500, false);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        artworkBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        File artworkFile = new File(Environment.getExternalStorageDirectory() + "/artwork.jpg");

        if (!artworkFile.exists())
            artworkFile.createNewFile();

        FileOutputStream out = new FileOutputStream(artworkFile);
        artworkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

        Artwork artwork = Artwork.createArtworkFromFile(artworkFile);

        artwork.setBinaryData(byteArray);
        AudioFile audioFile = AudioFileIO.read(file);
        Tag tag = audioFile.getTagOrCreateAndSetDefault();


        if (tag.getFirstArtwork() != null) {
            tag.deleteArtworkField();
            tag.setField(artwork);
        } else {
            tag.addField(artwork);
        }

        Uri uri = MusicUtils.getAlbumArtUri(song._albumId);
        DiskCacheUtils.removeFromCache(uri.toString(), ImageLoader.getInstance().getDiskCache());
        String path = FileUtils.getRealPathFromURI(uri);
        new File(path).delete();
        artworkFile.delete();
    }
}
