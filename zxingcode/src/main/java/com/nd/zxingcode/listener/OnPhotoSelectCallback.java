package com.nd.zxingcode.listener;

/**
 * 照片选择回调，用于外部自定义图片选择视图之后，异步返回图片地址，进而进行解析
 *
 * @author cwj
 * @date 2021/7/8 16:25
 */
public interface OnPhotoSelectCallback {

    /**
     * 图片地址回调
     *
     * @param path 图片地址
     */
    void onPhotoSelect(String path);
}
