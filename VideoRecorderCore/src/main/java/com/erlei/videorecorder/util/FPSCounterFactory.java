package com.erlei.videorecorder.util;

import java.util.ArrayList;

public class FPSCounterFactory {


    public static FPSCounter getDefaultFPSCounter() {
        return new FPSCounter2();
    }

    public static abstract class FPSCounter {
        public abstract float getFPS();
    }

    /**
           * Real-time calculation
           * Calculated using the time interval of the previous frame
           */
    public static class FPSCounter2 extends FPSCounter {
        private long lastFrame = System.nanoTime();
        private float FPS = 0;

        public float getFPS() {
            checkFPS();
            return FPS;
        }

        public void checkFPS() {
            long time = (System.nanoTime() - lastFrame);
            FPS = 1 / (time / 1000000000.0f);
            lastFrame = System.nanoTime();
        }

    }

    /**
           * Precise sampling
           * N frames before sampling, then calculate the average
           */
    public static class FPSCounter1 extends FPSCounter {
        ArrayList<Long> lst = new ArrayList<>();
        private long msPerFrame = 1;
        private long l;
        static final int frame = 30;

        private void update() {
            long currentTime = System.nanoTime();
            msPerFrame = currentTime - l;
            l = currentTime;
            synchronized (this) {
                lst.add(msPerFrame);
                if (lst.size() > frame) {
                    lst.remove(0);
                }
            }
        }

        @Override
        public float getFPS() {
            update();
            long sum = 0;
            for (Long aLong : lst) {
                sum += aLong;
            }
            return (float) (1e9 * lst.size() / sum);
        }
    }
}
