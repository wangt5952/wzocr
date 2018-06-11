package cn.wz.scanner.scanlibrary.utils;

import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WzStringUtil extends StringUtils {

    /** 手机号码正则（前三位判断）. */
    private static String mobileRegex3 = "^(1|861)(30|31|32|33|34|35|36|37|38|39|45|47|49|50|51|52|53|55|56|57|58|59|66|70|71|72|73|75|76|77|78|80|81|82|83|84|85|86|87|88|89|98|99)\\d{8}$";
    /** 手机号码正则（前两位判断）. */
    private static String mobileRegex2 = "(1)(3|4|5|6|7|8|9)\\d{9}$*";

    /**
     * 字符串整合到一行.
     * @param str 源字符串
     * @return 整合到一行后的字符串
     */
    public static String str2OneLine(String str) {
        String tmpStr = str.trim();
        // !@#$%^&*()_+=-[]}{;:'"\|~`,./<>?
        tmpStr = tmpStr.replaceAll("[-|\\s]+", "");
        return tmpStr;
    }

    /**
     * 获取手机号码（识别到的第一个）.
     * @param str 识别字符串
     * @return 第一个手机号码
     */
    public static String getFirstMobileNo(String str) {
        if (isBlank(str)) {
            return null;
        }
        String tmpStr = str2OneLine(str);
        Pattern mobilePattern = Pattern.compile(mobileRegex2);
        Matcher matcher = mobilePattern.matcher(tmpStr);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 获取手机号码列表.
     * @param str 识别字符串
     * @return 手机号码列表
     */
    public static List<String> getMobileNoList(String str) {
        if (isBlank(str)) {
            return null;
        }
        List<String> mobileLs = new ArrayList<String>();
        Pattern mobilePattern = Pattern.compile(mobileRegex2);
        Matcher matcher = mobilePattern.matcher(str);
        while (matcher.find()) {
            mobileLs.add(matcher.group());
        }
        if (0 < mobileLs.size()) {
            return mobileLs;
        }
        return null;
    }

    /**
     * 获取手机号码列表.
     * @param str 识别字符串
     * @return 手机号码列表
     */
    public static List<String> getMobileNoLs(String str) {
        List<String> mobileLs = new ArrayList<String>();
        Pattern mobilePattern = Pattern.compile(mobileRegex2);
        Matcher matcher = mobilePattern.matcher(str);
        while (matcher.find()) {
            mobileLs.add(matcher.group());
        }
        if (0 < mobileLs.size()) {
            return mobileLs;
        }
        return null;
    }

    /**
     * 获取快件收件信息.
     * @param jsonArray 识别字符串列表
     * @return 快件收件信息[0-手机号码,1-姓名,2-地址]
     */
    public static String[] getMailInfoByYoutu(JSONArray jsonArray) throws Exception {
//        if (null == jsonArray || 0 == jsonArray.length()) {
//            return null;
//        } else {
//            String[] ret = new String[3];
//            for (int i = 0; i < jsonArray.length(); i++) {
//                if (isNotBlank(getFirstMobileNo(jsonArray.getJSONObject(i).getString("itemstring")))) {
//                    if (0 < i) {
//                        msg.setRecipientName(ja.getJSONObject(i - 1).getString("itemstring"));
//                    }
//                    msg.setRecipientMobile(WzStringUtil.getFirstMobileNo(ja.getJSONObject(i).getString("itemstring")));
//                    if (i + 1 <ja.length()) {
//                        msg.setRecipientAddr(ja.getJSONObject(i + 1).getString("itemstring"));
//                    }
//                    if (i + 2 <ja.length()) {
//                        msg.setRecipientAddr(msg.getRecipientAddr() + ja.getJSONObject(i + 2).getString("itemstring"));
//                    }
//                    return msg;
//                }
//            }
//            return null;
//        }
        return null;
    }
}
