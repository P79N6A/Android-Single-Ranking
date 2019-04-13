package com.example.gpstest;

import android.app.*;
import android.content.DialogInterface;
import android.os.*;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.gpstest.DbDebugEnvironment.DbOperator;

import java.util.ArrayList;

public class DeskService extends Service {

    private static final String TAG = "testapp";
    public static final int NOTICE_ID = 100;
    public static final String NOTICE_CHANNEL_ID = "Channel_1";

    //声明数据库操作器
    private DbOperator operator = null;
    //初始化地点存储容器
    private ArrayList<LatLng> locationList = null;
    //初始化定位
    private AMapLocationClient mLocationClient = null;

    private class mLocationListener implements AMapLocationListener {
        @Override
        public void onLocationChanged(AMapLocation currentLocation) {
            if (currentLocation != null) {
                if (currentLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    StringBuilder locationInfo = new StringBuilder();
                    locationInfo.append("经度: ").append(currentLocation.getLongitude()).append(" 纬度: ").append(currentLocation.getLatitude()).append(" 方向角: ").append(currentLocation.getBearing());
                    Log.d(TAG, "DeskService -> onLocationChanged -> " + locationInfo);
                    //比对本地地点数据
                    LatLng curloc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    ArrayList<LatLng> matchList = matchLocList(curloc);
                    //测试接口
                    setAlert();
                    if (!matchList.isEmpty()) {
                        setAlert();
                    }
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + currentLocation.getErrorCode() + ", errInfo:"
                            + currentLocation.getErrorInfo());
                }
            }
        }
    };

    private void setAlert() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                dialogBuilder.setTitle("[检测到您在附近有待办事务]");
                dialogBuilder.setMessage("待办事务标题:(balabalabalabala...)");
                dialogBuilder.setCancelable(false);
                dialogBuilder.setNegativeButton("退出登录", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0新特性
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1);
                } else {
                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                }
                alertDialog.show();
            }
        });
    }

    private ArrayList<LatLng> matchLocList(LatLng curloc) {
        Log.d(TAG, "DeskService -> matchLocList");
        ArrayList<LatLng> matchlist = new ArrayList<LatLng>();
        if (locationList != null && !locationList.isEmpty()) {
            for (int i = 0; i < locationList.size(); i++) {
                if (AMapUtils.calculateLineDistance(curloc, locationList.get(i)) <= 100) matchlist.add(locationList.get(i));
            }
        }
        return matchlist;
    }

    private void setLocOpt(int purpose, int interval, int timeout) {
        //声明AMapLocationClientOption对象
        AMapLocationClientOption option = new AMapLocationClientOption();
        Log.d(TAG, "DeskService -> setAmapOpt");
        //设置定位场景,默认无场景
        switch (purpose) {
            case 1:
                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
                break;
            case 2:
                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
                break;
            case 3:
                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Sport);
                break;
            default:
                break;
        }
        //设置定位间隔,默认2s
        option.setInterval(interval);
        //设置gps请求超时时间,默认30s
        option.setHttpTimeOut(timeout);
        mLocationClient.setLocationOption(option);
    }

    private void requestLocation() {
        if(mLocationClient != null){
            //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
            mLocationClient.stopLocation();
        }
        Log.d(TAG, "DeskService -> requestLocation");
        mLocationClient.startLocation();
    }

    private void setForeground(String title, String text) {
        Log.d(TAG, "DeskService -> setForeGround(" + title + ", " + text + ")");
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager manager = (NotificationManager) getSystemService (NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel (NOTICE_CHANNEL_ID, "DeskService", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, NOTICE_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource (getResources (),R.mipmap.ic_launcher))
                    .build();
            startForeground (NOTICE_ID, notification);
        } else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 26) {
            Notification.Builder notification = new Notification.Builder(this);
            notification.setSmallIcon(R.mipmap.ic_launcher);
            notification.setContentTitle(title);
            notification.setContentText(text);
            startForeground(NOTICE_ID, notification.build());
        } else {
            startForeground(NOTICE_ID, new Notification());
        }
//      如果觉得常驻通知栏体验不好, 可以通过启动CancelNoticeService将通知移除, oom_adj值不变
        startService(new Intent(this, CancelNoticeService.class));
    }

    private void setLocList() {
        Log.d(TAG, "DeskService -> setLocList");
        if (operator == null) {
            operator = new DbOperator(this);
        }
        locationList = operator.getLocationList();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "DeskService -> onCreate");
        mLocationClient = new AMapLocationClient(this);
        setLocOpt(3 , 1000, 20000);
        requestLocation();
        //设置定位回调监听
        mLocationClient.setLocationListener(new mLocationListener());
        //连接数据库操作器
        operator = new DbOperator(this);
        //用操作器拿本地地点列表
        setForeground("[地点提醒服务已开启]", "不必惊慌, 它只是为了能在合适的地点提醒您");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "DeskService -> onStartCommand");
        //服务的每次启动都要获取本地数据
        setLocList();
        // 如果Service被终止
        // 当资源允许情况下，重启service
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "DeskService -> onDestroy");
        super.onDestroy();
        // 如果Service被杀死，干掉通知
        if (Build.VERSION.SDK_INT >= 18) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) manager.deleteNotificationChannel(DeskService.NOTICE_CHANNEL_ID);
            manager.cancel(NOTICE_ID);
        }
        // 重启自己
        startService(new Intent(getApplicationContext(), DeskService.class));
    }

}
