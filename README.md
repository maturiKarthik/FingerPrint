# FingerPrint

THE CODE WORKS WELL TESTED ON SAMSUNG S7

CHECK ANDROID VERSION DURING RUNTIME 

if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
   // remaining code works here
}
USE FOLLOWING PERMISSION:
---------------------------
    <uses-permission android:name="android.permission.USE_FINGERPRINT"/>
    
    NOTE: 
    -------
    DONOT USE attribute require = ture . 
    Since , the app store will not install on the phone with android version below honeycomb.
    Use atleast two different process of authentification .
    
   
  
