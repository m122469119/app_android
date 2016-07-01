package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/13
 */

import android.content.Context;
import android.text.TextUtils;

import com.dym.film.application.UserInfo;
import com.dym.film.utils.LogUtils;

import cn.jpush.android.api.JPushInterface;

/**
 * 极光推送的管理类
 */
public class JPushMessageManager
{
    public final static String TAG = "JPushMessageManager";

    private final static JPushMessageManager mInstance = new JPushMessageManager();
    public static JPushMessageManager getInstance()
    {
        return mInstance;
    }

    private DatabaseManager mDBManager = DatabaseManager.getInstance();

    /**
     * 初始化 JPushInterface
     * @param context
     */
    public void initializeJPush(Context context)
    {
        JPushInterface.setDebugMode(true);
        JPushInterface.init(context);

//        String jid = JPushInterface.getRegistrationID(context);
//        if (TextUtils.isEmpty(jid)) {
//            return;
//        }
//
        LogUtils.e(TAG, "JPush: Udid: " + JPushInterface.getUdid(context));
        LogUtils.e(TAG, "JPush: RegId: " + JPushInterface.getRegistrationID(context));
    }

}
