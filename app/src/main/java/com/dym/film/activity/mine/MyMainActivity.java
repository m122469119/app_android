package com.dym.film.activity.mine;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.sharedticket.UserMessageActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.model.MyBaseInfo;
import com.dym.film.ui.CircleImageView;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class MyMainActivity extends BaseActivity
{
    private TextView tvName;
    private TextView tvRegisterDate;
    private TextView tvMsgCount;
    private View viewMsgRedCircle;
    private TextView tvAttentionCount;
    private CircleImageView imgPhoto;
    private CircleImageView imgAttentionPhoto;
    private TextView tvMyTicketCount;
    private ImageView imgMyTicketSmall;
    private TextView tvShareTicketCount;
    private View viewShareTicketRedCircle;
    private TextView tvLoginTips;
    private LinearLayout layThreeShareTicket;
    public  static boolean hasBaseInfo = false;
    private LayoutInflater inflater;
    private View viewSex;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_my_main;
    }

    @Override
    protected void initVariable()
    {
         inflater=getLayoutInflater();
    }

    @Override
    protected void findViews()
    {
        imgPhoto=$(R.id.imgPhoto);
        viewSex=$(R.id.viewSex);
        tvName = $(R.id.tvName);
        tvRegisterDate = $(R.id.tvRegisterDate);
        tvMsgCount = $(R.id.tvMsgCount);
        viewMsgRedCircle = $(R.id.viewMsgRedCircle);
        tvAttentionCount = $(R.id.tvAttentionCount);
        imgAttentionPhoto = $(R.id.imgAttentionPhoto);
        tvMyTicketCount = $(R.id.tvMyTicketCount);
        imgMyTicketSmall = $(R.id.imgMyTicketSmall);
        tvShareTicketCount = $(R.id.tvShareTicketCount);
        viewShareTicketRedCircle = $(R.id.viewShareTicketRedCircle);
        tvLoginTips = $(R.id.tvLoginTips);
        layThreeShareTicket=$(R.id.layThreeShareTicket);

    }

    @Override
    protected void initData()
    {
        hasBaseInfo=false;

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        LogUtils.i("123","onStart-=myset");
//        setMyBanner();
//        setMyInfor();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LogUtils.i("123","onResume-=myset");
        setMyBanner();
        setMyInfor();
    }

    @Override
    protected void setListener()
    {
//        btnLogin.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                openActivity(LoginActivity.class);
//            }
//        });

    }

    @Override
    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.btnToSetting:
                openActivity(MySetActivity.class);
                break;
            case R.id.layToMyMsg:
                // Added By  Liang Ke Jin 2015/11/20 打开消息界面
                if (UserInfo.isLogin){
                    viewMsgRedCircle.setVisibility(View.INVISIBLE);
                    openActivity(UserMessageActivity.class);
                }else{
                    openActivity(LoginActivity.class);
                }
                // End
                break;
            case R.id.layToMyTicket:
                if (UserInfo.isLogin){
                    openActivity(MyTicketActivity.class);
                }else{
                    openActivity(LoginActivity.class);
                }

                break;
            case R.id.layToMyAttention:
                if (UserInfo.isLogin){
                    openActivity(MyAttentionActivity.class);
                }else{
                    openActivity(LoginActivity.class);
                }
                break;
            case R.id.layToMyShareTicket:
                if (UserInfo.isLogin){
                    openActivity(MyShareTicketActivity.class);
                    viewShareTicketRedCircle.setVisibility(View.INVISIBLE);
                }else{
                    openActivity(LoginActivity.class);
                }
                break;
            case R.id.layThreeShareTicket:
                openActivity(MyShareTicketActivity.class);
                viewShareTicketRedCircle.setVisibility(View.INVISIBLE);
                break;
            case R.id.layMyBanner:
                if (!UserInfo.isLogin){
                    openActivity(LoginActivity.class);
                }
                break;
            case R.id.layToFeedback:
                openActivity(FeedBackActivity.class);
                break;
            default:
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.bottom_dialog_out);
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.bottom_dialog_out);
    }

    public void setMyBanner(){
        if (UserInfo.isLogin){
            tvLoginTips.setVisibility(View.INVISIBLE);
            if (!TextUtils.isEmpty(UserInfo.avatar)) {
                ImageLoader.getInstance().displayImage(UserInfo.avatar, imgPhoto);
            }else {
                imgPhoto.setImageResource(R.drawable.ic_default_photo);
            }
            tvName.setVisibility(View.VISIBLE);
            tvRegisterDate.setVisibility(View.VISIBLE);
            tvName.setText(UserInfo.name);
            String createTime= UserInfo.createTime.substring(0, UserInfo.createTime.indexOf(" "));
            tvRegisterDate.setText(createTime);
            viewSex.setVisibility(View.VISIBLE);
            if (UserInfo.gender==1){
                viewSex.setBackgroundResource(R.drawable.ic_my_sex_boy);
            }else{
                viewSex.setBackgroundResource(R.drawable.ic_my_sex_girl);
            }

        }else{
            tvLoginTips.setVisibility(View.VISIBLE);
            imgPhoto.setImageResource(R.drawable.ic_default_photo);
            tvName.setVisibility(View.INVISIBLE);
            viewSex.setVisibility(View.INVISIBLE);
            tvRegisterDate.setVisibility(View.INVISIBLE);
        }

    }
    private void setMyInfor()
    {
        if (UserInfo.isLogin){
            if (!hasBaseInfo){
                showProgressDialog();
                getMyInfor();
            }
        }else{//重置显示效果：没有登录时的效果
            //下面是初始化界面显示信息
            tvMsgCount.setText("消息");
            viewMsgRedCircle.setVisibility(View.INVISIBLE);
            //我的关注
            tvAttentionCount.setText("关注的影片/影评人");
            imgAttentionPhoto.setImageBitmap(null);
            //我的兑换劵
            tvMyTicketCount.setText("我的兑换劵");
            //我晒
            layThreeShareTicket.removeAllViews();
            viewShareTicketRedCircle.setVisibility(View.INVISIBLE);
            tvShareTicketCount.setText("我晒");
        }
    }
    public void getMyInfor(){

        apiRequestManager.getMyBaseInfo(new AsyncHttpHelper.ResultCallback<MyBaseInfo>()
        {
            @Override
            public void onSuccess(MyBaseInfo data)
            {

                //info里的message//下面是初始化界面显示信息
                MyBaseInfo.Msg msg= data.info.message;
                String msgNum = msg.msgNum+"";
                int hasNew = msg.hasNew;
                tvMsgCount.setText("消息(" + msgNum + ")");
                if (hasNew==1){
                    viewMsgRedCircle.setVisibility(View.VISIBLE);
                }else{
                    viewMsgRedCircle.setVisibility(View.INVISIBLE);
                }

                //following关注的影片 //我的关注
                MyBaseInfo.Following following = data.info.following;
                String followingNum = following.followingNum+"";
                String activeProfile = following.activeProfile;
                tvAttentionCount.setText("关注的影片/影评人(" + followingNum + ")");
                if (TextUtils.isEmpty(activeProfile)){
                    imgAttentionPhoto.setImageBitmap(null);
                }else{
                    ImageLoaderUtils.displayImage(activeProfile, imgAttentionPhoto,R.drawable.ic_default_photo);
                }

                //ticket我的兑换劵
                MyBaseInfo.Ticket ticketObject = data.info.ticket;
                String ticketNum = ticketObject.ticketNum+"";
                tvMyTicketCount.setText("我的兑换劵("+ticketNum+")");
                //myStubs我晒
                layThreeShareTicket.removeAllViews();
                MyBaseInfo.MyStubs myStubsObject = data.info.myStubs;
                int myStubNum = myStubsObject.myStubNum;
                int newSupported = myStubsObject.newSupported;
                ArrayList<MyBaseInfo.StubModel> stubsArray = myStubsObject.stubs;
                for (int i = 0; i < stubsArray.size(); i++) {
                    MyBaseInfo.StubModel stubModel = stubsArray.get(i);
                    String stubID = stubModel.stubID+"";
                    String stubImageUrl = stubModel.stubImageUrl;
                    int opinion = stubModel.opinion;
                    RelativeLayout layout= (RelativeLayout) inflater.inflate(R.layout.layout_my_shared_ticket, null);
                    LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0, DimenUtils.dp2px(MyMainActivity.this,85),1);
                    ImageView imageView= (ImageView) layout.findViewById(R.id.imgShareTicket);
                    View viewPraise= layout.findViewById(R.id.viewPraise);
                    layThreeShareTicket.addView(layout,params);
                    ImageLoaderUtils.displayImage(stubImageUrl, imageView,DimenUtils.dp2px(MyMainActivity.this,100),DimenUtils.dp2px(MyMainActivity.this,75));
                    if (opinion==1){
                        viewPraise.setBackgroundResource(R.drawable.ic_is_worth_yellow);
                    }else{
                        viewPraise.setBackgroundResource(R.drawable.ic_is_not_worth_green);
                    }
                }
                if (newSupported==0){
                    viewShareTicketRedCircle.setVisibility(View.INVISIBLE);
                }else{
                    viewShareTicketRedCircle.setVisibility(View.VISIBLE);
                }

                tvShareTicketCount.setText("我晒(" + myStubNum + ")");

                //结果信息标志
                hasBaseInfo=true;
                cancelProgressDialog();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }
}
