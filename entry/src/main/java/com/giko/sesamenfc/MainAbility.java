package com.giko.sesamenfc;

import com.giko.sesamenfc.cmd.QRCodeInfo;
import com.giko.sesamenfc.cmd.SesameCmd;
import com.giko.sesamenfc.persist.SettingsDatabaseHelper;
import com.giko.sesamenfc.slice.NotifyAPIResultAbilitySlice;
import com.giko.sesamenfc.slice.SettingsAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.window.dialog.ToastDialog;

public class MainAbility extends Ability {

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final String[] permissions = {
            "ohos.permission.CAMERA",
            "ohos.permission.INTERNET"
    };

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(SettingsAbilitySlice.class.getName());
        super.addActionRoute("action.show.result", NotifyAPIResultAbilitySlice.class.getName());

        if (intent.getStringParam("startFromLauncher") == null){
            SettingsDatabaseHelper helper = SettingsDatabaseHelper.getInstance(this);
            QRCodeInfo info = helper.getQRCodeInfoSet();
            String apiKey = helper.getAPIKey();
            if (info != null && apiKey != null & apiKey.length() > 0) {
                SesameCmd cmd = new SesameCmd(info, apiKey, SesameCmd.TOGGLE);
                int code = cmd.executeCmdSynchronously();
                if (code == 200) {
                    Intent newIntent = new Intent();
                    newIntent.setParam("code", code);
                    Operation operation = new Intent.OperationBuilder()
                            .withAction("action.show.result")
                            .build();
                    intent.setOperation(operation);
                    startAbility(newIntent);
                } else {
                    new ToastDialog(this)
                            .setText("调用开锁WebAPI发生错误，代码:" + code)
                            .setAlignment(1)
                            .setDuration(2000)
                            .show();
                }
            }
        }
        requestPermissionsFromUser(permissions, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsFromUserResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsFromUserResult(requestCode, permissions, grantResults);
        switch (requestCode) {

        }
    }
}