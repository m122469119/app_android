package com.dym.film.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class HtmlActivity extends BaseActivity
{
    public final static String KEY_HTML_URL = "url";
    public final static String KEY_HTML_ACTION = "action";//1资讯，2榜单banner3,兑换劵
    private WebView webView;
    private String curUrl = "";
    private int action = 0;
    private TextView tvTitle;
    private SwipeRefreshLayout mRefreshLayout;
    private String title="";
    private String description="";
    private ImageButton btnShareHtml;
    private ShareManager shareManager;
    private String imageUrl="";
    private String sharedUrl;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_html;
    }

    @Override
    protected void initVariable()
    {
        shareManager=new ShareManager(this);
    }

    @Override
    protected void findViews()
    {
//        webView = $(R.id.webView);
        showTopBar();
        tvTitle = getTitleView();
        btnShareHtml = getRightButton();
        btnShareHtml.setImageResource(R.drawable.ic_share_selector);

        webView=new WebView(getApplicationContext());
        webView.setBackgroundColor(0x00000000); // 设置背景色
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                webView.loadUrl(curUrl);
            }
        });
        mRefreshLayout.addView(webView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }


    private void setActionTitle(int action)
    {
        if (action == 1) {
            tvTitle.setText("热点详情");
//            title="公证电影|热点详情";
        }
        else if (action == 2) {
            tvTitle.setText("");
//            btnShareHtml.setVisibility(View.INVISIBLE);
//            title="公证电影";
        }
        else if (action == 3) {
            tvTitle.setText("兑换劵");
            btnShareHtml.setVisibility(View.INVISIBLE);

        } else if (action == 4) {
            tvTitle.setText("关于我们");
            btnShareHtml.setVisibility(View.INVISIBLE);

        }else if (action == 5) {
            tvTitle.setText("用户协议");
            btnShareHtml.setVisibility(View.INVISIBLE);

        }

    }

    @Override
    protected void initData()
    {
        Intent intent=getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            curUrl = uri.getQueryParameter(KEY_HTML_URL);
            sharedUrl = curUrl.substring(0,curUrl.indexOf("?"));
            action = 1;
            imageUrl = uri.getQueryParameter("imageUrl");
            title = uri.getQueryParameter("title");
        }
        else {
            curUrl = intent.getStringExtra(KEY_HTML_URL);
            if (curUrl.indexOf("?")!=-1) {
                sharedUrl = curUrl.substring(0, curUrl.indexOf("?"));
            }
            LogUtils.i("123",sharedUrl);
            action = intent.getIntExtra(KEY_HTML_ACTION, 0);
            if (intent.hasExtra("imageUrl")) {
                imageUrl = intent.getStringExtra("imageUrl");
            }
            if (intent.hasExtra("title")) {
                title = intent.getStringExtra("title");
            }
        }
        setActionTitle(action);
        LogUtils.i("123", "curUrl" + curUrl);

        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);          //支持缩放
        settings.setBuiltInZoomControls(false);  //隐藏内置缩放装置
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);    //启用JS脚本

        if (isNetworkConnected()){
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                LogUtils.i("123", "onPageStarted");
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                LogUtils.i("123", "shouldOverrideUrlLoading");
                LogUtils.i("123", "url" + url);
                webView.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url)
            {
                LogUtils.i("123", "onPageFinished");
                CommonManager.setRefreshingState(mRefreshLayout, false);
                if (isNetworkConnected()) {
                    onActivityLoadingSuccess();
                    if (action == 2 && TextUtils.isEmpty(title)) {
                        title = webView.getTitle();
//                        tvTitle.setText(title);
                    }
                    LogUtils.i("123", "title" + title);
                    LogUtils.i("123", "description" + description);

                }
                else {
                    onActivityLoadingFailed();
                }

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingFailed();
                LogUtils.i("123", "onReceivedError");
            }
        });
        startActivityLoading();

    }

    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        webView.loadUrl(curUrl);
    }

    @Override
    protected void setListener()
    {
        //点击后退按钮,让WebView后退一页(也可以覆写Activity的onKeyDown方法)
        webView.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                        webView.goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
            }
        });

        btnShareHtml.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                shareManager.setWebUrl(sharedUrl);
                shareManager.setTitle(title);
                shareManager.setTitleUrl(sharedUrl);
//                shareManager.setText(description);
                shareManager.setImageUrl(imageUrl);
                shareManager.showShareDialog(HtmlActivity.this);
            }
        });
    }

    // 网络状态
    public boolean isNetworkConnected()
    {

        ConnectivityManager mConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }

        return false;
    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        webView.clearCache(true);
        webView.removeAllViews();
        mRefreshLayout.removeAllViews();
        webView.destroy();
        webView = null;
    }
}
