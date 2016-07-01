package com.dym.film.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.price.CinemaActivityNew;
import com.dym.film.activity.price.PriceActivityNew;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.FilmListInfo;
import com.dym.film.utils.DimenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class PreFilmListAdapter extends CommonBaseAdapter<FilmListInfo.FilmModel>
{

    public PreFilmListAdapter(Context context, List<FilmListInfo.FilmModel> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder holder, FilmListInfo.FilmModel itemData, int position)
    {
        ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
        TextView tvFilmName = holder.getView(R.id.tvFilmName);
        TextView tvFilmDate = holder.getView(R.id.tvFilmDate);
        TextView tvFilmIntro = holder.getView(R.id.tvFilmIntro);
        Button btnBuyTicket = holder.getView(R.id.btnBuyTicket);

        String url = QCloudManager.urlImage1(itemData.post, DimenUtils.dp2px(mContext,65), DimenUtils.dp2px(mContext,90));
        ImageLoader.getInstance().displayImage(url, imgFilmCover);
        tvFilmName.setText(itemData.name);
//            tvFilmDate.setText(itemData.releaseDate.substring(5));
        tvFilmDate.setText(itemData.releaseDate + "上映");
        tvFilmIntro.setText(itemData.digest);
        if (itemData.sellingTicket == 0) {
            btnBuyTicket.setVisibility(View.INVISIBLE);
        }else{
            btnBuyTicket.setVisibility(View.VISIBLE);
        }
        btnBuyTicket.setTag(itemData);
        btnBuyTicket.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                FilmListInfo.FilmModel itemData = (FilmListInfo.FilmModel) view.getTag();
                Intent intent = new Intent();
                intent.setClass(mContext, CinemaActivityNew.class);
                intent.putExtra(PriceActivityNew.FILMID, itemData.filmID + "");
                intent.putExtra(PriceActivityNew.FILMIDNAME, itemData.name + "");
                intent.putExtra(PriceActivityNew.FLAG, false);
                mContext.startActivity(intent);

            }
        });
    }

    /**
     * 图片
     */
    private void addImageSpan(TextView tv, String content)
    {
        SpannableString spanString1 = new SpannableString("   ");
        SpannableString spanString2 = new SpannableString("   ");
        Drawable leftIcon = mContext.getResources().getDrawable(R.drawable.ic_text_left_mark);
        Drawable rightIcon = mContext.getResources().getDrawable(R.drawable.ic_text_right_mark);
        leftIcon.setBounds(0, 0, leftIcon.getIntrinsicWidth(), leftIcon.getIntrinsicHeight());//设置显示的大小
        rightIcon.setBounds(0, 0, rightIcon.getIntrinsicWidth(), rightIcon.getIntrinsicHeight());
        ImageSpan span1 = new ImageSpan(leftIcon, ImageSpan.ALIGN_BASELINE);
        ImageSpan span2 = new ImageSpan(rightIcon, ImageSpan.ALIGN_BOTTOM);
        spanString1.setSpan(span1, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanString2.setSpan(span2, 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText("");
        tv.append(spanString1);
        tv.append(content);
        tv.append(spanString2);
    }
}
