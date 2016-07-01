package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/16
 */

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.home.PreFilmDetailActivity;
import com.dym.film.activity.mine.LoginActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.receiver.JPushMessageReceiver;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.CenterRoundDisplayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static com.dym.film.application.ConfigInfo.APP_CACHE_DIR_FILE;
import static com.dym.film.application.ConfigInfo.APP_IMAGE_DIR_FILE;
import static com.dym.film.application.ConfigInfo.APP_ROOT_DIR_FILE;

/**
 * 通用的管理器，管理一些咋项
 * 传递数据
 */
public class CommonManager
{
    public final static String TAG = "CommonManager";

    private final static CommonManager mInstance = new CommonManager();

    public static CommonManager getInstance()
    {
        return mInstance;
    }

    public static int DEFAULT_REFRESH_DISTANCE = 200;

    /**
     * 瀑布流最高高度和最低高度
     */
    public static int MIN_STAGGER_IMAGE_HEIGHT = 150;
    public static int MAX_STAGGER_IMAGE_HEIGHT = 400;

    /**
     * 默认在瀑布流中获取图片的图片宽度
     */
    public final static int DEF_STAGGER_IMAGE_WIDTH = 300;

    /**
     * 屏幕分辨率
     */
    public static float DISPLAY_DENSITY = 1;
    public static DisplayMetrics DISPLAY_METRICS = null;

    /**
     * 字体
     */
    public static Typeface mChineseTypeface = null;
    public final static String FONT_CHINESE = "fonts/chinese.otf";

    public static Typeface mNumberTypeface = null;
    public final static String FONT_NUMBER = "fonts/DINCond_Light.otf";

    public void initializeCommon(Context context)
    {
        DISPLAY_METRICS = context.getResources().getDisplayMetrics();
        DISPLAY_DENSITY = DISPLAY_METRICS.density;

        DEFAULT_REFRESH_DISTANCE = dpToPx(250);

        MIN_STAGGER_IMAGE_HEIGHT = dpToPx(100);
        MAX_STAGGER_IMAGE_HEIGHT = dpToPx(300);

        // 初始化应用的文件夹
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!APP_ROOT_DIR_FILE.exists()) {
                APP_ROOT_DIR_FILE.mkdirs();
            }

            if (!APP_CACHE_DIR_FILE.exists()) {
                APP_CACHE_DIR_FILE.mkdirs();
            }

            if (!APP_IMAGE_DIR_FILE.exists()) {
                APP_IMAGE_DIR_FILE.mkdirs();
            }
        }

