package com.dym.film.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dym.film.R;
import com.dym.film.activity.price.CinemaActivityNew;
import com.dym.film.activity.price.PriceActivityNew;
import com.dym.film.activity.price.PriceActivity_New;
import com.dym.film.entity.DaoSession;
import com.dym.film.entity.cinema;
import com.dym.film.entity.cinemaDao;
import com.dym.film.fragment.PriceFragment;
import com.dym.film.fragment.PriceFragment_New;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.utils.LogUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by xusoku on 2015/12/1.
 */
public class CinemaListAdapter extends SimpleRecyclerAdapter<NetworkManager.CinemaRespModel> {

    private boolean flag=false;
    private String filmId="";
    private String selectDate="";

    public CinemaListAdapter(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType) {
        View itemLayout = mLayoutInflater
                .inflate(R.layout.cinema_layout_item, parent,false);
        return itemLayout;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    public void setSelectDate(String selectDate) {
        this.selectDate = selectDate;
    }

    @Override
    public void onBindModelToView(SimpleViewHolder holder, final int position) {


        TextView cinema_tv_name;
        TextView cinema_tv_place;
        TextView cinema_expandable_number;
        TextView cinema_expandable_buy_number;
        TextView cinema_expandable_tag;
        TextView cinema_expandable_tag1;


        cinema_tv_name = (TextView) holder.findView(R.id.cinema_tv_name);
        cinema_tv_place = (TextView) holder.findView(R.id.cinema_tv_place);
        cinema_expandable_number = (TextView) holder.findView(R.id.cinema_expandable_number);
        cinema_expandable_buy_number = (TextView) holder.findView(R.id.cinema_expandable_buy_number);
        cinema_expandable_tag = (TextView) holder.findView(R.id.cinema_expandable_tag);
        cinema_expandable_tag1 = (TextView) holder.findView(R.id.cinema_expandable_tag1);


        final NetworkManager.CinemaRespModel model=getItem(position);

        String name=model.name;
        String address=model.address;
        String tel=model.tel;
        String minPrice=model.minPrice;
        String showingThisFilm=model.showingThisFilm;

        if(TextUtils.isEmpty(name)){
            name="暂无";
        }
        if(TextUtils.isEmpty(address)){
            address="暂无";
        }
        if(TextUtils.isEmpty(minPrice)){
            minPrice="0";
        }

        if(TextUtils.isEmpty(showingThisFilm)){
            minPrice="0";
        }



        cinema_tv_name.setText(name);
        cinema_tv_place.setText(address);
        cinema_expandable_number.setText(minPrice);

        if(flag){
            cinema_expandable_tag1.setVisibility(View.INVISIBLE);
            cinema_expandable_tag.setVisibility(View.INVISIBLE);
            cinema_expandable_number.setVisibility(View.GONE);
        }else{
            cinema_expandable_tag1.setVisibility(View.VISIBLE);
            cinema_expandable_tag.setVisibility(View.VISIBLE);
            cinema_expandable_number.setVisibility(View.VISIBLE);
        }

        if(showingThisFilm.equals("0")){
            cinema_expandable_tag1.setText("暂无排片");
            cinema_expandable_tag.setVisibility(View.INVISIBLE);
            cinema_expandable_number.setVisibility(View.GONE);
        }else if(showingThisFilm.equals("1")){
            cinema_expandable_tag1.setText("起");
            cinema_expandable_tag.setVisibility(View.VISIBLE);
            cinema_expandable_number.setVisibility(View.VISIBLE);
        }



        try {

            double lat1=Double.parseDouble(model.latitude);
            double lng1=Double.parseDouble(model.longitude);
            LatLng p1 = new LatLng(lat1, lng1);
            SharedPreferences preferences = mActivity.getSharedPreferences(BaiduLBSManager.PREF_LBS_NAME, Context.MODE_PRIVATE);
            String lat = preferences.getString(BaiduLBSManager.KEY_LATITUDE, "");
            String lng = preferences.getString(BaiduLBSManager.KEY_LONGITUDE, "");
            double distance = DistanceUtil.getDistance(p1, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            distance/=1000;
            if(distance>100){
                cinema_expandable_buy_number.setText(">100km");
            }else {
                cinema_expandable_buy_number.setText(String.format("%.1f", distance) + "km");
            }
        }catch (Exception e){}



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(listener!=null){
                    LogUtils.e("setOnClickListener","setOnClickListener");
                    listener.onItemClick();
                }

                DaoSession daoSession = DatabaseManager.getInstance().getDaoSession();
                cinemaDao dao = daoSession.getCinemaDao();
                cinema c=new cinema();
                c.setCinemaID(model.cinemaID);
                c.setName(model.name);
                c.setAddress(model.address);
                c.setMinPrice(model.minPrice);
                c.setTel(model.tel);
                c.setLng(model.longitude);
                c.setLat(model.latitude);
                dao.saveCiname(dao, c);
                EventBus.getDefault().post(selectDate);
                if(flag){
                    Intent intent = new Intent();
                    intent.putExtra(PriceFragment_New.KEY, c);
                    intent.putExtra(CinemaActivityNew.SDATE,selectDate);
                    mActivity.setResult(PriceFragment_New.ResultCode, intent);
                    mActivity.finish();
                }else{
                    Intent intent = new Intent(mActivity, PriceActivity_New.class);
                    intent.putExtra(PriceFragment_New.KEY, c);
                    intent.putExtra(CinemaActivityNew.SDATE,selectDate);
                    intent.putExtra(PriceActivityNew.FILMID, filmId);
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }


            }
        });
    }

    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public interface onItemClickListener{
        public void onItemClick();
    }

    private onItemClickListener listener;

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener=listener;
    }

}
