/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.harmony.camera;

import ohos.app.Context;
import ohos.media.camera.CameraKit;
import ohos.media.camera.device.CameraInfo;

import java.util.List;

public class CameraInfoImpl implements CameraInfo {
    @Override
    public String getLogicalId() {
        return null;
    }

    @Override
    public int getFacingType() {
        return 0;
    }

    @Override
    public List<String> getPhysicalIdList() {
        return null;
    }

    public List<String> getPhysicalIdList(Context context, String LogicalCameraId) {
        CameraKit cameraKit = CameraKit.getInstance(context);
        // CameraInfo cameraInfo = new cameraKit.getCameraInfo(LogicalCameraId);
        return null;
    }


    @Override
    public int getDeviceLinkType(String s) {
        return 0;
    }
}
