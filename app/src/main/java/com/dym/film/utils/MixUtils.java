package com.dym.film.utils;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/10
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.ui.CustomDialog;

/**
 * 一些杂项的工具方法
 */
public class MixUtils
{
    /**
     * Toast 显示
     */
    protected static Toast mStaticToast = null;

    public static void toastShort(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastShort(Context context, String msg, int gravity)
    {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    public static void toastLong(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastLong(Context context, String msg, int gravity)
    {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }


    /**
     * 使用SnackBar进行显示提示
     */
    public static void snackShort(@NonNull Context context,
                                  @NonNull View view,
                                  @NonNull String msg)
    {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void snackShort(@NonNull Context context,
                                  @NonNull View view,
                                  @NonNull String msg,
                                  int resId,
                                  View.OnClickListener listener)
    {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).setAction(resId, listener).show();
    }


    public static void snackLong(@NonNull Context context,
                                 @NonNull View view,
                                 @NonNull String msg)
    {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void snackLong(@NonNull Context context,
                                 @NonNull View view,
                                 @NonNull String msg,
                                 int resId,
                                 View.OnClickListener listener)
    {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction(resId, listener).show();
    }

    /**
     * AlertDialog 显示
     */
    protected static AlertDialog mAlertDialog = null;
    /**
     * 显示AlertDialog;
     */
    public static void showAlertDialog(
            Context context, String title, String msg, boolean cancelable)
    {
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setPositiveButton("O K", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            mAlertDialog = builder.create();
        }

        mAlertDialog.setTitle(title);
        mAlertDialog.setMessage(msg);
        mAlertDialog.setCancelable(cancelable);

        mAlertDialog.show();
    }

    /**
     * 隐藏AlertDialog
     */
    public static void dismissAlertDialog()
    {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }


    /**
     * ProgressDialog 显示
     */
    protected static TextView mProgressTextView = null;
    protected static CustomDialog mProgressDialog = null;
    /**
     * 显示ProgressDialog
     */
    public static void showProgressDialog(Context context, String msg, boolean cancelable)
    {
//        if (mProgressDialog == null ) {
            mProgressDialog = new CustomDialog(context);
            mProgressDialog.setGravity(Gravity.CENTER);
            mProgressDialog.setWindowAnimations(R.style.default_dialog_animation);
            mProgressDialog.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            View view = View.inflate(context, R.layout.layout_progress_dialog, null);
            mProgressTextView = (TextView) view.findViewById(R.id.progressText);
            mProgressDialog.setContentView(view);
//        }

        if (msg == null) {
            mProgressTextView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressTextView.setVisibility(View.VISIBLE);
            mProgressTextView.setText(msg);
        }
        mProgressDialog.setCancelable(cancelable);

        mProgressDialog.show();
    }

    public static void setProgressDialogMessage(String msg)
    {
        if (mProgressTextView != null) {
            mProgressTextView.setVisibility(View.VISIBLE);
            mProgressTextView.setText(msg);
        }
    }

    /**
     * 隐藏ProgressDialog
     */
    public static void dismissProgressDialog()
    {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    /**
     * 像素转换工具
     */
    public static int dpToPx(Context context, float dp)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int pxToDp(Context context, float px)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }






    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
