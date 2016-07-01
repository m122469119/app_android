package com.dym.film.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.CustomDialog;

public class DownLoadSoftUpdate {

	/**
	 * 软件更新
	 */
	private Context mContext;
	private static final int TIMEOUT = 10 * 1000;// 超时
	private static final int DOWN_OK = 21;
	private static final int DOWN_ERROR = 20;
	private static final int NEW_UPDATE = 22;
	private static final int REF_PROGRESS = 33;
	private static final int NEW_UPDATED = 44;
	private static final int REF_SPEED = 55;
	private NotificationManager notificationManager;
	private Notification notification;
	private Intent updateIntent;
	private VersionInfo versionInfos;
	private PendingIntent pendingIntent;
	private int notification_id = 0;
	
	long a=0;

	private String path = "";

	public DownLoadSoftUpdate(Context context) {
		this.mContext = context;
		this.path = ConfigInfo.APP_ROOT_DIR_FILE.getAbsolutePath()+File.separator
				+ "dym.apk";
		File file=new File(ConfigInfo.APP_ROOT_DIR_FILE.getAbsolutePath());
		if(!file.exists()){
			file.mkdirs();
		}
	}

	public void CancelNotification() {
				NotificationManager notificationManager = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(notification_id);
	}

	public checkUpdateFlag updateFlag;
	protected boolean flagOk=false;

	public void setUpdateFlag(checkUpdateFlag updateFlag) {
		this.updateFlag = updateFlag;
	}

	public void checkVersionThread() {
		checkUpdate();
	}
	public void cancelDownLoad() {
		cancelFlag=false;
		CancelNotification();
	}

