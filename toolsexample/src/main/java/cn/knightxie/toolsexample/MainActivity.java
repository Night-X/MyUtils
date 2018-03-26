package cn.knightxie.toolsexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cn.knightxie.myutils.utils.OkHttpUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create an OkHttpClient instance from util.
        OkHttpUtils.getOkHttpClient(null);
    }
}
