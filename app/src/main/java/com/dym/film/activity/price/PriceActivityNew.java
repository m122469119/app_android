package com.dym.film.activity.price;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.home.PreFilmDetailActivity;
import com.dym.film.adapter.PriceRecyclerAdapter;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.BaseThread;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.DaoSession;
import com.dym.film.entity.cinema;
import com.dym.film.entity.cinemaDao;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.EffectAnim;
import com.dym.film.utils.ImageLoaderConfigUtil;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.FlingOneGallery;
import com.dym.film.views.LoadMoreRecyclerView;
import com.dym.film.views.MyScrollView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xusoku on 2016/1/8.
 */
public class PriceActivityNew extends BaseActivity{


    public static String KEY = "key";
    public static String NUM = "num";
    public static String PRICE = "price";
    public static String CINEMAID = "cinimaId";
    public static String FILMID = "filmId";
    public static String FILMIDNAME = "filmIdname";
    public static String FLAG = "flag";


    public static int ResultCode = 0x9;
    private cinema cinemaRespModel;

    private View viewHeader;
    private HorizontalScrollView price_horizontalScrollView;
    private View price_no_data_linear;
    private FlingOneGallery gallery;
    EffectAnim amin;
    private View preView;
    private ImageView image_big_bg;

    private LinearLayout price_iv_right_back;
    private MyScrollView price_scroll;
    private ImageView layout_no_data_iv;
    private TextView layout_no_data_tv;

    private LinearLayout price_liner_cinema;

    private TextView cinema_tv_name;
    private TextView cinema_tv_place;
    private TextView price_title_name_title;
    private LinearLayout price_iv_back;

    private NetworkManager networkManager = NetworkManager.getInstance();

    NetworkManager.RespCinemaFilmPriceLists ms;
    private String filmId = "";
    private String filmIdName = "";

    private SwipeRefreshLayout price_swipeLayout;

    private Toast toast;

    private LinearLayout fragment_price_title_linear;

    private RelativeLayout price_fragment_loading;


    private BaiduLBSManager baiduLBSManager= BaiduLBSManager.getInstance();


    private boolean isFirstLoad;
    @Override
    protected int setLayoutView() {
        return  R.layout.fragment_price_item_new;
    }

