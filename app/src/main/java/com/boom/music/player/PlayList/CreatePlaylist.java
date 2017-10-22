package com.boom.music.player.PlayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.boom.music.player.R;


public class CreatePlaylist extends Activity {
    private EditText mPlaylist;
    private TextView mPrompt;
    private Button mSaveButton;


    public interface Defs {
        int OPEN_URL = 0;
        int ADD_TO_PLAYLIST = 1;
        int USE_AS_RINGTONE = 2;
        int PLAYLIST_SELECTED = 3;
        int NEW_PLAYLIST = 4;
        int PLAY_SELECTION = 5;
        int GOTO_START = 6;
        int GOTO_PLAYBACK = 7;
        int PARTY_SHUFFLE = 8;
        int SHUFFLE_ALL = 9;
        int DELETE_ITEM = 10;
        int SCAN_DONE = 11;
        int QUEUE = 12;
        int EFFECTS_PANEL = 13;
        int CHILD_MENU_BASE = 14; // this should be the last item
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.create_playlist);
//        mPrompt = (TextView) findViewById(R.id.prompt);
//        mPlaylist = (EditText) findViewById(R.id.playlist);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mPlaylist, InputMethodManager.SHOW_IMPLICIT);

        mPlaylist.addTextChangedListener(mTextWatcher);
//        mSaveButton = (Button) findViewById(R.id.create);
        mSaveButton.setOnClickListener(mOpenClicked);
       /*((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               finish();
           }
       });*/
    }

    private int idForplaylist(String name) {
        Cursor c = this.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
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

    @Override
    public void onSaveInstanceState(Bundle outcicle) {
        outcicle.putString("defaultname", mPlaylist.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private View.OnClickListener mOpenClicked = new View.OnClickListener() {
        public void onClick(View v) {
            String name = mPlaylist.getText().toString();
            if (name != null && name.length() > 0) {
                ContentResolver resolver = getContentResolver();
                int id = idForplaylist(name);
                Uri uri;
                if (id >= 0) {
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, id);
                    clearPlaylist(CreatePlaylist.this, id);
                } else {
                    ContentValues values = new ContentValues(1);
                    values.put(MediaStore.Audio.Playlists.NAME, name);
                    uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                }
                setResult(RESULT_OK, (new Intent()).setData(uri));
                finish();
            }
        }
    };

    public static void makePlaylistMenu(Context context, SubMenu sub) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME
        };
        ContentResolver resolver = context.getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Playlists.NAME + " != ''";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    cols, whereclause, null,
                    MediaStore.Audio.Playlists.NAME);
            sub.clear();
            sub.add(1, Defs.NEW_PLAYLIST, 0, R.string.new_playlist);
            if (cur != null && cur.getCount() > 0) {
                //sub.addSeparator(1, 0);
                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    Intent intent = new Intent();
                    intent.putExtra("playlist", cur.getLong(0));
//                    if (cur.getInt(0) == mLastPlaylistSelected) {
//                        sub.add(0, MusicBaseActivity.PLAYLIST_SELECTED, cur.getString(1)).setIntent(intent);
//                    } else {
                    sub.add(1, Defs.PLAYLIST_SELECTED, 0, cur.getString(1)).setIntent(intent);
//                    }
                    cur.moveToNext();
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }


    public static void clearPlaylist(Context context, int plid) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", plid);
        context.getContentResolver().delete(uri, null, null);
        return;
    }


    TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // don't care about this one
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String newText = mPlaylist.getText().toString();
            if (newText.trim().length() == 0) {
                mSaveButton.setEnabled(false);
            } else {
                mSaveButton.setEnabled(true);
                // check if playlist with current name exists already, and warn the user if so.
                if (idForplaylist(newText) >= 0) {
                    mSaveButton.setText(R.string.overwrite);
                } else {
                    mSaveButton.setText(R.string.create_playlist);
                }
            }
        }

        public void afterTextChanged(Editable s) {
            // don't care about this one
        }
    };
}