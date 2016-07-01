package com.dym.film.activity.mine;

import android.support.v4.app.Fragment;
import android.view.View;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonFragmentAdapter;
import com.dym.film.fragment.RegisterFirstFragment;
import com.dym.film.fragment.RegisterSecondFragment;
import com.dym.film.fragment.RegisterThirdFragment;
import com.dym.film.views.HackyViewPager;

import java.util.ArrayList;

public class Register2Activity extends BaseActivity
{

    public HackyViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private int curPosition=0;
    private RegisterFirstFragment firstFragment;
    private RegisterSecondFragment secondFragment;
    private RegisterThirdFragment thirdFragment;
    public String mobile="";
    public  static boolean isFromMy=true;
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_register2;
    }

    @Override
    protected void initVariable()
    {
        firstFragment=new RegisterFirstFragment();
        secondFragment=new RegisterSecondFragment();
        thirdFragment=new RegisterThirdFragment();
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
            if (!isFromMy){
                openActivity(MainActivity.class);
            }
        }else{
            viewPager.setCurrentItem(viewPager.getCurrentItem()-1,true);
        }
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        isFromMy=true;
    }
}
