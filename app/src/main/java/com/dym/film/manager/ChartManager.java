package com.dym.film.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.home.SingleFilmReviewActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.model.FilmAllIndexListInfo;
import com.dym.film.ui.CircleImageView;
import com.dym.film.utils.DimenUtils;
import com.dym.film.views.LineChartMarkerView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/27.
 */
public class ChartManager
{
    private final String filmName;
    private LineChart mLineChart;
    private TextView tvLikeTicketIndex;
    private TextView tvUnLikeTicketIndex;
    private View likeBarView;
    private View unLikeBarView;
    private TextView tvXLable1;
    private TextView tvXLable2;
    private TextView tvXLable3;
    private TextView tvXLable4;
    private TextView tvXLable5;
    private TextView tvName1;
    private TextView tvName2;
    private TextView tvName3;
    private TextView tvName4;
    private TextView tvName5;
    private TextView tvName6;
    private CircleImageView imgPhoto1;
    private CircleImageView imgPhoto2;
    private CircleImageView imgPhoto3;
    private CircleImageView imgPhoto4;
    private CircleImageView imgPhoto5;
    private ImageView imgPhoto6;
    private View viewPraise1;
    private View viewPraise2;
    private View viewPraise3;
    private View viewPraise4;
    private View viewPraise5;
    private TextView tvPieChartTips;
    private LinearLayout layPieChartPhoto;
    private LinearLayout layPieChartTips;
   private  Context mContext;
    private String filmId;

    public ChartManager(Context pContext,String filmId,String filmName)
    {
        mContext=pContext;
        this.filmId=filmId;
        this.filmName=filmName;
    }

    public void initLineChart( LineChart mLineChart){
        this.mLineChart=mLineChart;
        mLineChart.setDrawGridBackground(false);
        // no description text
        mLineChart.setDescription("");
        mLineChart.setNoDataTextDescription("");
        mLineChart.setNoDataText("");
        mLineChart.setDescriptionColor(0xbbffffff);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);//必须
//        mLineChart.setBackgroundColor(0xff232323);
        // enable scaling and dragging
        mLineChart.setDragEnabled(true);//必须
        mLineChart.setScaleEnabled(false);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(false);
        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        LineChartMarkerView markerView = new LineChartMarkerView(mContext, R.layout.layout_line_chart_marker_view);
        // set the marker to the chart
        mLineChart.setMarkerView(markerView);
        //下面是x轴相关的设置
        XAxis xAxis = mLineChart.getXAxis();
//        xAxis.setEnabled(false);
//        xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        xAxis.setDrawAxisLine(false);
        xAxis.setAxisLineColor(Color.BLUE);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);//设置横向坐标每个点相距的长度，单位是字母宽度，根据横向坐标最大值和屏幕宽度，计算出有多少个点
        xAxis.setDrawGridLines(true);//设置与x轴相交的网格线
        xAxis.setGridColor(0xff303030);
        xAxis.setTextColor(0xffffffff);
        xAxis.setTextSize(12);
        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setStartAtZero(true);//从0线处开始画
        leftAxis.setEnabled(false);
        //leftAxis.setYOffset(20f);
        //leftAxis.enableGridDashedLine(10f, 10f, 0f);//与y坐标轴线相交线，设置为虚线
        mLineChart.getAxisRight().setEnabled(false);//右坐标不可用
        mLineChart.animateX(1000, Easing.EasingOption.EaseInOutSine);
        Legend l = mLineChart.getLegend();
        l.setEnabled(false);
    }
    public void setLineChartData( ArrayList<FilmAllIndexListInfo.ShardsModel> shards)
    {
        int size=shards.size();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i <size; i++) {
//            xVals.add((i+1) + "");
            xVals.add("");
        }
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < size; i++) {
            float val = (float)shards.get(i).box;
//            float val = (float) (Math.random() * 100) + 3;// + (float)
            yVals.add(new Entry(val, i));
        }
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "");

        // set1.setFillAlpha(110);
        //set1.setFillColor(Color.YELLOW);
        // set the line to be drawn like this "- - - - - -"
        //  set1.enableDashedLine(10f, 5f, 0f);
        // set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(0xffb10b0b);
        set1.setLineWidth(1f);
        set1.setCircleColor(0x70B10B0B);
        set1.setCircleSize(7f);
        set1.setDrawCircleHole(true);
        set1.setCircleColorHole(0x80B10B0B);

