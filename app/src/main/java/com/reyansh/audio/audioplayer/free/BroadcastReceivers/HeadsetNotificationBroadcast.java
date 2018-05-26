package com.reyansh.audio.audioplayer.free.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.reyansh.audio.audioplayer.free.Common;


/**
*Broadcast Receiver of headphone button clicks.
*/

public class HeadsetNotificationBroadcast extends BroadcastReceiver {
    private Common mApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mApp = (Common) context.getApplicationContext();
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    mApp.getPlayBackStarter().playSongs();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    mApp.getPlayBackStarter().pauseSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    mApp.getPlayBackStarter().playSongs();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    mApp.getPlayBackStarter().pauseSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    mApp.getPlayBackStarter().pauseSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    mApp.getPlayBackStarter().nextSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    mApp.getPlayBackStarter().previousSong();
                    break;
                default:
                    break;
            }
        }
    }

}