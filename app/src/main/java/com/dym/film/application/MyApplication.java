package com.dym.film.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.dym.film.R;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.JPushMessageManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;

import cn.sharesdk.framework.ShareSDK;

public class MyApplication extends Application
{

    public Context context;

    public void onCreate()
    {
        super.onCreate();
        context = getApplicationContext();

        String processName = CommonManager.getCurProcessName(getApplicationContext());
        processName = processName == null ? "" : processName;

        LogUtils.e("Application", "onCreate....: " + processName + "  PackageName: " + getPackageName());
        /**
         * 这里如果不是主进程，不需要再次进行初始化
         */
        if (!processName.equals(getPackageName())) {
            return;
        }
        initImageLoader(context);
        // 初始化 CommonManager
        NetworkManager.init(context);
        // 初始化 CommonManager
        CommonManager.getInstance().initializeCommon(context);
        // 初始化 DaoMaster
        DatabaseManager.getInstance().initializeDaoMaster(context);
        // 初始化 BaiDu LBS
        BaiduLBSManager.getInstance().initializeBaiduLBS(context);
        // 初始化用户信息
        UserInfo.getUserInfo(context);
        // 初始化分享平台
        ShareSDK.initSDK(context);
        // 初始化 JPush
        JPushMessageManager.getInstance().initializeJPush(context);
    }

    public void initImageLoader(Context context)
    {
        // 自定义缓存文件的目录
        File cacheDir = ConfigInfo.APP_CACHE_DIR_FILE;
        // 如果DisplayImageOption没有传递给ImageLoader.displayImage(…)方法，那么从配置默认显示选项
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                 .showImageOnLoading(R.drawable.ic_default_loading_img) // 设置图片下载期间显示的图片
                 .showImageForEmptyUri(R.drawable.ic_default_loading_img) // 设置图片Uri为空或是错误的时候显示的图片
//                 .showImageOnFail(R.drawable.ic_default_loading_img) // 设置图片加载或解码过程中发生错误显示的图片
//                .resetViewBeforeLoading(false) // default 设置图片在加载前是否重置、复位
                        // .delayBeforeLoading(1000) // 下载前的延迟时间
//                .cacheInMemory(true) // default 设置下载的图片是否缓存在内存中
                .cacheOnDisk(true) // default 设置下载的图片是否缓存在SD卡中
                        // .preProcessor(...)
                        // .postProcessor(...)
                        // .extraForDownloader(...)
                        // .considerExifParams(false) // defaul是否考虑JPEG图像EXIF参数（旋转，翻转


                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
                        // 设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565) // default 设置图片的解码类型
                        // .decodingOptions(...) // 图片的解码设置
                        // .displayer(new SimpleBitmapDisplayer()) // default
                        // 还可以设置圆角图片new RoundedBitmapDisplayer(20)
                        // .handler(new Handler()) // default
                .build();

        // 以下是所有设置项，只需设置自己需要的，不设置会使用默认值
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                // .memoryCacheExtraOptions(480, 800) // default = device screen
                // dimensions 内存缓存文件的最大长宽
                // .diskCacheExtraOptions(480, 800, null) // 本地缓存的详细信息(缓存的最大长宽)，最好不要设置这个
                // .taskExecutor(...)
                // .taskExecutorForCachedImages(...)
                // .threadPoolSize(3) // default 线程池内加载的数量
                // .threadPriority(Thread.NORM_PRIORITY - 2) // default 设置当前线程的优先级
                // .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                // .denyCacheImageMultipleSizesInMemory()
             //    .memoryCache(new LruMemoryCache(2 * 1024 * 1024)) //可以通过自己的内存缓存实现
//                 .memoryCacheSize(2 * 1024 * 1024) // 内存缓存的最大值
//                .memoryCacheSizePercentage(10) // 20%最大内存
//                .diskCache(new UnlimitedDiskCache(cacheDir)) // default 可以自定义缓存路径
                 .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
//                 .diskCacheFileCount(100) // 可以缓存的文件数量
                // default为使用HASHCODE对UIL进行加密命名， 还可以用MD5(new Md5FileNameGenerator())加密
                // .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                // .imageDownloader(new BaseImageDownloader(context)) // default
                // .imageDecoder(new BaseImageDecoder()) // default
                .defaultDisplayImageOptions(options)// default
//                .writeDebugLogs() // 打印debug log
                .build(); // 开始构建

        //全局初始化此配置,通常使用默认config就可以了创建默认的ImageLoader配置参数
        //ImageLoaderConfiguration config = ImageLoaderConfiguration .createDefault(this);
        //必须初始化这个配置. 否则会出现错误
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
//      Toast.makeText(this, "内存较低", Toast.LENGTH_SHORT).show();
    }
}
