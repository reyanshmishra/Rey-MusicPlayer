package com.boom.music.player.LauncherActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.boom.music.player.Album.AlbumFragment;
import com.boom.music.player.Artist.FragmentArtist;
import com.boom.music.player.Common;
import com.boom.music.player.FileDirectory.FolderFragment;
import com.boom.music.player.Genres.FragmentGenres;
import com.boom.music.player.PlayList.PlaylistFragment;
import com.boom.music.player.R;
import com.boom.music.player.Songs.SongsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SwipeAdapter extends FragmentPagerAdapter {
    private Map<Integer, String> mFragmentTags;
    private FragmentManager mFragmentManager;

    private String mPageTile[];

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public SwipeAdapter(FragmentManager fm) {
        super(fm);
        mPageTile = Common.getInstance().getResources().getStringArray(R.array.fragments_titles);
        mFragmentManager = fm;
        mFragmentTags = new HashMap<>();

        fragments.add(new AlbumFragment());
        fragments.add(new FragmentArtist());
        fragments.add(new SongsFragment());
        fragments.add(new FragmentGenres());
        fragments.add(new PlaylistFragment());
        fragments.add(new FolderFragment());

    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
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