package com.dym.film.common;

import android.os.Message;

import com.dym.film.application.UserInfo;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.LogUtils;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/17
 */

/**
 * 泛型化OkHttp Callback
 */
public abstract class HttpRespCallback
        <RespModel extends NetworkManager.BaseRespModel> extends BaseThread implements Callback
{
    public final static String TAG = "HttpRespCallback";

    public final static int WHAT_HTTP_FAILED = 0x11;

    public final static int WHAT_HTTP_SUCCESS = 0x12;

    /**
     * 自定义需求，标记一个
     */
    public int WHAT_CUSTOM_PURPOSE = 0;

    public HttpRespCallback()
    {
        WHAT_CUSTOM_PURPOSE = 0;
    }

    public HttpRespCallback(int purpose)
    {
        WHAT_CUSTOM_PURPOSE = purpose;
    }

    /**
     * 为了给Gson反射
     */
    private Class<RespModel> mRespModelClass = null;

    public void setRespModelClass(Class<RespModel> cls)
    {
        mRespModelClass = cls;
    }


    public abstract void onRespFailure(int code, String msg);

    public void onRespCode101(int code, ArrayList<String> msg) {   }

    public void onRespSuccess(RespModel model, String body)
    {
        sendMessage(WHAT_HTTP_SUCCESS, model);
    }

    @Override
    public void onFailure(Call call, IOException e)
    {
        sendMessage(WHAT_HTTP_FAILED, new HttpException(-1, e.getMessage()));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException
    {
        if (response.isSuccessful()) {
            String body = response.body().string();
            LogUtils.e(TAG, "Body: " + body);

            if (mRespModelClass == null) {
                onRespSuccess(null, body);
            }
            else {
                try {
                    RespModel model = NetworkManager.GSON.fromJson(body, mRespModelClass);
                    if (model != null) {
                        if (model.code != 0) {
                            sendMessage(WHAT_HTTP_FAILED, new HttpException(model.code, model.message,model.dates));
                        }
                        else {
                            onRespSuccess(model, null);
                        }
                    }
                    else {
                        onRespFailure(-1, "Json to Model is Null");
                    }
                }
                catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    sendMessage(WHAT_HTTP_FAILED, new HttpException(-2, "Parse The Json String Error！Pls Check it!"));
                }
            }
        }
        else {
            sendMessage(WHAT_HTTP_FAILED, new HttpException (response.code(), response.message()));
        }
    }


    protected void runOnMainThread(Message msg)
    {
        //
    }

    @Override
    protected final void handleMessage(Message msg)
    {
        if (msg.what == WHAT_HTTP_FAILED) {

            HttpException he = (HttpException) msg.obj;
            LogUtils.e(TAG, "Exception: " + he);
            switch (he.mCode) {
                case 23: // Token 无效, 需要重新登录
                    UserInfo.isLogin = false;
                    break;
                case 101: // 比价拍片日期
                    onRespCode101(he.mCode,he.dates);
                    break;
            }

            onRespFailure(he.mCode, he.mMessage);
        }
        else {
            runOnMainThread(msg);
        }
    }

    public static class HttpException
    {
        public int mCode = 0;
        public String mMessage = "";
        public ArrayList<String> dates = null;

        public HttpException(int code, String message,ArrayList<String> date)
        {
            mCode = code;
            mMessage = message;
            dates=date;
        }
        public HttpException(int code, String message)
        {
            mCode = code;
            mMessage = message;
        }

        @Override
        public String toString()
        {
            return "Code: " + mCode + " Msg: " + mMessage;
        }
    }
}
