package com.dym.film.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.activity.sharedticket.TicketShareActivity;
import com.dym.film.controllers.ShareTicketDialogViewController;
import com.dym.film.controllers.SharedTicketViewController;
import com.dym.film.utils.LogUtils;

public class SharedTicketFragment extends Fragment
{
    private SharedTicketViewController mController = null;
    private MainActivity mActivity = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = mController == null ? null : mController.getRootView();
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_share_ticket, container, false);
            mController = new SharedTicketViewController(mActivity, rootView);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void onResume()
    {
        super.onResume();

        mController.onResume();
    }

    public void onUserStateChanged(BaseViewCtrlActivity.UserState state)
    {
        mController.onUserStateChanged(state);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        if (mController != null) {
            mController.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        LogUtils.e("ShareTicketFragment", "request: " + requestCode + " result: " + resultCode + " Data: " + (data == null ? "null" : data.toString()));

        switch (requestCode) {
            case ShareTicketDialogViewController.REQUEST_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Intent intent = new Intent(mActivity, TicketShareActivity.class);
                        intent.setData(uri);

                        startActivity(intent);
                        return;
                    }
                }

                break;

            case ShareTicketDialogViewController.REQUEST_CODE_CAPTURE_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = ShareTicketDialogViewController.getCameraImageUri();
                    if (imageUri != null) {
                        mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                        Intent intent = new Intent(mActivity, TicketShareActivity.class);
                        intent.setData(imageUri);

                        startActivity(intent);
                    }
                }

                ShareTicketDialogViewController.onActivityFinished();
                break;
        }
    }

}