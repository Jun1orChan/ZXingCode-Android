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
   implementation 'implementation 'com.github.Jun1orChan:ZXingCode-Android:1.2.0''
 ```

## 使用

1.二维码扫描

```java
    public void goZXingCodeScanActivity(View view){
        ZXConfig zxConfig=new ZXConfig.Builder()
        //显示相册功能
        .isShowPhotoAlbum(true)
        //四角和扫描线的颜色
        .setScanColor(Color.BLUE)
        .builder();
        ZXNav.startScan(this,zxConfig,REQUEST_CODE_SCAN);
        }

@Override
protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK){
        return;
        }
        if(requestCode==REQUEST_CODE_SCAN)
        ((TextView)findViewById(R.id.tvResult)).setText("二维码内容："+data.getStringExtra(ZXNav.KEY_CODE_CONTENT));
        }

 ```

2.生成二维码

```java
    public void createQrCode(View view){
        Editable editable=((EditText)findViewById(R.id.etText)).getText();
        if(TextUtils.isEmpty(editable)){
        Toast.makeText(this,"请先输入文本",Toast.LENGTH_SHORT).show();
        return;
        }
        ((ImageView)findViewById(R.id.imgQrCode)).setImageBitmap(ZXingUtil.createQRImage(editable.toString()));
        }
```

3.带logo二维码

```java
    public void createQrCodeWithLogo(View view){
        Editable editable=((EditText)findViewById(R.id.etText)).getText();
        if(TextUtils.isEmpty(editable)){
        return;
        }
        ((ImageView)findViewById(R.id.imgQrCode)).setImageBitmap(ZXingUtil.createQRCodeWithLogo(editable.toString(),BitmapFactory.decodeResource(getResources(),R.mipmap.imgsel_take_photo)));
        }

```

4.自定义Activity

- 参考源码CaptureActivity；需要自行申请权限。

## 混淆

```text
# ZXing

-dontwarn com.google.zxing.**
-keep  class com.google.zxing.**{*;}

```

## 版本记录

### 1.2.0

- 适配Android 12
- Demo移除imgsel库

### 1.1.2

- 国际化默认中文

### 1.1.1

- 修复Android11下的获取状态栏为0的bug

### 1.1.0

- 支持国际化（中文简体、中文繁体-台湾，中文繁体-香港，英文-默认）

### 1.0.0

- 初始发布
