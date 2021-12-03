package com.bupt.videoproc;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VideoProc";
    private static ExecutorService service = Executors.newFixedThreadPool(3);


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

        Button test = findViewById(R.id.unit_test);

        /*
         *  Software encoding using FFmpeg
         */
        ffmpegSWEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg software encoding start");
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        FFmpegOp.SoftwareEncode();
                    }
                });
            }
        });

        /*
         * Software decoding using FFmpeg
         */
        ffmpegSWDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg software decoding start");
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        FFmpegOp.SoftwareDecode();
                    }
                });
            }
        });

        /*
         * Hardware encoding using FFmpeg
         */
        ffmpegHWEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg hardware encoding start");
            }
        });

        /*
         * Hardware decoding using FFmpeg
         */
        ffmpegHWDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: FFmpeg hardware decoding start");
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        String localPath = getFilesDir().getAbsolutePath();
                        FFmpegOp.HardwareDecode(localPath);
                    }
                });
            }
        });

        /*
         * Mediacodec encoding
         */
        mediacodecEncBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: MediaCodec encoding start");
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        MediaCodecOp.encodeVideoFromBuffer();
                    }
                });
            }
        });

        /*
         * Mediacodec decoding
         */
        mediacodecDecBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: MediaCodec decoding start");

                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        MediaCodecOp.decodeVideoFromFileAsync(getFilesDir().getAbsolutePath());
                    }
                });
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: Unit test button");
                service.submit(new Runnable() {
                    @Override
                    public void run() {
                        MediaCodecOp.testMediaExtractor(getFilesDir().getAbsolutePath());
                    }
                });
            }
        });
    }


}