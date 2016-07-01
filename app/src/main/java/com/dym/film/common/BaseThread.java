package com.dym.film.common;

import android.os.Handler;
import android.os.Message;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/15
 */

/**
 * 支持重复运行，线程停止，和线程交互，
 */
public class BaseThread implements Runnable
{
    protected boolean mIsRunning = false;

    protected ThreadHandler mHandler = new ThreadHandler(this);

    public void startThread()
    {
        if (mIsRunning) {
            return;
        }

        mIsRunning = true;

        new Thread(this).start();
    }

    public void stopThread()
    {
        mIsRunning = false;
    }

    public boolean isRunning()
    {
        return mIsRunning;
    }

    protected void sendMessage(int what)
    {
        mHandler.sendEmptyMessage(what);
    }

    protected void sendMessage(int what, Object obj)
    {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;

        mHandler.sendMessage(msg);
    }

    protected void sendMessage(Message msg)
    {
        mHandler.sendMessage(msg);
    }

    protected void handleMessage(Message msg)
    {
        //
    }

    @Override
    public void run()
    {
        //
    }


    protected static class ThreadHandler extends Handler
    {
        private BaseThread mThread = null;

        public ThreadHandler(BaseThread thread)
        {
            mThread = thread;
        }

        @Override
        public void handleMessage(Message msg)
        {
            mThread.handleMessage(msg);
        }
    }
}
