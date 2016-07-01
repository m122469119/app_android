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
import com.dym.film.utils.RegexUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdatePwdFirstFragment extends BaseFragment
{

    private EditText etMobile;
    private String mobile;
    private ImageButton btnBack;
    private TextView btnGetVcode;
    private UpdatePwd2Activity mActivity;

    @Override
    protected void initVariable()
    {
        mActivity= (UpdatePwd2Activity) getActivity();
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_update_pwd_first;
    }

    @Override
    protected void findViews(View view)
    {
        etMobile = (EditText) view.findViewById(R.id.etMobile);
        btnBack= (ImageButton) view.findViewById(R.id.btnBack);
        btnGetVcode= (TextView) view.findViewById(R.id.btnGetVcode);
    }

    @Override
    protected void initData()
    {

    }

    @Override
    protected void setListener()
    {
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mActivity.finish();
            }
        });
        btnGetVcode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mobile=etMobile.getText().toString();
                if(RegexUtils.isMobilePhoneNumber(mobile)){
                    mActivity.showProgressDialog();
                    getUpdatePwdVcode();
                }else{
                    mActivity.ShowMsg("你输入的手机号不正确");
                }

            }
        });
    }
    public void getUpdatePwdVcode(){
        apiRequestManager.getUpdatePwdVcode(mobile, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                mActivity.cancelProgressDialog();
                mActivity.mobile=mobile;
                mActivity.viewPager.setCurrentItem(1,true);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                mActivity. cancelProgressDialog();
            }
        });

    }
}
