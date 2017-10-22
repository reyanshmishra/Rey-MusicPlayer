package com.boom.music.player.TagEditor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PermissionToEditSdCardDialog;
import com.boom.music.player.Dialogs.SongInfoBottomSheetDialog;
import com.boom.music.player.Lastfmapi.ApiClient;
import com.boom.music.player.Lastfmapi.CachingControlInterceptor;
import com.boom.music.player.Lastfmapi.LastFmInterface;
import com.boom.music.player.Lastfmapi.Models.BestMatchesModel;
import com.boom.music.player.R;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.FileUtils;
import com.boom.music.player.Utils.KeyboardUtil;
import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Id3TagEditorActivity extends AppCompatActivity {

    private String TAG = "AAAAAAA";
    private SongInfoBottomSheetDialog songInfoBottomSheetDialog;
    private ScrollView mScrollView;
    private RelativeLayout mHeaderLayout;

    private ImageView mAlbumArtImage;

    private EditText mTitleEditText;
    private EditText mArtistEditText;
    private EditText mAlbumEditText;
    private EditText mAlbumArtistEditText;
    private EditText mGenreEditText;
    private EditText mProducerEditText;
    private EditText mCommentsEditText;
    private EditText mTrackEditText;
    private EditText mYearEditText;
    private EditText mTotalTrackEditText;


    private TextView mTextOf;


    private String title;
    private String artist;
    private String album;
    private String albumArtist;
    private String genre;
    private String producer;
    private String year;
    private String track;
    private String totalTrack;
    private String comment;

    private boolean titleEdited = false;
    private boolean artistEdited = false;
    private boolean albumEdited = false;
    private boolean albumArtistEdited = false;
    private boolean genreEdited = false;
    private boolean producerEdited = false;
    private boolean yearEdited = false;
    private boolean trackEdited = false;
    private boolean commentEdited = false;
    private boolean artWorkEdited = false;

    private String songTitle;
    private String songArtist;
    private String songAlbum;
    private String songAlbumArtist;
    private String songGenre;
    private String songProducer;
    private String songTrackNumber;
    private String songTrackTotals;
    private String songComments;
    private String songYear;
    private String artWorkUrl;   // "http://www.flat-e.com/flate5/wp-content/uploads/cover-960x857.jpg";
    private Artwork artwork;

    private AppBarLayout mAppBarLayout;

    private Button mUpdateButton;
    private Toolbar mToolbar;

    private String SONG_PATH;
    private String ALBUM_ID;

    private CompositeDisposable mCompositeDisposable;
    private FloatingActionButton mFetchDetailsFAB;
    private ProgressDialog mProgressUpdateDialog;
    private CardView mCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id3_tag_editor);

        SONG_PATH = getIntent().getExtras().getString("SONG_PATH");
        ALBUM_ID = getIntent().getExtras().getString("ALBUM_ID");

        if (SONG_PATH == null || SONG_PATH.equalsIgnoreCase("")) finish();

        mCompositeDisposable = new CompositeDisposable();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mFetchDetailsFAB = (FloatingActionButton) findViewById(R.id.save_fab);


        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.id_toolbar_container);

        KeyboardUtil keyboardUtil = new KeyboardUtil(this, findViewById(R.id.main_layout));
        keyboardUtil.enable();

        RelativeLayout.LayoutParams statusparams = (RelativeLayout.LayoutParams) mAppBarLayout.getLayoutParams();
        statusparams.topMargin = Common.getStatusBarHeight(this);
        mAppBarLayout.setLayoutParams(statusparams);

        mScrollView = (ScrollView) findViewById(R.id.scrollView1);
        mHeaderLayout = (RelativeLayout) findViewById(R.id.header_layout);


        mCardView = (CardView) findViewById(R.id.card_view);
        mAlbumArtImage = (ImageView) findViewById(R.id.album_art_image);
        MyGestureListener myGestureListener = new MyGestureListener(mScrollView, mAlbumArtImage);
        GestureDetectorCompat mDetector = new GestureDetectorCompat(this, myGestureListener);
        mScrollView.setOnTouchListener((v, event) -> {

            MotionEvent e = MotionEvent.obtain(event);
            mHeaderLayout.dispatchTouchEvent(e);
            e.recycle();

            int action = MotionEventCompat.getActionMasked(event);
            if (action == MotionEvent.ACTION_UP) {
                myGestureListener.upDetected();
            }
            return mDetector.onTouchEvent(event);
        });

        mTitleEditText = (EditText) findViewById(R.id.edit_title_field);
        mTitleEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mArtistEditText = (EditText) findViewById(R.id.edit_artist_field);
        mArtistEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));


        mAlbumEditText = (EditText) findViewById(R.id.edit_album_field);
        mAlbumEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mAlbumArtistEditText = (EditText) findViewById(R.id.edit_album_artist_field);
        mAlbumArtistEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mGenreEditText = (EditText) findViewById(R.id.edit_genre_field);
        mGenreEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mProducerEditText = (EditText) findViewById(R.id.edit_producer_field);
        mProducerEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mYearEditText = (EditText) findViewById(R.id.edit_year_field);
        mYearEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mTrackEditText = (EditText) findViewById(R.id.edit_track_field);
        mTrackEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));


        mCommentsEditText = (EditText) findViewById(R.id.edit_comment_field);
        mCommentsEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mTextOf = (TextView) findViewById(R.id.of);
        mTextOf.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mTotalTrackEditText = (EditText) findViewById(R.id.edit_track_total_field);
        mTotalTrackEditText.setTypeface(TypefaceHelper.getTypeface(getApplicationContext(), "Futura-Book-Font"));

        mUpdateButton = (Button) findViewById(R.id.fetch_best_match);
        mUpdateButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(Id3TagEditorActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.fetching_best_matched_result_for_this_track));
            progressDialog.show();
            fetchBestMatch(progressDialog);
        });

        mFetchDetailsFAB.setOnClickListener(v -> {

            if (!title.equalsIgnoreCase(mTitleEditText.getText().toString().trim())) {
                songTitle = mTitleEditText.getText().toString().replace("'", "''");
            } else {
                songTitle = null;
            }

            if (!artist.equalsIgnoreCase(mArtistEditText.getText().toString().trim())) {
                songArtist = mArtistEditText.getText().toString();
                songArtist = songArtist.replace("'", "''");
            } else {
                songArtist = null;
            }

            if (!album.equalsIgnoreCase(mAlbumEditText.getText().toString().trim())) {
                songAlbum = mAlbumEditText.getText().toString();
                songAlbum = songAlbum.replace("'", "''");
            } else {
                songAlbum = null;
            }

            if (!albumArtist.equalsIgnoreCase(mAlbumArtistEditText.getText().toString().trim())) {
                songAlbumArtist = mAlbumArtistEditText.getText().toString();
                songAlbumArtist = songAlbumArtist.replace("'", "''");
            } else {
                songAlbumArtist = null;
            }

            if (!genre.equalsIgnoreCase(mGenreEditText.getText().toString().trim())) {
                songGenre = mGenreEditText.getText().toString();
                songGenre = songGenre.replace("'", "''");
            } else {
                songGenre = null;
            }

            if (!producer.equalsIgnoreCase(mProducerEditText.getText().toString().trim())) {
                songProducer = mProducerEditText.getText().toString();
                songProducer = songProducer.replace("'", "''");
            } else {
                songProducer = null;
            }

            if (!track.equalsIgnoreCase(mTrackEditText.getText().toString().trim())) {
                songTrackNumber = mTrackEditText.getText().toString();
                songTrackNumber = songTrackNumber.replace("'", "''");
                songTrackTotals = mTotalTrackEditText.getText().toString();
                songTrackTotals = songTrackTotals.replace("'", "''");
            } else {
                songTrackNumber = null;
                songTrackTotals = null;
            }

            if (!comment.equalsIgnoreCase(mCommentsEditText.getText().toString().trim())) {
                songComments = mCommentsEditText.getText().toString();
                songComments = songComments.replace("'", "''");
            } else {
                songComments = null;
            }

            if (year.equalsIgnoreCase(mYearEditText.getText().toString().trim())) {
                songYear = mYearEditText.getText().toString();
                songYear = songYear.replace("'", "''");
            } else {
                songYear = null;
            }

            updateFile();
        });


        mScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = mScrollView.getScrollY();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHeaderLayout.getLayoutParams();
            params.topMargin = (-scrollY / 3);
            mHeaderLayout.setLayoutParams(params);
        });


        fetchDetails();


        if (MusicUtils.isFromSdCard(SONG_PATH) && !MusicUtils.hasPermission()) {
            PermissionToEditSdCardDialog takePermissionDialog = new PermissionToEditSdCardDialog(this);
            takePermissionDialog.show(getSupportFragmentManager(), "PERMISSION_DIALOG");
        }
    }


    private void fetchDetails() {
        File file = new File(SONG_PATH);

        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateAndSetDefault();

            title = tag.getFirst(FieldKey.TITLE);

            mTitleEditText.setText(title);
            mTitleEditText.setSelection(title.length());

            artist = tag.getFirst(FieldKey.ARTIST);
            mArtistEditText.setText(artist);
            mArtistEditText.setSelection(artist.length());

            album = tag.getFirst(FieldKey.ALBUM);
            mAlbumEditText.setText(album);
            mAlbumEditText.setSelection(album.length());


            albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST);
            mAlbumArtistEditText.setText(albumArtist);
            mAlbumArtistEditText.setSelection(albumArtist.length());


            genre = tag.getFirst(FieldKey.GENRE);
            mGenreEditText.setText(genre);
            mGenreEditText.setSelection(genre.length());

            producer = tag.getFirst(FieldKey.PRODUCER);
            mProducerEditText.setText(producer);
            mProducerEditText.setSelection(mProducerEditText.length());

            year = tag.getFirst(FieldKey.YEAR);
            mYearEditText.setText(year);
            mYearEditText.setSelection(year.length());

            track = tag.getFirst(FieldKey.TRACK);
            mTrackEditText.setText(track);
            mTrackEditText.setSelection(track.length());


            totalTrack = tag.getFirst(FieldKey.TRACK_TOTAL);
            mTotalTrackEditText.setText(totalTrack);
            mTotalTrackEditText.setSelection(totalTrack.length());

            comment = tag.getFirst(FieldKey.COMMENT);
            mCommentsEditText.setText(comment);
            mCommentsEditText.setSelection(comment.length());


            List<Artwork> artwork = tag.getArtworkList();

            if (artwork.size() > 0) {
                byte[] image = artwork.get(0).getBinaryData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                mCardView.setCardBackgroundColor(Palette.from(bitmap).generate().getC(R.color.deep_purple));
                mAlbumArtImage.setImageBitmap(bitmap);
            }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.track_is_malformed, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search_web) {
            songInfoBottomSheetDialog = new SongInfoBottomSheetDialog();
            Bundle bundle = new Bundle();
            bundle.putString("SONG_NAME", mTitleEditText.getText().toString());
            songInfoBottomSheetDialog.setArguments(bundle);
            songInfoBottomSheetDialog.show(getSupportFragmentManager(), "BOTTOM_SHEET");
        } else if (item.getItemId() == R.id.action_from_gallery) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_FROM_GALLERY);
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MusicUtils.URI_REQUEST_CODE_DELETE) {
            if (resultCode == RESULT_OK) {
                getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                finish();
            }
        } else if (requestCode == Constants.PICK_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                artWorkUrl = data.getData().toString();
                ImageLoader.getInstance().displayImage(artWorkUrl, mAlbumArtImage);
                albumArtistEdited = true;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateFile() {
        mProgressUpdateDialog = new ProgressDialog(this);
        mProgressUpdateDialog.setMessage(getResources().getString(R.string.updating_tags));
        mProgressUpdateDialog.setCancelable(false);
        mProgressUpdateDialog.show();

        mCompositeDisposable.add(Observable.fromCallable(() -> embedDataFile())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "ERROR " + e.getMessage());
                        mProgressUpdateDialog.dismiss();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }


    private boolean embedDataFile() throws IOException, ReadOnlyFileException, CannotWriteException, TagException, InvalidAudioFrameException, CannotReadException {
        File tempFile;
        File originalFile = new File(SONG_PATH);

        if (MusicUtils.isFromSdCard(SONG_PATH)) {

            /**
             *Create a temp file the internal storage where there is no boundation of editing any file.
             */

            tempFile = new File(Common.getInstance().getExternalCacheDir().getPath(), originalFile.getName());

            /**
             *Copy the sdcard file to internal storage where you can edit it freely.
             */
            FileUtils.copyFile(originalFile, tempFile);
            /**
             *Set tags or edit the temp file.
             */
            setTags(tempFile);
            /**
             *Copy it back to its original position i.e. in sdcard.
             */
            FileUtils.cutFile(tempFile, originalFile);

        } else {
            File orgFile = new File(SONG_PATH);
            setTags(orgFile);
        }
        MediaScannerConnection.scanFile(Common.getInstance(), new String[]{originalFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Logger.log("SUCCESSFULL TAGGED");
                mProgressUpdateDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });

        return false;
    }


    public void updateFetchedDetails(String artUrl,
                                     String trackName,
                                     String albumName,
                                     String artistName,
                                     String primaryGenreName,
                                     String releaseDate,
                                     int trackNumber,
                                     int trackCount) {
        artWorkUrl = artUrl;
        ImageLoader.getInstance().displayImage(artUrl, mAlbumArtImage);
        albumArtistEdited = true;
        mTitleEditText.setText(trackName);
        mAlbumEditText.setText(albumName);
        mArtistEditText.setText(artistName);
        mGenreEditText.setText(primaryGenreName);
        mYearEditText.setText(releaseDate.substring(0, 10));
        mTrackEditText.setText("" + trackNumber);
        mTotalTrackEditText.setText("" + trackCount);

    }


    /*private boolean embedDataFile() throws IOException, ReadOnlyFileException, CannotWriteException, TagException, InvalidAudioFrameException, CannotReadException {
        File tempFile;
        File originalFile = new File(SONG_PATH);

        if (MusicUtils.isFromSdCard(SONG_PATH)) {
            tempFile = new File(Common.getInstance().getFilesDir(), originalFile.getName());
            FileUtils.copyFile(originalFile, tempFile);

            setTags(tempFile);
            DocumentFile documentFile = FileUtils.getDocumentFile(originalFile);
            if (documentFile != null) {
                ParcelFileDescriptor pfd = Common.getInstance().getContentResolver().openFileDescriptor(documentFile.getUri(), "w");
                if (pfd != null) {
                    FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
                    FileUtils.copyFile(tempFile, fileOutputStream);
                    pfd.close();
                }
                tempFile.delete();
            }
        } else {
            File orgFile = new File(SONG_PATH);
            setTags(orgFile);
        }
        MediaScannerConnection.scanFile(Common.getInstance(), new String[]{originalFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.d(TAG, "SUCCESSFULL");
                mProgressUpdateDialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });

        return false;
    }*/

    public void setTags(File tempFile) throws IOException, TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, CannotWriteException {
        AudioFile audioFile = AudioFileIO.read(tempFile);
        Tag tag = audioFile.getTagOrCreateAndSetDefault();

        if (tag != null) {

            if (songTitle != null) {
                if (tag.getFirstField(FieldKey.TITLE) != null)
                    tag.setField(FieldKey.TITLE, songTitle);
                else {
                    tag.addField(FieldKey.TITLE, songTitle);
                }
            }

            if (songArtist != null) {
                if (tag.getFirstField(FieldKey.ARTIST) != null)
                    tag.setField(FieldKey.ARTIST, songArtist);
                else {
                    tag.addField(FieldKey.ARTIST, songArtist);
                }
            }

            if (songAlbum != null) {
                if (tag.getFirstField(FieldKey.ALBUM) != null)
                    tag.setField(FieldKey.ALBUM, songAlbum);
                else {
                    tag.addField(FieldKey.ALBUM, songAlbum);
                }
            }

            if (songAlbumArtist != null) {
                if (tag.getFirstField(FieldKey.ALBUM_ARTIST) != null)
                    tag.setField(FieldKey.ALBUM_ARTIST, songAlbumArtist);
                else {
                    tag.addField(FieldKey.ALBUM_ARTIST, songAlbumArtist);
                }
            }

            if (songGenre != null) {
                if (tag.getFirstField(FieldKey.GENRE) != null)
                    tag.setField(FieldKey.GENRE, songGenre);
                else {
                    tag.addField(FieldKey.GENRE, songGenre);
                }
            }

            if (songProducer != null) {
                if (tag.getFirstField(FieldKey.PRODUCER) != null)
                    tag.setField(FieldKey.PRODUCER, songProducer);
                else {
                    tag.addField(FieldKey.PRODUCER, songProducer);
                }
            }

            if (songYear != null) {
                if (tag.getFirstField(FieldKey.YEAR) != null)
                    tag.setField(FieldKey.YEAR, songYear);
                else {
                    tag.addField(FieldKey.YEAR, songYear);
                }

            }

            if (songTrackNumber != null) {
                if (tag.getFirstField(FieldKey.TRACK) != null)
                    tag.setField(FieldKey.TRACK, songTrackNumber);
                else {
                    tag.addField(FieldKey.TRACK, songTrackNumber);
                }
            }

            if (songTrackTotals != null) {
                if (tag.getFirstField(FieldKey.TRACK_TOTAL) != null)
                    tag.setField(FieldKey.TRACK_TOTAL, songTrackTotals);
                else {
                    tag.addField(FieldKey.TRACK_TOTAL, songTrackTotals);
                }
            }

            if (songComments != null) {
                if (tag.getFirstField(FieldKey.COMMENT) != null)
                    tag.setField(FieldKey.COMMENT, songComments);
                else {
                    tag.addField(FieldKey.COMMENT, songComments);
                }
            }

            if (artWorkUrl != null) {
                Bitmap artworkBitmap = ImageLoader.getInstance().loadImageSync(artWorkUrl);

                artworkBitmap = Bitmap.createScaledBitmap(artworkBitmap, 500, 500, false);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                artworkBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byte[] byteArray = stream.toByteArray();

                File artworkFile = new File(Environment.getExternalStorageDirectory() + "/artwork.jpg");

                if (!artworkFile.exists())
                    artworkFile.createNewFile();

                FileOutputStream out = new FileOutputStream(artworkFile);
                artworkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                artwork = Artwork.createArtworkFromFile(artworkFile);

                artwork.setBinaryData(byteArray);

                if (tag.getFirstArtwork() != null) {
                    tag.deleteArtworkField();
                    tag.setField(artwork);
                } else {
                    tag.addField(artwork);
                }

                try {
                    Uri uri = MusicUtils.getAlbumArtUri(Long.parseLong(ALBUM_ID));
                    DiskCacheUtils.removeFromCache(uri.toString(), ImageLoader.getInstance().getDiskCache());
                    String path = FileUtils.getRealPathFromURI(uri);
                    new File(path).delete();
                    artworkFile.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log(" " + e.getCause());
                }
            }

            audioFile.setTag(tag);
            audioFile.commit();
        }
    }

    private void fetchBestMatch(ProgressDialog progressDialog) {
        if (!CachingControlInterceptor.isOnline()) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.getClient().create(LastFmInterface.class)
                .getITunesSong(ApiClient.ITUNES_API_URL, mTitleEditText.getText().toString(), "song").enqueue(new Callback<BestMatchesModel>() {
            @Override
            public void onResponse(Call<BestMatchesModel> call, Response<BestMatchesModel> response) {
                if (response.isSuccessful()) {
                    List<BestMatchesModel.Results> results = response.body().results;
                    if (results != null && results.size() > 0) {
                        updateFetchedDetails(
                                results.get(0).artworkUrl100.replace("100x100", "500x500"),
                                results.get(0).trackName,
                                results.get(0).collectionName,
                                results.get(0).artistName,
                                results.get(0).primaryGenreName,
                                results.get(0).releaseDate,
                                results.get(0).trackNumber,
                                results.get(0).trackCount);
                    } else {
                        Toast.makeText(Id3TagEditorActivity.this, R.string.no_results_found, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Id3TagEditorActivity.this, R.string.failed_to_get_result, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<BestMatchesModel> call, Throwable t) {
                Logger.log("" + t.getCause());
                progressDialog.dismiss();
            }
        });

    }

    public void updateAlbumArt(String url) {
        artWorkUrl = url;
        ImageLoader.getInstance().displayImage(url, mAlbumArtImage);
        albumArtistEdited = true;
        if (songInfoBottomSheetDialog != null && songInfoBottomSheetDialog.isAdded()) {
            songInfoBottomSheetDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        validate();
    }

    private void validate() {
        if (albumArtistEdited
                || !title.equalsIgnoreCase(mTitleEditText.getText().toString())
                || !album.equalsIgnoreCase(mAlbumEditText.getText().toString())
                || !albumArtist.equalsIgnoreCase(mAlbumArtistEditText.getText().toString())
                || !artist.equalsIgnoreCase(mArtistEditText.getText().toString())
                || !genre.equalsIgnoreCase(mGenreEditText.getText().toString())
                || !producer.equalsIgnoreCase(mProducerEditText.getText().toString())
                || !year.equalsIgnoreCase(mYearEditText.getText().toString())
                || !track.equalsIgnoreCase(mTrackEditText.getText().toString())
                || !comment.equalsIgnoreCase(mCommentsEditText.getText().toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.discard_changes);
            builder.setNegativeButton(R.string.discard, (dialog, which) -> {
                dialog.dismiss();
                finish();
            });
            builder.setPositiveButton(R.string.keep_editing, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else {
            finish();
        }
    }


}

