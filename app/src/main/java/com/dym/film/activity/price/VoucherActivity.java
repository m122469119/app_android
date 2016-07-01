package com.dym.film.activity.price;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.CustomTypefaceTextView;
import com.dym.film.views.FlowLayout;

public class VoucherActivity extends BaseActivity {

    private String cinemaId="";
    private String filmId="";
    private String price="";
    private String num="";
    private TextView price_number;
    private TextView price_buy_number;
    private TextView voucher_iv_tv;
    private FlowLayout flowView;
    private TextView voucher_pay_btn;
    private NetworkManager networkManager=NetworkManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar(R.color.main_title_color);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_voucher;
    }

    @Override
    protected void initVariable() {
        cinemaId=getIntent().getStringExtra(PriceActivity.CINEMAID);
        filmId=getIntent().getStringExtra(PriceActivity.FILMID);
        num=getIntent().getStringExtra(PriceActivity.NUM);
        price=getIntent().getStringExtra(PriceActivity.PRICE);


    }

    @Override
    protected void findViews() {

        price_buy_number=$(R.id.price_buy_number);
        price_number=$(R.id.price_number);
        voucher_iv_tv=$(R.id.voucher_iv_tv);
        flowView=$(R.id.flowView);
        voucher_pay_btn=$(R.id.voucher_pay_btn);
    }

    @Override
    protected void initData() {
        price_number.setText(price);
        price_buy_number.setText(num);

        networkManager.getVoucherList(filmId,cinemaId,new HttpRespCallback<NetworkManager.RespVoucherList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                addItem("暂无影片",false);
                failVisiable();
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespVoucherList vl = (NetworkManager.RespVoucherList) msg.obj;
                if (vl != null && vl.films != null){
                    String tktnum=vl.films.tktNum;
                    if(TextUtils.isEmpty(tktnum)){
                         failVisiable();
                    }else{
                        int i=Integer.parseInt(tktnum);
                        if(i>0)
                             SussVisiable();
                        else
                            failVisiable();
                    }
                }else{
                    failVisiable();
                }
                if (vl != null && vl.films != null&& vl.films.list != null && vl.films.list.size() > 0) {
                    for (int i = 0; i < vl.films.list.size(); i++) {
                        addItem(vl.films.list.get(i),true);
                    }
                } else {
                    addItem("暂无影片",false);
                }
            }
        });
    }

    @Override
    protected void setListener() {
    }

    @Override
    public void doClick(View view) {

        switch (view.getId()){
            case  R.id.voucher_iv_back:
                finish();
                break;

            case  R.id.voucher_pay_btn:
                MatStatsUtil.eventClick(this, "buy_now_tickets", "buy_now_tickets");
                Intent it1=new Intent(this, OrderActivity.class);
                it1.putExtra(PriceActivity.CINEMAID, cinemaId);
                it1.putExtra(PriceActivity.FILMID, filmId);
                it1.putExtra(OrderActivity.PRICE, price);

                startActivity(it1);
                break;

        }
    }


    public void addItem(String str,boolean flag) {
        CustomTypefaceTextView newView = new CustomTypefaceTextView(this);
        if(flag)
            newView.setText("《"+str+"》");
        else
            newView.setText(str);
        newView.setTextSize(14);
        newView.setSingleLine();
        newView.setTextColor(getResources().getColor(R.color.item_text_gray_color));
        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int i=(10);
        params.rightMargin = i;
        params.topMargin = i;
        params.bottomMargin=i;
        params.gravity= Gravity.CENTER;
        newView.setLayoutParams(params);
        flowView.addView(newView);
    }
    private void failVisiable(){
        voucher_pay_btn.setText("暂无兑换券");
        voucher_pay_btn.setEnabled(false);
        voucher_pay_btn.setBackgroundResource(R.color.price_linerlayout_backgroudcolor);

    }
    private void SussVisiable(){
        voucher_pay_btn.setText("立即抢购");
        voucher_pay_btn.setEnabled(true);
        voucher_pay_btn.setBackgroundResource(R.drawable.btn_red_shape_selector);
    }

}
