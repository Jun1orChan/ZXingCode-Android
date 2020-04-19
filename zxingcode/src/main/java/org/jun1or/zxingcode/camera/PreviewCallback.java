package org.jun1or.zxingcode.camera;

import android.graphics.Point;
import android.hardware.Camera;

public final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewCallback.class.getSimpleName();

    private final CameraConfigurationManager configManager;

    PreviewCallback(CameraConfigurationManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        if (cameraResolution != null) {
            //2017.11.13 添加竖屏代码处理
            Point screenResolution = configManager.getScreenResolution();
            if (screenResolution.x < screenResolution.y) {
                // portrait
            } else {
                // landscape
            }
        }
    }

}
