package com.boom.music.player.SubGridViewActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boom.music.player.Animations.TranslateAnimation;
import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Interfaces.OnAdapterItemClicked;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Album;
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Search.SearchActivity;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class TracksSubGridViewFragment extends Fragment implements MusicUtils.Defs, OnAdapterItemClicked, OnTaskCompleted {

    private boolean mGoingBack = false;

    private ArrayList<Album> mAlbums;
    private SubGridViewAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private Context mContext;

    private TextView mHeaderTextView;
    private TextView mSubHeaderTextView;
    private ImageView mHeaderImage;
    private ImageButton mHeaderPopUp;
    private Common mApp;
    private View mView;

    private RelativeLayout mHeaderLayout;

    private String HEADER_TITLE;
    private int HEADER_SUB_TITLE;


    private String FROM_WHERE;
    private String SELECTION_VALUE;
    private String COVER_PATH;

    private Button mPlayAllButton;
    private Handler mHandler;
    private View mDummyBackgroundView;

    private OnScrolledListener mOnScrolledListener;
    private ImageButton mSearchButton;
    private ImageButton mBackButton;
    private int mPosition;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_browser_grid_list, container, false);
        mContext = Common.getInstance();
        mApp = (Common) mContext.getApplicationContext();
        Bundle bundle = getArguments();
        mView.setOnClickListener(v -> Logger.log("INTER"));

        mDummyBackgroundView = mView.findViewById(R.id.background);

        mHeaderLayout = (RelativeLayout) mView.findViewById(R.id.browser_sub_header_layout);
        mHeaderImage = (ImageView) mView.findViewById(R.id.browser_sub_header_image);
        mHeaderTextView = (TextView) mView.findViewById(R.id.browser_sub_header_text);
        mSubHeaderTextView = (TextView) mView.findViewById(R.id.browser_sub_header_sub_text);
        mHeaderPopUp = (ImageButton) mView.findViewById(R.id.overflow);
        mPlayAllButton = (Button) mView.findViewById(R.id.browser_sub_play_all);

        mSearchButton = (ImageButton) mView.findViewById(R.id.search_button);
        mBackButton = (ImageButton) mView.findViewById(R.id.back_button);

        mHeaderTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Futura-Book-Font"));
        mSubHeaderTextView.setTypeface(TypefaceHelper.getTypeface(mContext, "Futura-Book-Font"));
        mPlayAllButton.setTypeface(TypefaceHelper.getTypeface(mContext, "Futura-Bold-Font"));

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.browser_sub_grid_view);


        mRecyclerView.setOnTouchListener((v, event) -> {
            MotionEvent e = MotionEvent.obtain(event);
            mHeaderLayout.dispatchTouchEvent(e);
            e.recycle();
            return false;
        });


        HEADER_TITLE = bundle.getString(Constants.HEADER_TITLE);
        HEADER_SUB_TITLE = bundle.getInt(Constants.HEADER_SUB_TITLE);
        FROM_WHERE = bundle.getString(Constants.FROM_WHERE);
        SELECTION_VALUE = "" + bundle.getLong(Constants.SELECTION_VALUE);
        COVER_PATH = bundle.getString(Constants.COVER_PATH);

        mHeaderTextView.setText(HEADER_TITLE);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, Common.getNumberOfColms()));

        mHeaderPopUp.setOnClickListener(v -> onPopUpClicked(v));

        mPlayAllButton.setOnClickListener(v -> {
            ArrayList<Song> songs = CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE);
            if (songs.size() > 0) {
                mApp.getPlayBackStarter().playSongs(CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE), 0);
                startActivity(new Intent(getActivity(), NowPlayingActivity.class));
            } else {
                removeFragment();
            }
        });


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                try {

                    GridLayoutManager linearLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    View topChild = recyclerView.getChildAt(0);

                    int scrollY = -(topChild.getTop()) + findRealFirstVisibleItemPosition(linearLayoutManager.findFirstVisibleItemPosition()) * topChild.getHeight();

                    int adjustedScrollY = (int) ((-scrollY) - mApp.convertDpToPixels(280.0f, mContext));

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHeaderLayout.getLayoutParams();
                    params.topMargin = (adjustedScrollY / 3);
                    mHeaderLayout.setLayoutParams(params);


                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log("" + "CREASH");
                }


            }
        });
        mAdapter = new SubGridViewAdapter(this);


        if (HEADER_SUB_TITLE == 1) {
            mSubHeaderTextView.setText(HEADER_SUB_TITLE + " " + "album");
        } else {
            mSubHeaderTextView.setText(HEADER_SUB_TITLE + " " + "albums");
        }

        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                if (mOnScrolledListener != null)
                    mOnScrolledListener.onScrolledUp();
            }

            @Override
            public void onShow() {
                if (mOnScrolledListener != null)
                    mOnScrolledListener.onScrolledDown();
            }

        });

        setHeaderImage();
        mHandler = new Handler();
        mHandler.postDelayed(initGridView, 50);
        mHandler.postDelayed(animateContent, 50);
        mHandler.postDelayed(animatebackground, 50);

        RelativeLayout relativeLayout = (RelativeLayout) mSearchButton.getParent();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getActivity());
        relativeLayout.setLayoutParams(params);

        mSearchButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));
        mBackButton.setOnClickListener(v -> removeFragment());

        mRecyclerView.setAdapter(mAdapter);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAlbums = CursorHelper.getAlbumsForSelection(FROM_WHERE, SELECTION_VALUE);
        mAdapter.update(mAlbums);
        if (mAlbums.size() == 0) {
            removeFragment();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mOnScrolledListener = (OnScrolledListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnScrolledListener = null;
    }

    /**
     * Animates the content views in.
     */
    private Runnable animateContent = new Runnable() {

        @Override
        public void run() {

            //Slide down the header image.


            TranslateAnimation slideDown = new TranslateAnimation(mHeaderLayout, 500, null,
                    View.VISIBLE, Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -2.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);

            slideDown.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    mHeaderLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });

            slideDown.animate();
        }

    };


    /**
     * Animates the content views in.
     */
    private Runnable animatebackground = new Runnable() {

        @Override
        public void run() {

            //Slide down the header image.


            TranslateAnimation slideDown = new TranslateAnimation(mDummyBackgroundView, 500, null,
                    View.VISIBLE, Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -2.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);

            slideDown.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    mDummyBackgroundView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }

            });

            slideDown.animate();
        }

    };


    private void setHeaderImage() {
        try {
            Bitmap artwork = ImageLoader.getInstance().loadImageSync(COVER_PATH);
            if (artwork != null)
                mHeaderImage.setImageBitmap(artwork);
            else {
                mHeaderImage.setImageResource(R.drawable.ic_placeholder);
                int padding = MusicUtils.getDPFromPixel(120);
                mHeaderImage.setPadding(padding, padding, padding, padding);
            }
        } catch (Exception e) {
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri treeUri = data.getData();
                    Common.getInstance().getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    try {
                        MusicUtils.deleteFile(TracksSubGridViewFragment.this, CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE), this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void onPopUpMenuClickListener(View v, final int position) {
        final PopupMenu menu = new PopupMenu(getActivity(), v);
        mPosition = position;
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), sub, 0);
        ArrayList<Song> songs = CursorHelper.getTracksForSelection("ALBUMS", "" + mAlbums.get(mPosition)._Id);
        if (checkSongsEmpty(songs, mPosition)) return;

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_album_play_next:
                    new AsyncAddTo(mAlbums.get(mPosition)._albumName, false, songs).execute();
                    return true;
                case R.id.popup_album_add_to_queue:
                    new AsyncAddTo(mAlbums.get(mPosition)._albumName, true, songs).execute();
                    return true;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(songs));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getChildFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, songs);
                    return true;
                case R.id.popup_album_delete:
                    try {
                        MusicUtils.deleteFile(TracksSubGridViewFragment.this, songs, this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    return true;
                default:
                    break;
            }
            return false;
        });
        menu.inflate(R.menu.popup_album);
        menu.show();
    }

    @Override
    public void OnPopUpMenuClicked(View view, int position) {
        onPopUpMenuClickListener(view, position);
    }

    @Override
    public void OnShuffledClicked() {

    }


    @Override
    public void onSongDeleted() {
        mAlbums.remove(mPosition);
        mAdapter.update(mAlbums);
        if (mAlbums.size() == 0) {
            removeFragment();
        }
    }


    private Runnable initGridView = new Runnable() {

        @Override
        public void run() {
            android.view.animation.TranslateAnimation animation = new
                    android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 2.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);

            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationEnd(Animation arg0) {

                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationStart(Animation arg0) {
                    mRecyclerView.setVisibility(View.VISIBLE);


                }

            });

            mRecyclerView.startAnimation(animation);
        }

    };


    private void slideAwayHeader() {
        if (mGoingBack) return;
        TranslateAnimation slideDown = new TranslateAnimation(mHeaderLayout, 500, new AccelerateInterpolator(2.0f),
                View.INVISIBLE, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -2.0f);

        slideDown.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHeaderLayout.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        slideDown.animate();
    }


    private void slideAwayBackground() {
        TranslateAnimation slideDown = new TranslateAnimation(mDummyBackgroundView, 500, new AccelerateInterpolator(2.0f),
                View.INVISIBLE, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -2.0f);

        slideDown.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDummyBackgroundView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        slideDown.animate();
    }


    private void slideAwayGridView() {
        if (mGoingBack) return;
        android.view.animation.TranslateAnimation animation = new
                android.view.animation.TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 2.0f);

        animation.setDuration(500);
        animation.setInterpolator(new AccelerateInterpolator(2.0f));
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation arg0) {
                mRecyclerView.setVisibility(View.INVISIBLE);
                mGoingBack = false;
                getActivity().getSupportFragmentManager().beginTransaction().remove(TracksSubGridViewFragment.this).commit();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {
                mGoingBack = true;
            }

        });
        mRecyclerView.startAnimation(animation);
    }

    public void removeFragment() {
        slideAwayHeader();
        slideAwayBackground();
        slideAwayGridView();
    }


    private void onPopUpClicked(View view) {
        final PopupMenu menu = new PopupMenu(getActivity(), view);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), sub, 0);
        ArrayList<Song> songs = CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE);
        if (songs.size() == 0) {
            removeFragment();
            return;
        }

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_play_next:
                    if (mAlbums.size() != 0) {
                        new AsyncAddTo(HEADER_TITLE, false, songs).execute();
                    }
                    break;
                case R.id.menu_shuffle:
                    if (mAlbums.size() != 0) {
                        mApp.getPlayBackStarter().shuffleUp(songs);
                    }
                    break;
                case R.id.menu_add_to_queue:
                    if (mAlbums.size() != 0)
                        new AsyncAddTo(HEADER_TITLE, true, songs).execute();
                    break;
                case R.id.action_search:
                    Intent intent = new Intent(Intent.ACTION_SEARCH);
                    intent.putExtra(SearchManager.QUERY, HEADER_TITLE);
                    startActivity(intent);
                    break;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(songs));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getChildFragmentManager(), "FRAGMENT_TAG");
                    break;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, songs);
                    break;
                case R.id.menu_delete:
                    try {
                        MusicUtils.deleteFile(this, songs, this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    break;

            }
            return false;
        });

        menu.inflate(R.menu.popup_sub_list_menu);
        menu.getMenu().findItem(R.id.menu_edit_tags).setVisible(false);
        menu.show();
    }

    public int findRealFirstVisibleItemPosition(int pos) {
        View view;
        final GridLayoutManager linearLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        while (pos > 0) {
            view = linearLayoutManager.findViewByPosition(pos - 1);
            if (view == null) {
                break;
            }
            pos = pos - 1;
        }
        return pos / 2;
    }

    public String getFROMWHERE() {
        return FROM_WHERE;
    }

    public boolean checkSongsEmpty(ArrayList<Song> songs, int pos) {
        if (songs.size() == 0) {
            mAlbums.remove(pos);
            mAdapter.updateData(mAlbums);
            Toast.makeText(mContext, R.string.no_songs_in_this_album, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


}