package com.boom.music.player.AppWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

import com.boom.music.player.Common;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.R;
import com.boom.music.player.Services.QueueWidgetService;
import com.boom.music.player.Utils.Constants;

/**
 * Created by reyansh on 12/26/17.
 */

public class QueueWidgetProvider extends AppWidgetProvider {
    private Common mApp;
    private int mAppWidgetIds[];


    @Override
    public void onReceive(Context context, Intent intent) {
        mApp = (Common) context.getApplicationContext();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), QueueWidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
        mAppWidgetIds = appWidgetIds;

        onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;


        for (int i = 0; i < N; i++) {
            int currentAppWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.queue_widget_layout);


            if (mApp.isServiceRunning()) {
                Bitmap art = mApp.getService().getSongDataHelper().getAlbumArt();
                if (art != null) {
                    views.setImageViewBitmap(R.id.widget_album_art, mApp.getService().getSongDataHelper().getAlbumArt());
                } else {
                    views.setImageViewResource(R.id.widget_album_art, R.mipmap.ic_launcher);
                }

                String songTitle = mApp.getService().getSongDataHelper().getTitle();
                views.setTextViewText(R.id.songName5, songTitle);

                String songArtistName = mApp.getService().getSongDataHelper().getArtist();
                views.setTextViewText(R.id.artistName, songArtistName);

                Intent playPauseIntent = new Intent();
                playPauseIntent.setAction(Constants.ACTION_PAUSE);
                playPauseIntent.putExtra("isfromSmallWidget", true);
                PendingIntent playpausePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, playPauseIntent, 0);
                views.setOnClickPendingIntent(R.id.notification_expanded_base_play, playpausePendingIntent);


                Intent previousIntent = new Intent();
                previousIntent.setAction(Constants.ACTION_PREVIOUS);
                PendingIntent previousPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, previousIntent, 0);
                views.setOnClickPendingIntent(R.id.notification_expanded_base_previous, previousPendingIntent);

                Intent nextIntent = new Intent();
                nextIntent.setAction(Constants.ACTION_NEXT);
                PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, nextIntent, 0);
                views.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextPendingIntent);


                Intent nowPlayingIntent = new Intent(context, MainActivity.class);
                nowPlayingIntent.putExtra(Constants.FROM_NOTIFICATION, true);
                PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, 0);
                views.setOnClickPendingIntent(R.id.widget_album_art, configPendingIntent);


                if (mApp.isServiceRunning()) {
                    if (mApp.getService().getMediaPlayer().isPlaying()) {
                        views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
                    } else {
                        views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
                    }
                } else {
                    views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
                }

            /* Create a pendingIntent that will serve as a general template for the clickListener.
             * We'll create a fillInIntent in LargeWidgetAdapterService.java that will provide the
             * index of the listview item that's been clicked. */

                Intent intent = new Intent();
                intent.setAction("com.boom.music.player.action.STOP");
                PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(mApp.getApplicationContext(), 0, intent, 0);
                views.setPendingIntentTemplate(R.id.listview, pendingIntentTemplate);


                //Create the intent to fire up the service that will back the adapter of the listview.
                Intent serviceIntent = new Intent(mApp.getApplicationContext(), QueueWidgetService.class);
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds[i]);
                serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
                Common.getInstance().sendBroadcast(serviceIntent);

                views.setRemoteAdapter(R.id.listview, serviceIntent);

                appWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetIds, R.id.recyclerView);

                try {
                    appWidgetManager.updateAppWidget(currentAppWidgetId, views);
                } catch (Exception e) {
                    continue;
                }
            }

        }
    }


}
