package com.adan.gpsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TestGPS2 extends AppCompatActivity {
    int usersa=0;   //可用卫星总数
    int beidu=0;    //北斗
    int gps=0;      //美国gps
    int glo=0;      //欧洲伽利略
    int other=0;    //其他
    float zaimuth=0;//方位角
    float elevation=0;//海拔
    float snr=0;//信噪比
    boolean almanc=true;//年历
    boolean ephemris=true;//星历
    float cf=0;
    private static final int MSG_GPS = 11;
    private static final int MSG_SATELLITE = 12;
    public static final int CONSTELLATION_UNKNOWN = 0;
    /** Constellation type constant for GPS. */
    public static final int CONSTELLATION_GPS = 1;
    /** Constellation type constant for SBAS. */
    public static final int CONSTELLATION_SBAS = 2;
    /** Constellation type constant for Glonass. */
    public static final int CONSTELLATION_GLONASS = 3;
    /** Constellation type constant for QZSS. */
    public static final int CONSTELLATION_QZSS = 4;
    /** Constellation type constant for Beidou. */
    public static final int CONSTELLATION_BEIDOU = 5;
    /** Constellation type constant for Galileo. */
    public static final int CONSTELLATION_GALILEO = 6;
    /** Constellation type constant for IRNSS. */
    public static final int CONSTELLATION_IRNSS = 7;
    private Handler handler;
    private String TAG = "TestGPS2:xwg";
//    private Button btnShow;
    private TextView tvGPS,tvSatellite;
    /*************/
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    StringBuffer sb1,sb2;
    LocationManager lm;
    String bestProvider;
    Geocoder geocoder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvSatellite=(TextView) findViewById(R.id.tv_satellite); //卫星参数
        tvGPS =(TextView) findViewById(R.id.tv_gps);    //经纬度
        tvGPS.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvSatellite.setMovementMethod(ScrollingMovementMethod.getInstance());
        handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_SATELLITE:
                        tvSatellite.setText(msg.obj.toString());
                        break;
                    case MSG_GPS:
                        tvGPS.setText(msg.obj.toString());
                        break;
                }
            }
        };
        processPermission();
    }
//    进程权限
    private void processPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int hasPermission=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasPermission!=PackageManager.PERMISSION_GRANTED){
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION
                );
            }else {init();}
        }else {init();}
    }

//    获取权限
    public void onRequestPermissionResult(
            int requestCode,
            String[] permissions,
            int[] grentResult
    ){
        if(requestCode==REQUEST_LOCATION_PERMISSION){
            if(grentResult[0]== PackageManager.PERMISSION_GRANTED){init();}
                else{
                    Log.i(TAG,"can't get gps premission!");
                    finish();
            }
        }else{super.onRequestPermissionsResult(requestCode,permissions,grentResult);}
    }
//    gps init
    @SuppressLint("MissingPermission")
    private void init(){
        lm=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestGPS();
        Criteria criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider=lm.getBestProvider(criteria,true);//选择精度最高提供者
        geocoder=new Geocoder(this, Locale.getDefault());
        lm.requestLocationUpdates(bestProvider,1000,1,locationListener);
        lm.registerGnssStatusCallback(gpsCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(lm!=null)requestGPS();
    }

    @Override
    protected void onDestroy() {
        lm.unregisterGnssStatusCallback(gpsCallback);
        lm.removeUpdates(locationListener);
        super.onDestroy();
    }
//    open gps
    private void requestGPS(){
        if(!(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){
            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
            dialog.setTitle("open GPS")
                    .setMessage("GPS not open,whether open?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
        }
    }//end requestGPS
//    location listener
    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            try {
                double lat=location.getLatitude();
                double lng=location.getLongitude();
                List<Address>listAddr=geocoder.getFromLocation(lat,lng,1);
                String add=String.format("纬度%f：经度%f\n地址：%s",
                        lat,lng,listAddr.get(0).getAddressLine(0));
                Message msg1=new Message();
                msg1.what=MSG_GPS;
                msg1.obj=add;
                handler.sendMessage(msg1);
            }catch (IOException e){
                e.printStackTrace();
                Log.i(TAG,"locationListener error:"+e.toString());
            }
        }
    };//LocationListener end
//    gnssstatus回调
    GnssStatus.Callback gpsCallback=new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
//            Log.i(TAG,"2&2:"+(2&2));
//            Log.i(TAG,"0&2:"+(0&2));
//            Log.i(TAG,"512&2:"+(512&2));
//            Log.i(TAG,"512&128:"+(512&128));
//            int[] b = {2,4,6,12,14,19,20,31,9,17}; //卫星号10个
//            int[]b2=new int[10];
//            float[] c = {32.0f,12.0f,38.0f,30.0f,19.0f,21.0f,31.0f,0.0f,0.0f,0.0f};  //信噪比
//            float[] d = {33.0f,20.0f,55.0f,19.0f,14.0f,19.0f,29.0f,1.0f,33.0f,53.0f}; //卫星高度
//            float[] e = {295.0f,64.0f,340.0f,310.0f,177.0f,67.0f,230.0f,40.0f,104.0f,112.0f}; // 方位角
//            float[] f = {1.57542003E9f,1.57542003E9f,1.57542003E9f,1.57542003E9f,1.57542003E9f,
//                    1.60031245E9f,1.60031245E9f,1.60031245E9f,1.60031245E9f,1.60031245E9f};
//            for(int i=0;i<b.length;i++){
//                b2[i]=(b[i]<<8);
//            }
//            int svused=0;   //可用卫星
//            for(int i=0;i<10;i++){
//                if((b2[i]&(1<<2))!=0){svused++;}
//            }
//            Log.i(TAG,"svused:"+svused);
//            Log.i(TAG,"b:"+ Arrays.toString(b2));
            /********************/
            super.onSatelliteStatusChanged(status);
            int count=status.getSatelliteCount();
            String str="卫星总数："+count+"\n";
            for (int i=0;i<count;i++){
                if(status.usedInFix(i)){
                    usersa++;
                    str+="\n"+"信号强度:"+status.getCn0DbHz(i)+",id："+status.getSvid(i);
                }
                switch (status.getConstellationType(i)){
                    case GnssStatus.CONSTELLATION_BEIDOU:beidu++;break;
                    case GnssStatus.CONSTELLATION_GPS:gps++;break;
                    case GnssStatus.CONSTELLATION_GLONASS:glo++;break;
                    default:other++;
                }
                 zaimuth=status.getAzimuthDegrees(i);//方位角
                elevation=status.getElevationDegrees(i);//海拔
                 snr=status.getCn0DbHz(i);//信噪比
                almanc=status.hasAlmanacData(i);//年历
               ephemris=status.hasEphemerisData(i);//星历
                cf=status.getCarrierFrequencyHz(i); //载波频率
                str+= String.format("方位角:%f, 海拔:%f, 信噪比:%f, 年曆:%s, 星曆:%s,载波频率：%f\n",
                    zaimuth, elevation, snr, almanc, ephemris,cf);
            }
            str+="\n可用卫星数："+usersa;
            str+="\nbeidou："+beidu;
            str+="\ngps："+gps;
            str+="\nglo："+glo;
            str+="\nother："+other;
            Log.i(TAG,"str------->"+str);
            Message msg=new Message();
            msg.what=MSG_SATELLITE;
            msg.obj=str;
            handler.sendMessage(msg);
        }
    };
}