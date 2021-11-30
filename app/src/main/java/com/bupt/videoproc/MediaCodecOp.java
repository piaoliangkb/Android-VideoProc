package com.bupt.videoproc;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.util.Log;
import android.view.FrameMetrics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;


public class MediaCodecOp {

    private static String TAG = "MediaCodecOp";

    private static String MIME_TYPE = "video/avc";  // H.264 Video Coding
    private static int FRAME_RATE = 60;  // fps
    private static int IFRAME_INTERVAL = 10;  // 10 seconds between I-frames
    private static int NUM_FRAMES = 1200;  // Total frames

    private static int mWidth = 4096;
    private static int mHeight = 2160;
    private static int mBitRat = (int) 4.8 * mWidth * mHeight;

    public static void encodeVideoFromBuffer() {
        MediaCodec encoder = null;
        Log.i(TAG, "encodeVideoFromBuffer: Hallo");

        try {
            // Select codec
            MediaCodecInfo codecInfo = selectEncCodec(MIME_TYPE);
            if (codecInfo == null) {
                Log.e(TAG, "encodeVideoFromBuffer: unable to find an appropriate codec for " + MIME_TYPE);
                return;
            }
            Log.i(TAG, "encodeVideoFromBuffer: codec name " + codecInfo.getName());

            // Select colorFormat
            int colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
            Log.i(TAG, "encodeVideoFromBuffer: found colorFormat: " + colorFormat);

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRat);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            Log.i(TAG, "encodeVideoFromBuffer: format: " + format);

            encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no
     * match was found.
     */
    private static MediaCodecInfo selectEncCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Returns a color format that is supported by the codec and by this test code.  If no
     * match is found, this throws a test failure -- the set of formats known to the test
     * should be expanded for new platforms.
     */
    private static int selectColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int i = 0; i < capabilities.colorFormats.length; i++) {
            int colorFormat = capabilities.colorFormats[i];
            if (isRecognizedFormat(colorFormat)) {
                return colorFormat;
            }
        }
        Log.e(TAG, "selectColorFormat: couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;   // not reached
    }

    /**
     * Returns true if this is a color format that this test code understands (i.e. we know how
     * to read and generate frames in this format).
     */
    private static boolean isRecognizedFormat(int colorFormat) {
        return colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
    }

}
