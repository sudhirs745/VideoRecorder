package com.erlei.videorecorder.categoryHashtag.buttondilog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.erlei.videorecorder.FilterAdapter;
import com.erlei.videorecorder.FilterInterface;
import com.erlei.videorecorder.FilterModel;
import com.erlei.videorecorder.R;
import com.erlei.videorecorder.categoryHashtag.Model.HashTagModel;

import java.util.ArrayList;

public class HashtagBottomSheetFragment extends BottomSheetDialogFragment implements  FilterInterface{

    ArrayList  Number;

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        //Get the content View
        View contentView = View.inflate(getContext(), R.layout.hash_tag_bottom_sheet, null);
        dialog.setContentView(contentView);
        RecyclerView recyclerView = contentView.findViewById(R.id.recycleview);
        ArrayList<HashTagModel> hashTagModels = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        for (int i = 0; i < 8; i++) {
            HashTagModel  hashTagModel = new HashTagModel("F "+(i+1),"dsf" );
            hashTagModels.add(hashTagModel);
        }
        //recyclerView.setAdapter(new FilterAdapter(filterModels, getActivity(),this ));

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }


    }


    @Override
    public void onItemClick(String item , String type) {

      //  Camera.Parameters cameraParameters = mCameraController.getCameraParameters();
        Toast.makeText(getActivity(),item+" ",Toast.LENGTH_LONG).show();

    }
}
