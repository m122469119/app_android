package com.dym.film.activity.mine;


import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonFragmentAdapter;
import com.dym.film.fragment.MyAttentionAuthorFragment;
import com.dym.film.fragment.MyAttentionFilmFragment;
import com.dym.film.ui.viewpagerindicator.FixPageIndicator;
import com.dym.film.ui.viewpagerindicator.PageIndicator;
import com.dym.film.views.CustomTypefaceTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class MyAttentionActivity extends BaseActivity
{
    List<Fragment> fragments;
    private ViewPager viewPager;
    private MyAttentionFilmFragment myAttentionFilmFragment;
    private MyAttentionAuthorFragment myAttentionAuthorFragment;
    private CommonFragmentAdapter fragmentAdapter;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_my_attention;
    }


    protected void initVariable()
    {

        myAttentionFilmFragment = new MyAttentionFilmFragment();
        myAttentionAuthorFragment = new MyAttentionAuthorFragment();
        fragments = new ArrayList<>();
        fragments.add(myAttentionFilmFragment);
        fragments.add(myAttentionAuthorFragment);
    }


    protected void findViews()
    {
        showTopBar();
        setTitle("我的关注");
        fragmentAdapter = new CommonFragmentAdapter(getSupportFragmentManager(), fragments);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(fragmentAdapter);
        FixPageIndicator indicator = (FixPageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewPager, 0);
        indicator.setIndicatorAdapter(new PageIndicator.IndicatorAdapter()
        {
            @Override
            public View getIndicatorView(int position)
            {
                TextView textView = new CustomTypefaceTextView(mContext);
                textView.setTextSize(16);
                textView.setGravity(Gravity.CENTER);
                if (position == 0) {
                    textView.setText("影片");
                }
                else {
                    textView.setText("影评人");
                }
                textView.setBackgroundResource(R.drawable.vpi_tab_indicator);
                textView.setTextColor(mContext.getResources().getColorStateList(R.color.main_btn_tab_text_color));
                return textView;
            }

            @Override
            public void onPageScrolled(View view, int position, float selectPercent)
            {

            }
        });
    }

    protected void initData()
    {

    }

    protected void setListener()
    {

    }

    public void doClick(View view)
    {
        finish();
    }
}
