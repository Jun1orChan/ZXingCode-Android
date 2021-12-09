package com.nd.zxingcode.listener;

/**
 * @author cwj
 */
public interface OnScanListener {
    /**
     * 结果回调
     *
     * @param result
     */
    void onScanResult(String result);
}
