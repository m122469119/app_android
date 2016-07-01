package com.dym.film.activity.mine;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.sharedticket.SharedTicketDetailActivity;
import com.dym.film.activity.sharedticket.TagSharedTicketActivity;
import com.dym.film.activity.sharedticket.TicketShareActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.controllers.ShareTicketDialogViewController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.manager.data.MainSharedTicketDataManager;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.model.MySharedTicketListInfo;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.CustomTypefaceTextView;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.views.TagTextView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class MyShareTicketActivity extends BaseActivity
{
    private LoadMoreListView listMyShareTicket;
    private CommonBaseAdapter<NetworkManager.SharedTicketRespModel> mAdapter;
    private ArrayList<NetworkManager.SharedTicketRespModel> mMyShareTicketDatas;
    private CustomDialog delDialog;
    private ShareManager shareManager;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isLoadOrRefresh = true;//刷新
    private NetworkManager.SharedTicketRespModel curItemData;
    private LinearLayout layNoSharedTicket;
    private ShareTicketDialogViewController mSelectDialogController;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_my_share_ticket;
    }

    @Override
    protected void initVariable()
    {
        mMyShareTicketDatas = new ArrayList<NetworkManager.SharedTicketRespModel>();
        shareManager = new ShareManager(this);
    }

    @Override
    protected void findViews()
    {
        layNoSharedTicket = $(R.id.layNoSharedTicket);
        listMyShareTicket = $(R.id.listMyShareTicket);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getMyShareTicketList(0, limit);
                curPage = 0;
                isLoadOrRefresh = true;
            }
        });
    }

    @Override
    protected void initData()
    {
        isLoadOrRefresh = true;
        curPage = 0;
        mAdapter = new CommonBaseAdapter<NetworkManager.SharedTicketRespModel>(this, mMyShareTicketDatas, R.layout.list_item_my_share_ticket)
        {
            @Override
            public void convert(ViewHolder holder, NetworkManager.SharedTicketRespModel itemData, int position)
            {
                ImageView imgTicket = holder.getView(R.id.imgTicket);
                TextView tvShareTicketDate = holder.getView(R.id.tvShareTicketDate);
                TextView tvShareTicketDateUnit = holder.getView(R.id.tvShareTicketDateUnit);
                TextView tvShareTicketLocation = holder.getView(R.id.tvShareTicketLocation);
                TextView tvPraiseCount = holder.getView(R.id.tvPraiseCount);
                TextView tvCommentCount = holder.getView(R.id.tvCommentCount);
                TagTextView tvMyShareTicketContent = holder.getView(R.id.tvMyShareTicketContent);
                CustomTypefaceTextView btnDelMySharedTicket = holder.getView(R.id.btnDelMySharedTicket);
                CustomTypefaceTextView btnShareMySharedTicket = holder.getView(R.id.btnShareMySharedTicket);
                View viewPraise = holder.getView(R.id.viewPraise);
                //设置监听
                MyClickListener myClickListener = new MyClickListener(itemData);
                imgTicket.setTag(position);
                imgTicket.setOnClickListener(myClickListener);
                btnDelMySharedTicket.setOnClickListener(myClickListener);
                btnShareMySharedTicket.setOnClickListener(myClickListener);
                tvMyShareTicketContent.setTagClickListener(new TagTextView.TagClickListener()
                {
                    @Override
                    public void onTagClicked(String tag)
                    {
                        startTagSharedTicketActivity(tag);
                    }
                });
                //下面填充数据
                String content = itemData.content;
                String stubImageUrl = itemData.stubImage.url;
                int opinion = (int) itemData.opinion;
                int supportNum = (int) itemData.supportNum;
                long showOffTime = (long) itemData.showOffTime;
                String district = (String) itemData.district;
                String city = (String) itemData.city;

                tvMyShareTicketContent.setTagText(itemData.tags, content);
                ImageLoaderUtils.displayImage(stubImageUrl, imgTicket, DimenUtils.dp2px(MyShareTicketActivity.this, 200), DimenUtils.dp2px(MyShareTicketActivity.this, 200));

                CommonManager.setTime2(tvShareTicketDate, tvShareTicketDateUnit, showOffTime);
                tvShareTicketLocation.setText(city + district);
                tvPraiseCount.setText("赞 " + supportNum);
                tvCommentCount.setText("评论 " + itemData.commentsNum);
                if (opinion == 1) {
                    viewPraise.setBackgroundResource(R.drawable.ic_is_worth_yellow);
                }
                else {
                    viewPraise.setBackgroundResource(R.drawable.ic_is_not_worth_green);
                }
            }
        };
        listMyShareTicket.setAdapter(mAdapter);
        startActivityLoading();

    }
    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getMyShareTicketList(0, limit);
    }
    @Override
    protected void setListener()
    {

        listMyShareTicket.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {

                getMyShareTicketList(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (MainSharedTicketDataManager.mInstance.needRefreshAll()) {
            CommonManager.setRefreshingState(mRefreshLayout, true);
            getMyShareTicketList(0, limit);
            curPage = 0;
            isLoadOrRefresh = true;
            MainSharedTicketDataManager.mInstance.setNeedRefreshAll(false);
        }

    }

    @Override
    public void doClick(View view)
    {
        if (view.getId() == R.id.btnShareTicket) {
            // 启动晒票
            if (mSelectDialogController == null) {
                mSelectDialogController = new ShareTicketDialogViewController(this);
            }
            mSelectDialogController.show();

        }
        else {
            finish();
        }
    }

    public void showDelDialog(NetworkManager.SharedTicketRespModel itemData)
    {
        curItemData = itemData;
        if (delDialog == null) {
            delDialog = new CustomDialog(this, R.style.default_dialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null);
            Button btnCancel = $(view, R.id.btnCancel);
            Button btnConfirm = $(view, R.id.btnConfirm);

            btnCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    delDialog.dismiss();
                }
            });
            btnConfirm.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    delDialog.dismiss();
                    showProgressDialog();
                    delMySharedTicket(curItemData);
                }
            });
            delDialog.setContentView(view);
        }
        delDialog.show(Gravity.CENTER);

    }

    private void startTagSharedTicketActivity(String tag)
    {
        Intent intent = new Intent(this, TagSharedTicketActivity.class);
        intent.putExtra(TagSharedTicketActivity.KEY_TAG, tag);
        startActivity(intent);
    }


    public void getMyShareTicketList(int page, final int limit)
    {
        apiRequestManager.getMyShareTicketList(page, limit, new AsyncHttpHelper.ResultCallback<MySharedTicketListInfo>()
        {
            @Override
            public void onSuccess(MySharedTicketListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isLoadOrRefresh) {
                    mMyShareTicketDatas.clear();
                }
                ArrayList<NetworkManager.SharedTicketRespModel> stubs = data.stubs;

                mMyShareTicketDatas.addAll(stubs);


                mAdapter.notifyDataSetChanged();

                int size = stubs.size();
                layNoSharedTicket.setVisibility(View.INVISIBLE);
                if (size == 0) {
                    layNoSharedTicket.setVisibility(View.VISIBLE);
                    listMyShareTicket.onLoadUnavailable();
                }
                else if (size == limit) {
                    listMyShareTicket.onLoadSucess(true);
                }
                else {
                    listMyShareTicket.onLoadSucess(false);
                }
                cancelProgressDialog();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingFailed();
                if (!isLoadOrRefresh) {
                    listMyShareTicket.onLoadFailed();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        LogUtils.e("Kejin", "request: " + requestCode + " result: " + resultCode);
        switch (requestCode) {
            case ShareTicketDialogViewController.REQUEST_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(uri);

                        startActivity(intent);
                        return;
                    }
                }

                break;

            case ShareTicketDialogViewController.REQUEST_CODE_CAPTURE_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = ShareTicketDialogViewController.getCameraImageUri();
                    if (imageUri != null) {
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(imageUri);
                        startActivity(intent);
                    }
                }

                ShareTicketDialogViewController.onActivityFinished();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void delMySharedTicket(NetworkManager.SharedTicketRespModel itemData)
    {
        int id = (int) itemData.stubID;
        apiRequestManager.delMySharedTicket(id, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                mMyShareTicketDatas.remove(curItemData);
                mAdapter.notifyDataSetChanged();
                if (mMyShareTicketDatas.size() == 0) {
                    layNoSharedTicket.setVisibility(View.VISIBLE);
                    listMyShareTicket.onLoadUnavailable();
                }
            }
            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });
    }

    public class MyClickListener implements View.OnClickListener
    {
        private NetworkManager.SharedTicketRespModel itemData;

        public MyClickListener(NetworkManager.SharedTicketRespModel _itemData)
        {
            itemData = _itemData;
        }

        @Override
        public void onClick(View v)
        {
            switch (v.getId()) {
                case R.id.btnDelMySharedTicket:
                    showDelDialog(itemData);
                    break;
                case R.id.btnShareMySharedTicket:
                    shareManager.setTitle("公证电影｜看电影晒票根");
                    shareManager.setTitleUrl(ConfigInfo.BASE_URL + (String) itemData.shareUrl);
                    shareManager.setText((String) itemData.content);
                    shareManager.setImageUrl((String) itemData.stubImage.url);
                    shareManager.setWebUrl(ConfigInfo.BASE_URL + (String) itemData.shareUrl);
                    shareManager.showShareDialog(MyShareTicketActivity.this);

                    break;
                case R.id.imgTicket:

                    NetworkManager.UserModel userModel=new NetworkManager.UserModel();
                    itemData.writer=userModel;
                    userModel.avatar= UserInfo.avatar;
                    userModel.userID= UserInfo.userID;
                    userModel.gender= UserInfo.gender;
                    userModel.mobile= UserInfo.mobile;
                    userModel.name= UserInfo.name;
                    CommonManager.putData(SharedTicketDetailActivity.KEY_INTENT, itemData);
                    Intent intent = new Intent(mContext, SharedTicketDetailActivity.class);
                    startActivity(intent);

//                    Intent intent = new Intent(MyShareTicketActivity.this, MyShareTicketImageActivity.class);
//                    intent.putExtra("mMyShareTicketDatas", mMyShareTicketDatas);
//                    intent.putExtra("index", (Integer) v.getTag());
//                    MyShareTicketActivity.this.startActivity(intent);
//                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    break;
                default:
                    break;

            }
        }
    }

}