    @Override
    protected void initVariable() {
        amin = new EffectAnim();
        cinemaRespModel=new cinema();
        filmId=getIntent().getStringExtra(FILMID);
        selectDate=getIntent().getStringExtra(CinemaActivityNew.SDATE);
        isFirstLoad=true;
    }
    @Override
    protected void findViews() {
        viewHeader=getLayoutInflater().inflate(R.layout.fragment_price_new,null);
        price_no_data_linear=getLayoutInflater().inflate(R.layout.layout_load_no_data,null);
        price_horizontalScrollView = (HorizontalScrollView) viewHeader.findViewById(R.id.price_horizontalScrollView);
        price_iv_right_back = (LinearLayout) viewHeader.findViewById(R.id.price_iv_right_back);
        gallery = (FlingOneGallery) viewHeader.findViewById(R.id.price_gallery);
        gallery.setZoomRatio(0.33F);
        image_big_bg = (ImageView) viewHeader.findViewById(R.id.price_image_big_bg);
        price_title_name_title = (TextView) viewHeader.findViewById(R.id.price_title_name_title);
        price_liner_cinema = (LinearLayout) viewHeader.findViewById(R.id.price_liner_place);
        cinema_tv_name = (TextView) viewHeader.findViewById(R.id.price_tv_name);
        cinema_tv_place = (TextView) viewHeader.findViewById(R.id.price_tv_place);
        fragment_price_title_linear= (LinearLayout) viewHeader.findViewById(R.id.fragment_price_title_linear);
        price_scroll = (MyScrollView) viewHeader.findViewById(R.id.price_scroll);
        price_iv_back = (LinearLayout) viewHeader.findViewById(R.id.price_iv_back);
        price_iv_back.setVisibility(View.VISIBLE);

        listView=$(R.id.price_stretch_listview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(linearLayoutManager);
        listView.setLinearLayoutManager(linearLayoutManager);
        adapter=new PriceRecyclerAdapter(this);
        adapterWrapper = new ExRcvAdapterWrapper<>(adapter, linearLayoutManager);
        listView.setAdapter(adapterWrapper);
        adapterWrapper.setHeaderView(viewHeader);
        adapterWrapper.setFooterView(price_no_data_linear);
        // 设置固定大小
        listView.setHasFixedSize(true);



        layout_no_data_tv = (TextView) price_no_data_linear.findViewById(R.id.layout_no_data_tv);
        layout_no_data_iv = (ImageView) price_no_data_linear.findViewById(R.id.layout_no_data_iv);
        layout_no_data_iv.setImageResource(R.drawable.no_price_image);
        layout_no_data_tv.setText("暂无场次");
        price_fragment_loading=$(R.id.price_fragment_loading);
        price_swipeLayout=$(R.id.price_swipeLayout);
        price_swipeLayout.setDistanceToTriggerSync(CommonManager.DEFAULT_REFRESH_DISTANCE);
        price_swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isFirstLoad=true;
                initData();
            }
        });
        CommonManager.setRefreshingState(price_swipeLayout, true);
    }

    public void setTopFrgment(){
        if(price_scroll!=null){
            price_scroll.scrollTo(0, 0);
        }
    }
    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isFirstLoad=true;
        selectDate=intent.getStringExtra(CinemaActivityNew.SDATE);
        initLocal();
    }

    @Override
    protected void initData() {
        initDateTime();
        initLocal();
    }
    //初始化热门 电影列表
    public void init() {

//        if (ms != null && ms.films != null && ms.films.list != null && ms.films.list.size() > 0) {
//            initPosterFilm();
//            return;
//        }
        image_big_bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image_big_bg.setImageResource(R.drawable.ic_default_loading_img);
        ms = new NetworkManager.RespCinemaFilmPriceLists();
        price_title_name_title.setText("");
        gallery.setAdapter(new CommonBaseAdapter<NetworkManager.CinemaFilmPriceListModel>(this, ms.films, R.layout.gallery_image_item) {
            @Override
            public void convert(ViewHolder holder, NetworkManager.CinemaFilmPriceListModel itemData, int position) {
                ImageView iv = (ImageView) holder.getView(R.id.price_pre_image_item);
                CommonManager.displayImage(itemData.post, iv, R.drawable.ic_default_loading_img);
            }
        });


        networkManager.getCinemaFilmPriceListv2(cinemaRespModel.getCinemaID(), new HttpRespCallback<NetworkManager.RespCinemaFilmPriceLists>() {
            @Override
            public void onRespFailure(int code, String msg) {
                CommonManager.setRefreshingState(price_swipeLayout, false);
                price_fragment_loading.setVisibility(View.GONE);
                price_no_data_linear.setVisibility(View.VISIBLE);

            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);

                CommonManager.setRefreshingState(price_swipeLayout, false);
                price_fragment_loading.setVisibility(View.GONE);
                ms = (NetworkManager.RespCinemaFilmPriceLists) msg.obj;
                if (ms != null && ms.films != null && ms.films != null && ms.films.size() > 0) {
                    initPosterFilm();
                } else {
                    price_no_data_linear.setVisibility(View.VISIBLE);
                    showTextToast("影院暂无影片");
                }
            }
        });
    }

    private void initPosterFilm() {
        gallery.setAdapter(new CommonBaseAdapter<NetworkManager.CinemaFilmPriceListModel>(this, ms.films, R.layout.gallery_image_item) {
            @Override
            public void convert(ViewHolder holder, NetworkManager.CinemaFilmPriceListModel itemData, int position) {
                ImageView iv = (ImageView) holder.getView(R.id.price_pre_image_item);
                ImageLoaderConfigUtil.setDisplayImager(R.drawable.ic_default_loading_img, iv, itemData.post, true);
            }
        });

        if (!TextUtils.isEmpty(filmId)) {
            for (int i = 0; i < ms.films.size(); i++) {
                if (filmId.equals(ms.films.get(i).filmID+"")) {

                    gallery.setSelection(i);
                    return;
                }
            }
            if(ms.films.size()>0){
                gallery.setSelection(0);
                showTextToast("影院暂无该影片");
            }
        }
        if(ms.films.size()>0)
            gallery.setSelection(0);
        filmId = ms.films.get(0).filmID + "";
        filmIdName = ms.films.get(0).name + "";
//        initLocal();
    }

    //初始化影院列表第一个
    public void initCinemaFristData(){

        DaoSession session = DatabaseManager.getInstance().getDaoSession();
        cinemaDao dao = session.getCinemaDao();
        if (dao.getCinemaListCount(dao) > 0) {
            List<cinema> cinemaList = dao.getCinemaList(dao);
            if (cinemaList != null && cinemaList.size() > 0) {
                cinemaRespModel = cinemaList.get(0);
                if(!TextUtils.isEmpty(cinemaRespModel.getCinemaID())) {
                    cinema_tv_name.setText(cinemaRespModel.getName());
                    cinema_tv_place.setText(cinemaRespModel.getAddress());
                    LogUtils.e("init", "init2");
                    init();
//                    handler.removeMessages(1);
//                    handler.sendEmptyMessage(1);
                }
                return;
            }
        }

        cinemaRespModel=new cinema();
        String city=baiduLBSManager.getCity();
        String region=baiduLBSManager.getDistrict();
        SharedPreferences preferences = this.getSharedPreferences(BaiduLBSManager.PREF_LBS_NAME, Context.MODE_PRIVATE);
        String lng=preferences.getString(BaiduLBSManager.KEY_LONGITUDE, "");
        String lat=preferences.getString(BaiduLBSManager.KEY_LATITUDE, "");
        if (TextUtils.isEmpty(city)){
            city="北京市";
        }
        if(TextUtils.isEmpty(region)){
            region="";
        }
        if (TextUtils.isEmpty(lng)){
            lng="";
        }
        if(TextUtils.isEmpty(lat)){
            lat="";
        }

        networkManager.getCinemaList(city, region, lng, lat, 0, 3, new HttpRespCallback<NetworkManager.RespCinemaList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                cinema_tv_name.setText("暂无影院");
                cinema_tv_place.setText("暂无地址");
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespCinemaList ms = (NetworkManager.RespCinemaList) msg.obj;
                if (ms != null && ms.cinemas != null && ms.cinemas.size() > 0) {
                    NetworkManager.CinemaRespModel cinema = (ms.cinemas.get(0));

                    for (int i = 0; i < ms.cinemas.size(); i++) {
                        cinema = (ms.cinemas.get(i));
                        String price = ms.cinemas.get(i).minPrice;
                        if (TextUtils.isEmpty(price) || price.equals("0.0") || price.equals("0")) {
                            continue;
                        } else {
                            break;
                        }
                    }
                    cinemaRespModel.setCinemaID(cinema.cinemaID);
                    cinemaRespModel.setAddress(cinema.address);
                    cinemaRespModel.setName(cinema.name);
                    cinemaRespModel.setLat(cinema.latitude);
                    cinemaRespModel.setLng(cinema.longitude);
                    cinema_tv_name.setText(cinemaRespModel.getName());
                    cinema_tv_place.setText(cinemaRespModel.getAddress());

                    if (!TextUtils.isEmpty(cinemaRespModel.getCinemaID())) {
                        LogUtils.e("init","init1");
                        init();
//                        handler.removeMessages(1);
//                        handler.sendEmptyMessage(1);
                    }

                } else {
                    cinema_tv_name.setText("暂无影院");
                    cinema_tv_place.setText("暂无地址");
                }
            }
        });
    }


    //初始化比价购票
    private SparseArray<View> sparseArray;
    private ArrayList<NetworkManager.MYDATE> dates;
    private String selectDate="";
    private void initDateTime(){
        dates= new ArrayList<>();
        for (int i=0; i < 7; i++)
            dates.add(CommonManager.getToady(i));
    }
    private void InitTitlePager() {
        if(dates==null){
            return;
        }
        sparseArray=new SparseArray<View>();
        hashmap=new HashMap<>();
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(CommonManager.dpToPx(90), LinearLayout.LayoutParams.MATCH_PARENT);
        fragment_price_title_linear.removeAllViews();
        for (int i=0; i < 7; i++){
            View view=getLayoutInflater().inflate(R.layout.list_item_tab_title_layout,null);
            SetTiTleText(dates.get(i), view);
            sparseArray.put(i, view);
            view.setLayoutParams(params);
            fragment_price_title_linear.addView(sparseArray.get(i), i);
        }
        initViewPrce();
        SetTextColorFaction(0);
        SetTextOnClick();
    }

    private LoadMoreRecyclerView listView;
    private ExRcvAdapterWrapper adapterWrapper;
    private PriceRecyclerAdapter adapter;
    private HashMap<String,ArrayList<NetworkManager.TicketPriceModel>> hashmap;

    private void initViewPrce() {
        listView=$(R.id.price_stretch_listview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(linearLayoutManager);
        listView.setLinearLayoutManager(linearLayoutManager);
        adapter=new PriceRecyclerAdapter(this);
        adapterWrapper = new ExRcvAdapterWrapper<>(adapter, linearLayoutManager);
        listView.setAdapter(adapterWrapper);
        adapterWrapper.setHeaderView(viewHeader);
        adapterWrapper.setFooterView(price_no_data_linear);
        // 设置固定大小
        listView.setHasFixedSize(true);
        initprceData(dates.get(0).date);
    }

    private void initprceData(final String date) {

        if(hashmap.get(date)!=null&&hashmap.get(date).size()>0){
            price_no_data_linear.setVisibility(View.GONE);
            adapter.setMyData(date);
            adapter.setCinemaId(cinemaRespModel.getCinemaID());
            adapter.setFilmId(filmId);
            hashmap.get(date).get(0).flag=true;
            adapter.setAll(hashmap.get(date));
            return;
        }
        price_fragment_loading.setVisibility(View.VISIBLE);
        networkManager.getHotTicketPriceList(cinemaRespModel.getCinemaID(), filmId, date, new HttpRespCallback<NetworkManager.RespTicketPriceList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                price_fragment_loading.setVisibility(View.GONE);
                price_no_data_linear.setVisibility(View.VISIBLE);
                adapter.setAll(new ArrayList<NetworkManager.TicketPriceModel>());
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                price_fragment_loading.setVisibility(View.GONE);
                price_no_data_linear.setVisibility(View.GONE);
                NetworkManager.RespTicketPriceList ms = (NetworkManager.RespTicketPriceList) msg.obj;
                if (ms != null && ms.tickets != null && ms.tickets.size() > 0) {
                    hashmap.put(date, ms.tickets);
                    adapter.setMyData(date);
                    adapter.setCinemaId(cinemaRespModel.getCinemaID());
                    adapter.setFilmId(filmId);
                    ms.tickets.get(0).flag = true;
                    adapter.setAll(ms.tickets);
                } else {
                    price_no_data_linear.setVisibility(View.VISIBLE);
                    adapter.setAll(new ArrayList<NetworkManager.TicketPriceModel>());
                }

            }
        });
    }


    public static class MyHandler extends Handler {
        private final WeakReference<PriceActivityNew> mFragment;

        public MyHandler(PriceActivityNew activity) {
            mFragment = new WeakReference<PriceActivityNew>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PriceActivityNew activity = mFragment.get();
            if (activity != null) {
                activity.InitTitlePager();
            }
        }
    }
    public final MyHandler handler = new MyHandler(this);

    private String film_id="";
    @Override
    protected void setListener() {
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int arg2, long arg3) {
                if (preView != null) {
//                    amin.showLooseFocusAinimation(preView);
                    ImageView iv = (ImageView) preView.findViewById(R.id.price_pre_image_item);
                    iv.setBackgroundResource(R.color.main_bg_color);
                }
                // 放大
                if (view != null) {
//                    amin.showOnFocusAnimation(false, view);
                    if (ms.films.size() > 0) {
                        film_id=filmId;
                        filmId = ms.films.get(arg2).filmID+"";
                        if (!TextUtils.isEmpty(ms.films.get(arg2).post)) {
                            ImageView iv = (ImageView) view.findViewById(R.id.price_pre_image_item);
                            iv.setBackgroundResource(R.drawable.price_film_bg_red);
                        }
                        if(!isFirstLoad&&film_id.equals(filmId)){
                            preView = view;
                            return;
                        }
                        isFirstLoad=false;
                        filmIdName = ms.films.get(arg2).name+"";
                        price_title_name_title.setText(ms.films.get(arg2).name);
                        ImageLoaderConfigUtil.setBlurImager(PriceActivityNew.this,
                                R.drawable.ic_loading,
                                image_big_bg,
                                ms.films.get(arg2).post,
                                true);
                        price_horizontalScrollView.scrollTo(0, 0);

                        if(!TextUtils.isEmpty(cinemaRespModel.getCinemaID())) {
                            handler.removeMessages(1);
                            handler.sendEmptyMessageDelayed(1,600);
                        }else{
                            initLocal();
                        }
                    }
                }
                preView = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (ms.films.size() > 0) {
                    String filmid = ms.films.get(position).filmID+"";
                    if(filmId.equals(filmid)){
                        Intent intent = new Intent();
                        intent.setClass(PriceActivityNew.this, FilmDetailActivity.class);
                        intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, filmId + "");
                        startActivity(intent);
                    }
                }
            }
        });

        price_liner_cinema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(PriceActivityNew.this, CinemaActivityNew.class);
                it.putExtra(FILMID, filmId);
                it.putExtra(CinemaActivityNew.SDATE, selectDate);
                it.putExtra(PriceActivityNew.FILMIDNAME, filmIdName);
                it.putExtra(PriceActivityNew.FLAG, true);
                startActivityForResult(it, 12);
            }
        });

        price_title_name_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PriceActivityNew.this, FilmDetailActivity.class);
                intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, filmId + "");
                startActivity(intent);
            }
        });
        price_iv_right_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PriceActivityNew.this, FilmDetailActivity.class);
                intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, filmId + "");
                startActivity(intent);
            }
        });
    }

    @Override
    public void doClick(View view) {

        if(view.getId()==R.id.price_iv_back){
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == ResultCode) {
            cinema ci = (cinema) data.getSerializableExtra(KEY);
            if (!ci.getCinemaID().equals(cinemaRespModel.getCinemaID())) {
                cinemaRespModel = ci;
                cinema_tv_name.setText(cinemaRespModel.getName());
                cinema_tv_place.setText(cinemaRespModel.getAddress());
//                handler.removeMessages(1);
//                handler.sendEmptyMessage(1);
                LogUtils.e("init","init3");
                setListener();
                isFirstLoad=true;
                selectDate=data.getStringExtra(CinemaActivityNew.SDATE);
                init();
            }
        }
    }
    //初始化定位
    private LocationThread locationThread;
    private void initLocal(){
        String strCity=baiduLBSManager.getCity();
        if(TextUtils.isEmpty(strCity)){
            locationThread=new LocationThread();
            locationThread.startThread();
        }else {
            initCinemaFristData();
        }
    }

    //定位
    protected class LocationThread extends BaseThread
    {
        public final static int WHAT_LOCATION_FINISHED = 0x31;
        @Override
        public void run()
        {
            // 开始定位
            baiduLBSManager.registerLocationListener(mLocationListener);
            baiduLBSManager.startLocation();
        }
        @Override
        public void stopThread()
        {
            super.stopThread();
            baiduLBSManager.unregisterLocationListener(mLocationListener);
            baiduLBSManager.stopLocation();
        }

        protected BaiduLBSManager.SimpleLocationListener
                mLocationListener = new BaiduLBSManager.SimpleLocationListener()
        {
            @Override
            public void onLocationFinished(BDLocation location)
            {
                sendMessage(WHAT_LOCATION_FINISHED, location);
            }
        };

        @Override
        public void handleMessage(Message msg)
        {
            BDLocation location = (BDLocation) msg.obj;
            if (location == null) {
//                mTitleLocationText.setText("无法定位");
                initCinemaFristData();
                return;
            }
            double  mLatitude = location.getLatitude();
            double mLongitude = location.getLongitude();
            String district = location.getDistrict();
            String city = location.getCity();
            String province = location.getProvince();

            SharedPreferences preferences = mContext.getSharedPreferences(BaiduLBSManager.PREF_LBS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(BaiduLBSManager.KEY_PROVINCE, province);
            editor.putString(BaiduLBSManager.KEY_CITY, city);
            editor.putString(BaiduLBSManager.KEY_DISTRICT, district);
            editor.putString(BaiduLBSManager.KEY_LONGITUDE, String.valueOf(mLongitude));
            editor.putString(BaiduLBSManager.KEY_LATITUDE, String.valueOf(mLatitude));
            editor.apply();
            initCinemaFristData();
            baiduLBSManager.stopLocation();
            baiduLBSManager.unregisterLocationListener(mLocationListener);

        }
    }



    //设置日期内容
    private void  SetTiTleText(NetworkManager.MYDATE mydate ,View view){
        LinearLayout ll = (LinearLayout) view;
        if (ll == null) {
            return;
        }
        TextView tv_number = (TextView) ll.findViewById(R.id.price_number_fragment_title);
        TextView tv_week_number = (TextView) ll.findViewById(R.id.price_week_fragment_title);

        tv_number.setText(mydate.day);
        tv_week_number.setText(mydate.week);
    }
    //设置日期点击事件
    private void SetTextOnClick() {

        sparseArray.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(0);
                initprceData(dates.get(0).date);
            }
        });
        sparseArray.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(1);
                initprceData(dates.get(1).date);
            }
        });
        sparseArray.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(2);
                initprceData(dates.get(2).date);
            }
        });
        sparseArray.get(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(3);
                initprceData(dates.get(3).date);
            }
        });
        sparseArray.get(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(4);
                initprceData(dates.get(4).date);
            }
        });
        sparseArray.get(5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(5);
                initprceData(dates.get(5).date);
            }
        });
        sparseArray.get(6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetTextColorFaction(6);
                initprceData(dates.get(6).date);
            }
        });
    }
    //设置日期背景
    private void SetTextColorFaction(int position) {

        selectDate=dates.get(position).date;

        for (int i = 0; i < sparseArray.size(); i++) {
            View view = sparseArray.get(i);
            if (view != null) {
                LinearLayout lll = (LinearLayout) view;
                if (lll == null) {
                    continue;
                }
//                lll.setBackgroundResource(R.color.vp_tab_normal_color);
                TextView tv_number = (TextView) lll.findViewById(R.id.price_number_fragment_title);
                TextView tv_week_number = (TextView) lll.findViewById(R.id.price_week_fragment_title);
                tv_number.setTextColor(Color.parseColor("#b6b6b6"));
                tv_week_number.setTextColor(Color.parseColor("#b6b6b6"));
            }
        }
        LinearLayout ll = (LinearLayout) sparseArray.get(position);
        if (ll == null) {
            return;
        }
        ll.setBackgroundResource(R.color.red_color);
        TextView tv_number = (TextView) ll.findViewById(R.id.price_number_fragment_title);
        TextView tv_week_number = (TextView) ll.findViewById(R.id.price_week_fragment_title);
        tv_number.setTextColor(Color.parseColor("#ffffff"));
        tv_week_number.setTextColor(Color.parseColor("#ffffff"));
    }

    private void showTextToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationThread!=null){
            locationThread.stopThread();
        }
        if (handler != null) {
            handler.removeMessages(1);
        }
    }

}
