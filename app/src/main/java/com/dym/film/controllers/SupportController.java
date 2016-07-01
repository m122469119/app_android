package com.dym.film.controllers;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/24
 */

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.mine.LoginActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;

import java.util.ArrayList;

/**
 * 控制点赞人的头像显示 和 点赞动作
 */
public class SupportController
{
    public final static String TAG = "SupportCtrl";

    public final static int TYPE_FILM_REVIEW = 0;
    public final static int TYPE_SHARED_TICKET = 1;
    private int mViewType = TYPE_FILM_REVIEW;

    private long mReviewID = 0;
    private boolean mSupported = false;
    private boolean mIsSupporting = false;
    private ArrayList<NetworkManager.Supporters> mSupportedAvatars = new ArrayList<>();

    private View mSupportView = null;

    private View findViewById(int id) {
        return mSupportView.findViewById(id);
    }


    public SupportController(View view, long id, int supported, ArrayList<String> avatars)
    {
        ArrayList<NetworkManager.Supporters> supporterses = new ArrayList<>();
        for (String a : avatars) {
            NetworkManager.Supporters sup = new NetworkManager.Supporters();
            sup.userID = 0;
            sup.avatar = a;
            supporterses.add(sup);
        }

        initialize(view, id, supported, supporterses, TYPE_FILM_REVIEW);
    }

    public SupportController(View view, long id, int supported, ArrayList<NetworkManager.Supporters> avatars, int type)
    {
        initialize(view, id, supported, avatars, type);
    }

    private void initialize(View view, long id, int supported, ArrayList<NetworkManager.Supporters> avatars, int type) {

        if (view == null || view.findViewById(R.id.supportLayout) == null) {
            throw new IllegalArgumentException("Need Support Layout view");
        }

        mViewType = type;
        mSupportView = view;

        mReviewID = id;
        mSupported = supported==1;
        mSupportedAvatars.clear();
        mSupportedAvatars.addAll(avatars);

        switch (type) {
            case TYPE_SHARED_TICKET:
                findViewById(R.id.pubTime).setVisibility(View.VISIBLE);
                findViewById(R.id.commentLayout).setVisibility(View.VISIBLE);
                break;
            case TYPE_FILM_REVIEW:
                findViewById(R.id.pubTime).setVisibility(View.INVISIBLE);
                findViewById(R.id.commentLayout).setVisibility(View.GONE);
                break;
        }
        setupSupportButton();
        setupSupportersGrid();
    }

    public void setupCommentView(long pubTime, View.OnClickListener listener)
    {
        if (mViewType == TYPE_SHARED_TICKET) {
            TextView time = (TextView) findViewById(R.id.pubTime);
            time.setVisibility(View.VISIBLE);
            CommonManager.setTime(time, pubTime);

            findViewById(R.id.commentLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.commentLayout).setOnClickListener(listener);
        }
    }

    private void setupSupportButton()
    {
        final View supportBtn = findViewById(R.id.support);
        final View noSup = findViewById(R.id.noSup);
        final View hasSup = findViewById(R.id.hasSup);

        if (mSupported) {
            supportBtn.setOnClickListener(null);
            noSup.setVisibility(View.INVISIBLE);
            hasSup.setVisibility(View.VISIBLE);
        }
        else {
            noSup.setVisibility(View.VISIBLE);
            hasSup.setVisibility(View.INVISIBLE);
            supportBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    switch (mViewType) {
                        case TYPE_FILM_REVIEW:
                            MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.REVIEW_LIKE);
                            break;
                        case TYPE_SHARED_TICKET:
                            MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.SHOW_LIKE);
                            break;
                    }
                    if (!UserInfo.isLogin) {
                        view.getContext().startActivity(new Intent(view.getContext(), LoginActivity.class));
                        return;
                    }
                    if (mIsSupporting) {
                        return;
                    }

                    mIsSupporting = true;

