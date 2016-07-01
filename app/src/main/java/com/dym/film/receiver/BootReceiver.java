package com.dym.film.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{

    /*需要权限<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
     *
     *  <receiver android:name="com.highlink.callmanager.activity.BootReceiver"
            >
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />

                <category android:name="android.intent.category.HOME" />
            </intent-filter>
        </receiver>
*/
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated meothod stub

//		Intent intent2 = new Intent(context, NoticeService.class);
//		context.startService(intent2);
    }

}
