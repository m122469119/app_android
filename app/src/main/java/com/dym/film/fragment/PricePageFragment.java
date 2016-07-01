package com.dym.film.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.adapter.PriceRecyclerAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.views.AttachUtil;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import okhttp3.Call;

/**
 * Created by xusoku on 2015/11/25.
 */
@Deprecated
public class PricePageFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String CINEMAID = "cinemaid";
    public static final String FILMID = "filmid";
    private NetworkManager.MYDATE mPage;
    private  String cinemaId="";
    private  String filmId="";
    private RelativeLayout price_fragment_loading;
    private LinearLayout price_no_data_linear;
    private ImageView layout_no_data_iv;
    private TextView layout_no_data_tv;

    private LoadMoreRecyclerView listView;
    ExRcvAdapterWrapper adapterWrapper;
    private PriceRecyclerAdapter adapter;
    private ArrayList<NetworkManager.TicketPriceModel> tickets;
    private NetworkManager networkManager=NetworkManager.getInstance();

    public static PricePageFragment newInstance(NetworkManager.MYDATE page,String cinemaId,String filmId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PAGE, page);
        args.putString(CINEMAID, cinemaId);
        args.putString(FILMID, filmId);
        PricePageFragment pageFragment = new PricePageFragment();
        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = (NetworkManager.MYDATE) getArguments().getSerializable(ARG_PAGE);
        cinemaId=getArguments().getString(CINEMAID);
        filmId=getArguments().getString(FILMID);
        tickets=new ArrayList<>();
    }


    private void initView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(linearLayoutManager);
        listView.setLinearLayoutManager(linearLayoutManager);
        adapter=new PriceRecyclerAdapter(getActivity());
        adapter.setMyData(mPage.date);
        adapter.setCinemaId(cinemaId);
        adapter.setFilmId(filmId);
        adapterWrapper = new ExRcvAdapterWrapper<>(adapter, linearLayoutManager);
        listView.setAdapter(adapterWrapper);
        // 设置固定大小
        listView.setHasFixedSize(true);
    }

    public void initOnResume(){
        if(adapter!=null&&listView!=null) {
            listView.scrollToPosition(0);
            EventBus.getDefault().post(true);
        }
    }


//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(isVisibleToUser){
//            initOnResume();
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();
        MatStatsUtil.onPause(getActivity(), getClass().getSimpleName());
    }

    @Override
    public void onStop() {
        super.onStop();
        MatStatsUtil.onStop(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_price, container, false);
        listView= (LoadMoreRecyclerView) view.findViewById(R.id.price_stretch_listview);
        price_fragment_loading= (RelativeLayout) view.findViewById(R.id.price_fragment_loading);
        price_no_data_linear= (LinearLayout) view.findViewById(R.id.price_no_data_linear);
        layout_no_data_iv= (ImageView) view.findViewById(R.id.layout_no_data_iv);
        layout_no_data_tv= (TextView) view.findViewById(R.id.layout_no_data_tv);
        layout_no_data_iv.setImageResource(R.drawable.no_price_image);
        layout_no_data_tv.setText("暂无场次");
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                EventBus.getDefault().post(AttachUtil.isRecyclerViewAttach(recyclerView));
            }
        });
    }



    private Call call;
    private void initData(){
        price_fragment_loading.setVisibility(View.VISIBLE);
       call= networkManager.getHotTicketPriceList(cinemaId, filmId, mPage.date, new HttpRespCallback<NetworkManager.RespTicketPriceList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                price_fragment_loading.setVisibility(View.GONE);
//                if (getActivity()!=null&&!NetWorkUtils.isAvailable(getActivity())) {
//                    Snackbar.make(price_fragment_loading, "没有网络", Snackbar.LENGTH_LONG).show();
//                } else {
//                    Snackbar.make(price_fragment_loading, "暂无数据", Snackbar.LENGTH_LONG).show();
//                }
                price_no_data_linear.setVisibility(View.VISIBLE);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                price_fragment_loading.setVisibility(View.GONE);
                NetworkManager.RespTicketPriceList ms = (NetworkManager.RespTicketPriceList) msg.obj;
                if (ms != null && ms.tickets!= null && ms.tickets.size() > 0) {
                    tickets.addAll(ms.tickets);
                    adapter.setAll(ms.tickets);
                } else {
//                    if (getActivity()!=null&&!NetWorkUtils.isAvailable(getActivity())) {
//                        Snackbar.make(price_fragment_loading, "没有网络", Snackbar.LENGTH_LONG).show();
//                    } else {
//                        Snackbar.make(price_fragment_loading, "暂无数据", Snackbar.LENGTH_LONG).show();
//                    }
                    price_no_data_linear.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(call!=null){
            call.cancel();
        }
    }
}