//        mChineseTypeface = Typeface.createFromAsset(context.getAssets(), FONT_CHINESE);
        mNumberTypeface = Typeface.createFromAsset(context.getAssets(), FONT_NUMBER);
    }

    public static int dpToPx(float dp)
    {
        return (int) (dp * DISPLAY_DENSITY + 0.5f);
    }

    public static int pxToDp(float px)
    {
        return (int) (px / DISPLAY_DENSITY + 0.5f);
    }

    /**
     * 用于两个Activity或者情况下进行数据传递
     */
    private final static HashMap<String, Object> mBusData = new HashMap<>();

    public static void putData(String key, Object data)
    {
        if (!TextUtils.isEmpty(key)) {
            mBusData.put(key, data);
        }
    }

    public static Object getData(String key)
    {
        return getData(key, true);
    }


    public static Object getData(String key, boolean clear)
    {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        Object object = mBusData.get(key);

        if (clear) {
            mBusData.remove(key);
        }

        return object;
    }

    public static void removeData(String key)
    {
        if (!TextUtils.isEmpty(key)) {
            mBusData.remove(key);
        }
    }

    /**
     * 保存图片到相册
     */
    public static void saveImageToGallery(Context context, File file)
    {
        if (context == null || file == null || !file.exists()) {
            return;
        }

        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), null);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Uri uri = Uri.fromFile(file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        MediaScannerConnection.scanFile(context, new String[]{ file.getAbsolutePath() }, null, null);
    }

    /**
     * Try to return the absolute file path from the given Uri
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(Context context, final Uri uri)
    {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        }
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        }
        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 设置时间的通用方法，如果在几分钟或者几小时之内，显示为 XX 分钟前, XX小时前,
     * 否则显示为日期
     */
    public static void setTime(TextView timeView, long time)
    {
        long curTime = System.currentTimeMillis();
        long interTime = (curTime - time) / 1000;

        boolean flag = false;
        if (interTime >= 0) {
            if (interTime < 60) {
                timeView.setText("1 分钟内");
                flag = true;
            }
            else if (interTime < 60 * 60) {
                timeView.setText(String.valueOf(interTime / 60)+" 分钟前");
                flag = true;
            }
            else if (interTime < 60 * 60 * 12) {
                timeView.setText(String.valueOf(interTime / 60 / 60)+" 小时前");
                flag = true;
            }
        }

        if (!flag) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(time));

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);

            calendar.setTime(new Date(System.currentTimeMillis()));
            if (year != calendar.get(Calendar.YEAR)) {
                timeView.setText(String.format("%d.%02d.%02d", year, month, day));
            }
            else {
                timeView.setText(String.format("%02d.%02d", month, day));
            }
        }
    }

    public static void setTime2(TextView timeView, TextView unitView, long time)
    {
        long curTime = System.currentTimeMillis();
        long interTime = (curTime - time) / 1000;

        boolean flag = false;
        if (interTime >= 0) {
            if (interTime < 60) {
                timeView.setText("1");
                unitView.setText("分钟内");
                flag = true;
            }
            else if (interTime < 60 * 60) {
                timeView.setText(String.valueOf(interTime / 60));
                unitView.setText("分钟前");
                flag = true;
            }
            else if (interTime < 60 * 60 * 12) {
                timeView.setText(String.valueOf(interTime / 60 / 60));
                unitView.setText("小时前");
                flag = true;
            }
        }

        if (!flag) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(time));

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);

            timeView.setText(String.format("%02d.%02d", month, day));
            unitView.setText(String.valueOf(year));
        }
    }

    public static void setTime3(TextView timeView, String date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss");
        try {
            long time=sdf.parse(date).getTime();
            setTime3(timeView,time);
        }
        catch (ParseException e) {
            e.printStackTrace();
            timeView.setText(date);
        }
    }
    public static void setTime3(TextView timeView, long time)
    {
        long curTime = System.currentTimeMillis();
        long interTime = (curTime - time) / 1000;
        String unit = "";
        String date = "";
        boolean flag = false;
        if (interTime >= 0) {
            if (interTime < 60) {
                date = "1";
                unit = "分钟内";
                flag = true;
            }
            else if (interTime < 60 * 60) {
                date = (String.valueOf(interTime / 60));
                unit = ("分钟前");
                flag = true;
            }
            else if (interTime < 60 * 60 * 12) {
                date = (String.valueOf(interTime / 60 / 60));
                unit = ("小时前");
                flag = true;
            }
        }

        if (!flag) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(time));

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);

            unit = (String.format(".%02d.%02d %02d:%02d", month, day,hour,min));
            date = (String.valueOf(year));

        }

        timeView.setText(date + unit);



    }

    public static NetworkManager.MYDATE getToady(int i)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.add(Calendar.DAY_OF_MONTH, i);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int week = calendar.get(Calendar.DAY_OF_WEEK);

        NetworkManager.MYDATE mydate = new NetworkManager.MYDATE();
        mydate.day = month + "." + day;
        mydate.date = year + "-" + month + "-" + day;
        mydate.time = hour + ":" + min;
        switch (week) {
            case 2:
                mydate.week = "周一";
                break;
            case 3:
                mydate.week = "周二";
                break;
            case 4:
                mydate.week = "周三";
                break;
            case 5:
                mydate.week = "周四";
                break;
            case 6:
                mydate.week = "周五";
                break;
            case 7:
                mydate.week = "周六";
                break;
            case 1:
                mydate.week = "周日";
        }

        return mydate;

    }
    public static NetworkManager.MYDATE getToadyByString(String i)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date daystart = null;    //start_date是类似"2013-02-02"的字符串
        try {
            daystart = df.parse(i);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(daystart);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int week = calendar.get(Calendar.DAY_OF_WEEK);

        NetworkManager.MYDATE mydate = new NetworkManager.MYDATE();
        mydate.day = month + "." + day;
        mydate.date = year + "-" + month + "-" + day;
        mydate.time = hour + ":" + min;
        switch (week) {
            case 2:
                mydate.week = "周一";
                break;
            case 3:
                mydate.week = "周二";
                break;
            case 4:
                mydate.week = "周三";
                break;
            case 5:
                mydate.week = "周四";
                break;
            case 6:
                mydate.week = "周五";
                break;
            case 7:
                mydate.week = "周六";
                break;
            case 1:
                mydate.week = "周日";
        }

        return mydate;

    }

