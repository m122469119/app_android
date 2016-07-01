package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.price.CinemaActivityNew;
import com.dym.film.activity.price.PriceActivityNew;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.model.FilmBaseInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.model.FilmReviewListInfo;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.MyScrollView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
@Deprecated
public class PreFilmDetailActivity extends BaseActivity
{
    public final static String KEY_FILM_ID = "filmID";
    public final static String KEY_FILM_NAME = "name";
    private MyScrollView scrollView;
    private TextView tvTitleFilmName;
    private TextView tvFilmName;
    private ImageView imgFilmBanner;
    private ImageView imgFilmCover;
    private ImageView imgFilmPlay;
    private TextView tvFilmDate;
    private TextView tvFilmDirector;
    private TextView tvFilmActor;
    private TextView tvFilmIntro;
    private LinearLayout layFilmHot;

    private int distance;//标题滑动该距离，开始显示
    private SwipeRefreshLayout mRefreshLayout;
    private FilmBaseInfo.FilmModel filmBaseData;
    private LayoutInflater mInflater;
    private TextView tvFilmHotTheme;
    private long filmId = 12377;
    private String filmName = "";
    private ImageButton btnLike;
    private float curAlpha;

    private TextView tvFilmHighPraiseCount;
    private TextView tvFilmLowPraiseCount;
    private LinearLayout layFilmReview;
    private TextView tvShowMoreFilmReview;
    private LinearLayout layFilmReviewTitle;
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_pre_film_detail;
    }

    @Override
    protected void initVariable()
    {
        distance = DimenUtils.dp2px(PreFilmDetailActivity.this, 100);
        mInflater = getLayoutInflater();
    }

    @Override
    protected void findViews()
    {
        //顶部标题栏
        scrollView = $(R.id.scrollView);
        tvTitleFilmName = (TextView) findViewById(R.id.tvTitleFilmName);
        btnLike = (ImageButton) findViewById(R.id.btnLike);
        //影片基本信息
        imgFilmBanner = $(R.id.imgFilmBanner);
        imgFilmCover = $(R.id.imgFilmCover);
        imgFilmPlay= $(R.id.imgFilmPlay);
        tvFilmName = (TextView) findViewById(R.id.tvFilmName);
        tvFilmDate = (TextView) findViewById(R.id.tvFilmDate);
        tvFilmDirector = (TextView) findViewById(R.id.tvFilmDirector);
        tvFilmActor = (TextView) findViewById(R.id.tvFilmActor);
        tvFilmIntro = (TextView) findViewById(R.id.tvFilmIntro);

        //影评相关
        tvFilmHighPraiseCount = (TextView) findViewById(R.id.tvFilmHighPraiseCount);
        tvFilmLowPraiseCount = (TextView) findViewById(R.id.tvFilmLowPraiseCount);
        layFilmReview = (LinearLayout) findViewById(R.id.layFilmReview);
        tvShowMoreFilmReview = (TextView) findViewById(R.id.tvShowMoreFilmReview);

        layFilmReviewTitle = (LinearLayout) findViewById(R.id.layFilmReviewTitle);
        //资讯相关
        tvFilmHotTheme = (TextView) findViewById(R.id.tvFilmHotTheme);
        layFilmHot = (LinearLayout) findViewById(R.id.layFilmHot);

        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getDataFromNet();
            }
        });
