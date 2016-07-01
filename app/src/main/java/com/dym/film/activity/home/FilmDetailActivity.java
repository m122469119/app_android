package com.dym.film.activity.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.price.CinemaActivityNew;
import com.dym.film.activity.price.PriceActivityNew;
import com.dym.film.activity.sharedticket.SharedTicketDetailActivity;
import com.dym.film.activity.sharedticket.TagSharedTicketActivity;
import com.dym.film.activity.sharedticket.TicketShareActivity;
import com.dym.film.adapter.FilmSharedTicketGridAdapter;
import com.dym.film.adapter.FilmVideoPostListAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.controllers.ShareTicketDialogViewController;
import com.dym.film.manager.ChartManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.model.FilmAllIndexListInfo;
import com.dym.film.model.FilmBaseInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.model.FilmReviewListInfo;
import com.dym.film.model.FilmVideoPostInfo;
import com.dym.film.model.SharedTicketListInfo;
import com.dym.film.ui.HorizontalListView;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.MyScrollView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmDetailActivity extends BaseActivity implements OnChartValueSelectedListener
{
    public final static String KEY_FILM_ID = "filmID";
    public final static String KEY_FILM_NAME = "name";

    private LineChart mLineChart;
    private TextView tvMoney;
    private TextView tvBoxOfficeDate;
    private LinearLayout layMoney;
    private ChartManager chartManager;
    private LinearLayout layDymIndex;
    private LinearLayout layBoxOffice;
    private LinearLayout layTicketIndex;
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
    private ImageView imgOpenDetail;
    private TextView tvDymIndex;
    private TextView tvBoxOffice;
    private TextView tvTicketIndex;
    private TextView tvFilmHighPraiseCount;
    private TextView tvFilmLowPraiseCount;
    private LinearLayout layFilmReview;
    private TextView tvShowMoreFilmReview;
    private TextView tvTicketLikeCount;
    private TextView tvTicketUnLikeCount;
    private GridView gridSharedTicket;
    private TextView tvShowMoreSharedTicket;

    private ArrayList<NetworkManager.SharedTicketRespModel> sharedTicketDatas;

    private FilmSharedTicketGridAdapter sharedTicketGridAdapter;
    private RelativeLayout layBarChart;
    private RelativeLayout layLineChart;
    private RelativeLayout layPieChart;
    private LinearLayout layFilmHot;
    /**
     * 晒票的选项view控制
     */
    private ShareTicketDialogViewController mSelectDialogController = null;
    private int distance;//标题滑动该距离，开始显示
    private SwipeRefreshLayout mRefreshLayout;
    private FilmBaseInfo.FilmModel filmBaseData;
    private LayoutInflater mInflater;
    private TextView tvFilmHotTheme;

    private long filmId = 12377;
    private String filmName = "";
    private ImageButton btnLike;
    private ArrayList<FilmAllIndexListInfo.ShardsModel> shards;
    private TextView tvDymIndexUnit;
    private String dymIndex = "0";
    private boolean indexShowFlag = false;
    private int criticNum = 0;
    private ArrayList<FilmAllIndexListInfo.CriticsModel> criticArray;
    private LinearLayout layFilmReviewTitle;
    private LinearLayout laySharedTicketTitle;
    private LinearLayout layBottomBar;
    private LinearLayout layIndexTab;
    private float curAlpha;
    private int gridHeight;
    private HorizontalListView listVideoPost;
    private TextView tvShowMoreFilmVideo;
    private TextView tvShowMoreFilmPost;
    private FilmVideoPostListAdapter filmVideoPostListAdapter;
    private ArrayList<FilmVideoPostInfo> videoPostDatas;
    private TextView tvVideoPostTitle;
    private LinearLayout layShowMoreVideoPost;
    private TextView btnBuyTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_detail;
    }

    @Override
    protected void initVariable()
    {
        Intent intent=getIntent();
        filmId = Long.parseLong(intent.getStringExtra(KEY_FILM_ID));
        filmName = intent.getStringExtra(KEY_FILM_NAME);
        distance = DimenUtils.dp2px(FilmDetailActivity.this, 100);
        mInflater = getLayoutInflater();
        chartManager = new ChartManager(this,filmId+"",filmName);
        sharedTicketDatas = new ArrayList<NetworkManager.SharedTicketRespModel>();//晒票数据
        sharedTicketGridAdapter = new FilmSharedTicketGridAdapter(this, sharedTicketDatas, R.layout.grid_item_film_shared_ticket);

        videoPostDatas = new ArrayList<FilmVideoPostInfo>();//视频和海报数据
        filmVideoPostListAdapter = new FilmVideoPostListAdapter(mContext, videoPostDatas, R.layout.list_item_film_video_post);
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
        imgOpenDetail = (ImageView) findViewById(R.id.imgOpenDetail);

        //底部button
        layBottomBar = (LinearLayout) findViewById(R.id.layBottomBar);
        btnBuyTicket = (TextView) findViewById(R.id.btnBuyTicket);
        //指数图标相关
        layIndexTab= (LinearLayout) findViewById(R.id.layIndexTab);
        layDymIndex = (LinearLayout) findViewById(R.id.layDymIndex);
        tvDymIndex = (TextView) findViewById(R.id.tvDymIndex);
        tvDymIndexUnit = (TextView) findViewById(R.id.tvDymIndexUnit);

        layBoxOffice = (LinearLayout) findViewById(R.id.layBoxOffice);
        tvBoxOffice = (TextView) findViewById(R.id.tvBoxOffice);
        mLineChart = (LineChart) findViewById(R.id.lineChart);
        layMoney = (LinearLayout) findViewById(R.id.layMoney);
        tvMoney = (TextView) findViewById(R.id.tvMoney);
        tvBoxOfficeDate = (TextView) findViewById(R.id.tvBoxOfficeDate);

        layTicketIndex = (LinearLayout) findViewById(R.id.layTicketIndex);
        tvTicketIndex = (TextView) findViewById(R.id.tvTicketIndex);

        layPieChart = (RelativeLayout) findViewById(R.id.layPieChart);
        layLineChart = (RelativeLayout) findViewById(R.id.layLineChart);
        layBarChart = (RelativeLayout) findViewById(R.id.layBarChart);
        //初始化一些图表view
        chartManager.initLineChart(mLineChart);
        chartManager.initPieChart(layPieChart);
        chartManager.initBarChart(layBarChart);

        //视频海报相关
        listVideoPost = (HorizontalListView) findViewById(R.id.listVideoPost);
        tvShowMoreFilmVideo = (TextView) findViewById(R.id.tvShowMoreFilmVideo);
        tvShowMoreFilmPost = (TextView) findViewById(R.id.tvShowMoreFilmPost);

        tvVideoPostTitle= (TextView) findViewById(R.id.tvVideoPostTitle);
        layShowMoreVideoPost = (LinearLayout) findViewById(R.id.layShowMoreVideoPost);
        //影评相关
        tvFilmHighPraiseCount = (TextView) findViewById(R.id.tvFilmHighPraiseCount);
        tvFilmLowPraiseCount = (TextView) findViewById(R.id.tvFilmLowPraiseCount);
        layFilmReview = (LinearLayout) findViewById(R.id.layFilmReview);
        tvShowMoreFilmReview = (TextView) findViewById(R.id.tvShowMoreFilmReview);

        layFilmReviewTitle = (LinearLayout) findViewById(R.id.layFilmReviewTitle);
        //晒票相关
        tvTicketLikeCount = (TextView) findViewById(R.id.tvTicketLikeCount);
        tvTicketUnLikeCount = (TextView) findViewById(R.id.tvTicketUnLikeCount);
        gridSharedTicket = (GridView) findViewById(R.id.gridSharedTicket);
        tvShowMoreSharedTicket = (TextView) findViewById(R.id.tvShowMoreSharedTicket);

        laySharedTicketTitle = (LinearLayout) findViewById(R.id.laySharedTicketTitle);
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
                indexShowFlag = false;
                getDataFromNet();
            }
        });
