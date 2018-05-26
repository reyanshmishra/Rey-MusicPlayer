package com.reyansh.audio.audioplayer.free.NowPlaying;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.reyansh.audio.audioplayer.free.Common;

public class PlaylistPagerAdapter extends FragmentStatePagerAdapter {
    private Common mApp;
    private NowPlayingActivity mNowPlayingActivity;

    public PlaylistPagerAdapter(NowPlayingActivity nowPlayingActivity, FragmentManager fm) {
        super(fm);
        mNowPlayingActivity = nowPlayingActivity;
        mApp = (Common) Common.getInstance();
    }

    //This method controls the layout that is shown on each screen.
    @Override
    public Fragment getItem(int position) {

        	/* PlaylistPagerFragment.java will be shown on every pager screen. However,
             * the fragment will check which screen (position) is being shown, and will
        	 * update its TextViews and ImageViews to match the song that's being played. */
        Fragment fragment = new PlaylistPagerFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        fragment.setArguments(bundle);
        return fragment;

    }

    @Override
    public int getCount() {
        try {
            if (mApp.isServiceRunning()) {
                return mApp.getService().getSongList().size();
            } else {
                return mNowPlayingActivity.mSongs.size();
            }
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}