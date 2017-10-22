package com.boom.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.boom.music.player.R;

/**
 * Created by REYANSH on 3/23/2017.
 */

public class PopupWritePermission extends DialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.set_ringtone);
        builder.setMessage(R.string.write_settings_desc);

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
            getActivity().startActivity(intent);
            dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dismiss());

        return builder.create();
    }
}
