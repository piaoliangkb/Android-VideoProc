package com.bupt.videoproc;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

class RawVideoFile {
    public String filename;
    public String type;
    public int eachFrameSize;
    public int frameRate;
    public int totalFrameNum;
    public int width;
    public int height;
    public int bitrate;

    public RawVideoFile(String filename, String type, int eachFrameSize, int frameRate, int totalFrameNum, int width, int height, int bitrate) {
        this.filename = filename;
        if ("h264".equals(type)) {
            this.type = "video/avc";
        } else if ("hevc".equals(type)) {
            this.type = "video/hevc";
        } else {
            this.type = "unknown";
        }
        this.eachFrameSize = eachFrameSize;
        this.frameRate = frameRate;
        this.totalFrameNum = totalFrameNum;
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;  // bits/sec
    }
}


public class MediaCodecOp {

    private static final String TAG = "MediaCodecOp";
    private static final int rawFileColorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;
    private static final RawVideoFile Netflix_DinnerScene_1080p_30fps_1s_h264 = new RawVideoFile(
            "1s_Netflix_DinnerScene_1080p_60fps_yuv420p.yuv",
            "h264",
            3110400,
            30,
            30,
            1920,
            1080,
            1498 * 1000
    );

    private static final RawVideoFile Netflix_DinnerScene_1080p_60fps_1s_h264 = new RawVideoFile(
            "1s_Netflix_DinnerScene_1080p_60fps_yuv420p.yuv",
            "h264",
            3110400,
            60,
            60,
            1920,
            1080,
            1498 * 1000
    );

    private static final RawVideoFile Netflix_DinnerScene_1080p_60fps_2s_h264 = new RawVideoFile(
            "2s_Netflix_DinnerScene_1080p_60fps_yuv420p.yuv",
            "h264",
            3110400,
            60,
            120,
            1920,
            1080,
            1498 * 1000
    );

    private static final RawVideoFile Netflix_DinnerScene_4K_60fps_1s_h264 = new RawVideoFile(
            "1s_Netflix_DinnerScene_4K_60fps_yuv420p.yuv",
            "h264",
            13271040,
            60,
            60,
            4096,
            2160,
            10027 * 1000
    );

    private static final RawVideoFile Netflix_DinnerScene_4K_30fps_1s_h264 = new RawVideoFile(
            "1s_Netflix_DinnerScene_4K_30fps_yuv420p.yuv",
            "h264",
            13271040,
            30,
            30,
            4096,
            2160,
            10027 * 1000
    );

