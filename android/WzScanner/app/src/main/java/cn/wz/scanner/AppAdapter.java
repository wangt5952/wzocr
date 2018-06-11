package cn.wz.scanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.wz.scanner.scanlibrary.pojo.WzScanResult;

public class AppAdapter extends BaseQuickAdapter<WzScanResult, BaseViewHolder> {
    public AppAdapter(@Nullable List<WzScanResult> data) {
        super(R.layout.scan_result_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, WzScanResult item) {
        if (null == item) {
            return;
        }
        if (null == item.getBitmap() || 0 == item.getBitmap().length) {
            helper.setText(R.id.et_mail_no, "运单号：" + item.getMailNo())
                    .setText(R.id.et_phone, "手机号：" + item.getRecipientMobile())
                    .setText(R.id.et_name, "姓名：" + item.getRecipientName())
                    .setText(R.id.et_addr, "地址：" + item.getRecipientAddr());
        } else {
            Bitmap btmap = BitmapFactory.decodeByteArray(item.getBitmap(), 0, item.getBitmap().length);
            helper.setText(R.id.et_mail_no, "运单号：" + item.getMailNo())
                    .setText(R.id.et_phone, "手机号：" + item.getRecipientMobile())
                    .setText(R.id.et_name, "姓名：" + item.getRecipientName())
                    .setText(R.id.et_addr, "地址：" + item.getRecipientAddr())
                    .setImageBitmap(R.id.iv_scanimg, btmap);
        }
    }
}
