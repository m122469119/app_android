package com.dym.film.activity.sharedticket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.SimpleRecyclerAdapter;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.MixController;
import com.dym.film.controllers.SupportController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.ui.CircleImageView;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;
import com.dym.film.views.StretchedListView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/1/14
 */
public class SharedTicketDetailActivity extends BaseViewCtrlActivity
{
    public final static String KEY_INTENT = "SharedTicketDetail";

    public final static String KEY_ID = "id";

    public final static int MAX_COMMENT_LENGTH = 512;

    private SharedTicketDetailViewController mController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mController = this.new SharedTicketDetailViewController();
    }

    @Override
    protected void onUserStateChanged(UserState oldState)
    {
        // 重新刷新一次数据
        mController.refreshSharedTicket();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                CommonManager.dismissSoftInputMethod(this, findViewById(android.R.id.content).getWindowToken());
                break;
        }
        return super.onTouchEvent(event);
    }

    public class SharedTicketDetailViewController extends BaseContentViewController
    {
        /**
         * 数据
         */
        private NetworkManager.SharedTicketRespModel mSharedTicket = null;

        /**
         * RecyclerView
         */
        private LoadMoreRecyclerView mRecyclerView = null;

        private CommentAdapter mCommentAdapter = null;

        private ExRcvAdapterWrapper mAdapterWrapper = null;


        private View mCommentInputLayout = null;

        private EditText mCommentEdit = null;

        private SupportController mController = null;
        private TextView mCommentNumText = null;


        public SharedTicketDetailViewController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_shared_ticket_detail;
        }

        protected void initialize()
        {
            /**
             * 设置回退
             */
            setFinishView(R.id.backButtonImage);

            String id = "";
            Intent intent = mActivity.getIntent();
            Uri uri = intent.getData();
            if (uri != null) {
                id = uri.getQueryParameter(SharedTicketDetailActivity.KEY_ID);
            }
            else {
                long lid = mActivity.getIntent().getLongExtra(SharedTicketDetailActivity.KEY_ID, 0);
                if (lid > 0) {
                    id = String.valueOf(lid);
                }
            }

            if (!TextUtils.isEmpty(id)) {
                MixUtils.showProgressDialog(mActivity, "", true);
                NetworkManager.getInstance().getSharedTicketDetail(id, new HttpRespCallback<NetworkManager.RespSharedTicketDetail>()
                {
                    @Override
                    public void onRespFailure(int code, String msg)
                    {
                        if (!NetWorkUtils.isAvailable(mActivity)) {
                            MixUtils.toastShort(mActivity, "网络异常");
                        }
                        else {
                            MixUtils.toastShort(mActivity, "获取晒票失败");
                        }
                        MixUtils.dismissProgressDialog();
                    }

                    @Override
                    public void runOnMainThread(Message msg)
                    {
                        NetworkManager.RespSharedTicketDetail resp = (NetworkManager.RespSharedTicketDetail) msg.obj;

                        mSharedTicket = resp.stub;

                        MixUtils.dismissProgressDialog();
                        initializeViews();
                    }
                });
                return;
            }

            mSharedTicket = (NetworkManager.SharedTicketRespModel) CommonManager.getData(SharedTicketDetailActivity.KEY_INTENT);
            initializeViews();
        }

        public void refreshSharedTicket()
        {
            if (mSharedTicket == null) {
                return;
            }
            MixUtils.showProgressDialog(mActivity, "", true);
            NetworkManager.getInstance().getSharedTicketDetail(
                    String.valueOf(mSharedTicket.stubID), new HttpRespCallback<NetworkManager.RespSharedTicketDetail>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespSharedTicketDetail resp = (NetworkManager.RespSharedTicketDetail) msg.obj;
                    mSharedTicket = resp.stub;

                    MixUtils.dismissProgressDialog();
                    loadAllData();
                }
            });
        }

        protected void initializeViews()
        {
            if (mSharedTicket == null) {
                finish();
                return;
            }
            /**
             * 初始化评论
             */
            setOnClickListener(R.id.share);

            /**
             * 设置评论输入框
             */
            View overlay = findViewById(R.id.commentOverLayout);
            overlay.setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent)
                {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mCommentEdit.clearFocus();
                            break;
                    }
                    return false;
                }
            });
            mCommentInputLayout = findViewById(R.id.commentInputLayout);
            mCommentEdit = (EditText) findViewById(R.id.commentEdit);
            mCommentEdit.clearFocus();
            mCommentEdit.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View view, boolean b)
                {
                    if (b) {
                        CommonManager.showSoftInputMethod(mActivity);
                    }
                    else {
                        CommonManager.dismissSoftInputMethod(mActivity, view.getWindowToken());
                    }
                }
            });
            setOnClickListener(R.id.commentButton);
            controlKeyboardLayout(overlay, mCommentInputLayout);

            /**
             * 初始化Header
             */
            View header = View.inflate(mActivity, R.layout.layout_shared_ticket_detail_header, null);
            header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            /**
             * 设置recyclerView
             */
            mCommentAdapter = new CommentAdapter(mActivity);
            LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mActivity);
            mAdapterWrapper = new ExRcvAdapterWrapper<>(mCommentAdapter, mLinearLayoutManager);

            mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.loadMoreRecyclerView);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerView.setLinearLayoutManager(mLinearLayoutManager);
            mRecyclerView.showFooterMargin();
            mRecyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener()
            {
                @Override
                public void onNeedLoadMore()
                {
                    loadMoreCommentData();
                }
            });
            mRecyclerView.setAdapter(mAdapterWrapper);

            mAdapterWrapper.setHeaderView(header);
            mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());

            mAdapterWrapper.notifyDataSetChanged();

            initializeHeaderView(header);
            loadAllData();
        }

        private void initializeHeaderView(View header)
        {
            NetworkManager.UserModel writer = mSharedTicket.writer;

            ImageView mHeaderImageView = (CircleImageView) header.findViewById(R.id.userHeadImage);
            if (writer != null) {
                /**
                 * 设置用户头像
                 */
                CommonManager.displayAvatar(writer.avatar, mHeaderImageView);

                /**
                 * 设置名字
                 */
                TextView userName = (TextView) header.findViewById(R.id.userNameTextView);
                userName.setText(writer.name);

                /**
                 * 设置性别
                 */
                ImageView genderImage = (ImageView) header.findViewById(R.id.genderImage);
                genderImage.setImageResource(writer.gender == 1 ? R.drawable.ic_gender_male : R.drawable.ic_gender_female);
            }

            /**
             * 设置态度
             */
            ArrayList<String> tags = mSharedTicket.tags;
            MixController.setupAttLayout(header.findViewById(R.id.attLayout),
                    mSharedTicket.opinion, tags == null || tags.isEmpty() ? "" : tags.get(0));

            /**
             * 设置晒票内容
             */
            TextView mContentTextView = (TextView) header.findViewById(R.id.content);
            mContentTextView.setText(mSharedTicket.content);

            /**
             * 设置图片
             */
            ImageView mTicketImage = (ImageView) header.findViewById(R.id.ticketImageView);
            String url = "";
            if (mSharedTicket.stubImage != null) {
                url = QCloudManager.urlImage2(mSharedTicket.stubImage.url, 500);
            }
            LogUtils.e(TAG, "URL: " + url);
            CommonManager.displayImage(url, mTicketImage, R.drawable.ic_default_loading_img);
            mTicketImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public synchronized void onClick(View view)
                {
                    if (mSharedTicket.stubImage != null) {
                        Intent intent = new Intent(mActivity, SharedTicketOnlyImageActivity.class);
                        intent.putExtra(SharedTicketOnlyImageActivity.KEY_URL, mSharedTicket.stubImage.url);

                        mActivity.startActivity(intent);
                    }
                }
            });

            /**
             * 评论的个数
             */
            mCommentNumText = (TextView) header.findViewById(R.id.commentNumText);
            mCommentNumText.setText("0");

            mController = new SupportController(header.findViewById(R.id.supportLayout),
                    mSharedTicket.stubID, mSharedTicket.supported, new ArrayList<NetworkManager.Supporters>(), SupportController.TYPE_SHARED_TICKET);
            mController.setupCommentView(mSharedTicket.showOffTime, new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onComBtnClicked();
                }
            });
        }

        /**
         * 开始请求数据
         */
        public void loadAllData()
        {
            /**
             * 获取点赞人
             */
            NetworkManager.getInstance().getShareTicketSupporters(0, 20,
                    mSharedTicket.stubID, new HttpRespCallback<NetworkManager.RespGetSupporters>()
                    {
                        @Override
                        public void onRespFailure(int code, String msg)
                        {
                            //
                        }

                        @Override
                        public void runOnMainThread(Message msg)
                        {
                            NetworkManager.RespGetSupporters resp = (NetworkManager.RespGetSupporters) msg.obj;

                            mController.setAvatars(resp.supporters);
                        }
                    });

            /**
             * 获取评论
             */
            mCurPageIndex = 0;
            loadMoreCommentData();
        }

        private int mCurPageIndex = 0;
        private final static int PAGE_LIMIT = 10;
        private boolean mIsLoadingMore = false;

        private synchronized void loadMoreCommentData()
        {
            if (mIsLoadingMore) {
                return;
            }
            mIsLoadingMore = true;

            NetworkManager.getInstance().getSharedTicketComments(mCurPageIndex, PAGE_LIMIT, mSharedTicket.stubID, new HttpRespCallback<NetworkManager.RespSharedTicketComment>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespSharedTicketComment comments = (NetworkManager.RespSharedTicketComment) msg.obj;
                    NetworkManager.SharedTicketCommentResult result = comments.result;
                    if (result == null) {
                        onRespFailure(0, "");
                        return;
                    }

                    int size = result.comments == null ? 0 : result.comments.size();
                    if (mCurPageIndex == 0) {
                        mCommentAdapter.clear();
                    }
                    mCommentAdapter.appendAll(result.comments, false);

                    if (size < PAGE_LIMIT) {
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    }
                    else {
                        mCurPageIndex += 1;
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }

                    mCommentNumText.setText(String.valueOf(result.sum));
                }
            });
        }

        private void endLoadMore(int state)
        {
            mIsLoadingMore = false;

            mRecyclerView.loadMoreFinished(state);
            switch (state) {
                case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                    mRecyclerView.setFooterClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            mRecyclerView.startLoadMore();
                        }
                    });
                    break;

                case LoadMoreRecyclerView.LOAD_MORE_NO_MORE:
                    if (mCommentAdapter.getItemCount() == 0) {
                        mRecyclerView.setFooterText("暂无评论，快来抢个沙发");
                    }
                    else {
                        mRecyclerView.setFooterText("没有更多了");
                    }
                default:
                    mRecyclerView.setFooterClickListener(null);
                    break;
            }
        }

        /**
         * 发表评论
         */
        private boolean mIsCommenting = false;
        private void commentThisSharedTicket()
        {
            if (!UserInfo.isLogin) {
                MixUtils.toastShort(mActivity, "登录评论");
                CommonManager.startLoginActivity(mActivity);
                return;
            }

            if (mCommentEdit.getText().toString().isEmpty()) {
                MixUtils.toastShort(mActivity, "评论不能为空");
                return;
            }

            if (mIsCommenting) {
                MixUtils.toastShort(mActivity, "正在发布...");
                return;
            }
            mIsCommenting = true;
            MixUtils.showProgressDialog(mActivity, "", true);

            NetworkManager.ReqCommentShareTicket req = new NetworkManager.ReqCommentShareTicket();
            req.stubID = mSharedTicket.stubID;
            req.comment =  mCommentEdit.getText().toString();

            if (SID > 0) {
                req.subFollow = new NetworkManager.ReqStubComment();
                req.subFollow.stubFollowID = SID;

                if (SSID > 0) {
                    req.subFollow.refSubFollow = SSID;
                }
            }

            NetworkManager.getInstance().commentShareTicket(req, new HttpRespCallback<NetworkManager.RespCommentShareTicket>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    mIsCommenting = false;
                    MixUtils.dismissProgressDialog();
                    MixUtils.toastShort(mActivity, "发表失败!");
                }

                @Override
                protected void runOnMainThread(Message msg)
                {
                    NetworkManager.RespCommentShareTicket resp = (NetworkManager.RespCommentShareTicket) msg.obj;
                    NetworkManager.RespCommentFollowInfo info = resp.followInfo;

                    if (info != null && !info.subFollows.isEmpty()) {
                        mCommentAdapter.updateStubComment(info);
                    }
                    else {
                        /**
                         * 重新刷新评论
                         */
                        mRecyclerView.scrollToPosition(1);
                        mCurPageIndex = 0;
                        loadMoreCommentData();
                    }

                    mCommentEdit.setText("");
                    SCaches.put(SID, "");
                    SSCaches.put(SSID, "");
                    POS = -1;
                    SID = 0;
                    SSID = 0;
                    mCommentEdit.setHint("说点什么");

                    mIsCommenting = false;
                    MixUtils.dismissProgressDialog();

                }
            });
        }

        /**
         * @param root         最外层布局，需要调整的布局
         * @param scrollToView 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
         */
        private void controlKeyboardLayout(final View root, final View scrollToView)
        {
            root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
            {
                int mLastHeight = 0;
                int mLastBottom = -1;

                @Override
                public void onGlobalLayout()
                {
                    Rect rect = new Rect();
                    root.getWindowVisibleDisplayFrame(rect);

                    LogUtils.e(TAG, "Rect: " + rect);

                    if (mLastBottom == -1) {
                        mLastBottom = rect.bottom;
                        return;
                    }

                    int nb = rect.bottom;
                    int ob = mLastBottom;

                    if (nb < ob) {
                        // 键盘显示了， 滑上去
                        int[] location = new int[2];
                        scrollToView.getLocationInWindow(location);
                        int scrollHeight = (location[1] + scrollToView.getHeight()) - nb;

                        root.scrollTo(0, scrollHeight);
                        mLastHeight = scrollHeight;
                    }
                    else if (nb > ob) {
                        // 键盘隐藏了, 滑下来
                        root.scrollTo(0, 0);
                    }

                    if (nb != ob) {
                        mLastBottom = nb;
                    }
                }
            });
        }

        /**
         * 分享晒票
         */
        private ShareManager mShareManager = null;

        private void shareToSocial()
        {
            if (mShareManager == null) {
                mShareManager = new ShareManager(mActivity);
            }

            mShareManager.setTitle("公证电影｜看电影晒票根");
            mShareManager.setTitleUrl(NetworkManager.getShareUrl(mSharedTicket.shareUrl));
            if (mSharedTicket != null) {
                String film = "";
                if (!mSharedTicket.tags.isEmpty() && !TextUtils.isEmpty(mSharedTicket.tags.get(0))) {
                    film += "《" + mSharedTicket.tags.get(0) + "》 ";
                }
                mShareManager.setText(film + mSharedTicket.content);
                mShareManager.setWebUrl(NetworkManager.getShareUrl(mSharedTicket.shareUrl));
                if (mSharedTicket.stubImage != null) {
                    mShareManager.setImageUrl(mSharedTicket.stubImage.url);
                }
            }
            mShareManager.showShareDialog(mActivity);
        }

        @Override
        protected void onViewClicked(@NonNull View v)
        {
            switch (v.getId()) {
                case R.id.commentButton:
                    MatStatsUtil.eventClick(v.getContext(), MatStatsUtil.SHOW_COMMENT);
                    if (!UserInfo.isLogin) {
                        Toast.makeText(mActivity, "登录评论", Toast.LENGTH_SHORT).show();
                        CommonManager.startLoginActivity(mActivity);
                        return;
                    }
                    commentThisSharedTicket();
                    break;

                case R.id.share:
                    MatStatsUtil.eventClick(v.getContext(), MatStatsUtil.SHOW_SHARE);
                    shareToSocial();
                    break;
            }
        }

        private class CommentAdapter extends SimpleRecyclerAdapter<NetworkManager.SharedTicketComment>
        {

            public CommentAdapter(@NonNull Activity activity)
            {
                super(activity);
            }

            public void updateStubComment(NetworkManager.RespCommentFollowInfo resp)
            {
                NetworkManager.SharedTicketComment comment = getItem(POS);
                if (comment != null && resp != null) {
                    comment.subFollows.clear();
                    comment.subFollows.addAll(resp.subFollows);
                    notifyItemChanged(POS);
                }
            }

            @Override
            public View onCreateView(ViewGroup parent, int viewType)
            {
                View view = View.inflate(mActivity, R.layout.list_item_share_ticket_comment, null);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                return view;
            }

            @Override
            public void onBindModelToView(SimpleViewHolder viewHolder, final int position)
            {
                final NetworkManager.SharedTicketComment comment = getItem(position);
                final NetworkManager.UserModel writer = comment.owner;

                ImageView mHeaderImageView = (CircleImageView) viewHolder.findView(R.id.avatar);

                /**
                 * 设置用户头像
                 */
                CommonManager.displayAvatar(writer.avatar, mHeaderImageView);

                /**
                 * 设置名字
                 */
                TextView userName = (TextView) viewHolder.findView(R.id.userName);
                userName.setText(writer.name);

                /**
                 * 设置性别
                 */
                ImageView genderImage = (ImageView) viewHolder.findView(R.id.genderImage);
                genderImage.setImageResource(writer.gender == 1 ? R.drawable.ic_gender_male : R.drawable.ic_gender_female);

                /**
                 * 设置内容
                 */
                TextView commentContent = (TextView) viewHolder.findView(R.id.commentContent);
                commentContent.setText(comment.comment);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View view)
                    {
                        POS = position;
                        onSComClicked(comment.followID, writer.name);
                    }
                });

                /**
                 * 设置子评论
                 */
                final ArrayList<NetworkManager.StubComment> stubComments = comment.subFollows;
                if (stubComments.isEmpty()) {
                    viewHolder.findView(R.id.cCommentLayout).setVisibility(View.GONE);
                    return;
                }

                viewHolder.findView(R.id.cCommentLayout).setVisibility(View.VISIBLE);

                StretchedListView listView = (StretchedListView) viewHolder.findView(R.id.cCommentList);
                listView.setAdapter(new BaseAdapter()
                {
                    @Override
                    public int getCount()
                    {
                        return stubComments.size();
                    }

                    @Override
                    public Object getItem(int i)
                    {
                        return stubComments.get(i);
                    }

                    @Override
                    public long getItemId(int i)
                    {
                        return i;
                    }

                    @Override
                    public View getView(int i, View convertView, ViewGroup viewGroup)
                    {
                        View resView = null;
                        if (convertView != null) {
                            resView = (View) convertView.getTag();
                        }

                        if (resView == null){
                            resView = View.inflate(mActivity, R.layout.list_item_comment_comment, null);
                            convertView = resView;
                            convertView.setTag(resView);
                        }

                        final NetworkManager.StubComment stubCom = stubComments.get(i);
                        TextView text = (TextView) resView.findViewById(R.id.comment);

                        SpannableString owner = colorSpan(stubCom.owner.name + ": ", Color.WHITE);
                        text.setText(owner);
                        if (stubCom.target != null) {
                            text.append(colorSpan(atName(stubCom.target.name), Color.parseColor("#f5a623")));
                        }
                        text.append(colorSpan(stubCom.comment, Color.parseColor("#989898")));

                        return resView;
                    }
                });
                /**
                 * 对评论的的评论进行回复
                 */
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                    {
                        POS = position;
                        final NetworkManager.StubComment stubCom = stubComments.get(i);
                        onSSComClicked(comment.followID, stubCom.subFollowID, stubCom.owner.name);
                    }
                });
            }
        }

        private void onComBtnClicked()
        {
            SID = 0;
            SSID = 0;

            mCommentEdit.setHint("说点什么");
            mCommentEdit.requestFocus();
        }

        private void onSComClicked(long id, String name)
        {
            /**
             * 先缓存
             */
            String text = mCommentEdit.getText().toString();
            if (SSID == 0) {
                SCaches.put(SID, text);
            }
            else {
                SSCaches.put(SSID, text);
            }

            /**
             * 设置hint
             */
            mCommentEdit.setHint(String.valueOf("回复 " + name));

            /**
             * 取出缓存
             */
            mCommentEdit.setText("");
            if (SCaches.containsKey(id)) {
                mCommentEdit.setText(SCaches.get(id));
            }
            SID = id;
            SSID = 0;
            mCommentEdit.setSelection(mCommentEdit.length());

            mCommentEdit.requestFocus();
        }

        private void onSSComClicked(long sid, long ssid, String name)
        {
            /**
             * 先缓存
             */
            String text = mCommentEdit.getText().toString();
            if (SSID == 0) {
                SCaches.put(SID, text);
            }
            else {
                SSCaches.put(SSID, text);
            }

            /**
             * 设置hint
             */
            mCommentEdit.setHint(String.valueOf("回复 " + name));

            /**
             * 取出缓存
             */
            mCommentEdit.setText("");
            if (SSCaches.containsKey(ssid)) {
                mCommentEdit.setText(SSCaches.get(ssid));
            }
            SID = sid;
            SSID = ssid;
            mCommentEdit.setSelection(mCommentEdit.length());

            mCommentEdit.requestFocus();
        }

        /**
         * 当前进行回复的 position
         */
        public int POS = -1;

        /**
         * 当前进行回复的 id
         */
        public long SID = 0;
//        public String SName = "";

        public long SSID = 0;
//        public String SSName = "";

        /**
         * 对评论的缓存
         */
        public HashMap<Long, String> SCaches = new HashMap<>();
        /**
         * 对评论的回复的缓存
         */
        public HashMap<Long, String> SSCaches = new HashMap<>();
    }

    private String atName(String name) {
        return "@" + name + " ";
    }

    private SpannableString colorSpan(String s, int color)
    {
        SpannableString span = new SpannableString(s);
        span.setSpan(new ForegroundColorSpan(color), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return span;
    }
}
