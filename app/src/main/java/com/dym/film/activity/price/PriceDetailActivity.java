package com.dym.film.activity.price;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.XWebView;

import java.lang.reflect.Method;

public class PriceDetailActivity extends BaseActivity {

    private RelativeLayout price_detail_layout;
    private WebView xwebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_price_detail;
    }

    @Override
    protected void initVariable() {

    }

    @Override
    protected void findViews() {

        price_detail_layout=$(R.id.price_detail_layout);
        xwebView=new WebView(this);

//        xwebView.getSettings().setBuiltInZoomControls(false);
//        SpeedFun();

//        xwebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        xwebView.getSettings().setBlockNetworkImage(true);
//        xwebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        xwebView.getSettings().setAllowFileAccess(true);
//        xwebView.getSettings().setAppCacheEnabled(true);
//        xwebView.getSettings().setSaveFormData(false);
//        xwebView.getSettings().setLoadsImagesAutomatically(true);
//        // http请求的时候，模拟为火狐的UA会造成实时公交那边的页面存在问题，所以模拟iPhone的ua来解决这个问题
//        String user_agent =
//                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/124 (KHTML, like Gecko) Safari/125.1";
//        xwebView.getSettings().setUserAgentString(user_agent);
//        /* Enable zooming */
//        xwebView.getSettings().setSupportZoom(false);
//        xwebView.loadUrl("http://www.baidu.com");

        xwebView.getSettings().setJavaScriptEnabled(true);
        //HTML5 中有一个 DOM Storage 机制，在客户端默认的存储空间有限（比如保存键值对），打开这个选项，就提供了更大的容量。
        xwebView.getSettings().setDomStorageEnabled(true);
        xwebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        xwebView.setLayoutParams(params);
        price_detail_layout.addView(xwebView);
        xwebView.loadUrl("http://piao.huo.com/wap2/cinema/schedule?cinema_id=646");
        xwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                LogUtils.e(TAG, "Page onPageStarted....");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogUtils.e(TAG, "Page Loading Finished....");
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogUtils.e(TAG, "Page onReceivedError....");
            }

        });
        xwebView.setWebChromeClient(new SimpleChromeViewClient());
//        xwebView.loadUrl("http://m.maoyan.com/#tmp=showtime&cinemaid=220");
    }

    protected class SimpleChromeViewClient extends WebChromeClient
    {

        public void onProgressChanged(WebView view, int newProgress)
        {
            LogUtils.e(TAG, "onProgressChanged: " + String.valueOf(newProgress));
        }

        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            LogUtils.e(TAG, "onJsAlert: " + url + "  Msg: " + message + "  Result: " + result.toString());
            return false;
        }

        public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
        {
            LogUtils.e(TAG, "onJsConfirm: " + url + "  Msg: " + message + "  Result: " + result.toString());
            return false;
        }

        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
        {
            LogUtils.e(TAG, "onJsPrompt: " + url + "  Msg: " + message + "  Result: " + result.toString());
            return false;
        }
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

    private void SpeedFun(){
        try
        {
            //禁用硬件加速
            Method method = WebView.class.getMethod("setLayerType", int.class, Paint.class);
            method.setAccessible(true);
            method.invoke(xwebView, 1, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(xwebView!=null)
                xwebView.getSettings().setBlockNetworkImage(false);
            }
        }, 1000);
    }
    public void onBackEvent(View view)
    {
        xwebView.destroy();
        xwebView=null;
        finish();
    }
}
