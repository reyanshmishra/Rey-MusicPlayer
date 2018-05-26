package com.reyansh.audio.audioplayer.free.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Adapters.BestMatchesAdapter;
import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Lastfmapi.ApiClient;
import com.reyansh.audio.audioplayer.free.Lastfmapi.CachingControlInterceptor;
import com.reyansh.audio.audioplayer.free.Lastfmapi.LastFmInterface;
import com.reyansh.audio.audioplayer.free.Lastfmapi.Models.BestMatchesModel;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.TagEditor.Id3TagEditorActivity;
import com.reyansh.audio.audioplayer.free.Utils.Constants;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by REYANSH on 7/29/2017.
 * <p>
 * This class gets the album art cover and the details of the song and show them in RecyclerView.
 */

public class SongInfoBottomSheetDialog extends BottomSheetDialogFragment {

    private EditText mBestMatchesEdiText;
    private TextView mNoMatchesFoundTextView;

    private RecyclerView mBestMatchesRecyclerView;
    private View mView;
    private BestMatchesAdapter mBestMatchesAdapter;
    private List<BestMatchesModel.Results> results;
    private String SONG_NAME;
    private ProgressBar mProgressBar;
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            fetchBestMatches(mBestMatchesEdiText.getText().toString().trim());
        }
    };
    private ImageView mCrossImageViewButton;
    private ImageView mBackImageViewButton;
    private Handler mHandler;

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().length() == 0) {
                mCrossImageViewButton.setVisibility(View.INVISIBLE);
            } else {
                mCrossImageViewButton.setVisibility(View.VISIBLE);
            }
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 600);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        mView = getActivity().getLayoutInflater().inflate(R.layout.layout_bottomsheet_info, null, false);

        mBestMatchesEdiText = mView.findViewById(R.id.edit_text_search);
        mNoMatchesFoundTextView = mView.findViewById(R.id.text_view_no_matches_found);
        mNoMatchesFoundTextView.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), TypefaceHelper.FUTURA_BOLD));
        mHandler = new Handler();

        mCrossImageViewButton = (ImageButton) mView.findViewById(R.id.image_button_cross);
        mBackImageViewButton = (ImageButton) mView.findViewById(R.id.image_back_button);

        mCrossImageViewButton.setOnClickListener(v -> mBestMatchesEdiText.setText(""));
        mBackImageViewButton.setOnClickListener(v -> dismiss());
        getDialog().setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            View bottomSheetInternal = d.findViewById(android.support.design.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheetInternal).setState(BottomSheetBehavior.STATE_EXPANDED);
        });


        SONG_NAME = getArguments().getString("SONG_NAME", "");
        if (SONG_NAME.length() > 0) {
            mCrossImageViewButton.setVisibility(View.VISIBLE);
        }

        mBestMatchesEdiText.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), TypefaceHelper.FUTURA_BOLD));
        mBestMatchesEdiText.setText(SONG_NAME);
        mBestMatchesEdiText.setSelection(SONG_NAME.length());
        mBestMatchesEdiText.addTextChangedListener(mTextWatcher);

        mBestMatchesRecyclerView = mView.findViewById(R.id.best_matches_recycler_view);
        mProgressBar = mView.findViewById(R.id.progress_bar);

        dialog.setContentView(mView);

        mBestMatchesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), Common.getNumberOfColms() + 1));
        mBestMatchesAdapter = new BestMatchesAdapter((Id3TagEditorActivity) getActivity(), null);
        mBestMatchesRecyclerView.setAdapter(mBestMatchesAdapter);
        ((View) mView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        fetchBestMatches(SONG_NAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void fetchBestMatches(String songName) {
        mProgressBar.setVisibility(View.VISIBLE);
        mBestMatchesRecyclerView.setVisibility(View.INVISIBLE);
        mNoMatchesFoundTextView.setVisibility(View.INVISIBLE);

        if (!CachingControlInterceptor.isOnline()) {
            Toast.makeText(mProgressBar.getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getClient().create(LastFmInterface.class)
                .getITunesSong(ApiClient.ITUNES_API_URL, songName, Constants.ENTITY_SONG).enqueue(new Callback<BestMatchesModel>() {
            @Override
            public void onResponse(Call<BestMatchesModel> call, Response<BestMatchesModel> response) {
                if (response.isSuccessful()) {
                    results = response.body().results;
                    mBestMatchesAdapter.updateData(results);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    if (results != null && results.size() == 0) {
                        mNoMatchesFoundTextView.setVisibility(View.VISIBLE);
                        mBestMatchesRecyclerView.setVisibility(View.INVISIBLE);
                    } else {
                        mNoMatchesFoundTextView.setVisibility(View.INVISIBLE);
                        mBestMatchesRecyclerView.setVisibility(View.VISIBLE);
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<BestMatchesModel> call, Throwable t) {
                Log.d("FIALED", "SSSS");
            }
        });
    }
}
