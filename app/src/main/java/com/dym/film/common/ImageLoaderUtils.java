package com.dym.film.common;


import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.utils.BlurUtil;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.ImageLoaderConfigUtil;
import com.dym.film.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class ImageLoaderUtils
{

	public static void displayImage(String url, ImageView imageView){
		displayImage( url,  imageView,  R.drawable.ic_default_loading_img);
	}
	public static void displayImage(String url, ImageView imageView, final int failResId)
	{
		if (TextUtils.isEmpty(url)) {
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageResource(failResId);
			return;
		}

		ImageLoader.getInstance().displayImage(url, imageView, new SimpleImageLoadingListener()
		{

			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				super.onLoadingStarted(imageUri, view);
				((ImageView) view).setScaleType(ImageView.ScaleType.FIT_CENTER);
				((ImageView) view).setImageResource(failResId);

			}

			@Override
			public void onLoadingCancelled(String s, View view)
			{

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				super.onLoadingComplete(imageUri, view, loadedImage);
				ImageView imageView = (ImageView) view;
				((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setImageBitmap(loadedImage);
				LogUtils.i("123", "width:" + view.getWidth());
				LogUtils.i("123", "height:" + view.getHeight());
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
				super.onLoadingFailed(imageUri, view, failReason);
				LogUtils.i("123", failReason.toString());
			}
		});
	}
	public static void displayImage(String url, ImageView imageView,int width,int height){
		displayImage(url, imageView, width, height, R.drawable.ic_default_loading_img);
	}
	public static void displayImage(String url, final ImageView imageView, int width,int height,final int failResId)
	{
		if (TextUtils.isEmpty(url)){
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageResource(failResId);
			return;
		}
//		LogUtils.i("123", "width0:" + imageView.getLayoutParams().width);
//		LogUtils.i("123", "height0:" + imageView.getLayoutParams().height);
		ImageSize mImageSize = new ImageSize(width, height);
		ImageLoader.getInstance().loadImage(url, mImageSize, new SimpleImageLoadingListener()
		{

			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				super.onLoadingStarted(imageUri, view);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setImageResource(failResId);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				super.onLoadingComplete(imageUri, view, loadedImage);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setImageBitmap(loadedImage);
				LogUtils.i("123", "width:" + loadedImage.getWidth());
				LogUtils.i("123", "height:" + loadedImage.getHeight());
			}

		});

	}

	public static void setBlurImager(final Activity context,final ImageView imageView,String url){

		if (TextUtils.isEmpty(url)){
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageResource(R.drawable.ic_default_loading_img);
			return;
		}
		ImageSize mImageSize = new ImageSize(200, 200);
		ImageLoader.getInstance().loadImage(url, mImageSize, new ImageLoadingListener()
		{

			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setImageResource(R.drawable.ic_default_loading_img);

			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage)
			{
				// TODO Auto-generated method stub
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{

						Bitmap bitmap = ImageLoaderConfigUtil.centerSquareScaleBitmap(loadedImage, DimenUtils.getScreenWidth(context) / 8);
						final Bitmap bitmap1 = BlurUtil.fastblur(context, bitmap, 12);
						context.runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								if (imageView != null || bitmap1 != null) {
									imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
									imageView.setImageBitmap(bitmap1);
								}
							}
						});
					}
				}).start();
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view)
			{
				// TODO Auto-generated method stub

			}
		});
	};
}
