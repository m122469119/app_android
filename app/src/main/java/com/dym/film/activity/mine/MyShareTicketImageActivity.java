/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.dym.film.activity.mine;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.MyShareTecketImageAdapter;
import com.dym.film.manager.NetworkManager;
import com.dym.film.views.HackyViewPager;

import java.util.ArrayList;

/**
 * Lock/Unlock button is added to the ActionBar.
 * Use it to temporarily disable ViewPager navigation in order to correctly interact with ImageView by gestures.
 * Lock/Unlock state of ViewPager is saved and restored on configuration changes.
 * 
 * Julia Zudikova
 */

public class MyShareTicketImageActivity extends BaseViewCtrlActivity
{
	private static final String ISLOCKED_ARG = "isLocked";
	private HackyViewPager mViewPager;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_share_ticket_image);


        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
		ArrayList<NetworkManager.SharedTicketRespModel> mMyShareTicketDatas= (ArrayList<NetworkManager.SharedTicketRespModel>) getIntent().getSerializableExtra("mMyShareTicketDatas");
		mViewPager.setAdapter(new MyShareTecketImageAdapter(this,mMyShareTicketDatas));
		mViewPager.setCurrentItem(getIntent().getIntExtra("index",0));
		if (savedInstanceState != null) {
			boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
			((HackyViewPager) mViewPager).setLocked(isLocked);
		}
	}

    private void toggleLockBtnTitle() {
    	boolean isLocked = false;
    	if (isViewPagerActive()) {
    		isLocked = ((HackyViewPager) mViewPager).isLocked();
    	}

    }

    private boolean isViewPagerActive() {
    	return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }
    
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (isViewPagerActive()) {
			outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
    	}
		super.onSaveInstanceState(outState);
	}

	public void doClick(View view){
		finish();
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}
}
