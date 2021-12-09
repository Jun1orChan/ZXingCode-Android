package com.nd.zxingcode.camera;

import android.graphics.Point;
import android.hardware.Camera;

public final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final CameraConfigurationManager mConfigManager;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.mConfigManager = configManager;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = mConfigManager.getCameraResolution();
        if (cameraResolution != null) {
            //2017.11.13 添加竖屏代码处理
            Point screenResolution = mConfigManager.getScreenResolution();
            if (screenResolution.x < screenResolution.y) {
                // portrait
            } else {
                // landscape
            }
        }
    }

}
