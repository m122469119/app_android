package com.dym.film.ui;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.dym.film.R;

public class CustomDialog extends Dialog {

	Context context;
	public CustomDialog(Context context){
		this(context, R.style.default_dialog);
	}
	public CustomDialog(Context context, int themeResId) {
		super(context, themeResId);
		this.context=context;
		init();
	}
	private void init() {
		// TODO Auto-generated method stub
		 
		 setCancelable(true); 
	}
	public void show(int gravity ){
		DisplayMetrics d = context.getResources().getDisplayMetrics();
		show(gravity, (int) (d.widthPixels * 0.8));
	}
	public void show(int gravity ,int width){
		super.show();
		Window window = getWindow();
		//window.setGravity(Gravity.BOTTOM);
		window.setGravity(gravity);
		window.setLayout(width, LayoutParams.WRAP_CONTENT);
	}

	public void setGravity(int gravity){
		 getWindow().setGravity(gravity);
	}
	public void setLayout(int width, int height){
		
		getWindow().setLayout(width, height);
		
	}
	public void setWindowAnimations( int resId){
		getWindow().setWindowAnimations(resId);
	}
	
}
