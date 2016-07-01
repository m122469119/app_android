package com.dym.film.manager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/18
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;

import java.util.List;

/**
 * 管理百度的LBS服务
 */
public class BaiduLBSManager
{
    public final static String TAG = "BaiduLBSManager";

    private final static BaiduLBSManager mInstance = new BaiduLBSManager();

    public static BaiduLBSManager getInstance()
    {
        return mInstance;
    }

    public final static String PREF_LBS_NAME = "LBS";

    public final static String KEY_PROVINCE = "province";
    public final static String KEY_CITY = "city";
    public final static String KEY_DISTRICT = "district";
    public final static String KEY_LONGITUDE = "longitude";
    public final static String KEY_LATITUDE = "latitude";
    public final static String KEY_SUCCESS = "success";


    private Context mContext = null;

    private LocationClient mLocationClient = null;

    private boolean mIsLocating = false;

    public void initializeBaiduLBS(Context context)
    {
        mContext = context;
        SDKInitializer.initialize(context);
        mLocationClient = new LocationClient(context);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(false);//可选，默认false,设置是否使用gps
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要

        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mLocationListener);
        startLocation();
    }

    public void registerLocationListener(BDLocationListener listener)
    {
        if (listener != null) {
            mLocationClient.registerLocationListener(listener);
        }
    }

    public void unregisterLocationListener(BDLocationListener listener)
    {
        if (listener != null) {
            mLocationClient.unRegisterLocationListener(listener);
        }
    }

    public LocationClient getLocationClient()
    {
        return mLocationClient;
    }

    public void requestLocation()
    {
        mLocationClient.requestLocation();
    }

    public synchronized void startLocation()
    {
        mIsLocating = true;

        mLocationClient.start();
        mLocationClient.requestLocation();
    }

    public boolean isLocating()
    {
        return mIsLocating;
    }

    public void stopLocation()
    {
        mLocationClient.stop();
    }

    public BDLocationListener mLocationListener = new BDLocationListener()
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                    String province = bdLocation.getProvince();
                    String city = bdLocation.getCity();
                    String district = bdLocation.getDistrict();
                    double longitude = bdLocation.getLongitude();
                    double latitude = bdLocation.getLatitude();

                    if (TextUtils.isEmpty(province) ||
                            TextUtils.isEmpty(city) || TextUtils.isEmpty(district)) {
                        editor.putBoolean(KEY_SUCCESS, false);
                        break;
                    }

                    editor.putString(KEY_PROVINCE, province);
                    editor.putString(KEY_CITY, city);
                    editor.putString(KEY_DISTRICT, district);
                    editor.putString(KEY_LONGITUDE, String.valueOf(longitude));
                    editor.putString(KEY_LATITUDE, String.valueOf(latitude));
                    editor.putBoolean(KEY_SUCCESS, true);
                    break;

                default:
                    editor.putBoolean(KEY_SUCCESS, false);
                    break;
            }
            editor.apply();

            mLocationClient.stop();
            mIsLocating = false;
        }
    };

    public boolean isLocateSuccess()
    {
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            return preferences.getBoolean(KEY_SUCCESS, false);
        }

        return false;
    }

    public String getProvince()
    {
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            return preferences.getString(KEY_PROVINCE, "");
        }

        return "";
    }

    public String getCity()
    {
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            return preferences.getString(KEY_CITY, "");
        }

        return "";
    }

    public String getDistrict()
    {
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            return preferences.getString(KEY_DISTRICT, "");
        }

        return "";
    }

    public double getLongitude()
    {
        double res = 0;
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            String str =  preferences.getString(KEY_LONGITUDE, "");
            try {
                res = Double.valueOf(str);
            }
            catch (Exception e) {
                res = 0;
            }
        }

        return res;
    }

    public double getLatitude()
    {
        double res = 0;
        if (mContext != null) {
            SharedPreferences preferences = mContext.getSharedPreferences(PREF_LBS_NAME, Context.MODE_PRIVATE);

            String str =  preferences.getString(KEY_LATITUDE, "");
            try {
                res = Double.valueOf(str);
            }
            catch (Exception e) {
                res = 0;
            }
        }

        return res;
    }

    public static abstract class SimpleLocationListener implements BDLocationListener
    {
        protected String msg = "";

        public abstract void onLocationFinished(BDLocation location);

        @Override
        public void onReceiveLocation(BDLocation location)
        {
            /**
             * 保存位置信息到SharePreference里面
             */

            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            }
            else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            }
            else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            }
            else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            }
            else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            }
            else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            //Logi("BaiduLocationApiDem", sb.toString());
            msg = sb.toString();


            switch (location.getLocType()) {
                case BDLocation.TypeGpsLocation:
                case BDLocation.TypeNetWorkLocation:
                case BDLocation.TypeOffLineLocation:
                    onLocationFinished(location);
                    break;

                default:
                    onLocationFinished(null);
                    break;
            }

        }
    }
}
