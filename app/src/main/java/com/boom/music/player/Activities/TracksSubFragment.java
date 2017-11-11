package com.boom.music.player.Activities;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Search.SearchActivity;
import com.boom.music.player.TagEditor.Id3TagEditorActivity;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.CursorHelper;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.boom.music.player.Views.DividerItemDecoration;
import com.google.firebase.crash.FirebaseCrash;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class TracksSubFragment extends Fragment implements MusicUtils.Defs, OnAdapterItemClicked, OnTaskCompleted {

    public String HEADER_TITLE;
    public String HEADER_SUB_TITLE;
    public String FROM_WHERE;
    public String SELECTION_VALUE;
    private boolean mGoingBack = false;
    private ArrayList<Song> mSongList;
    private SubListViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    Animator.AnimatorListener mRecyclerAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
    private Context mContext;
    private TextView mHeaderTextView;
    private TextView mSubHeaderTextView;
    private ImageView mHeaderImage;
    private ImageButton mHeaderPopUp;
    private int mSelectedPosition;
    private Common mApp;
    private RelativeLayout mHeaderLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private Button mPlayAllButton;
    private View mView;
    private RelativeLayout mParent;
//    private CardView mEmptyCardView;
//    private TextView mEmptyTextView;
private Handler mHandler;
    private OnScrolledListener mOnScrolledListener;
    private ImageButton mSearchButton;
    private ImageButton mBackButton;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_browser_sub_list, container, false);
        mContext = Common.getInstance();
        mApp = (Common) mContext.getApplicationContext();
        mHandler = new Handler();
        Bundle bundle = getArguments();
        mParent = (RelativeLayout) mView.findViewById(R.id.browser_sub_drawer_parent);
        mParent.setOnClickListener(v -> Logger.log("INTERRUPTING THE CLICKS"));

        mPlayAllButton = (Button) mView.findViewById(R.id.browser_sub_play_all);

        mHeaderLayout = (RelativeLayout) mView.findViewById(R.id.browser_sub_header_layout);
        mHeaderImage = (ImageView) mView.findViewById(R.id.browser_sub_header_image);
        mHeaderTextView = (TextView) mView.findViewById(R.id.browser_sub_header_text);
        mSubHeaderTextView = (TextView) mView.findViewById(R.id.browser_sub_header_sub_text);
        mHeaderPopUp = (ImageButton) mView.findViewById(R.id.overflow);

        mSearchButton = (ImageButton) mView.findViewById(R.id.search_button);
        mBackButton = (ImageButton) mView.findViewById(R.id.back_button);

        mHeaderTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));
        mSubHeaderTextView.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOOK));
        mPlayAllButton.setTypeface(TypefaceHelper.getTypeface(mContext, TypefaceHelper.FUTURA_BOLD));

        HEADER_TITLE = bundle.getString(Constants.HEADER_TITLE);
        HEADER_SUB_TITLE = bundle.getString(Constants.HEADER_SUB_TITLE);
        FROM_WHERE = bundle.getString(Constants.FROM_WHERE);
        SELECTION_VALUE = "" + bundle.getLong(Constants.SELECTION_VALUE);


        /**
         *Log the screen event in firebase analytics
         */
        mApp.getFirebaseAnalytics().setCurrentScreen(getActivity(), FROM_WHERE, null);
        FirebaseCrash.log("SQL database failed to initialize");

        mHeaderTextView.setText(HEADER_TITLE);
        mHeaderTextView.setSelected(true);
        mSubHeaderTextView.setSelected(true);


        mRecyclerView = (RecyclerView) mView.findViewById(R.id.browser_sub_list_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(Common.getInstance(), DividerItemDecoration.VERTICAL_LIST, 20, 20));

        mHeaderPopUp.setOnClickListener(v -> onPopUpClicked(v));
        mHeaderPopUp.bringToFront();

        mPlayAllButton.setOnClickListener(v -> {
            if (mSongList.size() != 0) {
                mApp.getPlayBackStarter().playSongs(mSongList, 0);
                startActivity(new Intent(getActivity(), NowPlayingActivity.class));
            }
        });


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                try {

                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    View topChild = recyclerView.getChildAt(0);
                    int scrollY = -(topChild.getTop()) + findRealFirstVisibleItemPosition(linearLayoutManager.findFirstVisibleItemPosition()) * topChild.getHeight();
                    int adjustedScrollY = (int) ((-scrollY) - mApp.convertDpToPixels(0f, mContext));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHeaderLayout.getLayoutParams();
                    params.topMargin = (adjustedScrollY / 3);
                    mHeaderLayout.setLayoutParams(params);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        mAdapter = new SubListViewAdapter(getActivity(), mSongList, this);
        mSongList = CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE);
        mAdapter.update(mSongList);
        mSubHeaderTextView.setText(HEADER_SUB_TITLE);

        setHeaderImage();

        mHandler.postDelayed(initGridView, 0);
        mHandler.postDelayed(animateContent, 0);

        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                if (mOnScrolledListener != null)
                    mOnScrolledListener.onScrolledUp();
            }

            @Override
            public void onShow() {
                if (mOnScrolledListener != null) mOnScrolledListener.onScrolledDown();
            }

        });

        RelativeLayout relativeLayout = (RelativeLayout) mSearchButton.getParent();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        params.topMargin = Common.getStatusBarHeight(getActivity());
        relativeLayout.setLayoutParams(params);

        mSearchButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));
        mBackButton.setOnClickListener(v -> removeFragment());
        return mView;
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

    private void setHeaderImage() {
        if (mSongList.size() > 0) {
            Bitmap artwork = ImageLoader.getInstance().loadImageSync(MusicUtils.getAlbumArtUri(mSongList.get(0)._albumId).toString());
            if (artwork != null) {
                mHeaderImage.setImageBitmap(artwork);
            } else {
                int padding = MusicUtils.getDPFromPixel(120);
                mHeaderImage.setPadding(padding, padding, padding, padding);
            }

        } else {
            int padding = MusicUtils.getDPFromPixel(120);
            mHeaderImage.setPadding(padding, padding, padding, padding);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSongList = CursorHelper.getTracksForSelection(FROM_WHERE, SELECTION_VALUE);
        if (FROM_WHERE.equalsIgnoreCase("PLAYLISTS")) {
            mSubHeaderTextView.setText(MusicUtils.makeLabel(Common.getInstance(), R.plurals.Nsongs, mSongList.size()));
        }
        mAdapter.update(mSongList);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MusicUtils.URI_REQUEST_CODE_DELETE:
                if (resultCode == Activity.RESULT_OK) {
                    mContext.getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(mSongList.get(mSelectedPosition));
                    try {
                        try {
                            MusicUtils.deleteFile(this, song, this);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public void onPopUpMenuClickListener(View v, final int position) {
        mSelectedPosition = position;
        PopupMenu menu = new PopupMenu(getActivity(), v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), sub, 0);
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_song_play_next:
                    new AsyncAddTo(mSongList.get(position)._title, false, mSongList.get(position)).execute();
                    break;
                case R.id.popup_song_addto_queue:
                    new AsyncAddTo(mSongList.get(position)._title, true, mSongList.get(position)).execute();
                    break;
                case R.id.popup_song_add_to_favs:
                    mApp.getDBAccessHelper().addToFavorites(mSongList.get(position));
                    break;
                case R.id.popup_song_delete:
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(mSongList.get(mSelectedPosition));
                    try {
                        MusicUtils.deleteFile(this, song, this);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.popup_song_use_as_phone_ringtone:
                    MusicUtils.setRingtone((AppCompatActivity) getActivity(), mSongList.get(mSelectedPosition)._id);
                    break;
                case R.id.popup_song_share:
                    MusicUtils.shareTheMusic(getActivity(), mSongList.get(mSelectedPosition)._path);
                    break;
                case R.id.popup_edit_songs_tags:
                    Intent intent = new Intent(getActivity(), Id3TagEditorActivity.class);
                    intent.putExtra("SONG_PATH", mSongList.get(mSelectedPosition)._path);
                    intent.putExtra("ALBUM_ID", mSongList.get(mSelectedPosition)._albumId);
                    startActivityForResult(intent, Constants.EDIT_TAGS);
                    break;

                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", new long[]{mSongList.get(mSelectedPosition)._id});
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getChildFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    long[] list = new long[]{mSongList.get(mSelectedPosition)._id};
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(Common.getInstance(), list, playlist);
                    return true;
            }
            return false;
        });
        menu.inflate(R.menu.popup_song);
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
        mSongList.remove(mSelectedPosition);
        mAdapter.update(mSongList);
        if (mSongList.size() == 0) {
            removeFragment();
        }
    }


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
                mHeaderLayout.setVisibility(View.VISIBLE);

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
                getActivity().getSupportFragmentManager().beginTransaction().remove(TracksSubFragment.this).commit();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

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
        slideAwayGridView();
    }

    private void onPopUpClicked(View view) {
        final PopupMenu menu = new PopupMenu(getActivity(), view);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), sub, 0);

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_play_next:
                    if (mSongList.size() != 0) {
                        new AsyncAddTo(HEADER_TITLE, false, mSongList).execute();
                    }
                    break;
                case R.id.menu_shuffle:
                    if (mSongList.size() != 0) {
                        mApp.getPlayBackStarter().shuffleUp(mSongList);
                    }
                    break;
                case R.id.menu_add_to_queue:
                    if (mSongList.size() != 0) {
                        new AsyncAddTo(HEADER_TITLE, true, mSongList).execute();
                    }
                    break;
                case R.id.menu_edit_tags:
                    Toast.makeText(mContext, "Need to be implemented.", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_search:
                    Intent intent = new Intent(Intent.ACTION_SEARCH);
                    intent.putExtra(SearchManager.QUERY, HEADER_TITLE);
                    startActivity(intent);
                    break;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", MusicUtils.getPlayListIds(mSongList));
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getChildFragmentManager(), "FRAGMENT_TAG");
                    break;
                case PLAYLIST_SELECTED:
                    MusicUtils.insertIntoPlayList(mContext, item, mSongList);
                    break;
                case R.id.menu_delete:
                    try {
                        MusicUtils.deleteFile(TracksSubFragment.this, mSongList, this);
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
        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) mRecyclerView.getLayoutManager();

        while (pos > 0) {
            view = linearLayoutManager.findViewByPosition(pos - 1);
            if (view == null) {
                break;
            }
            pos = pos - 1;
        }
        return pos;
    }


}
