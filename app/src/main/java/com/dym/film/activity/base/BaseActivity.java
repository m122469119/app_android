package com.dym.film.activity.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.common.http.OkHttpUtils;
import com.dym.film.manager.ApiRequestManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.LogUtils;

public abstract class BaseActivity extends BaseViewCtrlActivity
{

    public ApiRequestManager apiRequestManager;
    public Context mContext;
    public Activity mActivity;
    public LayoutInflater mInflater;

    private RelativeLayout layTopBar;
    private TextView tvTitle;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private FrameLayout layBody;
    private ViewStub stubLoadingFailed;
    private FrameLayout layLoadingFailed;
    private LinearLayout layClickReload;
    private ProgressBar loadingProgress;
    private View contentView;
    private CustomDialog customDialog;
    private boolean isFirstLoading = false;

    /* 子类使用的时候无需再次调用onCreate(),如需要加载其他方法可重写该方法 */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        LogUtils.e("getSimpleName", this.getClass().getSimpleName().toString());
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_base);
        initBase();
        addContentView();
        initVariable();
        findViews();
        initData();
        setListener();

    }


    private void initBase()
    {
        // TODO Auto-generated method stub
        mContext = getApplicationContext();
        mActivity=this;
        apiRequestManager = ApiRequestManager.getInstance(mContext);
        setTranslucentStatusBar(R.color.main_title_color);
        mInflater = getLayoutInflater();
        // topbar相关
        layTopBar = (RelativeLayout) findViewById(R.id.layTopBar);
        tvTitle = (TextView) findViewById(R.id.tvTopBarTitle);
        btnLeft = (ImageButton) findViewById(R.id.btnLeft);
        btnRight = (ImageButton) findViewById(R.id.btnRight);

        // 内容区
        layBody = (FrameLayout) findViewById(R.id.layBody);
        stubLoadingFailed = (ViewStub) findViewById(R.id.stubLoadingFailed);

        hideTopBar();
    }

    public void hideTopBar()
    {
        layTopBar.setVisibility(View.GONE);
    }

    public void showTopBar()
    {
        layTopBar.setVisibility(View.VISIBLE);
    }

    /**
     * 得到左边的按钮
     */
    public ImageButton getLeftButton()
    {
        btnLeft.setVisibility(View.VISIBLE);
        return btnLeft;
    }

    /**
     * 得到右边的按钮
     */
    public ImageButton getRightButton()
    {
        btnRight.setVisibility(View.VISIBLE);
        return btnRight;
    }

    /**
     * 设置标题
     */
    public void setTitle(String title)
    {
        tvTitle.setText(title);
    }

    /**
     * 获取标题
     */
    public TextView getTitleView()
    {
        return tvTitle;
    }

    /**
     * 设置自定义view
     */
    public void setCustomTopBar(int resId)
    {
        View view = mInflater.inflate(resId, null);
        layTopBar.removeAllViews();
        layTopBar.addView(view);
    }

    public boolean isTranslucentStatusBar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            return true;
        }
        return false;
    }

    public void setTranslucentStatusBar(int ResId)
    {
        View customStatusBarView = $(R.id.customStatusBarView);
        if (customStatusBarView != null) {
            if (isTranslucentStatusBar()) {
                customStatusBarView.setVisibility(View.VISIBLE);
                customStatusBarView.setBackgroundResource(ResId);
            }
        }
    }

    /*如果首次加载先失败，就显示失败界面，
    对于一个界面多个接口，之间不会有影响
    isFirstLoading=false;
    */
    public void onActivityLoadingFailed()
    {
        if (layLoadingFailed == null || !isFirstLoading) {
            return;
        }
        layClickReload.setVisibility(View.VISIBLE);
        loadingProgress.setVisibility(View.INVISIBLE);
        isFirstLoading = false;
    }

    /*如果首次加载先成功，就显示成功界面，
       对于一个界面多个接口，之间不会有影响
       isFirstLoading=false;
       */
    public void onActivityLoadingSuccess()
    {

        if (layLoadingFailed == null || !isFirstLoading) {
            return;
        }
        layLoadingFailed.setVisibility(View.GONE);
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
        isFirstLoading = false;
    }

    public void startActivityLoading()
    {

        if (layLoadingFailed == null) {
            layLoadingFailed = (FrameLayout) stubLoadingFailed.inflate();
            layClickReload = (LinearLayout) layLoadingFailed.findViewById(R.id.layClickReload);
            loadingProgress = (ProgressBar) layLoadingFailed.findViewById(R.id.loadingProgress);
            layClickReload.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    // TODO Auto-generated method stub
                    startActivityLoading();
                }
            });
        }
        layLoadingFailed.setVisibility(View.VISIBLE);
        layClickReload.setVisibility(View.INVISIBLE);
        loadingProgress.setVisibility(View.VISIBLE);

        if (contentView != null) {
            contentView.setVisibility(View.INVISIBLE);
        }
        isFirstLoading = true;
        onActivityLoading();

    }

    /***
     * 设置内容区域
     */
    public void addContentView()
    {
        int resId = setLayoutView();
        View layContentView = mInflater.inflate(resId, null);
        if (layContentView == null) {
            return;
        }
        layContentView.setBackgroundColor(0x00000000);
        contentView = layContentView.findViewById(R.id.content);
        if (contentView == null) {
            contentView = layContentView;
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layContentView.setLayoutParams(layoutParams);
        layBody.addView(layContentView, 0);
    }

    /**
     * 得到内容的View
     *
     * @return
     */
    public View getContentView()
    {

        return contentView;
    }

    protected abstract int setLayoutView();

    protected abstract void initVariable();

    protected abstract void findViews();

    protected abstract void initData();

    protected abstract void setListener();

    public abstract void doClick(View view);

    protected void onActivityLoading()
    {

    }

    @Override
    protected void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();
        LogUtils.v(this, "----- onStart -----");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtils.v(this, "----- onResume -----");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LogUtils.v(this, "----- onPause -----");
    }


    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        // 在activity停止的同时设置停止请求, 停止线程请求回调
        OkHttpUtils.getInstance().cancelTag(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LogUtils.v(this, "----- onDestroy -----");
    }

    @SuppressWarnings("unchecked")
    protected final <T extends View> T $(@IdRes int id)
    {
        return (T) (findViewById(id));
    }

    @SuppressWarnings("unchecked")
    protected final <T extends View> T $(@NonNull View view, @IdRes int id)
    {
        return (T) (view.findViewById(id));
    }

    public void openActivity(Class<?> pClass)
    {
        Intent intent = new Intent();
        intent.setClass(this, pClass);
        super.startActivity(intent);
    }

    public void openActivityForResult(Class<?> pClass, int requestCode)
    {
        Intent intent = new Intent();
        intent.setClass(this, pClass);
        super.startActivityForResult(intent, requestCode);
    }

    public void ShowMsg(String pMsg)
    {
        Toast.makeText(this, pMsg, Toast.LENGTH_SHORT).show();
    }

    public AlertDialog showAlertDialog(String title, String message, DialogInterface.OnClickListener clickListener)
    {
        return new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("yes", clickListener).setNegativeButton("no", null).show();
    }

    public void showProgressDialog()
    {
        if (customDialog == null) {
            customDialog = new CustomDialog(this);
            customDialog.setWindowAnimations(R.style.default_dialog_animation);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_progress_wheel, null);
            customDialog.setContentView(view);
        }
        customDialog.show();
    }

    public void cancelProgressDialog()
    {
        if (customDialog != null) {
            customDialog.dismiss();
        }

    }

}
