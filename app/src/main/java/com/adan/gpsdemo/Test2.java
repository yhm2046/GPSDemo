package com.adan.gpsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Test2 extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        获取基本数据
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
//        float[]a = bundle.getFloatArray("a");
//        int[]b = bundle.getIntArray("b");
//        for(int i =0;i < a.length;i++){
//            Log.i("wxl","\na: "+a[i]);
//        }
//        for(int i =0;i < b.length;i++){
//            Log.i("wxl","\nb: "+b[i]);
//        }
//获取自定义数据
        SvStatusArrays svStatusArrays = (SvStatusArrays)bundle.getParcelable("sv");
        int b[] = svStatusArrays.getB();
        for(int i = 0;i < b.length;i++){
            Log.i("wxl","\nb[]:"+b[i]);
        }
    }
}
