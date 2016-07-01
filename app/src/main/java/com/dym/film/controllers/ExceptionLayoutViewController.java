package com.dym.film.controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/14
 */
public class ExceptionLayoutViewController extends BaseViewController
{
    private View mClickView = null;
    private ProgressBar mProgressBar = null;

    public ExceptionLayoutViewController(@NonNull Activity activity, @NonNull final ViewCallback inter, View view)
    {
        super(activity, view);

        mClickView = findViewById(R.id.clickLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgress);
        mClickView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                inter.onExceptionViewClicked();
            }
        });
    }

    public void show()
    {
        mRootView.setVisibility(View.VISIBLE);
        mClickView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void progress()
    {
        mRootView.setVisibility(View.VISIBLE);

        mClickView.setVisibility(View.GONE);
//        mClickView.setOnClickListener(null);

        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hide()
    {
        mRootView.setVisibility(View.GONE);
//        mClickView.setOnClickListener(null);
        mProgressBar.setVisibility(View.GONE);
    }

    public interface ViewCallback
    {
        /**
         * 点击重新加载
         */
        void onExceptionViewClicked();
    }
}
