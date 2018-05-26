package com.reyansh.audio.audioplayer.free.AppWidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.LauncherActivity.MainActivity;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.Constants;

/**
 * Created by Reyansh on 22/05/2016.
 * <p>
 * This is the just the small widget which can be put on the main screen and the songs can be played.
 */
public class SmallWidgetProvider extends AppWidgetProvider {

    private Common mApp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        mApp = (Common) context.getApplicationContext();

        for (int i = 0; i < appWidgetIds.length; i++) {
            int currentAppWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);


            Intent playpauseIntent = new Intent();
            playpauseIntent.setAction(Constants.ACTION_PAUSE);
            playpauseIntent.putExtra("isfromSmallWidget", true);
            PendingIntent playpausePendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, playpauseIntent, 0);
            views.setOnClickPendingIntent(R.id.notification_expanded_base_play, playpausePendingIntent);

            Intent intent = new Intent(context, SmallWidgetProvider.class);
            intent.setAction("use_custom_class");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.notification_expanded_base_play, pendingIntent);

            Intent nextIntent = new Intent();
            nextIntent.setAction(Constants.ACTION_NEXT);
            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, nextIntent, 0);
            views.setOnClickPendingIntent(R.id.notification_expanded_base_next, nextPendingIntent);


            Intent nowPlayingIntent = new Intent(context, MainActivity.class);
            nowPlayingIntent.putExtra(Constants.FROM_NOTIFICATION, true);
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, 0);
            views.setOnClickPendingIntent(R.id.notification_expanded_base_image, configPendingIntent);


            if (mApp.isServiceRunning()) {
                if (mApp.getService().getMediaPlayer().isPlaying()) {
                    views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_pause_light);
                } else {
                    views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
                }
            } else {
                views.setImageViewResource(R.id.notification_expanded_base_play, R.drawable.btn_playback_play_light);
            }


            if (mApp.isServiceRunning()) {
                views.setTextViewText(R.id.notification_expanded_base_line_one, mApp.getService().getSongDataHelper().getTitle());
                views.setTextViewText(R.id.notification_expanded_base_line_two, mApp.getService().getSongDataHelper().getAlbum());
                views.setTextViewText(R.id.notification_expanded_base_line_three, mApp.getService().getSongDataHelper().getArtist());
                views.setImageViewBitmap(R.id.notification_expanded_base_image, mApp.getService().getSongDataHelper().getAlbumArt());
                views.setImageViewBitmap(R.id.expnotifbg, mApp.getService().getSongDataHelper().getAlbumArt());
            }

            try {
                appWidgetManager.updateAppWidget(currentAppWidgetId, views);
            } catch (Exception e) {
                continue;
            }

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        String actionName = "use_custom_class";
        if (actionName.equals(action)) {
            mApp = (Common) context.getApplicationContext();
            mApp.getPlayBackStarter().playPauseSongs();
        }
    }
}
