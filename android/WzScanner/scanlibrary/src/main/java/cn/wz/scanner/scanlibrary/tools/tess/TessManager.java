package cn.wz.scanner.scanlibrary.tools.tess;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.wz.scanner.scanlibrary.R;
import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

/**
 * Tess-two管理器.
 */
public class TessManager {
    /** TAG. */
    static final String TAG = "WZ_" + TessManager.class.getName();

    /** 训练库存放根目录. */
    private static final String DEF_TRAINING_DATA_SAVED_ROOT_PATH = Environment.getExternalStorageDirectory() + File.separator + "wzscaner" + File.separator;

    /** Tess-Data字库文件名. */
    private static String DEF_TRAINING_DATA_FILE_NAME = "eng";

    /** 字库文件扩展名. */
    public static final String TRAINING_DATA_FILE_EXT = ".traineddata";

    /** Tess管理器单例. */
    private static TessManager mTessManager = null;

    /** 上下文. */
    private Context mContext;

    /** 在用的题库文件. */
    private String usedTrainingDataFileName = DEF_TRAINING_DATA_FILE_NAME;

    /** 字库是否已初始化. */
    private boolean isTrainDataInitiated;

    /** 字库文件根目录. */
    private String tessRootFolder;

    /**
     * 构造方法.
     * @param pContext 上下文
     * @param pTrainingDataFileName 字库文件名（不包含扩展名）
     */
    public TessManager(Context pContext, String pTrainingDataFileName) {
        this.mContext = pContext;
        if (WzStringUtil.isNotBlank(pTrainingDataFileName)) {
            this.usedTrainingDataFileName = pTrainingDataFileName;
        }
        this.isTrainDataInitiated = false;
        tessRootFolder = null;
    }

    /**
     * 初始化.
     */
    public synchronized void initTrainingData() {
        if (isTrainDataInitiated) {
            return;
        }
        // 获得APP路径
        File appFolder = mContext.getFilesDir();
        // 创建Tess-Two字库文件存放目录
//        File tessFolder = new File(appFolder, "tess");
//        if(!tessFolder.exists()){
//            tessFolder.mkdir();
//        }
        File tessFolder = new File(DEF_TRAINING_DATA_SAVED_ROOT_PATH, "tess");
        if(!tessFolder.exists()){
            tessFolder.mkdirs();
        }
        this.tessRootFolder = tessFolder.getAbsolutePath();
        Log.d(TAG, "字库根目录：" + tessRootFolder);
        File tessdataFolder = new File(tessFolder, "tessdata");
        if (!tessdataFolder.exists()) {
            tessdataFolder.mkdir();
        }
        Log.d(TAG, "字库文件保存目录：" + tessdataFolder.getAbsolutePath());

        // 创建字库文件
        String dataFileFullName = usedTrainingDataFileName + TRAINING_DATA_FILE_EXT;
        File dataFile = new File(tessdataFolder, dataFileFullName);
        if(!dataFile.exists()) {
            try {
                FileOutputStream fileOutputStream;
//                byte[] bytes = readRawTrainingData(mContext, dataFileFullName);
                byte[] bytes = readRawTrainingData(mContext, usedTrainingDataFileName);
                if (null == bytes || 0 == bytes.length){
                    return;
                }
                fileOutputStream = new FileOutputStream(dataFile);
                fileOutputStream.write(bytes);
                fileOutputStream.close();
                fileOutputStream = null;
                isTrainDataInitiated = true;
                Log.i(TAG, "初始化字库文件已正常完成");
            } catch (IOException e) {
                Log.e(TAG, "初始化字库文件异常", e);
            } catch (Exception e) {
                Log.e(TAG, "初始化字库文件发生未知异常", e);
            }
        } else{
            isTrainDataInitiated = true;
        }
    }

