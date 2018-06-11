package cn.wz.scanner.scanlibrary.tools.youtu;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.utils.WzExpressAnalysisUtil;

/**
 * Youto管理器.
 */
public class YoutuManager {
    /** TAG. */
    static final String TAG = "WZ_" + YoutuManager.class.getName();

    private String appId;
    private String secId;
    private String secKey;

    /**
     * 构造方法.
     */
    public YoutuManager(String appid, String secret_id, String secret_key) {
        this.appId = appid;
        this.secId = secret_id;
        this.secKey = secret_key;
    }

    /**
     * 解码手机号码.
     * @param pBmp 图像
     */
    public WzScanResult detectMobileNo(final Bitmap pBmp) {
        try {
            Youtu yt = new Youtu(appId, secId, secKey, Youtu.API_YOUTU_END_POINT);
            JSONObject ocrText = yt.MailOcr(pBmp);
            if (null == ocrText) {
                return null;
            } else if (0 == ocrText.getInt("errorcode")) {
                Log.i(TAG, ocrText.toString());
                JSONArray ja = ocrText.getJSONArray("items");
                String[] retArr = WzExpressAnalysisUtil.getMailInfoByYoutu(ja);
                if (null != retArr) {
                    WzScanResult msg = new WzScanResult();
                    msg.setRecipientMobile(retArr[0]);
                    msg.setRecipientName(retArr[1]);
                    msg.setRecipientAddr(retArr[2]);
                    return msg;
                } else {
                    return null;
                }
//                if (null == ja || 0 == ja.length()) {
//                    return null;
//                } else {
//                    StringBuffer sb = new StringBuffer();
//                    for (int i = 0; i < ja.length(); i++) {
//                        sb.append(ja.getJSONObject(i).getString("itemstring")).append("\n");
//                    }
//                    Log.i(TAG, sb.toString());
//                    for (int i = 0; i < ja.length(); i++) {
//                        if (WzStringUtil.isNotBlank(WzStringUtil.getFirstMobileNo(ja.getJSONObject(i).getString("itemstring")))) {
//                            WzScanResult msg = new WzScanResult();
//                            if (0 < i) {
//                                msg.setRecipientName(ja.getJSONObject(i - 1).getString("itemstring"));
//                            }
//                            msg.setRecipientMobile(WzStringUtil.getFirstMobileNo(ja.getJSONObject(i).getString("itemstring")));
//                            if (i + 1 <ja.length()) {
//                                msg.setRecipientAddr(ja.getJSONObject(i + 1).getString("itemstring"));
//                            }
//                            if (i + 2 <ja.length()) {
//                                msg.setRecipientAddr(msg.getRecipientAddr() + ja.getJSONObject(i + 2).getString("itemstring"));
//                            }
//                            return msg;
//                        }
//                    }
//                    return null;
//                }
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "OCR解码失败", e);
            return null;
        }
    }
}