//
    }

    @Override
    protected void initData()
    {
        Intent intent=getIntent();
        filmId = Long.parseLong(intent.getStringExtra(KEY_FILM_ID));
        filmName = intent.getStringExtra(KEY_FILM_NAME);
        curAlpha=0;
        tvTitleFilmName.setAlpha(1);
        tvTitleFilmName.setText(filmName);
//        CommonManager.setRefreshingState(mRefreshLayout, true);
        startActivityLoading();

    }

    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getDataFromNet();
    }

    private void getDataFromNet()
    {
        getFilmBaseInfo(filmId + "");//获取电影基本信息
        getFilmHotList(filmId + "", 0, 10);//获取热点
        getFilmReviewList(filmId + "", 0, 3);//影评
    }


    @Override
    protected void setListener()
    {
        //顶部标题栏
        scrollView.setOnScrollChangedListener(new MyScrollView.OnScrollChangedListener()
        {
            @Override
            public void onScrollChanged(int scrollY)
            {
                LogUtils.i("123", "scrollY-" + scrollY);

                if (scrollY > distance) {
                    curAlpha=(float) ((scrollY - distance) / (distance * 1.0));
                    tvTitleFilmName.setAlpha(curAlpha);
                }

                else {
                    curAlpha=0;
                    tvTitleFilmName.setAlpha(0);
                }
            }
        });
        //影片基本信息
        imgFilmCover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                ShowMsg("播放中");

                Intent IT = new Intent();
                IT.setClass(PreFilmDetailActivity.this, CinemaActivityNew.class);
                IT.putExtra(PriceActivityNew.FILMID, filmId + "");
                IT.putExtra(PriceActivityNew.FILMIDNAME, filmName + "");
                IT.putExtra(PriceActivityNew.FLAG, false);
                startActivity(IT);

                ArrayList<String> trailers = (ArrayList<String>) v.getTag();
                if (trailers == null || trailers.size() == 0||trailers.get(0).equals("")) {
                    ShowMsg("无视频资源");
                    return;
                }

                Intent intent = new Intent(PreFilmDetailActivity.this, VideoPlayerActivity.class);
                intent.putExtra("videoUrl", trailers.get(0));
                intent.putExtra("filmName", filmName);
                startActivity(intent);
            }
        });

    }

    @Override
    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.btnBack:
                this.finish();
                break;
            case R.id.btnBuyTicket:

                break;
            case R.id.btnShareTicket:

                break;
            case R.id.btnLike:
                boolean isSelected = view.isSelected();
                if (isSelected) {
                    cancelAttentionFilm(filmId + "");
                }
                else {
                    attentionFilm(filmId + "");
                }

                break;
        }

    }

    private void getFilmBaseInfo(String filmId)
    {
        apiRequestManager.getFilmBaseInfo(filmId, new AsyncHttpHelper.ResultCallback<FilmBaseInfo>()
        {
            @Override
            public void onSuccess(FilmBaseInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingSuccess();
                tvTitleFilmName.setAlpha(curAlpha);
                filmBaseData = data.film;
                filmName = filmBaseData.name;
                tvFilmName.setText(filmName);
                tvTitleFilmName.setText(filmName);
                String country = filmBaseData.country;
                if (TextUtils.isEmpty(country)) {
                    tvFilmDate.setText(filmBaseData.releaseDate + "上映");
                }
                else {
                    tvFilmDate.setText(filmBaseData.country + "/" + filmBaseData.releaseDate + "上映");
                }

                tvFilmActor.setText("主演：" + filmBaseData.cast);
                tvFilmDirector.setText("导演：" + filmBaseData.director);
                tvFilmIntro.setText("  剧情简介\n  " + filmBaseData.summary);
                if (filmBaseData.trailers.size()==0||filmBaseData.trailers.get(0).equals("")){
                    imgFilmPlay.setVisibility(View.INVISIBLE);
                }else{
                    imgFilmPlay.setVisibility(View.VISIBLE);
                }
                imgFilmCover.setTag(filmBaseData.trailers);//设置视频播放地址
//                    LogUtils.i("123", "video1:" + filmBaseData.get("trailers"));
                ImageLoaderUtils.displayImage(filmBaseData.post, imgFilmCover, R.drawable.ic_default_loading_img);
//                    ImageLoader.getInstance().displayImage(filmBaseData.get("post"), imgFilmBanner);
                ImageLoaderUtils.setBlurImager(PreFilmDetailActivity.this, imgFilmBanner, filmBaseData.post);
                int followed = filmBaseData.followed;
                if (followed == 1) {
                    btnLike.setSelected(true);
                }
                else {
                    btnLike.setSelected(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingFailed();
            }
        });
    }
    private void getFilmReviewList(String filmId, int page, int limit)
    {
        apiRequestManager.getFilmReviewList(filmId, page, limit, new AsyncHttpHelper.ResultCallback<FilmReviewListInfo>()
        {
            @Override
            public void onSuccess(FilmReviewListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingSuccess();
                tvTitleFilmName.setAlpha(curAlpha);
                layFilmReview.removeAllViews();
                ArrayList<FilmReviewListInfo.CinecismModel> cinecismList = data.cinecisms.list;
                int positiveSum = data.cinecisms.positiveNum;
                int negtiveSum = data.cinecisms.negativeNum;

                tvFilmHighPraiseCount.setText(positiveSum + "");
                tvFilmLowPraiseCount.setText(negtiveSum + "");
                int allNum = positiveSum + negtiveSum;
                tvShowMoreFilmReview.setText("查看全部（" + allNum + "）");
                if (allNum == 0) {
                    layFilmReviewTitle.setVisibility(View.GONE);
                    tvShowMoreFilmReview.setVisibility(View.GONE);
                }
                else {
                    layFilmReviewTitle.setVisibility(View.VISIBLE);
                    tvShowMoreFilmReview.setVisibility(View.VISIBLE);
                }

                for (int i = 0; i < cinecismList.size(); i++) {
                    FilmReviewListInfo.CinecismModel cinecism = cinecismList.get(i);
                    FilmReviewListInfo.Writer writer = cinecism.writer;

                    LinearLayout linearLayout = (LinearLayout) mInflater.inflate(R.layout.list_item_single_film_review, null);
                    View imgFilmReviewPraise = linearLayout.findViewById(R.id.imgFilmReviewPraise);
                    TextView tvFilmReviewScore = (TextView) linearLayout.findViewById(R.id.tvFilmReviewScore);
                    TextView tvFilmReviewTitle = (TextView) linearLayout.findViewById(R.id.tvFilmReviewTitle);
                    TextView tvFilmReviewWriter = (TextView) linearLayout.findViewById(R.id.tvFilmReviewWriter);
                    TextView tvWriterHonor= (TextView) linearLayout.findViewById(R.id.tvWriterHonor);
                    TextView tvFilmReviewDate= (TextView) linearLayout.findViewById(R.id.tvFilmReviewDate);

                    if (cinecism.opinion == 1) {
                        imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_is_worth_yellow);
                    }
                    else {
                        imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_is_worth_yellow);
                    }

                    if (cinecism.srcScore.equals("0.0") || cinecism.srcScore.equals("0")) {
                        tvFilmReviewScore.setText("FROM " + cinecism.srcMedia);
                    }
                    else {
                        tvFilmReviewScore.setText("FROM " + cinecism.srcMedia + " " + cinecism.srcScore);
                    }
//                    CommonManager.setTime3(tvFilmReviewDate,cinecism.createTime);
                    int year=Integer.valueOf(cinecism.createTime.substring(0,4));
                    if (year<2016){
                        tvFilmReviewDate.setText(cinecism.createTime.substring(0,10));
                    }else{
                        tvFilmReviewDate.setText(cinecism.createTime.substring(5,10));
                    }
                    if (TextUtils.isEmpty(cinecism.summary)){
                        tvFilmReviewTitle.setText(cinecism.title);
                    }else {
                        tvFilmReviewTitle.setText(cinecism.summary);
                    }
                    tvFilmReviewWriter.setText(writer.name + " ");
//                    tvWriterHonor.setText(writer.title);

                    linearLayout.setTag(cinecism.cinecismID);
                    linearLayout.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            long id = (long) v.getTag();
                            Intent intent = new Intent(PreFilmDetailActivity.this, FilmReviewDetailActivity.class);
                            intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            PreFilmDetailActivity.this.startActivity(intent);
                        }
                    });
                    layFilmReview.addView(linearLayout);

                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingFailed();
            }
        });
    }
    private void getFilmHotList(String filmId, int page, int limit)
    {
        apiRequestManager.getSingleFilmHotList(filmId, page, limit, new AsyncHttpHelper.ResultCallback<FilmHotListInfo>()
        {
            @Override
            public void onSuccess(FilmHotListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingSuccess();
                tvTitleFilmName.setAlpha(curAlpha);
                layFilmHot.removeAllViews();
                ArrayList<FilmHotListInfo.NewsModel> newFilmHotDatas = data.news;
                for (int i = 0; i < newFilmHotDatas.size(); i++) {
                    FilmHotListInfo.NewsModel newsModel = newFilmHotDatas.get(i);
                    LinearLayout linearLayout = (LinearLayout) mInflater.inflate(R.layout.list_item_film_hot, null);
                    ImageView imgFilmHotCover = (ImageView) linearLayout.findViewById(R.id.imgFilmHotCover);
                    TextView tvFilmHotTitle = (TextView) linearLayout.findViewById(R.id.tvFilmHotTitle);
                    TextView tvFilmHotDate = (TextView) linearLayout.findViewById(R.id.tvFilmHotDate);

                    ImageLoaderUtils.displayImage(newsModel.logo, imgFilmHotCover);
                    tvFilmHotTitle.setText(newsModel.title);
                    CommonManager.setTime3(tvFilmHotDate, newsModel.publishTime);

                    linearLayout.setTag(newsModel);
                    linearLayout.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            FilmHotListInfo.NewsModel hotMap = (FilmHotListInfo.NewsModel) v.getTag();
                            String url = hotMap.url;
                            url = ConfigInfo.BASE_URL + url;
                            Intent intent = new Intent(PreFilmDetailActivity.this, HtmlActivity.class);
                            intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
                            intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 1);
                            intent.putExtra("imageUrl", hotMap.logo);
                            intent.putExtra("title", hotMap.title);
                            startActivity(intent);
                        }
                    });
                    layFilmHot.addView(linearLayout);
                }
                if (newFilmHotDatas.size() == 0) {
                    tvFilmHotTheme.setVisibility(View.GONE);
                }
                else {
                    tvFilmHotTheme.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingFailed();
            }
        });
    }

    public void attentionFilm(String filmId)
    {
        apiRequestManager.attentionFilm(filmId, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                btnLike.setSelected(true);
                ShowMsg("关注成功");
            }

            @Override
            public void onFailure(String errorCode, String message)
            {

            }
        });

    }

    public void cancelAttentionFilm(String filmId)
    {
        apiRequestManager.cancelAttentionFilm(filmId, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                btnLike.setSelected(false);
                ShowMsg("取消关注");
            }

            @Override
            public void onFailure(String errorCode, String message)
            {

            }
        });

    }


}
