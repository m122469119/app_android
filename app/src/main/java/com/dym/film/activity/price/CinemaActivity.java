package com.dym.film.activity.price;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
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
import com.dym.film.entity.cinema;
import com.dym.film.manager.BaiduLBSManager;
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
import java.util.List;
@Deprecated
public class CinemaActivity extends BaseActivity {

    private LoadMoreRecyclerView cnima_listview;
    private  ExRcvAdapterWrapper adapterWrapper;
    private  LinearLayoutManager linearLayoutManager;
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
    private String city="";
    private String region="";
    private String lng="";
    private String lat="";
    private cinema cinemas;
    private boolean isCityloaded=false;

    private TextView cinema_city_locate_tv;//定位按钮
    private TextView cinema_tv_region_item;//不限按钮

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        settranslucentStatusBar(R.color.main_title_color);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_cinema;
    }

    @Override
    protected void initVariable() {
        city=BaiduLBSManager.getInstance().getCity();
        region=BaiduLBSManager.getInstance().getDistrict();
        SharedPreferences preferences = getSharedPreferences(BaiduLBSManager.PREF_LBS_NAME, Context.MODE_PRIVATE);
        lng=preferences.getString(BaiduLBSManager.KEY_LONGITUDE, "");
        lat=preferences.getString(BaiduLBSManager.KEY_LATITUDE, "");
        if (TextUtils.isEmpty(city)){
            city="城市";
        }
        if(TextUtils.isEmpty(region)){
            region="地区";
        }
        if (TextUtils.isEmpty(lng)){
            lng="";
        }
        if(TextUtils.isEmpty(lat)){
            lat="";
        }

        if(!TextUtils.isEmpty(ConfigInfo.city)){
            city=ConfigInfo.city;
        }
        if(!TextUtils.isEmpty(ConfigInfo.region)){
            region=ConfigInfo.region;
        }
        cinemas= (cinema) getIntent().getSerializableExtra(PriceActivity.KEY);
    }

    @Override
    protected void findViews() {
        cnima_listview=$(R.id.cnima_listview);
        cinema_no_data_linear=$(R.id.cinema_no_data_linear);
        layout_no_data_iv=$(R.id.layout_no_data_iv);
        layout_no_data_tv=$(R.id.layout_no_data_tv);
        cinema_city_relative=$(R.id.cinema_city_relative);
        cinema_city_region=$(R.id.cinema_city_region);
        cinema_expandable_image_city=$(R.id.cinema_expandable_image_city);
        cinema_expandable_image_region=$(R.id.cinema_expandable_image_region);
        listview_header=getLayoutInflater().inflate(R.layout.cinema_layout_listtop, null);
        cinema_text_city=$(R.id.cinema_text_city);
        cinema_text_region=$(R.id.cinema_text_region);
        progress_dialog=$(R.id.progress_dialog);
        loadInit();
        layout_no_data_iv.setImageResource(R.drawable.no_cinema_image);
        layout_no_data_tv.setText("暂无影院");
    }


    private void loadInit() {
        linearLayoutManager=new LinearLayoutManager(this);
        cnima_listview.setLayoutManager(linearLayoutManager);
        cnima_listview.setLinearLayoutManager(linearLayoutManager);
        // 设置ItemAnimator
        cnima_listview.setItemAnimator(new DefaultItemAnimator());
        adapter=new CinemaListAdapter(this);
        adapterWrapper = new ExRcvAdapterWrapper<>(adapter, linearLayoutManager);
//        adapterWrapper.setHeaderView(listview_header);
        cnima_listview.setAdapter(adapterWrapper);
        // 设置固定大小
        cnima_listview.setHasFixedSize(true);

    }


    @Override
    protected void initData() {

        list=new ArrayList<NetworkManager.CinemaRespModel>();
        cinema_text_city.setText(city);
        cinema_text_region.setText(region);
        initPopupRegionWindow();
        initPopupCityWindow();
        initCinemaData();
    }

    private void initCinemaData() {

        loadData(true);
    }

    private void loadData(final boolean isFirst) {

        if(isFirst){

            intCurrentPage = 0;
            list.clear();
            loadInit();
            // 可以显示加载界面
            progress_dialog.setVisibility(View.VISIBLE);
            cinema_no_data_linear.setVisibility(View.GONE);
        }

        if(region.equals("不限")||region.equals("地区")){
            region="";
        }
        if(city.equals("城市")){
            city="北京市";
        }
        mNetworkManager.getCinemaList(city, region, lng, lat,intCurrentPage++,PAGE_SIZE, new HttpRespCallback<NetworkManager.RespCinemaList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
            }
            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.RespCinemaList ms = (NetworkManager.RespCinemaList) msg.obj;
                if (ms != null && ms.cinemas != null && ms.cinemas.size() > 0) {
                    int size = (ms.cinemas == null ? 0 : ms.cinemas.size());
                    if (size < PAGE_SIZE) {
                        cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    } else {
                        cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                    adapter.appendAll(ms.cinemas, true);
                    list.addAll(ms.cinemas);
                    adapterWrapper.setFooterView(cnima_listview.getLoadMoreFooterController().getFooterView());
                    endRefresh(true);
                    adapter.setOnItemClickListener(new CinemaListAdapter.onItemClickListener() {
                        @Override
                        public void onItemClick() {
                            ConfigInfo.city = city;
                            ConfigInfo.region = region.equals("")?"不限":region;
                            LogUtils.e("city", " ConfigInfo.city " + city + "  region      " + region);
                        }
                    });
                } else {
                    cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    if(list.size()>0){
//                        Toast.makeText(CinemaActivity.this, "加载完毕", Toast.LENGTH_LONG).show();
                    }else{
                        cinema_no_data_linear.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
    }

    private void endRefresh(boolean result)
    {
        isNextPageLoading = false;
        if (result) {
            cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
        }
        else {
            cnima_listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            if(!NetWorkUtils.isAvailable(this)){
                cinema_no_data_linear.setVisibility(View.VISIBLE);
            }
            cnima_listview.setFooterClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(!NetWorkUtils.isAvailable(CinemaActivity.this)){
                        cinema_no_data_linear.setVisibility(View.VISIBLE);
                        return;
                    }
                    cnima_listview.startLoadMore();
                }
            });
        }
    }


    private  void loadMore()
    {
        if (isNextPageLoading) {
            return;
        }
        isNextPageLoading = true;
        loadData(false);
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

        switch (view.getId()){
            case R.id.cinema_city_region:
                if(isCityloaded){
                    Toast.makeText(this, "暂无数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                regionpopupWindow.showAsDropDown(cinema_city_relative);
                cinema_expandable_image_region.setImageResource(R.drawable.price_expandable_open);
                break;

            case R.id.cinema_city_relative:
                if(isCityloaded){
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


        ACache aCache=ACache.get(CinemaActivity.this);
        NetworkManager.RespCityList cityList= (NetworkManager.RespCityList) aCache.getAsObject("city");
        String isUpdataCity=aCache.getAsString("isUpdataCity");
        if(!TextUtils.isEmpty(isUpdataCity)&&isUpdataCity.equals("1")){
            initDataCity();
        }else{
            if(cityList!=null&&cityList.cities!=null&&!cityList.cities.isEmpty()){
                initCityView(cityList);
            }else{
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
    private void  initCityView(NetworkManager.RespCityList cityList){
        contactList = CityData.getSampleContactList(cityList.cities);
        if(contactList==null||contactList.size()==0){
            isCityloaded=true;
        }else{
            if(TextUtils.isEmpty(city)){
                city="北京市";
                region="不限";
                cinema_text_city.setText(city);
                cinema_text_region.setText(region);
            }
        }
        CityAdapter adapter = new CityAdapter(this,R.layout.city_item, contactList);
        city_listview= (ContactListViewImpl) cityView.findViewById(R.id.city_listview);
        cinema_city_locate_tv= (TextView) cityView.findViewById(R.id.cinema_city_locate_tv);
        cityView.findViewById(R.id.local_linear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = cinema_city_locate_tv.getText().toString().trim();
                if (TextUtils.isEmpty(str)) {
                    return;
                }
                city = BaiduLBSManager.getInstance().getCity();
                region = BaiduLBSManager.getInstance().getDistrict();
                cinema_text_city.setText(city);
                cinema_text_region.setText(region);
                citypopupWindow.dismiss();
                initCinemaData();
                initRegionView();
            }
        });
        city_listview.setFastScrollEnabled(true);
        city_listview.setAdapter(adapter);
        city_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position,
                                    long id) {

                city = contactList.get(position).getDisplayInfo();
                region = "不限";
                cinema_text_region.setText(region);
                cinema_text_city.setText(city);
                initRegionView();
                citypopupWindow.dismiss();
                initCinemaData();
            }
        });
        String cityy=BaiduLBSManager.getInstance().getCity();
        if(TextUtils.isEmpty(cityy)){
            cityy="城市";
        }
        cinema_city_locate_tv.setText(cityy);
        initRegionView();
    }

    private void initRegionView(){
        regionGradView = (GridView) regionView.findViewById(R.id.cinema_girdview);
        cinema_tv_region_item = (TextView) regionView.findViewById(R.id.cinema_tv_region_item);
        cinema_tv_region_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                region = "不限";
                cinema_text_region.setText(region);
                regionpopupWindow.dismiss();
                initCinemaData();
            }
        });
        final ArrayList<String> str=new ArrayList<String>();
        for (int i=0;i<contactList.size();i++){
            if(contactList.get(i).getDisplayInfo().contains(city)) {
                str.addAll(contactList.get(i).getDisplayList());
            }
        }
        regionGradView.setAdapter(new CommonBaseAdapter<String>(this,str,R.layout.cinema_layout_region_item) {
            @Override
            public void convert(ViewHolder holder, String itemData,int position) {
                holder.setText(R.id.cinema_tv_region_item,itemData);
            }
        });
        regionGradView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position,
                                    long id) {

                region = str.get(position);
                cinema_text_region.setText(region);
                regionpopupWindow.dismiss();
                initCinemaData();
            }
        });
    }

    private void initDataCity(){
        mNetworkManager.getCityList(new HttpRespCallback<NetworkManager.RespCityList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                isCityloaded = true;
                cinema_text_city.setText(city);
                cinema_text_region.setText(region);
            }
            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                isCityloaded = false;
                NetworkManager.RespCityList cityList = (NetworkManager.RespCityList) msg.obj;
                ACache aCache=ACache.get(CinemaActivity.this);
                aCache.put("city",cityList);
                initCityView(cityList);
            }
        });
    }
}
