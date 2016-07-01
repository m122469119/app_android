package com.dym.film.activity.sharedticket;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.BaseThread;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MixUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/1/15
 */
public class SharedTicketOnlyImageActivity extends BaseViewCtrlActivity
{
    private final static String TAG = "SharedTicketImage";

    public final static String KEY_URL = "URL_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        new SharedTicketOnlyImageController();
    }

    public class SharedTicketOnlyImageController extends BaseContentViewController
    {
        private String mImageUrl = "";

        private CustomDialog mSaveImageDialog = null;

        public SharedTicketOnlyImageController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_shared_ticket_only_image;
        }

        protected void initialize()
        {
            // 获取传递的数据
            String imageUrl = getIntentString(SharedTicketOnlyImageActivity.KEY_URL);

            setOnClickListener(R.id.closeButtonImage);

            final PhotoView photoView = (PhotoView) findViewById(R.id.ticketImageView);
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.loadProgress);

            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener()
            {
                @Override
                public void onViewTap(View view, float x, float y)
                {
                    finish();
                }
            });

            mImageUrl = QCloudManager.urlImage2(imageUrl, ConfigInfo.SIZE_STICKET_DETAIL_WIDTH);
            LogUtils.e(TAG, "URL: " + mImageUrl);
            ImageLoader.getInstance().loadImage(mImageUrl, new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingStarted(String s, View view)
                {
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason)
                {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap)
                {
                    progressBar.setVisibility(View.GONE);

                    if (bitmap != null) {
                        //Loge(TAG, "Width: " + bitmap.getWidth() + " Height: " + bitmap.getHeight());
                        photoView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view)
                {
                    progressBar.setVisibility(View.GONE);
                }
            });

            photoView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    showSaveImageDialog(mImageUrl);
                    return false;
                }
            });
        }

        @Override
        protected void onViewClicked(@NonNull View v)
        {
            switch (v.getId()) {
                case R.id.closeButtonImage:
                    finish();
                    break;
            }
        }


        private void showSaveImageDialog(final String url)
        {
            if (mSaveImageDialog == null) {
                mSaveImageDialog = new CustomDialog(mActivity);
                mSaveImageDialog.setGravity(Gravity.BOTTOM);
                mSaveImageDialog.setCancelable(true);
                mSaveImageDialog.setWindowAnimations(R.style.bottom_dialog_animation);
            }

            View view = View.inflate(mActivity, R.layout.layout_save_image_button, null);
            View saveImageBtn = view.findViewById(R.id.saveImageButton);
            saveImageBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    saveImage(url);
                }
            });
            mSaveImageDialog.setContentView(view);
            mSaveImageDialog.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            mSaveImageDialog.show();
        }

        private void saveImage(String url)
        {
            final File srcFile = ImageLoader.getInstance().getDiskCache().get(url);
            final File desFile = new File(ConfigInfo.APP_IMAGE_DIR_FILE, "DYM-" + System.currentTimeMillis() + ".jpg");
            if (srcFile.exists()) {
                new BaseThread()
                {
                    @Override
                    public void run()
                    {
                        try {
                            FileInputStream fis = new FileInputStream(srcFile);
                            FileOutputStream fos = new FileOutputStream(desFile);

                            byte[] buffer = new byte[1024];
                            int read = 0;
                            while ((read = fis.read(buffer)) > 0) {
                                fos.write(buffer, 0, read);
                            }
                            fos.flush();

                            fis.close();
                            fos.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        sendMessage(0);
                    }

                    @Override
                    protected void handleMessage(Message msg)
                    {
                        if (desFile.exists()) {
                            MixUtils.toastLong(mActivity, "图片成功保存至: " + desFile.getAbsolutePath());
                            CommonManager.saveImageToGallery(mActivity, desFile);
                        }
                        else {
                            MixUtils.toastShort(mActivity, "图片保存失败");
                        }

                        mSaveImageDialog.dismiss();
                    }
                }.startThread();
            }
            else {
                ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener()
                {
                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap)
                    {
                        if (bitmap != null) {
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(desFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        if (desFile.exists()) {
                            MixUtils.toastLong(mActivity, "图片成功保存至: " + desFile.getAbsolutePath());
                        }

                        mSaveImageDialog.dismiss();
                    }
                });
            }
        }

    }
}