//        set1.setValueTextSize(9f);
        set1.setDrawValues(false);
        set1.setDrawFilled(true);//设置开启填充
        set1.setFillColor(0x80de2626);

        set1.setDrawCubic(false);//不设置为平滑曲线set1
        set1.setDrawHighlightIndicators(false);//设置是否显示，点击时的相交虚线
        set1.setDrawVerticalHighlightIndicator(true);
        set1.setHighLightColor(0xff505050);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets
        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        // set data
        mLineChart.setData(data);
    }

    public void initPieChart(RelativeLayout layPieChart){

        tvPieChartTips= (TextView) layPieChart.findViewById(R.id.tvPieChartTips);
        layPieChartPhoto= (LinearLayout) layPieChart.findViewById(R.id.layPieChartPhoto);
        layPieChartTips= (LinearLayout) layPieChart.findViewById(R.id.layPieChartTips);
         imgPhoto1= (CircleImageView) layPieChart.findViewById(R.id.imgPhoto1);
         imgPhoto2= (CircleImageView) layPieChart.findViewById(R.id.imgPhoto2);
         imgPhoto3= (CircleImageView) layPieChart.findViewById(R.id.imgPhoto3);
         imgPhoto4= (CircleImageView) layPieChart.findViewById(R.id.imgPhoto4);
         imgPhoto5= (CircleImageView) layPieChart.findViewById(R.id.imgPhoto5);
         imgPhoto6= (ImageView) layPieChart.findViewById(R.id.imgPhoto6);

        tvName1= (TextView) layPieChart.findViewById(R.id.tvName1);
        tvName2= (TextView) layPieChart.findViewById(R.id.tvName2);
        tvName3= (TextView) layPieChart.findViewById(R.id.tvName3);
        tvName4= (TextView) layPieChart.findViewById(R.id.tvName4);
        tvName5= (TextView) layPieChart.findViewById(R.id.tvName5);
        tvName6= (TextView) layPieChart.findViewById(R.id.tvName6);

        viewPraise1= (View) layPieChart.findViewById(R.id.viewPraise1);
        viewPraise2= (View) layPieChart.findViewById(R.id.viewPraise2);
        viewPraise3= (View) layPieChart.findViewById(R.id.viewPraise3);
        viewPraise4= (View) layPieChart.findViewById(R.id.viewPraise4);
        viewPraise5= (View) layPieChart.findViewById(R.id.viewPraise5);
    }
    public void setPieChartData(ArrayList<FilmAllIndexListInfo.CriticsModel> critics , int criticNum)
    {
        if (criticNum< ConfigInfo.cinecismNum){
            layPieChartTips.setVisibility(View.VISIBLE);
            layPieChartPhoto.setVisibility(View.GONE);
            return;
        }
        layPieChartTips.setVisibility(View.GONE);
        layPieChartPhoto.setVisibility(View.VISIBLE);
        for (int i = 0; i <critics.size() ; i++) {
            FilmAllIndexListInfo.CriticsModel criticModel=critics.get(i);
            NetworkManager.CriticRespModel critic=new NetworkManager.CriticRespModel();
            critic.criticID=criticModel.criticID;
            critic.avatar=criticModel.avatar;
            critic.title=criticModel.title;
            critic.name=criticModel.name;

            if (i==0){
                tvName1.setText(criticModel.name);
                ImageLoaderUtils.displayImage(criticModel.avatar, imgPhoto1, R.drawable.ic_default_photo);
                if (criticModel.opinion==0){
                    viewPraise1.setBackgroundResource(R.drawable.ic_bad_movie);
                    imgPhoto1.setBorderColor(0xffb6b6b6);
                }else{

                    viewPraise1.setBackgroundResource(R.drawable.ic_good_movie);
                    imgPhoto1.setBorderColor(0xffb10b0b);
                }
                imgPhoto1.setOnClickListener(new MyClickListener(critic));

            }else if(i==1){
                tvName2.setText(criticModel.name);
                ImageLoaderUtils.displayImage(criticModel.avatar, imgPhoto2, R.drawable.ic_default_photo);
                if (criticModel.opinion==0){
                    viewPraise2.setBackgroundResource(R.drawable.ic_bad_movie);
                    imgPhoto2.setBorderColor(0xffb6b6b6);
                }else{
                    viewPraise2.setBackgroundResource(R.drawable.ic_good_movie);
                    imgPhoto2.setBorderColor(0xffb10b0b);
                }
                imgPhoto2.setOnClickListener(new MyClickListener(critic));
            }else if(i==2){
                tvName3.setText(criticModel.name);
                ImageLoaderUtils.displayImage(criticModel.avatar, imgPhoto3, R.drawable.ic_default_photo);
                if (criticModel.opinion==0){
                    viewPraise3.setBackgroundResource(R.drawable.ic_bad_movie);
                    imgPhoto3.setBorderColor(0xffb6b6b6);
                }else{
                    viewPraise3.setBackgroundResource(R.drawable.ic_good_movie);
                    imgPhoto3.setBorderColor(0xffb10b0b);
                }
                imgPhoto3.setOnClickListener(new MyClickListener(critic));
            }else if(i==3){
                tvName4.setText(criticModel.name);
                ImageLoaderUtils.displayImage(criticModel.avatar, imgPhoto4, R.drawable.ic_default_photo);
                if (criticModel.opinion==0){
                    viewPraise4.setBackgroundResource(R.drawable.ic_bad_movie);
                    imgPhoto4.setBorderColor(0xffb6b6b6);
                }else{
                    viewPraise4.setBackgroundResource(R.drawable.ic_good_movie);
                    imgPhoto4.setBorderColor(0xffb10b0b);
                }
                imgPhoto4.setOnClickListener(new MyClickListener(critic));
            }else if(i==4){
                tvName5.setText(criticModel.name);
                ImageLoaderUtils.displayImage(criticModel.avatar, imgPhoto5, R.drawable.ic_default_photo);
                if (criticModel.opinion==0){
                    viewPraise5.setBackgroundResource(R.drawable.ic_bad_movie);
                    imgPhoto5.setBorderColor(0xffb6b6b6);
                }else{
                    viewPraise5.setBackgroundResource(R.drawable.ic_good_movie);
                    imgPhoto5.setBorderColor(0xffb10b0b);
                }
                imgPhoto5.setOnClickListener(new MyClickListener(critic));
            }
        }

        tvName6.setText(criticNum+"");
        tvName6.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, SingleFilmReviewActivity.class);
                intent.putExtra("filmId", filmId + "");
                intent.putExtra("filmName", filmName + "");
                mContext.startActivity(intent);
            }
        });
    }


    public class  MyClickListener implements View.OnClickListener{

        NetworkManager.CriticRespModel critic;
        public MyClickListener( NetworkManager.CriticRespModel critic){
            this.critic=critic;
        }
        @Override
        public void onClick(View v)
        {
//            CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
//            Intent intent = new Intent(mContext, CriticDetailActivity.class);
//            mContext.startActivity(intent);
            Intent intent = new Intent(mContext, FilmReviewDetailActivity.class);
            intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, critic.criticID);
            intent.putExtra("filmID", filmId);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }
    public void initBarChart(RelativeLayout layBarChart){
         tvLikeTicketIndex= (TextView) layBarChart.findViewById(R.id.tvLikeTicketIndex);
         tvUnLikeTicketIndex= (TextView) layBarChart.findViewById(R.id.tvUnLikeTicketIndex);
         likeBarView= (View) layBarChart.findViewById(R.id.likeBarView);
         unLikeBarView= (View) layBarChart.findViewById(R.id.unLikeBarView);

         tvXLable1= (TextView) layBarChart.findViewById(R.id.tvXLable1);
         tvXLable2= (TextView) layBarChart.findViewById(R.id.tvXLable2);
         tvXLable3= (TextView) layBarChart.findViewById(R.id.tvXLable3);
         tvXLable4= (TextView) layBarChart.findViewById(R.id.tvXLable4);
//         tvXLable5= (TextView) layBarChart.findView(R.id.tvXLable5);

    }

    public void setBarChartData( int positiveSum,int negtiveSum){
        int screenWidth= (int) DimenUtils.getScreenWidth(mContext)-DimenUtils.dp2px(mContext,40);
        int maxSum=positiveSum>negtiveSum?positiveSum:negtiveSum;
//        float allSum= (float) (maxSum*7/6.0);//最大值是总共的9/10，这里算出总共值
//        float averageF= (float) (allSum/7.0);
//        int averageInt= (int) Math.ceil(2*averageF);
        int averageInt= (int) Math.ceil(maxSum/3.0);//是上面三步推导出
        float allSum= (float) (7*averageInt/2.0);
        float likePercent= (float) ((positiveSum*1.00)/allSum);
        float unlikePercent= (float) ((negtiveSum*1.00)/allSum);
        int likeViewWidth= (int) (screenWidth*likePercent);
        int unlikeViewWidth= (int) (screenWidth*unlikePercent);

        likeBarView.getLayoutParams().width=likeViewWidth;
        unLikeBarView.getLayoutParams().width=unlikeViewWidth;

        tvXLable1.setText("0");
        tvXLable2.setText(averageInt+"");
        tvXLable3.setText(averageInt*2+"");
        tvXLable4.setText(averageInt*3+"");
//        tvXLable5.setText(average*4+"");

        tvLikeTicketIndex.setText(positiveSum+"");
        tvUnLikeTicketIndex.setText(negtiveSum+"");
    }
}
