package com.boom.music.player.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.boom.music.player.Common;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.Constants;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Utils.TypefaceHelper;

import org.florescu.android.rangeseekbar.RangeSeekBar;

/**
 * Created by Reyansh on 23/07/2016.
 */
public class ABRepeatDialog extends DialogFragment {


    private Common mApp;

    private int repeatPointA;
    private int repeatPointB;

    private int currentSongDurationMillis;
    private int currentSongDurationSecs;

    private TextView repeatSongATime;
    private TextView repeatSongBTime;

    private RangeSeekBar mRangeSeekBar;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mApp = (Common) getActivity().getApplicationContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.a_b_repeat);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ab_repeat, null);
        mRangeSeekBar = (RangeSeekBar) view.findViewById(R.id.range_seek_bar);

        repeatSongATime = (TextView) view.findViewById(R.id.repeat_song_range_A_time);
        repeatSongBTime = (TextView) view.findViewById(R.id.repeat_song_range_B_time);

        repeatSongATime.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));
        repeatSongBTime.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));
        TextView textView = (TextView) view.findViewById(R.id.repeat_song_range_instructions);
        textView.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));

        currentSongDurationMillis = mApp.getService().getMediaPlayer().getDuration();
        currentSongDurationSecs = currentSongDurationMillis / 1000;


        initDialog();

        builder.setView(view);

        builder.setPositiveButton(R.string.repeat, (arg0, arg1) -> {
            PreferencesHelper.getInstance().put(PreferencesHelper.Key.REPEAT_MODE, Constants.A_B_REPEAT);
            mApp.getService().setRepeatSongRange(repeatPointA, repeatPointB);
            ((NowPlayingActivity) getActivity()).applyRepeatButton();
            dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }


    private void initDialog() {

        currentSongDurationMillis = mApp.getService().getMediaPlayer().getDuration();
        currentSongDurationSecs = currentSongDurationMillis / 1000;


        if (PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.REPEAT_MODE, Constants.REPEAT_OFF) == Constants.A_B_REPEAT) {

            repeatSongATime.setText(MusicUtils.convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointA()));
            repeatSongBTime.setText(MusicUtils.convertMillisToMinsSecs(mApp.getService().getRepeatSongRangePointB()));


            repeatPointA = mApp.getService().getRepeatSongRangePointA();
            repeatPointB = mApp.getService().getRepeatSongRangePointB();

            mRangeSeekBar.setRangeValues(0, currentSongDurationMillis);

            mRangeSeekBar.setSelectedMinValue(repeatPointA);
            mRangeSeekBar.setSelectedMaxValue(repeatPointB);

        } else {
            repeatSongATime.setText("0:00");
            repeatSongBTime.setText(MusicUtils.convertMillisToMinsSecs(currentSongDurationMillis));

            repeatPointA = 0;
            repeatPointB = currentSongDurationMillis;

            mRangeSeekBar.setRangeValues(0, currentSongDurationMillis);

            mRangeSeekBar.setSelectedMinValue(repeatPointA);
            mRangeSeekBar.setSelectedMaxValue(repeatPointB);

        }


        mRangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
            repeatPointA = (int) minValue;
            repeatPointB = (int) maxValue;

            repeatSongATime.setText(MusicUtils.convertMillisToMinsSecs(repeatPointA));
            repeatSongBTime.setText(MusicUtils.convertMillisToMinsSecs(repeatPointB));

        });

        repeatSongATime.setText(MusicUtils.convertMillisToMinsSecs(repeatPointA));
        repeatSongBTime.setText(MusicUtils.convertMillisToMinsSecs(repeatPointB));


    }
}
