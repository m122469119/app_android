package com.dym.film.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.price.CinemaActivityNew;
import com.dym.film.activity.price.PriceActivityNew;
import com.dym.film.activity.sharedticket.SharedTicketDetailActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.application.ConfigInfo;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.FilmListInfo;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class ShowFilmListAdapter extends CommonBaseAdapter<FilmListInfo.FilmModel>
{

	public ShowFilmListAdapter(Context context,
							   List<FilmListInfo.FilmModel> mDatas, int itemLayoutId) {
		super(context, mDatas, itemLayoutId);
	}

	@Override
	public void convert(ViewHolder holder, final FilmListInfo.FilmModel itemData,
						final int position) {
		ImageView imgFilmCover= holder.getView(R.id.imgFilmCover);
		TextView tvFilmName= holder.getView(R.id.tvFilmName);
		TextView tvFilmIntro= holder.getView(R.id.tvFilmIntro);
		TextView tvFilmDate= holder.getView(R.id.tvFilmDate);
		LinearLayout layExpertScore=holder.getView(R.id.layExpertScore);//专家平分
		LinearLayout layUserScore=holder.getView(R.id.layUserScore);//网友平分
		TextView tvExpertScore= holder.getView(R.id.tvExpertScore);
		TextView tvUserScore= holder.getView(R.id.tvUserScore);
		Button btnBuyTicket= holder.getView(R.id.btnBuyTicket);
		//标签布局
		LinearLayout layExpertCinecism=holder.getView(R.id.layExpertCinecism);
		LinearLayout layUserTicket=holder.getView(R.id.layUserTicket);
		LinearLayout layFilmHot=holder.getView(R.id.layFilmHot);
		TextView tvExpertCinecismTitle= holder.getView(R.id.tvExpertCinecismTitle);//专家影评标题
		TextView tvUserTicketTitle= holder.getView(R.id.tvUserTicketTitle);//网友晒票标题
		TextView tvFilmHotTitle= holder.getView(R.id.tvFilmHotTitle);//网友晒票标题
		if (itemData.rcmmCinecism!=null){
			layExpertCinecism.setVisibility(View.VISIBLE);
			tvExpertCinecismTitle.setText(itemData.rcmmCinecism.title);
			layExpertCinecism.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					LogUtils.i("123",itemData.rcmmCinecism.cinecismID+"");
					Intent intent = new Intent(mContext, FilmReviewDetailActivity.class);
					intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, itemData.rcmmCinecism.cinecismID);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					mContext.startActivity(intent);
				}
			});
		}else {
			layExpertCinecism.setVisibility(View.GONE);
		}
		if (itemData.rcmmStub!=null){
			layUserTicket.setVisibility(View.VISIBLE);
			tvUserTicketTitle.setText(itemData.rcmmStub.comment);
			layUserTicket.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					CommonManager.putData(SharedTicketDetailActivity.KEY_ID, itemData.rcmmStub.stubID);
					Intent intent = new Intent(mContext, SharedTicketDetailActivity.class);
					intent.putExtra(SharedTicketDetailActivity.KEY_ID,itemData.rcmmStub.stubID);
					mContext.startActivity(intent);
				}
			});
		}else {
			layUserTicket.setVisibility(View.GONE);
		}
		if (itemData.rcmmNews!=null){
			layFilmHot.setVisibility(View.VISIBLE);
			tvFilmHotTitle.setText(itemData.rcmmNews.title);
			layFilmHot.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					long newsID = itemData.rcmmNews.newsID;
					String url = ConfigInfo.BASE_URL + "/film/news/"+newsID+"?downloadable=0";
					Intent intent = new Intent(mContext, HtmlActivity.class);
					intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
					intent.putExtra(HtmlActivity.KEY_HTML_ACTION, 1);
					intent.putExtra("title", itemData.rcmmNews.title);
					mContext.startActivity(intent);
				}
			});
		}else {
			layFilmHot.setVisibility(View.GONE);
		}
		//填充数据
		String url = QCloudManager.urlImage1(itemData.post, DimenUtils.dp2px(mContext,65), DimenUtils.dp2px(mContext,90));
		ImageLoader.getInstance().displayImage(url, imgFilmCover);

		tvFilmName.setText(itemData.name);
		tvFilmIntro.setText(itemData.digest);
		if (itemData.hasShown==1){
			if (itemData.cinecismNum!=0&&itemData.stubNum!=0){
				layUserScore.setVisibility(View.VISIBLE);
				tvFilmDate.setText(itemData.cinecismNum+"专家影评 | "+itemData.stubNum+"人晒票");
			}else if (itemData.cinecismNum!=0&&itemData.stubNum==0){
				layUserScore.setVisibility(View.GONE);
				tvFilmDate.setText(itemData.cinecismNum+"专家影评");
			}else if(itemData.cinecismNum==0&&itemData.stubNum!=0){
				layUserScore.setVisibility(View.VISIBLE);
				tvFilmDate.setText(itemData.stubNum+"人晒票");
			}else{
				layUserScore.setVisibility(View.GONE);
				tvFilmDate.setText("");
			}
			if (itemData.cinecismNum>=ConfigInfo.cinecismNum){
				layExpertScore.setVisibility(View.VISIBLE);
			}else {
				layExpertScore.setVisibility(View.GONE);
			}

			int dymIndex= (int) Float.parseFloat(itemData.dymIndex);
			int stubIndex= (int) Float.parseFloat(itemData.stubIndex);
			tvExpertScore.setText(dymIndex+"");
			tvUserScore.setText(stubIndex+"");
            btnBuyTicket.setText("比价购票");
			btnBuyTicket.setBackgroundResource(R.drawable.bg_btn_buy_ticket_selector);
			btnBuyTicket.setTextColor(0xffffffff);
		}else{
			tvFilmDate.setText(itemData.releaseDate+"上映");
			layExpertScore.setVisibility(View.INVISIBLE);
			layUserScore.setVisibility(View.INVISIBLE);
            btnBuyTicket.setText("预售比价");
			btnBuyTicket.setBackgroundResource(R.drawable.bg_btn_presale_ticket_selector);
			btnBuyTicket.setTextColor(0xffFF7B7B);
		}

        btnBuyTicket.setTag(itemData);
        btnBuyTicket.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FilmListInfo.FilmModel itemData= (FilmListInfo.FilmModel) view.getTag();
                Intent intent = new Intent();
                intent.setClass(mContext, CinemaActivityNew.class);
                intent.putExtra(PriceActivityNew.FILMID, itemData.filmID + "");
                intent.putExtra(PriceActivityNew.FILMIDNAME, itemData.name + "");
                intent.putExtra(PriceActivityNew.FLAG, false);
                mContext.startActivity(intent);

            }
        });



	}

}
