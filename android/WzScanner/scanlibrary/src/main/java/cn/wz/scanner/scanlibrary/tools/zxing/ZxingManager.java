package cn.wz.scanner.scanlibrary.tools.zxing;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

public class ZxingManager {
    /** TAG. */
    static final String TAG = "WZ_" + ZxingManager.class.getName();

    /** 识别参数. */
    private Map<DecodeHintType, Object> mHints;

    /**
     * 构造方法.
     * @param pHint Zxing识别参数
     */
    public ZxingManager(Map<DecodeHintType, Object> pHint) {
        if (null == pHint) {
            mHints = new Hashtable<>();
            mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
            mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Collection<BarcodeFormat> barcodeFormats = new ArrayList<>();
            barcodeFormats.add(BarcodeFormat.CODE_39);
            barcodeFormats.add(BarcodeFormat.CODE_128); // 快递单常用格式39,128
            barcodeFormats.add(BarcodeFormat.CODABAR);
            barcodeFormats.add(BarcodeFormat.CODE_93);
            barcodeFormats.add(BarcodeFormat.EAN_8);
            barcodeFormats.add(BarcodeFormat.EAN_13);
            barcodeFormats.add(BarcodeFormat.UPC_A);
            barcodeFormats.add(BarcodeFormat.UPC_E);
//        barcodeFormats.add(BarcodeFormat.QR_CODE); //扫描格式自行添加
            mHints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);
        } else {
            this.mHints = pHint;
        }
    }

    /**
     * 解码一维码或二维码.
     * @param pBmp 图像
     * @return 解码结果
     */
    public String detectCode(final Bitmap pBmp) {
        MultiFormatReader mfr = new MultiFormatReader();
        int[] intArray = new int[pBmp.getWidth() * pBmp.getHeight()];
        // 将图像像素拷贝到intArray中
        pBmp.getPixels(intArray, 0, pBmp.getWidth(), 0, 0, pBmp.getWidth(), pBmp.getHeight());
        LuminanceSource source = new RGBLuminanceSource(pBmp.getWidth(), pBmp.getHeight(), intArray);
        BinaryBitmap bBmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result zxDetectResult = mfr.decode(bBmap, mHints);
            if (null == zxDetectResult || WzStringUtil.isBlank(zxDetectResult.getText())) {
                Log.e(TAG, "条码解码结果为空");
                return null;
            } else {
                Log.i(TAG, "条码解码成功");
                return zxDetectResult.getText();
            }
        } catch (Exception e) {
            Log.e(TAG, "条码解码失败", e);
            return null;
        }
    }
}
