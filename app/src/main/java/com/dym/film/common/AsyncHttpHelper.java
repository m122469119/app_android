package com.dym.film.common;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.dym.film.activity.mine.LoginActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.http.OkHttpUtils;
import com.dym.film.common.http.RequestParams;
import com.dym.film.common.http.callback.StringCallback;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.lang.reflect.Type;

import okhttp3.Call;

public class AsyncHttpHelper
{

    public final static Gson GSON = new GsonBuilder().setDateFormat("yyyy.MM.dd HH:mm:ss").create();

    protected AsyncHttpHelper()
    {
    }
    /**
     * Returns singleton class instance
     */
//    public static AsyncHttpClient getInstance()
//    {
//        if (instance == null) {
//            synchronized (AsyncHttpHelper2.class) {
//                if (instance == null) {
//
//                    instance = new AsyncHttpClient();
//                    instance.setEnableRedirects(true);
//                    instance.setTimeout(5000);
//                    instance.setMaxRetriesAndTimeout(3, 1500);
//                    //instance.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
//                    // instance.addHeader("Content-Type","application/json;charset=UTF-8");//全局配置，会覆盖具体的请求设置
//                }
//            }
//        }
//        return instance;
//    }

    private static void handleResult(Context mContext, String response, Type type, AsyncHttpHelper.ResultCallback callback)
    {
        LogUtils.i("123", response.toString());
        BaseRespInfo baseRespInfo = GSON.fromJson(response, type);
        if (baseRespInfo.code == 0) {
            if (callback != null) {
                callback.onSuccess(baseRespInfo);
            }
        }
        else if (baseRespInfo.code == 23) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            UserInfo.clearUserInfo(mContext);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            if (callback != null) {
                callback.onFailure(baseRespInfo.code + "", baseRespInfo.message);
            }
        }
        else {
            Toast.makeText(mContext, baseRespInfo.message, Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onFailure(baseRespInfo.code + "", baseRespInfo.message);
            }

        }
    }

    public static void getRequest(final Context mContext, String url, final Type type, final AsyncHttpHelper.ResultCallback callback)
    {

        MatStatsUtil.interfaceTest(mContext, url);
        OkHttpUtils.getInstance().get(mContext, url, new StringCallback()
        {
            @Override
            public void onFailure(Call call, Exception e)
            {
                Toast.makeText(mContext, "请检查网络", Toast.LENGTH_SHORT).show();
                callback.onFailure("-1", "请检查网络");
            }
            @Override
            public void onSuccess(String response)
            {
                handleResult(mContext, response, type, callback);
            }
        });
    }

    public static void postRequest(final Context mContext, String url, JSONObject jObject, final Type type, final AsyncHttpHelper.ResultCallback callback)
    {
        MatStatsUtil.interfaceTest(mContext, url);
        OkHttpUtils.getInstance().post(mContext, url, RequestParams.createStringBody(jObject.toString(),null), new StringCallback()
        {
            @Override
            public void onFailure(Call call, Exception e)
            {
                Toast.makeText(mContext, "请检查网络", Toast.LENGTH_SHORT).show();
                callback.onFailure("-1", "请检查网络");
            }

            @Override
            public void onSuccess(String response)
            {
                handleResult(mContext, response, type, callback);
            }
        });

    }
    public interface ResultCallback<T>
    {

        /**
         * 成功时调用
         *
         * @param data 返回的数据
         */
        public void onSuccess(T data);

        /**
         * 失败时调用
         *
         * @param errorEvent 错误码
         * @param message    错误信息
         */
        public void onFailure(String errorCode, String message);
    }


}
