package com.reyansh.audio.audioplayer.free.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.PreferencesHelper;
import com.github.channguyen.rsv.RangeSliderView;

/**
 * Created by Reyansh on 23/04/2016.
 */
public class RecentlyAddedDialog extends DialogFragment {

    private RangeSliderView mSmallSlider;
    private int mNoOfWeeks;
    private Common mApp;
    private AlertDialog mAlertDialog;
    final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
        @Override
        public void onSlide(int index) {
            mNoOfWeeks = index + 1;
            if (mNoOfWeeks == 1)
                mAlertDialog.setMessage(mNoOfWeeks + " " + "Week");
            else
                mAlertDialog.setMessage(mNoOfWeeks + " " + "Weeks");
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_recentlyadded, null);
        mSmallSlider = (RangeSliderView) view.findViewById(R.id.rsv_small);
        mApp = (Common) getActivity().getApplicationContext();
        builder.setTitle(R.string.recently_added_desc);

        if (PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1) != 0) {
            mSmallSlider.setInitialIndex(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1) - 1);
        } else {
            mSmallSlider.setInitialIndex(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1));
        }

        if (mNoOfWeeks == 1) {
            builder.setMessage(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1) + " " + "Week");
        } else {
            builder.setMessage(PreferencesHelper.getInstance().getInt(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, 1) + " " + "Weeks");

        }


        mSmallSlider.setOnSlideListener(listener);


        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            PreferencesHelper.getInstance().put(PreferencesHelper.Key.RECENTLY_ADDED_WEEKS, mNoOfWeeks);
            dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dismiss());
        builder.setView(view);
        mAlertDialog = builder.create();
        return mAlertDialog;
    }
}
