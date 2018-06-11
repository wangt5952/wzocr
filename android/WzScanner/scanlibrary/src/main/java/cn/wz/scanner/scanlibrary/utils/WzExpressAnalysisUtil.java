package cn.wz.scanner.scanlibrary.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WzExpressAnalysisUtil extends StringUtils {

    /** 手机号码正则（前三位判断）. */
    public static String REGEX_MOBILE_3 = "^(1|861)(30|31|32|33|34|35|36|37|38|39|45|47|49|50|51|52|53|55|56|57|58|59|66|70|71|72|73|75|76|77|78|80|81|82|83|84|85|86|87|88|89|98|99)\\d{8}$";
    /** 手机号码正则（前两位判断）. */
    public static String REGEX_MOBILE_2 = "(1)(3|4|5|6|7|8|9)\\d{9}$*";
    /** 手机号码正则（去除数字符号及空白）. */
    public static String REGEX_WITHOUT_SYMBOL_BLANK_NUMBER = "[\\s|!|@|#|$|%|^|&|*|(|)|_|+|=|\\-|\\[|\\]|}|{|;|:|\'|\\|\"|\\||~|`|,|.|/|<|>|?|1|2|3|4|5|6|7|8|9|0]+";
    /** 手机号码正则（去除数字符号及空白）. */
    public static String REGEX_WITHOUT_SYMBOL_BLANK = "[\\s|!|@|#|$|%|^|&|*|(|)|_|+|=|\\-|\\[|\\]|}|{|;|:|\'|\\|\"|\\||~|`|,|.|/|<|>|?]+";

    /**
     * 解析快递信息.
     * @param jsonArray 解析数据
     * @return [0-手机号码,1-姓名,2-地址]
     * @throws Exception 异常
     */
    public static String[] getMailInfoByYoutu(JSONArray jsonArray) throws Exception {
        if (null == jsonArray || 0 == jsonArray.length()) {
            return null;
        } else {
            String[] ret = new String[3];
            String mobStr = null;
            String tempStr = null;
            StringBuffer sbAddr = new StringBuffer();
            // 找到手机号码
            for (int i = 0; i < jsonArray.length(); i++) {
                mobStr = getFirstMobileNo(jsonArray.getJSONObject(i).getString("itemstring"));
                if (isBlank(mobStr)) {
                    continue;
                } else {
                    // 获得了手机号码
                    ret[0] = mobStr;
                    // 获得姓名
                    // 检查本行内是否有姓名
                    tempStr = doReplace(jsonArray.getJSONObject(i).getString("itemstring"), REGEX_WITHOUT_SYMBOL_BLANK_NUMBER, "");
                    tempStr = doReplace(tempStr, "(.)*电话", "");
                    tempStr = doReplace(tempStr, "(.)*收件人", "");
                    // 本行没有姓名时，查看上一行
                    if (isBlank(tempStr)) {
                        tempStr = doReplace(jsonArray.getJSONObject(i - 1).getString("itemstring"), REGEX_WITHOUT_SYMBOL_BLANK_NUMBER, "");
                        tempStr = doReplace(tempStr, "(.)*收件人", "");
                        if (isBlank(tempStr)) {
                            ret[1] = "未知姓名";
                        } else {
                            ret[1] = tempStr;
                        }
                    } else {
                        ret[1] = tempStr;
                    }
                    // 获得地址
                    for (int j = i + 1; j < jsonArray.length(); j++) {
                        tempStr = doReplace(jsonArray.getJSONObject(j).getString("itemstring"), REGEX_WITHOUT_SYMBOL_BLANK, "");
                        if (isBlank(tempStr) || 1 >= tempStr.length()) {
                            continue;
                        }
                        if (-1 < tempStr.toUpperCase().indexOf("收件ID") ) {
                            continue;
                        }
                        tempStr = doReplace(tempStr, "(.)*地址", "");
                        sbAddr.append(tempStr);
                    }
                    ret[2] = sbAddr.toString();
                    // 解析完成退出循环
                    break;
                }
            }
            return ret;
        }
    }

    /**
     * 获得第一个手机号码.
     * @param str 元字符串
     * @return 获取的手机号码，null表示没有获取到
     */
    public static String getFirstMobileNo(String str) {
        if (isBlank(str)) {
            return null;
        }
        String tmpStr = doReplace(str, REGEX_WITHOUT_SYMBOL_BLANK, "");
        Pattern mobilePattern = Pattern.compile(REGEX_MOBILE_2);
        Matcher matcher = mobilePattern.matcher(tmpStr);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 通过正则表达式过滤字符串.
     * @param str 源字符串
     * @param regexStr 正则
     * @param replaceStr 替换的字符串
     * @return 替换后的字符串
     */
    public static String doReplace(String str, String regexStr, String replaceStr) {
        if (isBlank(str)) {
            return null;
        }
        String tmpStr = str.trim();
        tmpStr = tmpStr.replaceAll(regexStr, replaceStr);
        return tmpStr;
    }
}
