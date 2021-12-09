package com.nd.zxingcode;

import android.graphics.Color;

/**
 * @author cwj
 */
public class ZXConfig {


    /**
     * 是否显示相册
     */
    private boolean mShowPhotoAlbum;
    /**
     * 扫描框和扫描线的颜色
     */
    private int mScanColor;

    private ZXConfig() {

    }

    private ZXConfig(Builder builder) {
        mShowPhotoAlbum = builder.showPhotoAlbum;
        mScanColor = builder.scanColor;
    }

    public boolean isShowPhotoAlbum() {
        return mShowPhotoAlbum;
    }

    public int getScanColor() {
        return mScanColor;
    }

    public static class Builder {
        private boolean showPhotoAlbum = true;
        private int scanColor = Color.parseColor("#4ea8ec");

        public ZXConfig builder() {
            return new ZXConfig(this);
        }

        public Builder isShowPhotoAlbum(boolean showPhotoAlbum) {
            this.showPhotoAlbum = showPhotoAlbum;
            return this;
        }

        public Builder setScanColor(int scanColor) {
            this.scanColor = scanColor;
            return this;
        }
    }
}
