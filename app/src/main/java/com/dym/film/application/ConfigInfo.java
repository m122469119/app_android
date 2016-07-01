package com.dym.film.application;


import android.os.Environment;

import java.io.File;

public class ConfigInfo
{
//  public final static String BASE_URL = "http://api-test.dymfilm.com";
    public final static String BASE_URL = "http://api.dymfilm.com";
    public static boolean isGetFromCache = true;
    public static String zipFolderName = "dymfilm";
    public static String unzipFolderName = "/dymfilm/unzip/";
    public static String jsonBaseFileName = "basedetail";
    public static String PREFERENCE_NAME_USER = "userInfo";
    public final static File APP_ROOT_DIR_FILE = new File(Environment.getExternalStorageDirectory(), "DaYinMu");

    public final static File APP_IMAGE_DIR_FILE = new File(APP_ROOT_DIR_FILE, "images");

    public final static File APP_CACHE_DIR_FILE = new File(APP_ROOT_DIR_FILE, "cache");

    public final static String QCLOUD_APPID = "10007714";

    public final static String QCLOUD_BUCKET = "weixin";

    public final static String QCLOUD_SECRET_ID = "AKIDBSDrKL7RU8YrRVnpj0jwPRLPALCRYgjV";

    public final static int MAX_UPLOAD_FILE_LIMIT = 1024*10; // 最大限制文件大小 10M

    public final static int SIZE_AVATAR = 130; // 头像的尺寸 150px
    public final static int SIZE_LITTLE_AVATAR = 100; // 小头像

    public final static int SIZE_REVIEW_WIDTH = 130; // 影评海报的宽

    /**
     * TODO: 注意这里没有考虑 宽度很大的图片
     */
    public final static int SIZE_STICKET_DETAIL_WIDTH = 600; // 晒票大图的宽

    public final static int SIZE_AR_STICKET_WIDTH = 150; // 周围晒票的宽
    public final static int SIZE_AR_STICKET_HEIGHT = 150;

    public final static int SIZE_FILM_POST_WIDTH = 120; // 电影海报的宽

    public static final int SIZE_BANNER_WIDTH = 500;
    public final static int SIZE_BANNER_HEIGHT = 200;


    public static String city="";
    public static String region="";

    public static String hmackey="DYM_qsc_@ef";
    public static int cinecismNum=10;//起评数
}