                    switch (mViewType) {
                        case TYPE_FILM_REVIEW: {
                            NetworkManager.ReqSupportReview req = new NetworkManager.ReqSupportReview();
                            req.cinecismID = mReviewID;
                            NetworkManager.getInstance().supportFilmReview(req, new HttpRespCallback<NetworkManager.RespSupportReview>()
                            {
                                @Override
                                public void onRespFailure(int code, String msg)
                                {
                                    mIsSupporting = false;
                                }

                                @Override
                                public void runOnMainThread(Message msg)
                                {
                                    mIsSupporting = false;

                                    supportBtn.setOnClickListener(null);
                                    noSup.setVisibility(View.INVISIBLE);
                                    hasSup.setVisibility(View.VISIBLE);

                                    // 把自己的头像加在最前面
                                    NetworkManager.Supporters my = new NetworkManager.Supporters();
                                    my.avatar = UserInfo.avatar;
                                    my.userID = UserInfo.userID;
                                    mSupportedAvatars.add(0, my);
                                    refreshAvatars();
                                }
                            });
                        }
                            break;

                        case TYPE_SHARED_TICKET: {
                            NetworkManager.ReqSupportSharedTicket req = new NetworkManager.ReqSupportSharedTicket();
                            req.stubID = mReviewID;
                            NetworkManager.getInstance().supportSharedTicket(req, new HttpRespCallback<NetworkManager.RespSupportSharedTicket>()
                            {
                                @Override
                                public void onRespFailure(int code, String msg)
                                {
                                    mIsSupporting = false;
                                }

                                @Override
                                public void runOnMainThread(Message msg)
                                {
                                    mIsSupporting = false;

                                    supportBtn.setOnClickListener(null);
                                    noSup.setVisibility(View.INVISIBLE);
                                    hasSup.setVisibility(View.VISIBLE);

                                    // 把自己的头像加在最前面
                                    NetworkManager.Supporters my = new NetworkManager.Supporters();
                                    my.avatar = UserInfo.avatar;
                                    my.userID = UserInfo.userID;
                                    mSupportedAvatars.add(0, my);
                                    refreshAvatars();
                                }
                            });
                        }
                            break;
                    }
                }
            });
        }
    }

    private int mColumnNumber = 0;
    private GridAdapter mAvatarAdapter = new GridAdapter();
    private GridView mAvatarGrid = null;
    private void setupSupportersGrid()
    {
        mAvatarGrid = (GridView) findViewById(R.id.avatarGrid);
        mAvatarGrid.setEnabled(false);
        mAvatarGrid.setAdapter(mAvatarAdapter);

        mAvatarGrid.post(new Runnable()
        {
            @Override
            public void run()
            {
                refreshAvatars();
            }
        });
        mAvatarGrid.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener()
        {
            @Override
            public void onViewAttachedToWindow(View view)
            {
                mAvatarGrid.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshAvatars();
                    }
                });
            }

            @Override
            public void onViewDetachedFromWindow(View view)
            {

            }
        });

        findViewById(R.id.avatarLayout).setVisibility(mSupportedAvatars.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void refreshAvatars()
    {
        if (!mSupported) {
            boolean supported = false;
            for (NetworkManager.Supporters sup : mSupportedAvatars) {
                if (sup.userID == UserInfo.userID) {
                    supported = true;
                    break;
                }
            }
            if (supported) {
                mSupported = true;
                setupSupportButton();
            }
        }

        int avatarWidth = CommonManager.dpToPx(33); // 头像的宽度
        int padding = CommonManager.dpToPx(10); // 两边的间距

        int width = mAvatarGrid.getWidth();
        mColumnNumber = (width - padding * 2) / avatarWidth;

//            LogUtils.e(TAG, "Colum: " + mColumnNumber);
        if (mColumnNumber == 0) {
            mColumnNumber = 7;
        }
        mAvatarGrid.setNumColumns(mColumnNumber);

        int lines = (mSupportedAvatars.size()-1) / mColumnNumber + 1;
        int height = lines*avatarWidth + (lines-1)*padding/2;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        mAvatarGrid.setLayoutParams(params);

        mAvatarAdapter.notifyDataSetChanged();
        findViewById(R.id.avatarLayout).setVisibility(mSupportedAvatars.isEmpty() ? View.GONE : View.VISIBLE);
    }


    public void setAvatars(ArrayList<NetworkManager.Supporters> supporters)
    {
        mSupportedAvatars.clear();
        if (supporters != null) {
            for (NetworkManager.Supporters sup : supporters) {
                mSupportedAvatars.add(sup);
            }
        }
        refreshAvatars();
    }

    public void addAvatars(ArrayList<NetworkManager.Supporters> supporters)
    {
        if (supporters == null) {
            return;
        }

        int oldSize = mSupportedAvatars.size();
        for (NetworkManager.Supporters sup : supporters) {
            if (mSupportedAvatars.contains(sup)) {
                continue;
            }
            mSupportedAvatars.add(sup);
        }

        if (mSupportedAvatars.size() != oldSize) {
            refreshAvatars();
        }
    }


    private class GridAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSupportedAvatars.size();
        }

        @Override
        public Object getItem(int position)
        {
            if (position < 0 || position >= mSupportedAvatars.size()) {
                return null;
            }

            return mSupportedAvatars.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = View.inflate(mSupportView.getContext(), R.layout.list_item_user_avatar, null);

            String url = mSupportedAvatars.get(position).avatar;
            url = QCloudManager.urlImage2(url, ConfigInfo.SIZE_LITTLE_AVATAR);

            CommonManager.displayImage(url,
                    (ImageView) view.findViewById(R.id.userAvatar), new ColorDrawable(Color.TRANSPARENT));

            return view;
        }
    }
}
