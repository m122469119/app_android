package com.dym.film.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.dym.film.manager.data.AroundSharedTicketDataManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.utils.LogUtils;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/29
 */
public class AroundMapView extends View
{
    public final static String TAG = "AroundMapView";


    private final static Paint mPaint = new Paint();

    private AroundSharedTicketDataManager mDataManager = AroundSharedTicketDataManager.mInstance;

    private int mViewWidth = 0;

    private int mViewHeight = 0;

    private int mCircleIntervalDp = 0;

    private int mCenterX = 0;

    private int mCenterY = 0;


    /**
     * 最边上圆离边缘的距离 dp
     */
    private final static int GAP_DP = 10; // dp

    /**
     * 每个头像间的最小间隔
     */
    private final static int PERSON_INTERVAL_DP = 30; // dp;

    /**
     * 同心圆的个数
     */
    private int mAllCircleNum = 0;
    private final static int DEFAULT_ALL_CIRCLE_NUM = 3; // 最多同心圆的个数

    /**
     * 一个圆上最多的头像个数
     */
    private final static int MAX_PERSON_PER_CIRCLE = 6;

    /**
     * 当前在第几个同心圆上
     */
    private final static float FIRST_CIRCLE_R = 45; // dp

    /**
     * 第一个同心圆的半径
     */
    private float mFirstCircleRadius = 0; // dp

    /**
     * 所有已经加入到地图上了的附近的人
     */
    private ArrayList<Integer> mSharedPersonsPositions = new ArrayList<>();

    private AroundMapViewCallback mMapViewCallback = null;

    /**
     * 保存圆上有几个头像了
     */
//    private int [] mCirclePersonNum = new int[DEFAULT_ALL_CIRCLE_NUM];

    private int [] mCircleColorAlpha = { 75, 50, 40 };

    private int [] mCircleBorderColors = {
            Color.parseColor("#D60000"),
            Color.parseColor("#AD0000"),
            Color.parseColor("#8C0000")
    };

    private int mBackgroundColor = Color.parseColor("#232323");

    private int mCircleColor = Color.parseColor("#FF0000");



    public AroundMapView(Context context)
    {
        this(context, null, 0);
    }

