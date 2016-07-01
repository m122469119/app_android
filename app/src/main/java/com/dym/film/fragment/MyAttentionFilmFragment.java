package com.dym.film.fragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.adapter.base.CommonRecyclerAdapter;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.MyAttentionFilmListInfo;
import com.dym.film.ui.LoadMoreRecyclerView;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class MyAttentionFilmFragment extends BaseFragment
{
    private LoadMoreRecyclerView recyclerView;
    private ArrayList<MyAttentionFilmListInfo.FilmModel> attentionFilmDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isLoadOrRefresh = true;//刷新
    private CommonRecyclerAdapter<MyAttentionFilmListInfo.FilmModel> adapter;
    private LinearLayout layNoAttention;

    @Override
    protected void initVariable()
    {
        attentionFilmDatas = new ArrayList<MyAttentionFilmListInfo.FilmModel>();
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_my_attention_film;
    }

    @Override
    protected void findViews(View view)
    {
        recyclerView = (LoadMoreRecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        layNoAttention = (LinearLayout) view.findViewById(R.id.layNoAttention);

        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(mContext, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getMyAttentionFilmList(0, limit);
                curPage = 0;
                isLoadOrRefresh = true;
            }
        });
    }

    protected void initData()
    {
        curPage = 0;
        isLoadOrRefresh = true;
        fillAdapter();
        startFragmentLoading();
    }

    @Override
    protected void onFragmentLoading()
    {
        super.onFragmentLoading();
        getMyAttentionFilmList(0, limit);
    }

    private void fillAdapter()
    {

        adapter = new CommonRecyclerAdapter<MyAttentionFilmListInfo.FilmModel>(mContext, attentionFilmDatas, R.layout.grid_item_my_attention_film)
        {


            @Override
            public void convert(BaseViewHolder holder, MyAttentionFilmListInfo.FilmModel itemData, int position)
            {
                ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
                TextView tvFilmName = holder.getView(R.id.tvFilmName);
                TextView tvFilmIndex = holder.getView(R.id.tvFilmIndex);
                TextView tvTicketIndex = holder.getView(R.id.tvTicketIndex);

                LogUtils.i("123", "width-" + imgFilmCover.getLayoutParams().width);
                int width = (DimenUtils.getScreenWidth(mContext) - 40) / 3;
                int height = width * 4 / 3;
                imgFilmCover.getLayoutParams().width = width;
                imgFilmCover.getLayoutParams().height = height;
                //填充数据
                String url = QCloudManager.urlImage1(itemData.post, 200,400);
                ImageLoader.getInstance().displayImage(url, imgFilmCover);
//                ImageLoaderUtils.displayImage((String) itemData.get("post"),imgFilmCover,200,288);
                tvFilmName.setText(itemData.name);
                tvFilmIndex.setText("好评率" + (int) Float.parseFloat(itemData.dymIndex) + "%");
                tvTicketIndex.setText("票根" + itemData.stubIndex + "%");
            }
        };
        View footerView = mInflater.inflate(R.layout.layout_load_more_footer, null);
        adapter.addFooterView(footerView);
        recyclerView.setAdapter(adapter);
    }

    public void setListener()
    {
        adapter.setOnItemClickLitener(new CommonRecyclerAdapter.OnItemClickLitener()
        {
            @Override
            public void onItemClick(View itemView, int position)
            {
                String filmId = attentionFilmDatas.get(position).filmID + "";
                String filmName = attentionFilmDatas.get(position).name;
                int status = attentionFilmDatas.get(position).status;
                Intent intent = new Intent();
                intent.setClass(mContext, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmId);
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmName);
                mContext.startActivity(intent);
            }

            @Override
            public void onItemLongClick(View itemView, int position)
            {

            }
        });

        recyclerView.setOnLoadListener(new LoadMoreRecyclerView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreRecyclerView recyclerView)
            {
                getMyAttentionFilmList(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });

    }


    public void getMyAttentionFilmList(int page, final int limit)
    {
        apiRequestManager.getMyAttentionFilmList(page, limit, new AsyncHttpHelper.ResultCallback<MyAttentionFilmListInfo>()
        {
            @Override
            public void onSuccess(MyAttentionFilmListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onFragmentLoadingSuccess();
                if (isLoadOrRefresh) {
                    attentionFilmDatas.clear();
                }

                ArrayList<MyAttentionFilmListInfo.FilmModel> films = data.films;
                attentionFilmDatas.addAll(films);
                adapter.notifyDataSetChanged();

                layNoAttention.setVisibility(View.INVISIBLE);
                if (films.size() == 0) {
                    layNoAttention.setVisibility(View.VISIBLE);
                    recyclerView.onLoadUnavailable();
                }
                else if (films.size() == limit) {
                    recyclerView.onLoadSucess(true);
                }
                else {
                    recyclerView.onLoadSucess(false);

                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onFragmentLoadingFailed();
                if (!isLoadOrRefresh) {
                    recyclerView.onLoadFailed();
                }
            }
        });

    }


}