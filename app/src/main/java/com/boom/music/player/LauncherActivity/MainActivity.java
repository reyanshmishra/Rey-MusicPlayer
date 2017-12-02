package com.boom.music.player.LauncherActivity;

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

import com.boom.music.player.Activities.TracksSubFragment;
import com.boom.music.player.Common;
import com.boom.music.player.FileDirectory.FolderFragment;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.R;
import com.boom.music.player.Search.SearchActivity;
import com.boom.music.player.Setting.SettingActivity;
import com.boom.music.player.Songs.SongsFragment;
import com.boom.music.player.SubGridViewFragment.TracksSubGridViewFragment;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.SortOrder;

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
        mTabLayout = (TabLayout) findViewById(R.id.id_tabs);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);


        mAdapter = new SwipeAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Common.getInstance());
        int startup_screen = Integer.parseInt(sharedPreferences.getString("preference_key_startup_screen", "2"));

        mViewPager.setCurrentItem(startup_screen);
        mViewPager.setOffscreenPageLimit(5);

        mTabLayout.setupWithViewPager(mViewPager);

        MusicUtils.changeTabsFont(mContext, mTabLayout);
        MusicUtils.applyFontForToolbarTitle(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.id_toolbar_container);


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

        if (mViewPager.getCurrentItem() == 5) {
            FolderFragment folderFragment = (FolderFragment) mAdapter.getFragment(5);
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





