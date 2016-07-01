package com.dym.film.activity.mine;

import android.support.v4.app.Fragment;
import android.view.View;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonFragmentAdapter;
import com.dym.film.fragment.UpdatePwdFirstFragment;
import com.dym.film.fragment.UpdatePwdSecondFragment;
import com.dym.film.fragment.UpdatePwdThirdFragment;
import com.dym.film.views.HackyViewPager;

import java.util.ArrayList;

public class UpdatePwd2Activity extends BaseActivity
{
    public HackyViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private UpdatePwdFirstFragment firstFragment;
    private UpdatePwdSecondFragment secondFragment;
    private UpdatePwdThirdFragment thirdFragment;
    public String mobile="";
    @Override
    protected int setLayoutView()
    {
        // TODO Auto-generated method stub
        return R.layout.activity_update_pwd2;
    }

    @Override
    protected void initVariable()
    {
        firstFragment=new UpdatePwdFirstFragment();
        secondFragment=new UpdatePwdSecondFragment();
        thirdFragment=new UpdatePwdThirdFragment();
        fragments = new ArrayList<Fragment>();
    }

    @Override
    protected void findViews()
    {
        viewPager= (HackyViewPager) findViewById(R.id.viewPager);

    }

    @Override
    protected void initData()
    {
        fragments.add(firstFragment);
        fragments.add(secondFragment);
        fragments.add(thirdFragment);
        viewPager.toggleLock();
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(new CommonFragmentAdapter(getSupportFragmentManager(), fragments));
    }

    @Override
    protected void setListener()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void doClick(View view)
    {

    }

    @Override
    public void onBackPressed()
    {
        if (viewPager.getCurrentItem()==0){
            super.onBackPressed();
        }else{
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
        }
    }

}
