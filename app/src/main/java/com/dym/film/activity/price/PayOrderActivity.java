package com.dym.film.activity.price;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.mine.MyTicketActivity;
import com.dym.film.alipay.sdk.PayResult;
import com.dym.film.alipay.sdk.ZhifubaoPayUtil;
import com.dym.film.utils.AppManager;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;

import java.lang.ref.WeakReference;

public class PayOrderActivity extends BaseActivity {

    public static String COUNTNUM="count";
    public static String CODE="code";
    private String totalPrice="0";
    private String code="";
    private String PAY_TYPE="Z";
    private TextView pay_order_totalPrice;
    private RelativeLayout pay_order_select_relative;
    private RelativeLayout pay_order_select_relative1;
    private PopupWindow popupWindow;
    private LinearLayout pay_order_iv_back;

    private ZhifubaoPayUtil payUtil;

    public static class MyHandler extends Handler{
        private final WeakReference<PayOrderActivity> mActivity;

        public MyHandler(PayOrderActivity activity) {
            mActivity = new WeakReference<PayOrderActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PayOrderActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case ZhifubaoPayUtil.SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);
                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultInfo = payResult.getResult();
                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            activity.popupWindow.showAsDropDown(activity.pay_order_iv_back);
                        } else {
                            // 判断resultStatus 为非“9000”则代表可能支付失败
                            // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                            if (TextUtils.equals(resultStatus, "8000")) {
                                Toast.makeText(activity, "支付结果确认中",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                                Toast.makeText(activity, "支付失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                    case ZhifubaoPayUtil.SDK_CHECK_FLAG: {
                        Toast.makeText(activity, "检查结果为：" + msg.obj,Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    }

    public final MyHandler mHandler = new MyHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        settranslucentStatusBar(R.color.main_title_color);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_pay_order;
    }

    @Override
    protected void initVariable() {
        totalPrice=getIntent().getStringExtra(COUNTNUM);
        code=getIntent().getStringExtra(CODE);
        payUtil=new ZhifubaoPayUtil(this,mHandler);
        initPopupWindow();
    }


    private void initPopupWindow() {
        // TODO Auto-generated method stub
        View view = getLayoutInflater().inflate(R.layout.layout_ticket_share_page_three, null);
        popupWindow = new PopupWindow(view,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popwin_recent_anim_style1);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        ImageView imageView= (ImageView) view.findViewById(R.id.closeButton);
        imageView.setVisibility(View.GONE);
        TextView shareButton_text= (TextView) view.findViewById(R.id.shareButton_text);
        TextView shareButton= (TextView) view.findViewById(R.id.shareButton);
        shareButton_text.setText("支付成功");
        shareButton.setText("查看兑换券");
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppManager.getAppManager().finishActivity(OrderActivity.class);
                AppManager.getAppManager().finishActivity(VoucherActivity.class);
                AppManager.getAppManager().finishActivity(PayOrderActivity.this);
                startActivity(new Intent(PayOrderActivity.this, MyTicketActivity.class));
            }
        });

    }

    @Override
    protected void findViews() {
        pay_order_select_relative=$(R.id.pay_order_select_relative);
        pay_order_totalPrice=$(R.id.pay_order_totalPrice);
        pay_order_select_relative1=$(R.id.pay_order_select_relative1);
        pay_order_iv_back=$(R.id.pay_order_iv_back);
    }

    @Override
    protected void initData() {
        pay_order_totalPrice.setText(totalPrice+"");
    }

    @Override
    protected void setListener() {

    }

    @Override
    public void doClick(View view) {

        switch (view.getId()){
            case R.id.pay_order_iv_back:
                AppManager.getAppManager().finishActivity(OrderActivity.class);
                AppManager.getAppManager().finishActivity(VoucherActivity.class);
                AppManager.getAppManager().finishActivity(PayOrderActivity.this);
                finish();
                break;
            case R.id.pay_comment_order_btn:
                if(MixUtils.isFastClick()){
                    return;
                }
                if(PAY_TYPE.equals("Z")){
                    MatStatsUtil.eventClick(this, "pay_click", "pay_click");
                    payUtil.pay("电影票兑换券","可用于兑换指定电影票", totalPrice+"",code);
                }else{
                    MatStatsUtil.eventClick(this, "pay_click_wechat", "pay_click_wechat");
                    Snackbar.make(view,"微信支付",Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.pay_order_relative:
//                pay_order_select_relative.setVisibility(View.GONE);
//                pay_order_select_relative1.setVisibility(View.VISIBLE);
                PAY_TYPE="Z";
                break;
            case R.id.pay_order_relative1:
                pay_order_select_relative1.setVisibility(View.VISIBLE);
                pay_order_select_relative.setVisibility(View.GONE);
                PAY_TYPE="W";
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppManager.getAppManager().finishActivity(OrderActivity.class);
        AppManager.getAppManager().finishActivity(VoucherActivity.class);
        AppManager.getAppManager().finishActivity(PayOrderActivity.this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null){
            mHandler.removeMessages(ZhifubaoPayUtil.SDK_CHECK_FLAG);
            mHandler.removeMessages(ZhifubaoPayUtil.SDK_PAY_FLAG);
        }
    }
}
