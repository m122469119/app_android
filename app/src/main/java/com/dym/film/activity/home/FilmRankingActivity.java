package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.model.FilmListInfo;
import com.dym.film.model.FilmRankingListInfo;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmRankingActivity extends BaseActivity
{
    private ArrayList<FilmListInfo.FilmModel> filmRankingDatas;
    private LoadMoreListView listFilmRanking;
    private CommonBaseAdapter<FilmListInfo.FilmModel> filmRankingAdapter;
    private ImageView imgFankingBanner;
    private int distance;//标题滑动该距离，开始显示
    private TextView tvTitle;

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
        return R.layout.activity_film_ranking;
    }

    @Override
    protected void initVariable()
    {
        filmRankingDatas = new ArrayList<FilmListInfo.FilmModel>();//观影指数
        distance = DimenUtils.dp2px(this, 130);
    }


    @Override
    protected void findViews()
    {
        listFilmRanking = (LoadMoreListView) findViewById(R.id.listFilmRanking);
        View view = getLayoutInflater().inflate(R.layout.layout_ranking_header_view, null);
        imgFankingBanner = (ImageView) view.findViewById(R.id.imgFankingBanner);
        listFilmRanking.addHeaderView(view);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

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
        FilmBboardListInfo.BillboardModel map = (FilmBboardListInfo.BillboardModel) getIntent().getSerializableExtra("bddata");
        bdid = map.bdID + "";
        logo = map.logo;
        title = map.title;

        tvTitle.setText(title);
        ImageLoaderUtils.displayImage(logo, imgFankingBanner, 500, 240);

        filmRankingAdapter = new CommonBaseAdapter<FilmListInfo.FilmModel>(this, filmRankingDatas, R.layout.list_item_film_ranking)
        {
            @Override
            public void convert(ViewHolder holder, FilmListInfo.FilmModel itemData, int position)
            {
                ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
                TextView tvFilmName = holder.getView(R.id.tvFilmName);
                TextView tvFilmDate = holder.getView(R.id.tvFilmDate);
                TextView tvFilmDirector = holder.getView(R.id.tvFilmDirector);
                TextView tvFilmActor = holder.getView(R.id.tvFilmActor);
                TextView tvFilmIndex = holder.getView(R.id.tvFilmIndex);
                TextView tvRankingIndex = holder.getView(R.id.tvRankingIndex);

                ImageLoaderUtils.displayImage(itemData.post, imgFilmCover, R.drawable.ic_default_loading_img);
                tvFilmName.setText(itemData.name);
                tvFilmDate.setText(itemData.country + "/" + itemData.releaseDate + "上映");
                tvFilmDirector.setText("导演：" + itemData.director);
                tvFilmActor.setText("主演：" + itemData.cast);
                tvFilmIndex.setText("专家好评" + itemData.dymIndex + "%" + " | 观众好评" + itemData.stubIndex + "%");

                tvRankingIndex.setText(++position + "");
                if (position < 4) {
                    tvRankingIndex.setTextColor(0xaab10b0b);
                }
                else {
                    tvRankingIndex.setTextColor(0xff464646);
                }


            }
        };

        CommonManager.setRefreshingState(mRefreshLayout, true);
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(filmRankingAdapter);
        animAdapter.setAbsListView(listFilmRanking);
        listFilmRanking.setAdapter(animAdapter);

        getFilmRankingListData(0);
        isLoadOrRefresh = true;
        curPage = 0;
    }

    @Override
    protected void setListener()
    {
        listFilmRanking.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (position==0){
                    return;
                }
                String filmId = filmRankingDatas.get(position-1).filmID + "";
                Intent intent = new Intent(FilmRankingActivity.this, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmId);
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmRankingDatas.get(position-1).name);
                startActivity(intent);
            }
        });

        listFilmRanking.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {

                getFilmRankingListData(++curPage);
                isLoadOrRefresh = false;
            }
        });
        listFilmRanking.setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                int scrollY = getScrollY();
                LogUtils.i("123", scrollY+"");
                LogUtils.i("123", distance+"");
                LogUtils.i("123", (float) (scrollY / (distance * 1.0))+"");
                tvTitle.setAlpha((float) (scrollY / (distance * 1.0)));

            }

            public int getScrollY()
            {
                View c = listFilmRanking.getChildAt(0);
                if (c == null) {
                    return 0;
                }
                int firstVisiblePosition = listFilmRanking.getFirstVisiblePosition();
                int top = c.getTop();
                return -top + firstVisiblePosition * c.getHeight();
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
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                if (!isLoadOrRefresh) {
                    listFilmRanking.onLoadFailed();
                }
                else {
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
