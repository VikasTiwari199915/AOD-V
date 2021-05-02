package com.anvi.aodv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartUpActivity extends AppCompatActivity {
    Button preview, settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_up);

        preview=findViewById(R.id.previewButton);
        settings=findViewById(R.id.settings);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(StartUpActivity.this,MainActivity.class);
                startActivity(i);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(StartUpActivity.this,PreferenceActivity.class);
                startActivity(i);
            }
        });
    }
}