//
    }

    @Override
    protected void initData()
    {

        curAlpha=0;
        tvTitleFilmName.setAlpha(1);
        tvTitleFilmName.setText(filmName);

        listVideoPost.setAdapter(filmVideoPostListAdapter);
        gridSharedTicket.setAdapter(sharedTicketGridAdapter);
        gridHeight=gridSharedTicket.getLayoutParams().height;
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
        getSharedTicketList(filmId + "", 0, 6);//晒票
        getFilmIndexList(filmId + "");//指数
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
//        imgFilmCover.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
////                ShowMsg("播放中");
//                ArrayList<String> trailers= (ArrayList<String>) v.getTag();
//                if (trailers==null||trailers.size()==0||trailers.get(0).equals("")){
//                    ShowMsg("无视频资源");
//                    return;
//                }
//                Intent intent = new Intent(FilmDetailActivity.this, VideoPlayerActivity.class);
//                intent.putExtra("videoUrl", trailers.get(0));
//                intent.putExtra("filmName", filmName);
//                startActivity(intent);
//            }
//        });
        imgOpenDetail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (imgOpenDetail.isSelected()) {
                    imgOpenDetail.setSelected(false);
                    tvFilmIntro.setMaxLines(3);//收起
                }
                else {
                    imgOpenDetail.setSelected(true);
                    tvFilmIntro.setMaxLines(Integer.MAX_VALUE);//展开
                }
            }
        });

        //指数图标相关
        mLineChart.setOnChartValueSelectedListener(this);
        layDymIndex.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                layDymIndex.setSelected(true);
                layBoxOffice.setSelected(false);
                layTicketIndex.setSelected(false);

                layPieChart.setVisibility(View.VISIBLE);
                layLineChart.setVisibility(View.GONE);
                layBarChart.setVisibility(View.GONE);
            }
        });
        layBoxOffice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                layDymIndex.setSelected(false);
                layBoxOffice.setSelected(true);
                layTicketIndex.setSelected(false);

                layPieChart.setVisibility(View.GONE);
                layLineChart.setVisibility(View.VISIBLE);
                layBarChart.setVisibility(View.GONE);
            }
        });
        layTicketIndex.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                layDymIndex.setSelected(false);
                layBoxOffice.setSelected(false);
                layTicketIndex.setSelected(true);

                layPieChart.setVisibility(View.GONE);
                layLineChart.setVisibility(View.GONE);
                layBarChart.setVisibility(View.VISIBLE);
            }
        });

        //影评相关
        tvShowMoreFilmReview.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MatStatsUtil.eventClick(mContext,MatStatsUtil.MORE_REVIEW,null);
                Intent intent = new Intent(FilmDetailActivity.this, SingleFilmReviewActivity.class);
                intent.putExtra("filmId", filmId + "");
                intent.putExtra("filmName", filmName + "");
                FilmDetailActivity.this.startActivity(intent);
            }
        });
        //晒票相关
        gridSharedTicket.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                NetworkManager.SharedTicketRespModel ticket =sharedTicketDatas.get(position);
                CommonManager.putData(SharedTicketDetailActivity.KEY_INTENT, ticket);
                Intent intent = new Intent(mContext, SharedTicketDetailActivity.class);
                startActivity(intent);
            }
        });
        tvShowMoreSharedTicket.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MatStatsUtil.eventClick(mContext,MatStatsUtil.MORE_SHOW,null);
                Intent intent = new Intent(FilmDetailActivity.this, TagSharedTicketActivity.class);
                intent.putExtra(TagSharedTicketActivity.KEY_TAG, filmName);
                startActivity(intent);
            }
        });

        tvShowMoreFilmVideo.setOnClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            MatStatsUtil.eventClick(mContext,MatStatsUtil.MORE_VIDEO,null);
            Intent intent = new Intent(mContext, FilmVideoActivity.class);
            intent.putExtra("filmId", filmId + "");
            intent.putExtra("filmName", filmName+ "");
            startActivity(intent);

        }
    });
        tvShowMoreFilmPost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MatStatsUtil.eventClick(mContext,MatStatsUtil.MORE_PIC,null);
                Intent intent = new Intent(mContext, FilmPostActivity.class);
                intent.putExtra("filmId", filmId + "");
                intent.putExtra("filmName", filmName+ "");
                intent.putExtra("photoSum", filmBaseData.photoSum);
                startActivity(intent);

            }
        });

        listVideoPost.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                FilmVideoPostInfo videoPostInfo=videoPostDatas.get(position);
                if (videoPostInfo.type==0){
                    Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                    intent.putExtra("videoUrl", videoPostInfo.videoUrl);
                    intent.putExtra("filmName", filmName);
                    startActivity(intent);
                }else{
                    FilmVideoPostInfo firstVideoPostInfo=videoPostDatas.get(0);
                    Intent intent = new Intent();
                    intent.setClass(mContext, FilmBigPostActivity.class);
                    intent.putExtra("filmPostDatas", filmBaseData.photos);
                    if (firstVideoPostInfo.type==0){
                        intent.putExtra("curPosition", position-1);
                    }else{
                        intent.putExtra("curPosition", position);
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);

                }

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
                MatStatsUtil.eventClick(mContext,MatStatsUtil.BUT_TICKET2,null);
