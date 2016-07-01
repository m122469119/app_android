package com.dym.film.activity.mine;

import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;

/**
 * Created by wbz360 on 2015/11/12.
 */
@Deprecated
public class SetRegVCodeActivity extends BaseActivity
{
    private EditText etVcode;
    private String mobile;
    private String vcode;
    private TextView tvTips;
    public  Handler handler = new Handler();
    public  int count = 60;
    public  Runnable runnable;
    private TextView btnVerCode;
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_set_vcode;
    }

    @Override
    protected void initVariable()
    {
    }

    @Override
    protected void findViews()
    {
        etVcode = (EditText) findViewById(R.id.etVcode);
        tvTips=(TextView)findViewById(R.id.tvTips);
        btnVerCode = (TextView) findViewById(R.id.btnGetVcode);
    }

    @Override
    protected void initData()
    {
        mobile= UserInfo.mobile;
        UserInfo.mobile="";//还原数据
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
    }
    @Override
    protected void onActivityLoading()
    {
    }
   
@Override
public void doClick(View view)    {
        switch (view.getId()) {
            case R.id.btnGetVcode:
                if (count!=60) {
                    return;
                }else{
                    btnVerCode.setText("重发("+count + "s)");
                    btnVerCode.setBackgroundResource(R.color.item_bg_color);
                    handler.postDelayed(runnable, 1000);
                    showProgressDialog();
                    getRegisterVcode();
                }

                break;
            case R.id.btnCheckVcode:
                showProgressDialog();
                vcode=etVcode.getText().toString();
                setRegisterVcode();
                break;
            default:
                this.finish();
                break;
        }
    }

    /*验证注册验证码的有效性*/
    public void setRegisterVcode()
    {
        apiRequestManager.setRegisterVcode(mobile, vcode, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.mobile=mobile;
                openActivity(SetRegPwdActivity.class);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
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
                cancelProgressDialog();
                tvTips.setText("验证码已发送至"+ mobile.substring(0,3)+"****"+mobile.substring(7,11));
//                tvTips.setText("验证码已发送至"+mobile);
            }
            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }
    @Override
    protected void onStart()
    {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
