package com.dym.film.activity.price;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.mine.LoginActivity;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.ProgressWheel;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.RegexUtils;

public class OrderActivity extends BaseActivity implements TextWatcher
{

    public static final String PRICE="price1";
    private TextView order_number;
    private TextView order_all_number;
    private EditText order_numbers;
    private EditText order_edit_phone;
    private int count=1;
    private int MINCOUNT=1;
    private int MAXCOUNT=50;
    private float price=0;
    private float totalPrice=0;
    private ProgressWheel comment_order_btn_load;
    private TextView comment_order_btn;

    private String cinemaId="";
    private String filmId="";
    private String prices="";

    private NetworkManager networkManager=NetworkManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        settranslucentStatusBar(R.color.main_title_color);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_order;
    }

    @Override
    protected void initVariable() {
        count=1;
        cinemaId=getIntent().getStringExtra(PriceActivity.CINEMAID);
        filmId=getIntent().getStringExtra(PriceActivity.FILMID);
        prices=getIntent().getStringExtra(PRICE);
    }

    @Override
    protected void findViews() {
        order_numbers=$(R.id.order_numbers);
        order_edit_phone=$(R.id.order_edit_phone);
        order_number=$(R.id.order_number);
        order_all_number=$(R.id.order_all_number);
        comment_order_btn=$(R.id.comment_order_btn);
        comment_order_btn_load=$(R.id.comment_order_btn_load);
        order_numbers.addTextChangedListener(this);
        order_numbers.setCursorVisible(false);

    }

    @Override
    protected void initData() {
        order_number.setText(prices);
        order_numbers.setText(prices);
        price=Float.valueOf(prices);
        totalPrice=count*price;
        order_all_number.setText(totalPrice + "");

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        String phone= UserInfo.mobile;
        if(!TextUtils.isEmpty(phone)){
            order_edit_phone.setText(phone);
        }
    }

    @Override
    public void doClick(View view) {

        switch (view.getId()){
            case R.id.order_iv_back:
                finish();
                break;
            case R.id.comment_order_btn:
                if(!UserInfo.isLogin){
                    startActivity(new Intent(this,LoginActivity.class));
                    return;
                }
                String phone=order_edit_phone.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                }else{
                    if(RegexUtils.isMobilePhoneNumber(phone)){
                        initPostData();
                    }else{
                        Toast.makeText(this, "请输入正确手机号", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.order_number_minus:
                if(!isVariable(view)){
                    return;
                }
                count--;
                if(count<=MINCOUNT){
                    count=MINCOUNT;
                }
                order_numbers.setText(count+"");
                totalPrice=count*price;
                order_all_number.setText(totalPrice+"");
                break;
            case R.id.order_number_plus:
                if(!isVariable(view)){
                    return;
                }
                count++;
                if(count>=MAXCOUNT){
                    count=MAXCOUNT;
                }
                order_numbers.setText(count+"");
                totalPrice=count*price;
                order_all_number.setText(totalPrice+"");
                break;

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String i=order_numbers.getText().toString().trim();
        if(TextUtils.isEmpty(i)){
            i="0";
        }
        float y=Float.valueOf(i);
        totalPrice=y*price;
        order_all_number.setText(totalPrice+"");
    }

    private boolean isVariable(View view) {
        String i=order_numbers.getText().toString().trim();
        if(TextUtils.isEmpty(i)){
            i="0";
        }
        int y=Integer.parseInt(i);
        count=y;
        if(y>MAXCOUNT){
            y=MAXCOUNT;
            count=y;
            order_numbers.setText(count + "");
            totalPrice=count*price;
            order_all_number.setText(totalPrice+"");
            Toast.makeText(this, "最多购买" + MAXCOUNT, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(y<MINCOUNT) {
            y=MINCOUNT;
            count=y;
            order_numbers.setText(count + "");
            totalPrice=count*price;
            order_all_number.setText(totalPrice+"");
            Toast.makeText(this, "最少购买" + MINCOUNT, Toast.LENGTH_SHORT).show();
            return  false;
        }
        return true;
    }

    public void initPostData(){
        if(!UserInfo.isLogin){
            startActivity(new Intent(this,LoginActivity.class));
            return;
        }
        String userId= UserInfo.userID+"";
        String token= UserInfo.token;
        String phone=order_edit_phone.getText().toString().trim();

        PayInit();
        networkManager.postRespOrderCode(1, totalPrice, Integer.parseInt(filmId),Integer.parseInt(cinemaId),phone,userId, token, new HttpRespCallback<NetworkManager.RespOrderCode>() {
            @Override
            public void onRespFailure(int code, String msg) {
                PayFail(msg);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);

                NetworkManager.RespOrderCode code = (NetworkManager.RespOrderCode) msg.obj;
                if (code != null) {
                    String co = code.orderCode;
                    if (TextUtils.isEmpty(co)) {
                        PayFail();
                    } else {
                        PaySuccess(co);
                    }
                } else {
                    PayFail();
                }
            }
        });
    }

    private void PayInit(){
        comment_order_btn_load.setVisibility(View.VISIBLE);
        comment_order_btn.setVisibility(View.GONE);
    }
    private void PaySuccess(String code){
                comment_order_btn_load.setVisibility(View.GONE);
                comment_order_btn.setVisibility(View.VISIBLE);
                MatStatsUtil.eventClick(OrderActivity.this,"submit_order","submit_order");
                Intent it=new Intent(OrderActivity.this,PayOrderActivity.class);
                it.putExtra(PayOrderActivity.COUNTNUM, totalPrice+"");
                it.putExtra(PayOrderActivity.CODE, code);
                startActivity(it);

    }
    private void PayFail(String str){
        comment_order_btn_load.setVisibility(View.GONE);
        comment_order_btn.setVisibility(View.VISIBLE);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    private void PayFail(){
        comment_order_btn_load.setVisibility(View.GONE);
        comment_order_btn.setVisibility(View.VISIBLE);
        Toast.makeText(this, "购买失败", Toast.LENGTH_SHORT).show();
    }
}