//                if (filmBaseData==null)
//                    return;
//                try {
//                    int status=Integer.parseInt(filmBaseData.get("status"));
//                    if(status==2){
//                         ShowMsg("电影已下映");
//                        return;
//                    }
//                }catch (Exception e){}

//                DaoSession session = DatabaseManager.getInstance().getDaoSession();
//                cinemaDao dao = session.getCinemaDao();
//                if (dao.getCinemaListCount(dao) > 0) {
//                    List<cinema> cinemaList = dao.getCinemaList(dao);
//                    if (cinemaList != null && cinemaList.size() > 0) {
//                        cinema c = cinemaList.get(0);
                        Intent intent = new Intent();
                        intent.setClass(this, CinemaActivityNew.class);
                        intent.putExtra(PriceActivityNew.FILMID, filmId + "");
                        intent.putExtra(PriceActivityNew.FILMIDNAME, filmName + "");
                        intent.putExtra(PriceActivityNew.FLAG, false);
                        startActivity(intent);
//                    }
//                    else {
//                        openActivity(CinemaActivity.class);
//                    }
//                }
//                else {
//                    openActivity(CinemaActivity.class);
//                }
//                EventBus.getDefault().post(filmId+"");
//                if(AppManager.getAppManager().IsAppActivity(FilmIndexActivity.class)){
//                    AppManager.getAppManager().finishActivity(FilmIndexActivity.class);
//                }
//                finish();
                break;
            case R.id.btnShareTicket:
                MatStatsUtil.eventClick(mContext,MatStatsUtil.TICKET_SHOW2,null);
                // 启动晒票
                if (mSelectDialogController == null) {
                    mSelectDialogController = new ShareTicketDialogViewController(this);
                }
                mSelectDialogController.show();
                break;
            case R.id.btnLike:
                boolean isSelected = view.isSelected();
                if (isSelected) {
                    MatStatsUtil.eventClick(mContext,MatStatsUtil.FAV_FILM,null);
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
                int videoCount=filmBaseData.trailers.size();
                if (videoCount==0||filmBaseData.trailers.get(0).equals("")){
                      imgFilmPlay.setVisibility(View.INVISIBLE);
                }else{
                    imgFilmPlay.setVisibility(View.INVISIBLE);
                    FilmVideoPostInfo videoInfo =new FilmVideoPostInfo();
                    videoInfo.type=0;
                    videoInfo.videoUrl=filmBaseData.trailers.get(0);
                    videoInfo.postUrl=filmBaseData.post;
                    videoPostDatas.add(videoInfo);
                }
                ImageLoaderUtils.displayImage(filmBaseData.post, imgFilmCover, R.drawable.ic_default_loading_img);
//                    ImageLoader.getInstance().displayImage(filmBaseData.get("post"), imgFilmBanner);
                ImageLoaderUtils.setBlurImager(FilmDetailActivity.this, imgFilmBanner, filmBaseData.post);
                //是否关注
                int followed = filmBaseData.followed;
                if (followed == 1) {
                    btnLike.setSelected(true);
                }
                else {
                    btnLike.setSelected(false);
                }
                //视频和海报
                tvShowMoreFilmVideo.setText("全部视频（"+filmBaseData.trailerSum+"）");
                tvShowMoreFilmPost.setText("全部海报（"+filmBaseData.photoSum+")");
                for (int i = 0; i <filmBaseData.photos.size() ; i++) {
                    FilmVideoPostInfo videoPostInfo=new FilmVideoPostInfo();
                    videoPostInfo.type=1;
                    videoPostInfo.postUrl=filmBaseData.photos.get(i);
                    videoPostDatas.add(videoPostInfo);
                }
                filmVideoPostListAdapter.notifyDataSetChanged();
                if (filmBaseData.trailerSum==0&&filmBaseData.photoSum==0){
                    listVideoPost.setVisibility(View.GONE);
                    tvVideoPostTitle.setVisibility(View.GONE);
                    layShowMoreVideoPost.setVisibility(View.GONE);
                }else{
                    listVideoPost.setVisibility(View.VISIBLE);
                    tvVideoPostTitle.setVisibility(View.VISIBLE);
                    layShowMoreVideoPost.setVisibility(View.VISIBLE);
                }

                //电影状态 ,是否可买票
                int sellingTicket = filmBaseData.sellingTicket;
                int status = filmBaseData.status;
                if (status == 1) {//即将上映
                    if (sellingTicket==0){
                        layBottomBar.setVisibility(View.GONE);
                    }else {
                        layBottomBar.setVisibility(View.VISIBLE);
                        btnBuyTicket.setText("预售比价");
                    }
                    layIndexTab.setVisibility(View.GONE);
                    layPieChart .setVisibility(View.GONE);
                    layLineChart.setVisibility(View.GONE);
                    layBarChart.setVisibility(View.GONE);
                    return;
                }else  if (status==2){//下架
                    layBottomBar.setVisibility(View.GONE);
                    layIndexTab .setVisibility(View.VISIBLE);
                }
                else {//热映
                    layBottomBar.setVisibility(View.VISIBLE);
                    layIndexTab .setVisibility(View.VISIBLE);
                }
                //银幕指数
                dymIndex = filmBaseData.dymIndex + "";
                LogUtils.i("123", "dymIndex" + dymIndex);
                if (indexShowFlag) {
                    if (criticNum<ConfigInfo.cinecismNum){
                        tvDymIndex.setText("暂无");
                        tvDymIndexUnit.setText("");
                        tvDymIndex.setTextSize(20);
                        layBoxOffice.performClick();
                    }else {
                        int index= (int) Float.parseFloat(dymIndex);
                        tvDymIndex.setText(index + "");
                        tvDymIndexUnit.setText("%");
                        tvDymIndex.setTextSize(30);
                        layDymIndex.performClick();
                    }
                }
                indexShowFlag = true;
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
                            Intent intent = new Intent(FilmDetailActivity.this, HtmlActivity.class);
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
                        imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_is_not_worth_green);
                    }

                    tvFilmReviewScore.setText("FROM " + cinecism.srcMedia);
