package com.dym.film.activity.home;

import android.graphics.Bitmap;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.RecyclingPagerAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.BaseThread;
import com.dym.film.manager.CommonManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MixUtils;
import com.dym.film.views.HackyViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class FilmBigPostActivity extends BaseActivity
{

    private HackyViewPager viewPager;
    private ArrayList<String> filmPostDatas;
    private int curPosition;
    private CustomDialog mSaveImageDialog;
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_big_post;
    }

    @Override
    protected void initVariable()
    {
        filmPostDatas = (ArrayList<String>) getIntent().getSerializableExtra("filmPostDatas");
        curPosition = getIntent().getIntExtra("curPosition", 0);
    }

    @Override
    protected void findViews()
    {
        showTopBar();
        setTitle((curPosition + 1) + "/" + filmPostDatas.size());
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
    }

    @Override
    protected void initData()
    {
        viewPager.setAdapter(new RecyclingPagerAdapter()
        {
            @Override
            public int getCount()
            {
                return filmPostDatas.size();
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup container)
            {
                PhotoView photoView = null;
                if (convertView == null) {
                    convertView =new PhotoView(mContext);
                }
                photoView = (PhotoView) convertView;
                ImageLoader.getInstance().displayImage(filmPostDatas.get(position), photoView, new SimpleImageLoadingListener()
                {
                    /*必须先设置完图片后再设置监听，否则无效，因为内部会更新photoView状态*/
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
                    {
                        view.setOnLongClickListener(new View.OnLongClickListener()
                        {
                            @Override
                            public boolean onLongClick(View view)
                            {
                                LogUtils.i("123", "photoView" + position);
                                showSaveImageDialog(filmPostDatas.get(position));
                                return true;
                            }
                        });
                    }
                });

                return convertView;
            }
        });
        viewPager.setCurrentItem(curPosition);
    }

    @Override
    protected void setListener()
    {
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                setTitle((position + 1) + "/" + filmPostDatas.size());
            }
        });
    }

    @Override
    public void doClick(View view)
    {
        this.finish();
        overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showSaveImageDialog(final String url)
    {
        if (mSaveImageDialog == null) {
            mSaveImageDialog = new CustomDialog(FilmBigPostActivity.this);
            mSaveImageDialog.setGravity(Gravity.BOTTOM);
            mSaveImageDialog.setCancelable(true);
            mSaveImageDialog.setWindowAnimations(R.style.bottom_dialog_animation);
        }
        View view = mInflater.inflate(R.layout.layout_save_image_button, null);
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
                        MixUtils.toastLong(mContext, "图片成功保存至: " + desFile.getAbsolutePath());
                        CommonManager.saveImageToGallery(mContext, desFile);
                    }
                    else {
                        MixUtils.toastShort(mContext, "图片保存失败");
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
                        MixUtils.toastLong(mContext, "图片成功保存至: " + desFile.getAbsolutePath());
                    }

                    mSaveImageDialog.dismiss();
                }
            });
        }
    }
}
