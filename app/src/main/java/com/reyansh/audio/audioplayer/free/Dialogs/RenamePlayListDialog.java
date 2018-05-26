package com.reyansh.audio.audioplayer.free.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.reyansh.audio.audioplayer.free.Common;
import com.reyansh.audio.audioplayer.free.R;
import com.reyansh.audio.audioplayer.free.Utils.MusicUtils;
import com.reyansh.audio.audioplayer.free.Utils.TypefaceHelper;

/**
 * Created by REYANSH on 7/11/2017.
 */

public class RenamePlayListDialog extends DialogFragment {

    private Common mApp;
    private EditText mEditText;
    private String mPlayListName;
    private Long mPlayListId;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mApp = (Common) getActivity().getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.create_playlist, null);
        mEditText = (EditText) view.findViewById(R.id.edit_text_playlist);
        mEditText.requestFocus();
        mEditText.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), TypefaceHelper.FUTURA_BOOK));
        mPlayListId = getArguments().getLong("PLAYLIST_ID");
        mPlayListName = nameForId(mPlayListId);
        mEditText.setText(mPlayListName);
        mEditText.setSelection(mPlayListName.length());

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setSaveButtonText();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        builder.setView(view);
        builder.setTitle(R.string.rename_playlist);

        builder.setPositiveButton(R.string.rename, (dialog, which) -> {
            String name = mEditText.getText().toString();
            if (name != null && name.length() > 0) {
                ContentResolver resolver = getActivity().getContentResolver();
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);
                resolver.update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Playlists._ID + "=?", new String[]{Long.valueOf(mPlayListId).toString()});
                Toast.makeText(getActivity(), R.string.play_list_renamed, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    private String nameForId(long id) {
        Cursor c = MusicUtils.query(mApp.getApplicationContext(), MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists.NAME},
                MediaStore.Audio.Playlists._ID + "=?",
                new String[]{Long.valueOf(id).toString()},
                MediaStore.Audio.Playlists.NAME);
        String name = null;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                name = c.getString(0);
            }
        }
        c.close();
        return name;
    }


    private void setSaveButtonText() {
        String typedName = mEditText.getText().toString();

        if (typedName.trim().length() == 0) {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            if (idForPlaylist(typedName) >= 0 && !mPlayListName.equals(typedName)) {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.overwrite);

            } else {
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.rename);
            }
        }
    }


    public int idForPlaylist(String name) {
        Cursor c = getActivity().getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?",
                new String[]{name},
                MediaStore.Audio.Playlists.NAME);
        int id = -1;
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                id = c.getInt(0);
            }
            c.close();
        }
        return id;
    }
}