	private void downLoadThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long downloadSize = downloadUpdateFile(versionInfos.url,new File(path));
				Message message = new Message();
				if(!cancelFlag){
					return;
				}
				if (downloadSize > 0) {
					// 下载成功
					flagOk=true;
					message.what = DOWN_OK;
					handler.sendMessage(message);
				} else {
					message.what = DOWN_ERROR;
					handler.sendMessage(message);
				}
			}
		}).start();
	}

	public static class MyHandler extends Handler{
		private final WeakReference<DownLoadSoftUpdate> weakReference;

		public MyHandler(DownLoadSoftUpdate downLoadSoftUpdate){
			weakReference=new WeakReference<DownLoadSoftUpdate>(downLoadSoftUpdate);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			DownLoadSoftUpdate downLoadSoftUpdate=weakReference.get();

			if(downLoadSoftUpdate!=null){
				switch (msg.what) {
					case DOWN_OK:
						if(!downLoadSoftUpdate.cancelFlag){
							return;
						}
						// 下载完成，点击安装
						if (!FileUtils.isSDCardEnable()) {
							try {
								String[] args2 = { "chmod", "777", downLoadSoftUpdate.path };
								Runtime.getRuntime().exec(args2);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						Uri uri = Uri.fromFile(new File(downLoadSoftUpdate.path));
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(uri,
								"application/vnd.android.package-archive");
						downLoadSoftUpdate.pendingIntent = PendingIntent.getActivity(downLoadSoftUpdate.mContext, 0, intent,0);
						downLoadSoftUpdate.notification.tickerText = "公证电影下载完成，点击安装";
						downLoadSoftUpdate.notification.icon = R.drawable.ic_launcher;
						downLoadSoftUpdate.contentView = new RemoteViews(downLoadSoftUpdate.mContext.getPackageName(),R.layout.notification_item_finish);
						downLoadSoftUpdate.notification.contentView = downLoadSoftUpdate.contentView;
						downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationfinishTitle,
								"公证电影");
						downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationfinishPercent,
								"下载完成，点击安装");
						downLoadSoftUpdate.notification.contentView = downLoadSoftUpdate.contentView;
						downLoadSoftUpdate.notification.contentIntent = downLoadSoftUpdate.pendingIntent;
						downLoadSoftUpdate.notificationManager.notify(downLoadSoftUpdate.notification_id, downLoadSoftUpdate.notification);
						downLoadSoftUpdate.notificationManager.cancel(downLoadSoftUpdate.notification_id);
						downLoadSoftUpdate.mContext.startActivity(intent);
						break;
					case DOWN_ERROR:
						// notification.setLatestEventInfo(mContext, "看客影视", "下载失败",
						// pendingIntent);
						if(downLoadSoftUpdate.mContext!=null&&downLoadSoftUpdate.notification!=null){
							Intent it = new Intent(downLoadSoftUpdate.mContext, MainActivity.class);
							PreferencesUtils.putString(downLoadSoftUpdate.mContext,"version","");
							downLoadSoftUpdate.pendingIntent = PendingIntent.getActivity(downLoadSoftUpdate.mContext, 0, it, 0);
							downLoadSoftUpdate.contentView = new RemoteViews(downLoadSoftUpdate.mContext.getPackageName(),
									R.layout.notification_item_finish);
							downLoadSoftUpdate.notification.contentView = downLoadSoftUpdate.contentView;
							downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationfinishTitle,
									"公证电影");
							downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationfinishPercent,
									"下载失败，请重试");
							downLoadSoftUpdate.notification.contentView = downLoadSoftUpdate.contentView;
							downLoadSoftUpdate.notification.contentIntent = downLoadSoftUpdate.pendingIntent;
							downLoadSoftUpdate.notificationManager.notify(downLoadSoftUpdate.notification_id, downLoadSoftUpdate.notification);
						}
						break;
					case REF_PROGRESS:
						downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationPercent, msg.arg1
								+ "%");
						downLoadSoftUpdate.contentView.setProgressBar(R.id.notificationProgress, 100,msg.arg1, false);
						// show_view
						if(downLoadSoftUpdate.cancelFlag)
							downLoadSoftUpdate.notificationManager.notify(downLoadSoftUpdate.notification_id, downLoadSoftUpdate.notification);
						if(downLoadSoftUpdate.flagOk){
							downLoadSoftUpdate.notificationManager.cancel(downLoadSoftUpdate.notification_id);
						}
						if(!downLoadSoftUpdate.cancelFlag){
							downLoadSoftUpdate.cancelDownLoad();
						}
						break;
					case REF_SPEED:
						downLoadSoftUpdate.contentView.setTextViewText(R.id.notificationSpeed, msg.obj.toString()+"");
						if(downLoadSoftUpdate.cancelFlag)
							downLoadSoftUpdate.notificationManager.notify(downLoadSoftUpdate.notification_id, downLoadSoftUpdate.notification);
						if(downLoadSoftUpdate.flagOk){
							downLoadSoftUpdate.notificationManager.cancel(downLoadSoftUpdate.notification_id);
						}
						if(!downLoadSoftUpdate.cancelFlag){
							downLoadSoftUpdate.cancelDownLoad();
						}
						break;
					case NEW_UPDATE:
						if (downLoadSoftUpdate.versionInfos != null) {
							downLoadSoftUpdate.showDialog(downLoadSoftUpdate.versionInfos.content);
						}
						break;
					case NEW_UPDATED:
						Toast.makeText(downLoadSoftUpdate.mContext,"已经是最新版",Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
				}
			}
		}
	}

	public final MyHandler handler=new MyHandler(this);

	private CustomDialog appUpdateDialog;
	public void showDialog(String msg) {
		// 发现新版本，提示用户更新
//		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
//		StringBuilder build = new StringBuilder();
//		alert.setTitle("发现新的版本")
//				.setMessage(msg)
//				.setPositiveButton("现在更新",
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog,
//									int which) {
//								createNotification();
//								downLoadThread();
//								cancelFlag=true;
//							}
//						})
//				.setNegativeButton("下次再说",
//						new DialogInterface.OnClickListener() {
//							public void onClick(DialogInterface dialog,
//									int which) {
//								dialog.dismiss();
//							}
//						});
//		alert.create().show();

		if (appUpdateDialog == null) {
			appUpdateDialog = new CustomDialog(mContext);
			View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_update_app, null);
			TextView tilte = (TextView) view.findViewById(R.id.update_app_title);
			Button buttonCancel = (Button) view.findViewById(R.id.btnCancelNickname);
			Button buttonUpdate = (Button) view.findViewById(R.id.btnUpdateNickname);
			TextView update_app_description = (TextView) view.findViewById(R.id.update_app_description);
			buttonCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					appUpdateDialog.dismiss();
				}
			});
			buttonUpdate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					createNotification();
					downLoadThread();
					cancelFlag=true;
					appUpdateDialog.dismiss();
				}
			});
			buttonUpdate.setText("现在更新");
			buttonCancel.setText("下次再说");
			tilte.setText("发现新的版本");
			update_app_description.setText(msg);
			appUpdateDialog.setContentView(view);
		}
		appUpdateDialog.show(Gravity.CENTER);
	}

	private RemoteViews contentView;

	private void createNotification() {
		notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = "公证电影更新";
		// notification.flags=Notification.FLAG_NO_CLEAR;
		contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notification_item);
		contentView.setTextViewText(R.id.notificationTitle, "正在下载");
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setTextViewText(R.id.notificationSpeed, "");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
		notification.contentView = contentView;
		updateIntent = new Intent(mContext, MainActivity.class);
		updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(mContext, 0, updateIntent, 0);
		notification.contentIntent = pendingIntent;
		notificationManager.notify(notification_id, notification);
	}

	private int compeletesize=0;
	private boolean cancelFlag=false;
	private long downloadUpdateFile(String down_url, File file) {
		if (file == null) {
			return 0;
		}
		
		int down_step = 5;// 提示step
		int totalSize;// 文件总大小
		int downloadCount = 0;// 已经下载好的大小
		int updateCount = 0;// 已经上传的文件大小
		InputStream inputStream;
		OutputStream outputStream;
		URL url;
		try {
			url = new URL(down_url);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection();
			httpURLConnection.setConnectTimeout(TIMEOUT);
			httpURLConnection.setReadTimeout(TIMEOUT);
			// 获取下载文件的size
			totalSize = httpURLConnection.getContentLength();
			if (httpURLConnection.getResponseCode() == 404) {
				return 0;
			}
			inputStream = httpURLConnection.getInputStream();
			outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
			byte buffer[] = new byte[1024];
			int readsize = 0;
			a=System.currentTimeMillis();
			while (cancelFlag&&(readsize = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, readsize);
				downloadCount += readsize;// 时时获取下载到的大小
				compeletesize+=readsize;
				if (updateCount == 0
						|| (downloadCount * 100 / totalSize - down_step) >= updateCount) {
					updateCount += down_step;
					Message message = new Message();
					message.arg1 = updateCount;
					message.what = REF_PROGRESS;
					handler.sendMessage(message);
					 new Thread(
		               		 new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										long  b=System.currentTimeMillis();
										long c = b-a;
										long pp=compeletesize/(c==0?1:c);

										Message message = new Message();
										message.obj = (int)pp>1000?Math.round((pp*1.0f/1000)>5?2*10:(pp*1.0f/1000)*10)/10.0+"M/s":pp+"kb/s";
										message.what = REF_SPEED;
										handler.sendMessage(message);
										a = b;
										compeletesize = 0;
									}}).start();
				}


			}
			if(!cancelFlag){
				cancelDownLoad();
			}
			if (httpURLConnection != null) {
				httpURLConnection.disconnect();
			}
			inputStream.close();
			outputStream.close();
			return downloadCount;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void checkUpdate()  {
		// 检测是否需要软件升级
		NetworkManager.getInstance().getAppSetting(new HttpRespCallback<NetworkManager.RespAppSetting>() {
			@Override
			public void onRespFailure(int code, String msg) {
				LogUtils.e("msg",msg);
			}

			@Override
			protected void runOnMainThread(Message msg) {
				super.runOnMainThread(msg);
				NetworkManager.RespAppSetting setting = (NetworkManager.RespAppSetting) msg.obj;
				if (setting != null && setting.settings != null) {
					versionInfos = new VersionInfo();
//					versionInfos.url = "http://gdown.baidu.com/data/wisegame/18feea30b054cd76/shuajijingling_49.apk";
					versionInfos.url = setting.settings.androidDownloadUrl.trim();
					versionInfos.content = setting.settings.androidUpdateDesc;
					versionInfos.version = setting.settings.android_version.trim();
					ACache aCache=ACache.get(mContext);
					aCache.put("isUpdataCity",setting.settings.cityListUpdated);
					String str = AppUtils.getVersionName(mContext);
					LogUtils.e("getVersionName",""+str+"  "+AppUtils.getVersionCode(mContext));
					if (!versionInfos.version.equals(str)) {
						Message message = new Message();
						message.what = NEW_UPDATE;
						handler.sendMessage(message);
						if (updateFlag != null)
							updateFlag.checkUpdate(true);
					} else {
//						handler.sendEmptyMessage(NEW_UPDATED);
//						if (updateFlag != null) {
//							updateFlag.checkUpdate(false);
//						}
					}
				}
			}
		});

	}

	public interface checkUpdateFlag {
		public void checkUpdate(boolean flag);
	}

	public static class VersionInfo{
		public String version;
		public String type;
		public String url;
		public String content;
	}

}
