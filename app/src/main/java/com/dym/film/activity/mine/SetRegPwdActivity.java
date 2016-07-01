package com.dym.film.activity.mine;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.UserRespInfo;
import com.dym.film.ui.CustomDialog;

/**
 * Created by wbz360 on 2015/11/12.
 */
@Deprecated
public class SetRegPwdActivity extends BaseActivity
{
    private EditText etPassword;
    private String pwd;
    private CustomDialog customDialog;
    private String mobile;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_set_pwd;
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
        mobile= UserInfo.mobile;
        UserInfo.mobile="";//还原数据
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
                pwd=etPassword.getText().toString();
                if (pwd.length() < 6 || pwd.length() > 20) {
                    ShowMsg("你输入的密码长度不正确！");
                }else{
                    showProgressDialog();
                    setRegisterPwd(mobile,pwd, UserInfo.jid);
                }

                break;

            default:
                this.finish();
                break;
        }
    }
    /*设置登录密码*/
    public void setRegisterPwd(String mobile, String pwd, String jid)
    {
        apiRequestManager.setRegisterPwd(mobile,pwd,jid,new AsyncHttpHelper.ResultCallback<UserRespInfo>()
        {
            @Override
            public void onSuccess(UserRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.userID = data.user.userID;
                UserInfo.mobile = data.user.mobile;
                UserInfo.token = data.user.token;
                UserInfo.name = data.user.name;
                UserInfo.avatar = data.user.avatar;
                UserInfo.createTime = data.user.createTime;
                UserInfo.isLogin = true;
                UserInfo.isNative = true;
                UserInfo.saveUserInfo(mContext);
                ShowMsg("注册成功");
                SetRegPwdActivity.this.finish();
                openActivity(MainActivity.class);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                ShowMsg(message);
                cancelProgressDialog();
            }
        });

    }
    @Override
    protected void onActivityLoading()
    {

    }

    public void showSucessDialog(){
        customDialog = new CustomDialog(this);
        customDialog.setWindowAnimations(R.style.default_dialog_animation);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_register_sucess, null);
        Button button =$(view,R.id.btnRegisterFinish);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                customDialog.dismiss();
                SetRegPwdActivity.this.finish();
//                openActivity(UserInfor.topActivity);
            }
        });
        customDialog.setContentView(view);
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show(Gravity.CENTER);
    }
}
