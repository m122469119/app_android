package com.dym.film.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.utils.LogUtils;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class LoginManager implements Handler.Callback
{
    private Handler handler;
    private static final int MSG_AUTH_CANCEL = 1;
    private static final int MSG_AUTH_ERROR= 2;
    private static final int MSG_AUTH_COMPLETE = 3;
    private AsyncHttpHelper.ResultCallback<String[]> callback;
    private  Context mContext;
    public LoginManager(Context pContext)
    {
        mContext=pContext;
        handler = new Handler(Looper.getMainLooper(), this);
    }

    /** @param platformName 平台名称
     *
     */
    public void getAuthorInfo(String platformName, AsyncHttpHelper.ResultCallback<String[]> callback) {
        this.callback=callback;
        if (platformName == null) {
            callback.onFailure("-1","平台名称不能为空");
            return;
        }
        Platform plat = ShareSDK.getPlatform(platformName);
        if (plat == null) {
            callback.onFailure("-1","没有该平台");
            return;
        }
        if (plat.isAuthValid()) {
            plat.removeAccount(true);//删除本地授权信息，每次点击都需要重新授权。如果不删除本地信息，先从本地读取用户信息，不会调用第三方授权。
        }
        //使用SSO授权，通过客户单授权
        plat.SSOSetting(false);
        plat.setPlatformActionListener(new PlatformActionListener() {//这是在子线程中调用的
            public void onComplete(Platform plat, int action,HashMap<String, Object> res) {
                PlatformDb platDB = plat.getDb();//获取数平台数据DB
                //通过DB获取各种数据
                platDB.getToken();
                platDB.getUserGender();
                platDB.getUserIcon();
                platDB.getUserId();
                platDB.getUserName();
                if (action == Platform.ACTION_USER_INFOR) {//showUser对应的action,自己调用授权authorize的action是ACTION_AUTHORIZING
                    Message msg = new Message();
                    msg.what = MSG_AUTH_COMPLETE;
                    msg.arg2 = action;
                    // msg.obj =  new Object[] {plat.getName(), res};//res里面也可以获取用户信息
                    msg.obj =  new String[] {plat.getName(),platDB.getToken(), platDB.getUserId()};
                    handler.sendMessage(msg);
                }
            }

            public void onError(Platform plat, int action, Throwable t) {
                if (action == Platform.ACTION_USER_INFOR) {
                    Message msg = new Message();
                    msg.what = MSG_AUTH_ERROR;
                    msg.arg2 = action;
                    msg.obj = new String[] {plat.getName(),t.getMessage()};
                    handler.sendMessage(msg);
                }
                t.printStackTrace();
            }

            public void onCancel(Platform plat, int action) {
                if (action == Platform.ACTION_USER_INFOR) {
                    Message msg = new Message();
                    msg.what = MSG_AUTH_CANCEL;
                    msg.arg2 = action;
                    msg.obj = plat;
                    handler.sendMessage(msg);
                }
            }
        });
        plat.showUser(null);
    }

    /**处理操作结果*/
    public boolean handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_AUTH_CANCEL: {
                // 取消
//              Toast.makeText(mContext, " 取消", Toast.LENGTH_SHORT).show();
                callback.onFailure("-1","");
            } break;
            case MSG_AUTH_ERROR: {
                // 失败
                String[] objs = (String[])msg.obj;
                String platName = (String)objs[0];
                String text = (String)objs[1];
                String tips=null;
                if (QQ.NAME.endsWith(platName)){
                    tips="未安装QQ或者版本太低";
                }else if (Wechat.NAME.endsWith(platName)){
                    tips="未安装微信或者版本太低";
                }else if (SinaWeibo.NAME.endsWith(platName)){
                    tips="未安装微博或者版本太低";
                }
                if (text==null){
                    Toast.makeText(mContext, tips, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(mContext, " 登录失败" + text, Toast.LENGTH_SHORT).show();
                }
                LogUtils.i("123", "text");
                callback.onFailure("-1","");
            } break;
            case MSG_AUTH_COMPLETE: {
                // 成功
                String[] objs = (String[])msg.obj;
                String platName = objs[0];
                String token = objs[1];
                String id = objs[2];
                // @SuppressWarnings("unchecked") HashMap<String, String> res = (HashMap<String, Object>) objs[1];
                // LogUtils.i("123",res.toString());
                callback.onSuccess(objs);
            } break;
        }
        return false;
    }

}
