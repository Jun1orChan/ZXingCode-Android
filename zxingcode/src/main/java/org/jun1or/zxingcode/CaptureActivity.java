package org.jun1or.zxingcode;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


import org.jun1or.dialog.MaterialDialog;
import org.jun1or.util.AppUtil;
import org.jun1or.util.StatusBarUtil;
import org.jun1or.zxingcode.listener.OnScanListener;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import java.util.List;

import static org.jun1or.zxingcode.ZXNav.KEY_CODE_CONTENT;
import static org.jun1or.zxingcode.ZXNav.KEY_SCAN_COLOR;
import static org.jun1or.zxingcode.ZXNav.KEY_SHOW_ALBUM;


public class CaptureActivity extends AppCompatActivity implements OnScanListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.translucent(this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.zxingcode_activity_capture);
        getPermission();
    }

    private void getPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.CAMERA)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        //授权
                        CaptureFragment captureFragment = new CaptureFragment();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(KEY_SHOW_ALBUM, getIntent().getBooleanExtra(KEY_SHOW_ALBUM, true));
                        bundle.putInt(KEY_SCAN_COLOR, getIntent().getIntExtra(KEY_SCAN_COLOR, getResources().getColor(R.color.colorAccent)));
                        captureFragment.setArguments(bundle);
                        captureFragment.setOnScanListener(CaptureActivity.this);
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.fmContainer, captureFragment, null)
                                .commit();
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
                .content(String.format(getString(R.string.zxingcode_camera_permission_denied_tips),
                        AppUtil.getAppName(this), AppUtil.getAppName(this)))
                .btnText(getString(R.string.zxingcode_btn_text_denied_cancel), getString(R.string.zxingcode_btn_text_denied_setting))
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
                        AppUtil.goAppDetailsSettings(getBaseContext());
                    }
                })
                .show(getSupportFragmentManager());
    }

    @Override
    public void onScanResult(String result) {
        Intent intent = new Intent();
        intent.putExtra(KEY_CODE_CONTENT, result);
        setResult(RESULT_OK, intent);
        finish();
    }
}
