package com.erlei.multipartrecorder;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;

import com.erlei.videorecorder.recorder.CameraController;
import com.erlei.videorecorder.recorder.IVideoRecorder;
import com.erlei.videorecorder.recorder.VideoRecorder;
import com.erlei.videorecorder.recorder.VideoRecorderHandler;
import com.erlei.videorecorder.util.LogUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Multi-segment recording
 */
public class MultiPartRecorder extends VideoRecorderHandler implements IVideoRecorder {
    static {
        LogUtil.TAG = "MultiPartRecorder";
    }

    private static final String TAG = "MultiPartRecorder";
    private final VideoRecorder mRecorder;
    private File mOutputFile;
    private final Context mContext;
    private final VideoRecorder.Config mConfig;
    private final VideoRecorderHandler mDefaultViewHandler;
    private final List<Part> mParts;
    private VideoMergeListener mMergeListener;
    private List<VideoPartListener> mPartListeners;
    private boolean mDelPartEnable = true;
    private FileFilter mFileFilter;


    private MultiPartRecorder(VideoRecorder.Builder builder) {
        if (builder == null)
            throw new IllegalArgumentException("VideoRecorder.Builder must not null");
        mParts = new ArrayList<>();
        mConfig = builder.getConfig();
        mDefaultViewHandler = mConfig.getViewHandler();
        mContext = mConfig.getContext();
        setOutPut(builder);
        builder.setCallbackHandler(this);
        mRecorder = builder.build();
    }

    public File getCurrentPartFile() {
        return mRecorder.getOutputFile();
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    @Override
    public SurfaceTexture getPreviewTexture() {
        return mRecorder.getPreviewTexture();
    }

    /**
     * Take a photo
     */
    @Override
    public void takePicture(TakePictureCallback callback) {
        mRecorder.takePicture(callback);
    }

    /**
           * @param delPartEnable Whether to enable video block deletion
           * The original intention is to consider that placing all video segments at the end of the recording may have a long waiting time.
           * So after considering recording a segment, merge it to avoid the final merge time is too long, but after incomplete testing,
           * The time consumption of the final merger is acceptable. Please handle it for the time being.
           */
    public void setDelPartEnable(boolean delPartEnable) {
        mDelPartEnable = delPartEnable;
    }

    /**
     * Delete video blocks based on file path
     */
    public Part removePart(String path) {
        if (!mDelPartEnable || mParts.isEmpty()) return null;
        int index = mParts.indexOf(new Part(path));
        if (index >= 0) return mParts.remove(index);
        return null;

    }

    /**
           * Delete the last video block
           * Warning, this method is not called between onRecordVideoPartStarted and onRecordVideoPartSuccess or onRecordVideoPartFailure
           * Causes loss of onRecordVideoPartSuccess or onRecordVideoPartFailure callbacks when switching between recording states quickly
           */
    public Part removeLastPart() {
        if (!mDelPartEnable || mParts.isEmpty()) return null;
        return mParts.remove(mParts.size() - 1);
    }

    public VideoMergeListener getMergeListener() {
        return mMergeListener;
    }

    public void setMergeListener(VideoMergeListener mergeListener) {
        mMergeListener = mergeListener;
    }


    public void addPartListener(VideoPartListener partListener) {
        if (mPartListeners == null) {
            mPartListeners = new ArrayList<>();
        }
        mPartListeners.add(partListener);
    }

    /**
           * Modify the configuration of the normal VideoRecorder, let him output the video block to the specified folder,
           * Let it support multi-segment recording
           */

    private void setOutPut(VideoRecorder.Builder builder) {
        mOutputFile = mConfig.getOutputFile();
        if (mOutputFile == null) {
            mOutputFile = getOutPut();
        }
        builder.setOutPutFile(null);
        File filesDir = mContext.getExternalCacheDir();
        if (filesDir == null) filesDir = mContext.getCacheDir();
        builder.setOutPutPath(new File(filesDir, TAG + File.separator).getAbsolutePath());

    }

    private File getOutPut() {
        //Or prefer to use the file storage path set before-
        String outputPath = mConfig.getOutputPath();
        if (outputPath == null) {
            File filesDir = mConfig.getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
            if (filesDir != null) outputPath = filesDir.getAbsolutePath();
            if (outputPath == null) {
                outputPath = new File(mConfig.getContext().getFilesDir(), TAG).getAbsolutePath();
            }
        }
        File path = new File(outputPath);
        if (!path.exists()) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }
        if (path.isFile()) path = path.getParentFile();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        return new File(path, format.format(new Date()) + ".mp4");
    }