    public AroundMapView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public AroundMapView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        initializeMapView();
    }


    public void initializeMapView()
    {
        mPaint.setAntiAlias(true);

        // 随机第一个同心圆的半径
        mFirstCircleRadius = FIRST_CIRCLE_R;

        mAllCircleNum = DEFAULT_ALL_CIRCLE_NUM;

        this.post(new Runnable()
        {
            @Override
            public void run()
            {
                mMapViewCallback.onMapViewInitialize(getWidth(), getHeight());
            }
        });
    }

    /**
     * 通过DataManager的索引找出viewPager的位置
     * @param index
     * @return
     */
    public int findPagePositionByIndex(int index)
    {
        return mSharedPersonsPositions.indexOf(index);
    }

    /**
     * 设置 callback
     * @param callback
     */
    public void setAroundMapViewCallback(AroundMapViewCallback callback)
    {
        mMapViewCallback = callback;
    }


    /**
     * 获取第i个园的半径
     * @param index 注意 index会加1，因为第一个为用户自己的头像
     */
    private int getCircleRadius(int index)
    {
        return CommonManager.dpToPx(mFirstCircleRadius + (index+1) * mCircleIntervalDp);
    }

    /**
     * 找出这个圆上所有的可用的点
     * @param circle 这个园的index
     * @param radius 需要放置的园的半径
     * @return
     */
    private ArrayList<PointF> findAllGoodPoints(int circle, float radius)
    {
        ArrayList<PointF> allGoodPoints = new ArrayList<>();

        /**
         * 循环一圈，每5度检测一次
         */
        int r = getCircleRadius(circle);
        for (int i = 0; i < 360; i+=1) {
            double radians = Math.toRadians(i);
            float x = mCenterX + (float) (r * Math.sin(radians));
            float y = mCenterY + (float) (r * Math.cos(radians));
            if (outBorder(x, y, radius)) {
                continue;
            }

            boolean noAnyIntersected = true;
            for (int pos : mSharedPersonsPositions) {
                AroundSharedTicket p = mDataManager.getAroundSharedTicket(pos);

                if (p != null && intersected(x, y, radius, p.mX, p.mY, radius)) {
                    noAnyIntersected = false;
                    break;
                }
            }

            // 如果没有任何相交的情况出现，就加入到备选
            if (noAnyIntersected) {
                allGoodPoints.add(new PointF(x, y));
            }
        }

        return allGoodPoints;
    }

    /**
     * 加入一个附近的人
     * 随机选定一个园，扫描一圈，找出所有可以放置头像的点，如果没有可用的点存在，则-1或者+1个园上扫描，
     * 在所有可用的点中，随机出一个点，画出头像
     */
    public synchronized AroundSharedTicket addAroundSharedPerson(int position)
    {
        AroundSharedTicket ticket = mDataManager.getAroundSharedTicket(position);
        if (ticket == null) {
            return null;
        }

        /**
         * 随机出一个圆
         */
//        int circle = 0;
//        ArrayList<PointF> allGoodPoints = new ArrayList<>();
//        for (int i = 0; i < 3; ++i) {
//            allGoodPoints.addAll(findAllGoodPoints(i, ticket.mR));
//            if (!allGoodPoints.isEmpty()) {
//                circle = i;
//                break;
//            }
//        }

        int circle = (int) (Math.random() * 3);
        ArrayList<PointF> allGoodPoints = findAllGoodPoints(circle, ticket.mR);

        if (allGoodPoints.isEmpty()) {
            circle = (circle + 1) % 3;
            allGoodPoints = findAllGoodPoints(circle, ticket.mR);

            if (allGoodPoints.isEmpty()) {
                circle = (circle + 2) % 3;
                allGoodPoints = findAllGoodPoints(circle, ticket.mR);
            }
        }

        /**
         * 如果还为空，表示已经没有地方可以放了
         */
        if (allGoodPoints.isEmpty()) {
            LogUtils.e(TAG, "No Place to holder ticket!!!");
            return null;
        }

        /**
         * 随机出一个index
         */
        int randIndex = (int) (Math.random() * allGoodPoints.size());
        PointF point= allGoodPoints.get(randIndex);
        ticket.mX = point.x;
        ticket.mY = point.y;
        ticket.mMapCircleIndex = circle;


        /**
         * 加入进来
         */
        mSharedPersonsPositions.add(position);

        //Loge(TAG, "Add Person Success!");
        return ticket;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
//        super.onDraw(canvas);

        mViewWidth = getWidth();
        mViewHeight = getHeight();

        mCenterX = mViewWidth / 2;
        mCenterY = mViewHeight / 2;

        int minSize = Math.min(mViewWidth, mViewHeight);
        mCircleIntervalDp = (int) ((CommonManager.pxToDp(minSize / 2f) - mFirstCircleRadius - GAP_DP) / mAllCircleNum);

        /**
         * 画地图圆圈
         */
        mPaint.setColor(mCircleColor);

        for (int i = DEFAULT_ALL_CIRCLE_NUM - 1; i >= 0; --i) {
            int radius = getCircleRadius(i);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCircleColor);
            mPaint.setAlpha(mCircleColorAlpha[i]);
            canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);

            mPaint.setColor(mCircleBorderColors[i]);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(CommonManager.dpToPx(1));
            canvas.drawCircle(mCenterX, mCenterY, radius, mPaint);
        }

        /**
         * 正在请求数据动画
         */
        mRippleController.drawRipples(canvas);
    }

    /**
     * 启动正在请求数据的动画
     */
    public void startQueryingAnimation()
    {
        mRippleController.startRipple();
    }

    public void stopQueryingAnimation(boolean force)
    {
        mRippleController.stopRipple(force);
    }


    /**
     * 设置回调
     * @param callback 回调
     */
    public void setMapViewCallback(AroundMapViewCallback callback)
    {
        mMapViewCallback = callback;
    }

    /**
     * 移除一个person
     * @param pos
     */
    public void removePerson(int pos)
    {
        mSharedPersonsPositions.remove(Integer.valueOf(pos));
    }

    /**
     * 获取人数
     * @return
     */
    public int getPersonSize()
    {
        return mSharedPersonsPositions.size();
    }

    public int getSharedPersonPosition(int index)
    {
        if (index < 0 || index >= mSharedPersonsPositions.size()) {
            return -1;
        }
        return mSharedPersonsPositions.get(index);
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        //Loge(TAG, "On Attached To Window");
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        //Loge(TAG, "On Detached To Window");
    }

    /**
     * 判断两个人的头像圆是否相交, 最少间隔为5dp
     */
    private boolean intersected(float ax, float ay, float ar, float bx, float by, float br)
    {
        float randomInterval = (float) (Math.random() * PERSON_INTERVAL_DP + PERSON_INTERVAL_DP);
        ar += CommonManager.dpToPx(randomInterval);

        return (ax - bx)*(ax - bx) + (ay - by)*(ay - by) <= (ar + br) * (ar + br);
    }

    /**
     * 判断是否超出边界
     */
    private boolean outBorder(float x, float y, float r)
    {
        float borderInterval = CommonManager.dpToPx(5); // 10dp

        r += borderInterval;

        return (x-r) <= 0 || (x+r) >= mViewWidth || (y-r) <= 0 || (y+r) >= mViewHeight;
    }

    private RippleAnimController mRippleController = new RippleAnimController();
    private class RippleAnimController
    {

        private boolean mIsRunning = false;
        private Handler mHandler = new Handler();

        private Paint mRipplePaint = new Paint();

        private ValueAnimator mRadiusAnimator = null;

        private float mDestFloatValue = 0;
        private final ArrayList<Float> mValueQues = new ArrayList<>();

        private final static int RIPPLE_DURATION = 300;
        private final static int RIPPLE_TIME = 1500;
        private Runnable mRippleRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                mRadiusAnimator.clone().start();

                if (mIsRunning) {
                    mHandler.postDelayed(this, RIPPLE_DURATION);
                }
            }
        };

        private void startRipple()
        {
            mForceStop = false;
            mIsRunning = true;
            mHandler.removeCallbacks(mRippleRunnable);
//            mValueQues.clear();
            mRipplePaint.setColor(Color.WHITE);

            post(new Runnable()
            {
                @Override
                public void run()
                {
                    mDestFloatValue = Math.min(getWidth(), getHeight()) / 2f;
                    mRadiusAnimator = ValueAnimator.ofFloat(CommonManager.dpToPx(35), mDestFloatValue);
                    mRadiusAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mRadiusAnimator.setDuration(RIPPLE_TIME);
                    mRadiusAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator)
                        {
                            if (mForceStop) {
                                valueAnimator.cancel();
                                return;
                            }
                            synchronized (mValueQues) {
                                float value = (float) valueAnimator.getAnimatedValue();
                                mValueQues.add(value);
                            }

                            postInvalidate();
                        }
                    });

                    mRadiusAnimator.start();
                    mHandler.postDelayed(mRippleRunnable, RIPPLE_DURATION);

                }
            });

            invalidate();
        }

        private boolean mForceStop = false;
        private void stopRipple(boolean force)
        {
            mForceStop = force;
            if (mForceStop) {
                mValueQues.clear();
            }

            mIsRunning = false;
            mHandler.removeCallbacks(mRippleRunnable);
            invalidate();
        }

        /**
         * 画波纹
         */
        private synchronized void drawRipples(final Canvas canvas)
        {
            if (mForceStop) {
                return;
            }

            synchronized (mValueQues) {
                for (float value : mValueQues) {

                    mRipplePaint.setAlpha((int) ((1 - value / mDestFloatValue) * 120));
                    mRipplePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    mRipplePaint.setAntiAlias(true);
                    mRipplePaint.setStrokeWidth(2);
                    canvas.drawCircle(mCenterX, mCenterY, value, mRipplePaint);
                }

                mValueQues.clear();
            }

        }
    }


    public interface AroundMapViewCallback
    {
        void onMapViewInitialize(int width, int height);
    }
}
