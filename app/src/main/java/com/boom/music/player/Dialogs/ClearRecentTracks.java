package com.boom.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.boom.music.player.Common;
import com.boom.music.player.Database.DataBaseHelper;
import com.boom.music.player.R;


/**
 * Created by REYANSH on 7/5/2017.
 */

public class ClearRecentTracks extends DialogFragment {


    private Common mApp;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mApp = (Common) getActivity().getApplicationContext();
        builder.setTitle(R.string.clear_recently_played);
        builder.setMessage(R.string.clear_recently_played_long);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mApp.getDBAccessHelper().getWritableDatabase().delete(DataBaseHelper.RECENTLY_PLAYED_TABLE, null, null);
            Toast.makeText(getActivity(), R.string.recently_played_cleared, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        return builder.create();
    }
}