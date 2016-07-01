package com.dym.film.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.adapter.base.BaseSimpleRecyclerAdapter;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.ui.ProgressWheel;
import com.dym.film.utils.AppUtils;
import com.dym.film.utils.EffectAnim;
import com.dym.film.utils.ImageLoaderConfigUtil;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.StretchedListView;

import java.util.ArrayList;

/**
 * Created by xusoku on 2015/12/10.
 */
public class PriceRecyclerAdapter extends BaseSimpleRecyclerAdapter<NetworkManager.TicketPriceModel,PriceRecyclerAdapter.PriceViewHodler>
{

    private Toast toast;
    private String myData=null;
    private String cinemaId="";
    private String filmId="";

    public PriceRecyclerAdapter(Activity activity) {
        super(activity);
    }

    public void setMyData(String myData) {
        this.myData = myData;
    }

    public void setCinemaId(String cinemaId) {
        this.cinemaId = cinemaId;
    }

    public void setFilmId(String filmId) {
        this.filmId = filmId;
    }

    @Override
    public PriceViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=mLayoutInflater.inflate(R.layout.list_item_price_expandable,parent,false);
        return new PriceViewHodler(view);
    }

    public  class PriceViewHodler extends BaseSimpleRecyclerAdapter.BaseViewHolder{
        RelativeLayout price_expandable;
        StretchedListView lst;
        TextView price_expandable_tv_name;
        TextView price_expandable_tv_place;
        TextView price_expandable_tv_end;
        TextView price_expandable_number;
        TextView price_expandable_buy_number;
        TextView price_expandable_slider_item_name;
        ProgressWheel price_expandable_slider_item_load;
        ImageView price_expandable_image;
        View view;
        View itemView;
        CustomDialog appUpdateDialog;


        public PriceViewHodler(View itemView) {
            super(itemView);
            this.itemView=itemView;
            price_expandable = (RelativeLayout) itemView.findViewById(R.id.price_expandable);
             lst = (StretchedListView) itemView.findViewById(R.id.listview);
             price_expandable_slider_item_name = (TextView) itemView.findViewById(R.id.price_expandable_slider_item_name);
             price_expandable_slider_item_load = (ProgressWheel) itemView.findViewById(R.id.price_expandable_slider_item_load);
             view =mLayoutInflater.inflate(R.layout.list_item_price_slider_item,lst,false);
             price_expandable_tv_name= (TextView) itemView.findViewById(R.id.price_expandable_tv_name);
             price_expandable_tv_place= (TextView) itemView.findViewById(R.id.price_expandable_tv_place);
            price_expandable_tv_end= (TextView) itemView.findViewById(R.id.price_expandable_tv_end);
            price_expandable_buy_number= (TextView) itemView.findViewById(R.id.price_expandable_buy_number);
             price_expandable_number= (TextView) itemView.findViewById(R.id.price_expandable_number);
             price_expandable_image= (ImageView) itemView.findViewById(R.id.price_expandable_image);
            price_expandable_slider_item_name.setText("暂无数据");
        }

        @Override
        public void bindModelToView(final int position) {


            if(!getItem(position).flag){
                price_expandable.setVisibility(View.GONE);
//                EffectAnim.rotateAnimate(price_expandable_image);
//                price_expandable_image.setImageResource(R.drawable.price_expandable_close);
                price_expandable_buy_number.setText("展开");
//                EventBus.getDefault().post(true);
            }else{
                price_expandable.setVisibility(View.VISIBLE);
                if(position==0) {
                    EffectAnim.rotateAnimate(price_expandable_image);
//                price_expandable_image.setImageResource(R.drawable.price_expandable_open);
                }
                price_expandable_buy_number.setText("收起");
                initData(lst, position, getItem(position).startTime, price_expandable_slider_item_load, price_expandable_slider_item_name);
//                EffectAnim.rotateAnimate(price_expandable_image);

            }
            NetworkManager.TicketPriceModel model = getItem(position);
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
            float price=model.price;
            String result = "" + price;
            if ((price - ((int)(price))) < 0.0001) {
                result = String.valueOf(((int)price));
            }

            price_expandable_tv_name.setText(startTime);
            price_expandable_tv_end.setText(endTime+ "散场");
            price_expandable_tv_place.setText( language +" / "+ remark);
            price_expandable_number.setText(result);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getItem(position).flag) {
                        getItem(position).flag = false;
                        price_expandable.setVisibility(View.GONE);
                        EffectAnim.rotateAnimate1(price_expandable_image);
                        price_expandable_buy_number.setText("展开");
                    } else {
                        initData(lst, position, getItem(position).startTime, price_expandable_slider_item_load,price_expandable_slider_item_name);
                        for (int i = 0; i < getItemCount(); i++) {
                            if (i != position && getItem(i).flag) {
                                getItem(i).flag = false;
                                notifyItemChanged(i);
                            }
                        }
                        getItem(position).flag = true;
                        price_expandable.setVisibility(View.VISIBLE);
                        price_expandable_buy_number.setText("收起");
                        EffectAnim.rotateAnimate(price_expandable_image);
                    }
//                    notifyItemChanged(position);

                }
            });


            lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                    activity.startActivity(new Intent(activity,PriceDetailActivity.class));

                    if(tickets!=null&&AppUtils.checkAppByPackageNameInstall(mActivity,tickets.get(position).srcAndroidEntry)){
//                        AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
//                        StringBuilder build = new StringBuilder();
//                        alert.setTitle("打开"+tickets.get(position).source+"APP")
//                                .setMessage("现在打开"+tickets.get(position).source+"去购票")
//                                .setPositiveButton("确定",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog,
//                                                                int which) {
//                                                AppUtils.startAppByPackageName(mActivity, tickets.get(position).srcAndroidEntry);
//                                            }
//                                        })
//                                .setNegativeButton("取消",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog,
//                                                                int which) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                        alert.create().show();


                            appUpdateDialog=null;
                            appUpdateDialog = new CustomDialog(mActivity);
                            View view_open_app = LayoutInflater.from(mActivity).inflate(R.layout.dialog_update_app, null);
                            TextView tilte = (TextView) view_open_app.findViewById(R.id.update_app_title);
                            Button buttonCancel = (Button) view_open_app.findViewById(R.id.btnCancelNickname);
                            Button buttonUpdate = (Button) view_open_app.findViewById(R.id.btnUpdateNickname);
                            TextView update_app_description = (TextView) view_open_app.findViewById(R.id.update_app_description);
                            buttonCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    appUpdateDialog.dismiss();
                                }
                            });
                            buttonUpdate.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    AppUtils.startAppByPackageName(mActivity, tickets.get(position).srcAndroidEntry);
                                    appUpdateDialog.dismiss();
                                }
                            });
                        tilte.setText("打开"+tickets.get(position).source+"APP");
                        update_app_description.setText("现在打开"+tickets.get(position).source+"去购票");
                        appUpdateDialog.setContentView(view_open_app);
                        appUpdateDialog.show(Gravity.CENTER);
                    }else{
                        showTextToast("未安装" + tickets.get(position).source + "APP");
                    }

                }
            });


        }
    }
    private ArrayList<NetworkManager.TicketPriceModel> tickets;
    private void initData(final StretchedListView lst, final int position,String startTime, final ProgressWheel price_expandable_slider_item_load,final TextView price_expandable_slider_item_name){

        price_expandable_slider_item_load.setVisibility(View.VISIBLE);
        NetworkManager.getInstance().getTicketPriceList(cinemaId, filmId,myData,startTime, new HttpRespCallback<NetworkManager.RespTicketPriceList>() {
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
                            RelativeLayout price_fragment_src_relative=holder.getView(R.id.price_fragment_src_relative);
                            TextView price_slider_item_name=holder.getView(R.id.price_slider_item_name);
                            TextView price_slider_item_name_install=holder.getView(R.id.price_slider_item_name_install);
                            TextView price_slider_item_number=holder.getView(R.id.price_slider_item_number);
                            TextView price_slider_item_tag=holder.getView(R.id.price_slider_item_tag);
                            ImageView iv=holder.getView(R.id.price_slider_item_iv_alias);

                            if(position==0){
                                iv.setImageResource(R.drawable.price_slider_item_iv_install_small);
                                price_slider_item_name.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.price_yellow_text));
                                price_slider_item_number.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.price_yellow_text));
                                price_slider_item_tag.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.price_yellow_text));
                                price_slider_item_name_install.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.price_yellow_hint_text));

                            }else{
                                iv.setImageResource(R.drawable.price_slider_item_iv_install);
                                price_slider_item_name.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.white));
                                price_slider_item_number.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.white));
                                price_slider_item_tag.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.white));
                                price_slider_item_name_install.setTextColor(price_slider_item_name.getContext().getResources().getColor(R.color.item_text_gray_color));
                            }

                            if(AppUtils.checkAppByPackageNameInstall(iv.getContext(),itemData.srcAndroidEntry)){
                                iv.setVisibility(View.VISIBLE);
                                holder.setText(R.id.price_slider_item_name_install, "已安装");
                                price_fragment_src_relative.setBackgroundResource(R.drawable.bg_btn_price_place_pressed_selector);
                            }else{
                                iv.setVisibility(View.INVISIBLE);
                                holder.setText(R.id.price_slider_item_name_install, "未安装");
                                price_fragment_src_relative.setBackgroundResource(R.color.black);
                            }


                            float price=itemData.price;
                            String result = "" + price;
                            if ((price - ((int)(price))) < 0.0001) {
                                result = String.valueOf(((int)price));
                            }
                            holder.setText(R.id.price_slider_item_number,result);
                            holder.setText(R.id.price_slider_item_name,itemData.source);
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
