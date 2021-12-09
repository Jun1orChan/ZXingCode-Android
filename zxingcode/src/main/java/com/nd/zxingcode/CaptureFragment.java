package com.nd.zxingcode;

import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.nd.util.DisplayUtil;
import com.nd.util.StatusBarUtil;
import com.nd.zxingcode.camera.CameraManager;
import com.nd.zxingcode.listener.OnComponentClickDelegate;
import com.nd.zxingcode.listener.OnPhotoSelectCallback;
import com.nd.zxingcode.listener.OnScanListener;
import com.nd.zxingcode.manager.BeepManager;
import com.nd.zxingcode.utils.DeCodeUtil;
import com.nd.zxingcode.utils.ZXingUtil;
import com.nd.zxingcode.view.ViewFinderView;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 扫描Fragment
 *
 * @author cwj
 */
public class CaptureFragment extends Fragment implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnClickListener {

    public static final String KEY_SHOW_ALBUM = "showAlbum";
    public static final String KEY_SCAN_COLOR = "scanColor";


    /**
     * 大部分状态栏都是25dp
     */
    private final static int STATUS_BAR_DEFAULT_HEIGHT_DP = 25;

    private SurfaceView mPreView;
    private ViewFinderView mViewFinder;
    private ImageView mImgLight;
    private TextView mTvLightInfo;
    private CameraManager mCameraManager;
    private BeepManager mBeepManager;

    private boolean mHasSurface;
    private MultiFormatReader mMultiFormatReader;

    private OnScanListener mOnScanListener;

    private boolean mLightOn = false;

    /**
     * 内部指定控件被单击代理
     */
    private OnComponentClickDelegate mOnComponentClickDelegate;

    /**
     * 图片选择回调
     */
    private OnPhotoSelectCallback mOnPhotoSelectCallback = new OnPhotoSelectCallback() {
        @Override
        public void onPhotoSelect(String path) {
            decodeFromPhoto(path);
        }
    };

    /**
     * 20个队列数据
     */
    private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingQueue<>(20);

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "CaptureTask #" + mCount.getAndIncrement());
        }
    };

    /**
     * 只允许单线程执行的线程池，和SingleThreadExecutor效果相同
     */
    private ThreadPoolExecutor mSingleThreadExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, WORK_QUEUE, THREAD_FACTORY);

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 扫码回调
     *
     * @param onScanListener 监听器
     */
    public void setOnScanListener(OnScanListener onScanListener) {
        this.mOnScanListener = onScanListener;
    }

    /**
     * 设置单击事件代理
     *
     * @param onComponentClickDelegate 单击事件代理
     */
    public void setOnComponentClickDelegate(OnComponentClickDelegate onComponentClickDelegate) {
        mOnComponentClickDelegate = onComponentClickDelegate;
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
        mPreView = view.findViewById(R.id.sfvPreView);
        mViewFinder = view.findViewById(R.id.viewFinder);
        mViewFinder.setScanLineColor(getArguments().getInt(KEY_SCAN_COLOR, Color.parseColor("#4ea8ec")));
        view.findViewById(R.id.llLight).setOnClickListener(this);
        mImgLight = view.findViewById(R.id.imgLight);
        mTvLightInfo = view.findViewById(R.id.tvLightInfo);
        View imgClose = view.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(this);
        int margin = DisplayUtil.dp2px(view.getContext(), 10);
        setMargins(imgClose, margin, StatusBarUtil.getStatusBarHeight(view.getContext()) + margin, margin, margin);
        View imgAlbum = view.findViewById(R.id.imgAlbum);
        if (!getArguments().getBoolean(KEY_SHOW_ALBUM, true)) {
            imgAlbum.setVisibility(View.GONE);
            return;
        }
        imgAlbum.setOnClickListener(this);
        setMargins(imgAlbum, margin, StatusBarUtil.getStatusBarHeight(view.getContext()) + margin, margin, margin);
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
        if (mHasSurface) {
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
        mCameraManager.stopPreview();
        mCameraManager.closeDriver();
        if (!mHasSurface) {
            SurfaceHolder surfaceHolder = mPreView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    private void decodeFromPhoto(final String path) {
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String codeText = ZXingUtil.syncDecodeQRCode(path);
                Result result = null;
                if (!TextUtils.isEmpty(codeText)) {
                    result = new Result(codeText, null, null, null);
                }
                dealResult(result, true);
            }
        });
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            return;
        }
        if (mCameraManager.isOpen()) {
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            mCameraManager.startPreview();
            mCameraManager.requestPreviewFrame(this);
            mViewFinder.drawViewfinder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
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
                        Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.zxingcode_find_nothing), Toast.LENGTH_SHORT).show();
                    }
                    mCameraManager.requestPreviewFrame(CaptureFragment.this);
                } else {
                    mBeepManager.playBeepSoundAndVibrate();
                    if (mOnScanListener != null) {
                        mOnScanListener.onScanResult(result.getText());
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llLight) {
            toggleLight();
        } else if (id == R.id.imgClose) {
            if (mOnComponentClickDelegate == null) {
                //如果没有设置，默认关闭当前Activity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                //向外传递
                mOnComponentClickDelegate.onComponentClick(OnComponentClickDelegate.COMPONENT.CLOSE, mOnPhotoSelectCallback);
            }
        } else if (id == R.id.imgAlbum) {
            if (mOnComponentClickDelegate != null) {
                //向外传递
                mOnComponentClickDelegate.onComponentClick(OnComponentClickDelegate.COMPONENT.ALBUM, mOnPhotoSelectCallback);
            }
        }
    }


    private void toggleLight() {
        if (mLightOn) {
            try {
                mLightOn = false;
                mCameraManager.offLight();
                mImgLight.setImageResource(R.mipmap.zxingcode_scan_flash_light_off);
                mTvLightInfo.setText(R.string.zxingcode_light_open);
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.zxingcode_light_close_failed), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            try {
                mLightOn = true;
                mCameraManager.openLight();
                mImgLight.setImageResource(R.mipmap.zxingcode_scan_flash_light_on);
                mTvLightInfo.setText(R.string.zxingcode_light_close);
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), getString(R.string.zxingcode_light_open_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSingleThreadExecutor.shutdown();
        mOnPhotoSelectCallback = null;
    }
}
