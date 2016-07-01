package com.dym.film.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.ProgressWheel;
import com.dym.film.utils.AppUtils;
import com.dym.film.utils.ImageLoaderConfigUtil;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.StretchedListView;

import java.util.ArrayList;

/**
 * Created by xusoku on 2015/12/10.
 */
public class PriceFragmentAdapter extends BaseAdapter
{

    private Toast toast;
    private NetworkManager.MYDATE myData=null;
    private String cinemaId="";
    private String filmId="";
    private Context mActivity;

    private ArrayList<NetworkManager.TicketPriceModel> list;

    public PriceFragmentAdapter(Activity activity) {
        this.mActivity=activity;
        myData=new NetworkManager.MYDATE();
        list=new ArrayList<>();
    }

    public void setData(ArrayList<NetworkManager.TicketPriceModel> list){
        if(list!=null&&list.size()>0){
            this.list.clear();
            this.list.addAll(list);
        }
        else{

        }

        notifyDataSetChanged();

    }
    public void setMyData(NetworkManager.MYDATE myData) {
        this.myData = myData;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder1 item = null;
        if(convertView == null){
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.list_item_price_expandable, parent, false);
            item = new ViewHolder1(convertView);
            convertView.setTag(item);
        }else{
            item = (ViewHolder1) convertView.getTag();
        }
        final NetworkManager.TicketPriceModel model = list.get(position);

        if(!model.flag){
            item.price_expandable.setVisibility(View.GONE);
            item.price_expandable_image.setImageResource(R.drawable.price_expandable_close);
        }else{
            item.price_expandable.setVisibility(View.VISIBLE);
            item.price_expandable_image.setImageResource(R.drawable.price_expandable_open);
            initData(item.lst, position, model.startTime, item.price_expandable_slider_item_load,item.price_expandable_slider_item_name);
        }

        String startTime=model.startTime;
        if(TextUtils.isEmpty(startTime)){
            startTime="00:00";
        }
        String endTime=model.endTime;
        if(TextUtils.isEmpty(endTime)){
            endTime="00:00";
        }
        String remark=model.remark;
        if(TextUtils.isEmpty(remark)){
            remark="";
        }
        String language=model.language;
        if(TextUtils.isEmpty(language)){
            language="";
        }
        String price=model.price+"";
        if(TextUtils.isEmpty(price)){
            price="0";
        }



        item.price_expandable_tv_name.setText(startTime);
        item.price_expandable_tv_place.setText(endTime + "散场 / " + language +" / "+ remark);
        item.price_expandable_number.setText(price);


        item.price_expandable_toggle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (model.flag) {
                    model.flag = false;
                } else {
                    for (int i = 0; i < getCount(); i++) {
                        if (list.get(i).flag) {
                            list.get(i).flag = false;
                            notifyDataSetChanged();
                        }
                    }
                    list.get(position).flag = true;
                }
                notifyDataSetChanged();

            }
        });

        item.lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    activity.startActivity(new Intent(activity,PriceDetailActivity.class));
                if(tickets!=null&&!AppUtils.startAppByPackageName(mActivity, tickets.get(position).srcAndroidEntry)){
                    showTextToast("未安装"+tickets.get(position).source+"APP");
                }
            }
        });

        return convertView;
    }

    public  class  ViewHolder1{
        RelativeLayout price_expandable_toggle_button;
        RelativeLayout price_expandable;
        StretchedListView lst;
        TextView price_expandable_tv_name;
        TextView price_expandable_tv_place;
        TextView price_expandable_number;
        TextView price_expandable_slider_item_name;
        ProgressWheel price_expandable_slider_item_load;
        ImageView price_expandable_image;
        View view;


        public ViewHolder1(View itemView) {
            price_expandable_toggle_button = (RelativeLayout) itemView.findViewById(R.id.price_expandable_toggle_button);
            price_expandable = (RelativeLayout) itemView.findViewById(R.id.price_expandable);
             lst = (StretchedListView) itemView.findViewById(R.id.listview);
             price_expandable_slider_item_name = (TextView) itemView.findViewById(R.id.price_expandable_slider_item_name);
             price_expandable_slider_item_load = (ProgressWheel) itemView.findViewById(R.id.price_expandable_slider_item_load);
             view =LayoutInflater.from(mActivity).inflate(R.layout.list_item_price_slider_item, lst, false);
             price_expandable_tv_name= (TextView) itemView.findViewById(R.id.price_expandable_tv_name);
             price_expandable_tv_place= (TextView) itemView.findViewById(R.id.price_expandable_tv_place);
             price_expandable_number= (TextView) itemView.findViewById(R.id.price_expandable_number);
             price_expandable_image= (ImageView) itemView.findViewById(R.id.price_expandable_image);
            price_expandable_slider_item_name.setText("暂无数据");
        }
    }

    private ArrayList<NetworkManager.TicketPriceModel> tickets;
    private void initData(final StretchedListView lst, final int position,String startTime, final ProgressWheel price_expandable_slider_item_load,final TextView price_expandable_slider_item_name){

        price_expandable_slider_item_load.setVisibility(View.VISIBLE);
        NetworkManager.getInstance().getTicketPriceList(cinemaId, filmId,myData.date,startTime, new HttpRespCallback<NetworkManager.RespTicketPriceList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                 price_expandable_slider_item_load.setVisibility(View.GONE);
                    lst.setVisibility(View.GONE);
                 price_expandable_slider_item_name.setVisibility(View.VISIBLE);
                if (!NetWorkUtils.isAvailable(mActivity)) {
                    price_expandable_slider_item_name.setText("没有网络");
                } else {
                    price_expandable_slider_item_name.setText("暂无数据");
                }
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                price_expandable_slider_item_load.setVisibility(View.GONE);
                NetworkManager.RespTicketPriceList ms = (NetworkManager.RespTicketPriceList) msg.obj;
                if (ms != null && ms.tickets != null && ms.tickets.size() > 0) {
                    lst.setVisibility(View.VISIBLE);
                    price_expandable_slider_item_name.setVisibility(View.GONE);
                    tickets=ms.tickets;
                    lst.setAdapter(new CommonBaseAdapter(mActivity, ms.tickets, R.layout.list_item_price_slider_item) {

                        @Override
                        public void convert(ViewHolder holder, Object itemData1, int position) {
                            NetworkManager.TicketPriceModel itemData= (NetworkManager.TicketPriceModel) itemData1;
                            holder.setText(R.id.price_slider_item_name,itemData.source);
                            holder.setText(R.id.price_slider_item_number, itemData.price + "");
//                            holder.setImageByUrl(R.id.price_slider_item_iv, itemData.srcLogo);
                            ImageLoaderConfigUtil.setRouteDisplayImager(R.drawable.ic_default_loading_img,(ImageView)holder.getView(R.id.price_slider_item_iv),itemData.srcLogo,true);
                        }
                    });
                } else {
                    lst.setVisibility(View.GONE);
                    price_expandable_slider_item_name.setVisibility(View.VISIBLE);
                    if (!NetWorkUtils.isAvailable(mActivity)) {
                        price_expandable_slider_item_name.setText("没有网络");
                    } else {
                        price_expandable_slider_item_name.setText("暂无数据");
                    }

                }

            }
        });
    }

    private void showTextToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
