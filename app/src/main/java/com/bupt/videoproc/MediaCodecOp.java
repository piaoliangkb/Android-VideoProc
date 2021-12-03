package com.bupt.videoproc;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MediaCodecOp {

    private static String TAG = "MediaCodecOp";

    public static void encodeVideoFromFileSync(String appPath) {
        String rawFilePath = appPath + "/Netflix_DinnerScene_4K_60fps_yuv420p.yuv";
        // String rawFilePath = appPath + "/Netflix_DinnerScene_1080p_60fps_yuv420p.yuv";  // Another file with low resolution

        String MIME_TYPE = "video/avc";  // H.264 Video Coding
        int FRAME_RATE = 60;  // fps
        int IFRAME_INTERVAL = 10;  // 10 seconds between I-frames
        int NUM_FRAMES = 1200;  // Total frames

        int WIDTH = 4096;
        int HEIGHT = 2160;
        int BITRATE = (int) 4.8 * WIDTH * HEIGHT;

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

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, WIDTH, HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            Log.i(TAG, "encodeVideoFromBuffer: format: " + format);

            MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            // TODO: What to do here?

            encoder.stop();
            encoder.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extractor frame by frame from an existing video file using MediaExtractor object, feed each
     * frame to decoder's input buffer for decoding, remember to release decoder's output buffer
     * to keep the decoding processing going on.
     *
     * @param appPath: application internal storage path.
     */
    public static void decodeVideoFromFileSync(String appPath) {
        // Test MediaExtractor from current file
        String videoPath = appPath + "/" + "netflix_dinnerscene_1080p_60fps_h264.mp4";
        MediaExtractor extractor = getMediaExtractor(videoPath);

        // Create codec by MIME string (We use the video MIME, in case of H.264 video, the string is video/avc)
        MediaFormat format = extractor.getTrackFormat(0);
        String decoderName = new MediaCodecList(MediaCodecList.ALL_CODECS).findDecoderForFormat(format);
        Log.i(TAG, "testMediaExtractor: the decoder name is" + decoderName);
        try {
            MediaCodec decoder = MediaCodec.createByCodecName(decoderName);
            Log.i(TAG, "testMediaExtractor: " + decoder.getName() + ", " + decoder.getCodecInfo());

            // Configure decoder using MediaFormat object extracted from file
            decoder.configure(format, null, null, 0);

            decoder.start();  // Start decoder

            int frameNum = 0;
            long st = 0, end = 0;
            while (true) {
                int inputIndex = decoder.dequeueInputBuffer(-1);  // Get the index of available input buffer
                Log.i(TAG, "testMediaExtractor: inputIndex: " + inputIndex);

                if (inputIndex >= 0) {
                    ByteBuffer byteBuffer = decoder.getInputBuffer(inputIndex);  // Get byteBuffer by the index of input buffer
                    int size = extractor.readSampleData(byteBuffer, 0);  // Get the size of a sample frame
                    long time = extractor.getSampleTime();  // Get the presentation time of a sample frame
                    Log.i(TAG, "testMediaExtractor: sampleSize: " + size + ", time: " + time);

                    if (time == 0) {
                        st = System.currentTimeMillis();
                    }
                    if (size > 0 && time >= 0) {
                        frameNum++;
                        Log.i(TAG, "testMediaExtractor: The " + frameNum + " frame is processing");
                        decoder.queueInputBuffer(inputIndex, 0, size, time, extractor.getSampleFlags());  // Enqueue a frame saved in inputBuffer at inputIndex
                        extractor.advance();  // Advance to the next sample
                    } else {
                        end = System.currentTimeMillis();
                        // If size <= 0 or time < 0, means that the video is done break execution
                        Log.i(TAG, "testMediaExtractor: decoding finished, exit");
                        decoder.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        break;
                    }
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
                Log.d(TAG, "outIndex: " + outIndex);
                if (outIndex >= 0) {
                    decoder.releaseOutputBuffer(outIndex, false);  // Must release output buffer, else the process of encoding will be paused
                }
            }

            decoder.stop();
            decoder.release();
            extractor.release();
            Log.i(TAG, "testMediaExtractor: end-to-end time in synchronize mode: " + (end - st));  // Pipeline releasing of a output buffer costs at most 1 ms

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use the async approach to enqueue input buffer / dequeue output buffer instantly.
     *
     * @param appPath: application internal storage path.
     */
    public static void decodeVideoFromFileAsync(String appPath) {
        String videoPath = appPath + "/" + "netflix_dinnerscene_1080p_60fps_h264.mp4";
        MediaExtractor extractor = getMediaExtractor(videoPath);

        MediaFormat format = extractor.getTrackFormat(0);
        String decoderName = new MediaCodecList(MediaCodecList.ALL_CODECS).findDecoderForFormat(format);
        try {
            MediaCodec decoder = MediaCodec.createByCodecName(decoderName);
            decoder.setCallback(new MediaCodec.Callback() {
                int frameNum = 0;
                long st, end;

                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    // Fill input buffer with index with frame extracted from extractor
                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                    int size = extractor.readSampleData(inputBuffer, 0);
                    long time = extractor.getSampleTime();
                    if (size > 0 && time >= 0) {
                        if (time == 0) {
                            st = System.currentTimeMillis();
                        }
                        codec.queueInputBuffer(index, 0, size, time, extractor.getSampleFlags());
                        Log.i(TAG, "onInputBufferAvailable: enqueue " + frameNum + " frame with size: " + size + ", time: " + time);
                        extractor.advance();
                        frameNum++;
                    } else {
                        end = System.currentTimeMillis();
                        codec.queueInputBuffer(index, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        Log.i(TAG, "onInputBufferAvailable: total frames: " + frameNum + ", end-to-end time: " + (end - st) + " ms");
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                    Log.i(TAG, "onError: decoder error");
                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                    Log.i(TAG, "onOutputFormatChanged: no processing");
                }
            });

            Log.i(TAG, "decodeVideoFromFileAsync: the decoder name: " + decoderName);
            decoder.configure(format, null, null, 0);
            decoder.start();

            // Wait for processing to complete
            // How to wait for processing finish
            Thread.sleep(10000);

            decoder.stop();
            decoder.release();
            extractor.release();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a MediaExtractor object to track video track.
     *
     * @param videoPath: video path in application local storage.
     * @return A MediaExtractor object.
     */
    private static MediaExtractor getMediaExtractor(String videoPath) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; ++i) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);
                break;
            }
        }
        return extractor;
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
