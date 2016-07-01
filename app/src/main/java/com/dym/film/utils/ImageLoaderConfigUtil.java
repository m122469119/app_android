package com.dym.film.utils;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ImageLoaderConfigUtil {
	/**
	 * 设置图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param url
	 *            图片链接
	 * @param flag
	 *            是否缓存
	 */
	public static void setDisplayImager(final int Res, ImageView imageView,String url, boolean flag) {

		if (TextUtils.isEmpty(url)) {
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageResource(Res);
			return;
		}
		ImageLoader.getInstance().displayImage(url, imageView, new SimpleImageLoadingListener()
		{
			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				super.onLoadingStarted(imageUri, view);
				((ImageView) view).setScaleType(ScaleType.FIT_CENTER);
				((ImageView) view).setImageResource(Res);
			}

			@Override
			public void onLoadingCancelled(String s, View view)
			{
			}
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				super.onLoadingComplete(imageUri, view, loadedImage);
				if(view!=null) {
					ImageView imageView = (ImageView) view;
					((ImageView) view).setScaleType(ScaleType.FIT_XY);
					imageView.setImageBitmap(loadedImage);
				}
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
				super.onLoadingFailed(imageUri, view, failReason);
			}
		});
	}
	

	/**
	 * 设置圆形图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param url
	 *            图片链接
	 * @param flag
	 *            是否缓存
	 */
	public static void setRouteDisplayImager(int Res, ImageView imageView,String url, boolean flag) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(Res)
		// .decodingOptions(setOptions(imageView))
		.cacheInMemory(true)
		.cacheOnDisk(flag).bitmapConfig(Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(200))
		.build();
		if (StringUtils.isEmpty(url)) {
			url = Scheme.DRAWABLE.wrap(""+ Res);
		}
		ImageLoader.getInstance().displayImage(url, imageView, options);
	}
	/**
	 * 设置本地图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param path
	 *            图片路径
	 * @param flag
	 *            是否缓存
	 */
	public static void setLoaclImager(int Res,ImageView imageView,String path,boolean flag){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(Res)
//		.decodingOptions(null)
//		.imageScaleType(ImageScaleType.NONE)
//		.imageScaleType(ImageScaleType.NONE_SAFE)
//		.imageScaleType(ImageScaleType.EXACTLY)
//		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
//		.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
		.cacheInMemory(true)
		.cacheOnDisk(flag).bitmapConfig(Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(1))
		.build();
		if (StringUtils.isEmpty(path)) {
			path = Scheme.DRAWABLE.wrap(""+ Res);
		}else{
			path=Scheme.FILE.wrap(path);
		}
		ImageLoader.getInstance().displayImage(path, imageView, options);
	};
	
	/**
	 * 设置模糊图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param path
	 *            图片路径
	 * @param flag
	 *            是否缓存
	 */
	 static Bitmap bitmap1=null;
	public static void setBlurImager(final Activity context,final int Res,final ImageView imageView,String path,boolean flag){

		imageView.setAlpha(0.2f);
//		if(bitmap1!=null)
//		imageView.setImageBitmap(bitmap1);
		ImageLoader.getInstance().displayImage(path, imageView,new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				((ImageView) view).setScaleType(ImageView.ScaleType.FIT_CENTER);
				((ImageView) view).setImageResource(Res);

			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
				// TODO Auto-generated method stub
				new Thread(new Runnable() {
					@Override
					public void run() {

						Bitmap bitmap = centerSquareScaleBitmap(loadedImage, DimenUtils.getScreenWidth(context)/8);
						bitmap1=BlurUtil.fastblur(context, bitmap, 12);
						context.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if(imageView!=null||bitmap1!=null) {
									imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
									imageView.setAlpha(0.5f);
									imageView.setImageBitmap(bitmap1);
								}
							}
						});
					}
				}).start();
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
		});
	};
	/**
	 * 设置本地图片
	 *
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param path
	 *            图片路径
	 * @param flag
	 *            是否缓存
	 */
	public static void setLoaclSizeImager(int Res,final ImageView imageView,String path,boolean flag){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(Res)
		.cacheInMemory(true)
		.imageScaleType(ImageScaleType.NONE)

		.cacheOnDisk(flag).bitmapConfig(Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(1))
		.build();
		if (StringUtils.isEmpty(path)) {
			path = Scheme.DRAWABLE.wrap(""+ Res);
		}else{
			path=Scheme.FILE.wrap(path);
		}
		ImageLoader.getInstance().displayImage(path, imageView, options,new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				// TODO Auto-generated method stub
				imageView.setScaleType(ScaleType.CENTER_INSIDE);
//				imageView.setImageBitmap(centerSquareScaleBitmap(loadedImage, AttriExtractor.getScreenWidth()/2));
			}

			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub

			}
		});
	};
	/**
	 * 设置下载本地图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param path
	 *            图片路径
	 * @param flag
	 *            是否缓存
	 */
	public static void setDownLoadLoaclSizeImager(int Res,final ImageView imageView,String path,boolean flag){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(Res)
		.cacheInMemory(true)
		.imageScaleType(ImageScaleType.NONE)
		
		.cacheOnDisk(flag).bitmapConfig(Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(1))
		.build();
		if (StringUtils.isEmpty(path)) {
			path = Scheme.DRAWABLE.wrap(""+ Res);
		}else{
			path=Scheme.FILE.wrap(path);
		}
		ImageLoader.getInstance().displayImage(path, imageView, options,new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				
			}
			   
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				// TODO Auto-generated method stub
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setImageBitmap(downLoadScaleBitmap(loadedImage, 164,92));
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
		});
	};
	/**
	 * 设置本地图片
	 * 
	 * @param Res
	 *            默认图片
	 * @param imageView
	 *            显示的控件
	 * @param path
	 *            图片路径
	 * @param flag
	 *            是否缓存
	 */
	public static void setLoaclSizeLargeImager(int Res,final ImageView imageView,String path,boolean flag){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(Res)
		.cacheInMemory(true)
		.imageScaleType(ImageScaleType.NONE)
		
		.cacheOnDisk(flag).bitmapConfig(Config.RGB_565)
		.displayer(new RoundedBitmapDisplayer(1))
		.build();
		if (StringUtils.isEmpty(path)) {
			path = Scheme.DRAWABLE.wrap(""+ Res);
		}else{
			path=Scheme.FILE.wrap(path);
		}
		ImageLoader.getInstance().displayImage(path, imageView, options,new ImageLoadingListener() {
			
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoadingFailed(String imageUri, View view,
					FailReason failReason) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				// TODO Auto-generated method stub
				imageView.setImageBitmap(loadedImage);
			}
			
			@Override
			public void onLoadingCancelled(String imageUri, View view) {
				// TODO Auto-generated method stub
				
			}
		});
	};
	
	//显示图的中间部分
	 public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength)
	  {
	   if(null == bitmap || edgeLength <= 0)
	   {
	    return  null;
	   }
	                                                                                 
	   int widthOrg = bitmap.getWidth();
	   int heightOrg = bitmap.getHeight();
	    Bitmap result = null;                                                                         
//	   if(widthOrg > edgeLength && heightOrg > edgeLength)
//	   {
	    //压缩到一个最小长度是edgeLength的bitmap
	    int longerEdge = (int)(edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
	    int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
	    int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
	    Bitmap scaledBitmap;
	          try{
	           scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
	          }
	          catch(Exception e){
	           return null;
	          }
	       //从图中截取正中间的正方形部分。
	       int xTopLeft = (scaledWidth - edgeLength) / 2;
	       int yTopLeft = (scaledHeight - edgeLength) / 2;
	                                                                                     
	       try{
	        result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
	        scaledBitmap.recycle();
	       }
	       catch(Exception e){
	        return null;
	       }       
	                                                                                      
	   return result;
	  }
    //下载的图片显示
	 public static Bitmap downLoadScaleBitmap(Bitmap bitmap, int edgeLength,int edgeHight)
	 {
		 if(null == bitmap || edgeLength <= 0)
		 {
			 return  null;
		 }
		 int widthOrg = bitmap.getWidth();
		 int heightOrg = bitmap.getHeight();
		 Bitmap result = null;                                                                         
	//压缩到一个最小长度是edgeLength的bitmap
		 int longerEdge = (int)(edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
		 int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
		 int scaledHeight = widthOrg > heightOrg ? edgeHight : longerEdge;
		 Bitmap scaledBitmap;
		 try{
			 scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
		 }
		 catch(Exception e){
			 return null;
		 }
		 //从图中截取正中间的正方形部分。
		 int xTopLeft =( scaledWidth- edgeLength) / 2;
		 int yTopLeft = (scaledHeight - edgeLength) / 2;
		 
		 try{
			 result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeHight);
			 scaledBitmap.recycle();
		 }
		 catch(Exception e){
			 return null;
		 }       
		 
		 return result;
	 }
	 
	 
	 
	/**
	 * 按照控件大小缩放图片显示比例
	 * 
	 * @param imageView
	 * @return
	 */
	public static Options setOptions(final ImageView imageView) {

		int width = imageView.getWidth();
		int height = imageView.getHeight();
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		// 计算图片缩放比例
		final int minSideLength = Math.min(width, height);
		opts.inSampleSize = computeSampleSize(opts, minSideLength, width
				* height);
		opts.inJustDecodeBounds = false;
		opts.inInputShareable = true;
		opts.inPurgeable = true;
		return opts;
	}

	public static int computeSampleSize(Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}
	/**
	 * 清除所有内存缓存图片
	 */
	public static void clearAllMemoryUrl() {
   		ImageLoader.getInstance().clearMemoryCache();
	}
	/**
	 * 清除所有磁盘缓存图片
	 */
	public static void clearAllDiskUrl() {
		ImageLoader.getInstance().clearDiskCache();
	}
	/**
	 * 清除所有图片
	 */
	public static void clearAllUrl() {
		ImageLoaderConfigUtil.clearAllMemoryUrl();
		ImageLoaderConfigUtil.clearAllDiskUrl();
	}
	
	
	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static  Bitmap toRoundBitmap(Bitmap bitmap) {
		if (bitmap!=null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			float roundPx;
			float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
			if (width <= height) {
				roundPx = width / 2;
				top = 0;
				bottom = width;
				left = 0;
				right = width;
				height = width;
				dst_left = 0;
				dst_top = 0;
				dst_right = width;
				dst_bottom = width;
			} else {
				roundPx = height / 2;
				float clip = (width - height) / 2;
				left = clip;
				right = width - clip;
				top = 0;
				bottom = height;
				width = height;
				dst_left = 0;
				dst_top = 0;
				dst_right = height;
				dst_bottom = height;
			}

			Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect src = new Rect((int) left, (int) top, (int) right,
					(int) bottom);
			final Rect dst = new Rect((int) dst_left, (int) dst_top,
					(int) dst_right, (int) dst_bottom);
			final RectF rectF = new RectF(dst);

			paint.setAntiAlias(true);

			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, src, dst, paint);
			return output;
		}else{
			return null;
		}
		
	}
	
	
	public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

	    public static   List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

	    @Override
	    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	        if (loadedImage != null) {
	            ImageView imageView = (ImageView) view;
	            boolean firstDisplay = !displayedImages.contains(imageUri);
	            if (firstDisplay) {
	                FadeInBitmapDisplayer.animate(imageView, 500);
	                displayedImages.add(imageUri);
	            }
	        }
	    }
	}
	
	/**
	 * 保存Logo
	 */
	// 将资源的图片保存到本地图片
		public static void savePic(Context context,int ResId,String filepath) {
			File pic = new File(filepath);
			if (!pic.exists()) {
				try {
					// 把资源文件转成bitmap
					Bitmap bitmap = BitmapFactory.decodeResource(
							context.getResources(), ResId);
					// 再转成字节数组
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int i = 100;
					bitmap.compress(Bitmap.CompressFormat.PNG, i, out);
					byte[] array = out.toByteArray();

					// 最后通过流在保存
					FileOutputStream fos = new FileOutputStream(pic);
					fos.write(array);
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO: handle exception
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();

				}

			}
		}
}