    /**
     * Merge video clips
     */
    public AsyncTask<Part, Float, File> mergeVideoParts() {
        if (mParts == null || mParts.isEmpty()) return null;
        Part[] parts = new Part[mParts.size()];
        parts = mParts.toArray(parts);
        removeAllPart();
        return new VideoPartMergeTask(mOutputFile, mMergeListener, false, 2000).execute(parts);
    }

    @Override
    public void startPreview() {
        mRecorder.startPreview();
    }

    @Override
    public synchronized void startRecord() {
        mRecorder.setRecordEnabled(true);
    }

    @Override
    public synchronized void stopRecord() {
        mRecorder.setRecordEnabled(false);
    }

    public synchronized void setRecordEnabled(boolean enable) {
        mRecorder.setRecordEnabled(enable);
    }

    @Override
    public CameraController getCameraController() {
        return mRecorder.getCameraController();
    }

    @Override
    public boolean isRecordEnable() {
        return mRecorder.isRecordEnable();
    }

    /**
     * @return Is the mixer running?
     */
    @Override
    public boolean isMuxerRunning() {
        return mRecorder.isMuxerRunning();
    }

    @Override
    public void onSizeChanged(int width, int height) {
        mRecorder.onSizeChanged(width, height);
    }

    @Override
    public synchronized void stopPreview() {
        mRecorder.stopPreview();
    }

    @Override
    public void release() {
        if (mPartListeners != null) mPartListeners.clear();
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (mDefaultViewHandler != null) {
            mDefaultViewHandler.handleMessage(msg);
        }
    }

    @Override
    protected void handleMediaCaptureStarted(String output) {
        LogUtil.logd("handleMediaCaptureStarted : " + output);
        Part part = new Part(output);
        mParts.add(part);
        if (mPartListeners != null) {
            for (VideoPartListener listener : mPartListeners) {
                listener.onRecordVideoPartStarted(part);
            }
        }
    }

    @Override
    protected void handleMediaCaptureStopped(String output) {
        super.handleMediaCaptureStopped(output);
        LogUtil.logd("handleMediaCaptureStopped : " + output);
    }

    @Override
    protected void handleVideoMuxerStarted(String output) {
        super.handleVideoMuxerStarted(output);
        LogUtil.logd("handleVideoMuxerStarted : " + output);
    }

    @Override
    protected void handleVideoMuxerStopped(String output) {
        int i = mParts.indexOf(new Part(output));
        LogUtil.logd("handleVideoMuxerStopped : " + "index = " + i + "\t\t" + output);
        if (i < 0) return;
        Part part = mParts.get(i);
        part.end();
        handleMuxerEnd(part);
    }

    private void handleMuxerEnd(Part part) {
        if (mPartListeners != null) {
            if (!part.file.exists()) {
                for (VideoPartListener listener : mPartListeners) {
                    listener.onRecordVideoPartFailure(part);
                }
            } else {
                if (mFileFilter != null) {
                    if (mFileFilter.filter(part)) {
                        for (VideoPartListener listener : mPartListeners) {
                            listener.onRecordVideoPartSuccess(part);
                        }
                    } else {
                        for (VideoPartListener listener : mPartListeners) {
                            listener.onRecordVideoPartFailure(part);
                        }
                    }
                } else {
                    if (part.duration > 1000 && part.file.length() > 1000) {
                        for (VideoPartListener listener : mPartListeners) {
                            listener.onRecordVideoPartSuccess(part);
                        }
                    } else {
                        for (VideoPartListener listener : mPartListeners) {
                            listener.onRecordVideoPartFailure(part);
                        }
                    }

                }

            }
        }
    }

