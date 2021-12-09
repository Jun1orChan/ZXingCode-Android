package com.nd.zxingcode.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.nd.zxingcode.R;
import com.nd.zxingcode.camera.CameraManager;

/**
 * 扫码框视图
 *
 * @author cwj
 */
public final class ViewFinderView extends View {

    private CameraManager mCameraManager;
    private final Paint mPaint;
    private Paint mPaintText;
    private Paint mPaintLine;
    private int mMaskColor;
    private int mLaserColor;

    private int mLinePosition = 0;

    private static String mHintMsg;

    private Context mContext;

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.zxingcode_scan_viewfinder_mask);
        mLaserColor = resources.getColor(R.color.zxingcode_scan_viewfinder_laser);
        mHintMsg = resources.getString(R.string.zxingcode_scan_hint_text);
        //文字
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTextSize(spToPx(14));
        mPaintText.setTextAlign(Paint.Align.CENTER);
        //扫描线 + 四角
        mPaintLine.setColor(mLaserColor);
    }


    /**
     * 设置颜色
     *
     * @param laserColor
     */
    public void setScanLineColor(int laserColor) {
        this.mLaserColor = laserColor;
        mPaintLine.setColor(this.mLaserColor);
    }


    /**
     * 设置文案
     *
     * @param msg
     */
    public void setHintText(String msg) {
        mHintMsg = msg;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.mCameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mCameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = mCameraManager.getFramingRect();
        Rect previewFrame = mCameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 半透明背景
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        //文字
        canvas.drawText(mHintMsg, width / 2, frame.top - dp2px(mContext, 24), mPaintText);

        mPaintLine.setShader(null);
        //四角线块
        int rectH = dp2px(mContext, 2);
        int rectW = dp2px(mContext, 14);
        //左上角
        canvas.drawRect(frame.left, frame.top, frame.left + rectW, frame.top + rectH, mPaintLine);
        canvas.drawRect(frame.left, frame.top, frame.left + rectH, frame.top + rectW, mPaintLine);
        //右上角
        canvas.drawRect(frame.right - rectW, frame.top, frame.right + 1, frame.top + rectH, mPaintLine);
        canvas.drawRect(frame.right - rectH, frame.top, frame.right + 1, frame.top + rectW, mPaintLine);
        //左下角
        canvas.drawRect(frame.left, frame.bottom - rectH, frame.left + rectW, frame.bottom + 1, mPaintLine);
        canvas.drawRect(frame.left, frame.bottom - rectW, frame.left + rectH, frame.bottom + 1, mPaintLine);
        //右下角
        canvas.drawRect(frame.right - rectW, frame.bottom - rectH, frame.right + 1, frame.bottom + 1, mPaintLine);
        canvas.drawRect(frame.right - rectH, frame.bottom - rectW, frame.right + 1, frame.bottom + 1, mPaintLine);

        //中间的线：动画
        int margin = dp2px(mContext, 6);
        int lineW = dp2px(mContext, 2);
        if (mLinePosition == 0) {
            mLinePosition = frame.top + margin;
        } else {
            if (mLinePosition > frame.bottom - margin * 2) {
                mLinePosition = frame.top + margin;
            } else {
                mLinePosition += 8;
            }
        }
        canvas.drawRect(frame.left + margin, mLinePosition, frame.right - margin, mLinePosition + lineW, mPaintLine);

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(20,
                frame.left,
                frame.top,
                frame.right,
                frame.bottom);
    }

    public void drawViewfinder() {
        invalidate();
    }


    private int spToPx(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * dp 转 px
     *
     * @param dpValue dp 值
     * @return px 值
     */
    private int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
