package org.jun1or.zxingcode_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.istrong.imgsel.ISNav;
import com.istrong.imgsel.callback.ImageLoader;
import com.istrong.zxingcode_android.GlideApp;

import org.jun1or.zxingcode.ZXConfig;
import org.jun1or.zxingcode.ZXNav;
import org.jun1or.zxingcode.utils.ZXingUtil;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_SCAN = 100;

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
        ZXConfig zxConfig = new ZXConfig.Builder()
                //显示相册功能
                .isShowPhotoAlbum(true)
                //四角和扫描线的颜色
                .setScanColor(Color.RED)
                .builder();
        ZXNav.startScan(this, zxConfig, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_SCAN)
            ((TextView) findViewById(R.id.tvResult)).setText("二维码内容：" + data.getStringExtra(ZXNav.KEY_CODE_CONTENT));
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
