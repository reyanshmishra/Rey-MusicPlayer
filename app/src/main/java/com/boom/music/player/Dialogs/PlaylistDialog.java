package com.boom.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;

public class PlaylistDialog extends DialogFragment {

    private long[] mSelectedId;
    private AlertDialog.Builder mBuilder;
    private Button mPositiveButton;
    private EditText mEditText;

    public static void createPlaylist(Context context, int plid) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
        context.getContentResolver().delete(uri, null, null);
        return;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBuilder = new AlertDialog.Builder(getActivity());
        if (getArguments() != null)
            mSelectedId = getArguments().getLongArray("PLAYLIST_IDS");

        View view = getActivity().getLayoutInflater().inflate(R.layout.create_playlist, null);
        mEditText = (EditText) view.findViewById(R.id.edit_text_playlist);
        mEditText.addTextChangedListener(mTextWatcher);
        mEditText.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.black));
        mEditText.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Book-Font"));
        mEditText.setHint(R.string.new_playlist_name);
        mBuilder.setView(view);

        mBuilder.setTitle(R.string.create_playlist);

        mBuilder.setPositiveButton(R.string.save, (dialog, which) -> savePlaylist());
        mBuilder.setNegativeButton(R.string.cancel, (dialog, which) -> dismiss());

        return mBuilder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPositiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        mPositiveButton.setEnabled(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void savePlaylist() {
        String name = mEditText.getText().toString();
        if (name != null && name.length() > 0) {
            ContentResolver resolver = getContext().getContentResolver();
            int id = MusicUtils.idForPlaylist(getContext(), name);
            final Uri uri;
            if (id >= 0) {
                uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                createPlaylist(getContext(), id);
            } else {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.Playlists.NAME, name);
                uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
            }
            if (mSelectedId != null) {
                MusicUtils.addToPlaylist(getContext(), mSelectedId, Integer.valueOf(uri.getLastPathSegment()));
                String message = MusicUtils.makeLabel(getContext(), R.plurals.Nsongs, mSelectedId.length) + " " + getContext().getString(R.string.added_to_playlist);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                dismiss();
            }
            ((MainActivity) getActivity()).mAdapter.getFragment(4).onResume();
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            String newText = mEditText.getText().toString();
            mEditText.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Book-Font"));
            if (newText.trim().length() == 0) {
                mPositiveButton.setEnabled(false);
            } else {
                mPositiveButton.setEnabled(true);
                if (MusicUtils.idForPlaylist(getActivity().getApplicationContext(), newText) >= 0) {
                    mPositiveButton.setText(R.string.overwrite);
                } else {
                    mPositiveButton.setText(R.string.create_playlist);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
