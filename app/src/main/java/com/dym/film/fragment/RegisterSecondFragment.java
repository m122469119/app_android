package com.dym.film.fragment;


import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.mine.Register2Activity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterSecondFragment extends BaseFragment
{

    private EditText etVcode;
    private String mobile;
    private String vcode;
    private TextView tvTips;
    public  Handler handler = new Handler();
    public  int count = 60;
    public  Runnable runnable;
    private TextView btnVerCode;
    Register2Activity mActivity;
    private ImageButton btnBack;
    private TextView btnCheckVcode;

    @Override
    protected void initVariable()
    {
        mActivity= (Register2Activity) getActivity();
    }

    @Override
    protected int setContentView()
    {
        return  R.layout.fragment_register_second;
    }

    @Override
    protected void findViews(View view)
    {
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        etVcode = (EditText) view.findViewById(R.id.etVcode);
        tvTips=(TextView)view.findViewById(R.id.tvTips);
        btnVerCode = (TextView)view. findViewById(R.id.btnGetVcode);
        btnCheckVcode= (TextView)view. findViewById(R.id.btnCheckVcode);
    }

    @Override
    protected void initData()
    {
        mobile= mActivity.mobile;
        tvTips.setText("验证码已发送至"+ mobile.substring(0,3)+"****"+mobile.substring(7,11));
        runnable = new Runnable() {

            @Override
            public void run() {
                // handler自带方法实现定时
                count--;
                //Logi("123", count + "");
                if (btnVerCode!=null) {
                    btnVerCode.setText("重发("+count + "s)");
                }
                if (count==0) {
                    handler.removeCallbacks(this);
                    count=60;
                    btnVerCode.setText("获取验证码");
                    btnVerCode.setBackgroundResource(R.color.red_color);
                }else {
                    handler.postDelayed(this, 1000);
                }

            }

        };
        handler.postDelayed(runnable, 0);
        btnVerCode.setBackgroundResource(R.color.item_bg_color);
    }

    @Override
    protected void setListener()
    {
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mActivity.viewPager.setCurrentItem(0,true);
            }
        });
        btnVerCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (count!=60) {
                    return;
                }else{
                    btnVerCode.setText("重发("+count + "s)");
                    btnVerCode.setBackgroundResource(R.color.item_bg_color);
                    handler.postDelayed(runnable, 1000);
                    mActivity.showProgressDialog();
                    getRegisterVcode();
                }
            }
        });
        btnCheckVcode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                vcode=etVcode.getText().toString();
                if (TextUtils.isEmpty(vcode)){
                    mActivity.ShowMsg("验证码不能为空");
                    return;
                }
                mActivity.showProgressDialog();
                setRegisterVcode();
            }
        });
    }

    /*验证注册验证码的有效性*/
    public void setRegisterVcode()
    {
        apiRequestManager.setRegisterVcode(mobile, vcode, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                mActivity.cancelProgressDialog();
                mActivity.mobile=mobile;
                mActivity.viewPager.setCurrentItem(2,true);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                mActivity.cancelProgressDialog();
            }
        });

    }
    /*获取注册验证码请求*/
    public void getRegisterVcode(){
        apiRequestManager.getRegisterVcode(mobile, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                mActivity.cancelProgressDialog();
                tvTips.setText("验证码已发送至"+ mobile.substring(0,3)+"****"+mobile.substring(7,11));
//                tvTips.setText("验证码已发送至"+mobile);
            }
            @Override
            public void onFailure(String errorCode, String message)
            {
                mActivity.cancelProgressDialog();
            }
        });

    }
}
