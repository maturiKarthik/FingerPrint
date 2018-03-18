package com.example.iosdev.fingerprint;
/**
 * Created by Maturikarthik on 18/03/2018.
 */

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateExpiredException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends Activity {
    //DECLARING ALL VARIABLES
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    //private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView display = (TextView)findViewById(R.id.display);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            display.setText("Your Version is Greater Or Equal To HONEYCOMB");
            KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
            FingerprintManager fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);

            if(keyguardManager.isKeyguardLocked() == true){
                Log.d("KM","isKeyguardLocked-LOCKED");
            }else{
                Log.d("KM","isKeyguardLocked-NOT-LOCKED");
            }
            /*
            if(keyguardManager.isDeviceLocked() == true){
                Log.d("KM","DEVICE-LOCKED");

            }
            */
            if(keyguardManager.isDeviceSecure() == true){
                Log.d("KM","DEVICE-SECURED");
            }else{
                Log.d("KM","DEVICE-NOT-SECURED");
            }
            // checking the permission grnated in manifest or not
            if(PermissionChecker.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) == PermissionChecker.PERMISSION_GRANTED){
                Log.d("KM", "PERMISSION-CHECKER-GRANTED-IN-MANIFEST-YES");
            }
            //fingerprintManager to Detect the services
            if(fingerprintManager.isHardwareDetected() == true) {
                    Log.d("KM", "HARWARE-AVAILABLE-YES");
            }
            // Checking wether the finger print already exist
            if(fingerprintManager.hasEnrolledFingerprints()){
                Log.d("KM", "FINGERPRINT-ENROLLED-AVAILABLE-YES");
            }else{
                Log.d("KM", "FINGERPRINT-NOT-ENROLLED-AVAILABLE-NO");
            }

            //Checking for KeyGuard Secure
            if(!keyguardManager.isKeyguardSecure()){
                Log.d("KM", "KEYGUARD-NOTSECURE-NO");
            }else{
                Log.d("KM", "KEYGUARD-SECURE-YES");
                try{
                    generateKey();
                }catch (Exception e){
                    e.printStackTrace();
                }

                try{
                    if (initCipher()) {
                        //If the cipher is initialized successfully, then create a CryptoObject instance//
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);

                        // Here, I’m referencing the FingerprintHandler class that we’ll create in the next section. This class will be responsible
                        // for starting the authentication process (via the startAuth method) and processing the authentication process events//
                        FingerprintHandler helper = new FingerprintHandler(this);
                        helper.startAuth(fingerprintManager, cryptoObject);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }



            }

           // Log.d("SDK","BUILD-SERIAL"+Build.getSerial());
            Log.d("SDK","SDK_VERSION :" + Build.VERSION.SDK_INT);
            Log.d("SDK","RADIO-VERSION"+Build.getRadioVersion());
            Log.d("SDK","MODEL-"+Build.MODEL);
            Log.d("SDK","BRAND-"+Build.BRAND);
            Log.d("SDK","DEVICE-"+Build.DEVICE);
            Log.d("SDK","BOARD-"+Build.BOARD);
            Log.d("SDK","BOOT_LOADER-"+Build.BOOTLOADER);
            Log.d("SDK","DISPLAY-"+Build.DISPLAY);
            Log.d("SDK","FINGERPRINT-"+Build.FINGERPRINT);
            Log.d("SDK","HARDWARE-"+Build.HARDWARE);
        }
    }

    // Function To Generate the key
    private void generateKey() throws Exception {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (Exception exc) {
            exc.printStackTrace();

        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    public boolean initCipher() throws Exception{
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateExpiredException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }


    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }

}
