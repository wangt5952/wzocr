package cn.wz.scanner;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import cn.wz.scanner.scanlibrary.acitvity.ScanActivity;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenView;
    private RecyclerView rsltView;
    private AppAdapter mAppAdapter;
    private Switch switchBatchSacn;
    private Switch switchFlash;
    private boolean isOpenFlash = false;
    private boolean isBatchScan = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // I can control the camera now
                    } else {
                        // Oups permission denied
                    }
                });

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        btnOpenView = (Button) findViewById(R.id.btnStart);
        btnOpenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, ScanActivity.class);
                it.putExtra("isFlashOpen", isOpenFlash);
                it.putExtra("isMulScan", isBatchScan);
                it.putExtra("decodeType", "1010");
                it.putExtra("isAutoScan", true);
                startActivityForResult(it, 1000);
            }
        });
        switchFlash = (Switch) findViewById(R.id.switchUseFlashLight);
        switchFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isOpenFlash = isChecked;
            }
        });
        switchBatchSacn = (Switch) findViewById(R.id.switchBatchScan);
        switchBatchSacn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBatchScan = isChecked;
            }
        });
        rsltView = (RecyclerView) findViewById(R.id.rvScanResults);
        rsltView.addItemDecoration(new TopSpaceItemDecoration(DensityUtil.dpTopx(this, 10)));
        rsltView.setHasFixedSize(true);
        rsltView.setLayoutManager(new LinearLayoutManager(this));

        mAppAdapter = new AppAdapter(null);
        mAppAdapter.openLoadAnimation();
        rsltView.setAdapter(mAppAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1000) {
                ArrayList<WzScanResult> result = (ArrayList<WzScanResult>) data.getExtras().get("scanRslt");
                if (null == result || 0 == result.size()) {
                    return;
                }
                mAppAdapter.setNewData(result);
            }
        }
    }
}
