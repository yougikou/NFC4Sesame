/*
 * Copyright 2014 David Lázaro Esparcia.
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
package com.dlazaro66.qrcodereaderview;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.FormatException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.harmony.camera.BitmapLuminanceSource;
import com.google.zxing.client.harmony.camera.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import ohos.agp.components.AttrSet;
import ohos.agp.components.surfaceprovider.SurfaceProvider;
import ohos.agp.graphics.SurfaceOps;
import ohos.agp.utils.Point;
import ohos.app.Context;
import ohos.bundle.IBundleManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.media.camera.CameraKit;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Size;
import ohos.security.SystemPermission;
import ohos.utils.PacMap;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.logging.Logger;


/**
 * QRCodeReaderView Class which uses ZXING lib and let you easily integrate a QR decoder view.
 * Take some classes and made some modifications in the original ZXING - Barcode Scanner project.
 */
public class QRCodeReaderView extends SurfaceProvider implements SurfaceOps.Callback {

    private static OnQRCodeReadListener mOnQRCodeReadListener;
    private static final String TAG = "QRCodeReaderView";
    private int mPreviewWidth;
    private int mPreviewHeight;
    private CameraManager mCameraManager;
    private Map<DecodeHintType, Object> decodeHints;
    private DecodeTask mDecodeTask;
    private QRCodeReader mQRCodeReader;
    private boolean mQrDecodingEnabled = true;
    private boolean mSurfaceCreated = false;
    private static final int DECODE_SUCCEED = 100;
    private static int mPointSize = 0;
    private boolean mIsStartingCamera = false;


    @Override
    public void surfaceCreated(SurfaceOps surfaceOps) {
        Logger.getLogger(TAG).severe("surface created");
        initQRReader();
        mSurfaceCreated = true;

        if (getContext().verifySelfPermission(SystemPermission.CAMERA) == IBundleManager.PERMISSION_GRANTED) {
            startCamera();
            mIsStartingCamera = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceOps surfaceOps, int i, int i1, int i2) {
        Logger.getLogger(TAG).severe("surface surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceOps surfaceOps) {
        stopCamera();
        mSurfaceCreated = false;
        mIsStartingCamera = false;
        Logger.getLogger(TAG).severe("surface destory");
    }

    public boolean getSurfaceCreated(){
        return mSurfaceCreated;
    }

    public boolean isStartingCamera(){
        return mIsStartingCamera;
    }

    public interface OnQRCodeReadListener {
        void onQRCodeRead(String text, Point[] points);
    }

    public QRCodeReaderView(Context context) {
        super(context);
    }

    public QRCodeReaderView(Context context, AttrSet attrSet) {
        super(context, attrSet);
    }

    public QRCodeReaderView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
    }


    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware() {
        if (CameraKit.getInstance(getContext()) == null) {
            return false;
        }
        return true;
    }

    public void setQRDecodingEnabled(boolean qrDecodingEnabled) {
        this.mQrDecodingEnabled = qrDecodingEnabled;
    }

    public void setDecodeHints(Map<DecodeHintType, Object> decodeHints) {
        this.decodeHints = decodeHints;
    }

    /**
     * Set Torch enabled/disabled.
     * default value is false
     *
     * @param enabled torch enabled/disabled.
     */
    public void setTorchEnabled(boolean enabled) {
        if (mCameraManager != null) {
            mCameraManager.setTorchEnabled(enabled);
        }
    }

    /**
     * Starts camera preview and decoding
     */
    public void startCamera() {
        if (!mSurfaceCreated) {
            return;
        }
        mCameraManager.startPreview();
        Size size = mCameraManager.getSupportPictureSizes();
        setPreViewSize(size.width, size.height);
        doDecode();
    }

    /**
     * Stop camera preview and decoding
     */
    public void stopCamera() {
        stopDecode();
        mCameraManager.stopPreview();
    }

    public void setOnQRCodeReadListener(OnQRCodeReadListener onQRCodeReadListener) {
        mOnQRCodeReadListener = onQRCodeReadListener;
    }

    public void initQRReader() {
        mCameraManager = CameraManager.getInstance(getContext());
        mQRCodeReader = new QRCodeReader();
        mCameraManager.setPreViewSurface(getSurfaceOps().get().getSurface());
    }

    public void setPreViewSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }

    public void doDecode() {
        if (!mQrDecodingEnabled) {
            return;
        }
        if (!mSurfaceCreated) {
            return;
        }
        mCameraManager.isQROpen(true);
        mDecodeTask = new DecodeTask(this, decodeHints);
        mCameraManager.getEventHandler().postTask(mDecodeTask, 200);
    }

