package com.dym.film.activity.filmreview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.activity.home.SingleFilmReviewActivity;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.BaseViewController;
import com.dym.film.controllers.ExceptionLayoutViewController;
import com.dym.film.controllers.SupportController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MixUtils;
import com.dym.film.views.XWebView;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */


/**
 * 影评详情的页面
 */
public class FilmReviewDetailActivity extends BaseViewCtrlActivity
{
    private final static String TAG = "FilmReviewDetail";

    public final static String KEY_FILM_REVIEW_DATA = "stubID";

    private FilmReviewDetailViewController mViewController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mViewController = new FilmReviewDetailViewController();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        setIntent(intent);

        if (mViewController == null) {
            mViewController = new FilmReviewDetailViewController();
        }
        mViewController.onNewIntent(intent);
    }

    @Override
    public void onDestroy()
    {
        if (mViewController != null) {
            mViewController.onDestroy();
        }

        super.onDestroy();
    }

    private class FilmReviewDetailViewController extends BaseContentViewController
    {
        /**
         * 进入页面加载的progress bar和加载失败的 textview
         */
        private ExceptionLayoutViewController mExcepController = null;


        private ScrollView mScrollView = null;

        private LinearLayout mContentLayout = null;

        private XWebView mReviewContentWebView = null;

        private SupportController mSupportCtrl = null;

        private LinearLayout mFilmLayout = null;
        private LinearLayout mAboutFilmReviewLayout = null;

        private ViewGroup.LayoutParams mFilmLayoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, CommonManager.dpToPx(94));

        private ShareManager mShareManager = null;

        /**** 数据 ***/

        private boolean mWebViewFinished = false;
        private boolean mAboutReviewFinished = false;


        /**
         * 影评的分享链接
         */
        private long mReviewID = 0;
        private String mReviewTitle = "";
        private String mReviewShareUrl = "";
        private String mReviewPost = "";
        private String mReviewSummary = "";

        private ArrayList<String> mFollowerAvatars = new ArrayList<>();

        private ArrayList<ReviewDetailFilmViewController> mFilmCtrls = new ArrayList<>();
        private String filmID=null;
        private String filmName = "";

        protected FilmReviewDetailViewController()
        {
            super(true);
            initialize();
        }

        protected void initialize()
        {
            if (initializeIntentData()) {
                initializeDetailFilmReviewView();
            }
        }

        public void onNewIntent(Intent intent)
        {
            if (initializeIntentData()) {
                initializeDetailFilmReviewView();
            }
        }

        public void onDestroy()
        {
            if (mContentLayout != null) {
                mContentLayout.removeAllViews();
            }
            if (mReviewContentWebView != null) {
                mReviewContentWebView.removeAllViews();
                mReviewContentWebView.clearCache(true);
                mReviewContentWebView.destroy();
                mReviewContentWebView = null;
            }
        }

        private boolean initializeIntentData()
        {
            Intent intent = mActivity.getIntent();
            mReviewID = intent.getLongExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, 0);
            filmID = intent.getStringExtra("filmID");
            if (mReviewID == 0) {
                Uri uri = intent.getData();
                if (uri != null) {
                    String rid = uri.getQueryParameter(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA);
                    String fid = uri.getQueryParameter("filmID");
                    try {
                        mReviewID = Long.valueOf(rid);
                    }
                    catch (Exception e) {
                        mReviewID = 0;
                    }

                    if (!TextUtils.isEmpty(fid)) {
                        filmID = fid;
                    }
                }
                if (mReviewID == 0) {
                    MixUtils.toastShort(mActivity, "没有数据");
                    finish();
                    return false;
                }
            }
            LogUtils.e(TAG, "ReviewID: " + mReviewID + " FilmID: " + filmID);
            return true;
        }

        private void initializeDetailFilmReviewView()
        {
            setFinishView(R.id.backButtonImage);
            setOnClickListener(R.id.titleShareButton);

            mScrollView = (ScrollView) findViewById(R.id.contentLoadingLayout);
            mExcepController = new ExceptionLayoutViewController(
                    mActivity, new ExceptionLayoutViewController.ViewCallback()
            {
                @Override
                public void onExceptionViewClicked()
                {
                    mExcepController.progress();
                    refresh();
                }
            }, findViewById(R.id.exceptionPage));
            mExcepController.progress();
            mScrollView.setVisibility(View.INVISIBLE);

            initializeHeaderView();

            refresh();
        }


        /**
         * 初始化header
         */
        private void initializeHeaderView()
        {
            /**
             * 设置电影的item
             */
            mFilmLayout = (LinearLayout) findViewById(R.id.filmLayout);
            mFilmLayout.removeAllViews();

            mAboutFilmReviewLayout = (LinearLayout) findViewById(R.id.aboutFilmReviewLayout);
            mAboutFilmReviewLayout.removeAllViews();
        }


        /**
         * 从服务器更新数据
         */
        private void refresh()
        {
            /**
             * 获取详情
             */
            NetworkManager.getInstance().getReviewDetail(mReviewID, filmID, new HttpRespCallback<NetworkManager.RespGetReviewDetail>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.ReviewDetailRespModel review = ((NetworkManager.RespGetReviewDetail) msg.obj).cinecism;

                    mReviewTitle = review.title;
                    mReviewShareUrl = review.shareUrl;
                    mReviewSummary = review.summary;
                    TextView view = (TextView) findViewById(R.id.title);
                    view.setText(mReviewTitle);

                    setupCriticViews(review.critic);

                    mSupportCtrl = new SupportController(findViewById(R.id.supportLayout),
                            review.cinecismID, review.supported, review.supporters.avatars);

                    /**
                     * 加载 html
                     */
                    if (mContentLayout == null) {
                        mContentLayout = (LinearLayout) findViewById(R.id.contentLayout);
                    }
                    mContentLayout.removeAllViews();

                    mReviewContentWebView = new XWebView(mActivity.getApplicationContext());
                    mReviewContentWebView.setBackgroundResource(R.color.main_bg_color);

                    mReviewContentWebView.loadDataWithBaseURL(NetworkManager.URL_BASE,
                            review.content, "text/html", NetworkManager.DEF_CHARSET, null);
                    mContentLayout.addView(mReviewContentWebView);
                    mReviewContentWebView.setWebViewClient(new WebViewClient()
                    {
                        @Override
                        public void onPageFinished(WebView view, String url)
                        {
                            LogUtils.e(TAG, "Page Loading Finished....");
                            mWebViewFinished = true;
                            showContent();
                        }
                    });
                }
            });

            /**
             * 获取电影信息
             */
            NetworkManager.getInstance().getReviewFilmInfo(mReviewID, new HttpRespCallback<NetworkManager.RespGetReviewFilmInfo>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    mExcepController.show();
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespGetReviewFilmInfo info = (NetworkManager.RespGetReviewFilmInfo) msg.obj;

                    mFilmCtrls.clear();
                    if (info.films != null && !info.films.isEmpty()) {
                        int minSize = Math.min(info.films.size(), 4);
                        mReviewPost = info.films.get(0).post;
                        filmID = String.valueOf(info.films.get(0).filmID);
                        filmName = String.valueOf(info.films.get(0).name);
                        for (int i = 0; i < minSize; ++i) {
                            NetworkManager.FilmRespModel film = info.films.get(i);
                            ReviewDetailFilmViewController ctrl = new ReviewDetailFilmViewController(mActivity);
                            ctrl.bindModelToView(film);
                            mFilmCtrls.add(ctrl);
                            mFilmLayout.addView(ctrl.getRootView(), mFilmLayoutParams);

                            NetworkManager.getInstance().getReviewAboutReview(mReviewID, film.filmID, 0, 3, new GetAboutReviewHttpHandler(film));
                        }
                    }

                    if (mFilmCtrls.isEmpty()) {
                        findViewById(R.id.aboutFilmLayout).setVisibility(View.GONE);
                        mAboutReviewFinished = true;
                        showContent();
                    }
                }
            });

        }

        private void setupCriticViews(final NetworkManager.CriticRespModel critic)
        {
            if (critic == null) {
                return;
            }

            /**
             * 影评人头像
             */
            ImageView criticAvatar = (ImageView) findViewById(R.id.criticAvatar);
            String avatar = TextUtils.isEmpty(critic.avatar) ? "" : critic.avatar;
            CommonManager.displayAvatar(avatar, criticAvatar);

            TextView criticName = (TextView) findViewById(R.id.criticNameText);
            criticName.setText(critic.name);

            TextView criticIntro = (TextView) findViewById(R.id.criticIntroText);
            criticIntro.setText(critic.title);

            TextView criticSum = (TextView) findViewById(R.id.criticFilmReviewNum);
            criticSum.setText(String.valueOf(critic.cinecismNum));


            findViewById(R.id.criticHeader).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
                    Intent intent = new Intent(mActivity, CriticDetailActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    mActivity.startActivity(intent);
                }
            });
        }

        /**
         * 如果所有的东西都加载好了，就显示
         */
        protected void showContent()
        {
            if (mWebViewFinished && mAboutReviewFinished) {
                mScrollView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mExcepController.hide();
                        mScrollView.setVisibility(View.VISIBLE);
                        mScrollView.scrollTo(0, 0);
                    }
                }, 200);
            }
        }

        @Override
        protected void onViewClicked(@NonNull View view)
        {
            switch (view.getId()) {
                case R.id.titleShareButton:
                    if (TextUtils.isEmpty(mReviewShareUrl)) {
                        break;
                    }

                    if (mShareManager == null) {
                        mShareManager = new ShareManager(mActivity);
                    }
                    mShareManager.setTitle("公证电影 ｜ " + mReviewTitle);
                    mShareManager.setText(mReviewSummary);
                    mShareManager.setWebUrl(NetworkManager.getShareUrl(mReviewShareUrl));
                    mShareManager.setTitleUrl(NetworkManager.getShareUrl(mReviewShareUrl));
                    mShareManager.setImageUrl(mReviewPost);
                    mShareManager.showShareDialog(mActivity);
                    break;
            }
        }


        private class GetAboutReviewHttpHandler extends HttpRespCallback<NetworkManager.RespReviewAboutReview>
        {
            private NetworkManager.FilmRespModel mFilm = null;

            public GetAboutReviewHttpHandler(NetworkManager.FilmRespModel model)
            {
                mFilm = model;
            }

            @Override
            public void onRespFailure(int code, String msg)
            {
                mExcepController.show();
            }


            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespReviewAboutReview reviews = (NetworkManager.RespReviewAboutReview) msg.obj;

                if (reviews.cinecisms != null && !reviews.cinecisms.isEmpty()) {

                    findViewById(R.id.aboutFilmLayout).setVisibility(View.VISIBLE);
                    mAboutFilmReviewLayout.removeAllViews();
                    for (NetworkManager.FilmReviewRespModel review : reviews.cinecisms) {
                        if (review.film == null) {
                            review.film = new ArrayList<>();
                            review.film.add(mFilm);
                        }

                        LittleViewHolder holder = new LittleViewHolder(View.inflate(mActivity, R.layout.list_item_simple_film_review, null));

                        holder.bindView(review);

                        mAboutFilmReviewLayout.addView(holder.itemView,
                                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }

                    TextView text = (TextView) findViewById(R.id.seeAll);
                    text.setText(String.valueOf("查看全部 " + reviews.sum + " 条"));
                    text.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Intent intent = new Intent(mActivity, SingleFilmReviewActivity.class);
                            intent.putExtra("filmId", filmID == null ? "0" : filmID);
                            intent.putExtra("filmName", filmName);
                            startActivity(intent);
                        }
                    });
                }
                else {
                    findViewById(R.id.aboutFilmLayout).setVisibility(View.GONE);
                }

                mAboutReviewFinished = true;
                showContent();
            }
        }


        public class LittleViewHolder
        {
            private View itemView = null;
            public LittleViewHolder(View itemView)
            {
                this.itemView = itemView;
            }


            public View findView(int id)
            {
                return itemView.findViewById(id);
            }

            public void bindView(NetworkManager.FilmReviewRespModel review)
            {
                NetworkManager.CriticRespModel writer = review.writer;

                /**
                 * 设置头像
                 */
                ImageView avatar = (ImageView) findView(R.id.criticAvatar);
                CommonManager.displayAvatar(writer.avatar, avatar);

                /**
                 * 设置态度
                 */
                ImageView opinionImage = (ImageView) findView(R.id.opinionImage);
                opinionImage.setImageResource(review.opinion == 1 ? R.drawable.ic_is_worth_white : R.drawable.ic_is_not_worth);

                /**
                 * 设置来源
                 */
                TextView textView = (TextView) findView(R.id.fromResourceText);
                textView.setText(review.srcMedia);

                /**
                 * 设置评分
                 */
                TextView scoreText = (TextView) findView(R.id.resourceScoreText);
                scoreText.setVisibility(View.INVISIBLE);
//                if (scoreText != null) {
//                    float score = 0;
//                    try {
//                        score = Float.valueOf(review.srcScore);
//                    }
//                    catch (Exception e) {
//                        score = 0;
//                    }
//
//                    if (score > 0) {
//                        scoreText.setText(review.srcScore);
//                        scoreText.setVisibility(View.VISIBLE);
//                    }
//                    else {
//                        scoreText.setVisibility(View.GONE);
//                    }
//                }

                /**
                 * 设置内容
                 */
                TextView contentText = (TextView) findView(R.id.summary);
                contentText.setText(TextUtils.isEmpty(review.summary) ? review.title : review.summary);

                /**
                 * 设置影评人名字
                 */
                TextView writerName = (TextView) findView(R.id.criticName);
                writerName.setText(writer.name);

                /**
                 * 设置时间
                 */
                TextView time = (TextView) findView(R.id.timeText);
                CommonManager.setTime(time, review.createTime == null ? 0 : review.createTime.getTime());

                /**
                 * 设置item点击事件
                 */
                final long id = review.cinecismID;
                itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        startFilmReviewDetailActivity(id);
                    }
                });
            }

            /**
             * 点击打开影评详情页面
             */
            public void startFilmReviewDetailActivity(long id)
            {
                Intent intent = new Intent(mActivity, FilmReviewDetailActivity.class);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
//            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                mActivity.startActivity(intent);
            }
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_film_review_detail;
        }

    }

    /**
     * 控制一个影评的相关电影的item view
     */
    private class ReviewDetailFilmViewController extends BaseViewController
    {
        public final static String TAG = "RDFVCtrl";

        private NetworkManager.FilmRespModel mFilmModel = null;

        /**
         * 电影海报图片
         */
        private ImageView mFilmPost = null;

        /**
         * 电影名
         */
        private TextView mFilmName = null;

        /**
         * 上映时间和地区
         */
        private TextView mFilmAreaTime = null;

        /**
         * 演员
         */
        private TextView mFilmCast = null;

        /**
         * 银幕指数
         */
        private TextView mFilmIndex = null;
        private TextView mFilmIndexPercent = null;


        public ReviewDetailFilmViewController(@NonNull Activity activity)
        {
            super(activity, R.layout.layout_review_detail_film);

            initializeViews();
        }

        private void initializeViews()
        {
            mFilmPost = (ImageView) findViewById(R.id.filmPost);
            mFilmName = (TextView) findViewById(R.id.filmNameText);
            mFilmAreaTime = (TextView) findViewById(R.id.filmAreaTime);
            mFilmCast = (TextView) findViewById(R.id.filmCast);
            mFilmIndex = (TextView) findViewById(R.id.filmIndex);
            mFilmIndexPercent = (TextView) findViewById(R.id.filmIndexPercent);
        }

        public NetworkManager.FilmRespModel getFilmRespModel()
        {
            return mFilmModel;
        }

        public void bindModelToView(NetworkManager.FilmRespModel film)
        {
            if (film == null) {
                return;
            }
            mFilmModel = film;

            String url = (film.post == null ? "" : film.post);
            //Loge(TAG, "Film Url: " + url);
            CommonManager.displayFilmPost(url, mFilmPost);

            mFilmName.setText(film.name);

            mFilmAreaTime.setText(String.valueOf(film.country + " / " + film.releaseDate));

            mFilmCast.setText(String.valueOf("演员: " + film.cast));

            String index = (film.index.equals("0.0") ||
                    film.index.equals("0") || film.index.equals("0.00")) ? "" : film.index;
            if (TextUtils.isEmpty(index)) {
                findViewById(R.id.indexLayout).setVisibility(View.GONE);
            }
            else {
                findViewById(R.id.indexLayout).setVisibility(View.VISIBLE);
                mFilmIndex.setText(index);
            }

            mRootView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    Intent intent = new Intent(mActivity, FilmDetailActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(FilmDetailActivity.KEY_FILM_ID, String.valueOf(mFilmModel.filmID));
                    intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, String.valueOf(mFilmModel.name));
                    mActivity.startActivity(intent);
                }
            });
        }

    }

}
