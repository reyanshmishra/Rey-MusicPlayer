package com.boom.music.player.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boom.music.player.Common;
import com.boom.music.player.Models.Album;
import com.boom.music.player.Models.Artist;
import com.boom.music.player.Models.Genre;
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Activities.TracksSubFragment;
import com.boom.music.player.SubGridViewFragment.TracksSubGridViewFragment;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Reyansh on 26/06/2016.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List mData;
    private Context mContext;
    private Common mApp;
    private SearchActivity mSearchActivity;


    public SearchAdapter(SearchActivity searchActivity) {
        this.mContext = searchActivity.getApplicationContext();
        this.mApp = (Common) searchActivity.getApplicationContext();
        mSearchActivity = searchActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item, parent, false);
                return new SongsHolder(itemView);
            case 1:
                View itemView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
                return new AlbumHolder(itemView2);
            case 2:
                View itemView3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_header, parent, false);
                return new HeaderHolder(itemView3);
            case 3:
                View itemView4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
                return new ArtistsHolder(itemView4);
            case 4:
                View itemView5 = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_view_item, parent, false);
                return new GenreHolder(itemView5);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                SongsHolder songsHolder = (SongsHolder) holder;
                Song song = (Song) mData.get(position);
                songsHolder.title.setText(song._title);
                songsHolder.artist.setText(song._artist);
                ImageLoader.getInstance().displayImage(MusicUtils.getAlbumArtUri(song._albumId).toString(), songsHolder.mAlbumArt);
                songsHolder.duration.setText(MusicUtils.makeShortTimeString(mContext, song._duration / 1000));
                break;
            case 1:
                AlbumHolder albumHolder = (AlbumHolder) holder;
                Album album = (Album) mData.get(position);
                albumHolder.albumName.setText(album._albumName);
                albumHolder.artistName.setText(album._artistName);
                ImageLoader.getInstance().displayImage(MusicUtils.getAlbumArtUri(album._Id).toString(), albumHolder.albumart);

                break;
            case 2:
                HeaderHolder headerHolder = (HeaderHolder) holder;
                headerHolder.mHeader.setText(((String) mData.get(position)));
                break;
            case 3:
                ArtistsHolder artistsHolder = (ArtistsHolder) holder;
                Artist artist = (Artist) mData.get(position);
                artistsHolder.albumName.setText(artist._artistName);
                ImageLoader.getInstance().displayImage(artist._artistAlbumArt, artistsHolder.albumart);

                try {
                    String nooftracks = MusicUtils.makeLabel(mSearchActivity.getApplicationContext(), R.plurals.Nsongs, artist._noOfTracksByArtist);
                    String noofalbums = MusicUtils.makeLabel(mSearchActivity.getApplicationContext(), R.plurals.Nalbums, artist._noOfAlbumsByArtist);
                    artistsHolder.artistName.setText(nooftracks + " | " + noofalbums);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    artistsHolder.artistName.setVisibility(View.INVISIBLE);
                }

                break;
            case 4:
                GenreHolder genreHolder = (GenreHolder) holder;
                Genre genre = (Genre) mData.get(position);
                ImageLoader.getInstance().displayImage(genre._genreAlbumArt, genreHolder.albumart);
                genreHolder.albumName.setText(genre._genreName);
                String noofalbums = MusicUtils.makeLabel(mSearchActivity.getApplicationContext(), R.plurals.Nalbums, genre._noOfAlbumsInGenre);
                genreHolder.artistName.setText(noofalbums);
                break;

        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position) instanceof Song)
            return 0;
        if (mData.get(position) instanceof Album)
            return 1;
        if (mData.get(position) instanceof String)
            return 2;
        if (mData.get(position) instanceof Artist)
            return 3;
        if (mData.get(position) instanceof Genre)
            return 4;
        return 5;
    }

    public void update(List mSongsList) {
        mData = mSongsList;
        notifyDataSetChanged();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    class SongsHolder extends ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView artist;
        private ImageView mAlbumArt;
        private TextView duration;
        private ImageView mOverFlow;

        public SongsHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listViewTitleText);
            artist = (TextView) itemView.findViewById(R.id.listViewSubText);
            title.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));
            artist.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));

            mAlbumArt = (ImageView) itemView.findViewById(R.id.listViewLeftIcon);
            duration = (TextView) itemView.findViewById(R.id.listViewRightSubText);
            mOverFlow = (ImageView) itemView.findViewById(R.id.listViewOverflow);
            mOverFlow.setVisibility(View.VISIBLE);
            mOverFlow.setOnClickListener(v -> mSearchActivity.onSongPopUpMenuClicked(v, getAdapterPosition()));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ArrayList<Song> hashMaps = new ArrayList<>();
            hashMaps.add((Song) mData.get(getAdapterPosition()));
            mApp.getPlayBackStarter().playSongs(hashMaps, 0);
            mSearchActivity.startActivity(new Intent(mContext, NowPlayingActivity.class));
        }
    }


    class HeaderHolder extends ViewHolder {
        public TextView mHeader;

        public HeaderHolder(View itemView) {
            super(itemView);
            mHeader = (TextView) itemView.findViewById(R.id.section_header);
            mHeader.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOLD));
        }
    }

    class ArtistsHolder extends ViewHolder implements View.OnClickListener {
        private TextView albumName;
        private TextView artistName;
        private ImageView albumart;
        private ImageView mOverFlow;

        public ArtistsHolder(View itemView) {
            super(itemView);
            albumName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            artistName = (TextView) itemView.findViewById(R.id.gridViewSubText);
            albumName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));
            artistName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));

            albumart = (ImageView) itemView.findViewById(R.id.gridViewImage);
            mOverFlow = (ImageView) itemView.findViewById(R.id.overflow);


            mOverFlow.setOnClickListener(v -> mSearchActivity.onArtistPopUpMenuClickListener(v, getAdapterPosition()));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            Artist artist = (Artist) mData.get(getAdapterPosition());
            bundle.putString(Constants.HEADER_TITLE, artist._artistName);
            bundle.putInt(Constants.HEADER_SUB_TITLE, artist._noOfAlbumsByArtist);
            bundle.putString(Constants.FROM_WHERE, "ARTIST");
            bundle.putString(Constants.COVER_PATH, artist._artistAlbumArt);
            bundle.putLong(Constants.SELECTION_VALUE, artist._artistId);

            TracksSubGridViewFragment tracksSubGridViewFragment = new TracksSubGridViewFragment();
            tracksSubGridViewFragment.setArguments(bundle);
            mSearchActivity.addFragment(tracksSubGridViewFragment);
        }
    }

    class AlbumHolder extends ViewHolder implements View.OnClickListener {

        private TextView albumName;
        private TextView artistName;
        private ImageView albumart;
        private ImageView mOverFlow;

        public AlbumHolder(View itemView) {
            super(itemView);
            albumName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            artistName = (TextView) itemView.findViewById(R.id.gridViewSubText);
            albumName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));
            artistName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));

            albumart = (ImageView) itemView.findViewById(R.id.gridViewImage);
            mOverFlow = (ImageView) itemView.findViewById(R.id.overflow);


            mOverFlow.setOnClickListener(v -> mSearchActivity.onAlbumPopUpMenuClickListener(v, getAdapterPosition()));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            Album album = (Album) mData.get(getAdapterPosition());

            bundle.putString(Constants.HEADER_TITLE, album._albumName);
            bundle.putString(Constants.HEADER_SUB_TITLE, album._artistName);
            bundle.putString(Constants.FROM_WHERE, "ALBUMS");
            bundle.putLong(Constants.SELECTION_VALUE, album._Id);

            TracksSubFragment tracksSubGridViewFragment = new TracksSubFragment();
            tracksSubGridViewFragment.setArguments(bundle);
            mSearchActivity.addFragment(tracksSubGridViewFragment);
        }
    }

    class GenreHolder extends ViewHolder implements View.OnClickListener {

        private TextView albumName;
        private TextView artistName;
        private ImageView albumart;
        private ImageView mOverFlow;

        public GenreHolder(View itemView) {
            super(itemView);
            albumName = (TextView) itemView.findViewById(R.id.gridViewTitleText);
            artistName = (TextView) itemView.findViewById(R.id.gridViewSubText);
            albumName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));
            artistName.setTypeface(TypefaceHelper.getTypeface(mSearchActivity.getApplicationContext(), TypefaceHelper.FUTURA_BOOK));

            albumart = (ImageView) itemView.findViewById(R.id.gridViewImage);
            mOverFlow = (ImageView) itemView.findViewById(R.id.overflow);


            mOverFlow.setOnClickListener(v -> mSearchActivity.onGenrePopUpMenuClickListener(v, getAdapterPosition()));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            Genre genre = (Genre) mData.get(getAdapterPosition());
            bundle.putString(Constants.HEADER_TITLE, genre._genreName);
            bundle.putInt(Constants.HEADER_SUB_TITLE, genre._noOfAlbumsInGenre);
            bundle.putString(Constants.FROM_WHERE, "GENRES");
            bundle.putLong(Constants.SELECTION_VALUE, genre._genreId);
            bundle.putString(Constants.COVER_PATH, genre._genreAlbumArt);

            TracksSubGridViewFragment tracksSubGridViewFragment = new TracksSubGridViewFragment();
            tracksSubGridViewFragment.setArguments(bundle);
            mSearchActivity.addFragment(tracksSubGridViewFragment);
        }
    }
}
