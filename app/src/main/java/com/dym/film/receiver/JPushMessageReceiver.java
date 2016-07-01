package com.dym.film.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.sharedticket.SharedTicketDetailActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.UserMessage;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MixUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/13
 */

/**
 * 用来接收服务消息
 * 点击消息打开特定页面
 */
public class JPushMessageReceiver extends BroadcastReceiver
{
    public final static String TAG = "JPushMessageReceiver";

    public final static String ACTION_MESSAGE_RECEIVED = "com.dym.film.message.received";
    /**
     * 传过的消息的key
     */
    public final static String KEY_USER_MESSAGE = "user_message";

    /**
     * 消息的类型
     */
    // 热点详情
    public final static int TYPE_HOTPOT        = 0;

    // 活动详情
    public final static int TYPE_ACTIVITY      = 1;

    // 影评详情消息
    public final static int TYPE_FILM_REVIEW   = 2;

    // 影片详情消息
    public final static int TYPE_MOVIE         = 3;

    // 我的晒票动态
    public final static int TYPE_SHARED_TICKET = 5;


    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            /**
             * 注册极光推送
             * 把这个id发送到服务器
             */
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            LogUtils.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...

            if (TextUtils.isEmpty(regId)) {
                return;
            }
            UserInfo.jid = regId;

            final NetworkManager.ReqUpdateJID req = new NetworkManager.ReqUpdateJID();
            req.jid = regId;

            NetworkManager.getInstance().updateJid(req, new HttpRespCallback<NetworkManager.BaseRespModel>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    final HttpRespCallback<NetworkManager.BaseRespModel> callback = this;
                    mHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            NetworkManager.getInstance().updateJid(req, callback);
                        }
                    }, 1000 * 60);
                }
            });
        }
        else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            //Logd(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);
        }
        else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            /**
             * 接收到普通Notification
             */
            processNotificationReceived(context, bundle);
        }
        else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            /**
             * 打开通知
             */
            processNotificationOpen(context, bundle);
        }
        else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            //Logd(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        }
        else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            //Logw(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
        }
        else {
            //Logd(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    /**
     * 处理消息打开
     * @param context
     * @param bundle
     */
    public static void processNotificationOpen(Context context, Bundle bundle)
    {
        final UserMessage message = getMessageFromNotification(bundle);
        if (message == null) {
            MixUtils.toastShort(context, "无效的消息");
            return;
        }

        // 标记为已读
        NetworkManager.ReqReadMessage req = new NetworkManager.ReqReadMessage();
        req.msgID = message.getMsgID();
        NetworkManager.getInstance().readedMessage(req, new HttpRespCallback<NetworkManager.RespReadMessage>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                message.setReaded(1);
            }
        });

        processNotificationOpen(context, message);
    }

    public static void processNotificationOpen(final Context context, final UserMessage message)
    {
        if (message == null) {
            MixUtils.toastShort(context, "无效的消息");
            return;
        }

//        Intent intent = new Intent();

        // 清楚消息通知
        JPushInterface.clearNotificationById(context, message.getNotifyID());

        switch (message.getCategory()) {
            case TYPE_ACTIVITY: {
                // 打开活动页面
                Intent intent = new Intent(context, HtmlActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(HtmlActivity.KEY_HTML_URL, message.getUrl());
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 2);
                context.startActivity(intent);
            }
                break;

            case TYPE_HOTPOT: {
                // 打开热点页面
                Intent intent = new Intent(context, HtmlActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(HtmlActivity.KEY_HTML_URL, message.getUrl());
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 2);
                context.startActivity(intent);
            }
                break;

            case TYPE_FILM_REVIEW: {
                // 打开影评页面
                Intent intent = new Intent(context, FilmReviewDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, message.getCinecismID());
                context.startActivity(intent);
            }
                break;

            case TYPE_MOVIE: {
                // 启动影片详情页面
                Intent intent = new Intent(context, FilmDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, String.valueOf(message.getFilmID()));
                context.startActivity(intent);
            }
                break;

            case TYPE_SHARED_TICKET: {
                // 打开晒票
                Intent intent = new Intent(context, SharedTicketDetailActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(SharedTicketDetailActivity.KEY_ID, message.getStubID());
                context.startActivity(intent);
            }
                break;
        }
    }

    /**
     * 处理消息接收
     * @param context
     * @param bundle
     */
    public static void processNotificationReceived(Context context, Bundle bundle)
    {
        UserMessage message = getMessageFromNotification(bundle);
        if (message != null && message.getMsgID() > 0) {
            Intent intent = new Intent(ACTION_MESSAGE_RECEIVED);
            intent.putExtra(KEY_USER_MESSAGE, message);
            context.sendBroadcast(intent);
        }
        LogUtils.e(TAG, "MSG: " + printBundle(bundle));
    }

    /**
     * 过滤非法的消息
     * @param bundle
     * @return
     */
    public synchronized static UserMessage getMessageFromNotification(Bundle bundle)
    {
        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
        if (TextUtils.isEmpty(extras)) {
            return null;
        }

        NetworkManager.UserMessageModel model = null;

        try {
            model = NetworkManager.GSON.fromJson(extras, NetworkManager.UserMessageModel.class);
        }
        catch (Exception e) {
            e.printStackTrace();
            model = null;
        }

        if (model == null) {
            return null;
        }

        UserMessage message = model.toUserMessage();

        int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
        message.setNotifyID(notificationId);

        return message;
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle)
    {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }
            else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            }
            else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
                    //Logi(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next().toString();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]");
                    }
                }
                catch (JSONException e) {
                    //Loge(TAG, "Get message extra JSON error!");
                }

            }
            else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    //send msg to MainActivity
    private void processCustomMessage(Context context, Bundle bundle)
    {
        //Loge(TAG, "process custom message");
    }
}
