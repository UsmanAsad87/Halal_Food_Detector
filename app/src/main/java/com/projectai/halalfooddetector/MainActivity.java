package com.projectai.halalfooddetector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button captureBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureBtn=findViewById(R.id.idBtnCapture2);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ExtractText.class);
                startActivity(i);
            }
        });
    }
}