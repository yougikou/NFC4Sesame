package com.google.zxing.client.harmony.camera.open;

import ohos.app.Context;

public class OpenCameraInterface {
    private static final String TAG = "OpenCameraInterface";

    private OpenCameraInterface() {
    }

    /** For {@link # open(int)}, means no preference for which camera to open. */
    public static final int NO_REQUESTED_CAMERA = -1;

    public static OpenCamera open(Context context,String cameraId) {
        return null;
    }

}
