package com.bupt.videoproc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VideoProc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button ffmpegSWEncBt = findViewById(R.id.ffmpeg_sw_enc);
        Button ffmpegSWDecBt = findViewById(R.id.ffmpeg_sw_dec);
        Button ffmpegHWEncBt = findViewById(R.id.ffmpeg_hw_enc);
        Button ffmpegHWDecBt = findViewById(R.id.ffmpeg_hw_dec);
        Button mediacodecEncBt = findViewById(R.id.mediacodec_enc);
        Button mediacodecDecBt = findViewById(R.id.mediacodec_dec);

        ffmpegSWEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg software encoding start");
            }
        });
        
        ffmpegSWDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg software decoding start");
            }
        });
        
        ffmpegHWEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg hardware encoding start");
            }
        });
        
        ffmpegHWDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg hardware decoding start");
            }
        });
        
        mediacodecEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: MediaCodec encoding start");
            }
        });
        
        mediacodecDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: MediaCodec decoding start");
            }
        });
    }


}