package com.adan.gpsdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.adan.gpsdemo.databinding.ActivityTestBinding;
import com.adan.gpsdemo.utils.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
/**
 * Date:2025.05.21  Wednesday
 * reference:
 * Android source code to get GPS info
 * <a href="https://juejin.cn/post/6854573221472272397#heading-2">...</a>
 *
 * Describe:get longitude and latitude
 * test:
 * pixel,pixel2XL,pixel8:android10,11,15,can display
 * huawei nova 9pro can't display
 *
 * google api default is WGS84,like 113°49'54''E,22°36'33''N can be identified
 */

public class TestLocationActivity extends AppCompatActivity {

    public static final int LOCATION_CODE = 301;
    public static final String TAG = "TestLocationActivity:wp";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private String locationProvider = null;
    ActivityTestBinding activityTestBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityTestBinding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(activityTestBinding.getRoot());

        // 检查定位服务是否开启
        if (!isLocationEnabled()) {
            Toast.makeText(this, "请开启定位服务", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return;
        }

        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_CODE);
        } else {
            getLocation();
        }
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 优先用 Google FusedLocationProviderClient
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    } else {
                        // 主动请求一次实时定位（API 30+ 推荐）
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener(this, loc -> {
                                        if (loc != null) {
                                            updateLocationUI(loc);
                                        } else {
                                            tryLocationManager();
                                        }
                                    });
                        } else {
                            tryLocationManager();
                        }
                    }
                });
    }

    // 兜底用 LocationManager
    private void tryLocationManager() {
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "没有可用的位置提供器", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationProvider);
        if (location != null) {
            updateLocationUI(location);
        } else {
            locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);
        }
    }

    private void updateLocationUI(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        showWGS84Value(longitude, latitude);
        showGCJ02Value(longitude, latitude);
        getAddress(location);
    }

    private void showGCJ02Value(double longitude, double latitude) {
        new Thread(() -> {
            LatLng latLng = new LatLng(latitude, longitude);
            LatLng result = com.adan.gpsdemo.utils.JZLocationConverter.wgs84ToGcj02(latLng);
            Log.i(TAG, "GCJ02:" + result.getLongitude() + "," + result.getLatitude());
            runOnUiThread(() -> activityTestBinding.tvGcj02Value.setText(
                    String.format("%s\n%s", result.getLongitude(), result.getLatitude())));
        }).start();
    }

    private void showWGS84Value(double longitude, double latitude) {
        new Thread(() -> {
            int hour = (int) longitude;
            double minute = getDecimalValue(longitude) * 60;
            double second = getDecimalValue(minute) * 60;

            int hour_lat = (int) latitude;
            double minute_lat = getDecimalValue(latitude) * 60;
            double second_lat = getDecimalValue(minute_lat) * 60;

            String strLong = hour + "°" + (int) minute + "'" + (int) second + "\"";
            String strLat = hour_lat + "°" + (int) minute_lat + "'" + (int) second_lat + "\"";
            Log.i(TAG, "WGS84:" + strLong + "E," + strLat + "N");
            runOnUiThread(() -> activityTestBinding.tvWgs84Value.setText(strLong + "E\n" + strLat + "N"));
        }).start();
    }

    private double getDecimalValue(double value) {
        long longPart = (long) value;
        BigDecimal bigDecimal = new BigDecimal(Double.toString(value));
        BigDecimal bigDecimalLongPart = new BigDecimal(Double.toString(longPart));
        double dPoint = bigDecimal.subtract(bigDecimalLongPart).doubleValue();
        Log.i(TAG, "DecimalValue:" + dPoint);
        return dPoint;
    }

    private final android.location.LocationListener locationListener = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            updateLocationUI(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(@NonNull String provider) {}
        @Override
        public void onProviderDisabled(@NonNull String provider) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "缺少权限", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void getAddress(Location location) {
        new Thread(() -> {
            try {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                List<Address> result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                String addValue = result != null && !result.isEmpty() ? result.get(0).toString() : "未知地址";
                runOnUiThread(() -> activityTestBinding.tvAddressValue.setText(addValue));
                Log.v(TAG, "获取地址信息：" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ignored) {}
        }
    }
}