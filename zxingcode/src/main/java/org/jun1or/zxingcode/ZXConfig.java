package org.jun1or.zxingcode;

import android.graphics.Color;

public class ZXConfig {

    //是否显示相册
    private boolean showPhotoAlbum;
    //扫描框和扫描线的颜色
    private int mScanColor;

    private ZXConfig() {

    }

    private ZXConfig(Builder builder) {
        showPhotoAlbum = builder.showPhotoAlbum;
        mScanColor = builder.scanColor;
    }

    public boolean isShowPhotoAlbum() {
        return showPhotoAlbum;
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
