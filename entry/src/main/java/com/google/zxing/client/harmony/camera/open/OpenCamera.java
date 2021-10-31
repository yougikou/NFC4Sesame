package com.google.zxing.client.harmony.camera.open;


import ohos.media.camera.device.Camera;


public final class OpenCamera {
    private final String cameraId;
    private final Camera camera;
    private final int facingType;


    public OpenCamera(String cameraId, Camera camera, int facingType) {
        this.cameraId = cameraId;
        this.camera = camera;
        this.facingType = facingType;
    }

    public String getCameraId() {
        return cameraId;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getFacing() {
        return facingType;
    }

    @Override
    public String toString() {
        return "Camera #" + cameraId + " : " + facingType;
    }

}

