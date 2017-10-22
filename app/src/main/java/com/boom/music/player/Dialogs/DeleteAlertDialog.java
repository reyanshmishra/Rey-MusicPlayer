package com.boom.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.boom.music.player.AsyncTasks.AsyncTaskDelete;
import com.boom.music.player.Interfaces.OnTaskCompleted;
import com.boom.music.player.Models.Song;
import com.boom.music.player.R;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by REYANSH on 10/09/2016.
 */
public class DeleteAlertDialog extends DialogFragment {


    private OnTaskCompleted mOnDeletedListener;
    private ArrayList<Song> mFiles;
    private int mPosition;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Set the dialog title.
        builder.setTitle(R.string.delete);
        StringBuilder message = new StringBuilder();

        message.append(getString(R.string.these_files_will_be_deleted_permanently) + "\n \n");

        for (int i = 0; i < mFiles.size(); i++) {
            File file = new File(mFiles.get(i)._path);
            message.append(file.getName() + ".\n");
        }

        builder.setMessage(message);

        builder.setNegativeButton(R.string.no, (arg0, arg1) -> dismiss());

        builder.setPositiveButton(R.string.yes, (arg0, arg1) -> {
            dismiss();
            AsyncTaskDelete asyncTaskDelete = new AsyncTaskDelete(getActivity());
            asyncTaskDelete.setListener(mOnDeletedListener);
            asyncTaskDelete.execute(mFiles);
        });

        return builder.create();

    }


    public void setTaskCompletionListener(OnTaskCompleted deleteListener) {
        mOnDeletedListener = deleteListener;
    }

    public void setFiles(ArrayList<Song> files) {
        mFiles = files;
    }

    public void setPosition(int position) {
        mPosition = position;
    }
}

