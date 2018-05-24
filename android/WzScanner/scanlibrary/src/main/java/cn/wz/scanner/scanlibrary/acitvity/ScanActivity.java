package cn.wz.scanner.scanlibrary.acitvity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import cn.wz.scanner.scanlibrary.R;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;
import cn.wz.scanner.scanlibrary.view.WzScannerView;

public class ScanActivity extends Activity  {
    /** TAG. */
    static final String TAG = "WZ_" + ScanActivity.class.getName();

    /** 扫描View. */
    private WzScannerView mScanView;
    /** 打开闪光灯按钮. */
    private Switch mSwitchOpenFlashLight;
    private Button mBtnTakePicture;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mScanView = (WzScannerView) findViewById(R.id.scanView);
        mScanView.setScanActivity(this);
        mSwitchOpenFlashLight = (Switch) findViewById(R.id.switchOpenFlashLight);
        mSwitchOpenFlashLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mScanView.setFlashLight(isChecked);
            }
        });
        boolean defFlashIsOpen = getIntent().getBooleanExtra("isFlashOpen", false);
        mSwitchOpenFlashLight.setChecked(defFlashIsOpen);
        mScanView.setDefFlashLightOpen(defFlashIsOpen);
//        mBtnTakePicture = findViewById(R.id.btnTakePicture);
//        mBtnTakePicture.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Camera tmpCamera = mScanView.getCamera();
//                if (null == tmpCamera) {
//                    Log.e(TAG, "摄像头没有启用");
//                    return ;
//                }
//                buildProgressDialog();
//                mScanView.doTakePic();
//            }
//        });
    }

    /**
     * Handler scan result
     * @param result
     */
    public void handleDecode(WzScanResult result) {
        Intent intent = getIntent();
        intent.putExtra("scanRslt", result);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void buildProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage("识别中...");
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

    public void cancelProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}
