package com.dym.film.activity.mine;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.MyTicketListInfo;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.views.CustomTypefaceTextView;
import com.dym.film.ui.LoadMoreListView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class MyTicketActivity extends BaseActivity
{
    private LoadMoreListView listMyTicket;
    private CommonBaseAdapter<MyTicketListInfo.OrderModel> mAdapter;
    private ArrayList<MyTicketListInfo.OrderModel> mMyTicketDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage=0;
    private int limit=20;
    private boolean isLoadOrRefresh=true;//刷新
    private LinearLayout layNoTicket;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_my_ticket;
    }

    @Override
    protected void initVariable()
    {
        mMyTicketDatas=new ArrayList<MyTicketListInfo.OrderModel>();
    }

    @Override
    protected void findViews()
    {
        listMyTicket=$(R.id.listMyTicket);
        layNoTicket=$(R.id.layNoTicket);
        showTopBar();
        setTitle("我的兑换劵");
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getMyTicketList(0, limit);
                curPage=0;
                isLoadOrRefresh=true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

       if(!TextUtils.isEmpty( UserInfo.token)&&mMyTicketDatas.size()>0) {
            getMyTicketList(0, limit);
            isLoadOrRefresh = true;
            curPage = 0;
        }
    }

    @Override
    protected void initData()
    {

        mAdapter=new CommonBaseAdapter<MyTicketListInfo.OrderModel>(this,mMyTicketDatas,R.layout.list_item_my_ticket_exchange)
        {
            @Override
            public void convert(ViewHolder holder, MyTicketListInfo.OrderModel itemData,int position)
            {
                CustomTypefaceTextView tvTicketTime = holder.getView(R.id.tvTicketTime);
                CustomTypefaceTextView tvTicketDate = holder.getView(R.id.tvTicketDate);
                CustomTypefaceTextView tvTicketName = holder.getView(R.id.tvTicketName);
                CustomTypefaceTextView tvTicketCount = holder.getView(R.id.tvTicketCount);
                CustomTypefaceTextView tvAllMoney = holder.getView(R.id.tvAllMoney);
                CustomTypefaceTextView tvStatus = holder.getView(R.id.tvStatus);
                String buyTime=itemData.buyTime;
                String month=buyTime.substring(5, 10);
                String year=buyTime.substring(0,4);
                tvTicketTime.setText(month);
                tvTicketDate.setText(year);

                tvTicketCount.setText(itemData.count+"张");
                tvAllMoney.setText("|￥"+itemData.amount);

                int status=itemData.status;
                if (status==3){
                    tvStatus.setText("兑换");
                    tvStatus.setTextColor(0xffffffff);
                    tvStatus.setBackgroundResource(R.color.red_color);
                }else if(status==5){
                    tvStatus.setText("已兑换");
                    tvStatus.setTextColor(getResources().getColor(R.color.item_text_gray_color));
                    tvStatus.setBackgroundResource(R.color.item_bg_color);
                }else if(status==-1){
                    tvStatus.setText("已退款");

                    tvStatus.setTextColor(0xffffffff);
                    tvStatus.setBackgroundResource(R.color.item_bg_color);
                }else {
                    tvStatus.setText("待付款");
                    tvStatus.setTextColor(getResources().getColor(R.color.item_text_gray_color));
                    tvStatus.setBackgroundResource(R.color.item_bg_color);
                }
            }

        };

        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listMyTicket);
        listMyTicket.setAdapter(animAdapter);

        isLoadOrRefresh=true;
        curPage=0;
        startActivityLoading();
    }
    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getMyTicketList(curPage,limit);
    }


    @Override
    protected void setListener()
    {
        listMyTicket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyTicketListInfo.OrderModel orderModel = mMyTicketDatas.get(position);
                Intent intent = new Intent(MyTicketActivity.this, TicketDetailActivity.class);
                intent.putExtra("ticketData", orderModel);
                startActivity(intent);
            }
        });

        listMyTicket.setOnLoadListener(new LoadMoreListView.OnLoadListener() {
            @Override
            public void onLoad(LoadMoreListView listView) {

                getMyTicketList(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }

    @Override
    public void doClick(View view)
    {
        finish();
    }

    public void getMyTicketList(int page, final int limit){

        apiRequestManager.getMyTicketList(page, limit, new AsyncHttpHelper.ResultCallback<MyTicketListInfo>()
        {
            @Override
            public void onSuccess(MyTicketListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isLoadOrRefresh){
                    mMyTicketDatas.clear();
                }
                ArrayList<MyTicketListInfo.OrderModel> orders=data.orders;
                int size=orders.size();
                mMyTicketDatas.addAll(orders);
                mAdapter.notifyDataSetChanged();
                layNoTicket.setVisibility(View.INVISIBLE);
                if (size==0){
                    layNoTicket.setVisibility(View.VISIBLE);
                    listMyTicket.onLoadUnavailable();
                }else if (size==limit){
                    listMyTicket.onLoadSucess(true);
                }else{
                    listMyTicket.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingFailed();
                if (!isLoadOrRefresh){
                    listMyTicket.onLoadFailed();
                }
            }
        });
    }
}
