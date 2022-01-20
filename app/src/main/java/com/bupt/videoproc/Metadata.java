package com.bupt.videoproc;

import android.util.Log;

import java.nio.ByteBuffer;

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
        this.bitrate = bitrate;  // bit per second
    }
}


class FrameInfo {
    FrameInfo(int frameNum, ByteBuffer buffer, long frameSize, int frameFlag, long framePts) {
        this.frameNum = frameNum;
        this.frameBuffer = buffer;
        this.frameSize = frameSize;
        this.frameFlag = frameFlag;
        this.framePts = framePts;
    }

    public void printFrameInfo() {
        Log.d("FrameInfo", "printFrameInfo:" +
                " frameNum=" + frameNum +
                " frameSize=" + frameSize +
                " frameFlag=" + frameFlag +
                " framePts=" + framePts);
    }

    int frameNum;  // # of this frame in a video
    ByteBuffer frameBuffer;
    long frameSize;
    int frameFlag;
    long framePts;
}


public class Metadata {
}
