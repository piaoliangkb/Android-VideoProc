package com.bupt.videoproc;

import static org.junit.Assert.*;

import org.junit.Test;

public class MediaCodecOpTest {

    @Test
    public void selectEncCodec() {
        String mime_type = "video/avc";
        MediaCodecOp.selectEncCodec(mime_type);
    }
}