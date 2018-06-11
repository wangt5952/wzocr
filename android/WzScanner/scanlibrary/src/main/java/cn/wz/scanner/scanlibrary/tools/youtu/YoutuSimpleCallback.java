package cn.wz.scanner.scanlibrary.tools.youtu;

import android.graphics.Bitmap;

import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

/**
 * YouTu识别回调.
 */
public interface YoutuSimpleCallback {
    void response(boolean isOk, WzScanResult respOcrText, Bitmap cropBmp);
}
