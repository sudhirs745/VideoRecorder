package com.erlei.videorecorder.fragment;


import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import com.erlei.videorecorder.FilterAdapter;
import com.erlei.videorecorder.FilterInterface;
import com.erlei.videorecorder.FilterModel;
import com.erlei.videorecorder.R;
import com.erlei.videorecorder.camera.FpsRange;
import com.erlei.videorecorder.camera.Size;
import com.erlei.videorecorder.recorder.CameraController;

import java.util.ArrayList;
import java.util.List;

public class FilterDialogFragment extends BottomSheetDialogFragment  implements SettingsDialogFragment.CameraControllerView , FilterInterface {

    private CameraController mCameraController;
    private ArrayAdapter<Size> mResolutionAdapter;
    private SettingsDialogFragment.CameraControllerView mCameraControllerView;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(getParentFragment() instanceof SettingsDialogFragment.CameraControllerView)) {
            throw new IllegalStateException("Attached getParentFragment are not implemented SettingsDialogFragment.CameraControllerView");
        }
        mCameraControllerView = ((SettingsDialogFragment.CameraControllerView) getParentFragment());
        mCameraController = mCameraControllerView.getCameraController();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        if (window == null) return;
        window.requestFeature(Window.FEATURE_NO_TITLE);
        super.onActivityCreated(savedInstanceState);
//        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_part_recorder, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //findViews(view);

        RecyclerView recyclerView = view.findViewById(R.id.recycleview);
        ArrayList<FilterModel> filterModels = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        Camera.Parameters cameraParameters = mCameraController.getCameraParameters();

           cameraParameters.getSupportedWhiteBalance();


        for (String  name  : cameraParameters.getSupportedWhiteBalance()) {
            FilterModel category = new FilterModel(name, R.mipmap.header_icon_1);
            filterModels.add(category);
        }
        recyclerView.setAdapter(new FilterAdapter(filterModels, getActivity(),this ));



       // mResolutionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, mCameraController.getSupportedPreviewSizes());
      //  Camera.Parameters cameraParameters = mCameraController.getCameraParameters();


    }

//    private void initZoomSeekBar() {
//        Camera.Parameters cameraParameters = mCameraController.getCameraParameters();
//        int maxZoom = cameraParameters.getMaxZoom();
//        mSbZoom.setMax(maxZoom);
//        mSbZoom.setProgress(cameraParameters.getZoom());
//        mSbZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (fromUser) {
//                    mCameraController.startSmoothZoom(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//
//    }



    private void initSpinner(final AppCompatSpinner sp, List<String> list, String key, final Callback callback) {
        if (list == null) return;
        Camera.Parameters cameraParameters = mCameraController.getCameraParameters();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list);
        sp.setAdapter(adapter);
        sp.setSelection(adapter.getPosition(cameraParameters.get(key)), false);
        sp.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                callback.set(adapter.getItem(position));
            }
        });
    }

    @Override
    public CameraController getCameraController() {
        return null;
    }

    @Override
    public void setPreviewSize(Size item) {

    }


    @Override
    public void onItemClick(int item) {
        Toast.makeText(getActivity(),item+" ",Toast.LENGTH_LONG).show();
        Camera.Parameters cameraParameters = mCameraController.getCameraParameters();
      //  Log.e("f_res" , cameraParameters.get("cloudy-daylight") ) ;
        mCameraController.setWhiteBalance(cameraParameters.getSupportedWhiteBalance().get(item));

    }

    private interface Callback {
        void set(String key);
    }



    public static FilterDialogFragment newInstance() {
        return new FilterDialogFragment();
    }



    public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
