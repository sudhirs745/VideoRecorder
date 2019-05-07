package com.erlei.videorecorder.recorder;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.support.annotation.IntRange;
import android.view.MotionEvent;

import com.erlei.videorecorder.camera.Camera;
import com.erlei.videorecorder.camera.FpsRange;
import com.erlei.videorecorder.camera.Size;
import com.erlei.videorecorder.camera.annotations.Antibanding;
import com.erlei.videorecorder.camera.annotations.ColorEffect;
import com.erlei.videorecorder.camera.annotations.Facing;
import com.erlei.videorecorder.camera.annotations.FlashModel;
import com.erlei.videorecorder.camera.annotations.FocusModel;
import com.erlei.videorecorder.camera.annotations.SceneModel;
import com.erlei.videorecorder.camera.annotations.WhiteBalance;

import java.util.List;

public interface CameraController {


    void setCameraBuilder(Camera.CameraBuilder cameraBuilder);

    List<Size> getSupportedPreviewSizes();

    Camera.CameraBuilder getCameraBuilder();

    void setPreviewFpsRange(FpsRange fpsRange);

    void setZoomOnTouch(MotionEvent event);

    /**
     * @return Is the camera turned on?
     */
    boolean isOpen();

    int getCameraOrientation();

    int getDisplayOrientation();

    void setFocusAreaOnTouch(MotionEvent event);

    void setMeteringAreaOnTouch(MotionEvent event);

    android.hardware.Camera.Parameters getCameraParameters();

    Camera getCamera();

    void setCameraParameters(android.hardware.Camera.Parameters parameters);

    Context getContext();

    Size getCameraSize();

    Size getSurfaceSize();

    boolean openCamera(SurfaceTexture texture);

    void closeCamera();

    /**
     * @return camera is front
     */
    boolean isFront();

    /**
     * Set zoom if the device supports it
     *       * The minimum value is 0 and the maximum value is getMaxZoom() . If the value set is greater than the maximum value obtained, it will be set to MaxZoom.
     *       *
     *       * @param zoom zoom level
     */
    void setZoom(@IntRange(from = 0, to = Integer.MAX_VALUE) int zoom);

    /**
     *Smooth scaling if the device supports it
     *       * The minimum value is 0 and the maximum value is getMaxZoom() . If the value set is greater than the maximum value obtained, it will be set to MaxZoom.
     *       *
     *       * @param zoom zoom level
     */
    void startSmoothZoom(@IntRange(from = 0, to = Integer.MAX_VALUE) int zoom);

    /**
     * @param sceneModels Scene mode
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
    void setSceneMode(@SceneModel String... sceneModels);

    /**
     * @param colorEffects Set color effects
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
    void setColorEffects(@ColorEffect String... colorEffects);

    /**
     * @param focusModels Focus mode
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_AUTO
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_VIDEO
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_CONTINUOUS_PICTURE
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_EDOF
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_FIXED
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_INFINITY
     * @see android.hardware.Camera.Parameters#FOCUS_MODE_MACRO
     */
    void setFocusMode(@FocusModel String... focusModels);

    /**
     * Set the anti-flicker parameter, (the stripe caused by the exposure of the digital camera affected by the light frequency (50HZ or 60HZ).)
     *
     * @param antibanding Anti-flicker value
     * @see android.hardware.Camera.Parameters#ANTIBANDING_50HZ
     * @see android.hardware.Camera.Parameters#ANTIBANDING_60HZ
     * @see android.hardware.Camera.Parameters#ANTIBANDING_AUTO
     * @see android.hardware.Camera.Parameters#ANTIBANDING_OFF
     */
    void setAntibanding(@Antibanding String... antibanding);

    /**
     * @param flashModels Flash mode
     * @see android.hardware.Camera.Parameters#FLASH_MODE_AUTO
     * @see android.hardware.Camera.Parameters#FLASH_MODE_OFF
     * @see android.hardware.Camera.Parameters#FLASH_MODE_ON
     * @see android.hardware.Camera.Parameters#FLASH_MODE_RED_EYE
     * @see android.hardware.Camera.Parameters#FLASH_MODE_TORCH
     */
    void setFlashMode(@FlashModel String... flashModels);

    /**
     * Whether it is recording mode
     *       *
     *       * @param recording Recording mode
     * @see android.hardware.Camera.Parameters#setRecordingHint(boolean)
     */
    void setRecordingHint(boolean recording);

    /**
     * Set the camera orientation used
     *       *
     *       * @param facing camera orientation
     * @see android.hardware.Camera.CameraInfo#CAMERA_FACING_BACK
     * @see android.hardware.Camera.CameraInfo#CAMERA_FACING_FRONT
     */
    void setFacing(@Facing int facing);

    /**
     * Set the white balance
     *       *
     *       * @param whiteBalance white balance
     */
    void setWhiteBalance(@WhiteBalance String... whiteBalance);

    /**
           * /**
           * Set exposure compensation
           *
           * @param compensation exposure compensation
           */
    void setExposureCompensation(int compensation);

    /**
     *Setting the metering area
     *       *
     *       * @param rect metering area list
     */
    void setMeteringAreas(Rect... rect);

    /**
     * @return Get supported preview frame rate interval
     */
    List<FpsRange> getSupportedPreviewFpsRange();

    /**
     * Set focus
     *       *
     *       * @param rect focus area list
     */
    void setFocusAreas(Rect... rect);

    /**
     * Switch camera orientation
     */
    void toggleFacing();

    /**
     * Get the mode supported by the camera, which can only be called after the camera is turned on.
     *
     * @param modes    modes
     */
    List<String> getSupportedModes(String... modes);


    /**
     * Setting mode
     * @param key key
     * @param value value
     */
    void setMode(String key, String value);
}
