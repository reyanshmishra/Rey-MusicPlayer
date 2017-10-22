package com.boom.music.player.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.boom.music.player.R;
import com.boom.music.player.TagEditor.Id3TagEditorActivity;
import com.boom.music.player.Utils.MusicUtils;
import com.boom.music.player.Utils.TypefaceHelper;

/**
 * Created by REYANSH on 4/24/2017.
 */

public class PermissionToEditSdCardDialog extends DialogFragment {

    private Activity mActivity;
    private Fragment mFragment;


    public PermissionToEditSdCardDialog(Activity context) {
        mActivity = context;
    }

    public PermissionToEditSdCardDialog(Fragment context) {
        mFragment = context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.ask_for_permission_dailog, null);

        TextView textView1 = (TextView) view.findViewById(R.id.text_line_number_1);
        TextView textView2 = (TextView) view.findViewById(R.id.text_line_number_2);
        TextView textView3 = (TextView) view.findViewById(R.id.text_line_number_3);
        TextView textView4 = (TextView) view.findViewById(R.id.text_line_number_4);
        TextView textView5 = (TextView) view.findViewById(R.id.text_line_number_5);
        TextView textView6 = (TextView) view.findViewById(R.id.text_line_number_6);
        TextView textView7 = (TextView) view.findViewById(R.id.text_line_number_7);

        textView1.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));
        textView2.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Bold-Font"));
        textView3.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));
        textView4.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));
        textView5.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));

        textView6.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Bold-Font"));
        textView7.setTypeface(TypefaceHelper.getTypeface(getActivity().getApplicationContext(), "Futura-Condensed-Font"));

        builder.setView(view);
        builder.setTitle(R.string.grant_permission);

        builder.setNegativeButton(R.string.no, (dialog, which) -> {
            dialog.dismiss();
            if (getActivity() instanceof Id3TagEditorActivity) {
                getActivity().finish();
            }
        });


        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            if (mFragment == null) {
                mActivity.startActivityForResult(intent, MusicUtils.URI_REQUEST_CODE_DELETE);
            } else {
                mFragment.startActivityForResult(intent, MusicUtils.URI_REQUEST_CODE_DELETE);
            }
            dialog.dismiss();
        });
        return builder.create();
    }
}
