# ZXingCode-Android

- 条形码扫描
- 二维码扫描
- 识别图中二维码
- 生成二维码（可带logo）
- 现成界面直接使用
- 支持相册显示与否、扫描框颜色定义

## 引用

1.项目build.gradle文件中添加

```gradle
   implementation 'org.jun1or.baselib:zxingcode:1.0.1'
 ```

## 使用

1.二维码扫描

```java
    public void goZXingCodeScanActivity(View view) {
        ZXConfig zxConfig = new ZXConfig.Builder()
                //显示相册功能
                .isShowPhotoAlbum(true)
                //四角和扫描线的颜色
                .setScanColor(Color.BLUE)
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

 ```
2.生成二维码

```java
    public void createQrCode(View view) {
        Editable editable = ((EditText) findViewById(R.id.etText)).getText();
        if (TextUtils.isEmpty(editable)) {
            Toast.makeText(this, "请先输入文本", Toast.LENGTH_SHORT).show();
            return;
        }
        ((ImageView) findViewById(R.id.imgQrCode)).setImageBitmap(ZXingUtil.createQRImage(editable.toString()));
    }
```

3.带logo二维码

```java
    public void createQrCodeWithLogo(View view) {
        Editable editable = ((EditText) findViewById(R.id.etText)).getText();
        if (TextUtils.isEmpty(editable)) {
            return;
        }
        ((ImageView) findViewById(R.id.imgQrCode)).setImageBitmap(ZXingUtil.createQRCodeWithLogo(editable.toString(), BitmapFactory.decodeResource(getResources(), R.mipmap.imgsel_take_photo)));
    }

```


## 混淆

```text
# ZXing

-dontwarn com.google.zxing.**
-keep  class com.google.zxing.**{*;}

```
## 版本记录

### 1.0.1
- 升级至AndroidX

### 1.0.0

- 初始发布
