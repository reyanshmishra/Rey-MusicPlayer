package com.boom.music.player.Services;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.boom.music.player.Common;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;
import com.boom.music.player.Utils.MusicUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;

/**
 * Created by reyansh on 12/26/17.
 */

public class QueueWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }


    class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private Common mApp;
        private ArrayList<Song> songs;

        public StackRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mApp = (Common) mContext.getApplicationContext();
            if (mApp.isServiceRunning()) {
                songs = mApp.getService().getSongList();
            }
        }

        @Override
        public int getCount() {
            if (songs != null) {
                return songs.size();
            } else {
                return 0;
            }

        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.queue_widget_listview_layout);
            if (position <= getCount()) {

                String songTitle = songs.get(position)._title;

                ImageSize imageSize = new ImageSize(75, 75);

                long songDurationInMillis = 0;
                try {
                    songDurationInMillis = songs.get(position)._duration;
                } catch (Exception e) {
                }

                remoteViews.setTextViewText(R.id.listViewSubText, songTitle);
                remoteViews.setTextViewText(R.id.listViewRightSubText, MusicUtils.convertMillisToMinsSecs(songDurationInMillis));


                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(String.valueOf(MusicUtils.getAlbumArtUri(mApp.getService().getSongList().get(position)._albumId)), imageSize);
                if (bitmap != null) {
                    remoteViews.setImageViewBitmap(R.id.listViewLeftIcon, bitmap);
                } else {
                    remoteViews.setImageViewResource(R.id.listViewLeftIcon, R.mipmap.ic_launcher);
                }

            }


          /* This intent latches itself onto the pendingIntentTemplate from
         * LargeWidgetProvider.java and adds the extra "INDEX" argument to it. */
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("INDEX", position);
            remoteViews.setOnClickFillInIntent(R.id.listViewParent, fillInIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onDataSetChanged() {

            if (mApp.isServiceRunning()) {
                songs = mApp.getService().getSongList();
            }

        }

        @Override
        public void onDestroy() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onCreate() {
            // TODO Auto-generated method stub

        }

    }
}
