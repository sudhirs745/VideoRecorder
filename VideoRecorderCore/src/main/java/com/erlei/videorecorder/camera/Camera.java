package com.erlei.videorecorder.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.erlei.videorecorder.camera.annotations.Antibanding;
import com.erlei.videorecorder.camera.annotations.ColorEffect;
import com.erlei.videorecorder.camera.annotations.Facing;
import com.erlei.videorecorder.camera.annotations.FlashModel;
import com.erlei.videorecorder.camera.annotations.FocusModel;
import com.erlei.videorecorder.camera.annotations.PictureFormat;
import com.erlei.videorecorder.camera.annotations.PreviewFormat;
import com.erlei.videorecorder.camera.annotations.SceneModel;
import com.erlei.videorecorder.camera.annotations.WhiteBalance;
import com.erlei.videorecorder.util.Config;
import com.erlei.videorecorder.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;


@SuppressWarnings({"WeakerAccess", "unused"})
public class Camera {
    private static final String S_CAMERA_SUPPORTED_MODES = "camera_supported_modes";
    private final SparseArray<HashMap<String, String>> mSupportedModes = new SparseArray<>();
    /**
     * android.hardware.Camera#getNumberOfCameras() == 0
     */

    public static final int NO_CAMERA = 1;
    /**
     * CameraBuilder == null , Should not happen
     */
    public static final int NOT_CONFIGURED = 2;

    /**
     * Set camera orientation device is not supported
     *
     * @see CameraBuilder#setFacing(int)
     */
    public static final int UNSUPPORTED_FACING = 3;

    /**
     * Failed to open the camera (please check the permissions)
     *
     * @see android.hardware.Camera#open()
     */
    public static final int OPEN_ERROR = 4;

    /**
     * Successfully connected camera service and preview
     */
    public static final int SUCCESS = 5;
    /**
     * Turn on preview failed
     */
    public static final int PREVIEW_ERROR = 6;
    /**
     * unknown mistake
     */
    public static final int UNKNOWN = 7;

    /**
     * Unsupported preview size
     */
    public static final int UNSUPPORTED_PREVIEW_SIZE = 8;
    /**
     * Unsupported photo size
     */
    public static final int UNSUPPORTED_PICTURE_SIZE = 9;

    private static final String TAG = Config.TAG;
    private static final boolean LOG_ENABLE = Config.DEBUG;
    private final Context mContext;
    private CameraBuilder mBuilder;
    private int mCameraId = -1;
    private android.hardware.Camera mCamera;
    private android.hardware.Camera.Size mPreviewSize;
    private android.hardware.Camera.Size mPictureSize;
    private int mDisplayOrientation;
    private int mCameraOrientation;
    private boolean mOpened;


    private Camera(Context context, CameraBuilder builder) {
        mContext = context;
        mBuilder = builder;
    }

    /**
     * Get the direction of the camera
     *
     * @return 0 , 90, 180 , 270
     */
    public int getCameraOrientation() {
        return mCameraOrientation;
    }

    public int getDisplayOrientation() {
        return mDisplayOrientation;
    }

    public CameraBuilder getBuilder() {
        return mBuilder;
    }

