package crazysheep.io.filemanager.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import crazysheep.io.filemanager.R;

/**
 * dialog utils
 * <p/>
 * Created by crazysheep on 15/11/16.
 */
public class DialogUtils {

    public interface ButtonAction {
        String getTitle();

        void onClick(DialogInterface dialog);
    }

    public interface InputCallback {
        void onInput(DialogInterface dialog, String s);
    }

    public static Dialog showConfirmDialog(@NonNull Activity activity, String title, String content,
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

    public static Dialog showSingleConfirmDialog(@NonNull Activity activity, String title,
                                                 String content) {
        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(title)
                .content(content)
                .positiveText(activity.getString(R.string.ok))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        dismissDialog(dialog);
                    }
                })
                .build();
        dialog.setOwnerActivity(activity);

        if(dialog.getOwnerActivity() != null
                && !dialog.getOwnerActivity().isFinishing())
            dialog.show();

        return dialog;
    }

    /**
     * show input dialog
     * */
    public static Dialog showInputDialog(@NonNull Activity activity, String title,
                                         String hint, final InputCallback callback) {
        Dialog dialog = new MaterialDialog.Builder(activity)
                .title(title)
                .input(hint, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (callback != null)
                            callback.onInput(dialog, input.toString());
                    }
                })
                .build();
        dialog.setOwnerActivity(activity);

        if(dialog.getOwnerActivity() != null
                && !dialog.getOwnerActivity().isFinishing())
            dialog.show();

        return dialog;
    }

    /**
     * show custom dialog
     * */
    public static Dialog showCustomDialog(@NonNull Activity activity, String title,
                                          @NonNull View contentView) {
        Dialog dialog = new MaterialDialog.Builder(activity)
                .title(title)
                .customView(contentView, true)
                .positiveText(R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                                        @NonNull DialogAction which) {
                        dismissDialog(dialog);
                    }
                })
                .build();
        dialog.setOwnerActivity(activity);

        if(dialog.getOwnerActivity() != null && !dialog.getOwnerActivity().isFinishing())
            dialog.show();

        return dialog;
    }

    /**
     * show loading dialog
     * */
    public static Dialog showLoadingDialog(@NonNull Activity activity, String title,
                                           boolean cancelable, boolean cancelTouchOutside) {
        Dialog dialog = new MaterialDialog.Builder(activity)
                .title(title)
                .progress(true, 0)
                .build();
        dialog.setOwnerActivity(activity);
        dialog.setCanceledOnTouchOutside(cancelTouchOutside);
        dialog.setCancelable(cancelable);

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
