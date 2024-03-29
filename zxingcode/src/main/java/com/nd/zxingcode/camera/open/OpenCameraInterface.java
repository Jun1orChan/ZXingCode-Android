package com.nd.zxingcode.camera.open;

import android.hardware.Camera;

/**
 * Abstraction over the {@link Camera} API that helps open them and return their metadata.
 *
 * @author cwj
 */
// camera APIs
public final class OpenCameraInterface {

    private static final String TAG = OpenCameraInterface.class.getName();

    /**
     * For {@link #open(int)}, means no preference for which camera to open.
     */
    public static final int NO_REQUESTED_CAMERA = -1;

    private OpenCameraInterface() {
    }

    /**
     * Opens the requested camera with {@link Camera#open(int)}, if one exists.
     *
     * @param cameraId camera ID of the camera to use. A negative value
     *                 or {@link #NO_REQUESTED_CAMERA} means "no preference", in which case a rear-facing
     *                 camera is returned if possible or else any camera
     * @return handle to {@link OpenCamera} that was opened
     */
    public static OpenCamera open(int cameraId) {

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras == 0) {
            return null;
        }

        boolean explicitRequest = cameraId >= 0;

        Camera.CameraInfo selectedCameraInfo = null;
        int index;
        if (explicitRequest) {
            index = cameraId;
            selectedCameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(index, selectedCameraInfo);
        } else {
            index = 0;
            while (index < numCameras) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(index, cameraInfo);
                CameraFacing reportedFacing = CameraFacing.values()[cameraInfo.facing];
                if (reportedFacing == CameraFacing.BACK) {
                    selectedCameraInfo = cameraInfo;
                    break;
                }
                index++;
            }
        }

        Camera camera;
        if (index < numCameras) {
            camera = Camera.open(index);
        } else {
            if (explicitRequest) {
                camera = null;
            } else {
                camera = Camera.open(0);
                selectedCameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, selectedCameraInfo);
            }
        }

        if (camera == null) {
            return null;
        }
        return new OpenCamera(index,
                camera,
                CameraFacing.values()[selectedCameraInfo.facing],
                selectedCameraInfo.orientation);
    }

}
