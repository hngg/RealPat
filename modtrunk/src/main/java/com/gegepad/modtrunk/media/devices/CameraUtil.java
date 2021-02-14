package com.gegepad.modtrunk.media.devices;

import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class CameraUtil {

    private static final String TAG = "CameraUtil";

    /**
     * 照相最佳的分辨率
     *
     * @param sizes
     * @return
     */
    public static Camera.Size findBestSizeValue(List<Camera.Size> sizes, int w, int h, double minDiff) {

        //摄像头这个size里面都是w > h
        if (w < h) {
            int t = h;
            h = w;
            w = t;
        }

        double targetRatio = (double) w / h;
        Log.e(TAG, "照相尺寸  w:" + w + "  h:" + h + "  targetRatio:" + targetRatio + "  minDiff:" + minDiff);
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            // 如果有符合的分辨率，则直接返回
//            if (size.width == defaultWidth && size.height == defaultHeight) {
//                Log.e(TAG, "get default preview size!!!");
//                return size;
//            }
//            if (size.width < MIN_PICTURE_WIDTH || size.height < MIN_PICTURE_HEIGHT) {
//                continue;
//            }

            double ratio = (double) size.width / size.height;

            double diff = Math.abs(ratio - targetRatio);

            Log.e(TAG, "照相支持尺寸  width:" + size.width + "  height:" + size.height + "  targetRatio:" + targetRatio + "" +
                    "  ratio:" + ratio + "   diff:" + diff);

            if (diff > minDiff) {
                continue;
            }

            if (optimalSize == null) {
                optimalSize = size;
            } else {
                if (optimalSize.width * optimalSize.height < size.width * size.height) {
                    optimalSize = size;
                }
            }
            minDiff = diff;
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff += 0.1f;
            if (minDiff > 1.0f) {
                optimalSize = sizes.get(0);
            } else {
                optimalSize = findBestSizeValue(sizes, w, h, minDiff);
            }
        }
        if (optimalSize != null)
            Log.e(TAG, "照相best尺寸  " + optimalSize.width + "  " + optimalSize.height);
        return optimalSize;

    }

}
