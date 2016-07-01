package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.MixController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/30
 */

/**
 * 影评首页的影评列表页的Adapter
 */
public class AllFilmReviewRecyclerAdapter extends SimpleRecyclerAdapter<NetworkManager.FilmReviewRespModel>
{
    public final static String TAG = "AllFRAdapter";

    private final static int NUM_INTER = 8;
    private ArrayList<NetworkManager.BannerModel> mBanners = new ArrayList<>();

    public AllFilmReviewRecyclerAdapter(Activity activity)
    {
        super(activity);
    }

    public void refreshBanner()
    {
        /**
         * 获取广告位
         */
        NetworkManager.getInstance().getBanners(3, new HttpRespCallback<NetworkManager.RespGetBanner>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespGetBanner resp = (NetworkManager.RespGetBanner) msg.obj;

                mBanners.clear();
                mBanners.addAll(resp.banners);

                updateFilmReviewData();
            }
        });
    }

    /**
     * 在影评列表里面插入空位
     */
    private synchronized void updateFilmReviewData()
    {
        if (mBanners.isEmpty()) {
            return;
        }

        int i = 0;
        int index = NUM_INTER;
        while (index < getItemCount()) {
            NetworkManager.FilmReviewRespModel model = getItem(index);
            if (model.cinecismID > 0) {
                NetworkManager.FilmReviewRespModel banner = new NetworkManager.FilmReviewRespModel();
                banner.cinecismID = -i;
                insert(index, banner);
            }

            i += 1;
            index += NUM_INTER + i;
        }
    }

    @Override
    public void setAll(Collection<? extends NetworkManager.FilmReviewRespModel> filmReviewRespModels)
    {
        super.setAll(filmReviewRespModels);

        updateFilmReviewData();
    }

    @Override
    public synchronized void appendAll(Collection<? extends NetworkManager.FilmReviewRespModel> filmReviewRespModels, boolean smooth)
    {
        super.appendAll(filmReviewRespModels, false);
        updateFilmReviewData();
    }

    private final static int TYPE_ADS = 0x1111;

    @Override
    public int getItemViewType(int position)
    {
        NetworkManager.FilmReviewRespModel review = getItem(position);
        if (review != null) {
            int bIndex = (int) -review.cinecismID;
            if (bIndex >= 0 && bIndex < mBanners.size()) {
                return TYPE_ADS;
            }
        }

        return super.getItemViewType(position);
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType)
    {
        if (viewType == TYPE_ADS) {
            return mLayoutInflater.inflate(R.layout.list_item_ads_banner, parent, false);
        }
        return mLayoutInflater.inflate(R.layout.list_item_home_film_review, parent, false);
    }

    @Override
    public void onBindModelToView(SimpleViewHolder viewHolder, int position)
    {
        NetworkManager.FilmReviewRespModel review = getItem(position);
        if (review == null) {
            return;
        }

        int bIndex = (int) -review.cinecismID;

        int type = viewHolder.getItemViewType();
        if (type == TYPE_ADS) {
            bindBannerModel(mBanners.get(bIndex), viewHolder, position);
        }
        else {
            bindFilmReviewModel(review, viewHolder, position);
        }
    }

    private void bindBannerModel(NetworkManager.BannerModel banner, SimpleViewHolder viewHolder, int position)
    {
        ImageView image = (ImageView) viewHolder.findView(R.id.bannerImage);

        String imageUrl = QCloudManager.urlImage2(banner.img, CommonManager.dpToPx(400));
        CommonManager.displayImage2(imageUrl, image, R.drawable.ic_default_loading_img);

        final String url = banner.url;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CommonManager.processBannerClick(mActivity, url);
            }
        });
    }

    private void bindFilmReviewModel(NetworkManager.FilmReviewRespModel review, SimpleViewHolder viewHolder, int position)
    {
        ArrayList<NetworkManager.FilmRespModel> films = review.film;
        NetworkManager.CriticRespModel writer = review.writer;

        NetworkManager.FilmRespModel film1 = films == null || films.isEmpty() ? null : films.get(0);
        NetworkManager.FilmRespModel film2 = films == null || films.size() < 2 ? null : films.get(1);
        /**
         * 加载影评logo
         */
        ImageView imageView = (ImageView) viewHolder.findView(R.id.filmReviewImage);
        CommonManager.displayReviewLogo(film1 == null ? "" : film1.post, imageView);

        /**
         * 设置来源
         */
        TextView textView = (TextView) viewHolder.findView(R.id.fromResourceText);
        textView.setText(review.srcMedia);

        /**
         * 设置评分
         */
        TextView scoreText = (TextView) viewHolder.findView(R.id.resourceScoreText);
        scoreText.setVisibility(View.INVISIBLE);
//        float score = 0;
//        try {
//            score = Float.valueOf(review.srcScore);
//        }
//        catch (Exception e) {
//            score = 0;
//        }
//
//        if (score > 0) {
//            scoreText.setText(review.srcScore);
//            scoreText.setVisibility(View.VISIBLE);
//        }
//        else {
//            scoreText.setVisibility(View.GONE);
//        }

        String res = (film1 != null ? film1.name : "") + (film2 != null ? film2.name : "");
        MixController.setupAttLayout(viewHolder.findView(R.id.attLayout), review.opinion, res);


        /**
         * 设置内容
         */
        TextView contentText = (TextView) viewHolder.findView(R.id.filmReviewContentText);
        contentText.setText(TextUtils.isEmpty(review.summary) ? review.title : review.summary);

        /**
         * 设置影评人头像
         */

        ImageView avatar = (ImageView) viewHolder.findView(R.id.criticAvatar);
        CommonManager.displayAvatar(writer.avatar, avatar);

        /**
         * 设置影评人名字
         */
        TextView writerName = (TextView) viewHolder.findView(R.id.criticName);
        writerName.setText(writer.name);

        /**
         * 设置时间
         */
        TextView time = (TextView) viewHolder.findView(R.id.timeText);
        CommonManager.setTime(time, review.createTime == null ? 0 : review.createTime.getTime());

        /**
         * 设置item点击事件
         */
        final long id = review.cinecismID;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startFilmReviewDetailActivity(id);
            }
        });
    }

    /**
     * 点击打开影评详情页面
     */
    public void startFilmReviewDetailActivity(long id)
    {
        Intent intent = new Intent(mActivity, FilmReviewDetailActivity.class);
        intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mActivity.startActivity(intent);
    }
}
