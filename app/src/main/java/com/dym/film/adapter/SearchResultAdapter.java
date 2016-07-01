package com.dym.film.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.home.PreFilmDetailActivity;
import com.dym.film.controllers.MixController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.model.BaseRespInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wbz360 on 2016/3/22.
 */
public class SearchResultAdapter extends BaseAdapter {
    private static final int TYPE_GROUP_ITEM = 0;
    private static final int TYPE_CHILD1_ITEM = 1;//电影
    private static final int TYPE_CHILD2_ITEM = 2;//影评人
    private static final int TYPE_CHILD3_ITEM = 3;//影评
    private ArrayList<ArrayList<NetworkManager.BaseRespModel>> mListItemData;
    private ArrayList<String> mListGroupData;//分组标签
    private LayoutInflater mInflater;
    private Context context;

    public SearchResultAdapter(Context context, ArrayList<ArrayList<NetworkManager.BaseRespModel>> mListItemData, ArrayList<String> mListGroupData) {

        this.context = context;
        this.mListGroupData = mListGroupData;
        this.mListItemData = mListItemData;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int count = mListGroupData.size();
        //  所有分组数，加上每个分组中items的总和
        for (List<NetworkManager.BaseRespModel> list : mListItemData) {
            count += list.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    public Object getGroup(int groupPosition) {
        return mListGroupData.get(groupPosition);
    }

    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mListItemData.get(groupPosition).get(childPosition);
    }

    public int[] getCurSection(int position) {
        int groupPosition = 0;//当前第几个分组
        int childPosition = -1;//在当前分组里是第几个item，-1表示为分组类型
        // 异常情况处理
        if (position < 0 || position > getCount()) {
            return null;
        }
        // 该分类之前所有分类数目之和
        int allFrontGroupCount = 0;
        for (int i = 0; i < mListItemData.size(); i++) {
            //每个分组的item数量（包括分组）
            int size = mListItemData.get(i).size() + 1;
            // 在当前分类中的索引值
            int curIndex = position - allFrontGroupCount;
            // item在当前分类内
            if (curIndex < size) {
                childPosition = curIndex - 1;//比如-1说明是分组项
                groupPosition = i;
                int[] curSection = {groupPosition, childPosition};
                return curSection;
            }
            // 索引移动到当前分类结尾，即下一个分类第一个元素索引
            allFrontGroupCount += size;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        int[] curSection = getCurSection(position);
        String groupTitle = mListGroupData.get(curSection[0]);
        if (curSection[1] == -1) {
            return TYPE_GROUP_ITEM;
        } else if (groupTitle.equals("电影")) {
            return mListGroupData.indexOf("电影")+1;
        } else if (groupTitle.equals("影评人")) {
            return mListGroupData.indexOf("影评人")+1;
        } else if (groupTitle.equals("影评")) {
            return mListGroupData.indexOf("影评")+1;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return mListGroupData.size() +1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int[] curSection = getCurSection(position);
        String groupTitle = mListGroupData.get(curSection[0]);
        if (curSection[1] == -1) {//说明是分组view
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.list_item_search_head_result, null);
            }
            TextView textView = (TextView) convertView.findViewById(R.id.serch_result_et);
            textView.setText(groupTitle);
        } else if (groupTitle.equals("电影")) {
            convertView = getViewFlim(convertView, curSection);

        } else if (groupTitle.equals("影评人")) {
            convertView = getViewCritic(convertView, curSection);

        } else if (groupTitle.equals("影评")) {
            convertView = getViewCinecism(convertView, curSection);
        }
        return convertView;
    }

    private  View getViewFlim(View convertView, int[] curSection) {
        ViewHolderFilm viewHolder = null;

        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.list_item_search_film_result, null);
            viewHolder = new ViewHolderFilm();
            viewHolder.iv_play = (View) convertView.findViewById(R.id.serch_result_play);
            viewHolder.iv_play.setVisibility(View.GONE);
            viewHolder.iv = (ImageView) convertView.findViewById(R.id.serch_result_imgFilmCover_iv);
            viewHolder.serch_result_tvFilmName = (TextView) convertView.findViewById(R.id.serch_result_tvFilmName);
            viewHolder.serch_result_tvFilmRegion = (TextView) convertView.findViewById(R.id.serch_result_tvFilmRegion);
            viewHolder.serch_result_tvFilmDirector = (TextView) convertView.findViewById(R.id.serch_result_tvFilmDirector);
            viewHolder.serch_result_tvFilmActor = (TextView) convertView.findViewById(R.id.serch_result_tvFilmActor);
            viewHolder.serch_result_yinmu = (TextView) convertView.findViewById(R.id.serch_result_yinmu);
            viewHolder.serch_result_piaogen = (TextView) convertView.findViewById(R.id.serch_result_piaogen);
            viewHolder.serch_result_tv_month = (TextView) convertView.findViewById(R.id.serch_result_tv_month);
            viewHolder.serch_result_tv_year = (TextView) convertView.findViewById(R.id.serch_result_tv_year);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderFilm) convertView.getTag();
        }
        // 绑定数据
        final NetworkManager.SearchFilmModel model = (NetworkManager.SearchFilmModel) getChild(curSection[0], curSection[1]);
        String name = model.name;
        if (TextUtils.isEmpty(name)) {
            name = "暂无";
        }
        String country = model.country;
        if (TextUtils.isEmpty(country)) {
            country = "暂无";
        }
        String director = model.director;
        if (TextUtils.isEmpty(director)) {
            director = "暂无";
        }
        String cast = model.cast;
        if (TextUtils.isEmpty(cast)) {
            cast = "暂无";
        }
        String releaseDate = model.releaseDate;
        if (TextUtils.isEmpty(releaseDate)) {
            releaseDate = "0";
        }

        String dymIndex = model.dymIndex;
        if (TextUtils.isEmpty(dymIndex) || dymIndex.equals("0") || dymIndex.equals("0.0")) {
            dymIndex = "  暂无";
        }

        String stubIndex = model.stubIndex;
        if (TextUtils.isEmpty(stubIndex) || stubIndex.equals("0") || stubIndex.equals("0.0")) {
            stubIndex = "  暂无";
        }
        final String status = model.status;

        viewHolder.serch_result_tvFilmName.setText(name);
        CommonManager.displayImage(model.post, viewHolder.iv, R.drawable.ic_default_loading_img);
        viewHolder.serch_result_tvFilmDirector.setText(director);
        viewHolder.serch_result_tvFilmActor.setText(cast);
        viewHolder.serch_result_tvFilmRegion.setText(country + " / " + releaseDate);
        viewHolder.serch_result_yinmu.setText(dymIndex.trim().equals("暂无") ? dymIndex : dymIndex + "%");
        viewHolder.serch_result_piaogen.setText(stubIndex.trim().equals("暂无") ? stubIndex : stubIndex + "%");
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.equals("1")) {
                    Intent it = new Intent(context, PreFilmDetailActivity.class);
                    it.putExtra(FilmDetailActivity.KEY_FILM_ID, model.filmID);
                    context.startActivity(it);
                } else {
                    Intent it = new Intent(context, FilmDetailActivity.class);
                    it.putExtra(FilmDetailActivity.KEY_FILM_ID, model.filmID);
                    context.startActivity(it);
                }
            }
        });
        return convertView;
    }

    private View getViewCritic(View convertView, int[] curSection) {
        ViewHolderCritic viewHolder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.list_item_critic, null);
            viewHolder = new ViewHolderCritic();
            viewHolder.mAvatar = (ImageView) convertView.findViewById(R.id.criticAvatar);
            viewHolder.nameText = (TextView) convertView.findViewById(R.id.filmReviewerNameText);
            viewHolder.introText = (TextView) convertView.findViewById(R.id.filmReviewerIntroText);
            viewHolder.numText = (TextView) convertView.findViewById(R.id.criticFilmReviewNum);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderCritic) convertView.getTag();
        }
        // 绑定数据
        final NetworkManager.CriticRespModel critic = (NetworkManager.CriticRespModel) getChild(curSection[0], curSection[1]);
        CommonManager.displayAvatar(critic.avatar, viewHolder.mAvatar);
        viewHolder.nameText.setText(critic.name);
        viewHolder.introText.setText(critic.title);
        viewHolder.numText.setText(String.valueOf(critic.cinecismNum));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
                Intent intent = new Intent(context, CriticDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
            }
        });
        return convertView;
    }

    private View getViewCinecism(View convertView, int[] curSection) {
        ViewHolderCinecism viewHolder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.list_item_film_review, null);
