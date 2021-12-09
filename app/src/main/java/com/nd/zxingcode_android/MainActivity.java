package com.nd.zxingcode_android;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nd.zxingcode.utils.ZXingUtil;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    public void goZXingCodeScanActivity(View view) {
        Intent intent = new Intent(this, QrCodeActivity.class);
        startActivity(intent);
    }

    public void createQrCode(View view) {
        Editable editable = ((EditText) findViewById(R.id.etText)).getText();
        if (TextUtils.isEmpty(editable)) {
            Toast.makeText(this, "请先输入文本", Toast.LENGTH_SHORT).show();
            return;
        }
        ((ImageView) findViewById(R.id.imgQrCode)).setImageBitmap(ZXingUtil.createQRImage(editable.toString()));
    }

    public void createQrCodeWithLogo(View view) {
        Editable editable = ((EditText) findViewById(R.id.etText)).getText();
        if (TextUtils.isEmpty(editable)) {
            return;
        }
        ((ImageView) findViewById(R.id.imgQrCode)).setImageBitmap(
                ZXingUtil.createQRCodeWithLogo(editable.toString(), 600, 1 / 3f,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round)));
    }
}