    /**
     * 将Raw中的字库文件读入内存.
     * @param pContext 上下文
     * @param rawTrainingDataFileName Raw中的字库文件
     * @return 字库文件Byte数组
     */
    private byte[] readRawTrainingData(Context pContext, String rawTrainingDataFileName) throws IOException {
//            InputStream fileInputStream = pContext.getResources().getAssets().open(rawTrainingDataFileName);
            InputStream fileInputStream = pContext.getResources().openRawResource(R.raw.eng_traineddata);
            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int bytesRead;
            while (-1 != (bytesRead = fileInputStream.read(b))){
                buff.write(b, 0, bytesRead);
            }
            fileInputStream.close();
            fileInputStream = null;
            return buff.toByteArray();
    }

    /**
     * 获得Tess-Two根目录.
     * @return 目录路径
     */
    public String getTessRootFolder() {
        return tessRootFolder;
    }

    /**
     * 检查字库是否正确存放.
     * @return true：正常；其他：异常
     */
    public boolean checkTrainingData() {
        if (WzStringUtil.isBlank(tessRootFolder)) {
            throw new RuntimeException("字库没有正常初始化");
        }
        File file = new File(tessRootFolder + "/tessdata/");
        if (!file.exists()) {
            throw new RuntimeException("未发现正确的字库目录: \"" + tessRootFolder + "/tessdata/\"");
        }
        String dataFile = tessRootFolder + "/tessdata/" + usedTrainingDataFileName + TRAINING_DATA_FILE_EXT;
        file = new File(dataFile);
        if (!file.exists()) {
            throw new RuntimeException("未发现正确的字库文件: \"" + dataFile + "\"");
        }
        return true;
    }

    /**
     * 解码手机号码.
     * @param pBmp 图像
     */
    public String detectMobileNo(final Bitmap pBmp) {
        try {
            TessBaseAPI tessBaseAPI;
            if (checkTrainingData()) {
                tessBaseAPI = new TessBaseAPI();
//                tessBaseAPI.setDebug(true);
                tessBaseAPI.setDebug(false);
                Log.d(TAG,"字库：" + usedTrainingDataFileName);
                tessBaseAPI.init(getTessRootFolder(), usedTrainingDataFileName, TessBaseAPI.OEM_TESSERACT_ONLY);
                // 白名单
//                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "abcdefghijklmnopqrstuvwxyz0123456789");
//                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789");
                // 黑名单
                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?");
//                tessBaseAPI.setVariable(TessBaseAPI.VAR_SAVE_BLOB_CHOICES, TessBaseAPI.VAR_TRUE);
//                tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
                tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                tessBaseAPI.setImage(pBmp);
//                String ocrText = tessBaseAPI.getUTF8Text();
                String ocrText = tessBaseAPI.getHOCRText(0);
//                tessBaseAPI.clear();
                tessBaseAPI.end();
                System.gc();
                if (WzStringUtil.isBlank(ocrText)) {
                    Log.e(TAG, "OCR解码结果为空");
                    return null;
                } else {
                    Log.i(TAG, "OCR解码成功");
                    Log.d(TAG, "MobileNo字符串：" + ocrText);
                    List<String> ml = WzStringUtil.getMobileNoList(ocrText);
                    if (null == ml || 0 == ml.size()) {
                        Log.e(TAG, "OCR解码未发现手机号码");
                        return null;
                    }
                    return ml.get(0);
//                    return ocrText;
                }
            } else {
                Log.e(TAG, "字库路径异常");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "OCR解码失败", e);
            return null;
        }
    }

    /**
     * 获得Tess-Two管理器实例.
     * @param pContext 上下文
     * @param pTrainingDataFileName 字库文件名（不包含扩展名，默认eng）
     * @return Tess-Two管理器实例
     */
    public static TessManager getInstances(Context pContext, String pTrainingDataFileName) {
        if (null == mTessManager) {
            synchronized (TessManager.class) {
                if (null == mTessManager) {
                    mTessManager = new TessManager(pContext, pTrainingDataFileName);
//                    mTessManager.initTrainingData();
                }
            }
        }
        return mTessManager;
    }
}
