package cn.wz.scanner.scanlibrary.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.wz.scanner.scanlibrary.R;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;

public class ScanLibAdapter extends BaseQuickAdapter<WzScanResult, BaseViewHolder> {

    public ScanLibAdapter(@Nullable List<WzScanResult> data) {
        super(R.layout.scanlib_result_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, WzScanResult item) {
        if (null == item) {
            return;
        }
        helper.setText(R.id.et_mail_no, item.getMailNo())
                .setText(R.id.et_phone, item.getRecipientMobile())
                .setText(R.id.et_name, item.getRecipientName())
                .addOnClickListener(R.id.btn_del);
    }
}
