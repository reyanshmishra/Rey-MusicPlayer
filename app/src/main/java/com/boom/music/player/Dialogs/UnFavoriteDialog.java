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
