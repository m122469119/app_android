package com.dym.film.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonFragmentAdapter;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.MyBaseInfo;
import com.dym.film.receiver.JPushMessageReceiver;
import com.dym.film.ui.viewpagerindicator.FixPageIndicator;
import com.dym.film.ui.viewpagerindicator.PageIndicator;
import com.dym.film.ui.viewpagerindicator.scrollbar.ColorBar;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.CustomTypefaceTextView;
import com.dym.film.views.HackyViewPager;

import java.util.ArrayList;


public class HomeFragment extends BaseFragment
{
    private HackyViewPager viewPager;
    private FixPageIndicator indicator;
    private String[] tabNames = { "热映", "待映" , "榜单"};
    private ImageView unReadImage = null;
    @Override
    protected void initVariable()
    {
        MatStatsUtil.eventClick(mContext,MatStatsUtil.FILM,null);
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_home;
    }

    @Override
    protected void findViews(View view)
    {
        viewPager = (HackyViewPager) view.findViewById(R.id.viewPager);
        // viewPager.toggleLock();
        viewPager.setOffscreenPageLimit(3);
        indicator = (FixPageIndicator) view.findViewById(R.id.indicator);
        unReadImage = (ImageView) view.findViewById(R.id.unreadImage);
    }

    @Override
    protected void initData()
    {
        ShowFilmlistFragment filmShowListFragment = new ShowFilmlistFragment();
        PreFilmlistFragment filmPreListFragment = new PreFilmlistFragment();
        BboardFilmlistFragment bboardFilmlistFragment = new BboardFilmlistFragment();
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(filmShowListFragment);
        fragments.add(filmPreListFragment);
        fragments.add(bboardFilmlistFragment);

        // TODO Auto-generated method stub
        viewPager.setAdapter(new CommonFragmentAdapter(getChildFragmentManager(),
                fragments));
        indicator.setViewPager(viewPager, 0);
        indicator.setIndicatorAdapter(new PageIndicator.IndicatorAdapter() {
            @Override
            public View getIndicatorView(int position) {

                CustomTypefaceTextView textView=new CustomTypefaceTextView(mContext);
                textView.setTextColor(getResources().getColorStateList(R.color.film_type_tab_text_color));
                textView.setText(tabNames[position]);
                textView.setTextSize(12);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }

            @Override
            public void onPageScrolled(View view, int position,
                                       float selectPercent) {
            }
        });
        ColorBar colorBar = new ColorBar(mContext, 0xffb10b0b);
        colorBar.setRadius(DimenUtils.dp2px(mContext, 3));
        indicator.setScrollBar(colorBar);
    }

    @Override
    protected void setListener()
    {

    }
    public void onResume()
    {
        super.onResume();

        startCheckUserMessage();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void startCheckUserMessage()
    {
        if (UserInfo.isLogin) {
            checkNewMessage();
        }
        else if (unReadImage != null) {
            unReadImage.setVisibility(View.INVISIBLE);
        }

        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new SimpleBroadcastReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(JPushMessageReceiver.ACTION_MESSAGE_RECEIVED);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    private SimpleBroadcastReceiver mBroadcastReceiver = null;
    private void checkNewMessage()
    {
        apiRequestManager.getMyBaseInfo(new AsyncHttpHelper.ResultCallback<MyBaseInfo>()
        {
            @Override
            public void onSuccess(MyBaseInfo data)
            {
                if (unReadImage != null) {
//                    LogUtils.e("Kejin", "hasNew: " + data.info.message.hasNew);
                    unReadImage.setVisibility((data == null || data.info == null ||
                            data.info.message == null || data.info.message.hasNew == 0) ? View.INVISIBLE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                //
            }
        });
    }

    /**
     * 监听消息
     */
    private class SimpleBroadcastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            LogUtils.e("Kejin", "Action: " + action);
            if (JPushMessageReceiver.ACTION_MESSAGE_RECEIVED.equals(action)) {
                /**
                 * 从服务器请求一次
                 */
                LogUtils.e("Kejin", "check message");
                checkNewMessage();
            }
        }
    }
}
