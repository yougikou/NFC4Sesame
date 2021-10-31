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


import com.google.zxing.LuminanceSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.Rect;

public class BitmapLuminanceSource extends LuminanceSource {
    private byte[] bitmapPixels;

    public BitmapLuminanceSource(PixelMap pixelMap) {
        super(pixelMap.getImageInfo().size.width, pixelMap.getImageInfo().size.height);

        // 首先，要取得该图片的像素数组内容
        int[] data = new int[getWidth() * getHeight()];
        this.bitmapPixels = new byte[getWidth() * getHeight()];
        pixelMap.readPixels(data, 0, getWidth(), new Rect(0, 0, getWidth(), getHeight()));
        // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
        for (int i = 0; i < data.length; i++) {
            this.bitmapPixels[i] = (byte) data[i];
        }
    }

    @Override
    public byte[] getMatrix() {
        // 返回我们生成好的像素数据
        return bitmapPixels;
    }

    @Override
    public byte[] getRow(int y, byte[] row) {
        // 这里要得到指定行的像素数据
        System.arraycopy(bitmapPixels, y * getWidth(), row, 0, getWidth());
        return row;
    }
}