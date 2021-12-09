package com.nd.zxingcode.utils;

import android.graphics.Bitmap;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.nd.zxingcode.camera.CameraManager;

import java.io.ByteArrayOutputStream;

/**
 * @author cwj
 */
public class DeCodeUtil {
    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    public static Result decode(byte[] data, int width, int height, MultiFormatReader multiFormatReader, CameraManager cameraManager) {
        if (width < height) {
            // portrait
            byte[] rotatedData = new byte[data.length];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    rotatedData[y * width + width - x - 1] = data[y + x * height];
                }
            }
            data = rotatedData;
        }
        Result rawResult = null;
        PlanarYUVLuminanceSource source = cameraManager.buildLuminanceSource(data, width, height);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
//                re.printStackTrace();
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }
        return rawResult;
    }

    public static byte[] bundleThumbnail(PlanarYUVLuminanceSource source) {
        int[] pixels = source.renderThumbnail();
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
        return out.toByteArray();
    }
}
