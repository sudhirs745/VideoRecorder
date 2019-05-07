package com.erlei.videorecorder.recorder;

import android.os.Handler;
import android.os.Message;

import com.erlei.videorecorder.encoder.MuxerCallback;

public class VideoRecorderHandler extends Handler implements MuxerCallback {

    protected static final int MSG_UPDATE_FPS = 1;
    protected static final int MSG_MEDIA_MUXER_STOPPED = 2;
    protected static final int MSG_MEDIA_MUXER_START = 3;
    protected static final int MSG_MEDIA_CAPTURE_START = 4;
    protected static final int MSG_MEDIA_CAPTURE_STOPPED = 5;


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_UPDATE_FPS:
                handleUpdateFPS((float) msg.obj);
                break;
            case MSG_MEDIA_MUXER_STOPPED:
                handleVideoMuxerStopped((String) msg.obj);
                break;
            case MSG_MEDIA_MUXER_START:
                handleVideoMuxerStarted((String) msg.obj);
                break;
            case MSG_MEDIA_CAPTURE_START:
                handleMediaCaptureStarted((String) msg.obj);
                break;
            case MSG_MEDIA_CAPTURE_STOPPED:
                handleMediaCaptureStopped((String) msg.obj);
                break;
        }
    }

    /**
           * Stop capturing audio and video data
           *
           * @param output The video file path recorded this time
           * @see VideoRecorderHandler#handleMediaCaptureStarted(java.lang.String)
           */
    protected void handleMediaCaptureStopped(String output) {

    }

    /**
           * Start capturing audio and video data
           *
           * @param output The video file path recorded this time
           * @see VideoRecorderHandler#handleMediaCaptureStopped(java.lang.String)
           */
    protected void handleMediaCaptureStarted(String output) {

    }

    /**
           * Start to merge audio and video encoded data
           *
           * @param output The video file path recorded this time
           * <p>
           * Note that this method and handleVideoMuxerStopped are not always paired. There is an exception.
           * Calling stopRecord immediately after calling startRecord will cause handleVideoMuxerStarted not to be called
           * This is because the audio and video encoded data cannot be started immediately after calling startRecord.
           * You need to wait for the mix to add a track to open the mixer, so if you stop recording immediately after you start recording,
           * Causes no VideoRecorderHandler.MSG_MEDIA_MUXER_START to be received
           * message that caused no call to handleVideoMuxerStarted()
           * @see VideoRecorderHandler#handleVideoMuxerStopped(java.lang.String)
           */
    protected void handleVideoMuxerStarted(String output) {

    }


    /**
           * Stop combining audio and video encoded data
           *
           * @param output The video file path recorded this time
           * <p>
           * Note that this method and handleVideoMuxerStarted are not always paired. There is an exception.
           * Calling stopRecord immediately after calling startRecord will cause handleVideoMuxerStarted not to be called
           * This is because the audio and video encoded data cannot be started immediately after calling startRecord.
           * You need to wait for the mix to add a track to open the mixer, so if you stop recording immediately after you start recording,
           * Causes no VideoRecorderHandler.MSG_MEDIA_MUXER_START to be received
           * message that caused no call to handleVideoMuxerStarted()
           * @see VideoRecorderHandler#handleVideoMuxerStarted(java.lang.String)
           */
    protected void handleVideoMuxerStopped(String output) {

    }

    protected void handleUpdateFPS(float obj) {

    }


    protected void updateFPS(float fps) {
        sendMessage(obtainMessage(MSG_UPDATE_FPS, fps));
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onMuxerStarted(String output) {
        sendMessage(obtainMessage(MSG_MEDIA_MUXER_START, output));
    }

    @Override
    public void onMuxerStopped(String output) {
        sendMessage(obtainMessage(MSG_MEDIA_MUXER_STOPPED, output));
    }

    public void onCaptureStarted(String output) {
        sendMessage(obtainMessage(MSG_MEDIA_CAPTURE_START, output));
    }

    public void onCaptureStopped(String output) {
        sendMessage(obtainMessage(MSG_MEDIA_CAPTURE_STOPPED, output));
    }
}
