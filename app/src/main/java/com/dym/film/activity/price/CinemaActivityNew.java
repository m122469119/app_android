package com.dym.film.activity.price;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.CinemaListAdapter;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.ProgressWheel;
import com.dym.film.ui.citylist.adapter.CityAdapter;
import com.dym.film.ui.citylist.data.CityData;
import com.dym.film.ui.citylist.widget.ContactItemInterface;
import com.dym.film.ui.citylist.widget.ContactListViewImpl;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.ACache;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CinemaActivityNew extends BaseActivity {

    public static final String SDATE="selectDate";
    private TextView cinema_iv_title_name;
    private LoadMoreRecyclerView cnima_listview;
    private ExRcvAdapterWrapper adapterWrapper;
    private LinearLayoutManager linearLayoutManager;
    private View listview_header;
    private ArrayList<NetworkManager.CinemaRespModel> list;
    private CinemaListAdapter adapter;

    private ImageView cinema_expandable_image_city;
    private View cityView;
    private PopupWindow citypopupWindow;
    private RelativeLayout cinema_city_relative;
    private ContactListViewImpl city_listview;
    List<ContactItemInterface> contactList;
    List<ContactItemInterface> filterList;


    private ImageView cinema_expandable_image_region;
    private View regionView;
    private PopupWindow regionpopupWindow;
    private RelativeLayout cinema_city_region;
    private GridView regionGradView;

    private TextView cinema_text_city;
    private TextView cinema_text_region;
    private String city = "";
    private String region = "";
    private String lng = "";
    private String lat = "";
    private String flimId = "";
    private String flimIdName = "";
    private String selectDate = "";
    private boolean isCityloaded = false;

    private TextView cinema_city_locate_tv;//定位按钮
    private TextView cinema_tv_region_item;//区域不限按钮

    // 第一页
    private int intCurrentPage = 0;
    // 每页10项数据
    private int PAGE_SIZE = 20;
    // 下一页的加载状态
    private boolean isNextPageLoading = true;
    private NetworkManager mNetworkManager = NetworkManager.getInstance();

    private ProgressWheel progress_dialog;
    private LinearLayout cinema_no_data_linear;
    private ImageView layout_no_data_iv;
    private TextView layout_no_data_tv;


    private LinearLayout fragment_price_title_linear;
    private HorizontalScrollView fragment_price_horizontalScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_cinema_new;
    }


    //初始化比价购票
    private SparseArray<View> sparseArray;
    private ArrayList<NetworkManager.MYDATE> dates;
    private int totalDates = 7;

    private void initDateTime() {
        dates = new ArrayList<>();
        for (int i = 0; i < totalDates; i++)
            dates.add(CommonManager.getToady(i));
    }

    private void InitTitlePager(int m) {
        sparseArray = new SparseArray<View>();
        hashMap.clear();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(CommonManager.dpToPx(70), LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(CommonManager.dpToPx(7),CommonManager.dpToPx(7),CommonManager.dpToPx(7),CommonManager.dpToPx(7));
        fragment_price_title_linear.removeAllViews();
        for (int i = 0; i < totalDates; i++) {
            View view = getLayoutInflater().inflate(R.layout.list_item_tab_title_layout, null);
            SetTiTleText(dates.get(i), view);
            sparseArray.put(i, view);
            view.setBackgroundResource(R.drawable.bg_btn_grey_price_date);
            view.setLayoutParams(params);
            fragment_price_title_linear.addView(sparseArray.get(i), i);
        }

        setSelection2HScrollview(m);
        SetTextColorFaction(m);
        SetTextOnClick();
        if(flag){
            fragment_price_title_linear.setVisibility(View.GONE);
        }
    }

    private void setSelection2HScrollview(final int m) {

        if(!isOnclick)
        new Handler().postDelayed((new Runnable() {
            @Override
            public void run() {
                fragment_price_horizontalScrollView.scrollTo(((LinearLayout) fragment_price_title_linear.getChildAt(m)).getLeft() - 100, 0);
            }
        }), 5);
    }


    @Override
    protected void initVariable() {
        initDateTime();
        city = BaiduLBSManager.getInstance().getCity();
        region = BaiduLBSManager.getInstance().getDistrict();
        SharedPreferences preferences = getSharedPreferences(BaiduLBSManager.PREF_LBS_NAME, Context.MODE_PRIVATE);
        lng = preferences.getString(BaiduLBSManager.KEY_LONGITUDE, "");
        lat = preferences.getString(BaiduLBSManager.KEY_LATITUDE, "");
        if (TextUtils.isEmpty(city)) {
            city = "北京市";
        }
//        if(TextUtils.isEmpty(region)){
        region = "区域不限";
//        }
        if (TextUtils.isEmpty(lng)) {
            lng = "";
        }
        if (TextUtils.isEmpty(lat)) {
            lat = "";
        }

        if (!TextUtils.isEmpty(ConfigInfo.city)) {
            city = ConfigInfo.city;
        }
        if (!TextUtils.isEmpty(ConfigInfo.region)) {
            region = ConfigInfo.region;
        }
        flimId = getIntent().getStringExtra(PriceActivityNew.FILMID);
        flimIdName = getIntent().getStringExtra(PriceActivityNew.FILMIDNAME);
        //true 从底部tab进去  false 从影片详情进去
        flag = getIntent().getBooleanExtra(PriceActivityNew.FLAG, true);
        if (TextUtils.isEmpty(flimIdName)) {
            flimIdName = "切换影院";
        }
        if(flag){
            flimIdName = "切换影院";
        }
        selectDate=getIntent().getStringExtra(CinemaActivityNew.SDATE);

        for (int i=0;i<dates.size();i++) {
            if(dates.get(i).date.equals(selectDate)){
                selectPosition=i;
                break;
            }
        }
    }

    @Override
    protected void findViews() {
        fragment_price_horizontalScrollView = $(R.id.fragment_price_horizontalScrollView);
        cinema_iv_title_name = $(R.id.cinema_iv_title_name);
        cnima_listview = $(R.id.cnima_listview);
        cinema_no_data_linear = $(R.id.cinema_no_data_linear);
        layout_no_data_iv = $(R.id.layout_no_data_iv);
        layout_no_data_tv = $(R.id.layout_no_data_tv);
        cinema_city_relative = $(R.id.cinema_city_relative);
        cinema_city_region = $(R.id.cinema_city_region);
        cinema_expandable_image_city = $(R.id.cinema_expandable_image_city);
        cinema_expandable_image_region = $(R.id.cinema_expandable_image_region);
        listview_header = getLayoutInflater().inflate(R.layout.cinema_layout_listtop, null);
        cinema_text_city = $(R.id.cinema_text_city);
        cinema_text_region = $(R.id.cinema_text_region);
        progress_dialog = $(R.id.progress_dialog);
        fragment_price_title_linear = $(R.id.fragment_price_title_linear);
        loadInit();
        layout_no_data_iv.setImageResource(R.drawable.no_cinema_image);
        layout_no_data_tv.setText("暂无影院");

        cinema_iv_title_name.setText(flimIdName);
    }


    private void loadInit() {
        cnima_listview.getLoadMoreFooterController().setText("");
        linearLayoutManager = new LinearLayoutManager(this);
        cnima_listview.setLayoutManager(linearLayoutManager);
        cnima_listview.setLinearLayoutManager(linearLayoutManager);
        // 设置ItemAnimator
        cnima_listview.setItemAnimator(new DefaultItemAnimator());
        adapter = new CinemaListAdapter(this);
        adapter.setFlag(flag);
        adapter.setFilmId(flimId);
        adapter.setSelectDate(selectDate);
        adapterWrapper = new ExRcvAdapterWrapper<>(adapter, linearLayoutManager);
//        adapterWrapper.setHeaderView(listview_header);
        cnima_listview.setAdapter(adapterWrapper);
        // 设置固定大小
        cnima_listview.setHasFixedSize(true);
    }


    @Override
    protected void initData() {
        hashMap = new HashMap<>();
        list = new ArrayList<NetworkManager.CinemaRespModel>();
        cinema_text_city.setText(city);
        cinema_text_region.setText(region);

        startActivityLoading();
    }

    @Override
    protected void onActivityLoading() {
        super.onActivityLoading();
        initPopupRegionWindow();
        initPopupCityWindow();
    }


    private int selectPosition = 0;
    private boolean flag = true;
    private boolean isFirstFlag = false;
    private HashMap<String, ArrayList<NetworkManager.CinemaRespModel>> hashMap;

    private void loadData(final boolean isFirst, final int position) {

        this.selectPosition = position;
        selectDate=dates.get(selectPosition).date;
        if (isFirst) {
            intCurrentPage = 0;
            list.clear();
            loadInit();
            // 可以显示加载界面
            progress_dialog.setVisibility(View.VISIBLE);
            cinema_no_data_linear.setVisibility(View.GONE);
        }

        if (region.equals("区域不限") || region.equals("地区")) {
            region = "";
        }
        if (city.equals("城市")) {
            city = "北京市";
        }
        mNetworkManager.getCinemaList(flag, flimId, dates.get(position).date, city, region, lng, lat, intCurrentPage++, PAGE_SIZE, new HttpRespCallback<NetworkManager.RespCinemaList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
                cinema_no_data_linear.setVisibility(View.VISIBLE);
                onActivityLoadingFailed();
                if (!isFirstFlag) {
                    InitTitlePager(position);
                }
            }

            @Override
            public void onRespCode101(int code, ArrayList<String> msg) {
                super.onRespCode101(code, msg);
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
                cinema_no_data_linear.setVisibility(View.GONE);
                onActivityLoadingSuccess();
                if (isFirstFlag) {
                    return;
                }
                isFirstFlag = true;
                if (code == 101) {
                    ArrayList<String> listDatas = msg;
                    if (listDatas == null || listDatas.size() == 0) {
                        if (dates == null || dates.size() == 0) {
                            initDateTime();
                        }
                        InitTitlePager(0);
                    } else {
                        NetworkManager.MYDATE date = dates.get(0);
                        dates.clear();
                        dates.add(date);
                        for (String str : listDatas) {
                            dates.add(CommonManager.getToadyByString(str));
                        }
                        totalDates = dates.size();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (totalDates >= 1) {
                                    InitTitlePager(1);
                                    getinitLoadData(1);
                                } else {
                                    InitTitlePager(0);
                                }
                            }
                        }, 100);
                    }
                }
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                progress_dialog.setVisibility(View.GONE);
                cinema_no_data_linear.setVisibility(View.GONE);
                onActivityLoadingSuccess();
                if (!isFirstFlag) {
                    InitTitlePager(position);
                }
                NetworkManager.RespCinemaList ms = (NetworkManager.RespCinemaList) msg.obj;
                if (ms != null && ms.cinemas != null && ms.cinemas.size() > 0) {
                    int size = (ms.cinemas == null ? 0 : ms.cinemas.size());
                    if (size < PAGE_SIZE) {
                        cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    } else {
                        cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                    adapter.appendAll(ms.cinemas, false);
                    list.addAll(ms.cinemas);

                    if (isFirst) {
                        hashMap.put(dates.get(position).date, ms.cinemas);
                    }
                    adapterWrapper.setFooterView(cnima_listview.getLoadMoreFooterController().getFooterView());
                    endRefresh(true);
                    adapter.setOnItemClickListener(new CinemaListAdapter.onItemClickListener() {
                        @Override
                        public void onItemClick() {
                            ConfigInfo.city = city;
                            ConfigInfo.region = region.equals("") ? "区域不限" : region;
                            LogUtils.e("city", " ConfigInfo.city " + city + "  region      " + region);
                        }
                    });
                } else {
                    cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    if (list.size() > 0) {
//                        Toast.makeText(CinemaActivity.this, "加载完毕", Toast.LENGTH_LONG).show();
                    } else {
                        cinema_no_data_linear.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
    }

    private void endRefresh(boolean result) {
        isNextPageLoading = false;
        if (result) {
            cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
        } else {
            cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            if (!NetWorkUtils.isAvailable(this)) {
                cinema_no_data_linear.setVisibility(View.VISIBLE);
            }
            cnima_listview.setFooterClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetWorkUtils.isAvailable(CinemaActivityNew.this)) {
                        cinema_no_data_linear.setVisibility(View.VISIBLE);
                        return;
                    }
                    cnima_listview.startLoadMore();
                }
            });
        }
    }


    private void loadMore() {
        if (isNextPageLoading) {
            return;
        }
        isNextPageLoading = true;
        isOnclick=true;
        loadData(false, selectPosition);
    }

    @Override
    protected void setListener() {

        regionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (regionpopupWindow != null && regionpopupWindow.isShowing()) {
                    regionpopupWindow.dismiss();
                }
            }
        });

        cnima_listview.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onNeedLoadMore() {
                loadMore();
            }
        });


    }

    @Override
    public void doClick(View view) {

        switch (view.getId()) {
            case R.id.cinema_city_region:
                if (isCityloaded) {
                    Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                regionpopupWindow.showAsDropDown(cinema_city_relative);
                cinema_expandable_image_region.setImageResource(R.drawable.price_expandable_open);
                break;

            case R.id.cinema_city_relative:
                if (isCityloaded) {
                    Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                citypopupWindow.showAsDropDown(cinema_city_relative);
                cinema_expandable_image_city.setImageResource(R.drawable.price_expandable_open);
                break;
            case R.id.cinema_iv_back:
//                if(cinemas!=null){
//                    Intent intent = new Intent();
//                    intent.setClass(this, PriceActivity.class);
//                    intent.putExtra(PriceActivity.KEY,cinemas);
//                    startActivity(intent);
//                    finish();
//                }else{
                finish();
//                }
                break;

        }
    }


    //初始化城市
    private void initPopupCityWindow() {
        // TODO Auto-generated method stub
        cityView = getLayoutInflater().inflate(R.layout.cinema_citylist_layout, null);
        citypopupWindow = new PopupWindow(cityView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        citypopupWindow.setFocusable(true);
        citypopupWindow.setOutsideTouchable(true);
        citypopupWindow.setAnimationStyle(R.style.popwin_recent_anim_style);
        citypopupWindow.setBackgroundDrawable(new BitmapDrawable());


        ACache aCache = ACache.get(CinemaActivityNew.this);
        NetworkManager.RespCityList cityList = (NetworkManager.RespCityList) aCache.getAsObject("city");
        String isUpdataCity = aCache.getAsString("isUpdataCity");
        if (!TextUtils.isEmpty(isUpdataCity) && isUpdataCity.equals("1")) {
            initDataCity();
        } else {
            if (cityList != null && cityList.cities != null && !cityList.cities.isEmpty()) {
                initCityView(cityList);
            } else {
                initDataCity();
            }
        }

        citypopupWindow.setOnDismissListener(new popupWindowclickListener());
    }

    //初始化城市
    private void initPopupRegionWindow() {
        // TODO Auto-generated method stub
        regionView = getLayoutInflater().inflate(R.layout.cinema_layout_region, null);
        regionpopupWindow = new PopupWindow(regionView,
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        regionpopupWindow.setFocusable(true);
        regionpopupWindow.setOutsideTouchable(true);
        regionpopupWindow.setAnimationStyle(R.style.popwin_recent_anim_style);
        regionpopupWindow.setBackgroundDrawable(new BitmapDrawable());
        regionpopupWindow.setOnDismissListener(new popupWindowclickListener());
    }

    class popupWindowclickListener implements PopupWindow.OnDismissListener {

        @Override
        public void onDismiss() {
            cinema_expandable_image_city.setImageResource(R.drawable.price_expandable_close);
            cinema_expandable_image_region.setImageResource(R.drawable.price_expandable_close);
        }
    }

    private void initCityView(NetworkManager.RespCityList cityList) {
        if(cityList!= null)
        contactList = CityData.getSampleContactList(cityList.cities);
        if (contactList == null || contactList.size() == 0) {
            isCityloaded = true;
        } else {
            if (TextUtils.isEmpty(city)) {
                city = "北京市";
                region = "区域不限";
                cinema_text_city.setText(city);
                cinema_text_region.setText(region);
            }

            CityAdapter adapter = new CityAdapter(this, R.layout.city_item, contactList);
            city_listview = (ContactListViewImpl) cityView.findViewById(R.id.city_listview);
            city_listview.setAdapter(adapter);

            city_listview.setFastScrollEnabled(true);
            cinema_city_locate_tv = (TextView) cityView.findViewById(R.id.cinema_city_locate_tv);
            cityView.findViewById(R.id.local_linear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str = cinema_city_locate_tv.getText().toString().trim();
                    if (TextUtils.isEmpty(str) || str.equals("定位失败")) {
                        return;
                    }
                    city = BaiduLBSManager.getInstance().getCity();
//                region = BaiduLBSManager.getInstance().getDistrict();
                    region = "区域不限";
                    cinema_text_city.setText(city);
                    cinema_text_region.setText(region);
                    citypopupWindow.dismiss();
                    hashMap.clear();
                    getinitLoadData(0);

                }
            });
            city_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View v, int position,
                                        long id) {

                    city = contactList.get(position).getDisplayInfo();
                    region = "区域不限";
                    cinema_text_region.setText(region);
                    cinema_text_city.setText(city);
                    citypopupWindow.dismiss();
                    hashMap.clear();
                    getinitLoadData(0);
                }
            });
            String cityy = BaiduLBSManager.getInstance().getCity();
            if (TextUtils.isEmpty(cityy)) {
                cityy = "定位失败";
            }
            cinema_city_locate_tv.setText(cityy);
        }
        getinitLoadData(selectPosition);
    }

    private HashMap<String, String> map = new HashMap<>();

    private void initRegionView(final ArrayList<NetworkManager.CinemaNumberModel> list) {

//         android:background="@drawable/bg_shape_item_cinema_region"

        regionGradView = (GridView) regionView.findViewById(R.id.cinema_girdview);
        if(flag){
            regionGradView.setNumColumns(4);
        }else{
            regionGradView.setNumColumns(3);
        }
        cinema_tv_region_item = (TextView) regionView.findViewById(R.id.cinema_tv_region_item);
        cinema_tv_region_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                region = "区域不限";
                cinema_text_region.setText(region);
                regionpopupWindow.dismiss();
                if(hashMap!=null)
                hashMap.clear();
                getinitLoadData(selectPosition);
            }
        });
