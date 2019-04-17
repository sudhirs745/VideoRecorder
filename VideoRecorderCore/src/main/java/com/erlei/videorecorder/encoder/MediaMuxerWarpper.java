package com.erlei.videorecorder.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseArray;

import com.erlei.videorecorder.util.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaMuxerWarpper extends HandlerThread {

    private static final String TAG = "MediaMuxerWarpper";

    private MediaMuxerHandler mHandler;
    private int mTrackCount;
    private MediaMuxer mMediaMuxer;
    private String mOutPutPath;
    private MuxerCallback mCallBack;
    private volatile boolean mIsStarted;

    public MediaMuxerWarpper(String output) {
        super(TAG);
        if (TextUtils.isEmpty(output)) throw new IllegalArgumentException("output must not null");
        mOutPutPath = output;
        try {
            mMediaMuxer = new MediaMuxer(mOutPutPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.loge(TAG, "create MediaMuxer error " + e);
        }
    }

    public String getOutPutPath() {
        return mOutPutPath;
    }

    public synchronized MediaMuxerHandler getHandler() {
        if (!isAlive()) return null;
        if (mHandler == null) {
            mHandler = new MediaMuxerHandler(getLooper(), this);
        }
        return mHandler;
    }

    public MuxerCallback getCallBack() {
        return mCallBack;
    }

    public void setCallBack(MuxerCallback callBack) {
        mCallBack = callBack;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        LogUtil.logd(TAG, TAG + " thread prepared");
        if (mCallBack != null) mCallBack.onPrepared();
    }


    public synchronized int addMediaTrack(MediaFormat format) {
        ++mTrackCount;
        LogUtil.loge(TAG, "OutputFormat = " + format.toString());
        return mMediaMuxer.addTrack(format);
    }


    private void startMuxer() {
        LogUtil.logd(TAG, TAG + " startMuxer");
        try {
            mMediaMuxer.start();
            mIsStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loge(TAG, "startMuxer error " + e);
        }
        if (mCallBack != null) {
            mCallBack.onMuxerStarted(mOutPutPath);
        }
    }

    private void stopMuxer() {
        LogUtil.logd(TAG, TAG + " stopMuxer");
        try {
            mMediaMuxer.stop();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loge(TAG, "stopMuxer error " + e);
        }
        if (mCallBack != null) {
            mCallBack.onMuxerStopped(mOutPutPath);
        }
        mIsStarted = false;
    }

    private void release() {
        try {
            mMediaMuxer.release();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.loge(TAG, "MediaMuxer.release() error " + e);
        }
        quitSafely();
    }


    /**
           * If the mixer is not started, then return false, (re-send to the message queue, and keep the original order, wait for the mixer to start, usually does not wait too long)
           * If you remove the logic to re-send to the message queue, there will be problems with generating video on some phones, such as Huawei Honor 9 .
           * Because the key frame of the first frame is lost, if the first frame of the video frame is not a key frame, it will be stuck until the next key frame is played, and the key frame interval is less than the recording time, which will cause the mixer to stop and crash.
           */
    private boolean writeSampleData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
        LogUtil.logd(TAG,"IFrame = "+((bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0)+"\t\t size = "+bufferInfo.size);
        if (isMuxerStarted()) {
            mMediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);
            return true;
        } else {
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0) {
                SystemClock.sleep(10);
            }
            return false;
        }
    }

    public boolean isMuxerStarted() {
        return mIsStarted;
    }

    public static class MediaMuxerHandler extends Handler {
        private static final int MSG_START = 4;
        private static final int MSG_STOP = 3;
        private static final int MSG_WRITE_SAMPLE_DATA = 5;
        private final WeakReference<MediaMuxerWarpper> mReference;

        private final List<SampleData> mCache;

        MediaMuxerHandler(Looper looper, MediaMuxerWarpper mediaMuxerWarpper) {
            super(looper);
            mReference = new WeakReference<>(mediaMuxerWarpper);
            mCache = Collections.synchronizedList(new ArrayList<SampleData>());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MediaMuxerWarpper muxer = mReference.get();
            if (muxer == null) {
                LogUtil.loge(TAG, "handleMessage: weak ref is null");
                return;
            }
            switch (msg.what) {
                case MSG_START:
                    muxer.startMuxer();
                    break;
                case MSG_STOP:
                    muxer.stopMuxer();
                    release();
                    muxer.release();
                    break;
                case MSG_WRITE_SAMPLE_DATA:
                    SampleData sampleData = (SampleData) msg.obj;
                    if (muxer.writeSampleData(sampleData.trackIndex, sampleData.encodedData, sampleData.bufferInfo)) {
                        LogUtil.logd(TAG, "mCache.add(sampleData);");
                        mCache.add(sampleData);
                    } else {
                        //重新发送到消息队列 ,保持消息队列的顺序
                        sendMessageAtTime(obtainMessage(MSG_WRITE_SAMPLE_DATA, sampleData), msg.getWhen());
                    }
                    break;

            }

        }

        private void release() {
            for (SampleData sampleData : mCache) {
                sampleData.release();
            }
        }

        public void start() {
            sendMessageAtFrontOfQueue(obtainMessage(MSG_START));
        }

        public void stop() {
            sendMessage(obtainMessage(MSG_STOP));
        }

        public void sendEncodedData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
            sendMessage(obtainMessage(MSG_WRITE_SAMPLE_DATA, obtainSampleData(trackIndex, encodedData, bufferInfo)));
        }

        private synchronized SampleData obtainSampleData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
            if (mCache.isEmpty()) {
                LogUtil.logd(TAG, "new SampleData");
                return new SampleData(trackIndex, encodedData, bufferInfo);
            } else {
                LogUtil.logd(TAG, "mCache = " + mCache.size());
                SampleData sampleData = mCache.remove(mCache.size() - 1);
                sampleData.trackIndex = trackIndex;
                sampleData.setEncodedData(trackIndex, encodedData);
                sampleData.bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
                return sampleData;
            }
        }


        private static class SampleData {

            int trackIndex;
            ByteBuffer encodedData;
            MediaCodec.BufferInfo bufferInfo;
            // TODO: 2018/6/21 可以优化 ， 存在内存浪费
            SparseArray<ByteBuffer> mCacheBuffer = new SparseArray<>();

            SampleData(int trackIndex, ByteBuffer encodedData, MediaCodec.BufferInfo bufferInfo) {
                this.trackIndex = trackIndex;
                this.bufferInfo = new MediaCodec.BufferInfo();
                this.bufferInfo.set(bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs, bufferInfo.flags);
                setEncodedData(trackIndex, encodedData);
            }

            void setEncodedData(int trackIndex, ByteBuffer encodedData) {
                ByteBuffer byteBuffer = mCacheBuffer.get(trackIndex);
                if (byteBuffer == null) {
                    byteBuffer = ByteBuffer.allocateDirect(encodedData.capacity());
                    mCacheBuffer.put(trackIndex, byteBuffer);
                }
                byteBuffer.limit(encodedData.limit());
                byteBuffer.position(encodedData.position());
                byteBuffer.put(encodedData);
                this.encodedData = byteBuffer;
            }

            public void release() {
                bufferInfo = null;
                for (int i = 0; i < mCacheBuffer.size(); i++) {
                    ByteBuffer byteBuffer = mCacheBuffer.get(i);
                    if (byteBuffer != null) byteBuffer.clear();
                }
                encodedData.clear();
                encodedData = null;
                mCacheBuffer.clear();
                mCacheBuffer = null;
            }
        }
    }
}
