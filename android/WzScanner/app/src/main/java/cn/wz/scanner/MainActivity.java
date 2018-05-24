package cn.wz.scanner;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.wz.scanner.scanlibrary.acitvity.ScanActivity;
import cn.wz.scanner.scanlibrary.pojo.WzScanResult;

public class MainActivity extends AppCompatActivity {

    Button btnOpenView;
    TextView txtScanRsltView;
    ImageView imgView;
    Switch switchFlash;
    private boolean isOpenFlash = false;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        btnOpenView = (Button) findViewById(R.id.btnStart);
        btnOpenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, ScanActivity.class);
                Bundle bundle=new Bundle();
                it.putExtra("isFlashOpen", isOpenFlash);
                startActivityForResult(it, 1000);
            }
        });
        btnOpenView.setText("扫描");
        txtScanRsltView = (TextView) findViewById(R.id.txtScanRslt);
        imgView = (ImageView) findViewById(R.id.imgScanRslt);
        switchFlash = (Switch) findViewById(R.id.switchUseFlashLight);
        switchFlash.setText("打开闪光灯");
        switchFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isOpenFlash = isChecked;
            }
        });
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
                WzScanResult result = (WzScanResult) data.getExtras().get("scanRslt");
                if (null == result) {
                    return;
                }
                txtScanRsltView.setText(result.toString());
                byte[] bts = result.getBitmap();
                Bitmap btmap = BitmapFactory.decodeByteArray(bts, 0, bts.length);
                imgView.setImageBitmap(btmap);
            }
        }
    }
}