//            viewHolder = new ViewHolderCinecism();
//                viewHolder.content = (TextView) convertView.findViewById(R.id.content);

//            viewHolder.imgFilmReviewPraise = convertView.findViewById(R.id.imgFilmReviewPraise);
//            viewHolder.tvFilmReviewScore = (TextView) convertView.findViewById(R.id.tvFilmReviewScore);
//            viewHolder.tvFilmReviewTitle = (TextView) convertView.findViewById(R.id.tvFilmReviewTitle);
//            viewHolder.tvFilmReviewWriter = (TextView)  convertView.findViewById(R.id.tvFilmReviewWriter);
//            viewHolder.tvWriterHonor = (TextView)  convertView.findViewById(R.id.tvWriterHonor);
//            viewHolder.tvFilmReviewDate = (TextView) convertView.findViewById(R.id.tvFilmReviewDate);
//            convertView.setTag(viewHolder);
        } else {
//            viewHolder = (ViewHolderCinecism) convertView.getTag();
        }

        // 绑定数据
        NetworkManager.FilmReviewRespModel cinecismModel = (NetworkManager.FilmReviewRespModel) getChild(curSection[0], curSection[1]);
        MixController.setupCriticFilmReview(cinecismModel,convertView,true,true);
//        viewHolder.tvWriterHonor.setVisibility(View.GONE);
//
//        final String cinecismID = cinecismModel.cinecismID;
//        String title = cinecismModel.title;
//        String srcMedia = cinecismModel.srcMedia;
//        String srcUrl = cinecismModel.srcUrl;
//        String srcScore = cinecismModel.srcScore;
//        String createTime = cinecismModel.createTime;
//        String filmPost = cinecismModel.logo;
//        String opinion = cinecismModel.opinion;
//
//        NetworkManager.SearchReviewWriterModel writer = cinecismModel.writer;
//        String criticID = writer.criticID;
//        String writerTitle = writer.title;
//        String writerName = writer.name;
//        String writerAvatar = writer.avatar;
//
//        ArrayList<NetworkManager.SearchReviewFilmModel> filmlist =cinecismModel.film;
//        String film_name=filmlist.get(0).name;
//
//
//        if (opinion.equals("1")) {
//            viewHolder.imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_good_movie2);
//        } else {
//            viewHolder.imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_bad_movie2);
//        }
//        if (srcScore.equals("0.0") || srcScore.equals("0")) {
//            viewHolder.tvFilmReviewScore.setText("FROM " + srcMedia);
//        } else {
//            viewHolder.tvFilmReviewScore.setText("FROM " + srcMedia + " " + srcScore);
//        }
//
//
//        viewHolder.tvFilmReviewTitle.setText(title);
//        viewHolder.tvFilmReviewWriter.setText("《"+film_name.trim()+"》 "+writerName + " ");
////        CommonManager.setTime3(tvFilmReviewDate,createTime);
//        viewHolder.tvFilmReviewDate.setText(createTime.substring(0, 10));
//        viewHolder.tvWriterHonor.setText(writerTitle);
//
//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(context, FilmReviewDetailActivity.class);
//                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, Long.parseLong(cinecismID));
//                context.startActivity(intent);
//            }
//        });

        return convertView;
    }


    private class ViewHolderFilm {
        View iv_play;
        ImageView iv;
        TextView serch_result_tvFilmName;
        TextView serch_result_tvFilmRegion;
        TextView serch_result_tvFilmDirector;
        TextView serch_result_tvFilmActor;
        TextView serch_result_yinmu;
        TextView serch_result_piaogen;
        TextView serch_result_tv_month;
        TextView serch_result_tv_year;
    }

    private class ViewHolderCritic {
        ImageView mAvatar;
        TextView nameText;
        TextView introText;
        TextView numText;
    }

    private class ViewHolderCinecism {
        View imgFilmReviewPraise;
        TextView tvFilmReviewScore;
        TextView tvFilmReviewTitle;
        TextView tvFilmReviewWriter;
        TextView tvWriterHonor;
        TextView tvFilmReviewDate;
    }

}
