package cn.wz.scanner.scanlibrary.tools.tess;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import java.util.Stack;

/**
 * Created by Sikang on 2017/7/12.
 */

public class TesseractUtil {

    private float proportion = 0.5f;

    /**
     * 调整阈值
     *
     * @param pro 调整比例
     */
    public int adjustThresh(float pro) {
        this.proportion += pro;

        if (proportion > 1f)
            proportion = 1f;
        if (proportion < 0)
            proportion = 0;
        return (int) (proportion * 100);
    }


    /**
     * 图片旋转
     *
     * @param tmpBitmap
     * @param degrees
     * @return
     */
    public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degrees);
        return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix,
                true);
    }

    private static final int PX_WHITE = -1;
    private static final int PX_BLACK = -16777216;
    private static final int PX_UNKNOW = -2;

    /**
     * 转为二值图像 并判断图像中是否可能有手机号
     *
     * @param bmp 原图bitmap
     * @return
     */
    public static Bitmap catchPhoneRect(final Bitmap bmp) {
        if (null == bmp) {
            return null;
        }
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int left = width;
        int top = height;
        int right = 0;
        int bottom = 0;

        /**
         * 二值化
         * */
        binarization(pixels, width, height);
        int space = 0;
        int textWidth = 0;
        int startX = 0;
        int centerY = height / 2 - 1;
        int textLength = 0;
        int textStartX = 0;

        /**
         * 遍历中间一行像素，粗略捕捉手机号
         * */
        for (int j = 0; j < width; j++) {
            int gray = pixels[width * centerY + j];
            pixels[width * centerY + j] = getColor(gray);
            if (pixels[width * centerY + j] == PX_WHITE) {
                if (space == 1)
                    textLength++;
                if (textWidth > 0 && startX > 0 && startX < height - 1 && (space > width / 10 || j == width - 1)) {
                    if (textLength > 10 && textLength < 22)
                        if (textWidth > right - left) {
                            left = j - space - textWidth - (space / 2);
                            if (left < 0)
                                left = 0;

                            right = j - 1 - (space / 2);
                            if (right > width)
                                right = width - 1;
                            textStartX = startX;
                        }
                    textLength = 0;
                    space = 0;
                    startX = 0;
                }
                space++;
            } else {
//                pixels[width * centerY + j] = PX_BLACK;
                if (startX == 0)
                    startX = j;
                textWidth = j - startX;
                space = 0;
            }

        }

        if (right - left < width * 0.3f) {
            bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            Log.d("scantest", "初步判断无手机号码");
            return null;
        }
        Log.d("scantest", "初步判断可能存在手机号码");

        //粗略计算高度
        top = (int) (centerY - (right - left) / 11 * 1.5);
        bottom = (int) (centerY + (right - left) / 11 * 1.5);
        if (top < 0)
            top = 0;
        if (bottom > height)
            bottom = height - 1;

        /**
         * 判断区域中有几个字符
         * */
        //已经使用过的像素标记
        int[] usedPixels = new int[width * height];
        int[] textRect = new int[]{right, bottom, 0, 0};
        //当前捕捉文字的rect
        int[] charRect = new int[]{textStartX, centerY, 0, centerY};
        //在文字块中捕捉到的字符个数
        int charCount = 0;
        //是否发现干扰
        boolean hasStain = false;
        startX = left;
        int charMaxWidth = (right - left) / 11;
        int charMaxHeight = (int) ((right - left) / 11 * 1.5);
        int charWidth = 0;
        boolean isInterfereClearing = false;
        while (true) {
            boolean isNormal = false;
            if (!isInterfereClearing)
                isNormal = catchCharRect(pixels, usedPixels, charRect, width, height, charMaxWidth, charMaxHeight, charRect[0], charRect[1]);
            else
                isNormal = clearInterfere(pixels, usedPixels, charRect, width, height, charWidth, charWidth, charRect[0], charRect[1]);
            charCount++;

            if (!isNormal) {
                hasStain = true;
                if (charWidth != 0) {
                    usedPixels = new int[width * height];
                    charRect = new int[]{textStartX, centerY, 0, centerY};
                    charCount = 0;
                    isInterfereClearing = true;
                }
            } else {
                if (hasStain && !isInterfereClearing) {
                    usedPixels = new int[width * height];
                    charWidth = charRect[3] - charRect[1];
//                    charMaxWidth = charMaxHeight;
                    charRect = new int[]{textStartX, centerY, 0, centerY};
                    charCount = 0;
                    isInterfereClearing = true;
                    continue;
                } else {
                    if (charWidth == 0) {
                        charWidth = charRect[3] - charRect[1];
                    }
                    if (textRect[0] > charRect[0])
                        textRect[0] = charRect[0];

                    if (textRect[1] > charRect[1])
                        textRect[1] = charRect[1];

                    if (textRect[2] < charRect[2])
                        textRect[2] = charRect[2];

                    if (textRect[3] < charRect[3])
                        textRect[3] = charRect[3];

                }
            }

            boolean isFoundChar = false;
            if (!hasStain || isInterfereClearing) {
                //获取下一个字符的rect
                for (int x = charRect[2] + 1; x <= right; x++)
                    if (pixels[width * centerY + x] != PX_WHITE) {
                        isFoundChar = true;
                        charRect[0] = x;
                        charRect[1] = centerY;
                        charRect[2] = 0;
                        charRect[3] = 0;
                        break;
                    }
            } else {
                for (int x = left; x <= right; x++)
                    if (pixels[width * centerY + x] != PX_WHITE && pixels[width * centerY + x - 1] == PX_WHITE) {
                        if (x <= startX)
                            continue;
                        startX = x;
                        isFoundChar = true;
                        charRect[0] = x;
                        charRect[1] = centerY;
                        charRect[2] = x;
                        charRect[3] = centerY;
                        break;
                    }
            }
            if (!isFoundChar) {
                break;
            }
        }


        left = textRect[0];
        top = textRect[1];
        right = textRect[2];
        bottom = textRect[3];
        Log.d("scantest", "捕捉到到 " + charCount + " 个字符 ");
        if (bottom - top > (right - left) / 5 || bottom - top == 0 || charCount != 11) {
            bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            return null;
        }

        Log.d("scantest", "发现手机号，准备识别");
        /**
         * 将最终捕捉到的手机号区域像素提取到新的数组
         * */
        int targetWidth = right - left;
        int targetHeight = bottom - top;
        int[] targetPixels = new int[targetWidth * targetHeight];
        int index = 0;

        for (int i = top; i < bottom; i++) {
            for (int j = left; j < right; j++) {
                if (index < targetPixels.length) {
                    if (pixels[width * i + j] == PX_BLACK)
                        targetPixels[index] = PX_BLACK;
                    else
                        targetPixels[index] = PX_WHITE;
                }
                index++;
            }
        }

        bmp.recycle();
        // 新建图片
        final Bitmap newBmp = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(targetPixels, 0, targetWidth, 0, 0, targetWidth, targetHeight);

        return newBmp;
    }

    private static final int MOVE_LEFT = 0;
    private static final int MOVE_TOP = 1;
    private static final int MOVE_RIGHT = 2;
    private static final int MOVE_BOTTOM = 3;

    /**
     * 捕捉字符
     */
    private static boolean catchCharRect(int[] pixels, int[] used, int[] charRect, int width, int height, int maxWidth, int maxHeight, int x, int y) {
        int nowX = x;
        int nowY = y;
        //记录动作
        Stack<Integer> stepStack = new Stack<>();

        while (true) {
            if (used[width * nowY + nowX] == 0) {
                used[width * nowY + nowX] = -1;
                if (charRect[0] > nowX)
                    charRect[0] = nowX;

                if (charRect[1] > nowY)
                    charRect[1] = nowY;

                if (charRect[2] < nowX)
                    charRect[2] = nowX;

                if (charRect[3] < nowY)
                    charRect[3] = nowY;

                if (charRect[2] - charRect[0] > maxWidth) {
                    return false;
                }

                if (charRect[3] - charRect[1] > maxHeight) {
                    return false;
                }

                if (nowX == 0 || nowX >= width - 1 || nowY == 0 || nowY >= height - 1) {
                    return false;
                }
            }

            //当前像素的左边是否还有黑色像素点
            int leftX = nowX - 1;
            if (leftX >= 0 && pixels[width * nowY + leftX] != PX_WHITE && used[width * nowY + leftX] == 0) {
                nowX = leftX;
                stepStack.push(MOVE_LEFT);
                continue;
            }

            //当前像素的上边是否还有黑色像素点
            int topY = nowY - 1;
            if (topY >= 0 && pixels[width * topY + nowX] != PX_WHITE && used[width * topY + nowX] == 0) {
                nowY = topY;
                stepStack.push(MOVE_TOP);
                continue;
            }


            //当前像素的右边是否还有黑色像素点
            int rightX = nowX + 1;
            if (rightX < width && pixels[width * nowY + rightX] != PX_WHITE && used[width * nowY + rightX] == 0) {
                nowX = rightX;
                stepStack.push(MOVE_RIGHT);
                continue;
            }


            //当前像素的下边是否还有黑色像素点
            int bottomY = nowY + 1;
            if (bottomY < height && pixels[width * bottomY + nowX] != PX_WHITE && used[width * bottomY + nowX] == 0) {
                nowY = bottomY;
                stepStack.push(MOVE_BOTTOM);
                continue;
            }

            if (stepStack.size() > 0) {
                int step = stepStack.pop();
                switch (step) {
                    case MOVE_LEFT:
                        nowX++;
                        break;
                    case MOVE_RIGHT:
                        nowX--;
                        break;
                    case MOVE_TOP:
                        nowY++;
                        break;
                    case MOVE_BOTTOM:
                        nowY--;
                        break;
                }
            } else {
                break;
            }
        }
        if (charRect[2] - charRect[0] == 0 || charRect[3] - charRect[1] == 0) {
            return false;
        }
        return true;
    }

    private static final int WAIT_HANDLE = 0;//待处理像素
    private static final int HANDLED = -1;//已处理像素
    private static final int HANDLING = -2;//处理过但未处理完成的像素

    /**
     * 清除干扰
     */
    private static boolean clearInterfere(int[] pixels, int[] used, int[] charRect, int width, int height, int maxWidth, int maxHeight, int x, int y) {
        int nowX = x;
        int nowY = y;
        //记录动作
        Stack<Integer> stepStack = new Stack<>();
        boolean needReset = true;
        while (true) {
            if (used[width * nowY + nowX] == WAIT_HANDLE) {
                used[width * nowY + nowX] = HANDLED;

                if (charRect[2] - charRect[0] <= maxWidth && charRect[3] - charRect[1] <= maxHeight) {
                    if (charRect[0] > nowX)
                        charRect[0] = nowX;

                    if (charRect[1] > nowY)
                        charRect[1] = nowY;

                    if (charRect[2] < nowX)
                        charRect[2] = nowX;

                    if (charRect[3] < nowY)
                        charRect[3] = nowY;
                } else {
                    if (needReset)
                        needReset = false;
                    used[width * nowY + nowX] = HANDLING;
                    pixels[width * nowY + nowX] = PX_UNKNOW;
                }
            } else if (pixels[width * nowY + nowX] == PX_UNKNOW) {

                if (charRect[2] - charRect[0] <= maxWidth && charRect[3] - charRect[1] <= maxHeight) {
                    pixels[width * nowY + nowX] = PX_BLACK;
                    if (charRect[0] > nowX)
                        charRect[0] = nowX;

                    if (charRect[1] > nowY)
                        charRect[1] = nowY;

                    if (charRect[2] < nowX)
                        charRect[2] = nowX;

                    if (charRect[3] < nowY)
                        charRect[3] = nowY;

                    used[width * nowY + nowX] = HANDLED;
                } else {
                    if (needReset)
                        needReset = false;
                }
            }


            //当前像素的左边是否还有黑色像素点
            int leftX = nowX - 1;
            int leftIndex = width * nowY + leftX;
            if (leftX >= 0 && pixels[leftIndex] != PX_WHITE && (used[leftIndex] == WAIT_HANDLE || (needReset && used[leftIndex] == HANDLING))) {
                nowX = leftX;
                stepStack.push(MOVE_LEFT);
                continue;
            }

            //当前像素的上边是否还有黑色像素点
            int topY = nowY - 1;
            int topIndex = width * topY + nowX;
            if (topY >= 0 && pixels[topIndex] != PX_WHITE && (used[topIndex] == WAIT_HANDLE || (needReset && used[topIndex] == HANDLING))) {
                nowY = topY;
                stepStack.push(MOVE_TOP);
                continue;
            }


            //当前像素的右边是否还有黑色像素点
            int rightX = nowX + 1;
            int rightIndex = width * nowY + rightX;
            if (rightX < width && pixels[rightIndex] != PX_WHITE && (used[rightIndex] == WAIT_HANDLE || (needReset && used[rightIndex] == HANDLING))) {
                nowX = rightX;
                stepStack.push(MOVE_RIGHT);
                continue;
            }


            //当前像素的下边是否还有黑色像素点
            int bottomY = nowY + 1;
            int bottomIndex = width * bottomY + nowX;
            if (bottomY < height && pixels[bottomIndex] != PX_WHITE && (used[bottomIndex] == WAIT_HANDLE || (needReset && used[bottomIndex] == HANDLING))) {
                nowY = bottomY;
                stepStack.push(MOVE_BOTTOM);
                continue;
            }

            if (stepStack.size() > 0) {
                int step = stepStack.pop();
                switch (step) {
                    case MOVE_LEFT:
                        nowX++;
                        break;
                    case MOVE_RIGHT:
                        nowX--;
                        break;
                    case MOVE_TOP:
                        nowY++;
                        break;
                    case MOVE_BOTTOM:
                        nowY--;
                        break;
                }
            } else {
                break;
            }
        }
        return true;
    }

    /**
     * 二值化
     */
    private static void binarization(int[] pixels, int width, int height) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int col = pixels[width * i + j];
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
                pixels[width * i + j] = Color.argb(alpha, gray, gray, gray);
                if (pixels[width * i + j] != PX_WHITE)
                    pixels[width * i + j] = PX_BLACK;
            }
        }
    }

    /**
     * 获取颜色
     */
    private static int getColor(int gray) {
        int alpha = 0xFF << 24;
        // 分离三原色
        alpha = ((gray & 0xFF000000) >> 24);
        int red = ((gray & 0x00FF0000) >> 16);
        int green = ((gray & 0x0000FF00) >> 8);
        int blue = (gray & 0x000000FF);
        int avgGray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
        if (95 >= avgGray) {
            avgGray = 0;
        } else {
            avgGray = 255;
        }
        return alpha | (avgGray << 16) | (avgGray << 8) | avgGray;
    }


}
