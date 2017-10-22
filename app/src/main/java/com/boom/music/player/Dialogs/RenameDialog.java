package com.boom.music.player.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.boom.music.player.Common;
import com.boom.music.player.FileDirectory.FolderFragment;
import com.boom.music.player.LauncherActivity.MainActivity;
import com.boom.music.player.R;
import com.boom.music.player.Utils.FileUtils;
import com.boom.music.player.Utils.TypefaceHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by REYANSH on 8/6/2017.
 */

public class RenameDialog extends DialogFragment {

    private View mView;
    private EditText mEditText;
    private File mFile;
    private File mParentFile;
    private ArrayList<String> mFileNames;
    private Button mPositiveButton;
    private String mFileExt;
    private String mFileName;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mView = getActivity().getLayoutInflater().inflate(R.layout.dialog_rename, null);
        builder.setView(mView);
        mFile = new File(getArguments().getString("FILE_PATH"));

        if (mFile.isFile()) {
            mFileExt = "." + FileUtils.getFileExtension(mFile.getName());
            mFileName = mFile.getName().substring(0, mFile.getName().lastIndexOf("."));
            if (mFileName.equalsIgnoreCase("")){
                mFileName=mFile.getName();
            }
        } else {
            mFileExt = "";
            mFileName = mFile.getName();
        }

        mParentFile = mFile.getParentFile();
        mFileNames = new ArrayList<>();
        builder.setTitle(R.string.rename);
        for (File file : mParentFile.listFiles()) {
            mFileNames.add(file.getName());
        }

        mEditText = (EditText) mView.findViewById(R.id.edit_text);
        mEditText.setText(mFileName);
        mEditText.setSelection(mFileName.length());
        mEditText.setTypeface(TypefaceHelper.getTypeface(Common.getInstance(), "Futura-Book-Font"));

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If there is any other files with the same name. Warn.
                if (mFileNames.contains(s.toString().trim())) {
                    mPositiveButton.setEnabled(false);
                } else {
                    mPositiveButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            FileUtils.rename(mFile, mEditText.getText().toString() + mFileExt);
            FolderFragment fragment = (FolderFragment) ((MainActivity) getActivity()).mAdapter.getFragment(5);
            fragment.refreshListView();

        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPositiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);

    }
}
