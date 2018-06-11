package cn.wz.scanner.scanlibrary.pojo;

import cn.wz.scanner.scanlibrary.utils.WzStringUtil;

public class WzDecodeType {
    /** 解码字符串. */
    private String decodeTypeStr = "1100";

    /** 是否解析一维条码 默认true. */
    private boolean isDecodeOneCode = true;
    /** 是否解析手机号码 默认true. */
    private boolean isDecodePhoneNo = true;
    /** 是否解析快件信息（通过腾讯优图，解析内容包含收件方姓名、手机号码、地址） 默认false. */
    private boolean isDecodeExpressInfo = false;
    /** 是否解析QR 默认false. */
    private boolean isDecodeQR = false;

    /**
     * 构造方法.
     * @param dts 解码字符串
     * <pre>
     * dts 为4位数字，0表示不需解析，1表示需要解析
     * 4位数字分别代表 一维条码、手机号码、快件信息、QR码
     * 其中手机号码和快件信息互斥，两者只能有一种存在，如果快件信息存在则手机号码自动为0
     * 默认仅需要解析一维条码和手机号码，即1100
     * 超过4位的取前4位，少于4位的字符串后用0补足4位，非0或1均作为0
     * </pre>
     */
    public WzDecodeType(String dts) {
        // 如果传入内容为空，则默认1100
        if (WzStringUtil.isBlank(dts)) {
            this.decodeTypeStr = "1100";
            this.isDecodeOneCode = true;
            this.isDecodePhoneNo = true;
            this.isDecodeExpressInfo = false;
            this.isDecodeQR = false;
        } else {
            this.decodeTypeStr = dts;
            String ch = null;
            boolean[] tsArr = new boolean[4];
            // 循环每位字符
            for (int i = 0; i < 4; i++) {
                // 传入参数不足四位的，对应位设置为0
                if (i >= dts.length()) {
                    tsArr[i] = false;
                } else {
                    ch = dts.substring(i, i + 1);
                    // 只有传入值为1才作为1处理
                    if ("1".equals(ch)) {
                        tsArr[i] = true;
                        // 如果第3位快件信息为1，则第2位手机号码自动改为0，不管原来是多少
                        if (2 == i) {
                            tsArr[i - 1] = false;
                        }
                    } else {
                        tsArr[i] = false;
                    }
                }
            }
            this.isDecodeOneCode = tsArr[0];
            this.isDecodePhoneNo = tsArr[1];
            this.isDecodeExpressInfo = tsArr[2];
            this.isDecodeQR = tsArr[3];
        }
    }

    /*
     * Getter方法.
     */
    public boolean isDecodeOneCode() {
        return isDecodeOneCode;
    }

    public boolean isDecodePhoneNo() {
        return isDecodePhoneNo;
    }

    public boolean isDecodeExpressInfo() {
        return isDecodeExpressInfo;
    }

    public boolean isDecodeQR() {
        return isDecodeQR;
    }
}
