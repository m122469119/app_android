package com.dym.film.activity.mine;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.ui.CustomDialog;

/**
 * Created by wbz360 on 2015/11/12.
 */
@Deprecated
public class SetNewPwdActivity extends BaseActivity
{
    private EditText etPassword;
    private String pwd;
    private CustomDialog customDialog;
    public static boolean isLoginOrUpdate=true;//true为登录
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_set_new_pwd;
    }

    @Override
    protected void initVariable()
    {
    }

    @Override
    protected void findViews()
    {
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    @Override
    protected void initData()
    {

    }

    @Override
    protected void setListener()
    {
    }

    @Override
    public void doClick(View view)
    {

        switch (view.getId()) {
            case R.id.btnSetPwd:
                pwd = etPassword.getText().toString();
                if (pwd.length() < 6 || pwd.length() > 20) {
                    ShowMsg("你输入的密码长度不正确！");
                }
                else {
                    showProgressDialog();
                    setNewPwd(UserInfo.mobile, pwd);
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
    /*设置登录密码*/
    public void setNewPwd(String mobile, String pwd)
    {
        apiRequestManager.setNewPwd(mobile, pwd, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                ShowMsg("修改成功");
                if (isLoginOrUpdate){
                    openActivity(LoginActivity.class);
                }else {
                    openActivity(MySetActivity.class);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }

    public void showSucessDialog()
    {
        customDialog = new CustomDialog(this);
        customDialog.setWindowAnimations(R.style.default_dialog_animation);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_register_sucess, null);
        Button button = $(view, R.id.btnRegisterFinish);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                customDialog.dismiss();
//                openActivity(UserInfor.topActivity);
                finish();
            }
        });
        customDialog.setContentView(view);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show(Gravity.CENTER);
    }
}
