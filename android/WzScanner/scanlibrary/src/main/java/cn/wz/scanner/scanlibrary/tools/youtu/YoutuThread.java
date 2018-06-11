package cn.wz.scanner.scanlibrary.tools.youtu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.tools.WzThread;
import cn.wz.scanner.scanlibrary.tools.image.ImageManager;
import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

/**
 * OCR解码工作线程（腾讯优图）.
 */
public class YoutuThread extends WzThread {

    /** TAG. */
    static final String TAG = "WZ_" + YoutuThread.class.getName();
    /** 待解析图片. */
    private Bitmap bmpWithOcr;
    /** 解析回调. */
    private YoutuSimpleCallback ytCallback;
    /** 图片字节. */
    private byte[] bmpBytes;
    /** 摄像头. */
    private Camera tessCamera;

    /**
     * 构造方法.
     * @param pBytes 待解析图片
     * @param pCamera 摄像头
     * @param cb 解析回调
     */
    public YoutuThread(final byte[] pBytes, final Camera pCamera, YoutuSimpleCallback cb) {
        this.bmpBytes = pBytes;
        this.tessCamera = pCamera;
        this.ytCallback = cb;
    }

    @Override
    public void run() {
        Log.d(TAG, "------------- OCR解析开始 ------------------");
        // OCR解析
        try {
            // 获取帧图片
            long subStartTime = System.currentTimeMillis();
            Camera.Size ocrPreSize = tessCamera.getParameters().getPreviewSize();
            Log.d(TAG, "Ocr预览范围：" + ocrPreSize.width + " x " + ocrPreSize.height);
            final YuvImage ocrImage = new YuvImage(bmpBytes, tessCamera.getParameters().getPreviewFormat(), ocrPreSize.width, ocrPreSize.height, null);
            Log.d(TAG, "Ocr预览图大小：" + ocrImage.getWidth() + " x " + ocrImage.getHeight());
            if (null == ocrImage) {
                Log.e(TAG, "Ocr帧数据转换图像中间过程失败");
                ytCallback.response(false, null, null);
            }
            // 直接截图
            int width = (int) ocrPreSize.width * 1 / 10;
            int height = (int) ocrPreSize.height * 5 / 6;
            int left = (ocrPreSize.width - width) / 4;
//            int left = ocrPreSize.width / 3;
            int top = (ocrPreSize.height - height) / 2;
            int right = left + width;
            int bottom = top + height;
            ByteArrayOutputStream ocrStream = new ByteArrayOutputStream();
            ocrImage.compressToJpeg(new Rect(left, top, right, bottom + 50), 80, ocrStream);
            bmpWithOcr = BitmapFactory.decodeByteArray(ocrStream.toByteArray(), 0, ocrStream.size());
            ocrStream.close();
            if (null == bmpWithOcr) {
                Log.e(TAG, "Ocr帧数据转换图像失败");
                ytCallback.response(false, null, null);
                return;
            }
            bmpWithOcr = ImageManager.rotateToDegrees(bmpWithOcr, 90);
            long subEndTime = System.currentTimeMillis();
            Log.i(TAG, "处理Ocr帧图像耗时: " + (subEndTime - subStartTime) + "ms");

            // 截取图片
//            int cMiddle = bmpWithOcr.getHeight() / 3;
//            int cTop = cMiddle - 80;
//            cTop = (cTop >= 0) ? cTop : 0;
//            int cBottom = cMiddle + 80;
//            cBottom = (cBottom <= bmpWithOcr.getHeight()) ? cBottom : bmpWithOcr.getHeight();
//            bmpWithOcr = ImageManager.cropByRect(bmpWithOcr, new Rect(0, cTop, bmpWithOcr.getWidth(), cBottom));
            // 缩放
//            bmpWithOcr = ImageManager.scale(bmpWithOcr, 0.5f);
            // 灰度化
//            bmpWithOcr = ImageManager.toGray(bmpWithOcr);
            // 二值化
//            bmpWithOcr = ImageManager.binarization(bmpWithOcr);
            // 锐化
//            bmpWithOcr = ImageManager.sharpenImageAmeliorate(bmpWithOcr);

//            // 膨胀
//            bmpWithOcr = ImageManager.expend(bmpWithOcr);
//            bmpWithOcr = ImageManager.expend(bmpWithOcr);
//            bmpWithOcr = ImageManager.expend(bmpWithOcr);
//            bmpWithOcr = ImageManager.corrode(bmpWithOcr);
//            bmpWithOcr = ImageManager.catchPhoneRect(bmpWithOcr);
            if (null != bmpWithOcr) {
                subStartTime = System.currentTimeMillis();
                YoutuManager ym = new YoutuManager("10133090",
                        "AKID0ucAWgpSAvhOGGpjy2udAVlurfHhOIUa",
                        "00rnfYSJ3bx7GPZm6Si4SmvsdEeMyOgm");

                Bitmap uBmp = bmpWithOcr.copy(Bitmap.Config.ARGB_8888, true);
                WzScanResult rsltTxt = ym.detectMobileNo(uBmp);
                subEndTime = System.currentTimeMillis();
                Log.i(TAG, "OCR解析耗时: " + (subEndTime - subStartTime) + "ms");
                Log.d(TAG, "OCR解析结果: " + (WzStringUtil.isBlank(rsltTxt.toString()) ? "" : rsltTxt));
                Log.d(TAG, "------------- OCR解析结束 ------------------");
//                String rsltTxt = "13913913913";
                if (null != rsltTxt && WzStringUtil.isNotBlank(rsltTxt.getRecipientMobile())) {
                    ytCallback.response(true, rsltTxt, bmpWithOcr);
                } else {
                    ytCallback.response(false, null, null);
                }
            } else {
                Log.e(TAG, "Ocr图像为空");
                ytCallback.response(false, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Ocr解析异常", e);
            bmpWithOcr.recycle();
            bmpWithOcr = null;
            ytCallback.response(false, null, null);
        }
//        getCdl().countDown();
    }
}
