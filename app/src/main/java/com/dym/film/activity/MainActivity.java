package com.dym.film.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.mine.MyMainActivity;
import com.dym.film.activity.search.SearchActivity;
import com.dym.film.adapter.base.CommonFragmentAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.fragment.FilmReviewHomeFragment;
import com.dym.film.fragment.HomeFragment;
import com.dym.film.fragment.PriceFragment_New;
import com.dym.film.fragment.SharedTicketFragment;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.viewpagerindicator.FixPageIndicator;
import com.dym.film.ui.viewpagerindicator.PageIndicator;
import com.dym.film.ui.viewpagerindicator.scrollbar.ColorBar;
import com.dym.film.utils.DownLoadSoftUpdate;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.HackyViewPager;

import java.util.ArrayList;

import cn.jpush.android.api.JPushInterface;
import de.greenrobot.event.EventBus;

public class MainActivity extends BaseActivity
{

    public final static int PRICEID=0x123;
    // Begin Added By  Liang Ke Jin  给fragment加tag, 因为需要在晒票fragment用到
    public final static String TAG_SHARE_TICKET = "shareTicket";
    public final static String TAG_FILM_REVIEW = "filmReview";
    public final static String TAG_FILM_CHARTS = "filmCharts";
    // End
    public HackyViewPager viewPager;
    //检查更新
    DownLoadSoftUpdate update;
    private FilmReviewHomeFragment mFilmReviewFragment;
    private HomeFragment mHomeFragment;
    private SharedTicketFragment mSharedTicketFragment;
    private PriceFragment_New mPriceFragment;
    private SharedPreferences preference;
    private long exitTime = 0;
    private FixPageIndicator indicator;

    private int[] tabDrawables = {R.drawable.main_btn_home_selector, R.drawable.main_btn_film_review_selector, R.drawable.main_btn_price_selector, R.drawable.main_btn_share_ticket_selector};
    private String[] tabNames = {"影片", "影评", "比价", "晒票"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    // End
    @Override
    protected int setLayoutView()
    {
        // TODO Auto-generated method stub
        return R.layout.activity_main;
    }

    @Override
    protected void initVariable()
    {
        preference = getSharedPreferences("userinfor", Context.MODE_PRIVATE);
    }

    protected void findViews()
    {
        viewPager = (HackyViewPager) findViewById(R.id.viewPager);
        viewPager.toggleLock();
        viewPager.setOffscreenPageLimit(4);
        indicator = (FixPageIndicator) findViewById(R.id.indicator);

    }

    protected void initData()
    {
        update = new DownLoadSoftUpdate(this);
        update.checkVersionThread();

        mFilmReviewFragment = new FilmReviewHomeFragment();
        mHomeFragment = new HomeFragment();
        mSharedTicketFragment = new SharedTicketFragment();
        mPriceFragment = new PriceFragment_New();
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(mHomeFragment);
        fragments.add(mFilmReviewFragment);
        fragments.add(mPriceFragment);
        fragments.add(mSharedTicketFragment);
        viewPager.setAdapter(new CommonFragmentAdapter(getSupportFragmentManager(), fragments));
        indicator.setViewPager(viewPager, 0);
        indicator.setIndicatorAdapter(new PageIndicator.IndicatorAdapter()
        {
            @Override
            public View getIndicatorView(int position)
            {
                TextView textView = (TextView) mInflater.inflate(R.layout.layout_main_tab_item, null);
                textView.setText(tabNames[position]);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, tabDrawables[position], 0, 0);
                return textView;
            }

            @Override
            public void onPageScrolled(View view, int position, float selectPercent)
            {

            }
        });
        ColorBar colorBar = new ColorBar(mContext, mContext.getResources().getColor(R.color.red_color));
//        colorBar.setWidth(DimenUtils.dp2px(mContext, 80));
        indicator.setScrollBar(colorBar);
    }

    public SharedTicketFragment getSharedTicketFragment()
    {
        return mSharedTicketFragment;
    }

    public PriceFragment_New getPriceFragment_New()
    {
        return mPriceFragment;
    }

    protected void setListener()
    {

    }

    public void doClick(View view)
    {

        if (view.getId() == R.id.btnMy) {
            MatStatsUtil.eventClick(mContext,MatStatsUtil.MY,null);
            openActivity(MyMainActivity.class);
            overridePendingTransition(R.anim.bottom_dialog_in, android.R.anim.fade_out);
        }
        else if (view.getId() == R.id.btnToSelectFilm) {
            MatStatsUtil.eventClick(this, "movie_search", "movie_search");
            Intent it=new Intent(this,SearchActivity.class);
            it.putExtra(SearchActivity.SearchTYPE, SearchActivity.FILM);
            startActivity(it);
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if(viewPager.getCurrentItem()!=0){
                viewPager.setCurrentItem(0);
                return true;
            }
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                update.cancelDownLoad();
                finish();
            }

            return true; // 返回true表示执行结束不需继续执行父类按键响应
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onEvent(String selectDate) {
        if(mPriceFragment!=null){
            mPriceFragment.setSelectDate(selectDate);
            mPriceFragment.initCinemaFristData();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!EventBus.getDefault().isRegistered(this))
        EventBus.getDefault().register(this);

        updateJid();
    }

    private static boolean mJidUpdated = false;
    private void updateJid()
    {
        if (mJidUpdated) {
            return;
        }

        String jid = JPushInterface.getRegistrationID(this);
        if (TextUtils.isEmpty(jid) || !UserInfo.isLogin) {
            return;
        }

        final NetworkManager.ReqUpdateJID req = new NetworkManager.ReqUpdateJID();
        req.jid = jid;

        NetworkManager.getInstance().updateJid(req, new HttpRespCallback<NetworkManager.BaseRespModel>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                final HttpRespCallback<NetworkManager.BaseRespModel> callback = this;
                mHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (UserInfo.isLogin) {
                            NetworkManager.getInstance().updateJid(req, callback);
                        }
                    }
                }, 1000 * 60);
            }

            @Override
            public void onRespSuccess(NetworkManager.BaseRespModel model, String body)
            {
                super.onRespSuccess(model, body);
                mJidUpdated = true;
            }
        });
    }

    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        ConfigInfo.region="";
    }

    @Override
    protected void onUserStateChanged(UserState oldState)
    {
        mSharedTicketFragment.onUserStateChanged(oldState);
    }
}



