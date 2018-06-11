package cn.wz.scanner;

import android.content.Context;

/**
 * Created by Administrator on 2016/3/17.
 */
public class DensityUtil {

    /**
     * 根据手机的分辨率从dp的单位转成为px(像素)
     */
    public static int dpTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从px(像素)的单位转成为dp
     */
    public static int pxTodp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