    public void removeAllPart() {
        mParts.clear();
    }

    public interface VideoMergeListener {
        void onStart();

        void onSuccess(File outFile);

        void onError(Exception e);

        /**
                   * Merger progress
                   *
                   * @param value 0 - 1
                   */
        void onProgress(float value);
    }

    public interface FileFilter {

        boolean filter(Part part);

    }

    public interface VideoPartListener {
        /**
                   * A video block starts recording
                   *
                   * @param part video block
                   */
        void onRecordVideoPartStarted(Part part);

        /**
                   * Complete recording of a video block
                   *
                   * @param part video block
                   * Judging whether the completion is based on the final file size and recording time
                   * Because I didn't find a good judgment condition, I used it roughly.
                   * part.file.exists() && part.duration > 300 && part.file.length() > 1000
                   * Judging, if the requirements are higher, you can use it yourself.
                   * MediaUtil.getVideoDuration(file.getAbsolutePath()); But getting this information takes about 100 milliseconds
                   */
        void onRecordVideoPartSuccess(Part part);

        /**
                   * A damaged video
                   *
                   * @param part video block
                   */
        void onRecordVideoPartFailure(Part part);
    }

    public static class Builder {

        private final VideoRecorder.Builder mBuilder;
        private final List<VideoPartListener> mVideoPartListeners = new ArrayList<>();
        private VideoMergeListener mMergeListener;
        private FileFilter mFileFilter;

        public Builder(VideoRecorder.Builder builder) {
            mBuilder = builder;
        }

        public MultiPartRecorder build() {
            MultiPartRecorder recorder = new MultiPartRecorder(mBuilder);
            for (VideoPartListener listener : mVideoPartListeners) {
                recorder.addPartListener(listener);
            }
            recorder.setMergeListener(mMergeListener);
            recorder.setFileFilter(mFileFilter);
            return recorder;
        }

        public Builder addPartListener(VideoPartListener videoPartListener) {
            mVideoPartListeners.add(videoPartListener);
            return this;
        }

        public Builder setFileFilter(FileFilter fileFilter) {
            mFileFilter = fileFilter;
            return this;
        }

        public Builder setMergeListener(VideoMergeListener mergeListener) {
            mMergeListener = mergeListener;
            return this;
        }
    }

    public void setFileFilter(FileFilter fileFilter) {
        mFileFilter = fileFilter;
    }


    public static class Part {
        /**
         * The approximate length of the video segment is calculated by starting the recording time and ending the recording time, including the error (coding performance).
         */
        public long duration;
        public long startTimeMillis;
        public long endTimeMillis = -1;
        public final File file;

        public Part(String output) {
            file = new File(output);
            startTimeMillis = System.currentTimeMillis();
        }

        public void end() {
            endTimeMillis = System.currentTimeMillis();
            duration = endTimeMillis - startTimeMillis;
//            duration = MediaUtil.getVideoDuration(file.getAbsolutePath());  //It takes 100 milliseconds
        }

        /**
         * @return Is recording
         */
        public boolean isRecording() {
            return endTimeMillis == -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Part part = (Part) o;
            return file.getPath().equals(part.file.getPath());
        }

        @Override
        public String toString() {
            return "Part{" +
                    "duration=" + duration +
                    ", startTimeMillis=" + startTimeMillis +
                    ", endTimeMillis=" + endTimeMillis +
                    ", file=" + file +
                    ", fileLength=" + file.length() +
                    '}';
        }

        @Override
        public int hashCode() {
            return file.getPath().hashCode();
//            return file.hashCode(); //Seems to be equal
        }
    }
}
