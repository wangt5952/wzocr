package cn.wz.scanner.scanlibrary.tools.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * 图像处理工具类.
 */
public class ImageManager {
    /** TAG. */
    static final String TAG = "WZ_" + ImageManager.class.getName();

    /**
     * 图片旋转.
     * @param tmpBitmap 原图
     * @param degrees 旋转角度
     * @return 旋转后的图
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        Bitmap newBmp = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix, true);
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "旋转图像耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 剪切图片.
     * @param tmpBitmap 原图
     * @param cropRect 剪切范围
     * @return 新图
     */
    public static Bitmap cropByRect(Bitmap tmpBitmap, Rect cropRect) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        try {
            Bitmap newBmp = Bitmap.createBitmap(tmpBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
            long subEndTime = System.currentTimeMillis();
            Log.i(TAG, "剪切图像耗时: " + (subEndTime - subStartTime) + "ms");
            return newBmp;
        } catch (Exception e) {
            Log.e(TAG, "剪切图像异常", e);
            return tmpBitmap;
        }
    }

    /**
     * 图片灰度化.
     * @param tmpBitmap 源图
     * @return 新图
     */
    public static Bitmap toGray(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        Paint paint = new Paint();
        ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(cmcf);
        Bitmap newBmp = Bitmap.createBitmap(tmpBitmap.getWidth(), tmpBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas drawingCanvas = new Canvas(newBmp);
        Rect src = new Rect(0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight());
        Rect dst = new Rect(src);
        drawingCanvas.drawBitmap(tmpBitmap, src, dst, paint);
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图像灰度化耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 线性灰度化.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap lineGrey(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        //得到图像的宽度和长度
        int width = tmpBitmap.getWidth();
        int height = tmpBitmap.getHeight();
        //创建线性拉升灰度图像
        Bitmap linegrayBmp = null;
        linegrayBmp = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到每点的像素值
                int col = tmpBitmap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 增加了图像的亮度
                red = (int) (1.1 * red + 30);
                green = (int) (1.1 * green + 30);
                blue = (int) (1.1 * blue + 30);
                // 对图像像素越界进行处理
                if (red >= 255) {
                    red = 255;
                }

                if (green >= 255) {
                    green = 255;
                }

                if (blue >= 255) {
                    blue = 255;
                }
                // 新的ARGB
                int newColor = alpha | (red << 16) | (green << 8) | blue;
                //设置新图像的RGB值
                linegrayBmp.setPixel(i, j, newColor);
            }
        }
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "线性灰度化耗时: " + (subEndTime - subStartTime) + "ms");
        return linegrayBmp;
    }

    /**
     * 图片二值化.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap binarization(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        int width = tmpBitmap.getWidth();
        int height = tmpBitmap.getHeight();
        Bitmap newBmp = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            // 不算边界行和列，为避免越界
            for (int j = 0; j < height; j++) {
                // 得到当前像素的值
                int col = newBmp.getPixel(i, j);
                // 得到alpha通道的值
                int alpha = col & 0xFF000000;
                // 得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                // 对图像进行二值化处理
                if (95 >= gray) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB
//                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                // 设置新图像的当前像素值
                newBmp.setPixel(i, j, Color.argb(alpha, gray, gray, gray));
            }
        }
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图像二值化耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 图片锐化（拉普拉斯变换）.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap sharpenImageAmeliorate(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        // 拉普拉斯矩阵
        int[] laplacian = new int[] { -1, -1, -1, -1, 9, -1, -1, -1, -1 };

        int width = tmpBitmap.getWidth();
        int height = tmpBitmap.getHeight();
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;

        int idx = 0;
        float alpha = 0.3F;
        int[] pixels = new int[width * height];
        tmpBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                idx = 0;
                for (int m = -1; m <= 1; m++) {
                    for (int n = -1; n <= 1; n++) {
                        pixColor = pixels[(i + n) * width + k + m];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);

                        newR = newR + (int) (pixR * laplacian[idx] * alpha);
                        newG = newG + (int) (pixG * laplacian[idx] * alpha);
                        newB = newB + (int) (pixB * laplacian[idx] * alpha);
                        idx++;
                    }
                }

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
                newR = 0;
                newG = 0;
                newB = 0;
            }
        }

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图片锐化（拉普拉斯变换）耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /*
     * 图像的膨胀腐蚀.
     */
    /**
     * 返回(x,y)周围像素的情况.
     * @param tmpBitmap 原图
     * @param x x坐标
     * @param y y坐标
     * @return 黑色为true
     */
    public static boolean[] getRoundPixel(Bitmap tmpBitmap, int x, int y) {
        boolean[] pixels = new boolean[8];
        int num = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                // 得到当前像素的值
                int col = tmpBitmap.getPixel(x + i, y + j);
                if (i != 0 || j != 0) {
                    // 因为经过了二值化，所以只要检查RGB中一个属性的值
                    if (255 == col) {
                        // 为白色，设置为false
                        pixels[num] = false;
                        num++;
                    } else if(0 == col) {
                        // 为黑色，设置为true
                        pixels[num] = true;
                        num++;
                    }
                }
            }
        }
        return pixels;
    }

    /**
     * 图像膨胀.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap expend(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        Bitmap newBmp = tmpBitmap.copy(tmpBitmap.getConfig(), true);
        int height = tmpBitmap.getHeight();
        int width = tmpBitmap.getWidth();
        boolean[] pixels;
        for (int i = 1; i < width-1; i++) {
            for (int j = 1; j < height-1; j++) {
                int col = tmpBitmap.getPixel(i, j);
                // 得到alpha通道的值
                int alpha = col & 0xFF000000;
//                // 得到图像的像素RGB的值
//                int red = (col & 0x00FF0000) >> 16;
//                int green = (col & 0x0000FF00) >> 8;
//                int blue = (col & 0x000000FF);
                if (0 != col) {
                    pixels = getRoundPixel(tmpBitmap, i, j);
                    for (int k = 0; k < pixels.length; k++) {
                        if (pixels[k]) {
                            // 设黑色
                            newBmp.setPixel(i, j, Color.argb(alpha, 0, 0, 0));
                            break;
                        }
                    }
                }
            }
        }
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图像膨胀耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 图像腐蚀.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap corrode(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        Bitmap newBmp = tmpBitmap.copy(tmpBitmap.getConfig(), true);
        int height = tmpBitmap.getHeight();
        int width = tmpBitmap.getWidth();
        boolean[] pixels;
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                int col = tmpBitmap.getPixel(i, j);
                // 得到alpha通道的值
                int alpha = col & 0xFF000000;
//                // 得到图像的像素RGB的值
//                int red = (col & 0x00FF0000) >> 16;
//                int green = (col & 0x0000FF00) >> 8;
//                int blue = (col & 0x000000FF);
                if (0 == col) {
                    pixels = getRoundPixel(tmpBitmap, i, j);
                    for (int k = 0; k < pixels.length; k++) {
                        if (false == pixels[k]) {
                            // 设白色
                            newBmp.setPixel(i, j, Color.argb(alpha, 255, 255, 255));
                            break;
                        }
                    }
                }
            }
        }
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图像腐蚀耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 缩放.
     * @param tmpBitmap 原图
     * @param usedScale 缩放比例
     * @return 新图
     */
    public static Bitmap scale(Bitmap tmpBitmap, float usedScale) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        Matrix matrix = new Matrix();
        matrix.setScale(usedScale, usedScale);
        Bitmap newBmp = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix, true);
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "图像缩放耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }

    /**
     * 将图片中大片白底区域剪切出来.
     * @param tmpBitmap 原图
     * @return 新图
     */
    public static Bitmap cropWhiteRect(Bitmap tmpBitmap) {
        if (null == tmpBitmap) {
            return null;
        }
        long subStartTime = System.currentTimeMillis();
        int width = tmpBitmap.getWidth();
        int height = tmpBitmap.getHeight();
        int cropLeft = 0;
        int cropTop = 0;
        int cropRight = width - 10;
        int cropBottom = height - 10;
        boolean isLeftEdge = false;
        boolean isTopEdge = false;
        boolean isRightEdge = false;
        boolean isBottomEdge = false;
        for (int i = 1; i < width - 11; i++) {
            for (int j = 1; j < height - 11; j++) {
                if (255 == tmpBitmap.getPixel(i, j)) {
                    if (!isLeftEdge && (255 == tmpBitmap.getPixel(i + 1, j))
                            && (255 == tmpBitmap.getPixel(i + 2, j))
                            && (255 == tmpBitmap.getPixel(i + 3, j))
                            && (255 == tmpBitmap.getPixel(i + 4, j))
                            && (255 == tmpBitmap.getPixel(i + 5, j))
                            && (255 == tmpBitmap.getPixel(i + 6, j))
                            && (255 == tmpBitmap.getPixel(i + 7, j))
                            && (255 == tmpBitmap.getPixel(i + 8, j))
                            && (255 == tmpBitmap.getPixel(i + 9, j))
                            && (255 == tmpBitmap.getPixel(i + 10, j))) {
                        cropLeft = i;
                        isLeftEdge = true;
                    }
                    if (!isTopEdge && (255 == tmpBitmap.getPixel(i, j + 1))
                            && (255 == tmpBitmap.getPixel(i, j + 2))
                            && (255 == tmpBitmap.getPixel(i, j + 3))
                            && (255 == tmpBitmap.getPixel(i, j + 4))
                            && (255 == tmpBitmap.getPixel(i, j + 5))
                            && (255 == tmpBitmap.getPixel(i, j + 6))
                            && (255 == tmpBitmap.getPixel(i, j + 7))
                            && (255 == tmpBitmap.getPixel(i, j + 8))
                            && (255 == tmpBitmap.getPixel(i, j + 9))
                            && (255 == tmpBitmap.getPixel(i, j + 10))) {
                        cropTop = i;
                        isTopEdge = true;
                    }
                }
                if (isLeftEdge && isTopEdge) {
                    break;
                }
            }
        }
        for (int i = width - 1; i >= cropLeft + 10; i--) {
            for (int j = height - 1; j >= cropTop + 10; j--) {
                if (255 == tmpBitmap.getPixel(i, j)) {
                    if (!isLeftEdge && (255 == tmpBitmap.getPixel(i - 1, j))
                            && (255 == tmpBitmap.getPixel(i - 2, j))
                            && (255 == tmpBitmap.getPixel(i - 3, j))
                            && (255 == tmpBitmap.getPixel(i - 4, j))
                            && (255 == tmpBitmap.getPixel(i - 5, j))
                            && (255 == tmpBitmap.getPixel(i - 6, j))
                            && (255 == tmpBitmap.getPixel(i - 7, j))
                            && (255 == tmpBitmap.getPixel(i - 8, j))
                            && (255 == tmpBitmap.getPixel(i - 9, j))
                            && (255 == tmpBitmap.getPixel(i - 10, j))) {
                        cropRight = i;
                        isRightEdge = true;
                    }
                    if (!isTopEdge && (255 == tmpBitmap.getPixel(i, j - 1))
                            && (255 == tmpBitmap.getPixel(i, j - 2))
                            && (255 == tmpBitmap.getPixel(i, j - 3))
                            && (255 == tmpBitmap.getPixel(i, j - 4))
                            && (255 == tmpBitmap.getPixel(i, j - 5))
                            && (255 == tmpBitmap.getPixel(i, j - 6))
                            && (255 == tmpBitmap.getPixel(i, j - 7))
                            && (255 == tmpBitmap.getPixel(i, j - 8))
                            && (255 == tmpBitmap.getPixel(i, j - 9))
                            && (255 == tmpBitmap.getPixel(i, j - 10))) {
                        cropBottom = i;
                        isBottomEdge = true;
                    }
                }
                if (isRightEdge && isBottomEdge) {
                    break;
                }
            }
        }
        if (cropLeft >= cropRight || cropTop >= cropBottom) {
            // 找不到白色区域，则表示没有扫到面单
            return null;
        }
        Bitmap newBmp = ImageManager.cropByRect(tmpBitmap, new Rect(cropLeft, cropTop, cropRight, cropBottom));
        long subEndTime = System.currentTimeMillis();
        Log.i(TAG, "剪切图片中白色背景区域耗时: " + (subEndTime - subStartTime) + "ms");
        return newBmp;
    }
}
