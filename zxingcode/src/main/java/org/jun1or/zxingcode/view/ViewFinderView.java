package org.jun1or.zxingcode.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import org.jun1or.util.DisplayUtil;
import org.jun1or.zxingcode.R;
import org.jun1or.zxingcode.camera.CameraManager;


/**
 * @author cwj
 */
public final class ViewFinderView extends View {

    private CameraManager cameraManager;
    private final Paint paint;
    private Paint paintText;
    private Paint paintLine;
    private int maskColor;
    private int laserColor;

    private int linePosition = 0;

    private static String hintMsg;

    private Context context;

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.zxingcode_scan_viewfinder_mask);
        laserColor = resources.getColor(R.color.zxingcode_scan_viewfinder_laser);
        hintMsg = resources.getString(R.string.zxingcode_scan_hint_text);
        //文字
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(spToPx(14));
        paintText.setTextAlign(Paint.Align.CENTER);
        //扫描线 + 四角
        paintLine.setColor(laserColor);
    }

    //设置颜色
    public void setScanLineColor(int laserColor) {
        this.laserColor = laserColor;
        paintLine.setColor(this.laserColor);
    }

    //设置文案
    public void setHintText(String msg) {
        hintMsg = msg;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = cameraManager.getFramingRect();
        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // 半透明背景
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        //文字
        canvas.drawText(hintMsg, width / 2, frame.top - DisplayUtil.dp2px(context, 24), paintText);

        paintLine.setShader(null);
        //四角线块
        int rectH = DisplayUtil.dp2px(context, 2);
        int rectW = DisplayUtil.dp2px(context, 14);
        //左上角
        canvas.drawRect(frame.left, frame.top, frame.left + rectW, frame.top + rectH, paintLine);
        canvas.drawRect(frame.left, frame.top, frame.left + rectH, frame.top + rectW, paintLine);
        //右上角
        canvas.drawRect(frame.right - rectW, frame.top, frame.right + 1, frame.top + rectH, paintLine);
        canvas.drawRect(frame.right - rectH, frame.top, frame.right + 1, frame.top + rectW, paintLine);
        //左下角
        canvas.drawRect(frame.left, frame.bottom - rectH, frame.left + rectW, frame.bottom + 1, paintLine);
        canvas.drawRect(frame.left, frame.bottom - rectW, frame.left + rectH, frame.bottom + 1, paintLine);
        //右下角
        canvas.drawRect(frame.right - rectW, frame.bottom - rectH, frame.right + 1, frame.bottom + 1, paintLine);
        canvas.drawRect(frame.right - rectH, frame.bottom - rectW, frame.right + 1, frame.bottom + 1, paintLine);

        //中间的线：动画
        int margin = DisplayUtil.dp2px(context, 6);
        int lineW = DisplayUtil.dp2px(context, 2);
        if (linePosition == 0) {
            linePosition = frame.top + margin;
        } else {
            if (linePosition > frame.bottom - margin * 2) {
                linePosition = frame.top + margin;
            } else {
                linePosition += 8;
            }
        }
        canvas.drawRect(frame.left + margin, linePosition, frame.right - margin, linePosition + lineW, paintLine);

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

}
