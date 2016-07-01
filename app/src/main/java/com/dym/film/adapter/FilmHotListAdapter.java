package com.dym.film.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.BannerListInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class FilmHotListAdapter extends CommonBaseAdapter<FilmHotListInfo.NewsModel>
{

    private ArrayList<BannerListInfo.BannerModel> banners;
    public FilmHotListAdapter(Context context, ArrayList<BannerListInfo.BannerModel> banners, List<FilmHotListInfo.NewsModel> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);
        this.banners=banners;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void convert(ViewHolder holder, FilmHotListInfo.NewsModel itemData,int position)
    {

        ImageView imgFilmHotCover= holder.getView(R.id.imgFilmHotCover);
        ImageView imgBanner= holder.getView(R.id.imgBanner);
        TextView tvFilmHotTitle= holder.getView(R.id.tvFilmHotTitle);
        TextView tvFilmHotDate= holder.getView(R.id.tvFilmHotDate);

        LogUtils.i("123","paramsH-"+imgFilmHotCover.getLayoutParams().height);
        LogUtils.i("123","viewH-"+imgFilmHotCover.getHeight());
        ImageLoaderUtils.displayImage(itemData.logo, imgFilmHotCover);
        tvFilmHotTitle.setText(itemData.title);
//        tvFilmHotDate.setText(itemData.get("publishTime"));
        CommonManager.setTime3(tvFilmHotDate,itemData.publishTime);

        if (position!=0&&position%8==0&&position<=banners.size()*8){
            imgBanner.setVisibility(View.VISIBLE);
            int width = DimenUtils.getScreenWidth(mContext);
            imgBanner.getLayoutParams().height = (int) (width / 3);
            int index= position/8-1;
            ImageLoader.getInstance().displayImage(banners.get(index).img, imgBanner);
            imgBanner.setTag(banners.get(index).url);
            imgBanner.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    //点击事件
//                    Toast.makeText(view.getContext(), "点击了第" + position + "个", Toast.LENGTH_SHORT).show();
                    String url = (String) view.getTag();
//                    Intent intent = new Intent(mContext, HtmlActivity.class);
//                    intent.putExtra(HtmlActivity.KEY_HTML_URL, url);
//                    startActivity(intent);
                    CommonManager.processBannerClick(mContext, url);
                }
            });
        }else{
            imgBanner.setVisibility(View.GONE);
        }
    }


}