    public void stopDecode() {
        mCameraManager.isQROpen(false);
        mCameraManager.getEventHandler().removeTask(mDecodeTask);
    }

    private static EventHandler resultHandler = new EventHandler(EventRunner.getMainEventRunner()) {
        @Override
        protected void processEvent(InnerEvent event) {
            super.processEvent(event);
            if (event.eventId == DECODE_SUCCEED) {
                String result = (String) event.object;
                PacMap pac = event.getPacMap();
                Point[] points = new Point[mPointSize];
                for (int i = 0; i < mPointSize; ++i) {
                    points[i] = (Point) pac.getObjectValue("point" + i).get();
                }
                mOnQRCodeReadListener.onQRCodeRead(result, points);
            }
        }
    };


    private static class DecodeTask implements Runnable {
        private final WeakReference<QRCodeReaderView> viewRef;
        private final WeakReference<Map<DecodeHintType, Object>> hintsRef;
        private final QRToViewPointTransformer qrToViewPointTransformer =
                new QRToViewPointTransformer();

        DecodeTask(QRCodeReaderView view, Map<DecodeHintType, Object> hints) {
            viewRef = new WeakReference<>(view);
            hintsRef = new WeakReference<>(hints);
        }


        @Override
        public void run() {
            final QRCodeReaderView view = viewRef.get();
            if (view == null) {
                return;
            }

            while (true) {

                byte[] data = view.mCameraManager.getData();
                if (data != null) {
                    ImageSource imageSource = ImageSource.create(data, new ImageSource.SourceOptions());
                    ImageSource.DecodingOptions options = new ImageSource.DecodingOptions();
                    options.rotateDegrees = 90f;
                    PixelMap map = imageSource.createPixelmap(options);
                    BitmapLuminanceSource bitmapSource = new BitmapLuminanceSource(map);
                    BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(bitmapSource));

                    try {
                        Result result = view.mQRCodeReader.decode(binaryBitmap, hintsRef.get());
                        Logger.getLogger(TAG).severe("decode result = " + result);
                        handleQRResult(result, view);
                    } catch (ChecksumException e) {
                        Logger.getLogger(TAG).severe("ChecksumException");
                    } catch (NotFoundException e) {
                        Logger.getLogger(TAG).severe("QR Code not found");
                    } catch (FormatException e) {
                        Logger.getLogger(TAG).severe("FormatException, e = " + e);
                    } finally {
                        Logger.getLogger(TAG).severe("FormatException");
                        view.mCameraManager.clearData();
                    }
                }
            }
        }

        public void handleQRResult(Result result, QRCodeReaderView view) {
            // Notify we found a QRCode
            if (view != null && result != null && view.mOnQRCodeReadListener != null) {
                // Transform resultPoints to View coordinates
                final Point[] transformedPoints =
                        transformToViewCoordinates(view, result.getResultPoints());

                mPointSize = transformedPoints.length;
                InnerEvent innerEvent = InnerEvent.get(DECODE_SUCCEED, result.getText());
                PacMap pac = new PacMap();
                for (int i = 0; i < transformedPoints.length; ++i) {
                    Logger.getLogger(TAG).severe("handleQRResult, i =" + i + " transformedPoints[i] = " + transformedPoints[i]);
                    pac.putObjectValue("point" + i, transformedPoints[i]);
                }
                innerEvent.setPacMap(pac);
                resultHandler.sendEvent(innerEvent);
            }
        }


        /**
         * Transform result to surfaceView coordinates
         * <p>
         * This method is needed because coordinates are given in landscape camera coordinates when
         * device is in portrait mode and different coordinates otherwise.
         *
         * @return a new PointF array with transformed points
         */
        private Point[] transformToViewCoordinates(QRCodeReaderView view,
                                                   ResultPoint[] resultPoints) {
            int orientationDegrees = 0;// to calculate orientation degree.
            Orientation orientation =
                    orientationDegrees == 90 || orientationDegrees == 270 ? Orientation.PORTRAIT
                            : Orientation.LANDSCAPE;
            Point viewSize = new Point(view.getWidth(), view.getHeight());
            Point cameraPreviewSize = view.mCameraManager.getPreviewSize();
            boolean isMirrorCamera = false;

            return qrToViewPointTransformer.transform(resultPoints, isMirrorCamera, orientation,
                    viewSize, cameraPreviewSize);
        }
    }
}
