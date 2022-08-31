package com.adan.gpsdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GPS 测试demo
 */
public class MainActivity extends AppCompatActivity {
    private StringBuffer sb;
    private TextView tvGPS,tvSatellite;
    LocationManager locationManager;
    //    卫星状态监听器
    private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>();
    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

            if (ActivityCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            GpsStatus status = locationManager.getGpsStatus(null);
            String satelliteInfo = updateGpsStatus(event,status);
            tvSatellite.setText(null);
            tvSatellite.setText(satelliteInfo);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvGPS=(TextView)findViewById(R.id.tv_gps);
        tvSatellite=(TextView)findViewById(R.id.tv_satellite);
        openGPSSettings();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
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
        Location location = locationManager.getLastKnownLocation(provider);
        LocationListener ll = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                String locInfo = updateMsg(location);
                Log.i("wxl","location:"+locInfo);
                tvGPS.setText(null);
                tvGPS.setText(locInfo);
            }

        };
        locationManager.requestLocationUpdates(provider,1000,1,ll);
        locationManager.addGpsStatusListener(statusListener);
    }

    /**
     * 更新卫星个数
     * @param event
     * @param status
     * @return
     */
    private String updateGpsStatus(int event,GpsStatus status){
        StringBuffer sb2 = new StringBuffer("");
        if(status == null){
            sb2.append("find statellite count:"+0);
        }else if(event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
            int maxSatelites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext()){
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
            }
            sb2.append("search the satellite count is :"+numSatelliteList.size());
        }
        return sb.toString();
    }

    /**
     * 更新gps信息
     * @param loc
     * @return
     */
    private String updateMsg(Location loc){
        sb = null;
        sb = new StringBuffer("Location info:\n");
        if(loc!=null){
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();
            sb.append("lat:"+lat+"\nlng:"+lng);
            if(loc.hasAccuracy()){
                sb.append("\naccuracy:"+loc.getAccuracy());
            }
        }else {
            sb.append("no info");
        }
        return sb.toString();
    }

    /**
     * 获取gps界面
     */
    private void openGPSSettings(){
        LocationManager alm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(alm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "GPS is right!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "please open GPS!", Toast.LENGTH_SHORT).show();
    }
}