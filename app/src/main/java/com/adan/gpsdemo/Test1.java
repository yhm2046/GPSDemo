package com.adan.gpsdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Test1 extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = new Bundle();
        Intent intent = new Intent(Test1.this,Test2.class);
//        传输基本数据
//        bundle.putFloatArray("a",new float[]{(float) 1.0, (float) 2.0});
//        bundle.putIntArray("b",new int[]{3,4});
//        传输自定义类
        SvStatusArrays svStatusArrays = new SvStatusArrays();
        svStatusArrays.setB(new int[]{21,22,23});
        bundle.putParcelable("sv",svStatusArrays);
        intent.putExtra("data",bundle);
        startActivity(intent);
        finish();
    }
}
