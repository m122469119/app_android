package com.dym.film.activity.mine;

import android.view.View;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.utils.RegexUtils;
@Deprecated
public class UpdatePwdActivity extends BaseActivity
{
    private EditText etMobile;
    private String mobile;
    @Override
    protected int setLayoutView()
    {
        // TODO Auto-generated method stub
        return R.layout.activity_update_pwd;
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

    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.btnGetVcode:
                mobile=etMobile.getText().toString();
                if(RegexUtils.isMobilePhoneNumber(mobile)){
                    showProgressDialog();
                    getUpdatePwdVcode();
                }else{
                    ShowMsg("你输入的手机号不正确");
                }
                break;
            default:
                this.finish();
                break;
        }
    }

    @Override
    protected void onActivityLoading()
    {
    }

    public void getUpdatePwdVcode(){
        apiRequestManager.getUpdatePwdVcode(mobile, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.mobile=mobile;
                openActivity(SetUpdatePwdVCodeActivity.class);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }


}
