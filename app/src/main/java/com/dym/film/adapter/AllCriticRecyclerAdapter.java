package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/30
 */

/**
 * 所有影评人列表的Adapter
 */
@Deprecated
public class AllCriticRecyclerAdapter extends SimpleRecyclerAdapter<NetworkManager.CriticRespModel>
{
    public final static String TAG = "AllCriticAdapter";

    public AllCriticRecyclerAdapter(Activity activity, BaseSimpleDataDelegate<NetworkManager.CriticRespModel> manager)
    {
        super(activity, manager);
    }

    public AllCriticRecyclerAdapter(Activity activity)
    {
        super(activity);
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType)
    {
        return mLayoutInflater.inflate(R.layout.list_item_critic, parent, false);
    }

    @Override
    public void onBindModelToView(SimpleViewHolder viewHolder, int position)
    {
        final NetworkManager.CriticRespModel critic = getItem(position);

        if (critic == null) {
            return;
        }

        ImageView mAvatar = (ImageView) viewHolder.findView(R.id.criticAvatar);
        /**
         * 设置影评人头像
         */
        CommonManager.displayAvatar(critic.avatar, mAvatar);

        /**
         * 设置名字
         */
        TextView nameText = (TextView) viewHolder.findView(R.id.filmReviewerNameText);
        nameText.setText(critic.name);

        /**
         * 设置介绍
         */
        TextView introText = (TextView) viewHolder.findView(R.id.filmReviewerIntroText);
        introText.setText(critic.title);

        /**
         * 设置条数
         */
        TextView numText = (TextView) viewHolder.findView(R.id.criticFilmReviewNum);
        numText.setText(String.valueOf(critic.cinecismNum));

        /**
         * 设置点击
         */
        LinearLayout itemLayout = (LinearLayout) viewHolder.findView(R.id.listItemLayout);
        itemLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
                startCriticDetailActivity();
            }
        });

        /**
         * 设置最近的影评
         */
        ArrayList<NetworkManager.StubFilmReviewRespModel> reviews = critic.cinecism;
        LinearLayout linearLayout = (LinearLayout) viewHolder.findView(R.id.recentFilmReviewLayout);
        if (reviews != null && !reviews.isEmpty()) {
//                Log.e(TAG, "cinecism size: " + reviews.size());
            linearLayout.setVisibility(View.VISIBLE);
            if (linearLayout.getChildCount() != reviews.size()) {
                linearLayout.removeAllViews();
                int i = 0;
                for (NetworkManager.StubFilmReviewRespModel review : reviews) {

                    View view = View.inflate(mActivity, R.layout.layout_sub_critic_item, null);
                    TextView timeText = (TextView) view.findViewById(R.id.userSharedTimeTextView);
                    TextView unitText = (TextView) view.findViewById(R.id.userSharedTimeUnitTextView);
                    CommonManager.setTime2(timeText, unitText, review.createTime.getTime());

                    ImageView opinionImage = (ImageView) view.findViewById(R.id.opinionImage);
                    opinionImage.setImageResource(review.opinion == 1 ? R.drawable.ic_good_movie1 : R.drawable.ic_bad_movie1);

                    TextView titleText = (TextView) view.findViewById(R.id.contentText);
                    titleText.setText(review.title);

                    final long id = review.cinecismID;
                    view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            startFilmReviewDetailActivity(id);
                        }
                    });

                    view.setTag(i++);
                    linearLayout.addView(view);
                }
            }
            else {
                int i = 0;
                for (NetworkManager.StubFilmReviewRespModel review : reviews) {
                    View view = linearLayout.findViewWithTag(i++);
                    if (view == null) {
                        continue;
                    }

                    TextView timeText = (TextView) view.findViewById(R.id.userSharedTimeTextView);
                    TextView unitText = (TextView) view.findViewById(R.id.userSharedTimeUnitTextView);
                    CommonManager.setTime2(timeText, unitText, review.createTime.getTime());

                    ImageView opinionImage = (ImageView) view.findViewById(R.id.opinionImage);
                    opinionImage.setImageResource(review.opinion == 1 ? R.drawable.ic_good_movie1 : R.drawable.ic_bad_movie1);

                    TextView titleText = (TextView) view.findViewById(R.id.contentText);
                    titleText.setText(review.title);

                    final long id = review.cinecismID;
                    view.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            startFilmReviewDetailActivity(id);
                        }
                    });
                }
            }
        }
        else {
            linearLayout.setVisibility(View.GONE);
        }

    }

    /**
     * 启动影评人详情界面
     */
    private void startCriticDetailActivity()
    {
        Intent intent = new Intent(mActivity, CriticDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mActivity.startActivity(intent);
    }

    /**
     * 启动影评详情页面
     */
    private void startFilmReviewDetailActivity(long id)
    {
        Intent intent = new Intent(mActivity, FilmReviewDetailActivity.class);
        intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mActivity.startActivity(intent);
    }
}