//                    if (cinecism.srcScore.equals("0.0") || cinecism.srcScore.equals("0")) {
//                        tvFilmReviewScore.setText("FROM " + cinecism.srcMedia);
//                    }
//                    else {
//                        tvFilmReviewScore.setText("FROM " + cinecism.srcMedia + " " + cinecism.srcScore);
//                    }
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
                            Intent intent = new Intent(FilmDetailActivity.this, FilmReviewDetailActivity.class);
                            intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            FilmDetailActivity.this.startActivity(intent);
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

    private void getSharedTicketList(String filmId, int page, int limit)
    {
        apiRequestManager.getSharedTicketList(filmId, page, limit, new AsyncHttpHelper.ResultCallback<SharedTicketListInfo>()
        {
            @Override
            public void onSuccess(SharedTicketListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingSuccess();
                tvTitleFilmName.setAlpha(curAlpha);
                sharedTicketDatas.clear();
                int positiveSum = data.stubs.positiveNum;
                int negtiveSum = data.stubs.negativeNum;
                int allNum = positiveSum + negtiveSum;
                tvTicketLikeCount.setText(positiveSum + "");
                tvTicketUnLikeCount.setText(negtiveSum + "");
                tvShowMoreSharedTicket.setText("查看全部（" + allNum + "）");
                if (allNum == 0) {
                    gridSharedTicket.setVisibility(View.GONE);
                    laySharedTicketTitle.setVisibility(View.GONE);
                    tvShowMoreSharedTicket.setVisibility(View.GONE);
                }
                else {
                    gridSharedTicket.setVisibility(View.VISIBLE);
                    laySharedTicketTitle.setVisibility(View.VISIBLE);
                    tvShowMoreSharedTicket.setVisibility(View.VISIBLE);
                    if (allNum<4){
                        gridSharedTicket.getLayoutParams().height=gridHeight/2;
                    }else {
                        gridSharedTicket.getLayoutParams().height=gridHeight;
                    }
                }
                ArrayList<NetworkManager.SharedTicketRespModel> stubList = data.stubs.list;
                sharedTicketDatas.addAll(stubList);
                sharedTicketGridAdapter.notifyDataSetChanged();
                scrollView.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        scrollView.scrollTo(0, 0);
                    }
                }, 0);

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingFailed();
            }
        });
    }

    private void getFilmIndexList(String filmId)
    {
        apiRequestManager.getFilmAllIndexList(filmId, new AsyncHttpHelper.ResultCallback<FilmAllIndexListInfo>()
        {
            @Override
            public void onSuccess(FilmAllIndexListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);
                onActivityLoadingSuccess();
                tvTitleFilmName.setAlpha(curAlpha);
                //大荧幕指数
                FilmAllIndexListInfo.CinecismModel cinecism = data.assess.cinecism;
                criticNum = cinecism.criticNum;
                criticArray = cinecism.critics;
                chartManager.setPieChartData(criticArray, criticNum);
                if (indexShowFlag) {
                    if (criticNum<ConfigInfo.cinecismNum){
                        tvDymIndex.setText("暂无");
                        tvDymIndexUnit.setText("");
                        tvDymIndex.setTextSize(20);
                        layBoxOffice.performClick();
                    }else {
                        int index= (int) Float.parseFloat(dymIndex);
                        tvDymIndex.setText(index + "");
                        tvDymIndexUnit.setText("%");
                        tvDymIndex.setTextSize(30);
                        layDymIndex.performClick();
                    }

                }
                indexShowFlag = true;
                //票房信息
                FilmAllIndexListInfo.BoxModel box = data.assess.box;
                String sum = box.sum;
                shards = box.shards;
                chartManager.setLineChartData(shards);
                tvBoxOffice.setText(sum);
                //票更指数
                FilmAllIndexListInfo.StubModel stub = data.assess.stub;
                chartManager.setBarChartData(stub.positiveNum, stub.negativeNum);
                tvTicketIndex.setText(stub.stubIndex + "");
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

    @Override
    public void onValueSelected(Entry entry, int i, Highlight highlight)
    {

//      tvMoney.setText(CommonManager.formatMoney(entry.getVal()+"", 1));
        tvMoney.setText(entry.getVal() + "");
        tvBoxOfficeDate.setText(shards.get(entry.getXIndex()).date);
        layMoney.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected()
    {
        layMoney.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
