package com.dym.film.activity.base;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.mine.LoginActivity;
import com.dym.film.activity.mine.Register2Activity;
import com.dym.film.adapter.base.CommonPagerAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.BannerListInfo;
import com.dym.film.utils.DimenUtils;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SplashActivity extends FragmentActivity
{
    private SharedPreferences preference;
    private boolean isFirstIn;
    private LayoutInflater mInflater;
    private ArrayList<View> viewData;
    private PagerAdapter adapter;

    private String splashUrl = ConfigInfo.BASE_URL+"/app/main/banner/list?category=4";
    private ImageView imgSplash;
    private TextView btnPassSplash;
    private Runnable delayRunnable;
    private RelativeLayout laySplash;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initVariable();
        findViews();
        initData();
    }

    protected void findViews()
    {
        btnPassSplash = (TextView) findViewById(R.id.btnPassSplash);
        imgSplash = (ImageView) findViewById(R.id.imgSplash);
        laySplash = (RelativeLayout) findViewById(R.id.laySplash);
        imgSplash.setMaxHeight(DimenUtils.getScreenHeight(getApplicationContext()));
        imgSplash.setMaxWidth(DimenUtils.getScreenWidth(getApplicationContext()));

        btnPassSplash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                imgSplash.removeCallbacks(delayRunnable);
                loadMain();
            }
        });
    }

    public void initVariable()
    {
        /*广告图片高宽比例控制在1.5*/
        mInflater = LayoutInflater.from(this);
        preference = getSharedPreferences(ConfigInfo.PREFERENCE_NAME_USER, Context.MODE_PRIVATE);
        isFirstIn = preference.getBoolean("isFirstIn", true);

        delayRunnable = new Runnable()
        {

            @Override
            public void run()
            {
                loadMain();
            }
        };
    }

    public void initData()
    {
        loadData();//每次启动都会下载最新启动页
        if (isFirstIn) {
            SharedPreferences.Editor edit = preference.edit();
            edit.putBoolean("isFirstIn", false);
            edit.commit();
            startWelcome();
        }
        else {
            imgSplash.postDelayed(delayRunnable, 3000);//延迟3s再进入主页
        }
    }


    public void loadData()
    {
        // 先显示上一次的图
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        final String lastUrl = preferences.getString(KEY_LAST_URL, "");
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true) // default 设置下载的图片是否缓存在SD卡中
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT) // default
                .bitmapConfig(Bitmap.Config.RGB_565) // default 设置图片的解码类型
                .build();
        ImageLoader.getInstance().displayImage(lastUrl, imgSplash,options);

        Type type = new TypeToken<BannerListInfo>() {}.getType();
        AsyncHttpHelper.getRequest(this, splashUrl, type, new AsyncHttpHelper.ResultCallback<BannerListInfo>()
        {

            @Override
            public void onSuccess(BannerListInfo data)
            {
                String imgUrl = data.banners != null && !data.banners.isEmpty() ? data.banners.get(0).img : "";
                String url = QCloudManager.urlImage2(imgUrl, CommonManager.dpToPx(300));

                if (!TextUtils.isEmpty(url) && !imgUrl.equals(lastUrl)) {
                    loadImage(imgUrl);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
            }
        });

    }

    private final static String KEY_LAST_URL = "LastUrl";
    public void loadImage(final String imgUrl)
    {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(KEY_LAST_URL, imgUrl);
        editor.apply();

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(false).build();
        ImageLoader.getInstance().displayImage(imgUrl, imgSplash, options);
//        new Thread(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                try {
//                    URL url = new URL(imgUrl);
//                    HttpURLConnection conn = null;
//                    conn = (HttpURLConnection) url.openConnection();
//                    conn.setConnectTimeout(5 * 1000);
//                    conn.setRequestMethod("GET");
//                    InputStream inStream = conn.getInputStream();
//                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                        if (inStream != null) {
//                            FileUtils.writeStreamFile(inStream, new File(filePath));
//                        }
//                    }
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }).start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
//        ImageLoader.getInstance().cancelDisplayTask(imgSplash);
        imgSplash.removeCallbacks(delayRunnable);
    }

    protected void loadMain()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void toLogin()
    {
        Intent intent = new Intent(this, LoginActivity.class);
        LoginActivity.isFromMy = false;
        startActivity(intent);
        finish();
    }

    protected void toRegister()
    {
        Intent intent = new Intent(this, Register2Activity.class);
        Register2Activity.isFromMy = false;
        startActivity(intent);
        finish();
    }

    public void startWelcome()
    {
        ViewPager viewPager = (ViewPager) mInflater.inflate(R.layout.layout_guide, null);
//		Animation anim ation2 = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.fade_in);
//		viewPager.startAnimation(animation2);
        laySplash.addView(viewPager);
        viewData = new ArrayList<View>();
        View guideView1 = mInflater.inflate(R.layout.guide_item_first, null);
        viewData.add(guideView1);
        View guideView2 = mInflater.inflate(R.layout.guide_item_second, null);
        viewData.add(guideView2);
        View guideView3 = mInflater.inflate(R.layout.guide_item_last, null);
        viewData.add(guideView3);
        final LinearLayout layGuideAction = (LinearLayout) guideView3.findViewById(R.id.layGuideAction);
        Button btnToLogin = (Button) guideView3.findViewById(R.id.btnToLogin);
        Button btnToRegister = (Button) guideView3.findViewById(R.id.btnToRegister);
        TextView btnToMain = (TextView) guideView3.findViewById(R.id.btnToMain);
        btnToLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toLogin();
            }
        });
        btnToRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toRegister();
            }
        });
        btnToMain.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                loadMain();
            }
        });
        adapter = new CommonPagerAdapter(viewData);
        viewPager.setAdapter(adapter);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                if (position == viewData.size() - 1) {
                    Animation animation2 = AnimationUtils.loadAnimation(SplashActivity.this, R.anim.option_entry_from_bottom);
                    animation2.setDuration(800);
                    layGuideAction.setVisibility(View.VISIBLE);
                    layGuideAction.startAnimation(animation2);
                }
                else {
                    layGuideAction.setVisibility(View.INVISIBLE);
                }

            }

        });
    }

}
