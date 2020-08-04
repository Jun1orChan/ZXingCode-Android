package org.jun1or.zxingcode_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;


import org.jun1or.dialog.MaterialDialog;
import org.jun1or.util.AppUtil;
import org.jun1or.zxingcode.CaptureFragment;
import org.jun1or.zxingcode.listener.OnScanListener;

import java.util.List;

import static org.jun1or.zxingcode.CaptureFragment.KEY_SCAN_COLOR;
import static org.jun1or.zxingcode.CaptureFragment.KEY_SHOW_ALBUM;


/**
 * @author cwj
 */

public class QrCodeActivity extends AppCompatActivity implements OnScanListener {

    private static final int REQUEST_CODE_PERMISSION = 0x1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_zxingcode);
        getPermission();
    }

    private void getPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.CAMERA)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //授权
                        addCaptureFragment();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //显示未授权提示框
                        showPermissionDeniedDialog();
                    }
                })
                .start();
    }


    private void showPermissionDeniedDialog() {
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
                        goAppDetailsSettings();
                    }
                })
                .show(getSupportFragmentManager());
    }

    public void goAppDetailsSettings() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .start(REQUEST_CODE_PERMISSION);
    }

    private void addCaptureFragment() {
        CaptureFragment captureFragment = new CaptureFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SHOW_ALBUM, getIntent().getBooleanExtra(KEY_SHOW_ALBUM, false));
        bundle.putInt(KEY_SCAN_COLOR, getIntent().getIntExtra(KEY_SCAN_COLOR, ContextCompat.getColor(this, R.color.colorPrimary)));
        captureFragment.setArguments(bundle);
        captureFragment.setOnScanListener(QrCodeActivity.this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fmContainer, captureFragment, null)
                .commit();
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == REQUEST_CODE_PERMISSION) {
            if (AndPermission.hasPermissions(this, Permission.CAMERA)) {
                // 有对应的权限
                addCaptureFragment();
            } else {
                // 没有对应的权限
                getPermission();
            }
        }
    }

    @Override
    public void onScanResult(String result) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        finish();
    }

}
