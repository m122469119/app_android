package com.dym.film.service;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.dym.film.R;
import com.dym.film.application.ConfigInfo;
import com.dym.film.utils.FileUtils;

public class DownloadService extends Service
{
    private static final int NOTIFY_ID = 0;
    private NotificationManager mNotificationManager;
    private RemoteViews contentView;
    private Notification mNotification;
    private OnProgressListner onProgressListner;

    private DownloadBinder binder;
    private Context mContext = this;

    public static String title = "";
    public static String apkurl = "http://shouji.360tpcdn.com/150210/8c50f39088c54fd4ec43dd9738b1ca48/com.baidu.netdisk_385.apk";
    public static String foldername = ConfigInfo.zipFolderName;
    public static String filename = "tempfile";

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        createNotification();// 首次创建
        //startDownload();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        binder = new DownloadBinder();
        return binder;
    }


    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent)
    {
        // TODO Auto-generated method stub
        super.onRebind(intent);
    }

    public interface OnProgressListner
    {

        public void OnProgress(String progress);

        public void OnFinish();

        public void onFailure(String arg1);
    }

    public void setOnProgressListner(OnProgressListner onProgressListner)
    {

        this.onProgressListner = onProgressListner;
    }

    public class DownloadBinder extends Binder
    {

        public DownloadService getService()
        {
            // TODO Auto-generated method stub
            return DownloadService.this;
        }

    }

    /**
     * ֪
     */
    private void createNotification()
    {
        int icon = R.drawable.ic_launcher;
        CharSequence tickerText = "开始下载";
        long when = System.currentTimeMillis();
        mNotification = new Notification(icon, tickerText, when);
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;
        /***
         * 在这里我们用自定的view来显示Notification
         */
//		contentView = new RemoteViews(getPackageName(),
//				R.layout.lay_download_notification);
//		contentView.setTextViewText(R.id.tvTitle, title+"正在下载");
//		contentView.setTextViewText(R.id.tvProgress, "0%");
//		contentView.setProgressBar(R.id.progressbar, 100, 0, false);
//		mNotification.contentView = contentView;

        // Intent updateIntent = new Intent(this,
        // NotificationUpdateActivity.class);
        // updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        // updateIntent,
        // PendingIntent.FLAG_UPDATE_CURRENT);
        // mNotification.contentIntent = contentIntent;
        mNotificationManager.notify(NOTIFY_ID, mNotification);
    }

    //

    /**
     * ����apk
     *
     * @param url
     */

    private void startDownload()
    {

    }

    /**
     * ��װapk
     *
     * @param url
     */
    private void installApk()
    {
        File apkfile = FileUtils.getFile(foldername, filename);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

}
