package com.example.refapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.example.refapp.R;

import roboguice.util.Ln;


public class DialogHelper {
    static public Dialog createDialog(final Activity activity, final int id, final Bundle args) {
        switch (id) {
            case R.id.dialog_id_searching: {
                return createProgressDialog(activity, R.string.msg_searching, null);
            }
            case R.id.dialog_id_loading: {
                return createProgressDialog(activity, R.string.msg_loading, null);
            }
        }
        return null;
    }

    static public Dialog createProgressDialog(Activity activity, int messageResId,
                                              DialogInterface.OnCancelListener onCancelListener) {
        Dialog dialog = null;

        try {
            dialog = ProgressDialog.show(activity, "", activity.getString(messageResId), true);
            dialog.setOwnerActivity(activity);
            if (onCancelListener == null) {
                dialog.setCancelable(false);
            } else {
                dialog.setCancelable(true);
                dialog.setOnCancelListener(onCancelListener);
            }
        } catch (Throwable e) {
            Ln.w(e);
            dialog = null;
        }
        return dialog;
    }
}