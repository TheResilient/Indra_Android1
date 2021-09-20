package com.example.voicerecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.scwang.wave.MultiWaveHeader;

public class MainActivity extends AppCompatActivity {
    MultiWaveHeader waveHeader, waveFooter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waveHeader=findViewById(R.id.wave_header);
        waveFooter=findViewById(R.id.wave_footer);
        waveHeader.setVelocity(3);
        waveHeader.setProgress(1);
//        waveHeader.isRunning();
        waveHeader.setGradientAngle(45);
        waveHeader.setWaveHeight(40);
        waveHeader.setStartColor(Color.RED);
        waveHeader.setCloseColor(Color.CYAN);

        waveFooter.setVelocity(3);
        waveFooter.setProgress(1);
//        waveFooter.isRunning();
        waveFooter.setGradientAngle(45);
        waveFooter.setWaveHeight(40);
        waveFooter.setStartColor(Color.RED);
        waveFooter.setCloseColor(Color.CYAN);

    }
}
