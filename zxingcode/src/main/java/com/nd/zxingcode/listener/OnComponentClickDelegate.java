package com.nd.zxingcode.listener;

/**
 * 组件单击事件代理
 * <p>
 * 用于向外部传递事件，由外部决定进一步的行为
 *
 * @author cwj
 * @date 2021/7/8 16:28
 */
public interface OnComponentClickDelegate {


    /**
     * 为了后续版本兼容性考虑，使用枚举的方式传入单击的控件名称
     * <p>
     * 后续新增控件，只要新增枚举即可
     *
     * @param component
     */
    void onComponentClick(COMPONENT component, OnPhotoSelectCallback onPhotoSelectCallback);

    enum COMPONENT {
        /**
         * 关闭按钮被单击
         */
        CLOSE,
        /**
         * 相册按钮被单击
         */
        ALBUM
    }
}
