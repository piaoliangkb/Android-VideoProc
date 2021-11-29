package com.bupt.videoproc;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FFmpegOp {

    private static final String TAG = "VideoOperation";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);


    public static void HardwareDecode(String localPath) {
        Log.i(TAG, "h264HardwareDecode: localPath is " + localPath);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                FFmpegSession session = FFmpegKit.executeAsync("-loglevel verbose -benchmark -y -vsync 0 -an" +
                        " -hwaccel mediacodec" +
                        " -c:v hevc_mediacodec" +
                        " -i /data/local/tmp/1080p_hevc.mp4" +
//                        " -i " + localPath + "/1080p_h264.mp4" +
                        " -f null -", session1 -> {
                    SessionState state = session1.getState();
                    ReturnCode returnCode = session1.getReturnCode();
                    Log.d(TAG, String.format("FFmpeg process exited with state %s and rc %s.%s", state, returnCode, session1.getFailStackTrace()));
                    Log.i(TAG, "apply: Duration = " + session1.getDuration());
                });

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i(TAG, "ffmpegOperation: session success =====================");
                    Log.i(TAG, "ffmpegOperation: " + session.getOutput());
                    Log.i(TAG, "ffmpegOperation: Duration = " + session.getDuration());
                } else if (ReturnCode.isCancel(session.getReturnCode())) {
                    Log.i(TAG, "ffmpegOperation: session cancel");
                    Log.i(TAG, "ffmpegOperation: " + session.getAllLogsAsString());
                } else {
                    Log.i(TAG, "ffmpegOperation: session failed");
                    Log.d(TAG, String.format("Command failed with state %s and rc %s.%s",
                            session.getState(), session.getReturnCode(), session.getFailStackTrace()));
                }
            }
        });
    }

    public static void SoftwareDecode() {
        FFmpegSession session = FFmpegKit.execute("-y -c:v h264" +
                " -i /data/local/tmp/netflix_dinnerscene_4K_60fps_h264.mp4" +
                " -f null - ");
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session success =====================");
            Log.i(TAG, "ffmpegOperation: " + session.getOutput());
            Log.i(TAG, "ffmpegOperation: Duration = " + session.getDuration());
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session cancel");
            Log.i(TAG, "ffmpegOperation: " + session.getAllLogsAsString());
        } else {
            Log.i(TAG, "ffmpegOperation: session failed");
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s",
                    session.getState(), session.getReturnCode(), session.getFailStackTrace()));
        }
    }

    public static void SoftwareEncode() {
        FFmpegSession session = FFmpegKit.execute("-loglevel verbose -benchmark -y -vsync 0" +
                " -f rawvideo " +
                "-pix_fmt yuv420p -s:v 4096x2160 -r 60" +
                " -i /data/local/tmp/Netflix_DinnerScene_4K_60fps_yuv420p.yuv" +
                " -c:v h264" +
                " -f null -");
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session success =====================");
            Log.i(TAG, "ffmpegOperation: " + session.getOutput());
            Log.i(TAG, "ffmpegOperation: Duration = " + session.getDuration());
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session cancel");
            Log.i(TAG, "ffmpegOperation: " + session.getAllLogsAsString());
        } else {
            Log.i(TAG, "ffmpegOperation: session failed");
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s",
                    session.getState(), session.getReturnCode(), session.getFailStackTrace()));
        }
    }

    public static void showCodecs() {
        FFmpegSession session = FFmpegKit.execute("-h decoder=h264_mediacodec");
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session success =====================");
            Log.i(TAG, "ffmpegOperation: " + session.getOutput());
            Log.i(TAG, "ffmpegOperation: Duration = " + session.getDuration());
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i(TAG, "ffmpegOperation: session cancel");
            Log.i(TAG, "ffmpegOperation: " + session.getAllLogsAsString());
        } else {
            Log.i(TAG, "ffmpegOperation: session failed");
            Log.d(TAG, String.format("Command failed with state %s and rc %s.%s",
                    session.getState(), session.getReturnCode(), session.getFailStackTrace()));
        }
    }

}
