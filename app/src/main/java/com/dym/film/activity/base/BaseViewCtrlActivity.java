package com.dym.film.activity.base;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import com.dym.film.R;
import com.dym.film.application.UserInfo;
import com.dym.film.utils.AppManager;
import com.dym.film.utils.MatStatsUtil;
import com.example.xusoku.adaptationlibrary.sliderlib.app.BaseSwipeBackActivity;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */
public class BaseViewCtrlActivity extends BaseSwipeBackActivity
{
    public final static String TAG = "BaseViewCtrl";

    protected boolean mStatusBarTranslucentFlag = false;

    /**
     * 防止二次启动
     */
    protected boolean mStartedFlag = false;

    public static class UserState
    {
        public boolean mIsLogin = UserInfo.isLogin;
        public int mUserGender = UserInfo.gender;
        public String mUserAvatar = UserInfo.avatar;
        public String mUserName = UserInfo.name;

        public UserState()
        {
            resetUserState();
        }

        public void resetUserState()
        {
            mIsLogin = UserInfo.isLogin;
            mUserGender = UserInfo.gender;
            mUserAvatar = UserInfo.avatar;
            mUserName = UserInfo.name;
        }

        public boolean isUserStateChanged()
        {
            return (mIsLogin != UserInfo.isLogin || !mUserAvatar.equals(UserInfo.avatar) || mUserGender != UserInfo.gender || !mUserName.equals(UserInfo.name));
        }
    }

    protected UserState mLastUserState = new UserState();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        MatStatsUtil.init(this);
        //添加Activity到堆栈
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MatStatsUtil.onResume(this, getClass().getSimpleName());

        mStartedFlag = false;
        if (mLastUserState.isUserStateChanged()) {
            onUserStateChanged(mLastUserState);
            mLastUserState.resetUserState();
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        MatStatsUtil.onPause(this, getClass().getSimpleName());
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        MatStatsUtil.onStop(this);
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
    }

    @Override
    public void setContentView(View view)
    {
        super.setContentView(view);
    }

    @Override
    public void setContentView(int layoutId)
    {
        super.setContentView(layoutId);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public Resources getResources()
    {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public synchronized void startActivity(Intent intent)
    {
        if (!mStartedFlag) {
            mStartedFlag = true;
            super.startActivity(intent);
        }
    }

    /**
     * 如果可以就透明系统的状态栏
     */
    protected void translucentStatusBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mStatusBarTranslucentFlag = true;
        }
    }

    /**
     * 判断是否透明了状态栏
     */
    public boolean isTranslucentStatusBar()
    {
        return mStatusBarTranslucentFlag;
    }

    /**
     * 当用户登录或者登出了， 回调此方法
     */
    protected void onUserStateChanged(UserState oldState)
    {
        //
    }

    /**
     * 从Intent中获取键值数据
     */
    protected String getIntentString(String key)
    {
        Intent intent = getIntent();
        if (intent != null) {
            return intent.getStringExtra(key);
        }

        return null;
    }

    /**
     * 专门用来控制 整个Activity View的控制器
     * 默认显示状态栏
     */
    protected abstract class BaseContentViewController
    {
        protected BaseViewCtrlActivity mActivity = null;

        public BaseContentViewController(boolean visibleCustomStatusBar)
        {
            this(true, visibleCustomStatusBar);
        }

        public BaseContentViewController(boolean translucentStatusBar, boolean visibleCustomStatusBar)
        {
            this(translucentStatusBar, visibleCustomStatusBar, R.id.customStatusBarView, -1);
        }

        public BaseContentViewController(boolean translucentStatusBar,
                                         boolean visibleCustomStatusBar,
                                         int customStatusBarId, int color)
        {
            mActivity = BaseViewCtrlActivity.this;
            int viewId = getViewId();
            if (viewId > 0) {
                setContentView(viewId);
            }

            if (translucentStatusBar) {
                translucentStatusBar();
                configCustomStatusBar(customStatusBarId, visibleCustomStatusBar, color);
            }
        }


        /**
         * 获取ViewId
         */
        protected abstract int getViewId();

        /**
         * 自定义状态栏
         */
        protected void configCustomStatusBar(int id, boolean visible, int color)
        {
            View view = findViewById(id);
            if (view != null) {
                if (isTranslucentStatusBar()) {
                    if (visible) {
                        view.setVisibility(View.VISIBLE);
                        if (color > 0) {
                            view.setBackgroundColor(color);
                        }
                    }
                    else {
                        view.setVisibility(View.GONE);
                    }
                }
                else {
                    view.setVisibility(View.GONE);
                }
            }
        }

        /**
         * 通用的按钮点击回调
         */
        protected void onViewClicked(@NonNull View v)
        {
            //
        }

        /**
         * 设置mViewClickedListener 为这个id的监听器
         */
        protected void setOnClickListener(int id)
        {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(mViewClickedListener);
            }
        }


        protected View.OnClickListener
                mViewClickedListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v != null) {
                    onViewClicked(v);
                }
            }
        };

        /**
         * 设置点击关闭整个页面的view click
         */
        protected void setFinishView(int id)
        {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        finish();
                    }
                });
            }
        }
    }
}
