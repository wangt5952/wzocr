package cn.wz.scanner.scanlibrary.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wz.scanner.scanlibrary.R;
import cn.wz.scanner.scanlibrary.acitvity.ScanActivity;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.tools.tess.TessManager;
import cn.wz.scanner.scanlibrary.tools.tess.TessMobileThread;
import cn.wz.scanner.scanlibrary.tools.tess.TessSimpleCallback;
import cn.wz.scanner.scanlibrary.tools.youtu.YoutuSimpleCallback;
import cn.wz.scanner.scanlibrary.tools.youtu.YoutuThread;
import cn.wz.scanner.scanlibrary.tools.zxing.ZxingSimpleCallback;
import cn.wz.scanner.scanlibrary.tools.zxing.ZxingThread;

/**
 * 扫描View.
 */
public class WzScannerView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    /** TAG. */
    static final String TAG = "WZ_" + WzScannerView.class.getName();

    private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
    private static final long ANIMATION_DELAY = 100L;
    private static final int OPAQUE = 0xFF;

    private static final int MIN_FOCUS_BOX_WIDTH = 50;
    private static final int MIN_FOCUS_BOX_HEIGHT = 35;
    private static final int MIN_FOCUS_BOX_TOP = 200;

    private int top;
    private Paint mPaint;
    private int mScannerAlpha;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mTextColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect
    private Rect mRect; //返回的Rect
    /** 摄像头. */
    private Camera mCamera;
    //帧率
//    private int frameRate = 30;
    // 屏幕分辨率
    private Point screenResolution;
    // 相机分辨率
    private Point cameraResolution;
    /** ViewHolder. */
    private SurfaceHolder mHolder;
    /** 父界面. */
    private ScanActivity mScanActivity;

    /**
     * 构造方法.
     * @param context 上下文
     */
    public WzScannerView(Context context) {
        this(context, null);
    }

    /**
     * 构造方法.
     * @param context 上下文
     * @param attrs 属性
     */
    public WzScannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 构造方法.
     * @param context 上下文
     * @param attrs 属性
     * @param defStyleAttr 默认样式属性
     */
    public WzScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        Resources resources = getResources();
        mMaskColor = resources.getColor(R.color.finder_mask);
        mFrameColor = resources.getColor(R.color.finder_frame);
        mLaserColor = resources.getColor(R.color.finder_laser);
        mTextColor = resources.getColor(R.color.white);
        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        init();
    }

    /**
     * 设置父页面.
     * @param scActivity ScanActivity
     */
    public void setScanActivity(ScanActivity scActivity) {
        this.mScanActivity = scActivity;
    }

    /**
     * 获得选取框范围.
     * @return 选取框范围
     */
    public Rect getMRect() {
        return mRect;
    }

    /**
     * 获得摄像头.
     * @return 摄像头
     */
    public Camera getCamera() {
        return mCamera;
    }

    /**
     * 初始化.
     */
    private void init() {
        // 设置SurfaceView 的SurfaceHolder的回调函数
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置相机及屏幕分辨率
        initScreenResolution();
        TessManager.getInstances(getContext(), "eng").initTrainingData();
    }

    /**
     * 初始化预览框.
     */
    public void initScanRect() {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);
        if (null == mFrameRect) {
            // 因为竖屏，所以摄像头的宽高要转90度
            Point cameraResForScreen = new Point();
            if (cameraResolution.x > cameraResolution.y) {
                cameraResForScreen.x = cameraResolution.y;
                cameraResForScreen.y = cameraResolution.x;
            }
            // 针对OCR
//            int width = cameraResForScreen.x * 9 / 10;
//            int height = cameraResForScreen.y * 2 / 3;
            // 针对优图设置
            int width = cameraResForScreen.x * 5 / 6;
            int height = cameraResForScreen.y * 1 / 10;

//            width = width == 0
//                    ? MIN_FOCUS_BOX_WIDTH
//                    : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;
//            height = height == 0
//                    ? MIN_FOCUS_BOX_HEIGHT
//                    : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

            int left = (screenResolution.x - width) / 2;
            int top = (screenResolution.y - height) / 4;
//            int top = screenResolution.y / 3;
            this.top = top; //记录初始距离上方距离

            mFrameRect = new Rect(left, top, left + width, top + height);
            mRect = mFrameRect;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

//        // 绘制焦点框外边的暗色背景
//        mPaint.setColor(mMaskColor);
//        canvas.drawRect(0, 0, width, frame.top, mPaint);
//        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
//        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
//        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
//        drawText(canvas, frame);
//        drawLaser(canvas, frame);
    }

    /**
     * 画聚焦框，白色的.
     * @param canvas 画布
     * @param rect 范围框
     */
    private void drawFocusRect(Canvas canvas, Rect rect) {
        // 绘制焦点框（黑色）
        mPaint.setColor(mFrameColor);
        // 上
        canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, mPaint);
        // 左
        canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength, mPaint);
        // 右
        canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength, mPaint);
        // 下
        canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom, mPaint);
    }

    /**
     * 画四个角.
     * @param canvas 画布
     * @param rect 方块
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(OPAQUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mAngleThick);
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        // 左上角
        canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
        canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
        // 右上角
        canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
        canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
        // 左下角
        canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
        canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
        // 右下角
        canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
        canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
    }

//    /**
//     * 写说明文字.
//     * @param canvas 画布
//     * @param rect 写字区域
//     */
//    private void drawText(Canvas canvas, Rect rect) {
//        String text = getResources().getString(R.string.auto_scan_notification);
//        int margin = 20;
//        mPaint.setColor(mTextColor);
//        mPaint.setTextSize(R.dimen.scan_hint_text_size_10dp);
//        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
//        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
////        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
////        float newY = rect.bottom + margin + offY;
//        float left = (screenResolution.x - mPaint.getTextSize() * text.length()) / 2;
//        canvas.drawText(text, left, rect.top - fontTotalHeight * 3, mPaint);
//    }

