package io.scaledml;


import com.lmax.disruptor.dsl.Disruptor;
import io.scaledml.io.LineBytesBuffer;

public class FtrlProximalRunner {
    Disruptor<LineBytesBuffer> inputDisruptor;
    Disruptor<FtrlProximalState.Increment> incrementDisruptor;

    
}
