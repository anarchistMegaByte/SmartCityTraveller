package com.utility.smartcitytraveller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LoginActivity extends AppCompatActivity implements FingerPrintAuthHelper.FingerPrintAuthListener{

    public static final String KEY_STORE_NAME = "keyStoreName";

    Button btnLogin;
    Button btnSignUp;
    TextView tvFacialRecognition;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    HashMap<String, String> userPass = new HashMap<>();
    ImageView ivTryAhain;
    FrameLayout flTryAgain;
    FrameLayout flfingerPrintSensor;
    ImageView ivClose;
    private TextInputEditText etUserName;
    private TextInputEditText etPassword;
    Utility utility = new Utility();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        userPass.put("Sheshadri", "sheshadri123");
        userPass.put("Gaurav", "gaurav123");
        userPass.put("Shreyas", "shreyas123");
        userPass.put("User", "user123");

        etUserName = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        ivClose = findViewById(R.id.iv_close);
        flfingerPrintSensor = findViewById(R.id.fl_finger_print_sensor);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flfingerPrintSensor.setVisibility(View.GONE);
            }
        });

        ivTryAhain = findViewById(R.id.iv_try_again);
        flTryAgain = findViewById(R.id.fl_try_again);

        ivTryAhain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flTryAgain.setVisibility(View.GONE);
//                startFingerPrintAuth();
            }
        });

        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

//        startFingerPrintAuth();



        btnLogin = findViewById(R.id.btn_login);
        tvFacialRecognition = findViewById(R.id.tv_facial_recognition);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = etUserName.getText().toString();
                String password = etPassword.getText().toString();

                SharedPreferences sharedPref =  getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                String dbUserNamePassword = sharedPref.getString( userName, null);
                if (dbUserNamePassword != null) {
                    if (dbUserNamePassword.equals(password)) {
                        utility.saveUserNamePassword(getApplicationContext(), userName, password);
                        utility.markLoggedIn(getApplicationContext());
                        utility.makeDecisionOfWhereToGo(getApplicationContext());
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Password.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User name does not exist. Please Sign up to create a new account.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnSignUp = findViewById(R.id.btn_signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        tvFacialRecognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flfingerPrintSensor.setVisibility(View.VISIBLE);
                startFingerPrintAuth();
            }
        });

        utility.makeDecisionOfWhereToGo(getApplicationContext());
        Log.e("LoginStatuas", utility.isLoggedIn(getApplicationContext()) + "");

    }





    private void startFingerPrintAuth() {
        if (checkFingerPrintInfo()) {
            //get the key
            generateKeyInAndroidKeyStore();
            //initialise the cipher -> used for encryption
            Cipher cipher = initialiseCipherObject();
            //get the cryptoObject
            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
            //get an instance of the finger print helper class and do the authentication
            FingerPrintAuthHelper fingerPrintAuthHelper = new FingerPrintAuthHelper(this, keyStore, this);
            fingerPrintAuthHelper.authenticate(fingerprintManager, cryptoObject);
        }else {
            Toast.makeText(this, "Insufficient Infomation, Can;t continue with the fingerPrint Auto", Toast.LENGTH_LONG).show();
        }
    }

    private Cipher initialiseCipherObject() {
        Cipher cipher = null;
        String transformation = KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7;
        try {
            cipher = Cipher.getInstance(transformation);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            String message = "This is the message i want send.";
            byte[] messageByte = message.getBytes();
            Log.e("Length", messageByte.length + "");
            //get the keystore instance, do we can get a key from it
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_STORE_NAME, null);
            //get the cipher to use this secret key to encrypt the message
            if (cipher != null) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                cipher.doFinal(messageByte);
            }


        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    private void generateKeyInAndroidKeyStore(){
        //get the instance of the android keystore
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            //create an instance of keyGenerator -> used to generate keys in keystore
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            //load the keystore
            keyStore.load(null);
            //generate the key using the keyGenerator, firstly we build the params  required to generate key
            KeyGenParameterSpec keyGenParameterSpec = getKeyGenParamsSpec();

            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private KeyGenParameterSpec getKeyGenParamsSpec() {
        //This uses a builder design pattern
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_STORE_NAME,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
        builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
        builder.setUserAuthenticationRequired(false);
        builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

        return builder.build();
    }

    private boolean checkFingerPrintInfo(){
        //check if the key guard is actively secured
        if (isKeyGuardSecured()){
            Log.i("keyGuardSecurityStatus","secured");
            //check if the fingerPrint hardware is present in the device
            if (fingerprintManager.isHardwareDetected()){
                Log.i("fingerPrintHardware", "present");
                //check if at least one finger print is already registered on the device
                if (fingerprintManager.hasEnrolledFingerprints()){
                    Log.i("enrolled","yes");
                    return true;
                }else {
                    Toast.makeText(this, "goto your device settings, and register a finger print", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(this, "Sorry your device does not have a finger print hardware", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "Set up a password, PIN, or Pattern..", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private boolean isKeyGuardSecured(){
        return keyguardManager.isKeyguardSecure();
    }

    @Override
    public void onSuccessful() {
        utility.markLoggedIn(getApplicationContext());
        utility.makeDecisionOfWhereToGo(getApplicationContext());
    }

    @Override
    public void failed() {
        Log.e("FingerPrintAuth", "Failed");
//        startFingerPrintAuth();
        flTryAgain.setVisibility(View.VISIBLE);
    }

    @Override
    public void error() {
        Log.e("FingerPrintAuth", "Error");
//        startFingerPrintAuth();
        flTryAgain.setVisibility(View.VISIBLE);
    }

}
