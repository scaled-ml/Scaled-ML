package io.scaledml;


import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.io.LineBytesBuffer;

import java.nio.ByteBuffer;

public class ParallelRun {
    Disruptor<LineBytesBuffer> inputDisruptor;
    Disruptor<FtrlProximalState.Increment> incrementDisruptor;
}
