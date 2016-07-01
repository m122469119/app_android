package com.dym.film.activity.mine;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.utils.RegexUtils;

@Deprecated
public class RegisterActivity extends BaseActivity
{
    private EditText etMobile;
    private String mobile;
    public  static boolean isFromMy=true;
    @Override
    protected int setLayoutView()
    {
        // TODO Auto-generated method stub
        return R.layout.activity_register;
    }

    @Override
    protected void initVariable()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void findViews()
    {
        etMobile = (EditText) findViewById(R.id.etMobile);
    }

    @Override
    protected void initData()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void setListener()
    {
        // TODO Auto-generated method stub
    }
    @Override
    protected void onActivityLoading()
    {

    }
    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.btnGetVcode:
                mobile=etMobile.getText().toString();
                if(RegexUtils.isMobilePhoneNumber(mobile)){
                    showProgressDialog();
                    getRegisterVcode();
                }else{
                    ShowMsg("你输入的手机号不正确");
                }
                break;
            case R.id.btnProtocol:
                String url= ConfigInfo.BASE_URL+"/app/license";
                Intent intent = new Intent(RegisterActivity.this, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,5);
                startActivity(intent);
                break;
            default:
                if (!isFromMy){
                    openActivity(MainActivity.class);
                }
                this.finish();
                break;
        }
    }
    /*获取注册验证码请求*/
    public void getRegisterVcode(){
        apiRequestManager.getRegisterVcode(mobile, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                UserInfo.mobile=mobile;
                openActivity(SetRegVCodeActivity.class);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {

            }
        });

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        isFromMy=true;
    }
    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
        if (!isFromMy){
            openActivity(MainActivity.class);
        }
        this.finish();
    }

}
