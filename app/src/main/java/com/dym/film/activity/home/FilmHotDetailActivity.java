package com.dym.film.activity.home;

import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.utils.LogUtils;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmHotDetailActivity extends BaseActivity
{
    public final static String KEY_FILM_HOT_DETAIL = "url";
    WebView webView;
    ImageView imgLoadHtmlError;
    String curUrl="";
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_hot_detail;
    }

    @Override
    protected void initVariable()
    {

    }

    @Override
    protected void findViews()
    {
         webView=$(R.id.webView);
         imgLoadHtmlError=$(R.id.imgLoadHtmlError);
    }

    @Override
    protected void initData()
    {
        curUrl=getIntent().getStringExtra(KEY_FILM_HOT_DETAIL);
        LogUtils.i("123", "curUrl"+curUrl);
        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(true);          //支持缩放
        settings.setBuiltInZoomControls(true);  //启用内置缩放装置
        settings.setJavaScriptEnabled(true);    //启用JS脚本

        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                LogUtils.i("123", "onPageStarted");
                showProgressDialog();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
//                Intent intent= new Intent();
//                intent.setAction("android.intent.action.VIEW");
//                Uri content_url = Uri.parse(url);
//                intent.setData(content_url);
//                startActivity(intent);
                LogUtils.i("123", "shouldOverrideUrlLoading");
                LogUtils.i("123", "url"+url);
                webView.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url)
            {
                LogUtils.i("123", "onPageFinished");
                cancelProgressDialog();
                imgLoadHtmlError.setVisibility(View.INVISIBLE);
            }

//            @Override
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
//            {
//                super.onReceivedError(view, request, error);
//                imgLoadHtmlError.setVisibility(View.VISIBLE);
//
//            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                imgLoadHtmlError.setVisibility(View.VISIBLE);
            }
        });

        webView.loadUrl(curUrl);

    }

    @Override
    protected void setListener()
    {
        //点击后退按钮,让WebView后退一页(也可以覆写Activity的onKeyDown方法)
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                        webView.goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }
}
