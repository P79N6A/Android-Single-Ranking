package com.example.gpsinactivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView positionText;
    private List<String> permissionList = new ArrayList<>();
    //初始化定位
    private AMapLocationClient mLocationClient = null;
    private String locationInfo;
    public class mLocationListner implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation currentLocation) {
            if (currentLocation != null) {
                if (currentLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    locationInfo = "经度: " + currentLocation.getLongitude() + "纬度: " + currentLocation.getLatitude();
                    positionText.setText(locationInfo);
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + currentLocation.getErrorCode() + ", errInfo:"
                            + currentLocation.getErrorInfo());
                }
            }
        }
    }

    private void requestLocation() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setInterval(1000);
        mLocationClient.setLocationOption(option);
        mLocationClient.startLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationClient = new AMapLocationClient(getApplicationContext());
        Log.d("getAppContext", getApplicationContext().toString());
        Log.d("getAppContext", MainActivity.this.toString());
        mLocationClient.setLocationListener(new mLocationListner());
        positionText = (TextView) findViewById(R.id.position_text_view);
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        Log.d(TAG, "MainActivity -> onRequestPermissionsResult");
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
//                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default :
                break;
        }
    }

    private void checkPermissions() {
//        Log.d(TAG, "MainActivity -> checkPermissions");
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }
}
