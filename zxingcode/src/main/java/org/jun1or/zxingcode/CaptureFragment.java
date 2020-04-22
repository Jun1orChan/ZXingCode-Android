package org.jun1or.zxingcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;

import org.jun1or.imgsel.ISNav;
import org.jun1or.imgsel.ImageConfig;
import org.jun1or.imgsel.image.ImageSelectActivity;
import org.jun1or.util.DisplayUtil;
import org.jun1or.util.StatusBarUtil;
import org.jun1or.zxingcode.camera.CameraManager;
import org.jun1or.zxingcode.listener.OnScanListener;
import org.jun1or.zxingcode.manager.BeepManager;
import org.jun1or.zxingcode.utils.DeCodeUtil;
import org.jun1or.zxingcode.utils.ZXingUtil;
import org.jun1or.zxingcode.view.ViewFinderView;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CaptureFragment extends Fragment implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnClickListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final int REQUESTCODE_ALBUN = 0x88;

    private SurfaceView mPreView;
    private ViewFinderView mViewFinder;
    private ImageView mImgLight;
    private TextView mTvLightInfo;
    private CameraManager mCameraManager;
    private BeepManager mBeepManager;

    private boolean hasSurface;
    private MultiFormatReader mMultiFormatReader;

    private OnScanListener mOnScanListener;

    private boolean isLightOn = false;

    private ExecutorService mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public void setOnScanListener(OnScanListener onScanListener) {
        this.mOnScanListener = onScanListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMultiFormatReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, null);
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, null);
        mMultiFormatReader.setHints(hints);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.zxingcode_fragment_capture, null, false);
        mBeepManager = new BeepManager(view.getContext());
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mPreView = (SurfaceView) view.findViewById(R.id.sfvPreView);
        mViewFinder = (ViewFinderView) view.findViewById(R.id.viewFinder);
        mViewFinder.setScanLineColor(getArguments().getInt(ZXNav.KEY_SCAN_COLOR, Color.parseColor("#4ea8ec")));
        view.findViewById(R.id.llLight).setOnClickListener(this);
        mImgLight = (ImageView) view.findViewById(R.id.imgLight);
        mTvLightInfo = (TextView) view.findViewById(R.id.tvLightInfo);
        View imgClose = view.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(this);
        int margin = DisplayUtil.dp2px(view.getContext(), 10);
        setMargins(imgClose, margin, StatusBarUtil.getStatusBarHeight(view.getContext()) + margin, margin, margin);
        View tvAlbum = view.findViewById(R.id.tvAlbum);
        if (!getArguments().getBoolean(ZXNav.KEY_SHOW_ALBUM, true)) {
            tvAlbum.setVisibility(View.GONE);
            return;
        }
        tvAlbum.setOnClickListener(this);
        setMargins(tvAlbum, margin, StatusBarUtil.getStatusBarHeight(view.getContext()) + margin, margin, margin);
    }


    @Override
    public void onResume() {
        super.onResume();
        mBeepManager.updatePrefs(true, true);
        mCameraManager = new CameraManager(getActivity());
        mViewFinder.setCameraManager(mCameraManager);
        if (mCameraManager != null && mCameraManager.isOpen()) {
            return;
        }
        SurfaceHolder surfaceHolder = mPreView.getHolder();
        if (hasSurface) {
            // 防止sdk8的设备初始化预览异常
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onPause() {
        mCameraManager.closeDriver();
        mCameraManager.stopPreview();
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mPreView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUESTCODE_ALBUN) {
            List<String> imageList = data.getStringArrayListExtra(ImageSelectActivity.KEY_result);
            if (imageList != null && imageList.size() > 0) {
                decodeFromPhoto(imageList.get(0));
            }
        }
    }

    private void decodeFromPhoto(final String path) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String codeText = ZXingUtil.syncDecodeQRCode(path);
                Result result = null;
                if (!TextUtils.isEmpty(codeText))
                    result = new Result(codeText, null, null, null);
                dealResult(result, true);
            }
        });
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            Log.e(TAG, "SurfaceHolder 不存在");
            return;
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            mCameraManager.startPreview();
            mCameraManager.requestPreviewFrame(this);
            mViewFinder.drawViewfinder();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "开启摄像头异常：" + e.toString());
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void onDestroyView() {
        mCameraManager.stopPreview();
        mBeepManager.close();
        super.onDestroyView();
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Point cameraResolution = mCameraManager.getConfigManager().getCameraResolution();
                if (cameraResolution != null) {
                    Result result = null;
                    Point screenResolution = mCameraManager.getConfigManager().getScreenResolution();
                    if (screenResolution.x < screenResolution.y) {
                        // portrait
                        result = DeCodeUtil.decode(data, cameraResolution.y, cameraResolution.x, mMultiFormatReader, mCameraManager);
                    } else {
                        // landscape
                        result = DeCodeUtil.decode(data, cameraResolution.x, cameraResolution.y, mMultiFormatReader, mCameraManager);
                    }
                    dealResult(result, false);
                }
            }
        });
    }

    private void dealResult(final Result result, final boolean isShowToast) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    if (isShowToast && getActivity() != null) {
                        Toast.makeText(getActivity(), "未发现内容！", Toast.LENGTH_SHORT).show();
                    }
                    mCameraManager.requestPreviewFrame(CaptureFragment.this);
                } else {
                    mBeepManager.playBeepSoundAndVibrate();
                    if (mOnScanListener != null)
                        mOnScanListener.onScanResult(result.getText());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llLight) {
            toogltLight();
        } else if (id == R.id.imgClose) {
            if (getActivity() != null)
                getActivity().finish();
        } else if (id == R.id.tvAlbum) {
            goToAlbumSelect();
        }
    }

    private void goToAlbumSelect() {
        ImageConfig config = new ImageConfig.Builder()
                .title("图片")
                .multiSelect(false)
                .build();
        ISNav.getInstance().toImageSelectActivity(this, config, REQUESTCODE_ALBUN);
    }

    private void toogltLight() {
        if (isLightOn) {
            isLightOn = false;
            mCameraManager.offLight();
            mImgLight.setImageResource(R.mipmap.zxingcode_scan_flash_light_off);
            mTvLightInfo.setText(R.string.zxingcode_light_open);
        } else {
            isLightOn = true;
            mCameraManager.openLight();
            mImgLight.setImageResource(R.mipmap.zxingcode_scan_flash_light_on);
            mTvLightInfo.setText(R.string.zxingcode_light_close);
        }
    }


    private void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
