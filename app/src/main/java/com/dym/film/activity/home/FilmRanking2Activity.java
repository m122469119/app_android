package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.model.FilmListInfo;
import com.dym.film.model.FilmRankingListInfo;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmRanking2Activity extends BaseActivity
{
    private ArrayList<FilmListInfo.FilmModel> filmRankingDatas;
    private LoadMoreListView listFilmRanking;
    private CommonBaseAdapter<FilmListInfo.FilmModel> filmRankingAdapter;
    private int distance;//标题滑动该距离，开始显示
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isLoadOrRefresh = true;//刷新
    private String bdid = "";
    private String logo = "";
    private String title = "";

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_ranking2;
    }

    @Override
    protected void initVariable()
    {
        FilmBboardListInfo.BillboardModel map = (FilmBboardListInfo.BillboardModel) getIntent().getSerializableExtra("bddata");
        bdid = map.bdID + "";
        logo = map.logo;
        title = map.title;
        filmRankingDatas = new ArrayList<FilmListInfo.FilmModel>();//观影指数
        distance = DimenUtils.dp2px(this, 130);
    }


    @Override
    protected void findViews()
    {
        listFilmRanking = (LoadMoreListView) findViewById(R.id.listFilmRanking);
        showTopBar();
        setTitle(title);

        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getFilmRankingListData(0);
                curPage = 0;
                isLoadOrRefresh = true;
            }
        });
    }

    @Override
    protected void initData()
    {
        filmRankingAdapter = new CommonBaseAdapter<FilmListInfo.FilmModel>(this, filmRankingDatas, R.layout.list_item_film_show)
        {
            @Override
            public void convert(ViewHolder holder, FilmListInfo.FilmModel itemData, int position)
            {
                ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
                TextView tvFilmName = holder.getView(R.id.tvFilmName);
                TextView tvFilmIntro = holder.getView(R.id.tvFilmIntro);
                TextView tvFilmDate = holder.getView(R.id.tvFilmDate);
                LinearLayout layExpertScore = holder.getView(R.id.layExpertScore);//专家平分
                LinearLayout layUserScore = holder.getView(R.id.layUserScore);//网友平分
                TextView tvExpertScore = holder.getView(R.id.tvExpertScore);
                TextView tvUserScore = holder.getView(R.id.tvUserScore);
                Button btnBuyTicket = holder.getView(R.id.btnBuyTicket);

                //填充数据
                String url = QCloudManager.urlImage1(itemData.post, DimenUtils.dp2px(mContext,65), DimenUtils.dp2px(mContext,90));
                ImageLoader.getInstance().displayImage(url, imgFilmCover);

                tvFilmName.setText(itemData.name);
                tvFilmIntro.setText(itemData.digest);

                tvFilmDate.setText(itemData.cinecismNum + "专家影评 | " + itemData.stubNum + "人晒票");
                layExpertScore.setVisibility(View.VISIBLE);
                layUserScore.setVisibility(View.VISIBLE);
                int dymIndex = (int) Float.parseFloat(itemData.dymIndex);
                int stubIndex = (int) Float.parseFloat(itemData.stubIndex);
                tvExpertScore.setText(dymIndex + "");
                tvUserScore.setText(stubIndex + "");
                btnBuyTicket.setVisibility(View.INVISIBLE);

                if (itemData.cinecismNum!=0&&itemData.stubNum!=0){
                    layUserScore.setVisibility(View.VISIBLE);
                    tvFilmDate.setText(itemData.cinecismNum+"专家影评 | "+itemData.stubNum+"人晒票");
                }else if (itemData.cinecismNum!=0&&itemData.stubNum==0){
                    layUserScore.setVisibility(View.GONE);
                    tvFilmDate.setText(itemData.cinecismNum+"专家影评");
                }else if(itemData.cinecismNum==0&&itemData.stubNum!=0){
                    layUserScore.setVisibility(View.VISIBLE);
                    tvFilmDate.setText(itemData.stubNum+"人晒票");
                }else{
                    layUserScore.setVisibility(View.GONE);
                    tvFilmDate.setText("");
                }
                if (itemData.cinecismNum>= ConfigInfo.cinecismNum){
                    layExpertScore.setVisibility(View.VISIBLE);
                }else {
                    layExpertScore.setVisibility(View.GONE);
                }

            }
        };

        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(filmRankingAdapter);
        animAdapter.setAbsListView(listFilmRanking);
        listFilmRanking.setAdapter(animAdapter);

        isLoadOrRefresh = true;
        curPage = 0;
        startActivityLoading();
    }

    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getFilmRankingListData(0);
    }

    @Override
    protected void setListener()
    {
        listFilmRanking.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String filmId = filmRankingDatas.get(position).filmID + "";
                Intent intent = new Intent(FilmRanking2Activity.this, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmId);
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmRankingDatas.get(position).name);
                startActivity(intent);
            }
        });

        listFilmRanking.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {
                isLoadOrRefresh = false;
                getFilmRankingListData(++curPage);

            }
        });

    }

    /*获取推荐电影列表*/
    public void getFilmRankingListData(int page)
    {
        apiRequestManager.getRankingFilmListData(bdid, page, limit, new AsyncHttpHelper.ResultCallback<FilmRankingListInfo>()
        {
            @Override
            public void onSuccess(FilmRankingListInfo data)
            {
                onActivityLoadingSuccess();//显示内容区
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                if (isLoadOrRefresh) {
                    filmRankingDatas.clear();
                }
                ArrayList<FilmListInfo.FilmModel> films = data.films;
                filmRankingDatas.addAll(films);
                filmRankingAdapter.notifyDataSetChanged();
                if (films.size() == limit) {
                    listFilmRanking.onLoadSucess(true);
                }
                else {
                    listFilmRanking.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {

                if (isLoadOrRefresh) {
                    onActivityLoadingFailed();
                    CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                }
                else {
                    listFilmRanking.onLoadFailed();
                    ShowMsg("刷新失败");
                }
            }
        });

    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }
}
