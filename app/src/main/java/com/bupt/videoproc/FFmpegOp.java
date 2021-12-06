package com.bupt.videoproc;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;

public class FFmpegOp {

    private static final String TAG = "VideoOperation";

    public static void HardwareDecode(String localPath) {
        Log.i(TAG, "h264HardwareDecode: localPath is " + localPath);
        FFmpegSession session = FFmpegKit.executeAsync("-hide_banner -loglevel debug -benchmark -y -vsync 0 -an" +
                // " -hwaccel mediacodec" +
                " -c:v h264_mediacodec" +
                " -i " + localPath + "/netflix_dinnerscene_1080p_60fps_h264.mp4" +
                // " -i " + localPath + "/dunkrik_1080p_h264.mp4" +
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

    public static void SoftwareDecode(String appPath) {
        String videoPath = appPath + "/netflix_dinnerscene_1080p_60fps_h264.mp4";
        FFmpegSession session = FFmpegKit.execute("-y -c:v h264" +
                " -i " + videoPath +
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

    public static void SoftwareEncode(String appPath) {
        String rawFilePath = appPath + "/Netflix_DinnerScene_4K_60fps_yuv420p.yuv";
        FFmpegSession session = FFmpegKit.execute("-loglevel verbose -benchmark -y -vsync 0" +
                " -f rawvideo " +
                "-pix_fmt yuv420p -s:v 4096x2160 -r 60" +
                " -i " + rawFilePath +
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
