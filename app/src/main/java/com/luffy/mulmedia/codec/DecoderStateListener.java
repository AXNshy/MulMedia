package com.luffy.mulmedia.codec;

import android.util.Log;

public class DecoderStateListener implements IDecoderStateListener {
    public static final String TAG = "DecoderStateListener";

    @Override
    public void decoderPrepared(IDecoder baseDecoder) {
        baseDecoder.goOn();
    }

    @Override
    public void decoderPaused(IDecoder baseDecoder) {

    }

    @Override
    public void decoderRunning(IDecoder baseDecoder) {

    }

    @Override
    public void decoderFinish(IDecoder baseDecoder) {

    }

    @Override
    public void decoderError(IDecoder baseDecoder, int what) {
        Log.e(TAG, "decoderError " + what);
    }

    @Override
    public void decoderDestroy(IDecoder baseDecoder) {

    }
}
