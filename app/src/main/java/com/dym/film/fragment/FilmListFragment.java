package com.dym.film.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.holder.Holder;
import com.dym.film.R;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.home.FilmHotListActivity;
import com.dym.film.activity.home.FilmRankingActivity;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.home.PreFilmActivity;
import com.dym.film.activity.home.PreFilmDetailActivity;
import com.dym.film.adapter.FilmRankingHorizontalListAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.BannerListInfo;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.model.FilmListInfo;
import com.dym.film.model.MyBaseInfo;
import com.dym.film.receiver.JPushMessageReceiver;
import com.dym.film.ui.HorizontalListView;
import com.dym.film.ui.loopbanner.LoopBanner;
import com.dym.film.ui.loopbanner.LoopPageAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.MyScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
public class FilmListFragment extends BaseFragment
{

    protected MainActivity mActivity = null;
    private LinearLayout layPreFilm;
    private HorizontalListView listNewFilmIndex;
    private LoopBanner loopBanner;
    private ArrayList<BannerListInfo.BannerModel> bannerImagesData;
    private TextView tvShowMoreFilmIndex;
    private TextView tvShowMorePreFilm;
    private TextView tvShowMoreFilmHot;

    //刷新的Layout
    private MyScrollView scrollView;
    private SwipeRefreshLayout mRefreshLayout = null;
    private boolean mIsRefreshingOrLoadMore = false;

    private LinearLayout layFilmHot;
    private int sumFilmIndex = 0;
    private LinearLayout layFilmBboard;
    private TextView tvFilmBboardTitle;
    private int firstLoadCount = 6;//初始化首次加载的次数

    private HorizontalListView listMonthFilmIndex;

    private ArrayList<FilmListInfo.FilmModel> preFilmDatas;
    private FilmRankingHorizontalListAdapter newFilmIndexListAdapter;
    private ArrayList<FilmListInfo.FilmModel> newFilmIndexDatas;

    private FilmRankingHorizontalListAdapter monthFilmIndexListAdapter;
    private ArrayList<FilmListInfo.FilmModel> monthFilmIndexDatas;

    private ImageView unReadImage = null;

    @Override
    protected void initVariable()
    {
        mActivity = (MainActivity) getActivity();
        bannerImagesData = new ArrayList<BannerListInfo.BannerModel>();

        preFilmDatas = new ArrayList<FilmListInfo.FilmModel>();

        newFilmIndexDatas = new ArrayList<FilmListInfo.FilmModel>();//观影指数
        newFilmIndexListAdapter = new FilmRankingHorizontalListAdapter(mContext, newFilmIndexDatas, R.layout.list_item_film_ranking_horizontal);
        newFilmIndexListAdapter.setType(0);

        monthFilmIndexDatas = new ArrayList<FilmListInfo.FilmModel>();//观影指数
        monthFilmIndexListAdapter = new FilmRankingHorizontalListAdapter(mContext, monthFilmIndexDatas, R.layout.list_item_film_ranking_horizontal);
        monthFilmIndexListAdapter.setType(1);
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_film_list;
    }