//    /**
//     * 或扫描线.
//     * @param canvas 画布
//     * @param rect 扫描线区域
//     */
//    private void drawLaser(Canvas canvas, Rect rect) {
//        // 绘制焦点框内固定的一条扫描线
//        mPaint.setColor(mLaserColor);
//        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
//        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
//        int middle = rect.height() / 3 + rect.top;
//        canvas.drawRect(rect.left + 2, middle - 1, rect.right - 1, middle + 2, mPaint);
//        mLaserHandler.sendEmptyMessageDelayed(1, ANIMATION_DELAY);
//    }
//
//    private Handler mLaserHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            invalidate();
//        }
//    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //Surface创建时开启Camera
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //设置Camera基本参数
        if (null != mCamera) {
            initCamera();
            initScanRect();
            setFlashLight(mScanActivity.getDefFlashIsOpen());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        try {
            release();
        } catch (Exception e) {
            Log.e(TAG, "关闭释放摄像头异常", e);
        }
    }

    /**
     * 设置屏幕分辨率.
     */
    public void initScreenResolution() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        this.screenResolution = point;
        Log.i(TAG, "屏幕分辨率[width：" + screenResolution.x + "；heigh：" + screenResolution.y + "]");
    }

    public void initCamera() {
        stopPreview();
        /** 因为竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point screenResolutionForCamera = new Point();
        screenResolutionForCamera.x = screenResolution.x;
        screenResolutionForCamera.y = screenResolution.y;

        if (screenResolution.x < screenResolution.y) {
            screenResolutionForCamera.x = screenResolution.y;
            screenResolutionForCamera.y = screenResolution.x;
        }
        int cameraWidth = screenResolutionForCamera.x;
        int cameraHeight = screenResolutionForCamera.y;
        Camera.Parameters camParams = mCamera.getParameters();
        List<Camera.Size> cSizes = camParams.getSupportedPreviewSizes();
        if (null == cSizes) {
            Log.w(TAG, "设备没有支持的预览大小设置; 使用默认设置");
            Camera.Size defaultSize = camParams.getPreviewSize();
            if (defaultSize == null) {
                throw new IllegalStateException("未发现可使用的预览大小设置");
            }
            return;
        }

        for (int i = 0; i < cSizes.size(); i++) {
            if ((cSizes.get(i).width >= cameraWidth && cSizes.get(i).height >= cameraHeight) || i == cSizes.size() - 1) {
                cameraWidth = cSizes.get(i).width;
                cameraHeight = cSizes.get(i).height;
                break;
            }
        }
        this.cameraResolution = new Point(cameraWidth, cameraHeight);
        camParams.setPreviewSize(cameraResolution.x, cameraResolution.y);
        camParams.setPictureSize(cameraResolution.x, cameraResolution.y);

        Log.i(TAG, "预览分辨率[width：" + cameraWidth + "；heigh：" + cameraHeight + "]");
        Log.i(TAG, "图像分辨率[width：" + cameraWidth + "；heigh：" + cameraHeight + "]");

//        camParams.setPreviewFrameRate(frameRate);
//        camParams.setPreviewFpsRange(20, 30);

        mCamera.setParameters(camParams);
        //取到的图像默认是横向的，这里旋转90度，保持和预览画面相同
        mCamera.setDisplayOrientation(90);
        startPreview();
    }

    /**
     * 开始预览.
     */
    public void startPreview() {
        try {
            mCamera.setPreviewCallback(this);
//            mCamera.setOneShotPreviewCallback(this);
            // set the surface to be used for live preview
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
            // 初始化是否自动扫描
            if (mScanActivity.isAutoScan()) {
                decodeBySelf();
            }
        } catch (IOException e) {
            Log.e(TAG, "启动扫描异常", e);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 停止预览.
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.setOneShotPreviewCallback(null);
            mCamera.setAutoFocusMoveCallback(null);
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
        }
    }

    /**
     * 打开指定摄像头.
     */
    public void openCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    mCamera = Camera.open(cameraId);
                } catch (Exception e) {
                    Log.e(TAG, "打开摄像头异常", e);
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                    }
                }
                break;
            }
        }
    }

    /**
     * 关闭释放摄像头.
     */
    public void release() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.setOneShotPreviewCallback(null);
            mCamera.setAutoFocusMoveCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 打开或关闭闪光灯.
     * @param open 控制是否打开
     * @return 打开或关闭失败，则返回false。
     */
    public boolean setFlashLight(boolean open) {
        if (null == mCamera) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) {
            // Use the screen as a flashlight (next best thing)
            return false;
        }
        String flashMode = parameters.getFlashMode();
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * 自动对焦处理.
     */
    /**
     * 摄像头自动对焦回调.
     */
    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                isAutoFocusing = false;
            }
            postDelayed(doAutoFocus, 500);
        }
    };

    /**
     * 执行自动对焦.
     */
    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null) {
                isAutoFocusing = true;
//                  mCamera.setOneShotPreviewCallback(WzScannerView.this);
                try {
                    mCamera.autoFocus(autoFocusCallback);
                } catch (Exception e) {
                    Log.e(TAG, "自动对焦异常", e);
                    postDelayed(doAutoFocus, 100);
                }
            }
        }
    };

    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        decodedImage(bytes, camera);
    }

    /*
     * 解码识别.
     */
    /** 是否正在自动对焦. */
    public boolean isAutoFocusing = true;
    /** 处理时间. */
    private long startTime, endTime;
    /** 是否Ocr识别中. */
    public boolean isOcrDecoding = true;
    /** 是否优图识别中. */
    public boolean isYoutuDecoding = true;
    /** 是否一维条码识别中. */
    public boolean isCodeDecoding = true;
    /** 是否QR码识别中. */
    public boolean isQRDecoding = true;

    // 解析结果
    private ArrayList<WzScanResult> scanRsltLs = new ArrayList<WzScanResult>();
    private WzScanResult scanResult = new WzScanResult();

    /**
     * 解析图片.
     * @param bytes 图片字节
     * @param camera 摄像头
     */
    private void decodedImage(final byte[] bytes, final Camera camera) {
        if ((isOcrDecoding && isCodeDecoding) || null == mScanActivity) {
            return;
        } else {
            if (scanResult.isRecipientMobileSuccess() && scanResult.isMailNoSuccess()) {
//            if (scanResult.isMailNoSuccess()) {
//                isCodeDecoding = true;
//                mScanActivity.setBtnTakePicShow();
//                return;
//            if (scanResult.isRecipientMobileSuccess()) {
                // 震动表示识别结束
                Vibrator vibrator = (Vibrator) mScanActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(200L);
                // 允许下次处理前不再进行扫描
                isOcrDecoding = true;
                isCodeDecoding = true;
                isYoutuDecoding = true;
                isQRDecoding = true;
                // 如果手动识别则关闭识别进度条
                if (!mScanActivity.isAutoScan()) {
                    mScanActivity.cancelProgressDialog();
                }
                // 如果是自动识别，则两次识别至少间隔1秒
                if (mScanActivity.isAutoScan()) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                    // 允许再次识别
                    isDecoding = false;
                    decodeBySelf();
                } else {
                    // 允许再次点击识别按钮
                    isDecoding = false;
                }
                // 如果是批量扫描
                if (mScanActivity.getDefMultipleScan()) {
                    // 将扫描结果放入列表
                    scanRsltLs.add(0, scanResult);
                    // 刷新结果列表
                    mScanActivity.refreshRsltLs(scanRsltLs);
                    // 初始化结果准备下次扫描
                    scanResult = new WzScanResult();
                } else {
                    // 将扫描结果放入列表
                    scanRsltLs.add(scanResult);
                    mScanActivity.handleDecode(scanRsltLs);
                    stopPreview();
                    return;
                }
            }
            // OCR解码
            if (!isOcrDecoding && null != bytes && 0 < bytes.length && !scanResult.isRecipientMobileSuccess()) {
                isOcrDecoding = true;
                new TessMobileThread(bytes, camera, getContext(), "eng", new TessSimpleCallback() {
                    @Override
                    public void response(boolean isOk, String respOcrText, Bitmap cropBmp) {
                        if (isOk) {
                            // 保存图片
                            try {
                                ByteArrayOutputStream tmpOcrStream = new ByteArrayOutputStream();
                                cropBmp.compress(Bitmap.CompressFormat.JPEG, 80, tmpOcrStream);
                                byte[] ocBmpBytes = tmpOcrStream.toByteArray();
                                tmpOcrStream.close();
                                scanResult.setBitmap(ocBmpBytes);
                                scanResult.setRecipientMobileSuccess(true);
                                scanResult.setRecipientMobile(respOcrText);
                            } catch (Exception e) {
                                isOcrDecoding = false;
                            }
                        }
                        isOcrDecoding = false;
                    }
                }).start();
            }
            // 优图解码
            if (!isYoutuDecoding && null != bytes && 0 < bytes.length && !scanResult.isRecipientMobileSuccess()) {
                isYoutuDecoding = true;
                new YoutuThread(bytes, camera, new YoutuSimpleCallback() {
                    @Override
                    public void response(boolean isOk, WzScanResult respOcrText, Bitmap cropBmp) {
                        try {
                            // 保存图片
                            ByteArrayOutputStream tmpYoutuStream = new ByteArrayOutputStream();
                            cropBmp.compress(Bitmap.CompressFormat.JPEG, 80, tmpYoutuStream);
                            byte[] ytBmpBytes = tmpYoutuStream.toByteArray();
                            tmpYoutuStream.close();
                            scanResult.setBitmap(ytBmpBytes);
                            if (isOk) {
                                scanResult.setRecipientMobileSuccess(true);
                                scanResult.setRecipientMobile(respOcrText.getRecipientMobile());
                                scanResult.setRecipientAddr(respOcrText.getRecipientAddr());
                                scanResult.setRecipientName(respOcrText.getRecipientName());
                            } else {
                                scanResult.setRecipientMobileSuccess(true);
                                scanResult.setRecipientMobile("未解析出内容");
                                scanResult.setRecipientAddr("未解析出内容");
                                scanResult.setRecipientName("未解析出内容");
                            }
                        } catch (Exception e) {
                            isOcrDecoding = false;
                            scanResult.setRecipientMobileSuccess(true);
                            scanResult.setRecipientMobile("解析异常");
                            scanResult.setRecipientAddr("解析异常");
                            scanResult.setRecipientName("解析异常");
                        }
                        isYoutuDecoding = false;
                    }
                }).start();
            }
            // 条码解码
            if (!isCodeDecoding && null != bytes && 0 < bytes.length && !scanResult.isMailNoSuccess()) {
                isCodeDecoding = true;
                new ZxingThread(bytes, camera, new ZxingSimpleCallback() {
                    @Override
                    public void response(boolean isOk, String respZxingText, Bitmap cropBmp) {
                        if (isOk) {
                            try {
//                                ByteArrayOutputStream tmpStream = new ByteArrayOutputStream();
//                                cropBmp.compress(Bitmap.CompressFormat.JPEG, 80, tmpStream);
//                                byte[] cdBmpBytes = tmpStream.toByteArray();
//                                tmpStream.close();
//                                scanResult.setBitmap(cdBmpBytes);
                                scanResult.setMailNoSuccess(true);
                                scanResult.setMailNo(respZxingText);
                                isCodeDecoding = false;
                            } catch (Exception e) {
                                Log.e(TAG, "条码处理结果失败", e);
                                isCodeDecoding = false;
                            }
                        } else {
                            isCodeDecoding = false;
                        }
                    }
                }).start();
            }
        }
    }


    /** 是否识别中. */
    public boolean isDecoding = false;

    /**
     * 手动识别.
     */
    public void decodeBySelf() {
        if (isDecoding) {
            return;
        } else {
            isDecoding = true;
            if (mScanActivity.getDefDecodeType().isDecodeOneCode()) {
                isCodeDecoding = false;
            } else {
                isCodeDecoding = true;
            }
            if (mScanActivity.getDefDecodeType().isDecodeExpressInfo()) {
                isYoutuDecoding = false;
                isOcrDecoding = true;
            } else {
                isYoutuDecoding = true;
                if (mScanActivity.getDefDecodeType().isDecodePhoneNo()) {
                    isOcrDecoding = false;
                } else {
                    isOcrDecoding = true;
                }
            }
            if (mScanActivity.getDefDecodeType().isDecodeQR()) {
                isQRDecoding = false;
            }
        }
    }

    /**
     * 结束识别并返回结果.
     */
    public void decodeOverAndBack() {
        mScanActivity.handleDecode(scanRsltLs);
    }
}
