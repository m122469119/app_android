package com.dym.film.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.search.SearchActivity;
import com.dym.film.ui.viewpagerindicator.FixPageIndicator;
import com.dym.film.ui.viewpagerindicator.PageIndicator;
import com.dym.film.ui.viewpagerindicator.scrollbar.ColorBar;
import com.dym.film.utils.DimenUtils;
import com.dym.film.views.CustomTypefaceTextView;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/22
 */
public class FilmReviewHomeFragment extends Fragment
{
    private final static String [] TABS = {"影评", "影评人"};

    private Activity mActivity = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_film_review_home, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view)
    {
        ViewPager pager = (ViewPager) view.findViewById(R.id.view_pager);

        final FilmReviewFragment review = new FilmReviewFragment();
        final FilmReviewerFragment reviewer = new FilmReviewerFragment();
        review.setViewPager(pager);
        reviewer.setViewPager(pager);

        pager.setAdapter(new FragmentPagerAdapter(getFragmentManager())
        {
            @Override
            public Fragment getItem(int position)
            {
                return position == 0 ? review : reviewer;
            }

            @Override
            public int getCount()
            {
                return 2;
            }
        });

        FixPageIndicator indicator = (FixPageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setIndicatorAdapter(new PageIndicator.IndicatorAdapter()
        {
            @Override
            public View getIndicatorView(int position)
            {
                CustomTypefaceTextView textView=new CustomTypefaceTextView(mActivity);
                textView.setTextColor(getResources().getColorStateList(R.color.film_type_tab_text_color));
                textView.setText(TABS[position]);
                textView.setTextSize(12);
                textView.setGravity(Gravity.CENTER);
                return textView;
            }

            @Override
            public void onPageScrolled(View view, int position, float selectPercent)
            {

            }
        });
        ColorBar colorBar = new ColorBar(mActivity, 0xffb10b0b);
        colorBar.setRadius(DimenUtils.dp2px(mActivity, 3));
        indicator.setScrollBar(colorBar);


        view.findViewById(R.id.fragment_filmReview_search).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(mActivity, SearchActivity.class);
                it.putExtra(SearchActivity.SearchTYPE, SearchActivity.REVIEW);
                mActivity.startActivity(it);
            }
        });
    }
}
