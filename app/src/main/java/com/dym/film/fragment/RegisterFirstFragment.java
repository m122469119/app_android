package com.dym.film.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.mine.Register2Activity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.utils.RegexUtils;


public class RegisterFirstFragment extends BaseFragment
{
    private EditText etMobile;
    private String mobile;
    Register2Activity mActivity;
    private ImageButton btnBack;
    private TextView btnGetVcode;
    private TextView btnProtocol;

    @Override
    protected void initVariable()
    {
        mActivity= (Register2Activity) getActivity();
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_register_first;
    }

    @Override
    protected void findViews(View view)
    {
        etMobile = (EditText)view.findViewById(R.id.etMobile);
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        btnGetVcode = (TextView)view.findViewById(R.id.btnGetVcode);
        btnProtocol= (TextView)view.findViewById(R.id.btnProtocol);
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
                if (!mActivity.isFromMy){
                    mActivity.openActivity(MainActivity.class);
                }
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
                    getRegisterVcode();
                }else{
                    mActivity.ShowMsg("你输入的手机号不正确");
                }

            }
        });
        btnProtocol.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String url= ConfigInfo.BASE_URL+"/app/license";
                Intent intent = new Intent(mActivity, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,5);
                mActivity.startActivity(intent);
            }
        });
    }

    /*获取注册验证码请求*/
    public void getRegisterVcode(){
        mActivity.showProgressDialog();
        apiRequestManager.getRegisterVcode(mobile, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
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
                mActivity.cancelProgressDialog();
            }
        });

    }


}
