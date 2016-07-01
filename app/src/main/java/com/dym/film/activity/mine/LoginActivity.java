package com.dym.film.activity.mine;

import android.view.View;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.LoginManager;
import com.dym.film.model.UserRespInfo;

import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class LoginActivity extends BaseActivity
{

    public static boolean isFromMy = true;
    private LoginManager loginManager;
    private String mobile;
    private String pwd;
    private EditText etUserName;
    private EditText etPassword;

    @Override
    protected int setLayoutView()
    {
        // TODO Auto-generated method stub
        return R.layout.activity_login;
    }

    @Override
    protected void initVariable()
    {
        // TODO Auto-generated method stub
        loginManager = new LoginManager(this);
    }

    @Override
    protected void findViews()
    {
        // TODO Auto-generated method stub
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }

    @Override
    protected void initData()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void setListener()
    {
    }

    @Override
    protected void onActivityLoading()
    {
    }

    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.btnLogin:
                startLogin();
                break;
            case R.id.btnToPassword:
                openActivity(UpdatePwd2Activity.class);
                break;
            case R.id.btnToRegister:
                openActivity(Register2Activity.class);
                break;
            case R.id.btnWechatLogin:
                getAuthorInfo(Wechat.NAME);
                break;
            case R.id.btnQQLogin:
                getAuthorInfo(QQ.NAME);
                break;
            case R.id.btnSinaLogin:
                getAuthorInfo(SinaWeibo.NAME);
                break;
            default:
                if (!isFromMy) {
                    openActivity(MainActivity.class);
                }
                this.finish();
                break;
        }
    }
    public void startLogin()
    {
        mobile = etUserName.getText().toString();
        pwd = etPassword.getText().toString();
        apiRequestManager.startLogin(mobile, pwd, new AsyncHttpHelper.ResultCallback<UserRespInfo>()
        {
            @Override
            public void onSuccess(UserRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.userID = data.user.userID;
                UserInfo.mobile = data.user.mobile;
                UserInfo.token = data.user.token;
                UserInfo.name = data.user.name;
                UserInfo.gender = data.user.gender;
                UserInfo.avatar = data.user.avatar;
                UserInfo.createTime = data.user.createTime;
                UserInfo.isLogin = true;
                UserInfo.isNative = true;
                UserInfo.saveUserInfo(mContext);
                if (!isFromMy) {
                    openActivity(MainActivity.class);
                }
                finish();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                ShowMsg(message);
                cancelProgressDialog();
            }
        });
        showProgressDialog();
    }

    public void startPlatformLogin(String platName, String token, String id, String jid)
    {
        showProgressDialog();
        apiRequestManager.startPlatformLogin(platName, token, id, jid, new AsyncHttpHelper.ResultCallback<UserRespInfo>()
        {
            @Override
            public void onSuccess(UserRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.userID = data.user.userID;
                UserInfo.mobile = data.user.mobile;
                UserInfo.token = data.user.token;
                UserInfo.name = data.user.name;
                UserInfo.gender = data.user.gender;
                UserInfo.avatar = data.user.avatar;
                UserInfo.createTime = data.user.createTime;
                UserInfo.isLogin = true;
                UserInfo.isNative = false;
                UserInfo.saveUserInfo(mContext);
                if (!isFromMy) {
                    openActivity(MainActivity.class);
                }
                finish();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                UserInfo.isLogin = false;
                ShowMsg(message);
                cancelProgressDialog();
            }
        });
    }

    public void getAuthorInfo(String platformName)
    {
        showProgressDialog();
        loginManager.getAuthorInfo(platformName, new AsyncHttpHelper.ResultCallback<String[]>()
        {
            @Override
            public void onSuccess(String[] data)
            {
                cancelProgressDialog();
                String platName = data[0];
                String token = data[1];
                String id = data[2];
                startPlatformLogin(platName, token, id, UserInfo.jid);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        cancelProgressDialog();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        isFromMy = true;
    }

    @Override
    public void onBackPressed()
    {
//        super.onBackPressed();
        if (!isFromMy) {
            openActivity(MainActivity.class);
        }
        this.finish();
    }
}
