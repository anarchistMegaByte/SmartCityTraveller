package com.utility.smartcitytraveller;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import java.security.KeyStore;

public class FingerPrintAuthHelper extends FingerprintManager.AuthenticationCallback{
    private Context context;
    private KeyStore keyStore;
    private FingerPrintAuthListener listener;

    public FingerPrintAuthHelper(Context context, KeyStore keyStore, FingerPrintAuthListener listener) {
        this.context = context;
        this.keyStore = keyStore;
        this.listener = listener;
    }

    /**
     * @param manager This is the fingerPrint manager instance
     * @param cryptoObject This contains all the cryptography params(key, clipper etc)
     * */

    public void authenticate(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){

        CancellationSignal cancellationSignal = new CancellationSignal();
        manager.authenticate(cryptoObject,
                cancellationSignal,
                0,
                this,
                null);

    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        listener.onSuccessful();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        listener.failed();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        listener.error();
    }

    public interface FingerPrintAuthListener{
        void onSuccessful();
        void failed();
        void error();
    }
}