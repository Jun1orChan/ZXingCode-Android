package com.nd.zxingcode_android;

import static com.nd.zxingcode.CaptureFragment.KEY_SCAN_COLOR;
import static com.nd.zxingcode.CaptureFragment.KEY_SHOW_ALBUM;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.nd.dialog.MaterialDialog;
import com.nd.util.AppUtil;
import com.nd.zxingcode.CaptureFragment;
import com.nd.zxingcode.listener.OnComponentClickDelegate;
import com.nd.zxingcode.listener.OnPhotoSelectCallback;
import com.nd.zxingcode.listener.OnScanListener;

import java.util.List;


/**
 * @author cwj
 */

public class QrCodeActivity extends AppCompatActivity implements OnScanListener, OnComponentClickDelegate {

    private static final int REQUEST_CODE_PERMISSION = 0x1;

    private static final int REQUESTCODE_ALBUN = 0x88;


    private OnPhotoSelectCallback mOnPhotoSelectCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_zxingcode);
        getPermission();
    }

    private void getPermission() {
        XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //授权
                        addCaptureFragment();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        //显示未授权提示框
                        showPermissionDeniedDialog(permissions);
                    }
                });
    }


    private void showPermissionDeniedDialog(List<String> permissions) {
        final MaterialDialog permissionDeniedDialog = new MaterialDialog();
        permissionDeniedDialog
                .content(String.format("请在“设置-应用管理-%s-应用权限”选项中，允许%s访问相机", AppUtil.getAppName(this), AppUtil.getAppName(this)))
                .btnText("取消", "去设置")
                .btnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionDeniedDialog.dismiss();
                        finish();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionDeniedDialog.dismiss();
                        goAppDetailsSettings(permissions);
                    }
                })
                .show(getSupportFragmentManager());
    }

    public void goAppDetailsSettings(List<String> permissions) {
        XXPermissions.startPermissionActivity(this, permissions, REQUEST_CODE_PERMISSION);
    }

    private void addCaptureFragment() {
        CaptureFragment captureFragment = new CaptureFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SHOW_ALBUM, getIntent().getBooleanExtra(KEY_SHOW_ALBUM, true));
        bundle.putInt(KEY_SCAN_COLOR, getIntent().getIntExtra(KEY_SCAN_COLOR, ContextCompat.getColor(this, R.color.colorPrimary)));
        captureFragment.setArguments(bundle);
        captureFragment.setOnComponentClickDelegate(this);
        captureFragment.setOnScanListener(QrCodeActivity.this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fmContainer, captureFragment, null)
                .commit();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == REQUEST_CODE_PERMISSION) {
            if (XXPermissions.isGranted(this, Permission.CAMERA)) {
                // 有对应的权限
                addCaptureFragment();
            } else {
                // 没有对应的权限
                getPermission();
            }
        }
        if (reqCode == REQUESTCODE_ALBUN && data != null) {
            ClipData clipData = data.getClipData();
            if (clipData == null) {
                if (mOnPhotoSelectCallback != null) {
                    mOnPhotoSelectCallback.onPhotoSelect(AppUtil.uriToFile(this, data.getData()).getAbsolutePath());
                }
            } else {
                if (clipData.getItemCount() > 0) {
//                for (int i = 0; i < clipData.getItemCount(); i++) {
//                    Uri uri = clipData.getItemAt(i).getUri();
//                    AppUtil.getRealPathFromURI(this, uri);
//                }
                    String cacheFile = AppUtil.uriToFile(this, clipData.getItemAt(0).getUri()).getAbsolutePath();
                    Log.e("TAG======", "=========" + cacheFile);
                    if (mOnPhotoSelectCallback != null) {
                        mOnPhotoSelectCallback.onPhotoSelect(cacheFile);
                    }
//                Log.e("TAG=========", clipData.getItemAt(0).getUri().toString());
//                Log.e("TAG========", "result is:" + ZXingUtil.syncDecodeQRCode(ZXingUtil.decodeUriAsBitmap(this, clipData.getItemAt(0).getUri())));
                }
            }
        }
    }

    @Override
    public void onScanResult(String result) {
        Log.e("TAG", "onScanResult==========" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onComponentClick(COMPONENT component, OnPhotoSelectCallback onPhotoSelectCallback) {
        mOnPhotoSelectCallback = onPhotoSelectCallback;
        if (component == COMPONENT.CLOSE) {
            //单击关闭按钮
            finish();
        } else if (component == COMPONENT.ALBUM) {
            //单击相册
            goToAlbumSelect();
        }
    }

    private void goToAlbumSelect() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.setAction(Intent.ACTION_GET_CONTENT)
        intent.setAction(Intent.ACTION_PICK);
        //直接打开系统相册  不设置会有选择相册一步（例：系统相册、QQ浏览器相册）
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUESTCODE_ALBUN);
    }

}
