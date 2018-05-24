package cn.wz.scanner.scanlibrary.tools.zxing;

import android.graphics.Bitmap;

/**
 * Zxing识别回调.
 */
public interface ZxingSimpleCallback {
    void response(boolean isOk, String respZxingText, Bitmap cropBmp);
}
