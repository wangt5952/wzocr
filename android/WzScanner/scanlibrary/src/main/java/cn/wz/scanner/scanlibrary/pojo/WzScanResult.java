package cn.wz.scanner.scanlibrary.pojo;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * 扫描结果.
 */
public class WzScanResult implements Serializable {

    /** 是否运单号解析成功. */
    private boolean isMailNoSuccess = false;
    /** 是否收件人手机号码解析成功. */
    private boolean isRecipientMobileSuccess = false;
    /** 运单号. */
    private String mailNo;
    /** 收件人手机号码. */
    private String recipientMobile;

    private byte[] bitmap;

    /*
     * Getter And Setter 方法.
     */
    public boolean isMailNoSuccess() {
        return isMailNoSuccess;
    }

    public void setMailNoSuccess(boolean mailNoSuccess) {
        isMailNoSuccess = mailNoSuccess;
    }

    public boolean isRecipientMobileSuccess() {
        return isRecipientMobileSuccess;
    }

    public void setRecipientMobileSuccess(boolean recipientMobileSuccess) {
        isRecipientMobileSuccess = recipientMobileSuccess;
    }

    public String getMailNo() {
        return mailNo;
    }

    public void setMailNo(String mailNo) {
        this.mailNo = mailNo;
    }

    public String getRecipientMobile() {
        return recipientMobile;
    }

    public void setRecipientMobile(String recipientMobile) {
        this.recipientMobile = recipientMobile;
    }

    public byte[] getBitmap() {
        return bitmap;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "运单号：" + mailNo + System.lineSeparator() + "收件人手机号码：" + recipientMobile;
    }
}