    /**
     * Turn on the camera
     *
     * @return whether succeed
     */
    @Nullable
    public synchronized Camera open() {
        int cameras = android.hardware.Camera.getNumberOfCameras();
        if (cameras == 0) {
            loge("The device has no camera available");
            handleCameraCallback(NO_CAMERA, "The device has no camera available");
            return null;
        }
        if (mBuilder == null) {
            loge("No camera configured");
            handleCameraCallback(NOT_CONFIGURED, "No camera configured");
            throw new IllegalStateException("No camera configured");
        }
        android.hardware.Camera.CameraInfo cameraInfo = new android.hardware.Camera.CameraInfo();
        for (int i = 0; i < cameras; i++) {
            android.hardware.Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == mBuilder.mFacing) mCameraId = i;
        }
        if (mCameraId == -1) {
            loge("Set the camera orientation that the device does not support");
            handleCameraCallback(UNSUPPORTED_FACING, "Set the camera orientation that the device does not support");
            return null;
        }
        try {
            if (mCamera != null) close();
            mCamera = android.hardware.Camera.open(mCameraId);
            mOpened = true;
        } catch (Exception e) {
            loge(e.toString());
            e.printStackTrace();
            close();
            handleCameraCallback(OPEN_ERROR, e.toString());
            return null;
        }
        setCameraParameters();
        startPreview();
        return this;
    }

    private void handleCameraCallback(final int code, final String errMsg) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBuilder == null) return;
                for (CameraCallback callback : mBuilder.mCameraCallbacks) {
                    if (TextUtils.isEmpty(errMsg)) {
                        callback.onSuccess(Camera.this);
                    } else {
                        callback.onFailure(code, errMsg);
                    }
                }
            }
        };
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }


    }

    /**
     * @return Get the size of the preview. This method needs to be called after the camera is successfully previewed to have the correct result.
     */
    public Size getPreviewSize() {
        return new Size(mPreviewSize);
    }

    /**
     * @return activity is  landscape
     */

    public boolean isLandscape() {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null && windowManager.getDefaultDisplay() != null) {
            int rotation = windowManager.getDefaultDisplay().getRotation();
            LogUtil.logd(TAG, "rotation=" + rotation);
            return rotation == ROTATION_90 || rotation == ROTATION_270;
        }
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    private void setCameraParameters() {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters cameraParameters = getParameters();
        if (cameraParameters == null) return;
        // Set the focus mode
        setFocusMode(cameraParameters, mBuilder.mFocusModes);

        //Set flash mode
        setFlashMode(cameraParameters, mBuilder.mFlashModes);

        //Set scene mode
        setSceneMode(cameraParameters, mBuilder.mSceneModes);

        //Color effect
        setColorEffect(cameraParameters, mBuilder.mColorEffects);

        //White balance
        setWhiteBalance(cameraParameters, mBuilder.mWhiteBalances);

        //Refresh rate
        setAntibanding(cameraParameters, mBuilder.mAntibanding);

        //Set the pixel format of the preview frame
        int previewFormat = getSupportedModelOfInt(cameraParameters.getSupportedPreviewFormats(), mBuilder.mPreviewFormats);
        if (previewFormat != -1) cameraParameters.setPreviewFormat(previewFormat);

        //Set the pixel format of the image
        int pictureFormat = getSupportedModelOfInt(cameraParameters.getSupportedPictureFormats(), mBuilder.mPictureFormats);
        if (pictureFormat != -1) cameraParameters.setPictureFormat(pictureFormat);

        //Whether it is recording mode
        cameraParameters.setRecordingHint(mBuilder.mRecordingHint);


        //Set preview size
        if (mBuilder.mPreviewSizeSelector != null) {
            mPreviewSize = mBuilder.mPreviewSizeSelector.select(cameraParameters.getSupportedPreviewSizes(), mBuilder.mPreviewSize);
        } else {
            mPreviewSize = getOptimalSize("SupportedPreviewSizes", cameraParameters.getSupportedPreviewSizes(), mBuilder.mPreviewSize);
        }
        if (mPreviewSize == null) {
            handleCameraCallback(UNSUPPORTED_PREVIEW_SIZE, "Set preview size");
            throw new IllegalStateException("Did not find a suitable preview size");
        }
        log("requestPreviewSize ：" + mBuilder.mPreviewSize.toString() + "\t\t previewSize : " + getPreviewSize().toString());
        cameraParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);


        //Set the size of the photo taken
        if (mBuilder.mPictureSize != null) {
            if (mBuilder.mPictureSizeSelector != null) {
                mPictureSize = mBuilder.mPictureSizeSelector.select(cameraParameters.getSupportedPreviewSizes(), mBuilder.mPictureSize);
            } else {
                mPictureSize = getOptimalSize("SupportedPictureSizes", cameraParameters.getSupportedPictureSizes(), mBuilder.mPictureSize);
            }
            if (mPictureSize == null) {
                handleCameraCallback(UNSUPPORTED_PICTURE_SIZE, "Did not find a suitable image size");
                throw new IllegalStateException("Did not find a suitable image size");
            }
            log("requestPictureSize ：" + mBuilder.mPictureSize.toString() + "\t\t pictureSize : " + getPictureSize().toString());
            cameraParameters.setPictureSize(mPictureSize.width, mPictureSize.height);
        }
        //Photo zoom
        setZoom(cameraParameters, mBuilder.mZoom);


        mCamera.setParameters(cameraParameters);
        log(cameraParameters.flatten().replaceAll(";", ";\t"));
    }

    private void setZoom(android.hardware.Camera.Parameters cameraParameters, int zoom) {
        if (zoom != -1) {
            int target = clamp(zoom, 0, cameraParameters.getMaxZoom());
            if (cameraParameters.isZoomSupported()) cameraParameters.setZoom(target);
        }
    }

    @SuppressWarnings("all")
    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void setAntibanding(android.hardware.Camera.Parameters cameraParameters, List<String> antibandings) {
        String antibanding = getSupportedModelOfString(cameraParameters.getSupportedAntibanding(), antibandings);
        if (!TextUtils.isEmpty(antibanding)) cameraParameters.setAntibanding(antibanding);
    }

    private void setWhiteBalance(android.hardware.Camera.Parameters cameraParameters, List<String> whiteBalances) {
        String whiteBalance = getSupportedModelOfString(cameraParameters.getSupportedWhiteBalance(), whiteBalances);
        if (!TextUtils.isEmpty(whiteBalance)) cameraParameters.setWhiteBalance(whiteBalance);
    }

    private void setColorEffect(android.hardware.Camera.Parameters cameraParameters, List<String> colorEffects) {
        String colorEffect = getSupportedModelOfString(cameraParameters.getSupportedColorEffects(), colorEffects);
        if (!TextUtils.isEmpty(colorEffect)) cameraParameters.setColorEffect(colorEffect);
    }

    private void setSceneMode(android.hardware.Camera.Parameters cameraParameters, List<String> sceneModels) {
        String sceneModel = getSupportedModelOfString(cameraParameters.getSupportedSceneModes(), sceneModels);
        if (!TextUtils.isEmpty(sceneModel)) cameraParameters.setSceneMode(sceneModel);
    }

    private void setFlashMode(android.hardware.Camera.Parameters cameraParameters, List<String> flashModels) {
        String flashModel = getSupportedModelOfString(cameraParameters.getSupportedFlashModes(), flashModels);
        if (!TextUtils.isEmpty(flashModel)) cameraParameters.setFlashMode(flashModel);
    }

    private void setFocusMode(android.hardware.Camera.Parameters cameraParameters, List<String> focusModels) {
        String focusMode = getSupportedModelOfString(cameraParameters.getSupportedFocusModes(), focusModels);
        if (!TextUtils.isEmpty(focusMode)) cameraParameters.setFocusMode(focusMode);
    }

    /**
     * Switch flash status
     */
    public synchronized void toggleFlash() {
        if (mCamera == null) return;
        try {
            android.hardware.Camera.Parameters parameters = getParameters();
            if (parameters == null) return;
            if (flashIsOpen()) {
                //Open state - need to close
                parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
            } else {
                //Closed state - need to open
                parameters.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
            loge("Switch flash failed");
        }
    }

    /**
     * @return Is the flash turned on?
     */
    public boolean flashIsOpen() {
        android.hardware.Camera.Parameters parameters = getParameters();
        return parameters != null && mCamera != null && android.hardware.Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode());
    }

    /**
     * Switch camera orientation
     */
    public synchronized void toggleFacing() {
        if (mCamera == null) return;
        if (getFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
            mBuilder.mFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mBuilder.mFacing = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        open();
    }

    /**
     * @return Get the current camera orientation
     */
    public int getFacing() {
        if (mCamera == null) return -1;
        return getCameraInfo(mCameraId).facing;
    }


    public Size getPictureSize() {
        return new Size(mPictureSize);
    }


    public void takePicture(android.hardware.Camera.ShutterCallback shutter, android.hardware.Camera.PictureCallback raw,
                            android.hardware.Camera.PictureCallback postview, android.hardware.Camera.PictureCallback jpeg) {
        if (mCamera == null) return;
        mCamera.takePicture(shutter, raw, postview, jpeg);
    }

    public void takePicture(android.hardware.Camera.ShutterCallback shutter, android.hardware.Camera.PictureCallback raw,
                            android.hardware.Camera.PictureCallback jpeg) {
        if (mCamera == null) return;
        takePicture(shutter, raw, null, jpeg);
    }


    /**
           * Set focus
           *
           * @param rect focus area
           */
    public void setFocusAreas(Rect... rect) {
        if (mCamera == null || rect == null || rect.length == 0) return;
        final android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;

        int maxNumFocusAreas = parameters.getMaxNumFocusAreas();
        if (maxNumFocusAreas > 0) {
            ArrayList<android.hardware.Camera.Area> focusAreas = new ArrayList<>();
            for (int i = 0; i < maxNumFocusAreas; i++) {
                if (i < rect.length)
                    focusAreas.add(new android.hardware.Camera.Area(rect[i], 1000));
            }
            parameters.setFocusAreas(focusAreas);
        }
        final String currentFocusMode = parameters.getFocusMode();
        if (isSupported(parameters.getSupportedFocusModes(), android.hardware.Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(new android.hardware.Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, android.hardware.Camera camera) {
                    //Cannot use the above. If the parameter of the focus has changed during this time, it will be lost.
                    android.hardware.Camera.Parameters cameraParameters = camera.getParameters();
                    cameraParameters.setFocusMode(currentFocusMode);
                    camera.setParameters(cameraParameters);
                    cancelAutoFocus();
                    if (mBuilder.mAutoFocusCallback != null) {
                        mBuilder.mAutoFocusCallback.onAutoFocus(b, camera);
                    }
                }
            });
        } else {
            mCamera.setParameters(parameters);
        }
    }

    @Nullable
    private android.hardware.Camera.Parameters getParameters() {
        try {
            return mCamera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getSupportedModelOfInt(List<Integer> supportedModel, List<Integer> requestModels) {
        if (supportedModel != null && !supportedModel.isEmpty() && requestModels != null && !requestModels.isEmpty()) {
            for (Integer model : requestModels) {
                if (supportedModel.contains(model)) return model;
            }
        }
        return -1;
    }

    @Nullable
    private String getSupportedModelOfString(List<String> supportedModel, List<String> requestModels) {
        if (supportedModel != null && !supportedModel.isEmpty() && requestModels != null && !requestModels.isEmpty()) {
            for (String model : requestModels) {
                if (isSupported(supportedModel, model)) {
                    return model;
                }
            }
        }
        return null;
    }

    private boolean isSupported(List<String> models, String model) {
        return !(models == null || models.isEmpty()) && models.contains(model);
    }

    /**
     * The camera must be successfully turned on before the correct return result
     *
     * @return android.hardware.Camera.Parameters
     */
    @Nullable
    public android.hardware.Camera.Parameters getCameraParameters() {
        if (mCamera == null) return null;
        return getParameters();
    }

    /**
     * Turn on camera preview
     */
    private synchronized void startPreview() {
        if (mCamera == null) {
            handleCameraCallback(UNKNOWN, "unknown mistake");
            return;
        }
        try {
            if (mBuilder.mSurfaceTexture != null) {
                mCamera.setPreviewTexture(mBuilder.mSurfaceTexture);
            } else if (mBuilder.mSurfaceHolder != null) {
                mCamera.setPreviewDisplay(mBuilder.mSurfaceHolder);
            }
            if (mBuilder.mDisplayOrientation == -1 && mContext != null) {
                mDisplayOrientation = getCameraDisplayOrientation(getActivityOrientation(mContext));
            } else {
                mDisplayOrientation = mBuilder.mDisplayOrientation;
            }
            mCamera.setDisplayOrientation(mDisplayOrientation);
            mCamera.startPreview();
            handleCameraCallback(SUCCESS, null);
        } catch (Exception e) {
            loge(e.toString());
            e.printStackTrace();
            handleCameraCallback(PREVIEW_ERROR, toString());
            close();
        }
    }

    private int getActivityOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null && windowManager.getDefaultDisplay() != null) {
            return windowManager.getDefaultDisplay().getRotation();
        }
        return 90;
    }

    /**
           * Calculate the angle required for Camera to display correctly according to the current angle of the screen
           *
           * @param rotation screen angle
           * @return CameraDisplayOrientation
           * @see android.view.Display#getRotation()
           */
    private int getCameraDisplayOrientation(int rotation) {
        android.hardware.Camera.CameraInfo info = getCameraInfo(mCameraId);
        mCameraOrientation = info.orientation;
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    @Nullable
    public android.hardware.Camera.CameraInfo getCameraInfo() {
        if (mCameraId == -1) return null;
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraId, info);
        return info;
    }

    @NonNull
    public android.hardware.Camera.CameraInfo getCameraInfo(int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCameraId, info);
        return info;
    }

    /**
           * @param tag log prefix
           * @param supportedPreviewSizes preview size supported by the device
           * @param request request preview size
           * @return Find the appropriate preview size based on the requested size in the size supported by the device.
           * If the requested size is supported by the device, return the size directly, otherwise find the closest size according to the specified width and height.
           */
    private android.hardware.Camera.Size getOptimalSize(String tag, List<android.hardware.Camera.Size> supportedPreviewSizes, Size request) {

        Collections.sort(supportedPreviewSizes, new Comparator<android.hardware.Camera.Size>() {
            //            @Override
//            public int compare(android.hardware.Camera.Size o1, android.hardware.Camera.Size o2) {
//                return o1.width == o2.width ? o1.height - o2.height : o1.width - o2.width;
//            }
            @Override
            public int compare(android.hardware.Camera.Size pre, android.hardware.Camera.Size after) {
                return Long.signum((long) pre.width * pre.height - (long) after.width * after.height);
            }
        });
        if (LOG_ENABLE) {
            for (android.hardware.Camera.Size size : supportedPreviewSizes) {
                log(tag + "\t\twidth :" + size.width + "\t height :" + size.height + "\t ratio : " + ((float) size.width / (float) size.height));
            }
        }
        //If the device supports the requested size
        for (android.hardware.Camera.Size size : supportedPreviewSizes) {
            if (request.equals(size)) return size;
        }

        for (android.hardware.Camera.Size size : supportedPreviewSizes) {
            if (size.width >= request.width && size.height >= request.height) return size;
        }

        //I haven't found it yet, I use the biggest one.
        return Collections.max(supportedPreviewSizes, new Comparator<android.hardware.Camera.Size>() {
            @Override
            public int compare(android.hardware.Camera.Size pre, android.hardware.Camera.Size after) {
                return Long.signum((long) pre.width * pre.height -
                        (long) after.width * after.height);
            }
        });
    }


    public synchronized void close() {
        if (mCamera == null) return;
        try {
            mOpened = false;
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setPreviewCallback(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {
            loge(e.getMessage());
            e.printStackTrace();
        }
    }


    public Size getRequestPreviewSize() {
        return mBuilder.mPreviewSize;
    }

    public List<Size> getSupportedPreviewSizes() {
        ArrayList<Size> sizes = new ArrayList<>();
        if (mCamera == null) return sizes;
        android.hardware.Camera.Parameters cameraParameters = getCameraParameters();
        if (cameraParameters == null) return sizes;
        List<android.hardware.Camera.Size> supportedPreviewSizes = cameraParameters.getSupportedPreviewSizes();
        for (android.hardware.Camera.Size previewSize : supportedPreviewSizes) {
            sizes.add(new Size(previewSize));
        }
        return sizes;
    }

    public void setFacing(int facing) {
        mBuilder.mFacing = facing;
        open();
    }

    public void setFlashMode(String... flashModes) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setFlashMode(parameters, Arrays.asList(flashModes));
        mCamera.setParameters(parameters);
    }

    public void setWhiteBalance(String... whiteBalance) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setWhiteBalance(parameters, Arrays.asList(whiteBalance));
        mCamera.setParameters(parameters);
    }

    public void setMeteringAreas(Rect... rect) {
        if (mCamera == null || rect == null || rect.length == 0) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters != null) {
            int maxNumMeteringAreas = parameters.getMaxNumMeteringAreas();
            if (maxNumMeteringAreas > 0) {
                ArrayList<android.hardware.Camera.Area> focusAreas = new ArrayList<>();
                for (int i = 0; i < maxNumMeteringAreas; i++) {
                    if (i < rect.length)
                        focusAreas.add(new android.hardware.Camera.Area(rect[i], 1000));
                }
                parameters.setMeteringAreas(focusAreas);
                mCamera.setParameters(parameters);
            }
        } else {
            loge("Setting the metering area failed");
        }
    }

    /**
     *
     *
     * @param compensation compensation <= maxExposureCompensation && compensation >= minExposureCompensation
     * @see android.hardware.Camera.Parameters#getMaxExposureCompensation()
     * @see android.hardware.Camera.Parameters#getMinExposureCompensation()
     */
    public void setExposureCompensation(int compensation) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        int maxExposureCompensation = parameters.getMaxExposureCompensation();
        int minExposureCompensation = parameters.getMinExposureCompensation();
        if (compensation <= maxExposureCompensation && compensation >= minExposureCompensation) {
            parameters.setExposureCompensation(compensation);
            mCamera.setParameters(parameters);
        }

    }

    public void setRecordingHint(boolean recording) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        parameters.setRecordingHint(recording);
        mCamera.setParameters(parameters);
    }

    public void setAntibanding(String... antibanding) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setAntibanding(parameters, Arrays.asList(antibanding));
        mCamera.setParameters(parameters);
    }

    public void setFocusMode(String... focusModes) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setFocusMode(parameters, Arrays.asList(focusModes));
        mCamera.setParameters(parameters);
    }

    public void setColorEffects(String... colorEffects) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setColorEffect(parameters, Arrays.asList(colorEffects));
        mCamera.setParameters(parameters);
    }

    public void setSceneMode(String... sceneModels) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setSceneMode(parameters, Arrays.asList(sceneModels));
        mCamera.setParameters(parameters);
    }

    public void setZoom(int zoom) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        setZoom(parameters, zoom);
        mCamera.setParameters(parameters);
    }

    public void smoothZoom(int zoom) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        int maxZoom = parameters.getMaxZoom();
        int target = clamp(zoom, 0, maxZoom);
        if (parameters.isZoomSupported() && parameters.isSmoothZoomSupported()) {
            mCamera.startSmoothZoom(target);
        }
    }

    public void setCameraParameters(android.hardware.Camera.Parameters parameters) {
        if (mCamera == null || parameters == null) return;
        mCamera.setParameters(parameters);
    }

    public List<FpsRange> getSupportedPreviewFpsRange() {
        ArrayList<FpsRange> fpsRanges = new ArrayList<>();
        if (mCamera == null) return fpsRanges;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return fpsRanges;
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        for (int[] ints : supportedPreviewFpsRange) {
            fpsRanges.add(new FpsRange(ints));
        }
        return fpsRanges;
    }

    public void setPreviewFpsRange(FpsRange fpsRange) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = getParameters();
        if (parameters == null) return;
        List<FpsRange> supportedPreviewFpsRange = getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRange.contains(fpsRange)) {
            parameters.setPreviewFpsRange(fpsRange.min, fpsRange.max);
            mCamera.setParameters(parameters);
        }
    }

    public void cancelAutoFocus() {
        if (mCamera == null) return;
        mCamera.cancelAutoFocus();
    }

    public boolean isOpen() {
        return mOpened;
    }

    public List<String> getSupportedModes(String... modes) {
        if (mCamera == null) return null;
        int cameraId = getCameraId();
        if (cameraId == -1) return new ArrayList<>();
        ArrayList<String> supportedModes = getMemoryCachedSupportedModes(cameraId, modes);
        if (supportedModes == null) {
            //The memory is not fetched, taken from SharedPreferences
            supportedModes = getFileCachedSupportedModes(cameraId, modes);
            if (supportedModes == null) {
                //The file was not taken, taken from Camera
                supportedModes = getSupportedModesFromCamera(modes);
            }
        }
        return supportedModes;
    }


    private ArrayList<String> getSupportedModesFromCamera(String... modes) {
        android.hardware.Camera.Parameters cameraParameters = getCameraParameters();
        if (cameraParameters == null) return null;
        String flatten = cameraParameters.flatten();
        int cameraId = getCameraId();
        unFlatten(cameraId, flatten);
        SharedPreferences preferences = mContext.getSharedPreferences(S_CAMERA_SUPPORTED_MODES, Context.MODE_PRIVATE);
        preferences.edit().putString(S_CAMERA_SUPPORTED_MODES + cameraId, flatten).apply();
        return getMemoryCachedSupportedModes(cameraId, modes);
    }

    private ArrayList<String> getFileCachedSupportedModes(int cameraId, String... modes) {
        SharedPreferences preferences = mContext.getSharedPreferences(S_CAMERA_SUPPORTED_MODES, Context.MODE_PRIVATE);
        String flatten = preferences.getString(S_CAMERA_SUPPORTED_MODES + cameraId, "");
        if (!TextUtils.isEmpty(flatten)) {
            unFlatten(cameraId, flatten);
            return getMemoryCachedSupportedModes(cameraId, modes);
        } else {
            return null;
        }
    }

    private void unFlatten(int cameraId, String flatten) {
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(';');
        splitter.setString(flatten);
        HashMap<String, String> map = new HashMap<>();
        for (String kv : splitter) {
            int pos = kv.indexOf('=');
            if (pos == -1) continue;
            String k = kv.substring(0, pos);
            String v = kv.substring(pos + 1);
            map.put(k, v);
        }
        mSupportedModes.put(cameraId, map);
    }

    public ArrayList<String> getMemoryCachedSupportedModes(int cameraId, String... modes) {
        if (mSupportedModes.size() > 0) {
            ArrayList<String> supportedModes = new ArrayList<>();
            //Cache with data
            HashMap<String, String> map = mSupportedModes.get(cameraId);
            for (String mode : modes) {
                String value = map.get(mode);
                if (!TextUtils.isEmpty(value)) supportedModes.addAll(split(value));
            }
            return supportedModes;
        } else {
            return null;
        }

    }

    private ArrayList<String> split(String str) {
        if (str == null) return null;

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList<String>();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    public int getCameraId() {
        if (mCamera == null) return -1;
        return mCameraId;
    }

    public void setMode(String key, String value) {
        if (mCamera == null) return;
        android.hardware.Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) return;
        parameters.set(key, value);
        mCamera.setParameters(parameters);
    }

    @SuppressWarnings("unused")
    public static class CameraBuilder {

        private final Context mContext;
        private int mFacing;
        private Size mPictureSize;
        private Size mPreviewSize;
        private List<String> mFlashModes;
        private SurfaceTexture mSurfaceTexture;
        private SurfaceHolder mSurfaceHolder;
        private List<CameraCallback> mCameraCallbacks = new ArrayList<>();
        private List<String> mFocusModes;
        private List<String> mSceneModes;
        private List<Integer> mPreviewFormats;
        private List<String> mColorEffects;
        private List<Integer> mPictureFormats;
        private int mZoom = -1;
        private int mDisplayOrientation = -1;
        private android.hardware.Camera.AutoFocusCallback mAutoFocusCallback;
        private List<String> mAntibanding;
        private SizeSelector mPreviewSizeSelector;
        private SizeSelector mPictureSizeSelector;
        private boolean mRecordingHint;
        private List<String> mWhiteBalances;

        public CameraBuilder(Context context) {
            mContext = context;
        }

        public void release() {
            mSurfaceHolder = null;
            mSurfaceTexture = null;
            if (mCameraCallbacks != null) mCameraCallbacks.clear();
            mCameraCallbacks = null;
        }


        public CameraBuilder useDefaultConfig() {
            return setFacing(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                    .setPreviewSize(new Size(1920, 1080))
                    .setSceneMode(android.hardware.Camera.Parameters.SCENE_MODE_AUTO)
                    .setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                    .setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)
                    .setPictureSize(new Size(1920, 1080));
        }

        public CameraBuilder setAutoFocusCallback(android.hardware.Camera.AutoFocusCallback autoFocusCallback) {
            mAutoFocusCallback = autoFocusCallback;
            return this;
        }

        /**
                   * Set zoom if the device supports it
                   * The minimum value is 0 and the maximum value is getMaxZoom() . If the value set is greater than the maximum value obtained, it will be set to MaxZoom.
                   *
                   * @param zoom zoom level
                   */
        public CameraBuilder setZoom(@IntRange(from = 0, to = Integer.MAX_VALUE) int zoom) {
            mZoom = zoom;
            return this;
        }

        public CameraBuilder setDisplayOrientation(int displayOrientation) {
            mDisplayOrientation = displayOrientation;
            return this;
        }

        /**
         * @param size size of picture
         */
        public CameraBuilder setPictureSize(@NonNull Size size) {
            mPictureSize = size;
            return this;
        }

        /**
         * 是否是录制模式
         *
         * @param recording 录制模式
         * @see android.hardware.Camera.Parameters#setRecordingHint(boolean)
         */
        public CameraBuilder setRecordingHint(boolean recording) {
            mRecordingHint = recording;
            return this;
        }

        /**
         * @param size 相机预览大小
         */
        public CameraBuilder setPreviewSize(@NonNull Size size) {
            mPreviewSize = size;
            return this;
        }

        /**
         * 摄像头朝向
         *
         * @see android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK
         * @see android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT
         */
        public CameraBuilder setFacing(@Facing int facing) {
            mFacing = facing;
            return this;
        }

        /**
         * 场景模式
         *
         * @see android.hardware.Camera.Parameters#SCENE_MODE_ACTION
         * @see android.hardware.Camera.Parameters#SCENE_MODE_AUTO
         * @see android.hardware.Camera.Parameters#SCENE_MODE_BARCODE
         * @see android.hardware.Camera.Parameters#SCENE_MODE_BEACH
         * @see android.hardware.Camera.Parameters#SCENE_MODE_CANDLELIGHT
         * @see android.hardware.Camera.Parameters#SCENE_MODE_FIREWORKS
         * @see android.hardware.Camera.Parameters#SCENE_MODE_HDR
         * @see android.hardware.Camera.Parameters#SCENE_MODE_LANDSCAPE
         * @see android.hardware.Camera.Parameters#SCENE_MODE_NIGHT
         * @see android.hardware.Camera.Parameters#SCENE_MODE_NIGHT_PORTRAIT
         * @see android.hardware.Camera.Parameters#SCENE_MODE_PARTY
         * @see android.hardware.Camera.Parameters#SCENE_MODE_SNOW
         * @see android.hardware.Camera.Parameters#SCENE_MODE_PORTRAIT
         * @see android.hardware.Camera.Parameters#SCENE_MODE_SPORTS
         * @see android.hardware.Camera.Parameters#SCENE_MODE_THEATRE
         * @see android.hardware.Camera.Parameters#SCENE_MODE_SUNSET
         * @see android.hardware.Camera.Parameters#SCENE_MODE_STEADYPHOTO
         */
        public CameraBuilder setSceneMode(@SceneModel String... sceneModes) {
            mSceneModes = Arrays.asList(sceneModes);
            return this;
        }

        /**
         * 设置色彩效果
         *
         * @see android.hardware.Camera.Parameters#EFFECT_SEPIA
         * @see android.hardware.Camera.Parameters#EFFECT_AQUA
         * @see android.hardware.Camera.Parameters#EFFECT_BLACKBOARD
         * @see android.hardware.Camera.Parameters#EFFECT_MONO
         * @see android.hardware.Camera.Parameters#EFFECT_NEGATIVE
         * @see android.hardware.Camera.Parameters#EFFECT_NONE
         * @see android.hardware.Camera.Parameters#EFFECT_POSTERIZE
         * @see android.hardware.Camera.Parameters#EFFECT_SOLARIZE
         * @see android.hardware.Camera.Parameters#EFFECT_WHITEBOARD
         */
        public CameraBuilder setColorEffects(@ColorEffect String... colorEffects) {
            mColorEffects = Arrays.asList(colorEffects);
            return this;
        }

        /**
         * 对焦模式
         *
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_AUTO
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_VIDEO
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_EDOF
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_FIXED
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_INFINITY
         * @see android.hardware.Camera.Parameters#FOCUS_MODE_MACRO
         */
        public CameraBuilder setFocusMode(@FocusModel String... focusModes) {
            mFocusModes = Arrays.asList(focusModes);
            return this;
        }

        /**
         * 图片格式
         *
         * @see ImageFormat#NV21
         * @see ImageFormat#JPEG
         * @see ImageFormat#RGB_565
         */
        public CameraBuilder setPictureFormat(@PictureFormat Integer... pictureFormat) {
            mPictureFormats = Arrays.asList(pictureFormat);
            return this;
        }

        /**
         * Set the anti-flicker parameter, (the stripe caused by the exposure of the digital camera affected by the light frequency (50HZ or 60HZ).)
         *           *
         *           * @param antibanding anti-flicker value
         *
         * @see android.hardware.Camera.Parameters#ANTIBANDING_50HZ
         * @see android.hardware.Camera.Parameters#ANTIBANDING_60HZ
         * @see android.hardware.Camera.Parameters#ANTIBANDING_AUTO
         * @see android.hardware.Camera.Parameters#ANTIBANDING_OFF
         */
        public CameraBuilder setAntibanding(@Antibanding String... antibanding) {
            mAntibanding = Arrays.asList(antibanding);
            return this;
        }

        /**
         * 预览帧格式
         *
         * @see ImageFormat#NV21
         * @see ImageFormat#YV12
         */
        public CameraBuilder setPreviewFormat(@PreviewFormat Integer... previewFormat) {
            mPreviewFormats = Arrays.asList(previewFormat);
            return this;
        }

        /**
         * 因为某些设备不支持指定的模式 ， 所以请设置闪光灯模式优先级列表
         *
         * @see android.hardware.Camera.Parameters#FLASH_MODE_AUTO
         * @see android.hardware.Camera.Parameters#FLASH_MODE_OFF
         * @see android.hardware.Camera.Parameters#FLASH_MODE_ON
         * @see android.hardware.Camera.Parameters#FLASH_MODE_RED_EYE
         * @see android.hardware.Camera.Parameters#FLASH_MODE_TORCH
         */
        public CameraBuilder setFlashMode(@FlashModel String... flashModes) {
            mFlashModes = Arrays.asList(flashModes);
            return this;
        }

        /**
         * 设置比白平衡
         *
         * @param whiteBalance 白平衡
         */
        public CameraBuilder setWhiteBalance(@WhiteBalance String... whiteBalance) {
            mWhiteBalances = Arrays.asList(whiteBalance);
            return this;
        }

        /**
         * 设置使用 SurfaceHolder 预览
         */
        public CameraBuilder setSurfaceHolder(@NonNull SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
            return this;
        }

        /**
         * Set to use surfaceTexture preview
         */
        public CameraBuilder setSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
            mSurfaceTexture = surfaceTexture;
            return this;
        }

        /**
         * Add camera preview size selector
         */
        public CameraBuilder setPreviewSizeSelector(SizeSelector sizeSelector) {
            mPreviewSizeSelector = sizeSelector;
            return this;
        }

        /**
         * 添加相机拍照尺寸选择器
         */
        public CameraBuilder setPictureSizeSelector(SizeSelector sizeSelector) {
            mPictureSizeSelector = sizeSelector;
            return this;
        }

        /**
         * 添加相机预览的回调
         */
        public CameraBuilder addPreviewCallback(@NonNull CameraCallback callback) {
            mCameraCallbacks.add(callback);
            return this;
        }

        /**
         * 构建相机参数
         *
         * @return Camera
         */
        public Camera build() {
            return new Camera(this.mContext, this);
        }

        /**
         * 构建相机参数
         *
         * @return Camera
         */
        public Camera build(CameraBuilder builder) {
            mFacing = builder.mFacing;
            mPictureSize = builder.mPictureSize;
            mPreviewSize = builder.mPreviewSize;
            mFlashModes = builder.mFlashModes;
            mSurfaceTexture = builder.mSurfaceTexture;
            mSurfaceHolder = builder.mSurfaceHolder;
            mCameraCallbacks = new ArrayList<>();
            mCameraCallbacks.addAll(builder.mCameraCallbacks);
            mFocusModes = builder.mFocusModes;
            mSceneModes = builder.mSceneModes;
            mPreviewFormats = builder.mPreviewFormats;
            mColorEffects = builder.mColorEffects;
            mWhiteBalances = builder.mWhiteBalances;
            mPictureFormats = builder.mPictureFormats;
            mZoom = builder.mZoom;
            mDisplayOrientation = builder.mDisplayOrientation;
            mAutoFocusCallback = builder.mAutoFocusCallback;
            mAntibanding = builder.mAntibanding;
            return new Camera(mContext, this);
        }

    }


    public interface SizeSelector {
        /**
                   * Choose the right size
                   *
                   * @param supportedSizes List of supported sizes
                   * @param requestSize expected size
                   * @return final size
                   */
        android.hardware.Camera.Size select(List<android.hardware.Camera.Size> supportedSizes, Size requestSize);
    }

    public interface CameraCallback {
        /**
                   * Turn on the camera successfully
                   * If you are using SurfaceView preview, you can resize the SurfaceView according to camera.getPreviewSize() in this callback to avoid preview distortion.
                   */
        void onSuccess(Camera camera);

        /**
         * Camera preview failure callback
         *
         * @see Camera#NO_CAMERA
         * @see Camera#NOT_CONFIGURED
         * @see Camera#UNSUPPORTED_FACING
         * @see Camera#OPEN_ERROR
         * @see Camera#SUCCESS
         * @see Camera#PREVIEW_ERROR
         * @see Camera#UNKNOWN
         * @see Camera#UNSUPPORTED_PREVIEW_SIZE
         * @see Camera#UNSUPPORTED_PICTURE_SIZE
         */
        void onFailure(int code, String msg);

    }

    public void log(String msg) {
        if (LOG_ENABLE) Log.d(TAG, msg);
    }

    public void loge(String msg) {
        if (LOG_ENABLE) Log.e(TAG, msg);
    }

}
