/*
 * Copyright (C) 2008 ZXing authors
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

import com.google.zxing.PlanarYUVLuminanceSource;
import ohos.agp.graphics.Surface;
import ohos.agp.utils.Point;
import ohos.agp.utils.TextTool;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraInfo;
import ohos.media.camera.device.FrameResult;
import ohos.media.camera.device.FrameStateCallback;
import ohos.media.camera.device.CameraConfig;
import ohos.media.camera.device.CameraStateCallback;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.params.Metadata;
import ohos.media.image.Image;
import ohos.media.image.ImageReceiver;
import ohos.media.image.common.ImageFormat;
import ohos.media.image.common.Size;


import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static ohos.media.camera.device.Camera.FrameConfigType.FRAME_CONFIG_PREVIEW;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
public final class CameraManager {

    private static final String TAG = "CameraManager";


    private final Context context;
    private static CameraManager instance;
    private CameraKit mCameraKit;
    EventHandler eventHandler = new EventHandler(EventRunner.create("CameraCb"));
    private Surface mPreviewSurface;
    private FrameConfig.Builder mPreviewFrameConfig;
    private ImageReceiver imageReceiver;
    private Size pictureSize;
    private byte[] data;
    private Camera mCamera;
    private String mCameraId;
    private boolean isQROpen = false;


    public static CameraManager getInstance(Context context) {
        if (instance == null) {
            instance = new CameraManager(context);
        }
        return instance;
    }

    private CameraManager(Context context) {
        this.context = context;
        Logger.getLogger(TAG).severe("CameraManager init");
    }


    //init cameraKit
    public void initCameraKit() {
        mCameraKit = CameraKit.getInstance(context);
    }


    //get a camera instance
    public Camera getmCamera() {
        return mCamera;
    }


    //set surface
    public void setPreViewSurface(Surface surface) {
        mPreviewSurface = surface;
    }

    //return cameraId. facing : CameraInfo.FacingType.CAMERA_FACING_BACK
    public String getCameraIdByFacing(int facing) {
        String[] cameraIds = mCameraKit.getCameraIds();
        for (String cid : cameraIds) {
            CameraInfo cameraInfo = mCameraKit.getCameraInfo(cid);
            if (TextTool.isNullOrEmpty(mCameraId)) {
                Logger.getLogger(TAG).severe("cameraInfo.getFacingType()   = " + cameraInfo.getFacingType());
                if (cameraInfo.getFacingType() == facing) {
                    mCameraId = cid;
                    Logger.getLogger(TAG).severe("getCameraIdByFacing ,cameraId =" + mCameraId + "  info =" + cameraInfo.toString());
                    break;
                }
            }
        }
        return mCameraId;
    }

    public Size getSupportPictureSizes() {
        return pictureSize;
    }


    //create camera
    public void createCamera(String cameraId) {
        ininReceiver(cameraId);
        mCameraKit.createCamera(cameraId, cameraStateCallback, eventHandler);
    }

    public void ininReceiver(String cameraId) {
        pictureSize = mCameraKit.getCameraAbility(cameraId).getSupportedSizes(ImageFormat.JPEG).get(0);//ImageFormat.JPEG
        //ImageFormat.YUV420_888
        imageReceiver = ImageReceiver.create(Math.max(pictureSize.width, pictureSize.height),
                Math.min(pictureSize.width, pictureSize.height), ImageFormat.JPEG, 3); // 创建ImageReceiver对象，注意creat函数中宽度要大于高度；5为最大支持的图像数，请根据实际设置。

        imageReceiver.setImageArrivalListener(imageArrivalListener);
    }

    public void  setTorchEnabled(boolean enabled) {
        if (enabled) {
            if ((mPreviewFrameConfig != null) && (mPreviewFrameConfig.getFlashMode() == Metadata.FlashMode.FLASH_OPEN)) {
                return;
            }
            mPreviewFrameConfig.setFlashMode(Metadata.FlashMode.FLASH_ALWAYS_OPEN);
        } else {
            if ((mPreviewFrameConfig != null) && (mPreviewFrameConfig.getFlashMode() == Metadata.FlashMode.FLASH_CLOSE)) {
                return;
            }
            mPreviewFrameConfig.setFlashMode(Metadata.FlashMode.FLASH_CLOSE);
        }
        Camera camera = instance.getmCamera();
        if(camera == null){
            return;
        }
        instance.getmCamera().triggerLoopingCapture(mPreviewFrameConfig.build());


//        int[] flashModes = mCameraKit.getCameraAbility(mCameraId).getSupportedFlashMode();
//        boolean success = false;
//        int flashMode = 0;
//        if (enabled) {
//            flashMode = 1;
//        }
//        for (int mode : flashModes) {
//            if (mode == flashMode) {
//                mCameraKit.setFlashlight(mCameraId, enabled);
//                mPreviewFrameConfig.setFlashMode(flashMode);
//                success = true;
//                break;
//            }
//        }
//        if (!success && flashMode == 0) {
//            new ToastDialog(context)
//                    .setText("Don't support Flash Mode!!!")
//                    .setAlignment(LayoutAlignment.BOTTOM)
//                    .show();
//        }
    }

    class MyCameraStateCallback extends CameraStateCallback {

        @Override
        public void onCreated(Camera camera) {
            super.onCreated(camera);
            mCamera = camera;
            CameraConfig.Builder cameraConfigBuilder = camera.getCameraConfigBuilder();
            if (cameraConfigBuilder == null) {
                Logger.getLogger(TAG).severe("onCreated cameraConfigBuilder is null");
                return;
            }
            // 配置预览的Surface

            cameraConfigBuilder.addSurface(mPreviewSurface);
            // 配置拍照的Surface
            cameraConfigBuilder.addSurface(imageReceiver.getRecevingSurface());

            // 配置帧结果的回调
            cameraConfigBuilder.setFrameStateCallback(frameStateCallback, EventHandler.current());

            try {
                // 相机设备配置
                camera.configure(cameraConfigBuilder.build());
            } catch (IllegalArgumentException e) {
                Logger.getLogger(TAG).severe("Argument  Exception");
            } catch (IllegalStateException e) {
                Logger.getLogger(TAG).severe("State  Exception");
            }

        }

        @Override
        public void onCreateFailed(String cameraId, int errorCode) {
            super.onCreateFailed(cameraId, errorCode);
            Logger.getLogger(TAG).severe("onCreateFailed  errorCode=" + errorCode);
        }

        @Override
        public void onConfigured(Camera camera) {
            super.onConfigured(camera);
            mPreviewFrameConfig = camera.getFrameConfigBuilder(FRAME_CONFIG_PREVIEW);
            mPreviewFrameConfig.addSurface(mPreviewSurface);
            mPreviewFrameConfig.addSurface(imageReceiver.getRecevingSurface());

            Logger.getLogger(TAG).severe("cameraStateCallback ,onConfigured   imageReceiver.getRecevingSurface()=" + imageReceiver.getRecevingSurface());
            try {
                camera.triggerLoopingCapture(mPreviewFrameConfig.build());

            } catch (IllegalArgumentException e) {
                Logger.getLogger(TAG).severe("State IllegalArgumentException");
            } catch (IllegalStateException e) {
                Logger.getLogger(TAG).severe("State IllegalStateException");
            }
        }

        @Override
        public void onPartialConfigured(Camera camera) {
            super.onPartialConfigured(camera);
        }

        @Override
        public void onConfigureFailed(Camera camera, int errorCode) {
            super.onConfigureFailed(camera, errorCode);
        }

        @Override
        public void onReleased(Camera camera) {
            super.onReleased(camera);
            Logger.getLogger(TAG).severe("onReleased  onReleased");
        }

        @Override
        public void onFatalError(Camera camera, int errorCode) {
            super.onFatalError(camera, errorCode);
            Logger.getLogger(TAG).severe("onFatalError  camera = " + camera + "  errorCode=" + errorCode);
        }

        @Override
        public void onCaptureRun(Camera camera) {
            super.onCaptureRun(camera);
        }

        @Override
        public void onCaptureIdle(Camera camera) {
            super.onCaptureIdle(camera);
        }


    }

    private MyCameraStateCallback cameraStateCallback = new MyCameraStateCallback() ;

    class MyFrameStateCallback extends  FrameStateCallback{

        @Override
        public void onFrameStarted(Camera camera, FrameConfig frameConfig, long frameNumber, long timestamp) {
            super.onFrameStarted(camera, frameConfig, frameNumber, timestamp);
        }

        @Override
        public void onFrameProgressed(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameProgressed(camera, frameConfig, frameResult);
        }

        @Override
        public void onFrameFinished(Camera camera, FrameConfig frameConfig, FrameResult frameResult) {
            super.onFrameFinished(camera, frameConfig, frameResult);
        }

        @Override
        public void onFrameError(Camera camera, FrameConfig frameConfig, int errorCode, FrameResult frameResult) {
            super.onFrameError(camera, frameConfig, errorCode, frameResult);
        }

        @Override
        public void onCaptureTriggerStarted(Camera camera, int captureTriggerId, long firstFrameNumber) {
            super.onCaptureTriggerStarted(camera, captureTriggerId, firstFrameNumber);
            Logger.getLogger(TAG).severe("onCaptureTriggerStarted ");
        }

        @Override
        public void onCaptureTriggerFinished(Camera camera, int captureTriggerId, long lastFrameNumber) {
            super.onCaptureTriggerFinished(camera, captureTriggerId, lastFrameNumber);
            Logger.getLogger(TAG).severe("onCaptureTriggerFinished ");
        }

        @Override
        public void onCaptureTriggerInterrupted(Camera camera, int captureTriggerId) {
            super.onCaptureTriggerInterrupted(camera, captureTriggerId);
        }


    }
    private FrameStateCallback frameStateCallback = new MyFrameStateCallback() ;

    // 单帧捕获生成图像回调Listener
    private final ImageReceiver.IImageArrivalListener imageArrivalListener = new ImageReceiver.IImageArrivalListener() {
        @Override
        public void onImageArrival(ImageReceiver imageReceiver) {
            Image image = imageReceiver.readLatestImage();
            if (image == null) {
                return;
            }
            if (isQROpen && data == null) {
                data = translateImageToJPEG(image);
            }
            image.release();
        }
    };


    public void isQROpen(boolean state) {
        isQROpen = state;
    }

    private byte[] translateImageToJPEG(Image image) {

        Size imageSize = image.getImageSize();
        Image.Component component = image.getComponent(ImageFormat.ComponentType.JPEG);
        byte[] data = new byte[component.remaining()];
        ByteBuffer buffer = component.getBuffer();
        buffer.get(data);
        return data;
    }
    


    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public byte[] getData() {
        return data;
    }

    public void clearData() {
        data = null;
    }

    public Point getPreviewSize() {
        Point point = new Point(pictureSize.width, pictureSize.height);
        return point;
    }

    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        return new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
    }


    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        initCameraKit();
        String cameraId = getCameraIdByFacing(CameraInfo.FacingType.CAMERA_FACING_BACK);
        createCamera(cameraId);
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        if (mCamera != null) {
            mCamera.stopLoopingCapture();
            mCamera.release();
            mCamera = null;
            eventHandler.removeAllEvent();
        }

    }
}
