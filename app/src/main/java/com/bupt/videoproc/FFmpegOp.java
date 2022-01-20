package com.bupt.videoproc;

import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.SessionState;

public class FFmpegOp {

    private static final String TAG = "VideoOperation";

    public static void HardwareDecode(String localPath) {
        FFmpegSession session = FFmpegKit.execute("-hide_banner -loglevel debug -benchmark -y -vsync 0 -an" +
                " -c:v h264_mediacodec" +
                " -i " + "/data/local/tmp/netflix_dinnerscene_1080p_60fps_h264.mp4" +
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

    public static void SoftwareDecode(String appPath) {
        String videoPath = "/data/local/tmp/" + "netflix_dinnerscene_1080p_60fps_h264.mp4";
        FFmpegSession session = FFmpegKit.execute("-hide_banner -loglevel verbose -benchmark" +
                " -c:v h264" +
                " -i " + videoPath +
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

    public static void SoftwareEncode(String appPath) {
        String rawFilePath = "/data/local/tmp/Netflix_DinnerScene_1080p_60fps_yuv420p.yuv";
        FFmpegSession session = FFmpegKit.execute("-hide_banner -loglevel verbose -benchmark -vsync 0" +
                " -f rawvideo " +
                "-pix_fmt yuv420p -s:v 1920x1080 -r 60" +
                " -i " + rawFilePath +
                " -c:v h264 -b:v 1492k" +
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

    // TODO
    public static String constructFFmpegCommand() {
        return "";
    }

}
