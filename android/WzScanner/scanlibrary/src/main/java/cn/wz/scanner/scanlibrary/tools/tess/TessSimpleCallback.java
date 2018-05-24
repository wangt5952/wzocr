package cn.wz.scanner.scanlibrary.tools.tess;

import android.graphics.Bitmap;

/**
 * Tess-Two识别回调.
 */
public interface TessSimpleCallback {
    void response(boolean isOk, String respOcrText, Bitmap cropBmp);
}
