package com.dym.film.controllers;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/10
 */

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * 基础view控制类
 */
public class BaseViewController
{
    protected View mRootView = null;

    protected Activity mActivity = null;

    protected BaseViewController(@NonNull Activity activity, int viewId)
    {
        mActivity = activity;
        mRootView = View.inflate(mActivity, viewId, null);
    }

    protected BaseViewController(@NonNull Activity activity, @NonNull View view)
    {
        mActivity = activity;
        mRootView = view;
    }

    /**
     * 获取Root View
     */
    public View getRootView()
    {
        return mRootView;
    }

    /**
     * 查找view
     * @param id view的id
     * @return
     */
    protected View findViewById(int id)
    {
        return mRootView.findViewById(id);
    }

    /**
     * 从Intent中获取键值数据
     * @param key
     * @return
     */
    protected String getIntentString(String key)
    {
        Intent intent = mActivity.getIntent();
        if (intent != null) {
            return intent.getStringExtra(key);
        }

        return null;
    }

    /**
     * View Inflate
     */
    protected View inflateView(int id)
    {
        return View.inflate(mActivity, id, null);
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
     * @param id
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
}
