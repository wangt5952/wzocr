package cn.wz.scanner.scanlibrary.acitvity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;

import java.util.ArrayList;
import java.util.List;

import cn.wz.scanner.scanlibrary.R;
import cn.wz.scanner.scanlibrary.adapter.ScanLibAdapter;
import cn.wz.scanner.scanlibrary.pojo.WzDecodeType;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.utils.DensityUtil;
import cn.wz.scanner.scanlibrary.view.WzScannerView;
import cn.wz.scanner.scanlibrary.view.WzSpaceItemDecoration;

/**
 * 扫描页面.
 */
public class ScanActivity extends Activity  {
    /** TAG. */
    static final String TAG = "WZ_" + ScanActivity.class.getName();

    /** 扫描View. */
    private WzScannerView mScanView;
    /** 打开闪光灯按钮. */
    private Switch mSwitchOpenFlashLight;
    /** 识别按钮. */
    private Button mBtnDecoding;
    /** 结束识别并返回按钮. */
    private FloatingActionButton mBtnOverAndBack;
    /** 识别中进度条. */
    private ProgressDialog progressDialog;
    /** 识别结果列表. */
    private RecyclerView mRsltLsView;
    /** 识别结果Adapter. */
    private ScanLibAdapter mScanLibAdapter;
    /** 开始时间. */
    private long stTime = 0l;
    /** 结束时间. */
    private long edTime = 0l;
    /** 是否默认打开闪光灯. */
    private boolean defFlashIsOpen = false;
    /** 是否默认批量扫描. */
    private boolean defMultipleScan = false;
    /** 是否手动扫描. */
    private boolean defAutoScan = false;
    /** 默认扫描类别. */
    private WzDecodeType defDecodeType = new WzDecodeType("1100");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        // 接收参数
        // 是否默认打开闪光灯
        this.defFlashIsOpen = getIntent().getBooleanExtra("isFlashOpen", false);
        // 是否批量扫描
        this.defMultipleScan = getIntent().getBooleanExtra("isMulScan", false);
        // 扫描类别
        this.defDecodeType = new WzDecodeType(getIntent().getStringExtra("decodeType"));
        // 是否自动扫描
        this.defAutoScan = getIntent().getBooleanExtra("isAutoScan", false);

        // 设置扫描View
        mScanView = (WzScannerView) findViewById(R.id.scanView);
        mScanView.setScanActivity(this);

        // 设置闪光灯按钮
        mSwitchOpenFlashLight = (Switch) findViewById(R.id.switchOpenFlashLight);
        mSwitchOpenFlashLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mScanView.setFlashLight(isChecked);
            }
        });
        mSwitchOpenFlashLight.setChecked(defFlashIsOpen);

        // 设置完成扫描及返回按钮
        mBtnOverAndBack = (FloatingActionButton) findViewById(R.id.btnOverAndBack);
        mBtnOverAndBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScanView.decodeOverAndBack();
            }
        });

        // 设置识别按钮
        mBtnDecoding = (Button) findViewById(R.id.btnDecoding);
        mBtnDecoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildProgressDialog();
                mScanView.decodeBySelf();
            }
        });
        // 只有不涉及网络识别的情况下才允许自动扫描（用腾讯优图识别快件信息时是网络识别）
        if (defAutoScan && !defDecodeType.isDecodeExpressInfo()) {
            mBtnDecoding.setVisibility(View.INVISIBLE);
        } else {
            mBtnDecoding.setVisibility(View.VISIBLE);
        }

        // 设置结果View
        mRsltLsView = (RecyclerView) findViewById(R.id.lsScanRsltLs);
        mRsltLsView.addItemDecoration(new WzSpaceItemDecoration(DensityUtil.dp2px(this, 10)));
        mRsltLsView.setHasFixedSize(true);
        mRsltLsView.setLayoutManager(new LinearLayoutManager(this));
        mScanLibAdapter = new ScanLibAdapter(null);
        mScanLibAdapter.openLoadAnimation();
        mRsltLsView.setAdapter(mScanLibAdapter);
        mRsltLsView.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                adapter.remove(position);
            }
        });
        Log.i(TAG, "============ 扫描并解析开始 ===============");
        stTime = System.currentTimeMillis();
    }

    /**
     * 获得是否默认打开闪光灯.
     * @return 标志
     */
    public boolean getDefFlashIsOpen() {
        return defFlashIsOpen;
    }

    /**
     * 获得是否默认批量扫描.
     * @return 标志
     */
    public boolean getDefMultipleScan() {
        return defMultipleScan;
    }

    /**
     * 获得解码类别.
     * @return 解码类别
     */
    public WzDecodeType getDefDecodeType() {
        return defDecodeType;
    }

    /**
     * 判断是否可以自动扫描.
     * @return 标志
     */
    public boolean isAutoScan() {
        // 只有不涉及网络识别的情况下才允许自动扫描（用腾讯优图识别快件信息时是网络识别）
        if (defAutoScan && !defDecodeType.isDecodeExpressInfo()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 刷新结果列表.
     * @param data 结果列表数据
     */
    public void refreshRsltLs(List<WzScanResult> data) {
        mScanLibAdapter.setNewData(data);
    }

    /**
     * 完成解码并返回结果.
     * @param results 识别结果
     */
    public void handleDecode(ArrayList<WzScanResult> results) {
        edTime = System.currentTimeMillis();
        Log.i(TAG, "============ 扫描并解析结束 ===============");
        Log.i(TAG, "总处理过程：" + (edTime - stTime) + " ms");
        Intent intent = getIntent();
        intent.putExtra("scanRslt", results);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 显示识别过程进度条.
     */
    public void buildProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage("识别中...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    /**
     * 隐藏识别过程进度条.
     */
    public void cancelProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}
