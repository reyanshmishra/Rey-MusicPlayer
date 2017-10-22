package com.boom.music.player.LauncherActivity;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.boom.music.player.Album.AlbumFragment;
import com.boom.music.player.Artist.FragmentArtist;
import com.boom.music.player.FileDirectory.FolderFragment;
import com.boom.music.player.Genres.FragmentGenres;
import com.boom.music.player.PlayList.PlaylistFragment;
import com.boom.music.player.R;
import com.boom.music.player.Songs.SongsFragment;

import java.util.HashMap;
import java.util.Map;

public class SwipeAdapter extends FragmentPagerAdapter {
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;

    private String mPageTile[];

    public SwipeAdapter(FragmentManager fm, Context context) {
        super(fm);
        mPageTile = context.getResources().getStringArray(R.array.fragments_titles);
        mFragmentManager = fm;
        mFragmentTags = new HashMap<>();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AlbumFragment();
            case 1:
                return new FragmentArtist();
            case 2:
                return new SongsFragment();
            case 3:
                return new FragmentGenres();
            case 4:
                return new PlaylistFragment();
            case 5:
                return new FolderFragment();
            default:
                break;
        }
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        if (obj instanceof Fragment) {
            Fragment f = (Fragment) obj;
            String tag = f.getTag();
            mFragmentTags.put(position, tag);
        }
        return obj;
    }

    public Fragment getFragment(int position) {
        String tag = mFragmentTags.get(position);
        if (tag == null)
            return null;
        return mFragmentManager.findFragmentByTag(tag);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPageTile[position];
    }

    @Override
    public int getCount() {
        return 6;
    }
}