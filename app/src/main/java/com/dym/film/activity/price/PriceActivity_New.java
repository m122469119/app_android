package com.dym.film.activity.price;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.entity.cinema;
import com.dym.film.fragment.PriceFragment_New;
/**
 * Created by xusoku on 2016/3/22.
 */
public class PriceActivity_New extends BaseActivity {

    public static String KEY = "key";
    public static String NUM = "num";
    public static String PRICE = "price";
    public static String CINEMAID = "cinimaId";
    public static String FILMID = "filmId";
    public static String FILMIDNAME = "filmIdname";
    public static String FLAG = "flag";


    public static int ResultCode = 0x9;
    private cinema cinemaRespModel;
    private String filmId = "";
    private String filmIdName = "";
    private String selectDate = "";


    private PriceFragment_New mPriceFragment;
    @Override
    protected int setLayoutView() {
        return R.layout.activity_price_new;
    }
    @Override
    protected void initVariable() {
        cinemaRespModel=new cinema();
        filmId=getIntent().getStringExtra(FILMID);
        selectDate=getIntent().getStringExtra(CinemaActivityNew.SDATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        cinemaRespModel=new cinema();
        filmId=intent.getStringExtra(FILMID);
        selectDate=intent.getStringExtra(CinemaActivityNew.SDATE);
        if(mPriceFragment!=null){
            mPriceFragment.initFragmentVariable(cinemaRespModel,filmId,selectDate);
        }
    }

    @Override
    protected void findViews() {
        setDefaultFragment();
    }
    @Override
    protected void initData() {
    }
    @Override
    protected void setListener() {
    }
    @Override
    public void doClick(View view) {
    }
    private void setDefaultFragment()
    {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mPriceFragment= new PriceFragment_New();
        if(mPriceFragment!=null){
            mPriceFragment.initFragmentVariable(cinemaRespModel,filmId,selectDate);
        }
        transaction.add(R.id.price_new_framelayout, mPriceFragment);
        transaction.commit();
    }
}
