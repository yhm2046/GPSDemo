package com.adan.gpsdemo;

import static com.adan.gpsdemo.utils.JZLocationConverter.wgs84ToGcj02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.adan.gpsdemo.databinding.ActivityTestBinding;
import com.adan.gpsdemo.utils.LatLng;

import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Date:2022.10.26  Wednesday
 * reference:
 * Android原生方式获取经纬度和城市信息
 * https://juejin.cn/post/6854573221472272397#heading-2
 *
 * Describe:get longitude and latitude
 * test:
 * pixel4xl: android13,no display
 * huawei nova 5z:harmonyOS 2.0.0,can display
 *
 * google api default is WGS84,like 113°49'54''E,22°36'33''N can be identified
 */
public class TestLocationActivity extends AppCompatActivity {

    public static final int LOCATION_CODE = 301;
    public static final String TAG = "TestLocationActivity:wp";
    private LocationManager locationManager;
    private String locationProvider = null;
    com.adan.gpsdemo.databinding.ActivityTestBinding activityTestBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTestBinding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(activityTestBinding.getRoot());
        getLocation();
    }

    private void getLocation(){
        //1.获取位置管理器
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            Log.v(TAG, "定位方式GPS");
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.v(TAG, "定位方式Network");
        }else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            //获取权限（如果没有开启权限，会弹出对话框，询问是否开启权限）
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                //3.获取上次的位置，一般第一次运行，此值为null
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location!=null){
//                    notice textview change
                    showWGS84Value(location.getLongitude(),location.getLatitude());
                    showGCJ02Value(location.getLongitude(),location.getLatitude());
                    getAddress(location);

                }else{
                    //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                    locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);
                }
            }
        } else {
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location!=null){
                showWGS84Value(location.getLongitude(),location.getLatitude());
                showGCJ02Value(location.getLongitude(),location.getLatitude());
                Log.v(TAG, "获取上次的位置-经纬度："+location.getLongitude()+"   "+location.getLatitude());
                getAddress(location);

            }else{
                //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);
            }
        }
    }

    private void showGCJ02Value(double longitude, double latitude){

        new Thread(()->{
            LatLng latLng = new LatLng(latitude,longitude);
            LatLng reslut = wgs84ToGcj02(latLng);
            Log.i(TAG,"GCJ02:"  +  reslut.getLongitude() + "," + reslut.getLatitude());
            activityTestBinding.tvGcj02Value.setText(String.format("%s\n%s",  reslut.getLongitude(),reslut.getLatitude()));
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void showWGS84Value(double longitude, double latitude){
        new Thread(()->{
//            39.951111, 75.172778 change to  75°10'22''E,39°57'04''N
            int hour = (int) longitude;
            double minute = getDecimalValue(longitude) * 60;
            double second = getDecimalValue(minute) * 60;

            int hour_lat = (int) latitude;
            double minute_lat = getDecimalValue(latitude) * 60;
            double second_lat = getDecimalValue(minute_lat) * 60;

            String strLong = hour + "°" + (int)minute + "'" + (int)second + "\"";
            String strLat = hour_lat + "°" + (int)minute_lat + "'" + (int)second_lat + "\"";
            Log.i(TAG,"WGS84:" + strLong + "E," + strLat + "N");
            activityTestBinding.tvWgs84Value.setText(strLong + "E\n" + strLat + "N");
        }).start();
    }

    /**
     * get decimal of a value
     * @param value double include int and decimal
     * @return decimal of value
     */
    private double getDecimalValue(double value){
        long longPart = (long) value;
        BigDecimal bigDecimal = new BigDecimal(Double.toString(value));
        BigDecimal bigDecimalLongPart = new BigDecimal(Double.toString(longPart));
        double dPoint = bigDecimal.subtract(bigDecimalLongPart).doubleValue();
        Log.i(TAG,"DecimalValue:" + dPoint);
        return dPoint;
    }

    public LocationListener locationListener;

    {
        locationListener = new LocationListener() {
            // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
            @SuppressWarnings("deprecation")
            @Contract(pure = true)
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            // Provider被enable时触发此函数，比如GPS被打开
            @Override
            public void onProviderEnabled(String provider) {
            }

            // Provider被disable时触发此函数，比如GPS被关闭
            @Override
            public void onProviderDisabled(String provider) {
            }

            //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    //如果位置发生变化，重新显示地理位置经纬度
                    showWGS84Value(location.getLongitude(),location.getLatitude());
                    showGCJ02Value(location.getLongitude(),location.getLatitude());
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "申请权限", Toast.LENGTH_LONG).show();
                try {
                    List<String> providers = locationManager.getProviders(true);
                    if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                        //如果是Network
                        locationProvider = LocationManager.NETWORK_PROVIDER;

                    } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                        //如果是GPS
                        locationProvider = LocationManager.GPS_PROVIDER;
                    }
                    Location location = locationManager.getLastKnownLocation(locationProvider);
                    if (location != null) {
                        showWGS84Value(location.getLongitude(),location.getLatitude());
                        showGCJ02Value(location.getLongitude(),location.getLatitude());
                    } else {
                        // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                    }

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "缺少权限", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }


    //获取地址信息:城市、街道等信息
    private void getAddress(Location location) {
        List<Address> result;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                new Thread(()->{
                    String addValue = result.toString();
                    activityTestBinding.tvAddressValue.setText(addValue);
                }).start();
                Log.v(TAG, "获取地址信息："+ result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

}