package com.dym.film.utils;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EffectAnim {
	private long duration;
	// private float fromAlpha;
	private float fromXScale;
	private float toXScale;
	private float fromYScale;
	// private float toAlpha;
	private float toYScale;

	public Animation alphaAnimation(float paramFloat1, float paramFloat2, long paramLong1, long paramLong2) {
		AlphaAnimation localAlphaAnimation = new AlphaAnimation(paramFloat1, paramFloat2);
		localAlphaAnimation.setDuration(paramLong1);
		localAlphaAnimation.setStartOffset(paramLong2);
		localAlphaAnimation.setInterpolator(new AccelerateInterpolator());
		return localAlphaAnimation;
	}

	public Animation rotateAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY,long duration) {

		RotateAnimation rotateAnimation=new RotateAnimation(fromDegrees, toDegrees,pivotX,pivotY);
		rotateAnimation.setDuration(duration);
		rotateAnimation.setInterpolator(new AccelerateInterpolator());
		rotateAnimation.setInterpolator(new LinearInterpolator());
		return rotateAnimation;
	}


	public static void rotateAnimate(ImageView image){
		/** 设置旋转动画 */
		image.clearAnimation();
		RotateAnimation animation  = new RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(200);//设置动画持续时间
			animation.setInterpolator(new AccelerateInterpolator());
			animation.setFillEnabled(true);
			animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
			image.startAnimation(animation);
	}
	public static void rotateAnimate1(ImageView image){
		/** 设置旋转动画 */
		image.clearAnimation();
		RotateAnimation animation  = new RotateAnimation(180f, 360f, Animation.RELATIVE_TO_SELF,
					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(100);//设置动画持续时间
			animation.setInterpolator(new AccelerateInterpolator());
			animation.setFillEnabled(true);
			animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
			image.startAnimation(animation);
	}


	public Animation createAnimation() {
		ScaleAnimation localScaleAnimation = new ScaleAnimation(this.fromXScale, this.toXScale, this.fromYScale,
				this.toYScale, 1, 0.5F, 1, 0.5F);
		localScaleAnimation.setFillAfter(true);
		localScaleAnimation.setInterpolator(new AccelerateInterpolator());
		localScaleAnimation.setDuration(this.duration);
		// localScaleAnimation.setFillAfter(true);
		return localScaleAnimation;
	}

	public void setAttributs(float fromX, float toX, float formY, float toY, long duration) {
		this.fromXScale = fromX;
		this.toXScale = toX;
		this.fromYScale = formY;
		this.toYScale = toY;
		this.duration = duration;
	}
	/**
	 * 缩小
	 * @param view
	 */
	public void showLooseFocusAinimation(View view) {
		view.setPadding(0, 0, 0, 0);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			this.setAttributs(1.1F, 1.0F, 1.1F, 1.0F, 100L);
			view.startAnimation(this.createAnimation());
		}else{
			PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1.0f);  
			PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1.0f);
			ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).setDuration(100L).start(); 
		}
		
	}
	/**
	 * 放大
	 * @param view
	 */
	public void showOnFocusAnimation(boolean isNext, View view) {
		view.setPadding(22, 0, 22, 0);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			this.setAttributs(1.0F, 1.1F, 1.0F, 1.1F, 100L);
			view.startAnimation(this.createAnimation());
		}else{
			PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1.49f);
			PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1.49f);
			ObjectAnimator.ofPropertyValuesHolder(view, pvhX, pvhY).setDuration(100L).start();
		}
	}

}
