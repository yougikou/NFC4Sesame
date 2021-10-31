package com.giko.sesamenfc.slice;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.giko.sesamenfc.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.utils.Point;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;


public class ScanAbilitySlice extends AbilitySlice implements QRCodeReaderView.OnQRCodeReadListener {
    private static final String TAG = "MainAbility";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_scan_layout);
        WindowManager windowManager = WindowManager.getInstance();
        Window window = windowManager.getTopWindow().get();
        window.setTransparent(true);

        QRCodeReaderView mQrCodeReaderView = (QRCodeReaderView) findComponentById(ResourceTable.Id_qrcoderView);
        mQrCodeReaderView.getSurfaceOps().get().addCallback(mQrCodeReaderView);
        mQrCodeReaderView.setOnQRCodeReadListener(this);
    }

    @Override
    protected void onActive() {
        super.onActive();
        QRCodeReaderView mQrCodeReaderView = (QRCodeReaderView) findComponentById(ResourceTable.Id_qrcoderView);
        if (mQrCodeReaderView != null) {
            mQrCodeReaderView.startCamera();
        }
//        Logger.getLogger(TAG).severe("onActive  mQrCodeReaderView =" + mQrCodeReaderView);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        QRCodeReaderView mQrCodeReaderView = (QRCodeReaderView) findComponentById(ResourceTable.Id_qrcoderView);
        if (mQrCodeReaderView != null) {
            mQrCodeReaderView.stopCamera();
        }
//        Logger.getLogger(TAG).severe("onInactive  mCheckBox_2.isChecked() =" + mCheckBox_2.isChecked());
    }

    @Override
    protected void onBackground() {
        super.onBackground();
//        Logger.getLogger(TAG).severe("onBackground  mQrCodeReaderView =" + mQrCodeReaderView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        QRCodeReaderView mQrCodeReaderView = (QRCodeReaderView) findComponentById(ResourceTable.Id_qrcoderView);
        if (mQrCodeReaderView != null) {
            mQrCodeReaderView.stopCamera();
        }
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed
    @Override
    public void onQRCodeRead(String text, Point[] points) {
        Intent resultIntent = new Intent();
        resultIntent.setParam("sesameQRText",text);
        setResult(resultIntent);
        terminate();
//        mQrCodeReaderView.stopDecode();
    }
}
