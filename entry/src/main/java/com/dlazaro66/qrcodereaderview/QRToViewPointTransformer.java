package com.dlazaro66.qrcodereaderview;

import com.google.zxing.ResultPoint;
import ohos.agp.utils.Point;

public class QRToViewPointTransformer {

    public Point[] transform(ResultPoint[] qrPoints, boolean isMirrorPreview,
                             Orientation orientation,
                             Point viewSize, Point cameraPreviewSize) {
        Point[] transformedPoints = new Point[qrPoints.length];
        int index = 0;
        for (ResultPoint qrPoint : qrPoints) {
            Point transformedPoint = transform(qrPoint, isMirrorPreview, orientation, viewSize,
                    cameraPreviewSize);
            transformedPoints[index] = transformedPoint;
            index++;
        }
        return transformedPoints;
    }

    public Point transform(ResultPoint qrPoint, boolean isMirrorPreview, Orientation orientation,
                           Point viewSize, Point cameraPreviewSize) {
        float previewX = cameraPreviewSize.getPointX();
        float previewY = cameraPreviewSize.getPointY();

        Point transformedPoint = null;
        float scaleX;
        float scaleY;

        if (orientation == Orientation.PORTRAIT) {
            scaleX = viewSize.getPointX() / previewY;
            scaleY = viewSize.getPointY() / previewX;
            float pointX = (previewY - qrPoint.getY()) * scaleX;
            float pointY = qrPoint.getX() * scaleY;
            if (isMirrorPreview) {
                pointY = viewSize.getPointY() - pointY;
            }
            transformedPoint = new Point(pointX, pointY);
        } else if (orientation == Orientation.LANDSCAPE) {
            scaleX = viewSize.getPointX() / previewX;
            scaleY = viewSize.getPointY() / previewY;
            float pointX = viewSize.getPointX() - qrPoint.getX() * scaleX;
            float pointY = viewSize.getPointY() - qrPoint.getY() * scaleY;
            if (isMirrorPreview) {
                pointX = viewSize.getPointX() - pointX;
            }
            transformedPoint = new Point(pointX, pointY);
        }
        return transformedPoint;
    }
}
