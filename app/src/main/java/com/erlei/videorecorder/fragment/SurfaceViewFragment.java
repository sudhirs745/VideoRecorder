package com.erlei.videorecorder.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.erlei.videorecorder.R;
import com.erlei.videorecorder.camera.Camera;
import com.erlei.videorecorder.camera.Size;
import com.erlei.videorecorder.util.LogUtil;


public class SurfaceViewFragment extends Fragment implements SurfaceHolder.Callback, Camera.CameraCallback {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    public static SurfaceViewFragment newInstance() {
        return new SurfaceViewFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_surface, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSurfaceView = view.findViewById(R.id.SurfaceView);
        mSurfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = new Camera.CameraBuilder(getContext()).useDefaultConfig().setSurfaceHolder(holder).addPreviewCallback(this).build().open();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) mCamera.close();
    }


    @Override
    public void onSuccess(Camera camera) {
        adjustSurfaceViewSize(camera.getPreviewSize());
    }

    @Override
    public void onFailure(int code, String msg) {
        LogUtil.loge(msg);
    }

    /**
           * Adjust the size ratio of the SurfaceView to avoid preview distortion
           *
           * @param previewSize preview size
           */

    private void adjustSurfaceViewSize(Size previewSize) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width * ((previewSize.getWidth() * 1.0f) / previewSize.getHeight()));
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mSurfaceView.setLayoutParams(lp);
    }


}
