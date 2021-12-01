# Android MediaCodec Tutorial

Reference Link: https://developer.android.com/reference/android/media/MediaCodec

## Supported data types

- Compressed data

- Raw audio data

- Raw video data

Using `ByteBuffers` to process all kinds of data. Using `Surface` for raw video data.

## State of a Codec

Uninitialized state (Created by a factory method) => 

Configure => 

Start (Executing state) => 

Dequeue data from input buffer and processing (Running state) => 

Meeting the End of Stream buffer, processing the remaining unfinished data (End-of-Stream State) => 

Back to Execution state (By calling `flush` method) or Uninitialized stat (By calling `stop` method) => 

Release the codec by calling `release` method

## How to create a Codec

### Creating MediaCodec for specific MediaFormat

1. Get the format from `MediaExtractor.getTrackFormat`: https://developer.android.com/reference/android/media/MediaExtractor

2. Get codec name by format using `MediaCodecList.findDecoderForFormat(format)`

3. Create codec by the format type using `MediaCodec.createByCodecName(String)`

## Data processing

1. Fill input buffer with data using `queueInputBuffer`.

2. Get output buffer using `dequeueOutputBuffer`.

3. After processing output buffer, call `releaseOutputBuffer` to return the buffer to the codec.

### Using Input Surface

When using an input Surface, the buffers are automatically passed from the input surface to the codec

### Using Output Surface

When using an output Surface, the output buffers will not be accessible, and are represented as `null` values.

## Stream data boundary

The input data after `start()` and `flush()` should start at a key frame (a key frame can be decoded completely on its own, e.g., I-frame).