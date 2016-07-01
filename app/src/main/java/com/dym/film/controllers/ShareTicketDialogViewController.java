package com.dym.film.controllers;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.fragment.SharedTicketFragment;
import com.dym.film.manager.CommonManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.MixUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/18
 */

/**
 * 选择是 相册 或者 拍照 来进行晒票的view控制器
 */
public class ShareTicketDialogViewController extends BaseViewController
{

    public final static int REQUEST_CODE_PICK_IMAGE = 0x21;
    public final static int REQUEST_CODE_CAPTURE_CAMERA = 0x31;

    /**
     * 用来保存从Camera拍照完成之后，图片存储的URI
     */
    private static Uri mCameraImageUri = null;

    private CustomDialog mCustomDialog = null;


    public ShareTicketDialogViewController(@NonNull Activity activity)
    {
        super(activity, R.layout.dialog_select_picture_camera);

        if (mCustomDialog == null) {
            mCustomDialog = new CustomDialog(activity);
            mCustomDialog.setWindowAnimations(R.style.bottom_dialog_animation);
            mCustomDialog.setContentView(mRootView);
            mCustomDialog.setGravity(Gravity.BOTTOM);
            mCustomDialog.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            mCustomDialog.setCancelable(true);
        }

        initializeDialogView();
    }

    private void initializeDialogView()
    {
        setOnClickListener(R.id.cameraButton);
        setOnClickListener(R.id.galleryButton);
        setOnClickListener(R.id.backButton);
    }

    public void show()
    {
        if (UserInfo.isLogin) {
            mCustomDialog.show();
        }
        else {
            MixUtils.toastShort(mActivity, "晒票需要登录");
            CommonManager.startLoginActivity(mActivity);
        }
    }

    @Override
    protected void onViewClicked(@NonNull View v)
    {
        switch (v.getId()) {
            case R.id.cameraButton:
                getImageFromCamera();
                break;

            case R.id.galleryButton:
                getImageFromAlbum();
                break;

            case R.id.backButton:
                break;
        }

        mCustomDialog.dismiss();
    }

    protected synchronized void getImageFromCamera()
    {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = null;
        if (ConfigInfo.APP_IMAGE_DIR_FILE.exists()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.CHINA);
            imageFile = new File(ConfigInfo.APP_IMAGE_DIR_FILE,
                    "DYM-" + format.format(new Date()) + ".jpg");
        }
        else {
            imageFile = new File(mActivity.getFilesDir(), "DYM-share-ticket.jpg");
        }

        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, "公证电影晒票");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri imageFilePath =  mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFilePath); //这样就将文件的存储方式和uri指定到了Camera应用中

        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        mCameraImageUri = imageFilePath;
        if (mActivity instanceof MainActivity) {

            SharedTicketFragment fragment = ((MainActivity) mActivity).getSharedTicketFragment();
            if (fragment != null) {
                fragment.startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMERA);
            }
        }
        else {
            mActivity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMERA);
        }
    }

    protected void getImageFromAlbum()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        if (mActivity instanceof MainActivity) {
            SharedTicketFragment fragment = ((MainActivity) mActivity).getSharedTicketFragment();
            if (fragment != null) {
                fragment.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        }
        else {
            mActivity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    public static Uri getCameraImageUri()
    {
        return mCameraImageUri;
    }

    public static void onActivityFinished()
    {
        mCameraImageUri = null;
    }
}
