package com.giko.sesamenfc.slice;

import com.giko.sesamenfc.ResourceTable;
import com.giko.sesamenfc.cmd.QRCodeInfo;
import com.giko.sesamenfc.cmd.SesameCmd;
import com.giko.sesamenfc.persist.SettingsDatabaseHelper;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.agp.window.dialog.ToastDialog;

/**
 * Settings List Ability Slice
 */
public class SettingsAbilitySlice extends AbilitySlice {
    private static final int QR_SCAN_REQUEST_CODE = 1;

    @Override
    public void onStart(Intent intent) {
        // Database helper
        setUIContent(createComponent());
    }

    private ComponentContainer createComponent() {
        Component mainComponent = LayoutScatter.getInstance(this).parse(ResourceTable.Layout_main_ability,
                null, false);
        DependentLayout backButton = (DependentLayout) mainComponent.findComponentById(ResourceTable
                .Id_title_area_back_icon_hot_area);
        backButton.setClickedListener(component -> this.terminate());
        DependentLayout addButton = (DependentLayout) mainComponent.findComponentById(ResourceTable
                .Id_title_area_add);
        addButton.setClickedListener(listener -> presentForResult(new ScanAbilitySlice(),new Intent(), QR_SCAN_REQUEST_CODE));

        QRCodeInfo info = SettingsDatabaseHelper.getInstance(this).getQRCodeInfoSet();
        if (info != null) {
            ((Text)mainComponent.findComponentById(ResourceTable.Id_sesame_name_value))
                    .setText(info.getName());
            ((Text)mainComponent.findComponentById(ResourceTable.Id_sesame_uuid_value))
                    .setText(info.getUuid());
        } else {
            ((Text)mainComponent.findComponentById(ResourceTable.Id_sesame_name_value))
                    .setText("<未设定>");
            ((Text)mainComponent.findComponentById(ResourceTable.Id_sesame_uuid_value))
                    .setText("<未设定>");
        }

        String savedApiKey = SettingsDatabaseHelper.getInstance(this).getAPIKey();
        TextField apiKeyText = (TextField)mainComponent.findComponentById(ResourceTable.Id_sesame_api_key_value);
        if (savedApiKey != null) {
            apiKeyText.setText(savedApiKey);
        }

        Button apikeyButton = (Button)mainComponent.findComponentById(ResourceTable.Id_test_and_save_button);
        apikeyButton.setClickedListener(listener -> {
            String apiKey = ((TextField)findComponentById(ResourceTable.Id_sesame_api_key_value)).getText();
            if (apiKey == null || apiKey.length() == 0) {
                new ToastDialog(this)
                        .setText("请先输入API Key")
                        .setAlignment(1)
                        .setDuration(2000)
                        .show();
                return;
            }
            QRCodeInfo savedInfo = SettingsDatabaseHelper.getInstance(this).getQRCodeInfoSet();
            if (savedInfo == null) {
                new ToastDialog(this)
                        .setText("无已登录的智能门锁QR钥匙")
                        .setAlignment(1)
                        .setDuration(2000)
                        .show();
                return;
            }
            SesameCmd cmd = new SesameCmd(savedInfo, apiKey, SesameCmd.TOGGLE);
            int code = cmd.executeCmdSynchronously();
            if (code == 200) {
                SettingsDatabaseHelper.getInstance(this).setAPIKey(apiKey);
            }
        });
        return (ComponentContainer) mainComponent;
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    protected void onResult(int requestCode, Intent resultIntent) {
        if (requestCode == QR_SCAN_REQUEST_CODE) {
            // Process resultIntent here.
            String resultString = resultIntent.getStringParam("sesameQRText");
            if (resultString == null)
                return;
            if (resultString.startsWith(QRCodeInfo.PROTOCOL_HEADER)) {
                QRCodeInfo info = new QRCodeInfo(resultString);
                SettingsDatabaseHelper.getInstance(this).addQRCodeInfo(info);
                setUIContent(createComponent());
            } else {
                new ToastDialog(this)
                        .setText("无效二维码信息，Sesame智能门锁登录失败。")
                        .setAlignment(1)
                        .setDuration(2000)
                        .show();
            }
        }
    }
}