//        final ArrayList<String> str = new ArrayList<String>();
//        ArrayList<String> str1 = new ArrayList<String>();
//        for (int i = 0; i < contactList.size(); i++) {
//            if (contactList.get(i).getDisplayInfo().contains(city)) {
//                str.addAll(contactList.get(i).getDisplayList());
//            }
//        }
//        for (NetworkManager.CinemaNumberModel model : list) {
//            map.put(model.district, " (" + model.sum + ")");
//        }
//
//        for (String string : str) {
//            str1.add(string + map.get(string));
//        }
        regionGradView.setAdapter(new CommonBaseAdapter<NetworkManager.CinemaNumberModel>(this, list, R.layout.cinema_layout_region_item) {
            @Override
            public void convert(ViewHolder holder, NetworkManager.CinemaNumberModel itemData, int position) {
                TextView textView=holder.getView(R.id.cinema_tv_region_item);
                if(flag){
                    textView.setText( itemData.district);
                }else{
                    textView.setText( itemData.district+" (" + itemData.sum + ")");
                }
//                holder.setText(R.id.cinema_tv_region_item, itemData.district+" (" + itemData.sum + ")");

                if(itemData.district.equals(region)){
                    textView.setBackgroundResource(R.color.red_color);
                    textView.setTextColor(getResources().getColor(R.color.white));
                }else{
                    textView.setBackgroundResource(R.drawable.bg_shape_text_item_cinema_region);
                    textView.setTextColor(getResources().getColor(R.color.cinema_region_text_item));
                }

            }
        });

        String regionLineartext = cinema_text_region.getText().toString().trim();
        if (!TextUtils.isEmpty(regionLineartext) && !regionLineartext.equals("区域不限")) {

            cinema_tv_region_item.setBackgroundResource(R.drawable.bg_shape_text_item_cinema_region);
            cinema_tv_region_item.setTextColor(getResources().getColor(R.color.cinema_region_text_item));
            for (NetworkManager.CinemaNumberModel string : list) {
                if (regionLineartext.contains(string.district)&&!flag) {
                    cinema_text_region.setText(string.district +" (" + string.sum + ")");
                }
            }
        }else{
            cinema_tv_region_item.setBackgroundResource(R.color.red_color);
            cinema_tv_region_item.setTextColor(getResources().getColor(R.color.white));
        }

        regionGradView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position,
                                    long id) {

                region = list.get(position).district;
                if(flag){
                    cinema_text_region.setText(region );
                }else{
                cinema_text_region.setText(region + " (" + list.get(position).sum+ ")");
                }
                cinema_tv_region_item.setBackgroundResource(R.drawable.bg_shape_text_item_cinema_region);
                cinema_tv_region_item.setTextColor(getResources().getColor(R.color.cinema_region_text_item));
                regionpopupWindow.dismiss();
                hashMap.clear();
                loadData(true, selectPosition);
            }
        });


    }

    private void initDataCity() {
        mNetworkManager.getCityList(new HttpRespCallback<NetworkManager.RespCityList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                isCityloaded = true;
                if (TextUtils.isEmpty(region)) {
                    region = "区域不限";
                }
                cinema_text_city.setText(city);
                cinema_text_region.setText(region);
                initCityView(null);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                isCityloaded = false;
                NetworkManager.RespCityList cityList = (NetworkManager.RespCityList) msg.obj;
                ACache aCache = ACache.get(CinemaActivityNew.this);
                aCache.put("city", cityList);
                initCityView(cityList);
            }
        });
    }


    //设置日期内容
    private void SetTiTleText(NetworkManager.MYDATE mydate, View view) {
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

        for (int i = 0; i < dates.size(); i++) {
            sparseArray.get(i).setOnClickListener(new onClickListener(i));
        }
    }

    private boolean isOnclick=false;
    private class onClickListener implements View.OnClickListener {

        private int i = 0;

        public onClickListener(int i) {
            this.i = i;
        }

        @Override
        public void onClick(View v) {
            isOnclick=true;
            SetTextColorFaction(i);
            getinitLoadData(i);
        }
    }

    //设置日期背景
    private void SetTextColorFaction(int position) {
        this.selectPosition = position;
        adapter.setSelectDate(dates.get(position).date);
        for (int i = 0; i < sparseArray.size(); i++) {
            View view = sparseArray.get(i);
            if (view != null) {
                LinearLayout lll = (LinearLayout) view;
                if (lll == null) {
                    continue;
                }
                lll.setBackgroundResource(R.drawable.bg_btn_grey_price_date);
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
        ll.setBackgroundResource(R.drawable.bg_btn_red_price_date);
        TextView tv_number = (TextView) ll.findViewById(R.id.price_number_fragment_title);
        TextView tv_week_number = (TextView) ll.findViewById(R.id.price_week_fragment_title);
        tv_number.setTextColor(Color.parseColor("#ffffff"));
        tv_week_number.setTextColor(Color.parseColor("#ffffff"));
    }

    private void getinitLoadData(int position) {
        ArrayList<NetworkManager.CinemaRespModel> hashlist = hashMap.get(dates.get(position).date);
        if (hashlist != null && hashlist.size() > 0) {
            loadInit();
            intCurrentPage = 1;
            cinema_no_data_linear.setVisibility(View.GONE);
            cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
            adapter.appendAll(hashlist, true);
            list.addAll(hashlist);
            adapterWrapper.setFooterView(cnima_listview.getLoadMoreFooterController().getFooterView());
            endRefresh(true);
            adapter.setOnItemClickListener(new CinemaListAdapter.onItemClickListener() {
                @Override
                public void onItemClick() {
                    ConfigInfo.city = city;
                    ConfigInfo.region = region.equals("") ? "区域不限" : region;
                    LogUtils.e("city", " ConfigInfo.city " + city + "  region      " + region);
                }
            });
        } else {
            loadData(true, position);
        }
        getCinemaNumber(flimId, city, dates.get(position).date);
    }

    //得到区的电影票
    public void getCinemaNumber(String filmid, String city1, String date) {
        mNetworkManager.getCinemaNumbers(filmid, city1, date, new HttpRespCallback<NetworkManager.RespCinemaNumbers>() {
            @Override
            public void onRespFailure(int code, String msg) {
                ArrayList<NetworkManager.CinemaNumberModel> list = new ArrayList<NetworkManager.CinemaNumberModel>();

                for (int i = 0; i < contactList.size(); i++) {
                    if (contactList.get(i).getDisplayInfo().contains(city)) {
                        ArrayList<String> ss=(contactList.get(i).getDisplayList());
                        for (int j = 0; j < ss.size(); j++) {
                            NetworkManager.CinemaNumberModel ci=new NetworkManager.CinemaNumberModel();
                            ci.district=ss.get(j);
                            list.add(ci);
                        }
                    }
                }
                initRegionView(list);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespCinemaNumbers numList = (NetworkManager.RespCinemaNumbers) msg.obj;
                ArrayList<NetworkManager.CinemaNumberModel> list = numList.result;
                initRegionView(list);
            }
        });
    }
}