//    public final static DisplayImageOptions.Builder mDisplayBuilder = new DisplayImageOptions.Builder();
//
//    static {
//        mDisplayBuilder.resetViewBeforeLoading(true) // default 设置图片在加载前是否重置、复位
//                .cacheOnDisk(true) // default 设置下载的图片是否缓存在SD卡中
//                .cacheInMemory(false).imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
//                .bitmapConfig(Bitmap.Config.RGB_565) // default 设置图片的解码类型
//                .build();
//    }

    public static void displayRoundAvatar(String url, ImageView imageView)
    {
        url = QCloudManager.urlImage2(url, 164);
        displayRoundImage(url, dpToPx(2.5f), imageView, R.drawable.ic_default_loading_img);
    }

    public static void displayAvatar(String url, ImageView imageView)
    {
        url = QCloudManager.urlImage2(url, ConfigInfo.SIZE_AVATAR);
        displayImage(url, imageView, R.drawable.ic_default_photo);
    }

    public static void displayFilmPost(String url, ImageView imageView)
    {
        url = QCloudManager.urlImage2(url, ConfigInfo.SIZE_FILM_POST_WIDTH*2);
        displayImage(url, imageView, R.drawable.ic_default_loading_img);
    }

    public static void displayReviewLogo(String url, ImageView imageView)
    {
        url = QCloudManager.urlImage2(url, ConfigInfo.SIZE_REVIEW_WIDTH);
        displayImage(url, imageView, R.drawable.ic_default_loading_img);
    }

    public static void displayRoundImage(String url, int radius, final ImageView imageView, int failResId)
    {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheOnDisk(true)
                .cacheInMemory(false)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(failResId)
                .showImageOnFail(failResId)
                .displayer(new CenterRoundDisplayer(radius));

        ImageLoader.getInstance().displayImage(url, imageView, builder.build());
    }

    public static void displayImage(String url, final ImageView imageView, Drawable drawable)
    {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(drawable)
                .showImageOnFail(drawable);

        ImageLoader.getInstance().displayImage(url, imageView, builder.build());
    }

    public static void displayImage(String url, final ImageView imageView, int failResId)
    {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(failResId)
                .showImageOnLoading(failResId)
                .showImageOnFail(failResId);

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ImageLoader.getInstance().displayImage(url, imageView, builder.build(), new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                //
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason)
            {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                if (loadedImage != null) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view)
            {

            }
        });
    }

    public static void displayImage2(String url, final ImageView imageView, int failResId)
    {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(failResId)
                .showImageOnLoading(failResId)
                .showImageOnFail(failResId);

        ImageLoader.getInstance().displayImage(url, imageView, builder.build());
    }

    public static void setDistance(TextView lengthText, double length)
    {
        if (length < 100) {
            lengthText.setText(String.valueOf("< 100m"));
        }
        else if (length < 500) {
            lengthText.setText(String.valueOf("< 500m"));
        }
        else if (length < 1000) {
            lengthText.setText(String.valueOf("< 1km"));
        }
        else if (length < 5000) {
            lengthText.setText(String.valueOf("< 5km"));
        }
        else if (length < 10000) {
            lengthText.setText(String.valueOf("< 10km"));
        }
        else {
            lengthText.setText(String.valueOf("> 10km"));
        }
    }

    /**
     * 启动用户登录界面
     */
    public static void startLoginActivity(Context context)
    {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * SwipeRefreshLayout的主动刷新
     */
    public static void setRefreshingState(final SwipeRefreshLayout layout, boolean refreshing)
    {
        if (refreshing) {
            layout.post(new Runnable()
            {
                @Override
                public void run()
                {
                    layout.setRefreshing(true);
                }
            });
        }
        else {
            // 延迟一秒
            layout.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    layout.setRefreshing(false);
                }
            }, 800);
        }
    }


    /**
     * 隐藏输入法
     */
    public static void dismissSoftInputMethod(Context context, IBinder windowToken)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public static void showSoftInputMethod(Context context)
    {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //////////////////////////////////////////////******////////////////////////////////////////

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getImageDegree(String path)
    {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree)
    {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        catch (OutOfMemoryError e) {
            returnBm = null;
        }

        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    /**
     * 获取当前进程的名字
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context)
    {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 解析和处理banner的跳转动作， 传入 banner的 url
     * @param url
     * @return
     */
    private final static String KEY_TYPE = "type";

    private final static String KEY_CINECISM_ID = "cinecismID";

    private final static String KEY_FILM_ID = "filmID";
    private final static String KEY_STATUS = "status";

    public static void processBannerClick(Context context, String url)
    {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        LogUtils.e(TAG, "Banner URL: " + url);
        /**
         * H5 页面
         */
        if (url.startsWith("http://")) {
            Intent intent = new Intent(context, HtmlActivity.class);
            intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
            intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 2);
            context.startActivity(intent);
            return;
        }


        int type = -1;
        long cinecismID = 0;

        long filmID = 0;
        long status = 0;

        /**
         * 跳转内部页面
         */
        if (url.startsWith("dymfilm://")) {
            String params = url.replaceFirst("dymfilm://\\?", "");
            //Loge(TAG, "params: " + params);

            String [] paramArr = params.split("&");
            //Loge(TAG, "Length: " + paramArr.length);

            for (String param : paramArr) {
                //Loge(TAG, "Par: " + param);
                String [] arr = param.split("=", 2);

                //Loge(TAG, "Len: " + arr.length + " Arr[0]: " + arr[0]);
                if (arr.length == 2) {
                    //Loge(TAG, "Arr[1]: " + arr[1]);
                    switch (arr[0]) {
                        case KEY_TYPE:
                            type = parseInt(arr[1], -1);
                            break;

                        case KEY_CINECISM_ID:
                            cinecismID = parseLong(arr[1], 0);
                            break;

                        case KEY_FILM_ID:
                            filmID = parseLong(arr[1], 0);
                            break;

                        case KEY_STATUS:
                            status = parseInt(arr[1], 1);
                            break;
                    }
                }
            }
        }

        LogUtils.e(TAG, "Type: " + type + " CID: " + cinecismID + " FID: " + filmID);
        Intent intent = new Intent();
        switch (type) {
            case JPushMessageReceiver.TYPE_FILM_REVIEW:
                // 打开影评页面
                intent.setClass(context, FilmReviewDetailActivity.class);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, cinecismID);
                context.startActivity(intent);

                break;

            case JPushMessageReceiver.TYPE_MOVIE:
                // 启动影片详情页面
                intent.setClass(context, FilmDetailActivity.class);
                if (status == 1) {
                    intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, String.valueOf(filmID));
                }
                else {
                    intent.putExtra(FilmDetailActivity.KEY_FILM_ID, String.valueOf(filmID));
                }
                context.startActivity(intent);
                break;
        }
    }

    public static int parseInt(String i, int def)
    {
        int res = def;
        try {
            res = Integer.valueOf(i);
        }
        catch (Exception e) {
            res = def;
        }

        return res;
    }

    public static long parseLong(String i, long def)
    {
        long res = def;
        try {
            res = Long.valueOf(i);
        }
        catch (Exception e) {
            res = def;
        }

        return res;
    }

    /**
     * 图片质量压缩
     * 压缩到 limit kb， 如果压缩失败，则可能不会 < limit KB
     */
    public static Bitmap compressBitmapImage(Bitmap image, int maxOption, int limit)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = maxOption;
        while (baos.toByteArray().length / 1024 > limit) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中

        return BitmapFactory.decodeStream(isBm, null, null);
    }

    public static Bitmap compressBitmapImage(Bitmap image, int limit)
    {
        return compressBitmapImage(image, 100, limit);
    }

    /**
     * 比例压缩
     *
     * @param srcPath
     * @return
     */
    public static Bitmap compressSrcImage(String srcPath, int limit)
    {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        }
        else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例

        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        return compressBitmapImage(bitmap, limit);//压缩好比例大小后再进行质量压缩
    }

    /**
     * 根据bitmap压缩
     */

    public static Bitmap compressImageByBitmap(Bitmap image)
    {
        return compressImageByBitmap(image, 400, 240);
    }

    public static Bitmap compressImageByBitmap(Bitmap image, float width, float height)
    {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int options = 100;
            //判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            while (baos.toByteArray().length / 1024 > 1024) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 10;//每次都减少10
            }

            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            //开始读入图片，此时把options.inJustDecodeBounds 设回true了
            newOpts.inJustDecodeBounds = true;
            newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            int be = 1;//be=1表示不缩放
            if (w > h && w > height) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) (newOpts.outWidth / height);
            }
            else if (w < h && h > width) {//如果高度高的话根据宽度固定大小缩放
                be = (int) (newOpts.outHeight / width);
            }
            if (be <= 0) {
                be = 1;
            }
            newOpts.inSampleSize = be;//设置缩放比例

            newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//该模式是默认的,可不设

            //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
            isBm = new ByteArrayInputStream(baos.toByteArray());
            bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

            return bitmap;
