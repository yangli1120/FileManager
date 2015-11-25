package crazysheep.io.filemanager.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * dialog utils
 * <p/>
 * Created by crazysheep on 15/11/16.
 */
public class DialogUtils {

    public static interface ButtonAction {
        public String getTitle();

        public void onClick(DialogInterface dialog);
    }

    public static Dialog showConfirmDialog(Activity activity, String title, String content,
                                           final ButtonAction okAction,
                                           final ButtonAction cancelAction) {
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .positiveText(okAction.getTitle())
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        okAction.onClick(dialog);
                    }
                })
                .negativeText(cancelAction.getTitle())
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        cancelAction.onClick(dialog);
                    }
                })
                .build();
        dialog.setOwnerActivity(activity);

        if (dialog.getOwnerActivity() != null
                && !dialog.getOwnerActivity().isFinishing())
            dialog.show();

        return dialog;
    }

    /**
     * show custom dialog
     * */
    public static Dialog showCustomDialog(Activity activity, @NonNull View contentView) {
        Dialog dialog = new MaterialDialog.Builder(activity)
                .customView(contentView, false)
                .build();
        dialog.setOwnerActivity(activity);

        if(dialog.getOwnerActivity() != null && !dialog.getOwnerActivity().isFinishing())
            dialog.show();

        return dialog;
    }

    /**
     * dismiss dialog safety
     * */
    public static void dismissDialog(@NonNull Dialog dialog) {
        if(dialog.getOwnerActivity() != null && !dialog.getOwnerActivity().isFinishing())
            dialog.dismiss();
    }
}
