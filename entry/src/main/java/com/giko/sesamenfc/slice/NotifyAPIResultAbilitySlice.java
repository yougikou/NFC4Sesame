package com.giko.sesamenfc.slice;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.giko.sesamenfc.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;

public class NotifyAPIResultAbilitySlice extends AbilitySlice implements Runnable {
    private static final long DURATION = 3000;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(createComponent(intent.getIntParam("code", 0)));
        WindowManager windowManager = WindowManager.getInstance();
        Window window = windowManager.getTopWindow().get();
        window.setTransparent(true);
        new Thread(this).start();
    }

    private ComponentContainer createComponent(int code) {
        Component mainComponent = LayoutScatter.getInstance(this).parse(ResourceTable.Layout_result_layout,
                null, false);
        return (ComponentContainer) mainComponent;
    }


    @Override
    public void run() {
        try {
            Thread.sleep(DURATION);
            terminateAbility();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
