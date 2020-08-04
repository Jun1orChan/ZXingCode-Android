package org.jun1or.zxingcode_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import org.jun1or.imgsel.ISNav;
import org.jun1or.imgsel.callback.ImageLoader;
import org.jun1or.zxingcode.utils.ZXingUtil;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ISNav.getInstance().init(new ImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                GlideApp.with(context).load(path).into(imageView);
            }
        });
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
                        BitmapFactory.decodeResource(getResources(), R.mipmap.imgsel_take_photo)));
    }
}
