package com.reyansh.audio.audioplayer.free.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.Database.DataBaseHelper;
import com.reyansh.audio.audioplayer.free.R;

/**
 * Created by REYANSH on 7/5/2017.
 */

public class UnFavoriteDialog extends DialogFragment {


    private Common mApp;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mApp = (Common) getActivity().getApplicationContext();
        builder.setTitle(R.string.unfavorite);
        builder.setMessage(R.string.unfavorite_long);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            mApp.getDBAccessHelper().getWritableDatabase().delete(DataBaseHelper.FAVORITES_TABLE, null, null);
            Toast.makeText(getActivity(), R.string.all_favorites_song_removed_from_playlist, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        return builder.create();
    }
}
