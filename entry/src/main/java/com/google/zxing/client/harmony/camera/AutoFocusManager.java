/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * -- Class modifications
 *
 * Copyright 2016 David Lázaro Esparcia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.harmony.camera;

import com.dlazaro66.qrcodereaderview.SimpleLog;
import ohos.agp.render.render3d.Task;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.FrameConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

//implements CameraKit.AutoFocusCallback
final class AutoFocusManager {

    private static final String TAG = AutoFocusManager.class.getSimpleName();

    protected static final long DEFAULT_AUTO_FOCUS_INTERVAL_MS = 5000L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    private long autofocusIntervalMs = DEFAULT_AUTO_FOCUS_INTERVAL_MS;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>(2);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    //private final CameraKit camera;
    private final FrameConfig.Builder camera1;
    //private AsyncTask<?, ?, ?> outstandingTask;
    private Task outstandingTask;

    AutoFocusManager(FrameConfig.Builder camera) {
        this.camera1 = camera;
        int currentFocusMode = camera.getAfMode();//getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        SimpleLog.i(TAG, "Current focus mode '"
                + currentFocusMode
                + "'; use auto focus? "
                + useAutoFocus);
        start();
    }

    public synchronized void onAutoFocus(boolean success, CameraKit theCamera) {
        focusing = false;
        autoFocusAgainLater();
    }

    public void setAutofocusInterval(long autofocusIntervalMs) {
        if (autofocusIntervalMs <= 0) {
            throw new IllegalArgumentException("AutoFocusInterval must be greater than 0.");
        }
        this.autofocusIntervalMs = autofocusIntervalMs;
    }

    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                SimpleLog.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    synchronized void start() {
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    camera1.setAfTrigger(0);//触发自动对焦
                    focusing = true;
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+; continue?
                    SimpleLog.w(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }

    private synchronized void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getState() != Task.State.FINISHED) {
                outstandingTask.onCancel();
            }
            outstandingTask = null;
        }
    }

    synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            // Doesn't hurt to call this even if not focusing
            try {
                // camera1.cancelAutoFocus();
            } catch (RuntimeException re) {
                // Have heard RuntimeException reported in Android 4.0.x+; continue?
                SimpleLog.w(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }

    private final class AutoFocusTask extends Task {
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(autofocusIntervalMs);
            } catch (InterruptedException e) {
                // continue
            }
            start();
            return null;
        }

        @Override
        public void onInitialize() {

        }

        @Override
        public boolean onExecute() {
            return false;
        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onCancel() {

        }
    }
}
