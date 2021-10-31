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

import ohos.agp.graphics.Surface;
import ohos.agp.utils.Rect;
import ohos.location.Location;
import ohos.media.camera.device.FrameConfig;
import ohos.media.camera.params.ParameterKey.Key;

import java.util.List;

public class FrameConfigImpl implements FrameConfig {


    @Override
    public int getFrameConfigType() {
        return 0;
    }

    @Override
    public List<Surface> getSurfaces() {
        return null;
    }

    @Override
    public Surface getCoordinateSurface() {
        return null;
    }

    @Override
    public int getAfMode() {
        return 0;
    }

    @Override
    public Rect getAfRect() {
        return null;
    }

    @Override
    public int getAfTrigger() {
        return 0;
    }

    @Override
    public int getAeMode() {
        return 0;
    }

    @Override
    public Rect getAeRect() {
        return null;
    }

    @Override
    public int getAeTrigger() {
        return 0;
    }

    @Override
    public int getAwbMode() {
        return 0;
    }

    @Override
    public float getZoomValue() {
        return 0;
    }

    @Override
    public int getFlashMode() {
        return 0;
    }

    @Override
    public int getFaceDetectionType() {
        return 0;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public int getImageRotation() {
        return 0;
    }

    @Override
    public <T> T get(Key<T> key) {
        return null;
    }

    @Override
    public List<Key<?>> getKeys() {
        return null;
    }

    @Override
    public Object getMark() {
        return null;
    }
}

