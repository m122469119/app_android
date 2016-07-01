package com.dym.film.activity.price;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.cinema;
import com.dym.film.fragment.PricePageFragment;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.EffectAnim;
import com.dym.film.utils.ImageLoaderConfigUtil;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.DragTopLayout;
import com.dym.film.views.FlingOneGallery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * 比价
 */

/**
 * Author: xusoku ( xu )
 * Date: 2015/11/25
 */
@Deprecated
public class PriceActivity extends BaseActivity {

    public static String KEY = "key";
    public static String NUM = "num";
    public static String PRICE = "price";
    public static String CINEMAID = "cinimaId";
    public static String FILMID = "filmId";
    public static int ResultCode = 0x9;
    private cinema cinemas;
    private FlingOneGallery gallery;
    EffectAnim amin;
    private View preView;
    private ImageView image_big_bg;
    private LinearLayout price_no_data_linear1;
    private ImageView layout_no_data_iv;
    private TextView layout_no_data_tv;
    ArrayList<String> str;


    private DragTopLayout price_drag_layout;
    private ViewPager viewPager;
    private TabLayout tabLayout;


    private LinearLayout price_liner_price;
    private LinearLayout price_liner_place;

    private TextView cinema_tv_name;
    private TextView cinema_tv_place;
    private TextView price_buy_number;
    private TextView price_title_name_title;
    private TextView price_number;
    private TextView price_tag;
    private View activity_price_view;

    private NetworkManager networkManager = NetworkManager.getInstance();

