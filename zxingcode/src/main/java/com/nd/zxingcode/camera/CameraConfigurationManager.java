package com.nd.zxingcode.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.nd.zxingcode.camera.open.CameraFacing;
import com.nd.zxingcode.camera.open.OpenCamera;


/**
 * @author cwj
 */
public final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";

    private final Context mContext;
    private int mCwNeededRotation;
    private int mCwRotationFromDisplayToCamera;
    private Point mScreenResolution;
    private Point mCameraResolution;
    private Point mBestPreviewSize;
    private Point mPreviewSizeOnScreen;

    CameraConfigurationManager(Context context) {
        this.mContext = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    public void initFromCameraParameters(OpenCamera camera) {
        Camera.Parameters parameters = camera.getCamera().getParameters();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: " + displayRotation);
                }
        }

        int cwRotationFromNaturalToCamera = camera.getOrientation();

        // Still not 100% sure about this. But acts like we need to flip this:
        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
        }

        mCwRotationFromDisplayToCamera =
                (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;
        if (camera.getFacing() == CameraFacing.FRONT) {
            mCwNeededRotation = (360 - mCwRotationFromDisplayToCamera) % 360;
        } else {
            mCwNeededRotation = mCwRotationFromDisplayToCamera;
        }

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        mScreenResolution = theScreenResolution;

        /** 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = mScreenResolution.x;
        screenResolutionForCamera.y = mScreenResolution.y;

        if (mScreenResolution.x < mScreenResolution.y) {
            screenResolutionForCamera.x = mScreenResolution.y;
            screenResolutionForCamera.y = mScreenResolution.x;
        }

        mCameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolutionForCamera);
        mBestPreviewSize = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolutionForCamera);

        boolean isScreenPortrait = mScreenResolution.x < mScreenResolution.y;
        boolean isPreviewSizePortrait = mBestPreviewSize.x < mBestPreviewSize.y;

        if (isScreenPortrait == isPreviewSizePortrait) {
            mPreviewSizeOnScreen = mBestPreviewSize;
        } else {
            mPreviewSizeOnScreen = new Point(mBestPreviewSize.y, mBestPreviewSize.x);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getDisplaySize(final Display display) {
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        return point;
    }

    void setDesiredCameraParameters(OpenCamera camera, boolean safeMode) {

        Camera theCamera = camera.getCamera();
        Camera.Parameters parameters = theCamera.getParameters();

        if (parameters == null) {
            return;
        }

        parameters.setPreviewSize(mBestPreviewSize.x, mBestPreviewSize.y);

        theCamera.setParameters(parameters);

        theCamera.setDisplayOrientation(mCwRotationFromDisplayToCamera);

        Camera.Parameters afterParameters = theCamera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize != null && (mBestPreviewSize.x != afterSize.width || mBestPreviewSize.y != afterSize.height)) {
//            Log.w(TAG, "Camera said it supported preview size " + bestPreviewSize.x + 'x' + bestPreviewSize.y +
//                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            mBestPreviewSize.x = afterSize.width;
            mBestPreviewSize.y = afterSize.height;
        }
    }

    Point getBestPreviewSize() {
        return mBestPreviewSize;
    }

    Point getPreviewSizeOnScreen() {
        return mPreviewSizeOnScreen;
    }

    public Point getCameraResolution() {
        return mCameraResolution;
    }

    public Point getScreenResolution() {
        return mScreenResolution;
    }

    int getCWNeededRotation() {
        return mCwNeededRotation;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null &&
                        (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
                                Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }
        return false;
    }

}
