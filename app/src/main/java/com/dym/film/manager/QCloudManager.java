package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/24
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.dym.film.application.ConfigInfo;
import com.tencent.upload.Const;
import com.tencent.upload.UploadManager;

/**
 * 万象优图的上传，下载管理
 */
public class QCloudManager
{
    public final static String TAG = "QCloudManager";

    private final static QCloudManager mInstance = new QCloudManager();
    public static QCloudManager getInstance()
    {
        return mInstance;
    }


    public final static String URL_IMAGE_BASE = "http://web.image.myqcloud.com/photos";
    public final static String URL_GET_IMAGE_DETAIL = URL_IMAGE_BASE + "/v2/" +
            ConfigInfo.QCLOUD_APPID + "/" + ConfigInfo.QCLOUD_BUCKET + "/" + ConfigInfo.QCLOUD_SECRET_ID;

    public static UploadManager getPhotoUploadManager(@NonNull Context context)
    {
        return new UploadManager(context, ConfigInfo.QCLOUD_APPID, Const.FileType.Photo, "qcloudphoto");
    }

    public static String getImageFieldId(String imageUrl)
    {
        if (TextUtils.isEmpty(imageUrl)) {
            return "";
        }

        return imageUrl.replaceAll("^http://.*/", "");
    }

    public static String getImageDetailUrl(String imageUrl)
    {
        String fieldId = getImageFieldId(imageUrl);

        return URL_GET_IMAGE_DETAIL + "/" + fieldId;
    }

    public static String urlImage2(String url, int width)
    {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url + "?imageView2/2/w/" + width;
    }

    public static String urlImage2(String url, int width, int quality)
    {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url + "?imageView2/2/w/" + width + "/q/" + quality;
    }

    public static String urlImage1(String url, int width, int height, int quality)
    {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url + "?imageView2/1/w/" + width + "/h/" + height + "/q/" + quality;
    }

    public static String urlImage1(String url, int width, int height)
    {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return url + "?imageView2/1/w/" + width + "/h/" + height;
    }

//    public static String
}