//            return compressBitmapImage(bitmap, limit);//压缩好比例大小后再进行质量压缩
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap compressImageFromFile(String imgPath, float pixelW, float pixelH)
    {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容
        newOpts.inJustDecodeBounds = true;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 想要缩放的目标尺寸
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > pixelW) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / pixelW);
        }
        else if (w < h && h > pixelH) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / pixelH);
        }
        if (be <= 0) {
            be = 1;
        }
        newOpts.inSampleSize = be;//设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);
        // 压缩好比例大小后再进行质量压缩
//        return compress(bitmap, maxSize); // 这里再进行质量压缩的意义不大，反而耗资源，删除
        return bitmap;
    }

    public static Bitmap compressImageFromFile(String srcPath)
    {
        return compressImageFromFile(srcPath, 480, 640);
    }

    /**
     * 首先进行质量压缩
     *
     * @param srcBitmap
     * @return
     */
    public static Bitmap compressImageToUpload(Bitmap srcBitmap)
    {
        if (srcBitmap == null) {
            return null;
        }

        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();

        /**
         * 质量压缩
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while (baos.toByteArray().length / 1024 > 300 && options >= 50) {
            baos.reset();
            srcBitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }

        //Loge(TAG, "Length: " + baos.toByteArray().length + " Options: " + options);

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        /**
         * 按照 720 x 960来等比缩放
         */
        final int outWidth = 720;
        final int outHeight = 960;

        float srcRatio = srcHeight * 1.0f / srcWidth;
        if (Math.max(srcHeight, srcWidth) > 4096 && srcRatio > 5f || srcRatio < 1 / 5f) {
            //TODO 长图, 则按照原图的比例缩放
            return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, newOpts);
        }

        newOpts.inSampleSize = calculateSampleSize(srcWidth, srcHeight, outWidth, outHeight);//设置缩放比例

        return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, newOpts);
    }


    public static Bitmap compressImage(File file)
    {
        Bitmap bitmap = null;
        try {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            int i = 0;
            while (true) {
                if ((options.outWidth >> i <= 720) || (options.outHeight >> i <= 960)) {
                    in = new BufferedInputStream(new FileInputStream(file));
                    options.inSampleSize = (int) Math.pow(2.0D, i);
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    break;
                }
                i += 1;
            }
        }
        catch (Exception e) {
            bitmap = null;
        }

        return bitmap;
    }

    /**
     * 计算Option的inSampleSize属性
     */
    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight)
    {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;

        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        }
        else {
            return srcHeight / dstHeight;
        }
    }

    /**
     * 格式化金额
     *
     * @param s
     * @param len
     * @return
     */
    public static String formatMoney(String s, int len)
    {
        if (s == null || s.length() < 1) {
            return "";
        }
        NumberFormat formater = null;
        double num = Double.parseDouble(s);
        if (len == 0) {
            formater = new DecimalFormat("###,###");

        }
        else {
            StringBuffer buff = new StringBuffer();
            buff.append("###,###.");
            for (int i = 0; i < len; i++) {
                buff.append("#");
            }
            formater = new DecimalFormat(buff.toString());
        }
        String result = formater.format(num);
        if (result.indexOf(".") == -1) {
            result = result + ".0";
        }
        else {
            result = result;
        }
        return result;
    }
}