    public static void encodeVideoFromFileAsync(String appPath) {
        RawVideoFile video = Netflix_DinnerScene_1080p_60fps_2s_h264;
        Log.i(TAG, "encodeVideoFromFileAsync: encoding file asynchronously: " + video.filename);
        String MIME_TYPE = video.type;
        double EACH_FRAME_TIME_SLOT = (1000 * 1000) / (double) video.frameRate;  // milliseconds
        MediaCodec encoder;
        MediaMuxer muxer;
        try {
            muxer = new MediaMuxer(appPath + "/test-video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaCodecInfo codecInfo = selectEncCodec(MIME_TYPE);
            if (codecInfo == null) {
                Log.e(TAG, "encodeVideoFromBuffer: unable to find an appropriate codec for: " + MIME_TYPE);
                return;
            }
            Log.i(TAG, "encodeVideoFromFileAsync: codec name: " + codecInfo.getName());

            int colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, video.width, video.height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, video.bitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, video.frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            List<ByteBuffer> frameList = procRawVideoFile(appPath, video);

            encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            int videoTrack = muxer.addTrack(encoder.getOutputFormat());

            encoder.setCallback(new MediaCodec.Callback() {
                final int frameNum = frameList.size();
                int frameIndex = 0;
                int muxerIndex = 0;
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    if (frameIndex < frameNum) {
                        Log.i(TAG, "onInputBufferAvailable: current frame index: " + frameIndex);
                        ByteBuffer inputBuffer = codec.getInputBuffer(index);
                        ByteBuffer frame = frameList.get(frameIndex);
                        frame.rewind();
                        inputBuffer.put(frame);
                        codec.queueInputBuffer(index, 0, video.eachFrameSize, (long) (frameIndex * EACH_FRAME_TIME_SLOT), 0);
                        frameIndex++;
                    } else {
                        Log.i(TAG, "onInputBufferAvailable: enqueue end-of-stream buffer, current frameIndex: " + frameIndex);
                        codec.queueInputBuffer(index, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                    muxer.writeSampleData(videoTrack, outputBuffer, info);
                    codec.releaseOutputBuffer(index, false);
                    Log.i(TAG, "onOutputBufferAvailable: muxerIndex: " + (muxerIndex++));
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });
            muxer.start();
            encoder.start();

            Thread.sleep(10*1000);

            encoder.stop();
            encoder.release();
            muxer.stop();
            muxer.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void encodeVideoFromFileSync(String appPath) {
        RawVideoFile video = Netflix_DinnerScene_1080p_60fps_2s_h264;
        Log.i(TAG, "encodeVideoFromFileSync: encoding: " + video.filename);
        String MIME_TYPE = video.type;  // H.264 or Hevc encoding
        double EACH_FRAME_TIME_SLOT = (1000 * 1000) / (double) video.frameRate;  // milliseconds

        try {
            // Initialize a MediaMuxer
            MediaMuxer muxer = new MediaMuxer(appPath + "/test-video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            // Select codec
            MediaCodecInfo codecInfo = selectEncCodec(MIME_TYPE);
            if (codecInfo == null) {
                Log.e(TAG, "encodeVideoFromBuffer: unable to find an appropriate codec for: " + MIME_TYPE);
                return;
            }
            Log.i(TAG, "encodeVideoFromBuffer: codec name: " + codecInfo.getName());

            // Select colorFormat
            int colorFormat = selectColorFormat(codecInfo, MIME_TYPE);
            Log.i(TAG, "encodeVideoFromBuffer: found colorFormat: " + colorFormat);

            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, video.width, video.height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, video.bitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, video.frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);  // The default KeyFrame interval in FFmpeg is 2 seconds
            Log.i(TAG, "encodeVideoFromBuffer: format: " + format);

            MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            int videoTrack = muxer.addTrack(encoder.getOutputFormat());
            Log.i(TAG, "encodeVideoFromFileSync: ");
            // TODO: we should use InputSurface to pass raw video data? Now to question comes to:
            // how to pass raw video frame bytes to a Surface object?
            // Surface inputSurface = encoder.createInputSurface();  // This can only be called between configure and start method
            muxer.start();
            encoder.start();

            List<ByteBuffer> frameList = procRawVideoFile(appPath, video);
            int frameNum = frameList.size();
            int frameIndex = 0;  // Start from the first frame in frameList
            long st = System.currentTimeMillis(), end;

            while (frameIndex < frameNum) {
                ByteBuffer frame = frameList.get(frameIndex);
                int inputIndex = encoder.dequeueInputBuffer(-1);
                if (inputIndex >= 0) {
                    ByteBuffer byteBuffer = encoder.getInputBuffer(inputIndex);
                    // Fill byteBuffer with raw data read from file
                    frame.rewind();
                    byteBuffer.put(frame);
                    encoder.queueInputBuffer(inputIndex, 0, video.eachFrameSize, (long) (frameIndex * EACH_FRAME_TIME_SLOT), 0);
                    Log.i(TAG, "encodeVideoFromFileSync: input buffer index: " + inputIndex + ", frame: " + frameIndex);
                    frameIndex++;  // Move to the next frame
                }
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 0);  // Set timeoutUs = 0 to avoid block
                Log.i(TAG, "encodeVideoFromFileSync: bufferInfo: " + bufferInfo);
                if (outputIndex >= 0) {
                    // Add outputBuffer to MediaMuxer
                    ByteBuffer outputBuffer = encoder.getOutputBuffer(outputIndex);
                    muxer.writeSampleData(videoTrack, outputBuffer, bufferInfo);

                    encoder.releaseOutputBuffer(outputIndex, false);
                    Log.i(TAG, "encodeVideoFromFileSync: dequeue output buffer index: " + outputIndex);
                }
            }
            // Encode end-of-stream buffer
            int inputIndex = encoder.dequeueInputBuffer(-1);
            if (frameIndex == frameNum) {
                encoder.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }

            // Send unprocessed frames in output buffer to MediaMuxer
            // TODO: These code below will cause 'Moov Atom Not Found' error -- not ended normally
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputIndex = encoder.dequeueOutputBuffer(bufferInfo, -1);  // Block infinite time
            while (outputIndex >= 0) {
                ByteBuffer outputBuffer = encoder.getOutputBuffer(outputIndex);
                muxer.writeSampleData(videoTrack, outputBuffer, bufferInfo);
                encoder.releaseOutputBuffer(outputIndex, false);
                Log.i(TAG, "encodeVideoFromFileSync: dequeue output buffer index after finishing enqueue input buffer: " + outputIndex);
                outputIndex = encoder.dequeueOutputBuffer(bufferInfo, -1);  // Block infinite time
            }

            end = System.currentTimeMillis();
            Log.i(TAG, "encodeVideoFromFileSync: end-to-end encoding time for " + frameNum + " frames: " + (end - st));

            Thread.sleep(1000*10);

            encoder.stop();
            encoder.release();
            muxer.stop();
            muxer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static YuvImage createYuvImage(RawVideoFile video, ByteBuffer buffer) {
        YuvImage image = new YuvImage(buffer.array(), ImageFormat.YUV_420_888, video.width, video.height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 50, out);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream("test-image.jpeg");
            out.writeTo(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Using MediaMuxer object to create a encoded video file
     */
    public static void createEncodeVideoFile() {
        try {
            MediaMuxer muxer = new MediaMuxer("test-video.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat videoFormat = new MediaFormat();
            int videoTrackIndex = muxer.addTrack(videoFormat);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Processing a raw video file in YUV420P pixel format, returns a List object containing ByteBuffer
     * object of each frame.
     *
     * @param appPath: application internal storage path.
     */
    public static List<ByteBuffer> procRawVideoFile(String appPath, RawVideoFile rawVideoFile) {
        String rawPath = appPath + "/" + rawVideoFile.filename;
        int frameSize = rawVideoFile.eachFrameSize;

        File rawFile = new File(rawPath);
        Log.i(TAG, "procRawVideoFile: file length: " + rawFile.length());
        // NOTE: use ArrayList to reduce the get time
        List<ByteBuffer> byteBufferList = new ArrayList<>();
        try {
            FileInputStream is = new FileInputStream(rawPath);
            int i = 0;
            // int totalFrame = 0;
            while (i != -1) {
                // totalFrame++;
                // Log.i(TAG, "procRawVideoFile: frame: " + totalFrame);
                byte[] buf = new byte[frameSize];
                i = is.read(buf);
                if (i != -1) {
                    ByteBuffer buffer = ByteBuffer.wrap(buf);
                    byteBufferList.add(buffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "procRawVideoFile: frame size in byteBufferList: " + byteBufferList.size());
        return byteBufferList;
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
    public static MediaCodecInfo selectEncCodec(String mimeType) {
        MediaCodecInfo[] codecInfoList = new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfoList) {
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
        for (int colorFormat : capabilities.colorFormats) {
            if (colorFormat == rawFileColorFormat) {  // We use YUV420P here
                return colorFormat;
            }
        }
        Log.e(TAG, "selectColorFormat: couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return 0;   // not reached
    }

}
