package com.luffy.mulmedia.codec;

public interface IDecoderStateListener {
    void decoderPrepared(IDecoder baseDecoder);

    void decoderPaused(IDecoder baseDecoder);

    void decoderRunning(IDecoder baseDecoder);

    void decoderFinish(IDecoder baseDecoder);

    void decoderError(IDecoder baseDecoder, int what);

    void decoderDestroy(IDecoder baseDecoder);
}
