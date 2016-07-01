package com.dym.film.activity.sharedticket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.activity.mine.MyShareTicketActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.BaseThread;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.ShareTicketDialogViewController;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.manager.data.AroundSharedTicketDataManager;
import com.dym.film.ui.CircleImageView;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.VibrateUtil;
import com.dym.film.views.AroundMapView;
import com.dym.film.views.AroundSharedTicket;

import java.util.ArrayList;

import okhttp3.Call;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

/**
 * 周围晒票的页面
 */
public class AroundSharedTicketActivity extends BaseViewCtrlActivity
{
    private AroundSharedTicketViewController mViewController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        translucentStatusBar();

        if (mViewController == null) {
            mViewController = new AroundSharedTicketViewController();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mViewController.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mViewController.onPaused();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mViewController.onDestroyed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ShareTicketDialogViewController.REQUEST_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(uri);

                        startActivity(intent);
                        break;
                    }
                }

                break;

            case ShareTicketDialogViewController.REQUEST_CODE_CAPTURE_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = ShareTicketDialogViewController.getCameraImageUri();
                    if (imageUri != null) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(imageUri);

                        startActivity(intent);
                    }
                }

                ShareTicketDialogViewController.onActivityFinished();
                break;
        }
    }


    public class AroundSharedTicketViewController extends BaseContentViewController
    {
        public final static String TAG = "AroundViewController";
        /**
         * 每一页显示12个头像
         */
        public final static int PERSON_PAGE_ITEMS_LIMIT = 9;
        private final static int DEF_ADD_INTERVAL_TIME = 100;
        /**
         * 分页控制
         */
        private final static int PAGE_LIMIT = 20;
        public BaiduLBSManager mLBSManager = BaiduLBSManager.getInstance();
        public AroundSharedTicketDataManager mDataManager = AroundSharedTicketDataManager.mInstance;
        /**
         * 地图的回调
         */
        protected AroundMapView.AroundMapViewCallback mMapViewCallback = new AroundMapView.AroundMapViewCallback()
        {
            @Override
            public void onMapViewInitialize(int width, int height)
            {
            }
        };
        NetworkManager.ReqGetAroundSharedTicket mGetAstRequest = new NetworkManager.ReqGetAroundSharedTicket();
        private double mLatitude = 0;
        private double mLongitude = 0;
        /**
         * 显示位置信息的标题栏TextView
         */
        private TextView mTitleLocationText = null;
        private ProgressBar mTitleLocationProgress = null;
        /**
         * 显示周围晒票的人的自定义控件
         */
        private AroundMapView mAroundMapView = null;
        private FrameLayout mAroundMapLayout = null;
        /**
         * Ticket的ViewPager
         */
        private ViewPager mViewPager = null;
        private CircleViewPagerAdapter mPagerAdapter = null;
        private ArrayList<PageViewController> mPageCtrlList = new ArrayList<>(12);
        /**
         * 定位线程
         */
        private LocationThread mLocationThread = new LocationThread();
        private Call mQueryAroundCall = null;
        /**
         * 晒票的选项view控制
         */
        private ShareTicketDialogViewController mSelectDialogController = null;
        private int mCurPageIndex = 0;
        /**
         * 当前正在加入到地图上的ticket所在页数的位置
         */
        private int mCurAddPosition = 0;
        private int mSelectedIndex = -1;
        private ArrayList<AroundSharedTicket> mPageTickets = new ArrayList<>();
        /**
         * 用户自己
         */
        private CircleImageView mMyPersonImage = null;
        private Handler mHandler = new Handler();
        /**
         * Sensor Manager
         */
        private SensorManager mSensorManager = null;
        /**
         * 是否正在请求数据
         */
        private boolean mIsQueryingData = false;
        private boolean mIsNoMore = false;
        private long mLastShakeTime = 0;
        private PageActionRunnable mPageRunnable = new PageActionRunnable();
        private boolean mLocationSuccess = false;
        protected SensorEventListener mSensorListener = new SensorEventListener()
        {
            @Override
            public void onSensorChanged(SensorEvent event)
            {
                int sensorType = event.sensor.getType();
                //values[0]:X轴，values[1]：Y轴，values[2]：Z轴
                float[] values = event.values;

                /**
                 * 正常情况下，任意轴数值最大就在9.8~10之间，只有在突然摇动手机
                 * 的时候，瞬时加速度才会突然增大或减少。   监听任一轴的加速度大于17即可
                 */
//            LogUtils.e(TAG, "Values:X:" + values[0] + " , Y:" + values[1] + " ,Z:" + values[2]);
                if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                    if ((Math.abs(values[0]) > 13 || Math.abs(values[1]) > 13)) {
                        onPhoneShake();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i)
            {

            }
        };


        public AroundSharedTicketViewController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_around_shared_ticket;
        }

        protected void initialize()
        {
            // 初始化标题栏
            setFinishView(R.id.closeButton);
            setOnClickListener(R.id.shareTicketButton);

            mTitleLocationText = (TextView) findViewById(R.id.titleLocationText);
            mTitleLocationProgress = (ProgressBar) findViewById(R.id.titleLocationProgress);

            mMyPersonImage = (CircleImageView) findViewById(R.id.myPersonImage);
            if (UserInfo.isLogin) {
                CommonManager.displayAvatar(UserInfo.avatar, mMyPersonImage);
                mMyPersonImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        startActivity(new Intent(mActivity, MyShareTicketActivity.class));
                    }
                });
            }

            // 初始化MapView
            mAroundMapLayout = (FrameLayout) findViewById(R.id.aroundMapLayout);
            mAroundMapView = (AroundMapView) findViewById(R.id.aroundMapView);
            mAroundMapView.setMapViewCallback(mMapViewCallback);

            // 初始化ViewPager
            initializeViewPager();

            mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);

            mLocationThread.startThread();
        }

        private void initializeViewPager()
        {
            mPageCtrlList.clear();
            for (int i = 0; i < PERSON_PAGE_ITEMS_LIMIT; ++i) {
                mPageCtrlList.add(null);
            }
            mPagerAdapter = new CircleViewPagerAdapter();
            mViewPager = (ViewPager) findViewById(R.id.aroundViewPager);
            mViewPager.setAdapter(mPagerAdapter);

            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
            {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
                {
                }

                @Override
                public void onPageSelected(int position)
                {
                    onItemSelected(mAroundMapView.getSharedPersonPosition(position));
                }

                @Override
                public void onPageScrollStateChanged(int state)
                {

                }
            });

            mCurAddPosition = 0;
            mCurPageIndex = 0;
            mIsNoMore = false;
        }

        public void onResume()
        {
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            // mAroundMapView.stopQueryingAnimation(true);
        }

        public void onPaused()
        {
            mSensorManager.unregisterListener(mSensorListener);
            mAroundMapView.stopQueryingAnimation(true);
//        mPagerAdapter.stopCircle();
        }

        public void onDestroyed()
        {
            mLocationThread.stopThread();
            if (mQueryAroundCall != null) {
                mQueryAroundCall.cancel();
            }

            mDataManager.onDestroy();
        }

        @Override
        protected void onViewClicked(@NonNull View view)
        {
            switch (view.getId()) {
                case R.id.shareTicketButton:
                    MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.TICKET_SHOW);
                    // 启动晒票页面
                    // 启动晒票
                    if (mSelectDialogController == null) {
                        mSelectDialogController = new ShareTicketDialogViewController(mActivity);
                    }
                    mSelectDialogController.show();
                    break;
            }
        }

        /**
         * 开始请求周围晒票数据
         */
        protected synchronized void startQueryAroundSharedTickets()
        {
            if (mIsQueryingData) {
                return;
            }
            mIsQueryingData = true;

            mQueryAroundCall = NetworkManager.getInstance().getAroundSharedTicket(mCurPageIndex, PAGE_LIMIT, mGetAstRequest, new HttpRespCallback<NetworkManager.RespGetAroundSharedTicket>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    LogUtils.e(TAG, "Code: " + code + " Msg: " + msg);
                    mIsQueryingData = false;
                    mAroundMapView.stopQueryingAnimation(false);
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespGetAroundSharedTicket tickets = (NetworkManager.RespGetAroundSharedTicket) msg.obj;
                    if (mCurPageIndex == 0) {
                        mDataManager.clear();
                    }

                    int size = tickets.neighbours != null ? tickets.neighbours.size() : 0;

                    if (tickets.neighbours != null) {
                        for (NetworkManager.NeighboursRespModel model : tickets.neighbours) {

                            NetworkManager.SharedTicketRespModel ticket = model.toSharedTicketRespModel();

                            if (ticket != null) {
                                mDataManager.append(ticket);
                            }
                        }
                        LogUtils.e(TAG, "Around Size: " + mDataManager.getSize());

                        mCurPageIndex += 1;
                    }

                    if (size < PAGE_LIMIT) {
                        mIsNoMore = true;
                    }

                    mPageRunnable.changeOnePage();

                    mIsQueryingData = false;
                }
            });
        }

        protected synchronized void onPhoneShake()
        {
            if (mLocationThread.isRunning()) {
                return;
            }

            if (!mLocationSuccess) {
                mLocationThread.startThread();
                return;
            }

            if (System.currentTimeMillis() - mLastShakeTime < 1500) {
                return;
            }

            if (mDataManager.getSize() - mCurAddPosition < 6 && !mIsNoMore) {
                if (!mIsQueryingData) {
                    mLastShakeTime = System.currentTimeMillis();
                    vibratePhone();
                    startQueryAroundSharedTickets();
                }
                return;
            }

            if (!mPageRunnable.isChangingPage()) {
                mLastShakeTime = System.currentTimeMillis();
                vibratePhone();
                mPageRunnable.changeOnePage();
            }
        }

        private void playSearchSound()
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... voids)
                {
                    MediaPlayer.create(mActivity, R.raw.shake_sound_male).start();
                    return null;
                }
            }.execute();
        }

        private void playMatchSound()
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... voids)
                {
                    try {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MediaPlayer.create(mActivity, R.raw.shake_match).start();
                    return null;
                }
            }.execute();
        }

        protected void vibratePhone()
        {
            /**
             * 启动动画效果
             */
            mAroundMapView.startQueryingAnimation();

            VibrateUtil.vibrate(mActivity, 100);
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    VibrateUtil.vibrate(mActivity, 100);
                }
            }, 100);

            playSearchSound();
        }

        /**
         * 加载某一个到地图上
         */
        protected void addOneToMapView(AroundSharedTicket ticket)
        {
            if (ticket.getAvatarView() != null) {
                mAroundMapLayout.removeView(ticket.getAvatarView());
            }

            View view = ticket.initAvatarView(mActivity, new AroundSharedTicket.LoadAvatarCallback()
            {
                @Override
                public void onImageClicked(View v, int index)
                {
                    LogUtils.e(TAG, "SelectedIndex: " + mSelectedIndex);
                    onItemSelected(index);
                }
            });

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (ticket.mR * 2), (int) (ticket.mR * 2));

            params.leftMargin = (int) (ticket.mX - ticket.mR);
            params.topMargin = (int) (ticket.mY - ticket.mR);

            mPageTickets.add(ticket);
            mAroundMapLayout.addView(view, params);
            mPagerAdapter.notifyDataSetChanged();
        }

        /**
         * 当一个头像被选中时，其他头像则就需要恢复正常，
         */
        protected synchronized void onItemSelected(int index)
        {
            LogUtils.e(TAG, "On Item Selected: " + index);
            if (mSelectedIndex == index) {
                return;
            }

            // 前面有选中的头像
            if (mSelectedIndex >= 0) {
                AroundSharedTicket selectTicket = mDataManager.getAroundSharedTicket(mSelectedIndex);
                if (selectTicket != null) {
                    LogUtils.e(TAG, "unselect : " + mSelectedIndex);
                    selectTicket.unSelect(mActivity);
                }
            }

            mSelectedIndex = index;
            AroundSharedTicket ticket = mDataManager.getAroundSharedTicket(index);
            if (ticket != null) {
                LogUtils.e(TAG, "select : " + index);
                ticket.select(mActivity);

                int pagePosition = mAroundMapView.findPagePositionByIndex(index);

                LogUtils.e(TAG, "PagePosition: " + pagePosition);
                mViewPager.setCurrentItem(pagePosition, true);
            }
        }

        private class PageActionRunnable implements Runnable
        {
            private final static int ACTION_NONE = 0;
            private final static int ACTION_ADD = 1;
            private final static int ACTION_REMOVE = 2;
            private int mAction = ACTION_NONE;

            public synchronized void changeOnePage()
            {
                if (mAction != ACTION_NONE) {
                    return;
                }

                mAction = ACTION_REMOVE;

                mHandler.post(this);
            }

            public boolean isChangingPage()
            {
                return mAction != ACTION_NONE;
            }

            @Override
            public void run()
            {
                switch (mAction) {
                    case ACTION_ADD:
                        addOnePageToMap();
                        break;

                    case ACTION_REMOVE:
                        removeOnePageFromMap();
                        break;
                }
            }

            public void addOnePageToMap()
            {
                if (mDataManager.getSize() == 0 || mPageTickets.size() >= PERSON_PAGE_ITEMS_LIMIT) {
                    onAddPageFinished();
                    return;
                }

                if (mIsNoMore) {
                    mCurAddPosition = mCurAddPosition % mDataManager.getSize();
                    for (AroundSharedTicket ast : mPageTickets) {
                        if (ast.mPersonIndex == mCurAddPosition) {
                            onAddPageFinished();
                            return;
                        }
                    }
                }
                else if (mCurAddPosition >= mDataManager.getSize()) {
                    onAddPageFinished();
                    return;
                }

                AroundSharedTicket ticket = mAroundMapView.addAroundSharedPerson(mCurAddPosition);
                if (ticket == null) {
                    onAddPageFinished();
                    return;
                }

                addOneToMapView(ticket);
                mCurAddPosition += 1;

                mHandler.postDelayed(this, 50);
            }

            public void removeOnePageFromMap()
            {
                if (mPageTickets.isEmpty()) {
                    onRemovePageFinished();
                    return;
                }

                AroundSharedTicket ticket = mPageTickets.get(0);
                if (ticket.mPersonIndex == mSelectedIndex) {
                    ticket.hideSelected(mActivity);
                }
                else {
                    ticket.hide(mActivity, null);
                }

                mPageTickets.remove(0);
                mAroundMapView.removePerson(ticket.mPersonIndex);

                mHandler.postDelayed(this, 0);
            }

            protected void onAddPageFinished()
            {
                LogUtils.e(TAG, "PageTicks Size; " + mPageTickets.size());
                playMatchSound();
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!mPageTickets.isEmpty()) {
                            mSelectedIndex = -1;
                            AroundSharedTicket ticket = mPageTickets.get(0);
                            onItemSelected(ticket.mPersonIndex);
                        }
                    }
                }, 500);

                mAction = ACTION_NONE;
                mAroundMapView.stopQueryingAnimation(false);
            }

            public void onRemovePageFinished()
            {
                mAroundMapLayout.removeAllViews();

                mViewPager.setAdapter(mPagerAdapter);
                mPagerAdapter.notifyDataSetChanged();

                mAction = ACTION_ADD;
                mHandler.post(this);
            }
        }

        protected class CircleViewPagerAdapter extends PagerAdapter
        {

            @Override
            public int getCount()
            {
                return mAroundMapView.getPersonSize();
            }

            @Override
            public View instantiateItem(ViewGroup container, int position)
            {
                View view = View.inflate(container.getContext(), R.layout.layout_around_shared_ticket, null);

                PageViewController ctrl = new PageViewController(position, view);
                mPageCtrlList.set(position, ctrl);

                container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object)
            {
                container.removeView((View) object);
                mPageCtrlList.set(position, null);
            }

            @Override
            public boolean isViewFromObject(View view, Object object)
            {
                return view == object;
            }
        }

        private class PageViewController
        {
            private int mDataPosition = 0;

            public PageViewController(int pos, View view)
            {
                initializePageView(pos, view);
            }

            /**
             * 初始化一个page的view
             */
            private void initializePageView(int position, View view)
            {
                LogUtils.e(TAG, "Position: " + position);
                mDataPosition = mAroundMapView.getSharedPersonPosition(position);

                LogUtils.e(TAG, "DataPosition: " + mDataPosition);
                final NetworkManager.SharedTicketRespModel ticket = mDataManager.get(mDataPosition);
                if (ticket == null) {
                    return;
                }

                /**
                 * 加载图片
                 */
                ImageView image = (ImageView) view.findViewById(R.id.sharedTicketImage);
                String url = "";
                if (ticket.stubImage != null) {
                    url = QCloudManager.urlImage1(ticket.stubImage.url, ConfigInfo.SIZE_AR_STICKET_WIDTH, ConfigInfo.SIZE_AR_STICKET_HEIGHT);
                }
                CommonManager.displayImage(url, image, R.drawable.ic_loading);


                /**
                 * 设置值或不值得图片
                 */
                ImageView worthImage = (ImageView) view.findViewById(R.id.worthButtonImage);
                worthImage.setImageResource(ticket.opinion == 1 ? R.drawable.ic_is_worth_white : R.drawable.ic_is_not_worth);

                /**
                 * 设置内容
                 */
                TextView content = (TextView) view.findViewById(R.id.sharedTicketContentText);
                if (TextUtils.isEmpty(ticket.content)) {
                    content.setText("（这个家伙很懒，什么也没说）");
                }
                else {
                    content.setText(ticket.content);
                }

                /**
                 * 设置名字
                 */
                TextView ticketName = (TextView) view.findViewById(R.id.ticketName);
                ticketName.setText("");
                if (ticket.tags != null && !ticket.tags.isEmpty() && !TextUtils.isEmpty(ticket.tags.get(0))) {
                    ticketName.setText(String.valueOf("《" + ticket.tags.get(0) + "》"));
                }

                /**
                 * 设置距离
                 */
                TextView lengthText = (TextView) view.findViewById(R.id.lengthText);
                if (ticket.writer.location != null) {
                    LatLng p1 = new LatLng(ticket.writer.location.latitude, ticket.writer.location.longitude);
                    double distance = DistanceUtil.getDistance(p1, new LatLng(mLatitude, mLongitude));
                    //Loge(TAG, "Distance: " + distance);
                    CommonManager.setDistance(lengthText, distance);
                }
                else {
                    CommonManager.setDistance(lengthText, Double.MAX_VALUE);
                }


                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        CommonManager.putData(SharedTicketDetailActivity.KEY_INTENT, ticket);

                        Intent intent = new Intent(mActivity, SharedTicketDetailActivity.class);
                        mActivity.startActivity(intent);
                    }
                });
            }
        }

        protected class LocationThread extends BaseThread
        {
            public final static int WHAT_LOCATION_FINISHED = 0x31;
            protected BaiduLBSManager.SimpleLocationListener mLocationListener = new BaiduLBSManager.SimpleLocationListener()
            {
                @Override
                public void onLocationFinished(BDLocation location)
                {
                    sendMessage(WHAT_LOCATION_FINISHED, location);
                }
            };

            @Override
            public void startThread()
            {
                mTitleLocationText.setVisibility(View.GONE);
                mTitleLocationProgress.setVisibility(View.VISIBLE);
                mAroundMapView.startQueryingAnimation();

                super.startThread();
            }

            @Override
            public void run()
            {
                // 开始定位
                mLBSManager.registerLocationListener(mLocationListener);
                mLBSManager.startLocation();
            }

            @Override
            public void stopThread()
            {
                super.stopThread();
                mLBSManager.unregisterLocationListener(mLocationListener);
                mLBSManager.stopLocation();
            }

            @Override
            public void handleMessage(Message msg)
            {
                String district = "";
                String city = "";
                String province = "";

                BDLocation location = (BDLocation) msg.obj;
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();

                    district = location.getDistrict();
                    city = location.getCity();
                    province = location.getProvince();
                }

                if (TextUtils.isEmpty(district) || TextUtils.isEmpty(city) || district.equals("null") || city.equals("null")) {
                    mLatitude = mLBSManager.getLatitude();
                    mLongitude = mLBSManager.getLongitude();

                    district = mLBSManager.getDistrict();
                    city = mLBSManager.getCity();
                    province = mLBSManager.getProvince();
                }

                if (TextUtils.isEmpty(district) || TextUtils.isEmpty(city) || district.equals("null") || city.equals("null")) {

                    mTitleLocationProgress.setVisibility(View.GONE);
                    mTitleLocationText.setVisibility(View.VISIBLE);
                    mTitleLocationText.setText("未知位置");

                    mAroundMapView.stopQueryingAnimation(false);
                    mLocationSuccess = false;
                    return;
                }
                mLocationSuccess = true;
                stopThread();

                mTitleLocationProgress.setVisibility(View.GONE);
                mTitleLocationText.setVisibility(View.VISIBLE);
                mTitleLocationText.setText(String.valueOf(city + " " + district));

                mGetAstRequest.district = district;
                mGetAstRequest.city = city;
                mGetAstRequest.province = province;
                mGetAstRequest.longitude = mLongitude;
                mGetAstRequest.latitude = mLatitude;

                mIsNoMore = false;
                mDataManager.clear();
                startQueryAroundSharedTickets();
            }
        }
    }

}
