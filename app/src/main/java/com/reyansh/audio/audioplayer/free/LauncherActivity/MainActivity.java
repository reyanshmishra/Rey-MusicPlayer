package com.reyansh.audio.audioplayer.free.LauncherActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.reyansh.audio.audioplayer.free.Activities.TracksSubFragment;
import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.FileDirectory.FolderFragment;
import com.reyansh.audio.audioplayer.free.Interfaces.OnScrolledListener;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Search.SearchActivity;
import com.reyansh.audio.audioplayer.free.Setting.SettingActivity;
import com.reyansh.audio.audioplayer.free.Songs.SongsFragment;
import com.reyansh.audio.audioplayer.free.SubGridViewFragment.TracksSubGridViewFragment;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.CursorHelper;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.reyansh.audio.audioplayer.free.Utils.SortOrder;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnScrolledListener {

    public ViewPager mViewPager;
    public SwipeAdapter mAdapter;
    private Context mContext;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private Menu mMenu;
    private AppBarLayout mAppBarLayout;
    private ArrayList<Fragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        mFragments = new ArrayList<>();
        mTabLayout = findViewById(R.id.id_tabs);
        mViewPager = findViewById(R.id.view_pager);

        String[] tabs=getTabs();
        mAdapter = new SwipeAdapter(getSupportFragmentManager(),tabs);

        mViewPager.setAdapter(mAdapter);

        setDefaultTab(tabs);

        mViewPager.setOffscreenPageLimit(5);

        mTabLayout.setupWithViewPager(mViewPager);

        MusicUtils.changeTabsFont(mContext, mTabLayout);
        MusicUtils.applyFontForToolbarTitle(this);
        mToolbar = findViewById(R.id.toolbar);
        mAppBarLayout = findViewById(R.id.id_toolbar_container);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(this);
        mAppBarLayout.setLayoutParams(params);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> {
            onPrepareOptionsMenu(mMenu);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        });

    }

    private void setDefaultTab(String[] tabs){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Common.getInstance());
        String startup_screen = sharedPreferences.getString("preference_key_startup_screen", "SONGS");
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].equalsIgnoreCase(startup_screen)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        mMenu = menu;
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        switch (mViewPager.getCurrentItem()) {
            case 0:
                getMenuInflater().inflate(R.menu.menu_album, menu);

                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    menu.findItem(R.id.album_sort_type).setChecked(true);
                } else {
                    menu.findItem(R.id.album_sort_type).setChecked(false);
                }

                String albumSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_DEFAULT);

                if (albumSortOrder.equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_DEFAULT)) {
                    menu.findItem(R.id.album_sort_default).setChecked(true);
                } else if (albumSortOrder.equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_NAME)) {
                    menu.findItem(R.id.album_sort_name).setChecked(true);
                } else if (albumSortOrder.equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS)) {
                    menu.findItem(R.id.album_sort_no_of_songs).setChecked(true);
                } else if (albumSortOrder.equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_YEAR)) {
                    menu.findItem(R.id.album_sort_year).setChecked(true);
                } else if (albumSortOrder.equalsIgnoreCase(SortOrder.AlbumSortOrder.ALBUM_ARTIST)) {
                    menu.findItem(R.id.album_sort_artist_name).setChecked(true);
                }

                break;
            case 1:
                getMenuInflater().inflate(R.menu.menu_artist, menu);

                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    menu.findItem(R.id.artist_sort_type).setChecked(true);
                } else {
                    menu.findItem(R.id.artist_sort_type).setChecked(false);
                }

                String artistSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NAME);

                if (artistSortOrder.equalsIgnoreCase(SortOrder.ArtistSortOrder.ARTIST_NAME)) {
                    menu.findItem(R.id.artist_sort_name).setChecked(true);
                } else if (artistSortOrder.equalsIgnoreCase(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS)) {
                    menu.findItem(R.id.artist_sort_no_of_albums).setChecked(true);
                } else if (artistSortOrder.equalsIgnoreCase(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS)) {
                    menu.findItem(R.id.artist_sort_no_of_songs).setChecked(true);
                }

                break;
            case 2:

                break;
            case 3:
                getMenuInflater().inflate(R.menu.menu_genre, menu);

                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.GENRE_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    menu.findItem(R.id.genre_sort_type).setChecked(true);
                } else {
                    menu.findItem(R.id.genre_sort_type).setChecked(false);
                }

                String genreSortOrder = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.GENRE_SORT_ORDER, SortOrder.GenreSortOrder.GENRE_NAME);

                if (genreSortOrder.equalsIgnoreCase(SortOrder.GenreSortOrder.GENRE_NAME)) {
                    menu.findItem(R.id.genre_sort_name).setChecked(true);
                } else if (genreSortOrder.equalsIgnoreCase(SortOrder.GenreSortOrder.GENRE_NUMBER_OF_ALBUMS)) {
                    menu.findItem(R.id.genre_sort_no_of_albums).setChecked(true);
                }

                break;
            case 4:
                getMenuInflater().inflate(R.menu.menu_playlists, menu);
                break;
            case 5:
                getMenuInflater().inflate(R.menu.menu_playlists, menu);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_search:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            case R.id.album_sort_default:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_DEFAULT);
                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;
            case R.id.album_sort_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_NAME);
                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;
            case R.id.album_sort_year:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_YEAR);
                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;
            case R.id.album_sort_artist_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_ARTIST);
                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;
            case R.id.album_sort_no_of_songs:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;
            case R.id.album_sort_type:
                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.DESCENDING);
                } else {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.ALBUM_SORT_TYPE, Constants.ASCENDING);
                }

                (mAdapter.getFragment(0)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.artist_sort_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NAME);
                (mAdapter.getFragment(1)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.artist_sort_no_of_albums:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                (mAdapter.getFragment(1)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.artist_sort_no_of_songs:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                (mAdapter.getFragment(1)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.artist_sort_type:
                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.DESCENDING);
                } else {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.ARTIST_SORT_TYPE, Constants.ASCENDING);
                }
                (mAdapter.getFragment(1)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.genre_sort_name:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.GENRE_SORT_ORDER, SortOrder.GenreSortOrder.GENRE_NAME);
                (mAdapter.getFragment(3)).onResume();
                invalidateOptionsMenu();
                break;

            case R.id.genre_sort_no_of_albums:
                PreferencesHelper.getInstance().put(PreferencesHelper.Key.GENRE_SORT_ORDER, SortOrder.GenreSortOrder.GENRE_NUMBER_OF_ALBUMS);
                (mAdapter.getFragment(3)).onResume();
                invalidateOptionsMenu();
                break;


            case R.id.genre_sort_type:
                if (PreferencesHelper.getInstance().getString(PreferencesHelper.Key.GENRE_SORT_TYPE, Constants.ASCENDING).equalsIgnoreCase(Constants.ASCENDING)) {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.GENRE_SORT_TYPE, Constants.DESCENDING);
                } else {
                    PreferencesHelper.getInstance().put(PreferencesHelper.Key.GENRE_SORT_TYPE, Constants.ASCENDING);
                }
                (mAdapter.getFragment(3)).onResume();
                invalidateOptionsMenu();
                break;


            case R.id.item_shuffle:
                ((SongsFragment) mAdapter.getFragment(2)).shuffleSongs();
                return true;
            case R.id.item_settings:
                startActivity(new Intent(mContext, SettingActivity.class));
                break;

            /* Album sorting options*/
        }
        return super.onOptionsItemSelected(item);
    }

    private String[] getTabs(){
        String titles = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.TITLES);
        if (titles==null){
            String []tabTitles= new String[]{"ALBUMS", "ARTISTS", "SONGS", "GENRES", "PLAYLISTS", "DIRECTORY"};
            CursorHelper.saveTabTitles(tabTitles);
             return tabTitles;
        }else{
            Gson gson = new Gson();
            return gson.fromJson(titles, String[].class);
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragments.size() > 0) {
            Fragment fragment = mFragments.get(mFragments.size() - 1);
            if (fragment instanceof TracksSubFragment) {
                ((TracksSubFragment) fragment).removeFragment();
            }
            if (fragment instanceof TracksSubGridViewFragment) {
                ((TracksSubGridViewFragment) fragment).removeFragment();
            }
            mFragments.remove(fragment);
            return;
        }

        if (mAdapter.getFragment(mViewPager.getCurrentItem()) instanceof FolderFragment) {
            FolderFragment folderFragment = (FolderFragment) mAdapter.getFragment(mViewPager.getCurrentItem());
            if (folderFragment.getCurrentDir().equals("/")) {
                goHomeScreen();
            } else {
                folderFragment.getParentDir();
            }
        } else {
            goHomeScreen();
        }
    }

    private void goHomeScreen() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onScrolledUp() {

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) findViewById(R.id.bottom_bar).getLayoutParams();
        int fabBottomMargin = lp.bottomMargin + 150;

        (findViewById(R.id.bottom_bar))
                .animate()
                .translationY(findViewById(R.id.bottom_bar).getHeight() + fabBottomMargin)
                .setInterpolator(new AccelerateInterpolator(2))
                .start();
    }

    @Override
    public void onScrolledDown() {
        (findViewById(R.id.bottom_bar))
                .animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(2));
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void addFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_parent, fragment);
        fragmentTransaction.commitAllowingStateLoss();

        mFragments.add(fragment);
    }


}