    @Override
    protected void findViews(View view)
    {
        //广告条
        scrollView = $(R.id.scrollView);
        loopBanner = (LoopBanner) view.findViewById(R.id.loopBanner);
        int width = DimenUtils.getScreenWidth(mContext);
        loopBanner.getLayoutParams().height = (int) (width / 3);
        //观影指数
        listNewFilmIndex = (HorizontalListView) view.findViewById(R.id.listNewFilmIndex);
        listMonthFilmIndex = (HorizontalListView) view.findViewById(R.id.listMonthFilmIndex);
        tvShowMoreFilmIndex = (TextView) view.findViewById(R.id.tvShowMoreFilmIndex);
        listNewFilmIndex.setViewPager(mActivity.viewPager);
        listMonthFilmIndex.setViewPager(mActivity.viewPager);
        //即将上映
        layPreFilm = (LinearLayout) view.findViewById(R.id.layPreFilm);
        tvShowMorePreFilm = (TextView) view.findViewById(R.id.tvShowMorePreFilm);
        //热点
        layFilmHot = (LinearLayout) view.findViewById(R.id.layFilmHot);
        tvShowMoreFilmHot = (TextView) view.findViewById(R.id.tvShowMoreFilmHot);
        //推荐榜单
        layFilmBboard = (LinearLayout) view.findViewById(R.id.layFilmBboard);
        tvFilmBboardTitle = (TextView) view.findViewById(R.id.tvFilmBboardTitle);

        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(mContext, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                firstLoadCount = 6;
                getDataFromNet();
            }
        });

        unReadImage = (ImageView) view.findViewById(R.id.unreadImage);
    }


    protected void initData()
    {
        fillAdapter();
//        CommonManager.setRefreshingState(mRefreshLayout, true);
//        getDataFromNet();
        startFragmentLoading();
    }

    private void fillAdapter()
    {
        listNewFilmIndex.setAdapter(newFilmIndexListAdapter);
        listMonthFilmIndex.setAdapter(monthFilmIndexListAdapter);
        loopBanner.setPageAdapter(new LoopPageAdapter<BannerListInfo.BannerModel>(mContext, bannerImagesData, R.layout.layout_main_banner_item)
        {

            @Override
            public void convert(ViewHolder holder, final BannerListInfo.BannerModel itemData, final int position)
            {
                // TODO Auto-generated method stub
                ImageView imageView = (ImageView) holder.getConvertView();
                String img = itemData.img;
//                ImageLoaderUtils.displayImage(img, imageView);
                ImageLoader.getInstance().displayImage(img, imageView);
//                ImageLoaderUtils.displayImage(img, imageView, DimenUtils.getWidthInPx(mContext), DimenUtils.dip2px(mContext, 120));
                imageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //点击事件
//                    Toast.makeText(view.getContext(), "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
                        String url = itemData.url;
//                    Intent intent = new Intent(mContext, HtmlActivity.class);
//                    intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
//                    startActivity(intent);
                        CommonManager.processBannerClick(mContext, url);
                    }
                });
            }
        });
    }

    @Override
    protected void onFragmentLoading()
    {
        super.onFragmentLoading();
        getDataFromNet();
        firstLoadCount = 6;
    }

    private void getDataFromNet()
    {
        getFilmBannerData();//获取广告条信息
        getNewFilmIndexListData(0, 10);
        getMonthFilmIndexListData(0, 10);
        getPreFilmListData(0, 3);
        getFilmHotListData(0, 3);
        getFilmBillboardListData(0, 4);
        startCheckUserMessage();
    }

    private void loadFailed()
    {
        firstLoadCount--;
        if (firstLoadCount == 0) {
            onFragmentLoadingFailed();
            Toast.makeText(mContext, "刷新失败", Toast.LENGTH_SHORT).show();
            CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
        }

    }

    private void loadSuccess()
    {
        firstLoadCount = 0;
        onFragmentLoadingSuccess();
        CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
    }

    @Override
    protected void setListener()
    {

        tvShowMoreFilmIndex.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                MatStatsUtil.eventClick(mContext, "more_leaderboard", "more_leaderboard");
                Intent intent = new Intent(mContext, FilmRankingActivity.class);
                intent.putExtra("sum", sumFilmIndex);
                startActivity(intent);

            }
        });

        tvShowMorePreFilm.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MatStatsUtil.eventClick(mContext, "more_comingsoon", "more_comingsoon");
                Intent intent = new Intent(mContext, PreFilmActivity.class);
                startActivity(intent);
            }
        });
        tvShowMoreFilmHot.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MatStatsUtil.eventClick(mContext, "more_news", "more_news");
                Intent intent = new Intent(mContext, FilmHotListActivity.class);
                startActivity(intent);
            }
        });

        listNewFilmIndex.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                FilmListInfo.FilmModel filmModel = newFilmIndexDatas.get(i);
                Intent intent = new Intent(mContext, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmModel.filmID + "");
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmModel.name);
                startActivity(intent);
            }
        });
        listMonthFilmIndex.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                FilmListInfo.FilmModel filmModel = monthFilmIndexDatas.get(i);
                Intent intent = new Intent(mContext, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmModel.filmID + "");
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmModel.name);
                startActivity(intent);
            }
        });

    }

    private void getFilmBillboardListData(int page, int limit)
    {
        apiRequestManager.getFilmBillboardListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmBboardListInfo>()
        {
            @Override
            public void onSuccess(FilmBboardListInfo data)
            {
                loadSuccess();
                layFilmBboard.removeAllViews();
                ArrayList<FilmBboardListInfo.BillboardModel> newFilmBboardDatas = data.billboards;
                int size = newFilmBboardDatas.size() / 2;
                for (int i = 0; i < size; i++) {
                    FilmBboardListInfo.BillboardModel bboardModel1 = newFilmBboardDatas.get(2 * i);
                    FilmBboardListInfo.BillboardModel bboardModel2 = newFilmBboardDatas.get(2 * i + 1);
                    LinearLayout linearLayout = (LinearLayout) mInflater.inflate(R.layout.layout_film_bboard, null);
                    //第一个模块
                    RelativeLayout laySuggestFilmOne = (RelativeLayout) linearLayout.findViewById(R.id.laySuggestFilmOne);
                    ImageView imgSuggestFilmCover1 = (ImageView) linearLayout.findViewById(R.id.imgSuggestFilmCover1);
                    TextView tvSuggestFilmTitle1 = (TextView) linearLayout.findViewById(R.id.tvSuggestFilmTitle1);

                    tvSuggestFilmTitle1.setText(bboardModel1.title);
                    ImageLoaderUtils.displayImage(bboardModel1.logo, imgSuggestFilmCover1);
                    //第二个模块
                    RelativeLayout laySuggestFilmTwo = (RelativeLayout) linearLayout.findViewById(R.id.laySuggestFilmTwo);
                    ImageView imgSuggestFilmCover2 = (ImageView) linearLayout.findViewById(R.id.imgSuggestFilmCover2);
                    TextView tvSuggestFilmTitle2 = (TextView) linearLayout.findViewById(R.id.tvSuggestFilmTitle2);

                    tvSuggestFilmTitle2.setText(bboardModel2.title);
                    ImageLoaderUtils.displayImage(bboardModel2.logo, imgSuggestFilmCover2);

                    laySuggestFilmOne.setTag(bboardModel1);
                    laySuggestFilmOne.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            FilmBboardListInfo.BillboardModel bboardModel = (FilmBboardListInfo.BillboardModel) v.getTag();
                            Intent intent = new Intent(mContext, FilmRankingActivity.class);
                            intent.putExtra("bddata", bboardModel);
                            startActivity(intent);
                        }
                    });
                    laySuggestFilmTwo.setTag(bboardModel2);
                    laySuggestFilmTwo.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            HashMap<String, String> bboardModel = (HashMap<String, String>) v.getTag();
                            Intent intent = new Intent(mContext, FilmRankingActivity.class);
                            intent.putExtra("bddata", bboardModel);
                            startActivity(intent);
                        }
                    });
                    layFilmBboard.addView(linearLayout);
                }

                if (newFilmBboardDatas.size() > 0) {
                    tvFilmBboardTitle.setVisibility(View.VISIBLE);
                }
                else {
                    tvFilmBboardTitle.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });

    }

    private void getFilmHotListData(int page, int limit)
    {
        apiRequestManager.getFilmHotListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmHotListInfo>()
        {
            @Override
            public void onSuccess(FilmHotListInfo data)
            {
                loadSuccess();
                layFilmHot.removeAllViews();
                ArrayList<FilmHotListInfo.NewsModel> newFilmHotDatas = data.news;
                for (int i = 0; i < newFilmHotDatas.size(); i++) {
                    final FilmHotListInfo.NewsModel newsModel = newFilmHotDatas.get(i);
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
                            Intent intent = new Intent(mContext, HtmlActivity.class);
                            intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
                            intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 1);
                            intent.putExtra("imageUrl", hotMap.logo);
                            intent.putExtra("title", hotMap.title);
                            startActivity(intent);
//                            mActivity.overridePendingTransition(R.anim.activity_open_in_anim, android.R.anim.fade_out);

                        }
                    });
                    layFilmHot.addView(linearLayout);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });
    }

    private void getPreFilmListData(int page, int limit)
    {
        apiRequestManager.getPreFilmListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmListInfo>()
        {
            @Override
            public void onSuccess(FilmListInfo data)
            {
                loadSuccess();
                layPreFilm.removeAllViews();
                String sumPreFilm = data.films.sum + "";
                tvShowMorePreFilm.setText("查看全部（" + sumPreFilm + "）");
                ArrayList<FilmListInfo.FilmModel> listData = data.films.list;
                for (int i = 0; i < listData.size(); i++) {
                    final FilmListInfo.FilmModel filmModel = listData.get(i);
                    LinearLayout linearLayout = (LinearLayout) mInflater.inflate(R.layout.list_item_film_pre, null);
                    ImageView imgFilmCover = (ImageView) linearLayout.findViewById(R.id.imgFilmCover);
                    TextView tvFilmName = (TextView) linearLayout.findViewById(R.id.tvFilmName);
                    TextView tvFilmIntro = (TextView) linearLayout.findViewById(R.id.tvFilmIntro);
                    TextView tvFilmDate = (TextView) linearLayout.findViewById(R.id.tvFilmDate);

                    ImageLoaderUtils.displayImage(filmModel.post, imgFilmCover, R.drawable.ic_default_loading_img);
                    tvFilmName.setText(filmModel.name);
                    tvFilmDate.setText(filmModel.releaseDate.substring(5));
                    addImageSpan(tvFilmIntro,filmModel.digest);

                    linearLayout.setTag(filmModel);
                    linearLayout.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            final FilmListInfo.FilmModel filmModel= (FilmListInfo.FilmModel) v.getTag();
                            String filmId = filmModel.filmID + "";
                            Intent intent = new Intent(mContext, PreFilmDetailActivity.class);
                            intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, filmId);
                            intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmModel.name);
                            startActivity(intent);
                        }
                    });
                    layPreFilm.addView(linearLayout);
                }


            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });
    }

    private void getNewFilmIndexListData(int page, int limit)
    {
        apiRequestManager.getFilmListData(page, limit, "date", new AsyncHttpHelper.ResultCallback<FilmListInfo>()
        {
            @Override
            public void onSuccess(FilmListInfo data)
            {
                loadSuccess();
                listNewFilmIndex.setVisibility(View.VISIBLE);
                newFilmIndexDatas.clear();
                newFilmIndexDatas.addAll(data.films.list);
                newFilmIndexListAdapter.notifyDataSetChanged();
                sumFilmIndex = data.films.sum;
                tvShowMoreFilmIndex.setText("查看全部（" + sumFilmIndex + "）");

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });
    }

    private void getMonthFilmIndexListData(int page, int limit)
    {
        apiRequestManager.getFilmListData(page, limit, "", new AsyncHttpHelper.ResultCallback<FilmListInfo>()
        {
            @Override
            public void onSuccess(FilmListInfo data)
            {
                loadSuccess();
                listMonthFilmIndex.setVisibility(View.VISIBLE);
                monthFilmIndexDatas.clear();
                monthFilmIndexDatas.addAll(data.films.list);
                monthFilmIndexListAdapter.notifyDataSetChanged();
                sumFilmIndex = data.films.sum;
                tvShowMoreFilmIndex.setText("查看全部（" + sumFilmIndex + "）");

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });
    }

    private void getFilmBannerData()
    {
        apiRequestManager.getFilmBannerData(1,new AsyncHttpHelper.ResultCallback<BannerListInfo>()
        {
            @Override
            public void onSuccess(BannerListInfo data)
            {
                loadSuccess();
                ArrayList<BannerListInfo.BannerModel> banners = data.banners;
                bannerImagesData.clear();
                bannerImagesData.addAll(banners);
                loopBanner.setVisibility(View.VISIBLE);
                if (bannerImagesData.size() == 0) {
                    loopBanner.setVisibility(View.GONE);
                }
                loopBanner.notifyDataSetChanged();

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                loadFailed();
            }
        });
    }

    public void onResume()
    {
        super.onResume();
        loopBanner.startTurning(5000);

        startCheckUserMessage();
    }

    @Override
    public void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        loopBanner.stopTurning();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mActivity.unregisterReceiver(mBroadcastReceiver);
    }

    private void startCheckUserMessage()
    {
        if (UserInfo.isLogin) {
            checkNewMessage();
        }
        else if (unReadImage != null) {
            unReadImage.setVisibility(View.INVISIBLE);
        }

        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new SimpleBroadcastReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(JPushMessageReceiver.ACTION_MESSAGE_RECEIVED);
        mActivity.registerReceiver(mBroadcastReceiver, filter);
    }

    private SimpleBroadcastReceiver mBroadcastReceiver = null;
    private void checkNewMessage()
    {
        apiRequestManager.getMyBaseInfo(new AsyncHttpHelper.ResultCallback<MyBaseInfo>()
        {
            @Override
            public void onSuccess(MyBaseInfo data)
            {
                if (unReadImage != null) {
//                    LogUtils.e("Kejin", "hasNew: " + data.info.message.hasNew);
                    unReadImage.setVisibility((data == null || data.info == null ||
                            data.info.message == null || data.info.message.hasNew == 0) ? View.INVISIBLE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                //
            }
        });
    }

    /**
     * 监听消息
     */
    private class SimpleBroadcastReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            LogUtils.e("Kejin", "Action: " + action);
            if (JPushMessageReceiver.ACTION_MESSAGE_RECEIVED.equals(action)) {
                /**
                 * 从服务器请求一次
                 */
                LogUtils.e("Kejin", "check message");
                checkNewMessage();
            }
        }
    }

    public class ImageHolderView implements Holder<BannerListInfo.BannerModel>
    {
        private ImageView imageView;

        @Override
        public View createView(Context context)
        {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LogUtils.i("123", "createView");
            return imageView;
        }

        @Override
        public void UpdateUI(final Context context, final int position, final BannerListInfo.BannerModel bannerModel)
        {
            LogUtils.i("123", bannerModel.toString());
            String img = bannerModel.img;
            ImageLoaderUtils.displayImage(img, imageView);
//            ImageLoaderUtils.displayImage(img, imageView, DimenUtils.getWidthInPx(mContext), DimenUtils.dip2px(mContext, 120));
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //点击事件
//                    Toast.makeText(view.getContext(), "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
                    String url = bannerModel.url;
//                    Intent intent = new Intent(mContext, HtmlActivity.class);
//                    intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
//                    startActivity(intent);
                    CommonManager.processBannerClick(context, url);
                }
            });
        }
    }
    /**
     * 图片
     */
    private void addImageSpan(TextView tv,String content) {
        SpannableString spanString1 = new SpannableString("   ");
        SpannableString spanString2 = new SpannableString("   ");
        Drawable leftIcon = mContext.getResources().getDrawable(R.drawable.ic_text_left_mark);
        Drawable rightIcon = mContext.getResources().getDrawable(R.drawable.ic_text_right_mark);
        leftIcon.setBounds(0, 0, leftIcon.getIntrinsicWidth(), leftIcon.getIntrinsicHeight());//设置显示的大小
        rightIcon.setBounds(0, 0, rightIcon.getIntrinsicWidth(), rightIcon.getIntrinsicHeight());
        ImageSpan span1 = new ImageSpan(leftIcon, ImageSpan.ALIGN_BASELINE);
        ImageSpan span2 = new ImageSpan(rightIcon, ImageSpan.ALIGN_BOTTOM);
        spanString1.setSpan(span1, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanString2.setSpan(span2, 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText("");
        tv.append(spanString1);
        tv.append(content);
        tv.append(spanString2);
    }
}