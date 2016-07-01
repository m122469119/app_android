package com.dym.film.activity.mine;

import android.view.View;
import android.widget.EditText;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.model.BaseRespInfo;

/**
 * Created by wbz360 on 2015/11/12.
 */

public class FeedBackActivity extends BaseActivity
{
    private EditText etOpinion;
    private EditText etMobile;
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_feedback;
    }

    @Override
    protected void initVariable()
    {

    }

    @Override
    protected void findViews()
    {
        etOpinion=$(R.id.etOpinion);
        etMobile=$(R.id.etMobile);
    }

    @Override
    protected void initData()
    {
        etMobile.setText(UserInfo.mobile);
    }

    @Override
    protected void setListener()
    {

    }

    @Override
    public void doClick(View view)
    {
        switch (view.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnSubmitOpinion:
                String mobile=etMobile.getText().toString();
                String opinion=etOpinion.getText().toString();
                if(checkText(mobile,opinion)){
                    showProgressDialog();
                    submitOpinion(mobile,opinion);
                }
                break;

        }
    }

    @Override
    protected void onActivityLoading()
    {

    }

    public boolean checkText(String mobile,String opinion){
//        if (!StringUtils.isPhone(mobile)){
//            ShowMsg("手机号不正确");
//            return false;
//        }
        if (opinion.length()==0){
            ShowMsg("意见不能为空");
            return false;
        } if (opinion.length()>512){
            ShowMsg("字数不能超过512");
            return false;
        }
        return true;
    }

    public void submitOpinion(String mobile,String opinion)
    {
        apiRequestManager.submitOpinion(mobile, opinion, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                ShowMsg("反馈成功");
                cancelProgressDialog();
                finish();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                ShowMsg("失败");
                cancelProgressDialog();
            }
        });

    }
}
