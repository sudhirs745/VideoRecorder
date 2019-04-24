package com.erlei.videorecorder.fragment;

import android.annotation.SuppressLint;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.erlei.multipartrecorder.MultiPartRecorder;
import com.erlei.multipartrecorder.widget.MultiPartRecorderView;
import com.erlei.videorecorder.BottomSheetFragment;
import com.erlei.videorecorder.BuildConfig;
import com.erlei.videorecorder.R;
import com.erlei.videorecorder.VideoPlayer;
import com.erlei.videorecorder.camera.Camera;
import com.erlei.videorecorder.camera.Size;
import com.erlei.videorecorder.effects.CanvasOverlayEffect;
import com.erlei.videorecorder.effects.EffectsManager;
import com.erlei.videorecorder.recorder.CameraController;
import com.erlei.videorecorder.recorder.DefaultCameraPreview;
import com.erlei.videorecorder.recorder.ICameraPreview;
import com.erlei.videorecorder.recorder.IVideoRecorder;
import com.erlei.videorecorder.recorder.VideoRecorder;
import com.erlei.videorecorder.recorder.VideoRecorderHandler;
import com.erlei.videorecorder.util.FPSCounterFactory;
import com.erlei.videorecorder.util.LogUtil;
import com.erlei.videorecorder.util.RecordGestureDetector;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class MultiPartRecorderFragment extends Fragment implements SettingsDialogFragment.CameraControllerView {

    private TextureView mTextureView;
    private MultiPartRecorder mRecorder;
    private View mBtnRecord;
    private TextView mTvFps;
    private MultiPartRecorderView mRecorderIndicator;
    private CameraController mCameraController;
    private EffectsManager mEffectsManager;
    private TextView addSound;

    public  String  tempAudioFile;
    public  static int AUDIO_REQUEST =1;

    public  MediaPlayer mediaPlayer ;

    public static MultiPartRecorderFragment newInstance() {
        return new MultiPartRecorderFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multi_part_recorder, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = view.findViewById(R.id.textureView);
        mBtnRecord = view.findViewById(R.id.cbRecord);
        mTvFps = view.findViewById(R.id.tvFps);
        addSound= view.findViewById(R.id.add_sound);
        mRecorderIndicator = view.findViewById(R.id.recorderIndicator);
        mRecorderIndicator.setMaxDuration(60 * 3);
        mRecorderIndicator.setRecordListener(new MultiPartRecorderView.RecordListener() {

            /**
             * More than the minimum recording time
             */
            @Override
            public void onOvertakeMinTime() {

            }


            @Override
            public void onOvertakeMaxTime(ArrayList<MultiPartRecorderView.Part> parts) {
                disableRecordButton();
                stopRecord();
                mBtnRecord.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        enableRecordButton();
                    }
                }, 200);
            }


            @Override
            public void onDurationChange(long duration) {

            }
        });

        addSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent videoIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(videoIntent, "Select Audio"), AUDIO_REQUEST);

            }
        });
        mBtnRecord.setOnTouchListener(new View.OnTouchListener() {
            private final RecordGestureDetector mGestureDetector = new RecordGestureDetector(new RecordGestureDetector.SimpleOnGestureListener() {
                private static final String TAG = "TouchGestureDetector";

                @Override
                public void onLongPressDown(View view, MotionEvent e) {
                    LogUtil.logd(TAG, "onLongPressDown");
                    view.animate().scaleX(0.8f).scaleY(0.8f).setDuration(200).start();
                    view.setSelected(true);
                    startRecord();
                  if(mediaPlayer!=null){
                    mediaPlayer.start();
                  }

                }

                @Override
                public void onLongPressUp(View view, MotionEvent e) {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    view.setSelected(false);
                    stopRecord();

                    if(mediaPlayer!=null){
                        mediaPlayer.pause();
                    }

                    LogUtil.logd(TAG, "onLongPressUp");
                }

                @Override
                public void onSingleTap(View view, MotionEvent e) {
                    LogUtil.logd(TAG, "onSingleTap " + System.currentTimeMillis());
                    mRecorder.takePicture(new IVideoRecorder.TakePictureCallback() {
                        @Override
                        public void onPictureTaken(File picture) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            if (Build.VERSION.SDK_INT >= 24) {
                                intent.setDataAndType(FileProvider.getUriForFile(getContext().getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", picture), "image/*");
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                intent.setDataAndType(Uri.fromFile(picture), "image/*");
                            }
                            startActivity(intent);
                        }

                        @Override
                        public File onPictureTaken(Bitmap bitmap) {
                            return new File(getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES), System.currentTimeMillis() + ".png");
                        }
                    });
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(v, event);
            }
        });
        view.findViewById(R.id.ivNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRecorderIndicator.removeAllPart();
                AsyncTask<MultiPartRecorder.Part, Float, File> task = mRecorder.mergeVideoParts();


            }
        });
        view.findViewById(R.id.ivRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRecorderIndicator.lastPartRemoved()) {
                    if (!mRecorder.isRecordEnable()) {
                        mRecorderIndicator.markLastPartRemove();
                    }
                } else {
                    mRecorderIndicator.removeLastPart();
                    mRecorder.removeLastPart();
                }

            }
        });

        TextView tv_beauty = view.findViewById(R.id.tv_beauty);
        tv_beauty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterDialogFragment filterDialogFragment =null;

                if (filterDialogFragment != null && filterDialogFragment.isVisible()) {
                    filterDialogFragment.dismiss();
                    filterDialogFragment = null;
                }
                filterDialogFragment = FilterDialogFragment.newInstance();
                filterDialogFragment.show(getChildFragmentManager(), SettingsDialogFragment.class.getName());




//                BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheetFragment();
//                bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

            }
        });





        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                mRecorder.onSizeChanged(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //Back to the background may not call onSurfaceTextureDestroyed this method
                // ,If you open another camera app later, it will cause the preview to not open when you re-enter
                stopPreview();
                surface.release();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        });

        ((CheckBox) view.findViewById(R.id.cbToggleFacing)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCameraController.setFacing(isChecked ? android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT : android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        });

        view.findViewById(R.id.tv_filter).setOnClickListener(new View.OnClickListener() {

            private SettingsDialogFragment mSettingsDialogFragment;

            @Override
            public void onClick(View v) {
                if (mSettingsDialogFragment != null && mSettingsDialogFragment.isVisible()) {
                    mSettingsDialogFragment.dismiss();
                    mSettingsDialogFragment = null;
                }
                mSettingsDialogFragment = SettingsDialogFragment.newInstance();
                mSettingsDialogFragment.show(getChildFragmentManager(), SettingsDialogFragment.class.getName());
            }
        });

        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCameraController.setFocusAreaOnTouch(event);
                mCameraController.setMeteringAreaOnTouch(event);
                mCameraController.setZoomOnTouch(event);
                return true;
            }
        });
    }

    private void stopPreview() {
        mRecorder.stopPreview();
    }

    private void startPreview() {
        if (mRecorder == null) initRecorder();
        mRecorder.startPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.getSurfaceTexture() != null) {
            startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPreview();
    }

    public void disableRecordButton() {
        mBtnRecord.setSelected(false);
        mBtnRecord.clearAnimation();
        mBtnRecord.setScaleX(1);
        mBtnRecord.setScaleY(1);
        mBtnRecord.setClickable(false);
    }

    public void enableRecordButton() {
        mBtnRecord.setSelected(false);
        mBtnRecord.clearAnimation();
        mBtnRecord.setScaleX(1);
        mBtnRecord.setScaleY(1);
        mBtnRecord.setClickable(true);
    }

    private void stopRecord() {
        if (mRecorder != null && mRecorder.isRecordEnable()) {
            mRecorder.setRecordEnabled(false);
            mRecorderIndicator.stopRecord();
        }
    }

    private void startRecord() {
        if (mRecorder != null && !mRecorder.isRecordEnable()) {
            mRecorder.setRecordEnabled(true);
            mRecorderIndicator.startRecord(mRecorder.getCurrentPartFile());
        }
    }


    private void initRecorder() {
        ICameraPreview cameraPreview = new DefaultCameraPreview(mTextureView);
//        ICameraPreview cameraPreview = new OffscreenCameraPreview(getContext(), 1920, 1920); //Off-screen recording

        Camera.CameraBuilder cameraBuilder = new Camera.CameraBuilder(getActivity())
                .useDefaultConfig()
                .setFacing(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
                .setPreviewSize(new Size(2048, 1536))
                .setRecordingHint(true)
                .setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        //Video effects manager
        mEffectsManager = new EffectsManager();
        mEffectsManager.addEffect(new CanvasOverlayEffect() {
            private FPSCounterFactory.FPSCounter1 mCounter;
            Paint mPaint;

            @Override
            public void prepare(Size size) {
                super.prepare(size);
                mPaint = new Paint();
                mPaint.setColor(Color.YELLOW);
                mPaint.setAlpha(230);
                mPaint.setTextSize(40);
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setAntiAlias(true);

                mCounter = new FPSCounterFactory.FPSCounter1();
            }

            @Override
            protected void drawCanvas(Canvas canvas) {
                //canvas.drawText(String.format(Locale.getDefault(), "%.2f", mCounter.getFPS()), canvas.getWidth() / 2, canvas.getHeight() / 2, mPaint);
            }
        });

        VideoRecorder.Builder builder = new VideoRecorder.Builder(cameraPreview)
                .setCallbackHandler(new CallbackHandler())
                .setLogFPSEnable(false)
                .setCameraBuilder(cameraBuilder)
                .setDrawTextureListener(mEffectsManager)
                .setOutPutPath(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), File.separator + "VideoRecorder").getAbsolutePath())
                .setFrameRate(30)
                .setChannelCount(1);

        //Segment recording, back to deletion support
        MultiPartRecorder.Builder multiBuilder = new MultiPartRecorder.Builder(builder);
        mRecorder = multiBuilder
                .addPartListener(new MultiPartRecorder.VideoPartListener() {
                    @Override
                    public void onRecordVideoPartStarted(MultiPartRecorder.Part part) {
                        LogUtil.loge("onRecordVideoPartStarted \t" + part.toString());
                    }

                    @Override
                    public void onRecordVideoPartSuccess(MultiPartRecorder.Part part) {
                        LogUtil.loge("onRecordVideoPartSuccess \t" + part.toString());
                    }

                    @Override
                    public void onRecordVideoPartFailure(MultiPartRecorder.Part part) {
                        LogUtil.loge("onRecordVideoPartFailure \t" + part.file);
                        mRecorderIndicator.removePart(part.file.getAbsolutePath());
                        mRecorder.removePart(part.file.getAbsolutePath());
                    }
                })
                .setMergeListener(new MultiPartRecorder.VideoMergeListener() {
                    @Override
                    public void onStart() {
                        LogUtil.loge("merge onStart");
                    }

                    @Override
                    public void onSuccess(File outFile) {

                        LogUtil.loge("merge Success \t" +  outFile.getAbsolutePath());

                        LogUtil.loge("merge Success \t" + outFile);

                        if(mediaPlayer!=null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer=null;
                        }

                        Intent intent = new Intent(getActivity(), VideoPlayer.class);
                        intent.putExtra("file_path",outFile.getAbsolutePath());
                        startActivity(intent);

//                        Intent intent = new Intent(Intent.ACTION_VIEW);
//                        if (Build.VERSION.SDK_INT >= 24) {
//                            intent.setDataAndType(FileProvider.getUriForFile(getContext().getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", outFile), "video/*");
//                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        } else {
//                            intent.setDataAndType(Uri.fromFile(outFile), "video/*");
//                        }
//                        startActivity(intent);


                    }

                    @Override
                    public void onError(Exception e) {
                        LogUtil.logd("merge Error \t" + e.toString());
                    }

                    /**
                     * Merger progress
                     *
                     * @param value 0 - 1
                     */
                    @Override
                    public void onProgress(float value) {
                        LogUtil.logd("merge onProgress \t" + value);
                    }

                })
                .setFileFilter(new MultiPartRecorder.FileFilter() {
                    @Override
                    public boolean filter(MultiPartRecorder.Part part) {
                        return part.duration > 1500;
                    }
                })
                .build();

        mCameraController = mRecorder.getCameraController();
    }

    @Override
    public CameraController getCameraController() {
        return mCameraController;
    }

    @Override
    public void setPreviewSize(Size item) {
        if (item.equals(mCameraController.getCameraSize())) return;
        //Reset the camera's preview resolution and call onSizeChanged to update the rendered texture coordinates.
        Camera.CameraBuilder cameraBuilder = mCameraController.getCameraBuilder();
        cameraBuilder.setPreviewSize(item);
        mCameraController.setCameraBuilder(cameraBuilder);
        Size surfaceSize = mCameraController.getSurfaceSize();
        mCameraController.closeCamera();
        mCameraController.openCamera(mRecorder.getPreviewTexture());
        mRecorder.onSizeChanged(surfaceSize.getWidth(), surfaceSize.getHeight());
    }


    @SuppressLint("HandlerLeak")
    private class CallbackHandler extends VideoRecorderHandler {

        private Toast mToast;

        @Override
        protected void handleUpdateFPS(float fps) {
            //   mTvFps.setText(String.format(Locale.getDefault(), "%.2f", fps));
        }

        @SuppressLint("ShowToast")
        @Override
        protected void handleVideoMuxerStopped(String output) {
            if (mToast != null) {
                mToast.setText("1");
            } else {
                mToast = Toast.makeText(getContext(), "1", Toast.LENGTH_SHORT);
            }
            mToast.show();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_REQUEST && null != data) {
            if (requestCode == AUDIO_REQUEST) {

                Uri uri = data.getData();
                try {

                    String uriString = uri.toString();
//                    File myFile = new File(uriString);
//                    //    String path = myFile.getAbsolutePath();
                    String displayName = null;
                    String path2 = getAudioPath(uri);
                    tempAudioFile=path2;

                   // String filePath = Environment.getExternalStorageDirectory()+tempAudioFile;

                    if(tempAudioFile!=null) {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(tempAudioFile);
                        mediaPlayer.prepare();
                      //  mediaPlayer.start();
                    }

                    Log.e("url path2" , " data -- " +path2);
                    File f = new File(path2);

                    Log.e("url details" , " data -- " +f.getAbsolutePath());

                } catch (Exception e) {
                    //handle exception
                    Toast.makeText(getActivity(), "Unable to process,try again", Toast.LENGTH_SHORT).show();
                }
                //   String path1 = uri.getPath();

            }
        }

    }

    private String getAudioPath(Uri uri) {
        String[] data = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();

        Log.e("url getAudioPath-- " ,cursor.getString(column_index));
        return cursor.getString(column_index);
    }

}