    NetworkManager.RespCinemaFilmList ms;
    private String filmId = "";

    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTranslucentStatusBar(R.color.black);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_price;
    }

    @Override
    protected void initVariable() {
        amin = new EffectAnim();
        cinemas = (cinema) getIntent().getSerializableExtra(KEY);
        filmId =  getIntent().getStringExtra(FILMID);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        cinemas = (cinema) getIntent().getSerializableExtra(KEY);
        initData();
    }

    @Override
    protected void findViews() {
        gallery = $(R.id.price_gallery);
        image_big_bg = $(R.id.price_image_big_bg);
        price_title_name_title = $(R.id.price_title_name_title);
        price_no_data_linear1 = $(R.id.price_no_data_linear1);
        viewPager = $(R.id.price_viewPager);
        tabLayout = $(R.id.price_tabs);
        price_liner_price = $(R.id.price_liner_price);
        price_liner_place = $(R.id.price_liner_place);
        price_drag_layout = $(R.id.price_drag_layout);
        cinema_tv_name = $(R.id.price_tv_name);
        cinema_tv_place = $(R.id.price_tv_place);
        price_number = $(R.id.price_number);
        price_tag = $(R.id.price_tag);
        price_buy_number = $(R.id.price_buy_number);
        layout_no_data_iv = $(R.id.layout_no_data_iv);
        layout_no_data_tv = $(R.id.layout_no_data_tv);
        activity_price_view = $(R.id.activity_price_view);
        layout_no_data_iv.setImageResource(R.drawable.no_price_image);
        layout_no_data_tv.setText("暂无场次");
    }

    @Override
    protected void initData() {
        init();
        cinema_tv_name.setText(cinemas.getName());
        cinema_tv_place.setText(cinemas.getAddress());
        networkManager.getCinemaFilmList(cinemas.getCinemaID(), CommonManager.getToady(0).date, new HttpRespCallback<NetworkManager.RespCinemaFilmList>() {
            @Override
            public void onRespFailure(int code, String msg) {
//                if (!NetWorkUtils.isAvailable(PriceActivity.this)) {
//                    Snackbar.make(cinema_tv_name, "没有网络", Snackbar.LENGTH_LONG).show();
//                } else {
//                    Snackbar.make(cinema_tv_name, "暂无数据", Snackbar.LENGTH_LONG).show();
//                }
                price_no_data_linear1.setVisibility(View.VISIBLE);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                ms = (NetworkManager.RespCinemaFilmList) msg.obj;
                if (ms != null && ms.films != null) {
                    price_buy_number.setText(ms.films.sum.equals("0") ? "" : ms.films.sum + "人购买");
                }
                if (ms != null && ms.films != null && ms.films.list != null && ms.films.list.size() > 0) {
                    price_buy_number.setText(ms.films.sum.equals("0") ? "" : ms.films.sum + "人购买");
                    gallery.setAdapter(new CommonBaseAdapter<NetworkManager.CinemaFilmModel>(PriceActivity.this, ms.films.list, R.layout.gallery_image_item) {
                        @Override
                        public void convert(ViewHolder holder, NetworkManager.CinemaFilmModel itemData, int position) {
                            ImageView iv = (ImageView) holder.getView(R.id.price_pre_image_item);
                            ImageLoaderConfigUtil.setDisplayImager(R.drawable.ic_default_loading_img, iv, itemData.post, true);
                        }
                    });
                    if(!TextUtils.isEmpty(filmId)){
                        for (int i = 0; i < ms.films.list.size(); i++) {
                            if(filmId.equals(ms.films.list.get(i).filmID)){
                                gallery.setSelection(i);
                                return;
                            }
                        }
                        Toast.makeText(PriceActivity.this,"影院暂无该影片",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(PriceActivity.this,"影院暂无该影片",Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    if (!NetWorkUtils.isAvailable(PriceActivity.this)) {
//                        Snackbar.make(cinema_tv_name, "没有网络", Snackbar.LENGTH_LONG).show();
//                    } else {
//                        Snackbar.make(cinema_tv_name, "暂无数据", Snackbar.LENGTH_LONG).show();
//                    }
                    Toast.makeText(PriceActivity.this,"影院暂无该影片",Toast.LENGTH_SHORT).show();
                    price_no_data_linear1.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void InitViewPager() {
        ArrayList<NetworkManager.MYDATE> str1 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            str1.add(CommonManager.getToady(i));
        }
        viewPager = $(R.id.price_viewPager);
        tabLayout = $(R.id.price_tabs);
        tabLayout.setOnTabSelectedListener(null);
        viewPager.setAdapter(null);
        final MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), str1);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4);


        if (str1 != null && str1.size() > 4) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab.getTag() == null) {
                tab.setTag(pagerAdapter.getTabView(i));
                tab.setCustomView((LinearLayout)tab.getTag());
            }
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                SetTextColorFaction(tab);
                int count=tab.getPosition();
                viewPager.setCurrentItem(count);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                PricePageFragment fragment= (PricePageFragment) pagerAdapter.getFragment(position);
                fragment.initOnResume();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void SetTextColorFaction(TabLayout.Tab tab1) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                LinearLayout lll = (LinearLayout) tab.getTag();
                if (lll == null) {
                    continue;
                }
//                lll.setBackgroundResource(R.);
                TextView tv_number = (TextView) lll.findViewById(R.id.price_number_fragment_title);
                TextView tv_week_number = (TextView) lll.findViewById(R.id.price_week_fragment_title);
                tv_number.setTextColor(Color.parseColor("#b6b6b6"));
                tv_week_number.setTextColor(Color.parseColor("#b6b6b6"));
            }
        }
        LinearLayout ll = (LinearLayout) tab1.getTag();
        if (ll == null) {
            return;
        }
        ll.setBackgroundResource(R.color.red_color);
        TextView tv_number = (TextView) ll.findViewById(R.id.price_number_fragment_title);
        TextView tv_week_number = (TextView) ll.findViewById(R.id.price_week_fragment_title);
        tv_number.setTextColor(Color.parseColor("#ffffff"));
        tv_week_number.setTextColor(Color.parseColor("#ffffff"));
    }

//    public Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            InitViewPager();
//        }
//    };

    public static class MyHandler extends Handler{
        private final WeakReference<PriceActivity> mActivity;

        public MyHandler(PriceActivity activity) {
            mActivity = new WeakReference<PriceActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PriceActivity activity = mActivity.get();
            if (activity != null) {
                activity.InitViewPager();
            }
        }
    }

    public final MyHandler handler = new MyHandler(this);


    @Override
    protected void setListener() {

        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int arg2, long arg3) {
                if (preView != null) {
                    amin.showLooseFocusAinimation(preView);
                    ImageView iv = (ImageView) preView.findViewById(R.id.price_pre_image_item);
                    iv.setBackgroundResource(R.color.main_bg_color);
                }
                // 放大
                if (view != null) {
                    amin.showOnFocusAnimation(false, view);
                    if (ms.films.list.size() > 0) {
                        filmId = ms.films.list.get(arg2).filmID;
                        price_title_name_title.setText(ms.films.list.get(arg2).name);
                        price_tag.setVisibility(View.VISIBLE);
                        String price=ms.films.list.get(arg2).price;
                        if(TextUtils.isEmpty(price)||price.equals("0.0")||price.equals("0")){
                            price="无券";
                            price_tag.setVisibility(View.GONE);
                        }
                        price_number.setText(price);
                        ImageLoaderConfigUtil.setBlurImager(PriceActivity.this,
                                R.drawable.ic_loading,
                                image_big_bg,
                                ms.films.list.get(arg2).post,
                                true);
                        if (!TextUtils.isEmpty(ms.films.list.get(arg2).post)) {
                            ImageView iv = (ImageView) view.findViewById(R.id.price_pre_image_item);
                            iv.setBackgroundResource(R.color.red_bg_color);
                        }
                        handler.removeMessages(1);
                        handler.sendEmptyMessageDelayed(1, 1000);
                    }
                }
                preView = view;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        //禁止下拉放大效果
        price_drag_layout.setOverDrag(false);
        //设置头部View可上拉显示ContentView
        price_drag_layout.setCollapseOffset(1);

        price_drag_layout.listener(new DragTopLayout.PanelListener() {
            @Override
            public void onPanelStateChanged(DragTopLayout.PanelState panelState) {
            }
            @Override
            public void onSliding(float ratio) {
                if(ratio==1.0){
                    activity_price_view.setVisibility(View.VISIBLE);
                }else if(ratio==0.0){
                    activity_price_view.setVisibility(View.GONE);
                }
            }
            @Override
            public void onRefresh() {
            }
        });
    }

    @Override
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.price_liner_price:
                MatStatsUtil.eventClick(this, "huo_portal", "huo_portal");
                if (TextUtils.isEmpty(filmId)) {
                    Toast.makeText(this, "暂无影片", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(cinemas.getCinemaID())) {
                    Toast.makeText(this, "暂无影院", Toast.LENGTH_SHORT).show();
                    return;
                }
                String str = price_number.getText().toString().trim();
                if (TextUtils.isEmpty(str)||str.equals("0.0")||str.equals("0")||str.equals("无券")) {
                    showTextToast("暂无兑换券");
                    return;
                }
                Intent it1 = new Intent(this, VoucherActivity.class);
                it1.putExtra(CINEMAID, cinemas.getCinemaID());
                it1.putExtra(FILMID, filmId);
                it1.putExtra(NUM, price_buy_number.getText().toString().trim());
                it1.putExtra(PRICE, str);
                startActivity(it1);
                break;
            case R.id.price_liner_place:
                Intent it = new Intent(this, CinemaActivity.class);
                it.putExtra(KEY, cinemas);
                startActivityForResult(it, 12);
                break;
            case R.id.price_iv_back:
                finish();
                break;
        }
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<NetworkManager.MYDATE> titlelist;
        FragmentManager fm;

        public MyPagerAdapter(FragmentManager fm, ArrayList<NetworkManager.MYDATE> titlelist) {
            super(fm);
            this.fm = fm;
            if (titlelist == null) {
                this.titlelist = new ArrayList<NetworkManager.MYDATE>();
            } else {
                this.titlelist = titlelist;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return titlelist.size();
        }

        public View getTabView(int position) {
            View tab_title_layout = getLayoutInflater().inflate(R.layout.list_item_tab_title_layout, tabLayout, false);
            TextView tv_number = (TextView) tab_title_layout.findViewById(R.id.price_number_fragment_title);
            TextView tv_week_number = (TextView) tab_title_layout.findViewById(R.id.price_week_fragment_title);
            tv_number.setText(titlelist.get(position).day);
            tv_week_number.setText(titlelist.get(position).week);
            if (position == 0) {
                tab_title_layout.setBackgroundResource(R.color.red_color);
                tv_number.setTextColor(Color.parseColor("#ffffff"));
                tv_week_number.setTextColor(Color.parseColor("#ffffff"));
            }
            return tab_title_layout;
        }

        @Override
        public Fragment getItem(int position) {
            return new PricePageFragment().newInstance(titlelist.get(position), cinemas.getCinemaID(), filmId);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //得到缓存的fragment
                     Fragment fragment = (Fragment) super.instantiateItem(container,
                     position);
            //得到tag，这点很重要
                     String fragmentTag = fragment.getTag();
                //如果这个fragment需要更新
                FragmentTransaction ft = fm.beginTransaction();
                //移除旧的fragment
                ft.remove(fragment);
                //换成新的fragment
                fragment = getItem(position);
                //添加新fragment时必须用前面获得的tag，这点很重要
                ft.add(container.getId(), fragment, String.valueOf(position));
                ft.attach(fragment);
                ft.commitAllowingStateLoss();
            return fragment;
        }

        public Fragment getFragment(int position){
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment =fm.findFragmentByTag(String.valueOf(position));
            return  fragment;
        }

    }


    // Handle scroll event from fragments
    public void onEvent(Boolean b) {
        price_drag_layout.setTouchMode(b);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == ResultCode) {
            cinema ci = (cinema) data.getSerializableExtra(KEY);
            if (!ci.getCinemaID().equals(cinemas.getCinemaID())) {
                cinemas = ci;
                initData();
            }
        }
    }

    public void init() {
        price_no_data_linear1.setVisibility(View.GONE);
        viewPager.removeAllViews();
        image_big_bg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image_big_bg.setImageResource(R.drawable.ic_default_loading_img);
        ms = new NetworkManager.RespCinemaFilmList();
        price_title_name_title.setText("");
        price_number.setText("");
        price_buy_number.setText("");
        ArrayList<NetworkManager.CinemaFilmModel> cinemaFilmModels = new ArrayList<>();
//        NetworkManager.CinemaFilmModel c=new NetworkManager.CinemaFilmModel();
//        c.post="http://img1d.xgo-img.com.cn/pics/1538/a1537504.jpg";
//        cinemaFilmModels.add(c);
//        c.post="http://pic22.nipic.com/20120720/9397469_164631416100_2.jpg";
//        cinemaFilmModels.add(c);
//        c.post="http://image.tianjimedia.com/uploadImages/2012/233/26/903AG1GR4Q35.jpg";
//        cinemaFilmModels.add(c);
//        ms.films.list.addAll(cinemaFilmModels);
        gallery.setAdapter(new CommonBaseAdapter<NetworkManager.CinemaFilmModel>(PriceActivity.this, ms.films.list, R.layout.gallery_image_item) {
            @Override
            public void convert(ViewHolder holder, NetworkManager.CinemaFilmModel itemData, int position) {
                ImageView iv = (ImageView) holder.getView(R.id.price_pre_image_item);
                CommonManager.displayImage(itemData.post, iv, R.drawable.ic_default_loading_img);
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeMessages(1);
        }
    }
}
