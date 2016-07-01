package com.dym.film.activity.mine;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.alipay.sdk.PayResult;
import com.dym.film.alipay.sdk.ZhifubaoPayUtil;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.model.MyTicketListInfo;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;

import java.lang.ref.WeakReference;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class TicketDetailActivity extends BaseActivity
{


    private TextView btnDelete;
    private TextView tvStatus;
    private TextView tvBuyMonth;
    private TextView tvBuyYear;
    private TextView tvBuyTime;
    private TextView tvPayMoney;
    private TextView tvAllMoney;
    private TextView tvOrderCode;
    private TextView btnRefund;
    private  MyTicketListInfo.OrderModel ticketData;
    private TextView tvTicketCount;
    private TextView tvFilmName;
    private TextView voucher_pay_btn;
    private ImageView btnProtocolBack;


    private PopupWindow popupWindow;
    private ZhifubaoPayUtil payUtil;

    public static class MyHandler extends Handler {
        private final WeakReference<TicketDetailActivity> mActivity;

        public MyHandler(TicketDetailActivity activity) {
            mActivity = new WeakReference<TicketDetailActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TicketDetailActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case ZhifubaoPayUtil.SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);
                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultInfo = payResult.getResult();
                        String resultStatus = payResult.getResultStatus();
                        // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                        if (TextUtils.equals(resultStatus, "9000")) {
                            activity.popupWindow.showAsDropDown(activity.btnProtocolBack);
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
    protected int setLayoutView()
    {
        return R.layout.activity_my_ticket_detail;
    }

    @Override
    protected void initVariable()
    {

    }

    @Override
    protected void findViews()
    {
        tvBuyMonth=$(R.id.tvBuyMonth);
        tvBuyYear=$(R.id.tvBuyYear);
        tvBuyTime=$(R.id.tvBuyTime);

        tvTicketCount=$(R.id.tvTicketCount);
        tvStatus=$(R.id.tvStatus);
        tvPayMoney=$(R.id.tvPayMoney);
        tvAllMoney=$(R.id.tvAllMoney);
        tvOrderCode=$(R.id.tvOrderCode);

        tvFilmName=$(R.id.tvFilmName);
        btnRefund=$(R.id.btnRefund);
        voucher_pay_btn=$(R.id.voucher_pay_btn);
        btnProtocolBack=$(R.id.btnProtocolBack);
    }


    private void initPopupWindow() {
        // TODO Auto-generated method stub
        View view = getLayoutInflater().inflate(R.layout.layout_ticket_share_page_three, null);
        popupWindow = new PopupWindow(view,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.popwin_recent_anim_style1);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        ImageView imageView= (ImageView) view.findViewById(R.id.closeButton);
        imageView.setVisibility(View.GONE);
        TextView shareButton_text= (TextView) view.findViewById(R.id.shareButton_text);
        final TextView shareButton= (TextView) view.findViewById(R.id.shareButton);
        shareButton_text.setText("支付成功");
        shareButton.setText("查看兑换券");
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatus(3);
                popupWindow.dismiss();
            }
        });
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setStatus(3);
            }
        });

    }
    @Override
    protected void initData()
    {
        payUtil=new ZhifubaoPayUtil(TicketDetailActivity.this,mHandler);
        ticketData= ( MyTicketListInfo.OrderModel) getIntent().getSerializableExtra("ticketData");
        int status=ticketData.status;
        setStatus(status);
        String buyTime=ticketData.buyTime;
        String month=buyTime.substring(5, 10);
        String year=buyTime.substring(0,4);
        tvBuyMonth.setText(month);
        tvBuyYear.setText(year);
        tvBuyTime.setText("购买时间："+buyTime);

        tvTicketCount.setText(ticketData.count+"张");
        tvAllMoney.setText("|￥"+ticketData.amount);

        tvFilmName.setText(ticketData.film);
        tvOrderCode.setText("订单号："+ticketData.orderCode);
        tvPayMoney.setText("实付金额：￥" + ticketData.amount);

        initPopupWindow();
    }

    public void setStatus(int status){
        btnRefund.setVisibility(View.INVISIBLE);
        voucher_pay_btn.setVisibility(View.GONE);
        tvStatus.setClickable(false);
        if (status==3){
            tvStatus.setText("兑换");
            tvStatus.setTextColor(0xffffffff);
            tvStatus.setBackgroundResource(R.color.red_color);
//            btnRefund.setVisibility(View.VISIBLE);
            tvStatus.setClickable(true);
        }else if(status==5){
            tvStatus.setText("已兑换");
            tvStatus.setTextColor(getResources().getColor(R.color.item_text_gray_color));
            tvStatus.setBackgroundResource(R.color.item_bg_color);

        }else if (status==-1){
            tvStatus.setText("已退款");
            tvStatus.setTextColor(0xffffffff);
            tvStatus.setBackgroundResource(R.color.item_bg_color);

        }else {
            tvStatus.setText("待付款");
            tvStatus.setTextColor(getResources().getColor(R.color.item_text_gray_color));
            tvStatus.setBackgroundResource(R.color.item_bg_color);
            voucher_pay_btn.setVisibility(View.VISIBLE);
        }

    }
    @Override
    protected void setListener()
    {
        voucher_pay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MixUtils.isFastClick()){
                    return;
                }
                if (ticketData != null) {
                    payUtil.pay("电影票兑换券", "可用于兑换指定电影票", ticketData.amount+"", ticketData.orderCode+"");
                } else {
                    ShowMsg("信息错误");
                }
            }
        });

    }

    @Override
    public void doClick(View view)
    {
        switch (view.getId()){
            case R.id.tvStatus:
                MatStatsUtil.eventClick(this, "ticket_exchange", "ticket_exchange");
                String url =  ConfigInfo.BASE_URL+"/user/ticket/exchange?user_id="+ UserInfo.userID+"&user_token="+ UserInfo.token;
                Intent intent = new Intent(this, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,3);
                startActivity(intent);
                break;
            case R.id.btnRefund:
                MatStatsUtil.eventClick(this, "ticket_exchange", "ticket_exchange");
                break;
            default:
                finish();
                break;
        }
    }


}
