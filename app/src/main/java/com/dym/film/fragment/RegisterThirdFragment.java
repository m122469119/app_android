package com.dym.film.fragment;


import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.mine.Register2Activity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.UserRespInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterThirdFragment extends BaseFragment
{

    private EditText etPassword;
    private String pwd;
    private String mobile;
    Register2Activity mActivity;
    private ImageButton btnBack;
    private TextView btnSetPwd;

    @Override
    protected void initVariable()
    {
        mActivity= (Register2Activity) getActivity();
    }

    @Override
    protected int setContentView()
    {
         return R.layout.fragment_register_third;
    }

    @Override
    protected void findViews(View view)
    {
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        btnSetPwd = (TextView)view. findViewById(R.id.btnSetPwd);
    }

    @Override
    protected void initData()
    {
        mobile= mActivity.mobile;
    }

    @Override
    protected void setListener()
    {
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mActivity.viewPager.setCurrentItem(1,true);
            }
        });
        btnSetPwd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                pwd=etPassword.getText().toString();
                if (pwd.length() < 6 || pwd.length() > 20) {
                    mActivity.ShowMsg("你输入的密码长度不正确！");
                }else{
                    mActivity. showProgressDialog();
                    setRegisterPwd(mobile,pwd, UserInfo.jid);
                }
            }
        });
    }

    /*设置登录密码*/
    public void setRegisterPwd(String mobile, String pwd, String jid)
    {
        apiRequestManager.setRegisterPwd(mobile,pwd,jid,new AsyncHttpHelper.ResultCallback<UserRespInfo>()
        {
            @Override
            public void onSuccess(UserRespInfo data)
            {
                mActivity.cancelProgressDialog();
                UserInfo.userID = data.user.userID;
                UserInfo.mobile = data.user.mobile;
                UserInfo.token = data.user.token;
                UserInfo.name = data.user.name;
                UserInfo.avatar = data.user.avatar;
                UserInfo.createTime = data.user.createTime;
                UserInfo.isLogin = true;
                UserInfo.isNative = true;
                UserInfo.saveUserInfo(mContext);
                mActivity.ShowMsg("注册成功");
                mActivity.finish();
                mActivity.openActivity(MainActivity.class);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                mActivity.ShowMsg(message);
                mActivity.cancelProgressDialog();
            }
        });

    }
}
