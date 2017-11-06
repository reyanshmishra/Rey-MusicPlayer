package com.boom.music.player.FileDirectory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.boom.music.player.AsyncTasks.AsyncAddTo;
import com.boom.music.player.Common;
import com.boom.music.player.Dialogs.PlaylistDialog;
import com.boom.music.player.Interfaces.OnScrolledListener;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.Models.Song;
import com.boom.music.player.NowPlaying.NowPlayingActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.HidingScrollListener;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.PreferencesHelper;
import com.boom.music.player.Views.DividerItemDecoration;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class FolderFragment extends Fragment implements View.OnClickListener, MusicUtils.Defs {
    public static final int FOLDER = 0;
    public static final int FILE = 1;
    public static final int AUDIO_FILE = 3;
    public String currentDir;
    public ArrayList<Song> fetchedFiles = new ArrayList<>();

    private final long kiloBytes = 1024;
    private final long megaBytes = kiloBytes * kiloBytes;
    private final long gigaBytes = megaBytes * kiloBytes;
    private final long teraBytes = gigaBytes * kiloBytes;

    private Common mApp;
    private Context mContext;
    private View v;
    private String rootDir;
    private List<String> fileFolderNameList = null;
    private List<String> fileFolderPathList = null;
    private List<String> fileFolderSizeList = null;
    private List<Integer> fileFolderTypeList = null;

    private HashMap<String, Parcelable> mFolderStateMap;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private FolderAdapter mFolderAdapter;
    private OnScrolledListener mOnScrolledListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.file_directory, container, false);
        mContext = getActivity().getApplication();
        mApp = (Common) mContext.getApplicationContext();
        setHasOptionsMenu(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, 20, 20));

        mFolderAdapter = new FolderAdapter(this);
        mRecyclerView.setAdapter(mFolderAdapter);

        mFolderStateMap = new HashMap<>();
        rootDir = PreferencesHelper.getInstance().getString(PreferencesHelper.Key.PREVIOUS_ROOT_DIR, Environment.getExternalStorageDirectory().getPath());
        currentDir = rootDir;
        getDir(currentDir, null);
        mRecyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                mOnScrolledListener.onScrolledUp();
            }

            @Override
            public void onShow() {
                mOnScrolledListener.onScrolledDown();
            }

        });
        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mOnScrolledListener = (OnScrolledListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnScrolledListener = null;
    }


    private void getDir(String dirPath, Parcelable restoreState) {
        fileFolderNameList = new ArrayList<>();
        fileFolderPathList = new ArrayList<>();
        fileFolderSizeList = new ArrayList<>();
        fileFolderTypeList = new ArrayList<>();
        final File f = new File(dirPath);
        File[] files = f.listFiles();
        if (files != null) {
            Arrays.sort(files, new FileTypeComparator());
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.canRead()) {
                    if (file.isDirectory()) {
                        String filePath;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                            filePath = getRealFilePath(file.getAbsolutePath());
                        else
                            filePath = file.getAbsolutePath();

                        fileFolderPathList.add(filePath);
                        fileFolderNameList.add(file.getName());
                        File[] listOfFiles = file.listFiles();

                        if (listOfFiles != null) {
                            fileFolderTypeList.add(FOLDER);
                            if (listOfFiles.length == 1) {
                                fileFolderSizeList.add("" + listOfFiles.length + " item");
                            } else {
                                fileFolderSizeList.add("" + listOfFiles.length + " items");
                            }
                        } else {
                            fileFolderTypeList.add(FOLDER);
                            fileFolderSizeList.add("Unknown items");
                        }
                    } else {
                        try {
                            String path = file.getCanonicalPath();
                            fileFolderPathList.add(path);
                        } catch (IOException e) {
                            continue;
                        }
                        fileFolderNameList.add(file.getName());
                        String fileName = "";
                        try {
                            fileName = file.getCanonicalPath();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (getFileExtension(fileName).equalsIgnoreCase("mp3")
                                || getFileExtension(fileName).equalsIgnoreCase("ogg")
                                || getFileExtension(fileName).equalsIgnoreCase("wav")
                                || getFileExtension(fileName).equalsIgnoreCase("ma4")) {

                            fileFolderTypeList.add(AUDIO_FILE);
                            fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
                            String[] whereArgs = new String[]{file.getPath()};
                            fetchedFiles.add(getSongs(whereArgs));
                        } else {
                            fileFolderTypeList.add(FILE);
                            fileFolderSizeList.add("" + getFormattedFileSize(file.length()));
                        }
                    }
                }
            }
        }


        mFolderAdapter.updateData(fileFolderNameList,
                fileFolderTypeList,
                fileFolderSizeList,
                fileFolderPathList);
        if (restoreState != null) {
            mLayoutManager.onRestoreInstanceState(restoreState);
        } else if (mFolderStateMap.containsKey(dirPath)) {
            mLayoutManager.onRestoreInstanceState(mFolderStateMap.get(dirPath));
        }
        PreferencesHelper.getInstance().put(PreferencesHelper.Key.PREVIOUS_ROOT_DIR, dirPath);
    }

    public void onClick(View view, int index) {

        if (mFolderStateMap.size() == 3) {
            mFolderStateMap.clear();
        }

        mFolderStateMap.put(currentDir, mLayoutManager.onSaveInstanceState());

        String newPath = fileFolderPathList.get(index);
        if ((Integer) view.getTag(R.string.folder_list_item_type) == FOLDER)
            currentDir = newPath;
        if (fileFolderTypeList.get(index) == FOLDER) {
            fetchedFiles.clear();
            getDir(newPath, null);
        } else if (fileFolderTypeList.get(index) == AUDIO_FILE) {
            int fileIndex = 0;
            for (int i = 0; i < index; i++) {
                if (fileFolderTypeList.get(i) == AUDIO_FILE)
                    fileIndex++;
            }
            mApp.getPlayBackStarter().playSongs(fetchedFiles, fileIndex);
            startActivity(new Intent(mContext, NowPlayingActivity.class));
        } else {
            Toast.makeText(mContext, "Sorry can't open this file!!", Toast.LENGTH_SHORT).show();
        }

    }

    public String getFileExtension(String fileName) {
        String fileNameArray[] = fileName.split("\\.");
        String extension = fileNameArray[fileNameArray.length - 1];
        return extension;
    }

    @SuppressLint("SdCardPath")
    private String getRealFilePath(String filePath) {
        if (filePath.equals("/storage/emulated/0") ||
                filePath.equals("/storage/emulated/0/") ||
                filePath.equals("/storage/emulated/legacy") ||
                filePath.equals("/storage/emulated/legacy/") ||
                filePath.equals("/storage/sdcard0") ||
                filePath.equals("/storage/sdcard0/") ||
                filePath.equals("/sdcard") ||
                filePath.equals("/sdcard/") ||
                filePath.equals("/mnt/sdcard") ||
                filePath.equals("/mnt/sdcard/")) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return filePath;
    }

    public String getFormattedFileSize(final long value) {
        final long[] dividers = new long[]{teraBytes, gigaBytes, megaBytes, kiloBytes, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "bytes"};
        if (value < 1) {
            return "";
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    public String format(final long value, final long divider, final String unit) {
        final double result = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    public boolean getParentDir() {

        if (currentDir.equals("/"))
            return true;
        File currentFolder = new File(currentDir);
        String parentFolder = "";
        try {
            parentFolder = currentFolder.getParentFile().getCanonicalPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentDir = parentFolder;
        fetchedFiles.clear();
        getDir(parentFolder, null);
        return false;

    }

    public String getCurrentDir() {
        return currentDir;
    }


    @Override
    public void onClick(View v) {
        /*if ((v.getId() == R.id.back_imagebutton)) {
            if (getCurrentDir().equals("/")) {
            } else {
                getParentDir();
            }
        } else if (v.getId() == R.id.image_button_add) {
            if (fetchedFiles.size() != 0 && fetchedFiles != null) {
                mApp.getPlayBackStarter().addToQueue(fetchedFiles);
                Toast.makeText(mContext, fetchedFiles.size() + "Added to the queue.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mContext, "No songs in the current directory!!", Toast.LENGTH_SHORT).show();
            }
        }*/
    }


    public void onItemClicked(int index) {

        if (mFolderStateMap.size() == 3) {
            mFolderStateMap.clear();
        }

        mFolderStateMap.put(currentDir, mLayoutManager.onSaveInstanceState());

        String newPath = fileFolderPathList.get(index);
        if (fileFolderTypeList.get(index) == FOLDER)
            currentDir = newPath;
        if (fileFolderTypeList.get(index) == FOLDER) {
            fetchedFiles.clear();
            getDir(newPath, null);
        } else if (fileFolderTypeList.get(index) == AUDIO_FILE) {
            int fileIndex = 0;
            for (int i = 0; i < index; i++) {
                if (fileFolderTypeList.get(i) == AUDIO_FILE)
                    fileIndex++;
            }
            mApp.getPlayBackStarter().playSongs(fetchedFiles, fileIndex);
            startActivity(new Intent(mContext, NowPlayingActivity.class));
        } else {
            Toast.makeText(mContext, R.string.file_open_error, Toast.LENGTH_SHORT).show();
        }

    }

    public void refreshListView() {
        getDir(currentDir, mLayoutManager.onSaveInstanceState());
    }


    public void onPopUpMenuClickListener(View v, final int position) {

        final PopupMenu menu = new PopupMenu(getActivity(), v);
        SubMenu sub = (menu.getMenu()).addSubMenu(0, ADD_TO_PLAYLIST, 1, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(getActivity(), sub, 0);

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.popup_song_play_next:
                    new AsyncAddTo(fetchedFiles.get(position)._title, false, fetchedFiles.get(position)).execute();
                    break;
                case R.id.popup_song_addto_queue:
                    new AsyncAddTo(fetchedFiles.get(position)._title, true, fetchedFiles.get(position)).execute();
                    break;
                case R.id.popup_song_add_to_favs:
                    mApp.getDBAccessHelper().addToFavorites(fetchedFiles.get(position));
                    break;
                case R.id.popup_song_delete:
                    ArrayList<Song> song = new ArrayList<>();
                    song.add(fetchedFiles.get(position));
                    MusicUtils.deleteFile(FolderFragment.this, song, onTaskCompleted);
                    break;
                case R.id.popup_song_use_as_phone_ringtone:
                    MusicUtils.setRingtone((AppCompatActivity) getActivity(), fetchedFiles.get(position)._id);
                    break;
                case R.id.popup_song_share:
                    MusicUtils.shareTheMusic(FolderFragment.this.getActivity(), fetchedFiles.get(position)._path);
                    break;
                case NEW_PLAYLIST:
                    PlaylistDialog playlistDialog = new PlaylistDialog();
                    Bundle bundle = new Bundle();
                    bundle.putLongArray("PLAYLIST_IDS", new long[]{fetchedFiles.get(position)._id});
                    playlistDialog.setArguments(bundle);
                    playlistDialog.show(getActivity().getSupportFragmentManager(), "FRAGMENT_TAG");
                    return true;
                case PLAYLIST_SELECTED:
                    long[] list = new long[]{fetchedFiles.get(position)._id};
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(getContext(), list, playlist);
                    return true;
            }
            return false;
        });
        menu.inflate(R.menu.popup_song);
        menu.show();
    }

    public void onFilePopUpClicked(View v, int position) {
        final PopupMenu menu = new PopupMenu(getActivity(), v);
        menu.setOnMenuItemClickListener(item -> {
            ArrayList<Song> songs = new ArrayList<>();
            Collection<File> files = FileUtils.listFiles(new File(fileFolderPathList.get(position)), new String[]{"mp3", "ma4", "ogg", "wav"}, false);

            for (File file : files) {
                songs.add(getSongs(new String[]{file.getAbsolutePath()}));
            }

            if (songs.size() == 0) {
                Toast.makeText(mContext, R.string.audio_files_not_found, Toast.LENGTH_SHORT).show();
                return false;
            }

            switch (item.getItemId()) {
                case R.id.popup_file_play:
                    mApp.getPlayBackStarter().playSongs(songs, 0);
                    startActivity(new Intent(mContext, NowPlayingActivity.class));
                    break;
                case R.id.popup_file_add_to_queue:
                    new AsyncAddTo(getString(R.string.songs_added_to_queue), true, songs).execute();
                    break;
                case R.id.popup_file_play_next:
                    new AsyncAddTo(getString(R.string.will_be_played_next), false, songs).execute();
                    break;
            }
            return false;
        });

        menu.inflate(R.menu.popup_file);
        menu.show();
    }

    public void onUpClick() {
        getParentDir();
    }

    private class FileNameComparator implements Comparator<File> {
        public int compare(File a, File b) {
            return a.getName().compareTo(b.getName());

        }
    }

    private class FileTypeComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            String s1 = lhs.getName();
            String s2 = rhs.getName();
            Log.d("name", s1 + "" + s2);
            final int s1Dot = s1.lastIndexOf('.');
            final int s2Dot = s2.lastIndexOf('.');
            if ((s1Dot == -1) == (s2Dot == -1)) {
                s1 = s1.substring(s1Dot + 1);
                s2 = s2.substring(s2Dot + 1);
                return s1.compareTo(s2);
            } else if (s1Dot == -1) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private Song getSongs(String[] whereArgs) {
        Song song = null;
        String[] columns = {
                BaseColumns._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                columns,
                MediaStore.Audio.Media.DATA + " LIKE ?", whereArgs, MediaStore.Audio.Media.TITLE);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                song = new Song(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getString(4),
                        cursor.getLong(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getLong(8));
            } while (cursor.moveToNext());
        }
        return song;
    }

    OnTaskCompleted onTaskCompleted = () -> refreshListView();
}
