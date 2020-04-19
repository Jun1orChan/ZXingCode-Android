package org.jun1or.zxingcode;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class ZXNav {

    public static final String KEY_SHOW_ALBUM = "showAlbum";
    public static final String KEY_SCAN_COLOR = "scanColor";

    public static final String KEY_CODE_CONTENT = "code_content";


    public static void startScan(Activity activity, ZXConfig zxConfig, int requestCode) {
        if (zxConfig == null) {
            zxConfig = new ZXConfig.Builder().builder();
        }
        Intent intent = new Intent(activity, CaptureActivity.class);
        //是否显示相册按钮
        intent.putExtra(KEY_SHOW_ALBUM, zxConfig.isShowPhotoAlbum());
        intent.putExtra(KEY_SCAN_COLOR, zxConfig.getScanColor());
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startScan(Fragment fragment, ZXConfig zxConfig, int requestCode) {
        if (zxConfig == null) {
            zxConfig = new ZXConfig.Builder().builder();
        }
        Intent intent = new Intent(fragment.getActivity(), CaptureActivity.class);
        //是否显示相册按钮
        intent.putExtra(KEY_SHOW_ALBUM, zxConfig.isShowPhotoAlbum());
        intent.putExtra(KEY_SCAN_COLOR, zxConfig.getScanColor());
        fragment.startActivityForResult(intent, requestCode);
    }
}
