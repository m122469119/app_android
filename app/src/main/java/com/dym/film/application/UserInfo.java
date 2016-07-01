package com.dym.film.application;

import android.content.Context;
import android.content.SharedPreferences;

import com.dym.film.activity.MainActivity;
import com.dym.film.activity.mine.MyMainActivity;

public class UserInfo
{
    public static Class<?> topActivity= MainActivity.class;
    public static int userID =0;
    public static String mobile = "";
    public static String token = "";
    public static String name = "";
    public static String avatar = "";//头像url
    public static String jid = "";
    public static int deviceType = 2;
    public static int category=0;//0原生用户
    public static String createTime = "";
    public static boolean isLogin = false;
    public static boolean isNative = true;
    public static String province = "";
    public static String city = "";
    public static String district = "";
    public static long longitude = 0;
    public static long latitude = 0;
    public static String signToken = "";
    public static int gender=0;


    public static void saveUserInfo(Context context){
        SharedPreferences preference = context.getSharedPreferences(ConfigInfo.PREFERENCE_NAME_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("userID", UserInfo.userID);
        editor.putString("mobile", UserInfo.mobile);
        editor.putString("token", UserInfo.token);
        editor.putString("name", UserInfo.name);
        editor.putInt("gender", UserInfo.gender);
        editor.putString("avatar", UserInfo.avatar);
        editor.putString("createTime", UserInfo.createTime);
        editor.putBoolean("isLogin", UserInfo.isLogin);
        editor.putString("province", UserInfo.province);
        editor.putString("city", UserInfo.city);
        editor.putString("district", UserInfo.district);
        editor.putLong("longitude", UserInfo.longitude);
        editor.putLong("latitude", UserInfo.latitude);
        editor.putString("jid", UserInfo.jid);
        editor.putBoolean("isNative", UserInfo.isNative);
        editor.commit();
    }

    public static void getUserInfo(Context context){
        SharedPreferences preference = context.getSharedPreferences(ConfigInfo.PREFERENCE_NAME_USER, Context.MODE_PRIVATE);
        UserInfo.userID= preference.getInt("userID",0);
        UserInfo.mobile=preference.getString("mobile", "");
        UserInfo.token=preference.getString("token","");
        UserInfo.name=preference.getString("name","");
        UserInfo.gender=preference.getInt("gender", 0);
        UserInfo.avatar=preference.getString("avatar", "");
        UserInfo.createTime=preference.getString("createTime","");
        UserInfo.isLogin=preference.getBoolean("isLogin", false);
        UserInfo.isNative=preference.getBoolean("isNative", true);
        UserInfo.province=preference.getString("province", "");
        UserInfo.city=preference.getString("city","");
        UserInfo.district=preference.getString("district","");
        UserInfo.longitude=preference.getLong("longitude", 0);
        UserInfo.latitude=preference.getLong("latitude",0);
        UserInfo.jid=preference.getString("jid", "");
    }

    public static void clearUserInfo(Context context){
        SharedPreferences preference = context.getSharedPreferences(ConfigInfo.PREFERENCE_NAME_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putInt("userID", 0);
        editor.putString("mobile", "");
        editor.putString("token","");
        editor.putString("name","");
        editor.putInt("gender",0);
        editor.putString("avatar","");
        editor.putString("createTime","");
        editor.putBoolean("isLogin", false);
        editor.putString("province", "");
        editor.putString("city","");
        editor.putString("district","");
        editor.putLong("longitude", 0);
        editor.putLong("latitude",0);
        editor.commit();

        UserInfo.userID=0;
        UserInfo.mobile="";
        UserInfo.token="";
        UserInfo.name="";
        UserInfo.gender=0;
        UserInfo.avatar="";
        UserInfo.createTime="";
        UserInfo.isLogin=false;
        UserInfo.province="";
        UserInfo.city="";
        UserInfo.district="";
        UserInfo.longitude=0;
        UserInfo.latitude=0;
        MyMainActivity.hasBaseInfo=false;
    }
}
