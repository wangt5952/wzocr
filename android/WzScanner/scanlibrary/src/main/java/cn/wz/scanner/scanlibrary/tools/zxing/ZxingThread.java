package cn.wz.scanner.scanlibrary.tools.zxing;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import cn.wz.scanner.scanlibrary.tools.WzThread;
import cn.wz.scanner.scanlibrary.tools.image.ImageManager;
import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

/**
 * 条码解码工作线程.
 */
public class ZxingThread extends WzThread {
    /** TAG. */
    static final String TAG = "WZ_" + ZxingThread.class.getName();
    /** 待解析图片. */
    private Bitmap bmpWithCode;
    /** 解析回调. */
    private ZxingSimpleCallback zxCallBack;
    /** 图像数据. */
    private byte[] bmpBytes;
    /** 摄像头 */
    private Camera zxCamera;

    /**
     * 构造方法.
     * @param pBmpbytes 待解析图片
     * @param cb 回调
     */
    public ZxingThread(final byte[] pBmpbytes, final Camera pCamera,  ZxingSimpleCallback cb) {
        this.bmpBytes = pBmpbytes;
        this.zxCallBack = cb;
        this.zxCamera = pCamera;
    }

    @Override
    public void run() {
        Log.d(TAG, "------------- 条码解析开始 ------------------");
        // 条码解析
        try {
            // 获取帧图片
            long subStartTime = System.currentTimeMillis();
            Camera.Size zxPreSize = zxCamera.getParameters().getPreviewSize();
            Log.d(TAG, "条码预览范围：" + zxPreSize.width + " x " + zxPreSize.height);
            final YuvImage codeImage = new YuvImage(bmpBytes, zxCamera.getParameters().getPreviewFormat(), zxPreSize.width, zxPreSize.height, null);
            Log.d(TAG, "条码预览图大小：" + codeImage.getWidth() + " x " + codeImage.getHeight());
            if (null == codeImage) {
                Log.e(TAG, "条码帧数据转换图像中间过程失败");
                zxCallBack.response(false, null, null);
            }
            // 直接截图
            int width = (int) zxPreSize.width * 2 / 3;
            int height = (int) zxPreSize.height * 9 / 10;
            int left = (zxPreSize.width - width) / 2;
            int top = (zxPreSize.height - height) / 2;
            int right = left + width;
            int bottom = top + height;
            ByteArrayOutputStream codeStream = new ByteArrayOutputStream();
            codeImage.compressToJpeg(new Rect(left, top, right, bottom), 80, codeStream);
            bmpWithCode = BitmapFactory.decodeByteArray(codeStream.toByteArray(), 0, codeStream.size());
            codeStream.close();
            if (null == bmpWithCode) {
                Log.e(TAG, "条码帧数据转换图像失败");
                zxCallBack.response(false, null, null);
                return;
            }
            bmpWithCode = ImageManager.rotateToDegrees(bmpWithCode, 90);
            long subEndTime = System.currentTimeMillis();
            Log.d(TAG, "处理条码帧图像耗时: " + (subEndTime - subStartTime) + "ms");
            bmpWithCode = ImageManager.scale(bmpWithCode, 0.5f);
            if (null != bmpWithCode) {
                subStartTime = System.currentTimeMillis();
                ZxingManager zxManager = new ZxingManager(null);
                String rsltTxt = zxManager.detectCode(bmpWithCode);
                subEndTime = System.currentTimeMillis();
                Log.d(TAG, "条码解析耗时: " + (subEndTime - subStartTime) + "ms");
                Log.d(TAG, "条码解析结果: " + (WzStringUtil.isBlank(rsltTxt) ? "" : rsltTxt));
                Log.d(TAG, "------------- 条码解析结束 ------------------");
                if (WzStringUtil.isNotBlank(rsltTxt)) {
                    zxCallBack.response(true, rsltTxt, bmpWithCode);
                } else {
                    zxCallBack.response(false, null, null);
                }
            } else {
                Log.e(TAG, "条码图像为空");
                zxCallBack.response(false, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "条码解析异常", e);
            zxCallBack.response(false, null, null);
        }
//        getCdl().countDown();
    }
}
