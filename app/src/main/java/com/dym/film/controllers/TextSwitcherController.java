package com.dym.film.controllers;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.dym.film.R;
import com.dym.film.manager.CommonManager;
import com.dym.film.views.CustomTypefaceTextView;

/**
 * 基础view控制类
 */
public  class TextSwitcherController
{
    private TextSwitcher tvTextSwitcher;
    private int maxCount=5;
    private int finalIndex;
    private int averIndex;
    private int curCount;

    public TextSwitcherController(final TextSwitcher tvTextSwitcher, final Context context){
       this.tvTextSwitcher=tvTextSwitcher;
       //定义视图显示工厂，并设置
       tvTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

           public View makeView() {
               TextView tv =new CustomTypefaceTextView(context);
               if (CommonManager.mNumberTypeface == null) {
                   CommonManager.mNumberTypeface = Typeface.createFromAsset(context.getAssets(), CommonManager.FONT_NUMBER);
               }
               tv.setTypeface(CommonManager.mNumberTypeface);
               tv.setTextColor(Color.WHITE);
               tv.setTextSize(30);
               return tv;
           }
       });
       tvTextSwitcher.setCurrentText("0");//开始显示的默认文字，静态的，不会有动画切入效果
       Animation amAnimationIn=AnimationUtils.loadAnimation(context, R.anim.tv_in_from_top);
       Animation amAnimationOut=AnimationUtils.loadAnimation(context, R.anim.tv_out_from_bottom);
       // 设置切入动画
       tvTextSwitcher.setInAnimation(amAnimationIn);
       // 设置切出动画
       tvTextSwitcher.setOutAnimation(amAnimationOut);

       amAnimationIn.setAnimationListener(new Animation.AnimationListener()
       {
           @Override
           public void onAnimationStart(Animation animation)
           {
               curCount++;
               if (curCount==maxCount){
                   tvTextSwitcher.setText(finalIndex+"");
               }else if(curCount<maxCount){
                   tvTextSwitcher.setText(averIndex*curCount+"");
               }
           }

           @Override
           public void onAnimationEnd(Animation animation)
           {
              if(curCount<maxCount){
                   tvTextSwitcher.setText(averIndex*curCount+"");
               }
           }

           @Override
           public void onAnimationRepeat(Animation animation)
           {

           }
       });

   }
    public void setText(int index){
        finalIndex=index;
        averIndex=index/maxCount;
        curCount=1;
        tvTextSwitcher.setText(averIndex*curCount+"");
    }
    public void setText(String text){
        tvTextSwitcher.setCurrentText(text);
        TextView tv= (TextView) tvTextSwitcher.getCurrentView();
        tv.setTextSize(25);
    }

}
