package com.dym.film.fragment;


import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.mine.UpdatePwd2Activity;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdatePwdThirdFragment extends BaseFragment
{

    private EditText etPassword;
    private String pwd;
    private String mobile;
    UpdatePwd2Activity mActivity;
    private ImageButton btnBack;
    private TextView btnSetPwd;
    @Override
    protected void initVariable()
    {
        mActivity= (UpdatePwd2Activity) getActivity();
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_update_pwd_third;
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
                    setNewPwd(mobile, pwd);
                }
            }
        });
    }
    /*设置登录密码*/
    public void setNewPwd(String mobile, String pwd)
    {
        apiRequestManager.setNewPwd(mobile, pwd, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                mActivity.cancelProgressDialog();
                mActivity.ShowMsg("修改成功");
                mActivity.finish();
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
