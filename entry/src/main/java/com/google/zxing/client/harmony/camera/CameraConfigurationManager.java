/*
 * Copyright (C) 2010 ZXing authors
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
import com.google.zxing.client.harmony.camera.open.OpenCamera;
import ohos.agp.utils.Point;
import ohos.agp.window.service.Display;
import ohos.agp.window.service.DisplayManager;
import ohos.app.Context;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.Camera;
import ohos.media.camera.device.CameraAbility;
import ohos.media.image.common.ImageFormat;
import ohos.media.image.common.Size;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    // This is bigger than the size of a small screen, which is still supported. The routine
    // below will still select the default (presumably 320x240) size for these. This prevents
    // accidental selection of very low resolution on some devices.
    private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
    private static final int MAX_PREVIEW_PIXELS = 1280 * 720;
    private static final float MAX_EXPOSURE_COMPENSATION = 1.5f;
    private static final float MIN_EXPOSURE_COMPENSATION = 0.0f;
    private final Context context;

    private Point resolution;
    private Point cameraResolution;
    private Point bestPreviewSize;
    private Point previewSizeOnScreen;
    private int cwRotationFromDisplayToCamera;
    private int cwNeededRotation;
    private CameraAbility cameraAbility;
    private DisplayManager displayManager;
    private Display display;
    private CameraKit cameraKit;


    CameraConfigurationManager(Context context) {
        this.context = context;
    }


    void initFromCameraParameters(OpenCamera camera, int width, int height) {

        cameraKit = CameraKit.getInstance(context);
        cameraAbility = cameraKit.getCameraAbility(camera.getCameraId());
        displayManager = DisplayManager.getInstance();
        Optional<Display> defaultDisplay = displayManager.getDefaultDisplay(context);
        if (defaultDisplay.isPresent()) {
            display = defaultDisplay.get();
        }

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;

        int cwRotationFromNaturalToCamera = 1;//camera.getOrientation();
        SimpleLog.i(TAG, "Camera at: " + cwRotationFromNaturalToCamera);

        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera.getFacing() == 0) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
            SimpleLog.i(TAG, "Front camera overriden to: " + cwRotationFromNaturalToCamera);
        }

        cwRotationFromDisplayToCamera = 0;
        //  (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
        SimpleLog.i(TAG, "Final display orientation: " + cwRotationFromDisplayToCamera);
        if (camera.getFacing() == 1) {
            SimpleLog.i(TAG, "Compensating rotation for front camera");
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }
        SimpleLog.i(TAG, "Clockwise rotation from display to camera: " + cwNeededRotation);

        resolution = new Point(width, height);
        SimpleLog.i(TAG, "Screen resolution in current orientation: " + resolution);
        SimpleLog.i(TAG, "Preview size on screen: " + previewSizeOnScreen);
    }

    void setDesiredCameraParameters(OpenCamera camera, boolean safeMode) {

        Camera theCamera = camera.getCamera();
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return resolution;
    }

    // All references to Torch are removed from here, methods, variables...


    public Point findBestPreviewSizeValue(CameraAbility cameraAbility, Point screenResolution) {
        //根据图片格式获取支持的图片输出尺寸
        List<Size> rawSupportedSizes = cameraAbility.getSupportedSizes(ImageFormat.UNKNOWN);
        return null;
    }


    private static String findSettableValue(String name,
                                            Collection<String> supportedValues,
                                            String... desiredValues) {
        SimpleLog.i(TAG, "Requesting " + name + " value from among: " + Arrays.toString(desiredValues));
        SimpleLog.i(TAG, "Supported " + name + " values: " + supportedValues);
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    SimpleLog.i(TAG, "Can set " + name + " to: " + desiredValue);
                    return desiredValue;
                }
            }
        }
        SimpleLog.i(TAG, "No supported values match");
        return null;
    }

    boolean getTorchState(Camera camera) {
        return false;
    }

    void setTorchEnabled(Camera camera, boolean enabled) {
    }


}
